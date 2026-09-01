package org.dreamwork.concurrent.broadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Created by seth.yang on 2018/2/11
 */
public class LocalBroadcastWorker implements Runnable {
    private final BlockingQueue<Object> queue = new LinkedBlockingQueue<> ();
    private final String name;

    private final Object QUIT = new byte[0];
    private final Logger logger = LoggerFactory.getLogger (LocalBroadcastWorker.class);
    private final CountDownLatch latch = new CountDownLatch (1);

    private volatile Thread thread;

    public LocalBroadcastWorker (String name) {
        this.name = name;
    }

    public void add (Runnable runner) {
        try {
            queue.put (runner);
        } catch (InterruptedException ex) {
            Thread.currentThread ().interrupt ();
        }
    }

    public void shutdown () {
        try {
            queue.put (QUIT);
        } catch (InterruptedException ex) {
            logger.warn (ex.getMessage (), ex);
        }
        // 等待完成
        try {
            if (!latch.await (30, TimeUnit.SECONDS)) {
                Thread temp = thread;
                if (temp != null) {
                    temp.interrupt ();
                }
                logger.warn ("worker[{}] shutdown timeout", name);
            }
        } catch (InterruptedException ex) {
            Thread.currentThread ().interrupt ();
        }
    }

    @Override
    public void run () {
        thread = Thread.currentThread ();
        thread.setName (name);
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
                Thread.currentThread ().interrupt ();
                break; // 中断即退出，恢复标志
            }
        }
        latch.countDown ();
        logger.info ("worker shutdown");
    }
}