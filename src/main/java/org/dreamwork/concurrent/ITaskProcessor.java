package org.dreamwork.concurrent;

public interface ITaskProcessor<T> {
    void perform (T target) throws Exception;
}