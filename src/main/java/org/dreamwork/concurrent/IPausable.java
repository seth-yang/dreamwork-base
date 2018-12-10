package org.dreamwork.concurrent;

/**
 * 可暂停的接口
 *
 * <p>
 *
 * </p>
 *
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
     * @return
     */
    boolean isPaused ();
}