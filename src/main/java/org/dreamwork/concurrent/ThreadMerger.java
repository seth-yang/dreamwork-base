package org.dreamwork.concurrent;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by game on 2017/4/11
 */
public class ThreadMerger {
    private final Object        LOCKER  = new byte[0];
    private       AtomicInteger counter = new AtomicInteger (0);
    private       boolean       running = false;

    public void acquire () {
        counter.getAndIncrement ();
        synchronized (LOCKER) {
            running = true;
        }
    }

    public void release () {
        counter.decrementAndGet ();
        synchronized (LOCKER) {
            LOCKER.notifyAll ();
        }
    }

    public void abort () {
        counter.set (0);
        synchronized (LOCKER) {
            running = false;
            LOCKER.notifyAll ();
        }
    }

    public int getCount () {
        return counter.get ();
    }

    public void await (long timeout) throws InterruptedException {
        internalWait ();
        synchronized (LOCKER) {
            while (running) {
                int value = counter.get ();
                if (value > 0) {
                    LOCKER.wait (timeout);
                }
            }
        }
    }

    public void await () throws InterruptedException {
        internalWait ();

        while (running) {
            int value = counter.get ();
            if (value > 0) {
                synchronized (LOCKER) {
                    LOCKER.wait ();
                }
            } else {
                running = false;
            }
        }
    }

    public void await (long timeout, TimeUnit unit) throws InterruptedException {
        await (unit.toMillis (timeout));
    }

    private void internalWait () throws InterruptedException {
        synchronized (LOCKER) {
            while (!running) {
                LOCKER.wait ();
            }
        }
    }
}