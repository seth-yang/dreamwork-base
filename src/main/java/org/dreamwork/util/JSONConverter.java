package org.dreamwork.util;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 11-8-9
 * Time: 下午1:00
 */
@Deprecated
public class JSONConverter implements IConverter {
/*    public T convert (Class<T> type, String expression) {
        return null;
    }

    public String convert (Class<T> type, T bean) {
        return new JSConverter ().cast (bean);
    }*/

    public <T> T cast (Class<T> type, String expression) {
        return null;
    }

    public String cast (Object value) {
        return new JSConverter ().cast (value);
    }

    public String cast (Object value, String format) {
        return cast (value);
    }

    public byte[] castToByteArray (Object value) {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }
}
