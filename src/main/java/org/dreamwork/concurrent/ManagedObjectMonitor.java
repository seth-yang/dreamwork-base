package org.dreamwork.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 受管理可关闭对象的监视器.
 *
 * @param <T>
 */
public class ManagedObjectMonitor<T extends IManagedClosable<?>> {
    private static final String     LOOP_NAME = "MO.Monitor";
    private static final AtomicLong COUNTER   = new AtomicLong (0);

    private final Logger logger = LoggerFactory.getLogger (ManagedObjectMonitor.class);
    private final String name = LOOP_NAME + "." + COUNTER.getAndIncrement ();
    private final Lock locker = new ReentrantLock ();
    private final Condition c = locker.newCondition ();

    protected final List<T> pool = Collections.synchronizedList (new ArrayList<> ());
    protected final AtomicBoolean running = new AtomicBoolean (true);

    protected Listener listener;

    public void add (T o) {
        locker.lock ();
        try {
            pool.add (o);
            c.signalAll ();
        } finally {
            locker.unlock ();
        }
    }

    public void remove (T o) {
        locker.lock ();
        try {
            pool.remove (o);
            if (listener != null) {
                listener.onRemoved (o);
            }
            c.signalAll ();
        } finally {
            locker.unlock ();
        }
    }

    public void setListener (Listener listener) {
        this.listener = listener;
    }

    @SuppressWarnings ("unchecked")
    public void start () {
        if (Looper.exists (name)) {
            throw new IllegalStateException ("loop [" + name + "] already exists!");
        }

        Looper.create (name, 1, 1);
        Looper.runInLoop (name, () -> {
            try {
                if (logger.isTraceEnabled ()) {
                    logger.trace ("monitor[{}] started...", name);
                }
                running.set (true);
                while (running.get ()) {
                    if (pool.isEmpty ()) {
                        if (logger.isTraceEnabled ()) {
                            logger.trace ("pool is empty, wait for it change to non-empty...");
                        }
                        locker.lock ();
                        try {
                            c.await ();
                        } catch (InterruptedException ex) {
                            if (logger.isTraceEnabled ()) {
                                logger.warn (ex.getMessage (), ex);
                            }
                        } finally {
                            locker.unlock ();
                        }
                        if (logger.isTraceEnabled ()) {
                            logger.trace ("i'm wakeup...");
                        }
                    }

                    if (running.get ()) {
                        List<T> copy = new ArrayList<> ();
                        for (IManagedClosable<?> imc : pool) {
                            if (imc.isTimedOut ()) {
                                copy.add ((T) imc);
                            }
                        }

                        if (!copy.isEmpty ()) {
                            if (logger.isTraceEnabled ()) {
                                logger.trace ("there {} items timed out, preparing to remove them.", copy.size ());
                            }
                            pool.removeAll (copy);
                            if (logger.isTraceEnabled ()) {
                                logger.trace ("now pool contains: {}", pool);
                            }

                            for (IManagedClosable<?> imc : copy) {
                                if (logger.isTraceEnabled ()) {
                                    logger.trace ("{} is timed out, trying to close it", imc);
                                }
                                try {
                                    imc.close ();
                                } catch (Exception ex) {
                                    if (logger.isTraceEnabled ()) {
                                        logger.warn (ex.getMessage (), ex);
                                    }
                                } finally {
                                    if (listener != null) {
                                        listener.onClosed ((T) imc);
                                    }
                                }
                            }
                            copy.clear ();
                        }

                        try {
                            Thread.sleep (10);
                        } catch (InterruptedException e) {
                            e.printStackTrace ();
                        }
                    }
                }

                if (logger.isTraceEnabled ()) {
                    logger.trace ("monitor[{}] completed.", name);
                }
            } finally {
                if (logger.isTraceEnabled ()) {
                    logger.trace ("trying to destroy loop: {}", name);
                }
                Looper.destory (name);
                if (logger.isTraceEnabled ()) {
                    logger.trace ("loop [{}] destroyed.", name);
                }
            }
        });
    }

    public void stop () {
        if (logger.isTraceEnabled ()) {
            logger.trace ("trying to stop the monitor...");
        }
        running.set (false);
        locker.lock ();
        try {
            c.signalAll ();
        } finally {
            locker.unlock ();
        }
        if (logger.isTraceEnabled ()) {
            logger.trace ("stop message notified");
        }
    }

    public interface Listener {
        void onClosed (IManagedClosable<?> imc);
        void onRemoved (IManagedClosable<?> imc);
    }
}