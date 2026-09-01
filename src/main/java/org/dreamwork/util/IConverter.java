package org.dreamwork.util;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2010-12-9
 * Time: 23:53:43
 */
public interface IConverter {
    <T> T cast (Class<T> type, String expression);
    String cast (Object value);
    String cast (Object value, String format);
    byte[] castToByteArray (Object value);
}