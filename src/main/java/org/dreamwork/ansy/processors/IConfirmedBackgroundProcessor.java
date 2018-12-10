package org.dreamwork.ansy.processors;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-7-29
 * Time: 下午12:23
 */
public interface IConfirmedBackgroundProcessor {
    void commit ();
    void rollback ();
    void shutdown ();
    boolean isStopped ();
}