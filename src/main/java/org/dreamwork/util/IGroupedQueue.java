package org.dreamwork.util;

public interface IGroupedQueue<K, T> {
    void remove (K key);
    void offer (K key, T value);
    void dispose ();
}