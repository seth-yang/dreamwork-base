package org.dreamwork.util;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2010-12-9
 * Time: 23:53:43
 */
public interface IConverter {
    public<T> T cast (Class<T> type, String expression);
    public String cast (Object value);
    public String cast (Object value, String format);
    public byte[] castToByteArray (Object value);
}