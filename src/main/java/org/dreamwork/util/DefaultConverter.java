package org.dreamwork.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
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
    private static final ObjectMapper jackson = JsonHelper.createJackson ();

    public <T> T cast (Class<T> type, String expression) {
        try {
            return jackson.readValue (expression, type);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException (ex);
        }
    }

    public String cast (Object value) {
        try {
            return value == null ? null : jackson.writeValueAsString (value);
        } catch (JsonProcessingException ex) {
            throw new RuntimeException (ex);
        }
    }

    public String cast (Object value, String format) {
        if (value == null) return "";
        Class<?> type = value.getClass ();
        if (type == Boolean.class)
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