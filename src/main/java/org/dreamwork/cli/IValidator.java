package org.dreamwork.cli;

/**
 * Created by seth.yang on 2018/11/7
 */
public interface IValidator<T> {
    boolean validate (T value);
}
