package org.dreamwork.concurrent;

import java.util.concurrent.TimeUnit;

/**
 * Created by seth on 16-1-13
 */
public class IOTimer extends Thread {
    private ITimeoutProcessor processor;
    private long timestamp;
    private long timeout;
    private static final long INTERVAL = 20;

    public IOTimer (ITimeoutProcessor processor, int timeout, TimeUnit unit) {
        this.processor = processor;
        this.timeout = unit.toMillis (timeout);
        start ();
    }

    @Override
    public void run () {
        long timestamp = System.currentTimeMillis ();
        while (true) {

        }
    }
}