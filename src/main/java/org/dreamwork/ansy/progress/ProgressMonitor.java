package org.dreamwork.ansy.progress;

import org.dreamwork.util.IDisposable;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-7-29
 * Time: 下午12:27
 */
public class ProgressMonitor extends Thread implements IDisposable {
    private IProgressWatcher watcher;
    private long interval;
    private boolean watching = false, pause = false;

    private final Object locker = new Object ();

    public ProgressMonitor (IProgressWatcher watcher) {
        this (watcher, 200);
    }

    public ProgressMonitor (IProgressWatcher watcher, long interval) {
        this.watcher = watcher;
        this.interval = interval;
    }

    public void watchOn () {
        synchronized (locker) {
            locker.notifyAll ();
        }
    }

    @Override
    public void run () {
        try {
            watching = true;
            while (watching) {
                sleep (interval);
                watcher.updateProgress ();

                if (pause && watching) {
                    synchronized (locker) {
                        locker.wait ();
                    }
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }
    }

    public void dispose () {
        watching = false;
    }

    public void pause () {
        synchronized (locker) {
            pause = true;
        }
    }

    public void goOn () {
        synchronized (locker) {
            pause = false;
            locker.notifyAll ();
        }
    }

    public void shutdown () {
        try {
            if (pause) {
                goOn ();
            }
            watching = false;
        } catch (Exception ex) {
            // ex.printStackTrace ();
        }
    }
}
