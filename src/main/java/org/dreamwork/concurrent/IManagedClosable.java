package org.dreamwork.concurrent;

/**
 * 受管理的可关闭对象
 */
public interface IManagedClosable extends AutoCloseable {
    /**
     * 指示是否已超时
     * @return 超时 {@code true}，否在返回 {@code false}
     */
    boolean isTimedOut ();
}
