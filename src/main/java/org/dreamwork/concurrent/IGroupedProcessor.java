package org.dreamwork.concurrent;

public interface IGroupedProcessor<K, T> {
    default void setup (K key) {}

    void process (T message);

    default void cleanup (K key) {}
}