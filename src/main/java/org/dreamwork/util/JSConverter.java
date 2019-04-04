package org.dreamwork.util;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 11-8-8
 * Time: 下午5:16
 */
@Deprecated
public class JSConverter {
    @SuppressWarnings ("unchecked")
    public String cast (Object value) {
        if (value == null) return "";
        Class type = value.getClass ();

        ConverterInfo ci = ReferenceUtil.getAnnotation (type, ConverterInfo.class);
        if (ci != null) {
            Class<? extends IConverter> cc = ci.converter ();
            if (cc != DefaultConverter.class) {
                try {
                    IConverter c = cc.newInstance ();
                    return c.cast (value);
                } catch (Exception e) {
                    throw new ClassCastException (e.getMessage ());
                }
            }
        }

        DefaultConverter converter = new DefaultConverter ();

        if (value instanceof CharSequence) return "'" + jsEncode (value.toString ()) + "'";
        if (value instanceof Date) return "'" + converter.cast (value, null) + "'";
        if (ReferenceUtil.isKnownType (type)) return value.toString ();

        if (value instanceof Map)
            return castMap ((Map<Object, Object>) value);
        if (value instanceof Collection)
            return castCollection ((Collection<Object>) value);
        if (value instanceof IDataCollection)
            return castDataCollection ((IDataCollection<Object>) value);
        if (type.isArray ()) {
            int length = Array.getLength (value);
            List<Object> list = new ArrayList<Object> (length);
            for (int i = 0; i < length; i ++)
                list.add (Array.get (value, i));
            return castCollection (list);
        }

        try {
            return castNormalObject (type, value);
        } catch (Exception e) {
            throw new ClassCastException (e.getMessage ());
        }
    }

    private String castMap (Map<Object, Object> map) {
        StringBuilder builder = new StringBuilder ();
        for (Object key : map.keySet ()) {
            if (builder.length () > 0) builder.append (',');
            builder.append (key).append (':').append (cast (map.get (key)));
        }
        return "{" + builder + '}';
    }

    private String castCollection (Collection<Object> c) {
        StringBuilder builder = new StringBuilder ();
        for (Object o : c) {
            if (builder.length () > 0) builder.append (',');
            builder.append (cast (o));
        }
        return "[" + builder + "]";
    }

    private String castDataCollection (IDataCollection<Object> dc) {
        StringBuilder builder = new StringBuilder ();

        builder.append ("{pageNo:").append (dc.getPageNo ())
               .append (",pageSize:").append (dc.getPageSize ())
               .append (",totalRows:").append (dc.getTotalRows ())
               .append (",totalPages:").append (dc.getTotalPages ())
               .append (",data:").append (castCollection (dc.getData ()))
               .append ("}");
        return builder.toString ();
    }

    @SuppressWarnings ("unchecked")
    private String castNormalObject (Class type, Object o) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        StringBuilder builder = new StringBuilder ();
        Collection<String> propNames = ReferenceUtil.getPropertyNames (type);
        for (String name : propNames) {
            if (builder.length () > 0) builder.append (',');
            Object v = ReferenceUtil.get (o, name);

            if (v == null) continue;

            String expression = null;
            ConverterInfo ci = ReferenceUtil.getAnnotation (type, ConverterInfo.class, name);
            if (ci != null) {
                Class<? extends IConverter> cc = ci.converter ();
                if (cc != DefaultConverter.class) {
                    IConverter c = cc.newInstance ();
                    expression = c.cast (v);
                }
            }

            if (expression == null) expression = cast (v);
            builder.append (name).append (':').append (expression);
        }
        return "{" + builder + '}';
    }

    private String jsEncode (String text) {
        return text.replace ("'", "\\'");
    }
}
