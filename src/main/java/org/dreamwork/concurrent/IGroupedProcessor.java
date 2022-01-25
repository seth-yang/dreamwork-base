package org.dreamwork.concurrent;

/**
 * 分组队列消息处理器
 * @param <K> 队列的分组依据
 * @param <T> 将被压入队列的消息
 */
public interface IGroupedProcessor<K, T> {
    /**
     * 当分组队列首次处理一个分组消息前，会，且只有一次调该方法。
     * 该方法通常被用来处理分组内公共资源的初始化等工作.
     *
     * 注意：分组队列和处理器都是无状态的，实现类不应当默认处理器有状态.
     * @param key 队列的分组依据
     */
    default void setup (K key) {}

    /**
     * 处理分组内的一个消息
     * @param key     分组依据
     * @param message 消息
     */
    void process (K key, T message);

    /**
     * 当分组队列全部 (无论成功或失败) 处理完分组内所有消息后，会且只有一次调用该方法。
     * 该方法通常被用来处理组内公共资源的释放等工作。
     *
     * 注意：分组队列和处理器都是无状态的，实现类不应当默认处理器有状态.
     * @param key 队列的分组依据
     */
    default void cleanup (K key) {}
}