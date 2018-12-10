package org.dreamwork.concurrent;

/**
 * Created by seth on 16-1-11
 */
public abstract class PausableThread extends CancelableThread implements IPausable {
    protected final Object locker = new Object ();
    volatile protected boolean paused = true;

    protected PausableThread (boolean paused) {
        this.paused = paused;
//        running = true;
        start ();
    }

    protected PausableThread (boolean paused, String name) {
        super (name);
        this.paused = paused;
//        running = true;
        start ();
    }

    @Override
    public void proceed () {
        synchronized (locker) {
            paused = false;
            locker.notifyAll ();
        }
    }

    @Override
    public void pause () {
        paused = true;
    }

    @Override
    public boolean isPaused () {
        return paused;
    }

    @Override
    public synchronized void start () {
        if (!running) {
            running = true;
            super.start ();
        }
    }

    @Override
    public void cancel (boolean block) {
        if (paused)
            proceed ();
        super.cancel (block);
    }

    @Override
    public void run () {
        while (running) {
            while (paused) {
                synchronized (locker) {
                    try {
                        locker.wait ();
                    } catch (InterruptedException e) {
                        e.printStackTrace ();
                    }
                }
            }

            try {
                doWork ();
            } catch (InterruptedException ex) {
                break;
            }
        }
    }
}