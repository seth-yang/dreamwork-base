package org.dreamwork.concurrent;

/**
 * 可暂停的接口
 * Created by seth on 16-1-11
 *
 * @since 2.1.0
 */
public interface IPausable {
    /**
     * 暂停
     */
    void pause ();

    /**
     * 继续
     */
    void proceed ();

    /**
     * 是否暂停
     * @return 如果线程当前状态为暂停返回 true，否则返回 false
     */
    boolean isPaused ();
}