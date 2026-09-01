package org.dreamwork.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 线程管理工具类.
 * <p>
 * 封装了常用的几种线程管理的方式：
 * <ol>
 *     <li>在同一个线程中，安顺序执行一系列任务</li>
 *     <li>不在当前线程中执行一个异步任务</li>
 *     <li>计划任务</li>
 * </ol>
 * <p>
 * 对于 1 而言，首先要调用 <code>{@link #create(String, int)}</code> 方法创建一个 <strong>命名的线程</strong>，以后
 * 可以通过调用 <code>{@link #runInLoop(String, Runnable)}</code> 接口将一个任务添加到该线程中。需要注意的是，向线程中
 * 添加任务 <strong>不会</strong> 阻塞当前线程。被添加到线程中的任务，严格以添加的先后顺序执行。
 * </p>
 * <p>
 * 对于2，在很多场合下，需要临时启动一个异步任务来执行耗时的计算。您可以通过调用 <code>{@link #invokeLater(Runnable)}</code> 方法来
 * 启动异步任务。
 * </p>
 * <p>
 * 对于3，您可以调用 <code>{@link #schedule(Runnable, long, TimeUnit)}</code> 来计划一个任务。该任务无论在执行前，或执行中都可
 * 通过调用 <code>{@link #cancel(long)}</code> 方法来取消。
 * </p>
 * <p>
 * 以上3中方式添加的任务，都可被 <code>{@link #exit()}</code>、<code>{@link #waitForShutdown(int, TimeUnit)}</code>终止。
 * <code>{@link #waitForShutdown()}</code> 也 <strong>可能</strong> 终止它们，但这取决于 <code>任务</code> 的实现。
 * </p>
 * <p>
 * <code>Pool</code> 在 <code>JVM</code> 即将退出时，将 <strong>尽力</strong> 销毁自己所持有的线程及线程池。
 * </p>
 * Created by seth.yang on 2016/3/26
 * @since 2.1.0
 */
@SuppressWarnings ("all")
public class Looper {
    private static final Map<String, InternalLoop>     pool    = new ConcurrentHashMap<> ();
    private static final Map<Long, ScheduledFuture<?>> futures = new ConcurrentHashMap<> ();
    private static final Logger logger                         = LoggerFactory.getLogger (Looper.class);
    private static final Object LOCK                           = new Object ();
    private static final AtomicInteger namedTaskCount          = new AtomicInteger (0);
    private static final AtomicLong taskIdCounter              = new AtomicLong (0);

    private static volatile ExecutorService          executor;
    private static volatile ExecutorService          namedExecutor;
    private static volatile ScheduledExecutorService scheduler;

    static {
        Runtime.getRuntime ().addShutdownHook (new Thread (() -> {
            Thread.currentThread ().setName ("Looper.ShutdownHook");
            exit ();
        }));
    }

    /**
     * 创建一个命名的线程队列.
     * @param name 线程名
     * @param size 同时可容纳任务的数量
     * @see #runInLoop(String, Runnable) runInLoop
     * @see #exists(String) exists
     * @see #destory(String) destory
     */
    public static void create (String name, int size) {
        create (name, size, 1);
    }

    /**
     * 创建一个命名的线程队列.
     * @param name 线程名
     * @param capcity 同时可容纳任务的数量
     * @param threads 同时执行任务的线程数
     * @see #runInLoop(String, Runnable) runInLoop
     * @see #exists(String) exists
     * @see #destory(String) destory
     */
    public static void create (String name, int capcity, int threads) {
        if (name == null || name.trim ().isEmpty ()) {
            throw new IllegalArgumentException ("the looper name must not be null or empty!");
        }
        if (capcity <= 0) {
            throw new IllegalArgumentException ("the looper capacity must be positive!");
        }
        if (threads <= 0) {
            throw new IllegalArgumentException ("the looper threads must be positive!");
        }

        InternalLoop loop = new InternalLoop (name, capcity, threads);
        InternalLoop old = pool.putIfAbsent (name, loop);
        if (old != null) {
            throw new IllegalArgumentException ("the looper: " + name + " already exists!");
        }

        synchronized (LOCK) {
            if (namedExecutor == null || namedExecutor.isShutdown ()) {
                namedExecutor = Executors.newCachedThreadPool (r -> {
                    Thread thread = new Thread (r, "Looper.Named-" + name);
                    thread.setDaemon (true);
                    return thread;
                });
            }
            try {
                namedExecutor.execute (loop);
            } catch (RejectedExecutionException ex) {
                // namedExecutor 刚被其它线程关闭，重建后重试
                namedExecutor = Executors.newCachedThreadPool (r -> {
                    Thread thread = new Thread (r, "Looper.Named-" + name);
                    thread.setDaemon (true);
                    return thread;
                });
                namedExecutor.execute (loop);
            }
            namedTaskCount.incrementAndGet ();
        }
    }

    /**
     * 销毁一个命名的线程队列。重复调用没有任何影响
     * @param name 要销毁的命名线程队列
     *
     * @see #create(String, int) create
     * @see #runInLoop(String, Runnable) runInLoop
     * @see #exists(String) exists
     */
    public static void destory (String name) {
        InternalLoop loop = pool.remove (name);
        if (loop != null) {
            loop.cancel ();
            // 等待 loop 线程真正退出，避免"销毁后立即重建同名 loop"时旧线程误删新实例
            try {
                loop.latch.await ();
            } catch (InterruptedException e) {
                Thread.currentThread ().interrupt ();
            }
            int count = namedTaskCount.decrementAndGet ();
            if (count <= 0) {
                synchronized (LOCK) {
                    if (namedExecutor != null) {
                        namedExecutor.shutdownNow ();
                        namedExecutor = null;
                    }
                    namedTaskCount.set (0);
                }
            }
        }
    }

    /**
     * 在一个预先创建好的命名线程队列中执行一项任务
     * @param name 线程队列的名称
     * @param runner 任务
     * @see #create(String, int) create
     * @see #exists(String) exists
     * @see #destory(String) destory
     */
    public static void runInLoop (String name, Runnable runner) {
        if (runner == null) {
            throw new NullPointerException ("runner must not be null!");
        }
        InternalLoop looper = pool.get (name);
        if (looper == null) {
            throw new IllegalArgumentException ("The looper: " + name + " does not exist!");
        }

        if (logger.isTraceEnabled ()) {
            logger.trace ("submitting a new job to loop [" + name + ']');
            logger.trace ("trying to put a new job into queue, before put, size = {}", looper.queue.size ());
        }
        try {
            looper.queue.put (runner);
        } catch (InterruptedException ex) {
            Thread.currentThread ().interrupt ();
            logger.warn (ex.getMessage (), ex);
        }
        if (logger.isTraceEnabled ()) {
            logger.trace ("after put, size = {}", looper.queue.size ());
        }
    }

    /**
     * 判断指定的命名线程队列是否存在
     * @param name 线程队列名称
     * @return 若存在，返回 <code>true</code>，否则 <code>false</code>
     */
    public static boolean exists (String name) {
        return pool.containsKey (name);
    }

    /**
     * replaced by {@link #invokeLater(Runnable)}
     * @see #invokeLater(Runnable)
     * @param runner the runner
     */
    @Deprecated
    public static void runInOtherLoop (Runnable runner) {
        invokeLater (runner);
    }

    /**
     * 立即执行一项异步任务
     * @param runner 任务
     */
    public static void invokeLater (Runnable runner) {
        if (runner == null) {
            throw new NullPointerException ("runner must not be null!");
        }
        InternalRunner ir = new InternalRunner (runner);
        synchronized (LOCK) {
            if (executor == null || executor.isShutdown () || executor.isTerminated ()) {
                executor = Executors.newFixedThreadPool (16, r -> {
                    Thread thread = new Thread (r, "Looper.InternalExecutor");
                    thread.setDaemon (true);
                    return thread;
                });
            }
            InternalRunner.map.put (ir.index, executor.submit (ir));
        }
    }

    /**
     * 立即终止所有持有的线程、线程池，并停止一切受管理的线程.
     *
     * @see #waitForShutdown()
     * @see #waitForShutdown(int, TimeUnit)
     */
    public static void exit () {
        waitForShutdown (0, null);
    }

    /**
     * 等待所有已提交/计划/正在执行的任务<strong>全部</strong>都完成后，不再接受任何新任务被添加/计划.
     *
     * <p>
     * 通过 <code>{@link #runInLoop(String, Runnable)}</code> 提交的任务，若队列中有多个任务正在等待执行的情况，该方法会等到<strong>所有队列中的所有
     * 任务都完成后</strong>，才销毁线程池。但是在等待任务结束的过程中，将不再接受新的任务被提交。
     * </p>
     *
     * <p>
     * 这个方法 <strong><i style='color:#ff0000'>可能无法终止</i></strong> 通过 <code>{@link #invokeLater(Runnable) invokeLater}</code>
     * 方法提交的任务。这取决于任务本身的实现
     * <pre>
     *     public class MyTask implements Runnable {
     *         public void run () {
     *             while (true) {
     *                 // do somthink
     *             }
     *         }
     *     }
     * </pre>
     * 这种实现的任务，且被 {@link #invokeLater(Runnable) invokeLater} 提交，将不能被该方法终止。因为该方法是 <i>"等待所有任务完成”</i>；很显然，这个
     * 任务自己不会结束。
     * <p>
     * 若您确实需要执行一个"永不终止"的任务，又希望在适当
     * 的时刻将其终止（典型的场景是一个全局的周期性的监视器，一旦启动，就不会关闭，直到上层容器的生命周期结束，比如 ServletContainer 甚至 JVM 退出)的情况，
     * 您可以通过调用 <code>{@link #schedule(Runnable, long, TimeUnit)}</code>，传入适当的延迟，比如 1ms。
     * <pre>
     *     public class MyMonitor {
     *         private long id;
     *
     *         private Runnable work = new Runnable {
     *             public void run () {
     *                 while (true) {
     *                     // do something
     *                 }
     *             }
     *         };
     *
     *         public MyMonitor () {
     *             id = Looper.schedule (worker, 1, TimeUnit.MILLISECONDS);
     *         }
     *
     *         public void shutdown () {
     *             Looper.cancel (id);
     *         }
     *     }
     * </pre>
     * @see #waitForShutdown(int, TimeUnit)
     * @see #exit()
     */
    public static void waitForShutdown () {
        waitForShutdown (-1, null);
    }

    /**
     * 等待所有任务完成，或 超时后强制结束任务
     * <p>最长等 <code>timeout</code> 个 <code>unit</code>，在该时间超时后，若仍有任务未完成将被强制完成或取消</p>
     * 参数 <code>timeout</code> 的取值决定了这个方法的行为
     * <ul>
     *     <li>当 timeout &lt; 0 时，该方法将无限等待，和 <code>{@link #waitForShutdown()}</code> 无参的版本行为完全一致</li>
     *     <li>当 timeout == 0 时，该方法立即强制完成或取消任务，和 <code>{@link #exit()}</code> 行为完全一致</li>
     *     <li>当 timeout &gt; 0 时，进行正常等待</li>
     * </ul>
     *
     * 事实上，<strong>无参版本</strong> 和 <code>exit</code> 就是直接调用该方法的。
     *
     * @param timeout 超时时间，允许为 <strong>负数</strong> 和 <strong>0</strong>
     * @param unit 时间单位
     */
    public static void waitForShutdown (int timeout, TimeUnit unit) {
        if (logger.isTraceEnabled ()) {
            logger.trace ("waiting for the all loops shutdown");
        }

        if (timeout < 0) {
            ExecutorService e = executor;
            if (e != null) e.shutdown ();
            ScheduledExecutorService s = scheduler;
            if (s != null) s.shutdown ();
            ExecutorService n = namedExecutor;
            if (n != null) n.shutdown ();
        } else {
            if (timeout > 0 && unit != null) {
                try {
                    Thread.sleep (unit.toMillis (timeout));
                } catch (InterruptedException e) {
                    Thread.currentThread ().interrupt ();
                }
            }

            for (ScheduledFuture<?> sf : futures.values ()) {
                sf.cancel (true);
            }
            futures.clear ();

            for (Future<?> future : InternalRunner.map.values ()) {
                future.cancel (true);
            }
            InternalRunner.map.clear ();

            for (InternalLoop loop : pool.values ()) {
                loop.cancel ();
            }
            pool.clear ();

            ExecutorService e = executor;
            if (e != null) e.shutdownNow ();
            ScheduledExecutorService s = scheduler;
            if (s != null) s.shutdownNow ();
            ExecutorService n = namedExecutor;
            if (n != null) n.shutdownNow ();
        }

        // 无论哪种分支，都把池引用置空，保证之后可以重新创建新的池
        synchronized (LOCK) {
            executor = null;
            scheduler = null;
            namedExecutor = null;
            namedTaskCount.set (0);
        }
        pool.clear ();
        futures.clear ();
    }

    /**
     * 计划一个将来执行的任务.
     *
     * <p>返回的任务ID可以在将来调用 {@link #cancel(long)} 来取消任务</p>
     * @param runner 任务
     * @param delay  延迟时长
     * @param unit   延迟时长单位
     * @return 任务ID
     *
     * @see #cancel(long) cancel
     */
    public static long schedule (Runnable runner, long delay, TimeUnit unit) {
        if (runner == null) {
            throw new NullPointerException ();
        }
        if (unit == null) {
            unit = TimeUnit.MILLISECONDS;
        }

        long taskId = taskIdCounter.incrementAndGet ();
        ScheduleWorker worker = new ScheduleWorker (runner);
        worker.taskId = taskId;
        synchronized (LOCK) {
            if (scheduler == null || scheduler.isShutdown ()) {
                scheduler = new ScheduledThreadPoolExecutor (32, r -> {
                    Thread thread = new Thread (r, "Looper.Scheduler");
                    thread.setDaemon (true);
                    return thread;
                });
            }
            futures.put (taskId, scheduler.schedule (worker, delay, unit));
            if (logger.isTraceEnabled ()) {
                logger.trace ("task [" + taskId + "] scheduled executing in " + unit.toMillis (delay) + " ms.");
            }
        }
        return taskId;
    }

    /**
     * 取消一个计划任务
     * <p>无论这个任务是在等待，或正在执行中</p>
     * @param taskId 任务ID
     * @see #schedule(Runnable, long, TimeUnit)
     */
    public static void cancel (long taskId) {
        ScheduledFuture<?> future = futures.remove (taskId);
        if (future != null) {
            future.cancel (true);
            if (logger.isTraceEnabled ()) {
                logger.trace ("task [" + taskId + "] canceled.");
            }
        }
        synchronized (LOCK) {
            if (futures.isEmpty () && scheduler != null) {
                scheduler.shutdownNow ();
                scheduler = null;
            }
        }
    }

    private static final class InternalRunner implements Runnable {
        private final Runnable runner;
        private static final AtomicLong count = new AtomicLong (0);
        private static final Map<Long, Future<?>> map = new ConcurrentHashMap<> ();

        final long index;

        InternalRunner (Runnable runner) {
            this.runner = runner;
            index = count.incrementAndGet ();
        }

        @Override
        public void run () {
            Thread.currentThread ().setName ("Looper.InternalRunner." + index);
            try {
                runner.run ();
            } catch (Throwable t) {
                logger.warn (t.getMessage (), t);
            } finally {
                map.remove (index);
                if (logger.isTraceEnabled ())
                    logger.trace ("task done.");
            }
        }
    }

    private static final class InternalLoop implements Runnable {
        static final Object FINISH = new Object ();
        private final BlockingQueue<Object> queue;
        private final CountDownLatch latch = new CountDownLatch (1);
        private final String name;
        private final int threads;
        private final ThreadGroup group;
        private final AtomicInteger counter = new AtomicInteger (1);
        private final ExecutorService service;

        private volatile boolean running = true;
        private volatile long timeout;
        private volatile TimeUnit unit;

        private InternalLoop (String name, int size) {
            this (name, size, 1);
        }

        private InternalLoop (String name, int capcity, int threads) {
            this.name = name;
            this.threads = threads;
            group = new ThreadGroup (name);
            queue = new ArrayBlockingQueue<Object> (capcity);

            if (logger.isTraceEnabled ()) {
                logger.trace ("name = {}, count = {}, size = {}", name, threads, capcity);
            }

            service = Executors.newFixedThreadPool (threads, r -> {
                String threadName = name + "." + counter.getAndIncrement ();
                if (logger.isTraceEnabled ()) {
                    logger.trace ("thread-name = {}", threadName);
                }
                Thread thread = new Thread (group, r, threadName);
                // 避免阻止jvm退出
                thread.setDaemon (true);
                return thread;
            });
        }

        public InternalLoop timeout (int timeout, TimeUnit unit) {
            this.timeout = timeout;
            this.unit    = unit;
            return this;
        }

        @Override
        public void run () {
            Thread.currentThread ().setName (name);
            if (logger.isTraceEnabled ())
                logger.trace (">>>>>>> Internal Loop run <<<<<<<<<");
            try {
                while (running) {
                    Object o;
                    try {
                        if (timeout > 0 && unit != null) {
                            o = queue.poll (timeout, unit);
                        } else {
                            o = queue.take ();
                        }
                    } catch (InterruptedException ex) {
                        logger.warn ("i'm interrupted.");
                        break;
                    }
                    if (o == FINISH) {
                        break;
                    } else if (o instanceof Runnable) {
                        final Runnable task = (Runnable) o;
                        try {
                            service.execute (() -> runTask (task));
                        } catch (RejectedExecutionException ex) {
                            // service 已被关闭，说明本 loop 正在被销毁，丢弃剩余任务
                            logger.warn ("the looper [" + name + "] is shutting down, drop the pending task.");
                            break;
                        }
                    }
                }
            } finally {
                running = false;
                group.interrupt ();
                service.shutdownNow ();
                try {
                    service.awaitTermination (Long.MAX_VALUE, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    Thread.currentThread ().interrupt ();
                }
                // 仅当 pool 中对应 name 的值仍是本实例时才移除，避免误删"销毁后重建"的同名 looper
                pool.remove (name, this);
                latch.countDown ();
                if (logger.isTraceEnabled ()) {
                    logger.trace (name + " removed.");
                    logger.trace (">>>>>>> Internal Loop done <<<<<<<<<");
                }
            }
        }

        private static void runTask (Runnable task) {
            if (logger.isTraceEnabled ()) {
                logger.trace ("executing the runner...");
            }
            try {
                task.run ();
            } catch (Throwable t) {
                logger.warn (t.getMessage (), t);
            }
            if (logger.isTraceEnabled ()) {
                logger.trace ("the job done.");
            }
        }

        public void cancel () {
            running = false;
            service.shutdownNow ();
            for (int i = 0; i < threads; i ++) {
                queue.offer (FINISH);
            }
        }
    }

    private static final class ScheduleWorker implements Runnable {
        final Runnable runner;
        long taskId;
        private static final AtomicInteger count = new AtomicInteger (0);

        ScheduleWorker (Runnable runner) { this.runner = runner; }

        @Override
        public void run () {
            Thread.currentThread ().setName ("Looper.ScheduleWorker." + (count.incrementAndGet ()));
            try {
                runner.run ();
            } catch (Throwable t) {
                logger.warn (t.getMessage (), t);
            } finally {
                futures.remove (taskId);
            }
        }
    }
}
