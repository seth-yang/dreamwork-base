package org.dreamwork.concurrent;

/**
 * Created by seth on 16-1-13
 */
public interface ITimeoutProcessor {
    void touch ();
    void timeout ();
}