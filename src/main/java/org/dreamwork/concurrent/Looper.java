package org.dreamwork.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 线程管理工具类.
 * <p/>
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
    private static final Map<String, InternalLoop>     pool    = new HashMap<> ();
    private static final Map<Long, ScheduledFuture<?>> futures = new HashMap<> ();
    private static final Logger logger = LoggerFactory.getLogger (Looper.class);
    private static final ReentrantReadWriteLock locker         = new ReentrantReadWriteLock ();

    private static ExecutorService          executor  = Executors.newFixedThreadPool (16);
    private static ExecutorService          namedExecutor = Executors.newCachedThreadPool ();
    private static ScheduledExecutorService monitor   = new ScheduledThreadPoolExecutor (1);
    private static ScheduledExecutorService scheduler = new ScheduledThreadPoolExecutor (32);

    static {
        Runtime.getRuntime ().addShutdownHook (new Thread (Looper::exit));
    }

    /**
     * 创建一个命名的线程队列.
     * @param name 线程名
     * @param size 同时可容纳任务的数量
     * @see #runInLoop(String, Runnable) runInLoop
     * @see #exists(String) exists
     * @see #destory(String) destory
     */
    public synchronized static void create (String name, int size) {
        create (name, size, 1);
/*
        if (pool.containsKey (name)) {
            throw new IllegalArgumentException ("the looper: " + name + " already exists!");
        }

        InternalLoop loop = new InternalLoop (name, size);
        synchronized (pool) {
            pool.put (name, loop);
            namedExecutor.execute (loop);
        }
*/
    }

    /**
     * 创建一个命名的线程队列.
     * @param name 线程名
     * @param size 同时可容纳任务的数量
     * @param count 同时执行任务的线程数
     * @see #runInLoop(String, Runnable) runInLoop
     * @see #exists(String) exists
     * @see #destory(String) destory
     */
    public synchronized static void create (String name, int size, int count) {
        if (pool.containsKey (name)) {
            throw new IllegalArgumentException ("the looper: " + name + " already exists!");
        }

        InternalLoop loop = new InternalLoop (name, size, count);
        synchronized (pool) {
            pool.put (name, loop);
            namedExecutor.execute (loop);
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
    public synchronized static void destory (String name) {
        if (pool.containsKey (name)) {
            InternalLoop loop = pool.get (name);
            loop.cancel ();

            pool.remove (name);
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
        if (!pool.containsKey (name)) {
            throw new IllegalArgumentException ("The looper: " + name + " does not exist!");
        }

        if (logger.isTraceEnabled ()) {
            logger.trace ("submitting a new job to loop [" + name + ']');
        }
        synchronized (pool) {
            InternalLoop looper = pool.get (name);
            try {
                looper.queue.put (runner);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
    }

    /**
     * 判断指定的命名线程队列是否存在
     * @param name 线程队列名称
     * @return 若存在，返回 <code>true</code>，否则 <code>false</code>
     */
    public synchronized static boolean exists (String name) {
        return pool.containsKey (name);
    }

    /**
     * replaced by {@link #invokeLater(Runnable)}
     * @see #invokeLater(Runnable)
     * @param runner the runner
     */
    @Deprecated
    public static void runInOtherLoop (Runnable runner) {
//        executor.execute (runner);
        invokeLater (runner);
    }

    /**
     * 立即执行一项异步任务
     * @param runner 任务
     */
    public synchronized static void invokeLater (Runnable runner) {
        if (executor == null || executor.isShutdown () || executor.isTerminated ()) {
            executor = Executors.newFixedThreadPool (16);
        }
        if (logger.isTraceEnabled ()) {
            logger.trace ("submitting a new job to non-named loop...");
        }
        InternalRunner ir = new InternalRunner (runner);
        InternalRunner.map.put (ir.index, executor.submit (ir));
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
     * </p>
     * <p>
     * 若您确实需要执行一个“永不终止”的任务，有希望在适当
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
     * </p>
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
            executor.shutdown ();
            monitor.shutdown ();
            scheduler.shutdown ();
            namedExecutor.shutdown ();

/*
            while (!pool.isEmpty ()) {
                for (InternalLoop loop : pool.values ()) {
                    if (loop.queue.isEmpty ()) {
                        loop.cancel ();
                    }
                }
                try {
                    Thread.sleep (1);
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
            }
*/
        } else {
            if (timeout > 0 && unit != null) {
                synchronized (pool) {
                    try {
                        pool.wait (unit.toMillis (timeout));
                    } catch (InterruptedException e) {
                        e.printStackTrace ();
                    }
                }
            }

            for (ScheduledFuture<?> sf : futures.values ()) {
                sf.cancel (true);
            }

            for (Future<?> future : InternalRunner.map.values ()) {
                future.cancel (true);
            }

            for (InternalLoop loop : pool.values ()) {
                loop.cancel ();
            }

            executor.shutdownNow ();
            monitor.shutdownNow ();
            scheduler.shutdownNow ();
            namedExecutor.shutdownNow ();
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

        long taskId = (((long) runner.hashCode ()) << 32) ^ System.currentTimeMillis ();
        ScheduleWorker worker = new ScheduleWorker (runner);
        worker.taskId = taskId;
        futures.put (taskId, scheduler.schedule (worker, delay, unit));
        if (logger.isTraceEnabled ()) {
            logger.trace ("task [" + taskId + "] scheduled executing in " + unit.toMillis (delay) + " ms.");
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
        synchronized (futures) {
            if (futures.containsKey (taskId)) {
                futures.get (taskId).cancel (true);
                futures.remove (taskId);
                if (logger.isTraceEnabled ()) {
                    logger.trace ("task [" + taskId + "] canceled.");
                }
            }
        }
    }

    private static final AtomicLong counter = new AtomicLong (0);
//    private static ScheduledFuture future;

    private static final class InternalRunner implements Runnable {
        private Runnable runner;
        private static final AtomicLong count = new AtomicLong (0);
        private static final Object LOCKER = new byte [0];
        private static final Map<Long, Future<?>> map = new HashMap<> ();

        long index;

        InternalRunner (Runnable runner) {
            this.runner = runner;
            index = count.incrementAndGet ();
        }

        @Override
        public void run () {
            Thread.currentThread ().setName ("Looper.InternalRunner." + index);
            try {
                runner.run ();
            } finally {
                map.remove (index);
                count.decrementAndGet ();
                if (logger.isTraceEnabled ())
                    logger.trace ("task done.");
            }
        }
    }

    private static final class InternalLoop implements Runnable {
        static final Object FINISH = new byte [0];
        private final BlockingQueue<Object> queue;

        private String name;
        private boolean running = true;
        private long timeout;
        private TimeUnit unit;
        private ExecutorService service;
        private ThreadGroup group;
        private AtomicInteger counter = new AtomicInteger (1);

        private InternalLoop (String name, int size) {
            this (name, size, 1);
        }

        private InternalLoop (String name, int size, int count) {
            this.name = name;
            group = new ThreadGroup (name);
            queue = new ArrayBlockingQueue<Object> (size);

            System.out.printf ("name = %s, count = %d, size = %d%n", name, count, size);

            service = Executors.newFixedThreadPool (count, r -> {
                String threadName = name + "." + counter.getAndIncrement ();
                System.out.println ("thread-name = " + threadName);
                return new Thread (group, r, threadName);
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
            while (running) {
                try {
                    Object o;
                    if (timeout > 0 && unit != null) {
                        o = queue.poll (timeout, unit);
                    } else {
                        o = queue.take ();
                    }
                    if (o == FINISH) {
                        break;
                    } else if (o instanceof Runnable) {
                        service.execute ((Runnable) o);
/*
                        service.execute (() -> {
                            try {
                                ((Runnable) o).run ();
                            } catch (Throwable t) {
                                t.printStackTrace ();
                            }
                        });
*/
                    }
                } catch (InterruptedException ex) {
                    logger.warn ("i'm interrupted.");
                    break;
                }
            }

            pool.remove (name);
            service.shutdown ();
            while (!service.isTerminated ()) {
                try {
                    Thread.sleep (1);
                } catch (InterruptedException e) {
                    //
//                    e.printStackTrace ();
                }
            }
            group.destroy ();

            if (logger.isTraceEnabled ()) {
                logger.trace (name + " removed.");
                logger.trace (">>>>>>> Internal Loop done <<<<<<<<<");
            }
        }

        public void cancel () {
            running = false;
            queue.offer (FINISH);
        }
    }

    private static final class ScheduleWorker implements Runnable {
        Runnable runner;
        long taskId;
        private static volatile int count = 0;

        ScheduleWorker (Runnable runner) { this.runner = runner; }

        @Override
        public void run () {
            Thread.currentThread ().setName ("Looper.ScheduleWorker." + (++ count));
            try {
                runner.run ();
            } finally {
                synchronized (futures) {
                    if (futures.containsKey (taskId)) {
                        futures.remove (taskId);
                    }
                }
            }
        }
    }

    public static void main (String[] args) {
        Looper.create ("test", 1, 2);
        for (int i = 0; i < 3; i ++) {
            final int index = i;
            Looper.runInLoop ("test", () -> {
                String name = Thread.currentThread ().getName ();
                int time = (int) (Math.random () * 3 + 10) * 1000;
                info (name, String.format ("[%s] waiting for %d sec....", "runner-" + index, time));
                try {
                    Thread.sleep (time);
                } catch (InterruptedException e) {
                    info (name, "interrupted");
//                    e.printStackTrace ();
                }
                info (name, "done!");
            });
        }
        Looper.destory ("test");
        Looper.waitForShutdown ();
    }

    private static void info (String name, String message) {
        SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
        System.out.printf ("[%s][%s] - %s%n", name, sdf.format (System.currentTimeMillis ()), message);
    }
}