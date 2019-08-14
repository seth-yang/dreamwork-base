package org.dreamwork.compilation;

import org.dreamwork.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by seth.yang on 2017/6/27
 */
public class VirtualAnnotation extends VirtualType<VirtualAnnotation> {
    private static final String NO_VALUE   = "<NO_VALUE>";

    private String value;
    private Map<String, Object> parameters = new HashMap<> ();

    public VirtualAnnotation (String name) {
        setName (name);
    }

    public VirtualAnnotation setValue (Object value) {
        this.value = cast (value);
        return this;
    }

    @SuppressWarnings ("unchecked")
    public VirtualAnnotation addParameter (String name, Object value) {
        if (value == null) {
            parameters.put (name, NO_VALUE);
        } else {
            Object o = parameters.get (name);
            if (o == null) {
                parameters.put (name, cast (value));
            } else {
                Class<?> oc = o.getClass ();
                List list;
                if (List.class.isAssignableFrom (oc)) {
                    list = (List) o;
                } else {
                    list = new ArrayList ();
                    list.add (o);
                    parameters.put (name, list);
                }
                list.add (cast (value));
            }
        }

        return this;
    }

    @Override
    public VirtualAnnotation setStill (boolean still) {
        throw new NotSupportedException ();
    }

    @Override
    public VirtualAnnotation setFin (boolean fin) {
        throw new NotSupportedException ();
    }

    @Override
    public VirtualAnnotation setAbs (boolean abs) {
        throw new NotSupportedException ();
    }

    private String cast (Object value) {
        if (value == null) {
            return NO_VALUE;
        }
        Class<?> c = value.getClass ();
        if (c.isPrimitive () || Number.class.isAssignableFrom (c) || c == Boolean.class) {
            return value.toString ();
        } else if (c == String.class) {
            return '"' + value.toString ().replace ("\"", "\\\"") + '"';
        } else if (c.isEnum ()) {
            Enum e = (Enum) value;
            Class ec = e.getDeclaringClass ();
            return ec.getCanonicalName () + '.' + e.toString ();
        } else if (c == VirtualClass.class) {
            return ((VirtualClass) value).getReferenceName () + ".class";
        } else if (c == Class.class) {
            return ((Class) value).getName () + ".class";
        }

        throw new RuntimeException ("unknown type: " + c);
    }

    @Override
    public String toString () {
        StringBuilder builder = new StringBuilder ("@").append (name);
        if (!StringUtil.isEmpty (value)) {
            builder.append (" (").append (value).append (')');
        } else if (!parameters.isEmpty ()) {
            builder.append (" (");
            int index = 0;
            for (String key : parameters.keySet ()) {
                if (index > 0) {
                    builder.append (", ");
                }
                Object o = parameters.get (key);
                Class<?> c = o.getClass ();
                if (c == String.class) {
                    builder.append (key).append (" = ").append (o);
                } else {
                    builder.append (key).append (" = {");
                    List list = (List) o;
                    for (int j = 0; j < list.size (); j ++) {
                        if (j > 0) {
                            builder.append (", ");
                        }
                        builder.append (list.get (j));
                    }
                    builder.append ('}');
                }

                index ++;
            }
            builder.append (')');
        }
        return builder.toString ();
    }
}