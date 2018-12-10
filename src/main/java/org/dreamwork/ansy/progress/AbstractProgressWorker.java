package org.dreamwork.ansy.progress;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-7-29
 * Time: 下午12:27
 */
public abstract class AbstractProgressWorker<T> implements IProgressWorker {
    protected IProgressWatcher<T> watcher;
    protected ProgressMonitor monitor;

    public AbstractProgressWorker (IProgressWatcher<T> watcher) {
        this.watcher = watcher;
        monitor = new ProgressMonitor (watcher);
    }

    protected void fail (int code, String reason) {
        watcher.fail (code, reason);
        monitor.shutdown ();
    }
}