package org.dreamwork.concurrent.broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by seth.yang on 2018/2/11
 */
public class LocalBroadcastWorker implements Runnable {
    private final BlockingQueue<Object> queue = new LinkedBlockingQueue<> ();
    private final String name;
    private final Object locker = new byte[0];

    private final Object QUIT = new byte[0];
    private final Logger logger = LoggerFactory.getLogger (LocalBroadcastWorker.class);

    public LocalBroadcastWorker (String name) {
        this.name = name;
    }

    public void add (Runnable runner) {
        queue.offer (runner);
    }

    public void shutdown () {
        queue.offer (QUIT);
        synchronized (locker) {
            try {
                locker.wait ();
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
    }

    @Override
    public void run () {
        Thread.currentThread ().setName (name);
        logger.info ("worker started");
        while (true) {
            try {
                Object o = queue.take ();
                if (o == QUIT) {
                    break;
                }

                try {
                    ((Runnable) o).run ();
                } catch (Throwable ex) {
                    logger.warn (ex.getMessage (), ex);
                }
            } catch (InterruptedException e) {
                logger.warn (e.getMessage (), e);
            }
        }
        logger.info ("worker shutdown");
        synchronized (locker) {
            locker.notifyAll ();
        }
    }
}