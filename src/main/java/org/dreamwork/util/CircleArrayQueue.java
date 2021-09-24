package org.dreamwork.util;

import java.util.Arrays;

/**
 * 循环数组队列
 * @param <T>
 */
public class CircleArrayQueue<T> {
    private final T[] data;
    private int head, tail, size;

    @SuppressWarnings ("unchecked")
    public CircleArrayQueue (int capacity) {
        data = (T[]) new Object[capacity];
    }

    public synchronized void push (T item) {
        if (size < data.length) {
            data[tail ++] = item;
            size ++;
            if (tail == data.length) {
                tail = 0;
            }
        } else {
            throw new RuntimeException ("queue full.");
        }
    }

    public synchronized T take () {
        if (size > 0) {
            T value = data[head];
            data[head] = null;
            head ++;
            size --;
            if (head == data.length) {
                head = 0;
            }
            return value;
        }
        throw new RuntimeException ("empty queue");
    }

    public synchronized T header () {
        return size == 0 ? null : data[head];
    }

    public int size () {
        return size;
    }

    @Override
    public String toString () {
        return "{head=" + head + ",tail=" + tail + ",size=" + size + ",data=" + Arrays.toString (data) + "}";
    }

    public void reset () {
        Arrays.fill (data, null);
        head = tail = size = 0;
    }
}