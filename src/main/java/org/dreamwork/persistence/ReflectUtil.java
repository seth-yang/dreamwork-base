package org.dreamwork.persistence;

import org.dreamwork.util.StringUtil;

import java.lang.reflect.Field;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by seth.yang on 2017/6/16
 */
public class ReflectUtil {
    private Map<Class<?>, Map<String, Field>> cache = new HashMap<> ();
    private Map<Class<?>, Class<? extends DatabaseSchema>> mapping = new HashMap<> ();
    private final SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd", Locale.getDefault ());
    private final SimpleDateFormat stf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss", Locale.getDefault ());

    public Object[] getInsertParameters (DatabaseSchema schema, Object item, Class<?> type) throws IllegalAccessException {
        Map<String, Field> fields = getTypedList (type);

        Object[] values = new Object[fields.size ()];
        for (int i = 0; i < schema.fields.length; i ++) {
            String name = schema.fields [i];
            Field field = fields.get (name);
            if (field == null) {
                values [i] = null;
            } else {
                ISchemaField sf = field.getAnnotation (ISchemaField.class);
                if (sf.id ()) {
                    Class<?> cls = field.getType ();
                    if ((cls == int.class || cls == Integer.class ||
                            cls == long.class || cls == Long.class) && sf.autoincrement ()) {
                        values[i] = null;
                    } else {
                        values [i] = getValue (field, item);
                    }
                } else {
                    values [i] = getValue (field, item);
                }
            }
        }

        return values;
    }

    public Object getValue (Field field, Object item) throws IllegalAccessException {
        Object o = field.get (item);
        if (o == null) {
            return null;
        }

        Class<?> type = o.getClass ();
        if (type == java.sql.Timestamp.class) {
            return stf.format (o);
        } else if (java.util.Date.class.isAssignableFrom (type)) {
            return sdf.format (o);
        }

        return o;
    }

    public Map<String, Field> getTypedList (Class<?> type) {
        if (cache.containsKey (type)) {
            return cache.get (type);
        }

        List<Field> fields = getAllFields (type);
        Map<String, Field> map = new HashMap<> ();
        for (Field field : fields) {
            ISchemaField sf = field.getAnnotation (ISchemaField.class);
            if (sf != null) {
                String name = sf.name ();
                if (name.trim ().length () == 0) {
                    char[] buff = field.getName ().toCharArray ();
                    StringBuilder builder = new StringBuilder ("_");
                    for (char ch : buff) {
                        if (Character.isUpperCase (ch)) {
                            builder.append ('_');
                            builder.append (Character.toLowerCase (ch));
                        } else {
                            builder.append (ch);
                        }
                    }
                    name = builder.toString ();
                }
                map.put (name, field);
            }

            if (!field.isAccessible ()) {
                field.setAccessible (true);
            }
        }

        cache.put (type, map);
        return map;
    }

    public void setDateValue (Field field, Object item, Class<?> type, String v) {
        SimpleDateFormat fmt;
        if (type == java.sql.Timestamp.class) {
            fmt = stf;
        } else {
            fmt = sdf;
        }
        if (!StringUtil.isEmpty (v)) {
            try {
                field.set (item, fmt.parse (v));
            } catch (Exception ex) {
                ex.printStackTrace ();
                throw new RuntimeException (ex);
            }
        }
    }

    public DatabaseSchema map (Class<?> type) {
        Class<? extends DatabaseSchema> schemaType;
        if (mapping.containsKey (type)) {
            schemaType = mapping.get (type);
        } else {
            ISchema s = type.getAnnotation (ISchema.class);
            if (s != null) {
                mapping.put (type, s.value ());
                schemaType = s.value ();
            } else {
                throw new RuntimeException ("Can't map " + type);
            }
        }

        if (!DatabaseSchema.MAP.containsKey (schemaType)) {
            throw new RuntimeException ("The type [" + type + "] is not mapped.");
        }
        return DatabaseSchema.MAP.get (schemaType);
    }

    private List<Field> getAllFields (Class<?> type) {
        if (type == null) {
            return null;
        }

        if (type == Object.class || type.isInterface () || type.isEnum ()) {
            return null;
        }
        if (type.isPrimitive () || type == Void.class) {
            return null;
        }

        List<Field> fields = new ArrayList<> ();
        Class<?> parent = type.getSuperclass ();
        if (parent != null) {
            List<Field> sf = getAllFields (parent);
            if (sf != null && !sf.isEmpty ())
                fields.addAll (sf);
        }

        Field[] fs = type.getDeclaredFields ();

        if (fs != null && fs.length > 0) {
            fields.addAll (Arrays.asList (fs));
        }
        return fields;
    }
}