package org.dreamwork.cli;

/**
 * Created by seth.yang on 2018/11/16
 */
public interface IValueGenerator<T> {
    /**
     * 根据表达式产生一个值
     * @param express 表达式，注意：这个表达式可能为 <code>null</code>
     * @return 产生的值
     */
    T generate (String express);
}
