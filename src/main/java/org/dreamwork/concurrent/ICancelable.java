package org.dreamwork.concurrent;

/**
 * <p/>
 * Created by seth on 16-1-11
 * @since 2.1.0
 */
public interface ICancelable {
    /**
     * 是否已经取消
     * @return 若已取消，返回 <code>true</code>，否则返回 <code>false</code>
     */
    boolean isCanceled ();

    /**
     * 取消
     * @param block 是否阻塞当前线程
     * @throws InterruptedException
     */
    void cancel (boolean block) throws InterruptedException;
}