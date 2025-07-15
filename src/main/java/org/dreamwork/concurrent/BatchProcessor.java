package org.dreamwork.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 这是一个批处理器，用于批处理数据.
 * <p>批处理器允许往队列中添加数据，在将来的某个<strong>合适</strong>的时机，对这批数据经行集中处理。
 * 合理使用批处理器可以将<strong>零散</strong>且<strong>实时性不敏感</strong>的数据进行集中处理，
 * 比如日志数据的落盘，从而降低IO压力</p>
 * <p>批处理器可以由两个变量来控制其执行时机: <strong>容量</strong> 和 <strong>超时时间</strong></p>
 * <ul>
 * <li>当队列容量满时</li>
 * <li>或，尽管队列未满，但超时时间到达时</li>
 * </ul>
 * <p>都将触发批处理器的{@link #process(List) 动作}</p>
 * 示例代码:
 * <pre>
 * public static void main (String... args) {
 *     final String name = "test-processor";
 *     final int timeout = 60000;   // 1 minute
 *     final int size = 32;
 *     final int workers = 8, count = 16, total = workers * count;
 *     final CountDownLatch latch = new CountDownLatch (1);

 *     BatchProcessor&lt;String&gt; p = new BatchProcessor&lt;String&gt; (name, size, timeout) {
 *         int processedCount = 0;
 *         &#64;Override
 *         protected void process (List&lt;String&gt; data) {
 *             processedCount += data.size ();
 *             // some code to consume the data
 *             if (processedCount &lt;= total) {
 *                 latch.countDown ();
 *             }
 *         }
 *     };
 *     ExecutorService executor = Executors.newCachedThreadPool ();
 *     p.start (executor);
 *
 *     for (int i = 0; i &lt; workers; i ++) {
 *         executor.execute (() -&gt; {
 *             for (int j = 0; j &lt; count; j ++) {
 *                 p.add (String.format ("[%02d] - message #%02d", i + 1, j + 1));
 *             }
 *         });
 *     }
 *     executor.shutdown ();
 *     latch.await ();
 *     p.dispose (true);
 * }
 * </pre>
 * <p>必须在一个合适的时机调用批处理其的{@link #dispose(boolean) 销毁}方法，否则它将一直在后台执行</p>
 * <p>Created by seth.yang on 2018/11/5</p>
 */
public abstract class BatchProcessor<T> implements Runnable {
    private static final long BLOCK_TIME = 60000;
    private final Logger logger = LoggerFactory.getLogger (BatchProcessor.class);

    private final int     size;
    private final int     timeout;
    private final String  name;
    private volatile boolean running = true, stopping = false;
    private final List<T> data;
    private final Lock locker = new ReentrantLock ();
    private final Condition notFull = locker.newCondition (),   // 用于插入等待
                            transfer = locker.newCondition (),  // 用于复制等待
                            stopped  = locker.newCondition ();  // 用于停止等待
    private volatile boolean transferring = false;

    /**
     * 构造函数，创建一个指定名称、容量 和 超时时间 的批处理器
     * @param name    批处理器的名称
     * @param size    批处理器的队列大小
     * @param timeout 超时时间，毫秒
     */
    public BatchProcessor (String name, int size, int timeout) {
        this (name, size, size, timeout);
    }

    /**
     * 构造函数，创建一个指定名称、初始容量、容量 和 超时时间 的批处理器
     * @param name     批处理器的名称
     * @param capacity 队列的初始大小
     * @param size     批处理器的队列大小
     * @param timeout  超时时间，毫秒
     */
    public BatchProcessor (String name, int capacity, int size, int timeout) {
        this.name    = name;
        this.size    = size;
        this.timeout = timeout;

        data = new ArrayList<> (capacity);
    }

    /**
     * 向批处理器中添加一个目标元素.
     * <p>当队列容量未满时，直接向队列中添加该元素；否则将阻塞当前线程，并唤醒处理线程，
     * 将缓存队列中的数据移动到处理队列中，然后恢复当前线程执行，向缓存队列中添加目标元素</p>
     * @param target 目标元素
     */
    public void add (T target) {
        if (!stopping) {
            locker.lock ();
            try {
                while (transferring) {
                    notFull.await ();
                }

                data.add (target);
                if (data.size () >= size && !transferring) {
                    if (logger.isTraceEnabled ()) {
                        logger.warn ("{} data needs to be copied.", data.size ());
                    }
                    transferring = true;
                    transfer.signalAll ();
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException (ex);
            } finally {
                locker.unlock ();
            }
        }
    }

    /**
     * 在一个 ExecutorService 中开启处理线程.
     * <p>批处理器本身并不管理和维护线程池</p>
     * @param executor 线程池
     */
    public void start (ExecutorService executor) {
        executor.execute (this);
    }

    /**
     * 销毁这个批处理器.
     * <p>当参数{@code block} 被设置为 {@code true} 时，将阻塞当前线程，直到缓存中的数据被处理完成；或者超时</p>
     * @param block 是否阻塞到缓存中的所有数据被处理完
     */
    public void dispose (boolean block) {
        running = false;
        locker.lock ();
        try {
            notFull.signalAll ();
            transferring = true;
            transfer.signalAll ();
        } finally {
            locker.unlock ();
        }

        if (block) {
            locker.lock ();
            try {
                logger.info ("waiting for shutdown progress completed.");
                if (stopping) {
                    if (stopped.await (BLOCK_TIME, TimeUnit.MILLISECONDS)) {
                        logger.trace ("shutdown normally");
                    } else {
                        logger.trace ("shutdown forced");
                    }
                }
            } catch (InterruptedException ex) {
                logger.warn (ex.getMessage (), ex);
            } finally {
                locker.unlock ();
            }
        }

        if (logger.isInfoEnabled ()) {
            logger.info ("the processor has been terminated.");
        }
    }

    @Override
    public void run () {
        Thread.currentThread ().setName (name);
        if (logger.isTraceEnabled ()) {
            logger.trace ("setting thread name to {}", name);
        }
        if (logger.isInfoEnabled ()) {
            logger.info ("batch processor:{} started", name);
        }

        List<T> copy = null;

        while (running) {
            locker.lock ();
            try {
                while (!transferring && data.isEmpty ()) {
                    if (transfer.await (timeout, TimeUnit.MILLISECONDS)) {
                        if (!data.isEmpty () && logger.isTraceEnabled ()) {
                            logger.trace ("the buff is full, process the data ...");
                        }
                    } else if (logger.isTraceEnabled ()) {
                        logger.trace ("timed out, process the {} data now remains ...", data.size ());
                    }
                }

                if (running) {
                    copy = new ArrayList<> (data);
                    data.clear ();
                    transferring = false;
                    notFull.signalAll ();
                }
            } catch (InterruptedException ex) {
                logger.warn (ex.getMessage (), ex);
            } finally {
                locker.unlock ();
            }

            if (copy != null && !copy.isEmpty ()) {
                try {
                    process (copy);
                } catch (Exception ex) {
                    logger.warn (ex.getMessage (), ex);
                } finally {
                    copy.clear ();
                }
            }
        }

        if (logger.isInfoEnabled ()) {
            logger.info ("the process thread break, trying to shutdown thread");
        }

        if (!data.isEmpty ()) {
            if (logger.isTraceEnabled ()) {
                logger.trace ("there's some data remains, process them!");
            }

            locker.lock ();
            try {
                stopping = true;
                copy = new ArrayList<> (data);
                data.clear ();
                if (!copy.isEmpty ()) {
                    try {
                        process (copy);
                        transferring = false;
                        stopped.signalAll ();
                    } catch (Exception ex) {
                        logger.warn (ex.getMessage (), ex);
                    }
                }
            } finally {
                locker.unlock ();
            }
        }

        if (logger.isInfoEnabled ()) {
            logger.info ("all jobs done. trying to notify the dispose thread.");
            logger.info ("the dispose thread notified.");
        }
    }

    /**
     * 委托的数据消费方法，由子类提供具体的数据消费实现.
     * <p>批处理器会在执行数据消费前，将缓存队列的数据移动到这个处理队列中，这个方法的实现无论任何操作都将不会影响缓存队列</p>
     * <p>您需要大致评估这个方法的实现的用时和构造函数参数{@code timeout}的关系，通常需要保持timeout参数大于这个方法的执行时间</p>
     * @param data 复制出来的数据队列.
     * @throws Exception 任何错误
     */
    protected abstract void process (List<T> data) throws Exception;
}