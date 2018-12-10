package org.dreamwork.util;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 11-8-8
 * Time: 下午6:09
 */
@Deprecated
public interface IJSONConverter<T> {
    public T convert (Class<T> type, String expression);
    public String convert (Class<T> type, T bean);

    public static final class DefaultJSONConverter implements IJSONConverter<Object> {
        public Object convert (Class<Object> type, String expression) {
            return null;
        }

        public String convert (Class<Object> type, Object bean) {
            return null;
        }
    }
}
