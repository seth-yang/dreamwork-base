package org.dreamwork.ansy.progress;

import org.dreamwork.util.IDisposable;
import org.dreamwork.util.ThreadHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-7-29
 * Time: 下午12:27
 */
public class ProgressMonitor extends Thread implements IDisposable {
    private final Logger logger = LoggerFactory.getLogger (ProgressMonitor.class);

    private final IProgressWatcher<?> watcher;
    private final long interval;
    private boolean watching = false, pause = false;

    private final Object locker = new Object ();

    public ProgressMonitor (IProgressWatcher<?> watcher) {
        this (watcher, 200);
    }

    public ProgressMonitor (IProgressWatcher<?> watcher, long interval) {
        this.watcher = watcher;
        this.interval = interval;
    }

    @SuppressWarnings ("unused")
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
                ThreadHelper.delay (interval);
                watcher.updateProgress ();

                while (pause && watching) {
                    synchronized (locker) {
                        locker.wait (interval);
                    }
                }
            }
        } catch (InterruptedException ex) {
            logger.warn (ex.getMessage (), ex);
        }
    }

    public void dispose () {
        watching = false;
    }

    @SuppressWarnings ("unused")
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
