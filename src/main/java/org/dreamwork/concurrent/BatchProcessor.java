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
 * Created by seth.yang on 2018/11/5
 */
public abstract class BatchProcessor<T> implements Runnable {
    private static final long BLOCK_TIME = 60000;
    private final Logger logger     = LoggerFactory.getLogger (BatchProcessor.class);

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

    public BatchProcessor (String name, int capacity, int size, int timeout) {
        this.name       = name;
        this.size       = size;
        this.timeout    = timeout;

        data = new ArrayList<> (capacity);
    }

    public void add (T target) {
        if (!stopping) {
            locker.lock ();
            try {
                while (transferring) {
                    notFull.await ();
                }

                data.add (target);
                if (data.size () >= size && !transferring) {
                    logger.warn ("{} data needs to be copied.", data.size ());
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

    public void start (ExecutorService executor) {
        executor.execute (this);
    }

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
                        if (!data.isEmpty ()) {
                            logger.trace ("the buff is full, process the data ...");
                        }
                    } else {
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

        if (logger.isTraceEnabled ()) {
            logger.trace ("all jobs done. trying to notify the dispose thread.");
            logger.trace ("the dispose thread notified.");
        }
    }


    protected abstract void process (List<T> data) throws Exception;
}