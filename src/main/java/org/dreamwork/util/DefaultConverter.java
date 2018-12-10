package org.dreamwork.util;

import com.google.gson.Gson;
import org.dreamwork.gson.GsonHelper;

import java.io.*;
import java.security.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2010-12-9
 * Time: 23:56:09
 */
public class DefaultConverter extends AbstractConverter {
    private static final SimpleDateFormat stf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss");
    private static final SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd");
    private static Gson g = GsonHelper.getGson ();

    @SuppressWarnings ("unchecked")
    public <T> T cast (Class<T> type, String expression) {
        return g.fromJson (expression, type);
/*
        if (expression == null) return null;
        expression = expression.trim ();
        if (type == String.class || type == Object.class)
            return (T) expression;
        if (type == Integer.class || type == int.class)
            return (T) Integer.valueOf (expression.trim ());
        if (type == Double.class || type == double.class)
            return (T) Double.valueOf (expression.trim ());
        if (type == Long.class || type == long.class)
            return (T) Long.valueOf (expression.trim ());
        if (type == Short.class || type == short.class)
            return (T) Short.valueOf (expression);
        if (type == Character.class || type == char.class)
            return (T) Character.valueOf ((char) (expression.charAt (0) - '0'));
        if (type == Byte.class || type == byte.class)
            return (T) Byte.valueOf (expression);
        if (type == Boolean.class || type == boolean.class)
            return (T) Boolean.valueOf (expression);
        if (Date.class.isAssignableFrom (type)) {
            try {
                return (T) stf.parse (expression);
            } catch (Exception ex) {
                try {
                    return (T) sdf.parse (expression);
                } catch (Exception e) {
                    try {
                        return (T) new Date (Long.parseLong (expression));
                    } catch (Exception e2) {
                        throw new ClassCastException ("can't cast '" + expression + "' to java.util.Date");
                    }
                }
            }
        }
        if (type == BigDecimal.class) return (T) new BigDecimal (expression);
        if (type == BigInteger.class) return (T) new BigInteger (expression);

        throw new ClassCastException ("can't cast '" + expression + "' to " + type.getName ());
*/
    }

    public String cast (Object value) {
/*
        if (value == null) return "";
        if (value.getClass () == String.class) return (String) value;
        return value.toString ();
*/
        return g.toJson (value);
    }

    public String cast (Object value, String format) {
        if (value == null) return "";
        Class<?> type = value.getClass ();
        if (type == Boolean.class || type == boolean.class)
            return (value + "").toLowerCase ();
        if (Date.class.isAssignableFrom (type)) {
            try {
                return new SimpleDateFormat (format).format (value);
            } catch (Exception ex) {
                if (Timestamp.class.isAssignableFrom (type))
                    return stf.format (value);
                else
                    return sdf.format (value);
            }
        }
        return value.toString ();
    }

    public byte[] castToByteArray (Object value) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream ();
            ObjectOutputStream dos = new ObjectOutputStream (baos);
            if (value instanceof Serializable) {
                dos.writeObject (value);
                dos.flush ();
                return baos.toByteArray ();
            }
        } catch (IOException ioe) {
            throw new ClassCastException ("can't cast '" + value + "' to byte array");
        }
        throw new ClassCastException ("can't cast '" + value + "' to byte array");
    }
}