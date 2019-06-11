package org.dreamwork.ansy.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-7-29
 * Time: 下午12:30
 */
public abstract class AbstractConfirmedBackgroundProcessor extends Thread implements IConfirmedBackgroundProcessor {
    protected long waitingTime;
    protected boolean canceled = true, stopped = false;
    protected final Object locker = new Object ();
    public static final Logger logger = LoggerFactory.getLogger (AbstractConfirmedBackgroundProcessor.class);

    protected abstract void doRollback ();
    protected abstract void doCommit ();

    protected AbstractConfirmedBackgroundProcessor (long waitingTime) {
        this.waitingTime = waitingTime;
    }

    @Override
    public void run () {
        try {
            synchronized (locker) {
                if (logger.isTraceEnabled ())
                    logger.trace ("waiting for {} ms ...", waitingTime);
                locker.wait (waitingTime);
            }

            if (logger.isTraceEnabled ())
                logger.trace ("current operation: {}", (canceled ? "rollback" : "commit"));
            if (canceled)
                doRollback ();
            else
                doCommit ();
            stopped = true;
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public void rollback () {
        synchronized (locker) {
            canceled = true;
            locker.notifyAll ();
        }
    }

    @Override
    public void commit () {
        synchronized (locker) {
            canceled = false;
            locker.notifyAll ();
        }
    }

    public boolean isStopped () {
        return stopped;
    }

    @Override
    public void shutdown () {
        this.interrupt ();
    }
}