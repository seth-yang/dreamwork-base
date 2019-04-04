package org.dreamwork.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * Created by seth.yang on 2018/11/5
 */
public abstract class BatchProcessor<T> implements Runnable {
    private static final long BLOCK_TIME = 60000;
    private final Object LOCKER     = new byte[0];
    private final Object LOCKBACK   = new byte[0];
    private final Logger logger     = LoggerFactory.getLogger (BatchProcessor.class);

    private int     size;
    private int     timeout;
    private String  name;
    private boolean running = true;
    private List<T> data;

    public BatchProcessor (String name, int capacity, int size, int timeout) {
        this.name       = name;
        this.size       = size;
        this.timeout    = timeout;

        data = new ArrayList<> (capacity);
    }

    public void add (T target) {
        boolean local_flag = false;
        synchronized (LOCKER) {
            if (data.size () < size) {
                data.add (target);
            } else {
                LOCKER.notifyAll ();
                local_flag = true;
            }
        }

        if (local_flag) {
            synchronized (LOCKBACK) {
                try {
                    LOCKBACK.wait ();
                } catch (InterruptedException e) {
                    e.printStackTrace ();
                }
            }
            synchronized (LOCKER) {
                data.add (target);
            }
        }
    }

    public void start (ExecutorService executor) {
        executor.execute (this);
    }

    public void dispose (boolean block) {
        synchronized (LOCKER) {
            running = false;
            LOCKER.notifyAll ();
        }

        if (block) {
            long start = 0;
            if (logger.isTraceEnabled ()) {
                start = System.currentTimeMillis ();
            }
            synchronized (LOCKBACK) {
                try {
                    LOCKBACK.wait (BLOCK_TIME);          // wait for top to 60 seconds, and kill the thread.
                } catch (InterruptedException ex) {
                    if (logger.isTraceEnabled ()) {
                        logger.warn (ex.getMessage (), ex);
                    }
                }
            }
            if (logger.isTraceEnabled ()) {
                long end = System.currentTimeMillis ();
                long delta = end - start;
                if (delta < BLOCK_TIME) {
                    logger.trace ("shutdown normally");
                } else {
                    logger.trace ("shutdown forced");
                }
            }
        }
    }

    @Override
    public void run () {
        Thread.currentThread ().setName (name);
        if (logger.isTraceEnabled ()) {
            logger.trace ("setting thread name to " + name);
        }
        if (logger.isInfoEnabled ()) {
            logger.info ("batch processor:" + name + " started");
        }
        while (running) {
            while (running && data.isEmpty ()) {
                synchronized (LOCKER) {
                    try {
                        LOCKER.wait (timeout);
                    } catch (InterruptedException ex) {
                        if (logger.isTraceEnabled ()) {
                            logger.warn (ex.getMessage (), ex);
                        }

                        break;
                    }
                }
            }

            if (running) {
                process ();
            }
        }

        if (logger.isInfoEnabled ()) {
            logger.info ("the process thread break, trying to shutdown thread");
        }
        if (!data.isEmpty ()) {
            if (logger.isTraceEnabled ()) {
                logger.trace ("there's some data remain, process them!");
            }
            process ();
        }

        if (logger.isTraceEnabled ()) {
            logger.trace ("all jobs done. trying to notify the dispose thread.");
        }
        synchronized (LOCKBACK) {
            LOCKBACK.notifyAll ();
        }
        if (logger.isTraceEnabled ()) {
            logger.trace ("the dispose thread notified.");
        }
    }

    private void process () {
        List<T> copy = copy ();
        if (logger.isInfoEnabled ()) {
            logger.trace ("copied data: " + copy);
        }
        if (copy != null) {
            try {
                process (copy);
            } catch (Exception ex) {
                logger.warn (ex.getMessage (), ex);
            }
        }
    }

    private List<T> copy () {
        List<T> copy = null;
        synchronized (LOCKER) {
            if (!data.isEmpty ()) {
                copy = new ArrayList<> (data);
                data.clear ();
            }
        }
        synchronized (LOCKBACK) {
            LOCKBACK.notifyAll ();
        }

        return copy;
    }

    protected abstract void process (List<T> data) throws Exception;
}