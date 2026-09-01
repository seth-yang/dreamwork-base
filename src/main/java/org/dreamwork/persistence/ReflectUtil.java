package org.dreamwork.persistence;

import org.dreamwork.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.*;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by seth.yang on 2017/6/16
 */
public class ReflectUtil {
    private final Logger logger = LoggerFactory.getLogger (ReflectUtil.class);
    private final Map<Class<?>, Map<String, DatabaseFieldDefinition>> cache = new HashMap<> ();
    private final Map<Class<?>, Class<? extends DatabaseSchema>> mapping = new HashMap<> ();
    private final SimpleDateFormat sdf = new SimpleDateFormat ("yyyy-MM-dd", Locale.getDefault ());
    private final SimpleDateFormat stf = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss", Locale.getDefault ());

    private final Map<Class<?>, Map<String, Method>> getters = new HashMap<> (),
                                                     setters = new HashMap<> ();

    @SuppressWarnings ("unused")
    public Object[] getInsertParameters (DatabaseSchema schema, Object item, Class<?> type) throws IllegalAccessException, InvocationTargetException {
        Map<String, DatabaseFieldDefinition> items = getTypedList (type);

        Object[] values = new Object[items.size ()];
        for (int i = 0; i < schema.fields.length; i ++) {
            String name = schema.fields [i];
            DatabaseFieldDefinition def = items.get (name);

            if (def == null) {
                values [i] = null;
            } else {
                ISchemaField sf = def.annotation;
                if (sf.id ()) {
                    Class<?> cls = null;
                    if (def.field != null) {
                        cls = def.field.getType ();
                    } else if (def.getter != null) {
                        cls = def.getter.getReturnType ();
                    }

                    if ((cls == int.class || cls == Integer.class ||
                         cls == long.class || cls == Long.class) && sf.autoincrement ()) {
                        values[i] = null;
                    } else {
                        values [i] = getValue (def, item);
                    }
                } else {
                    values [i] = getValue (def, item);
                }
            }
        }

        return values;
    }

    public Object getValue (String name, Class<?> type, Object item) throws InvocationTargetException, IllegalAccessException {
        Map<String, Method> readers = getters.get (type);
        if (readers == null || readers.isEmpty ()) {
            return null;
        }

        Method method = readers.get (name);
        if (method != null) {
            Object o = method.invoke (item);
            if (o != null) {
                Class<?> clazz = o.getClass ();
                if (clazz == java.sql.Timestamp.class) {
                    return stf.format (o);
                } else if (java.util.Date.class.isAssignableFrom (clazz)) {
                    return sdf.format (o);
                }
            }
        }

        return null;
    }

    private static String getDatabaseFieldName (ISchemaField sf, Field field) {
        String dbFieldName = sf.value ();
        if (StringUtil.isEmpty (dbFieldName)) {
            dbFieldName = sf.name ();
        }
        if (StringUtil.isEmpty (dbFieldName)) {
            dbFieldName = field.getName ();
        }
        return dbFieldName;
    }

    private static String getDatabaseFieldName (ISchemaField sf, Method method) {
        String dbFieldName = sf.value ();
        if (StringUtil.isEmpty (dbFieldName)) {
            dbFieldName = sf.name ();
        }
        if (StringUtil.isEmpty (dbFieldName)) {
            String methodName = method.getName ();
            if (methodName.startsWith ("get") && methodName.length () > 3) {
                dbFieldName = methodName.substring (3);
            } else if (methodName.startsWith ("is") && methodName.length () > 2) {
                dbFieldName = methodName.substring (2);
            }
        }
        return dbFieldName;
    }

    public Map<String, DatabaseFieldDefinition> getTypedList (Class<?> type) {
        if (cache.containsKey (type)) {
            return cache.get (type);
        }

        // 对外的直接缓存
        Map<String, DatabaseFieldDefinition> map = cache.computeIfAbsent (type, k -> new HashMap<> ());
        Map<String, Method> readers = getters.computeIfAbsent (type, k -> new HashMap<> ());
        Map<String, Method> writers = setters.computeIfAbsent (type, k -> new HashMap<> ());

        // 查找所有注解过的字段
        Set<Field> fields = getAllAnnotatedFields (type);
        Set<Method> mappedGetters = new HashSet<> ();

        for (Field field : fields) {
            ISchemaField sf = field.getAnnotation (ISchemaField.class);
            String dbFieldName = getDatabaseFieldName (sf, field);

            if (Modifier.isPublic (field.getModifiers ())) {
                // public 字段, 直接使用
                DatabaseFieldDefinition def = new DatabaseFieldDefinition ();
                def.name = dbFieldName;
                def.field = field;
                def.annotation = sf;
                map.put (dbFieldName, def);
            } else {
                String fieldName = field.getName ();
                String suffix = Character.toUpperCase (fieldName.charAt (0)) + fieldName.substring (1);
                Method method = findGetter (type, suffix, fieldName);

                // 保存到缓存
                DatabaseFieldDefinition def = new DatabaseFieldDefinition ();
                def.name = dbFieldName;
                def.getter = method;
                def.annotation = sf;
                map.put (dbFieldName, def);
                readers.put (dbFieldName, method);

                try {
                    Method setter = type.getMethod ("set" + suffix, method.getReturnType ());
                    writers.put (dbFieldName, setter);
                    mappedGetters.add (method);
                    def.setter = setter;
                } catch (NoSuchMethodException ex) {
                    logger.warn (ex.getMessage (), ex);
                    throw new RuntimeException ("cannot find setter for field '" + fieldName + "'");
                }
            }
        }

        // 解析方法
        Method[] methods = type.getMethods ();
        for (Method method : methods) {
            if (method.isAnnotationPresent (ISchemaField.class) && // 必须有注解
                !mappedGetters.contains (method) &&                // 未被映射过
                method.getParameterCount () == 0) {                // 无参数
                String methodName = method.getName ();

                ISchemaField sf = method.getAnnotation (ISchemaField.class);
                String dbFieldName = getDatabaseFieldName (sf, method);
                String suffix = null;

                if (methodName.startsWith ("get") && methodName.length () > 3) {
                    suffix = methodName.substring (3);
                } else if (methodName.startsWith ("is") && methodName.length () > 2) {
                    suffix = methodName.substring (2);
                }

                if (!StringUtil.isEmpty (suffix)) {
                    DatabaseFieldDefinition def = new DatabaseFieldDefinition ();
                    def.name = dbFieldName;
                    def.getter = method;
                    def.annotation = sf;

                    map.put (dbFieldName, def);
                    readers.put (dbFieldName, method);

                    try {
                        Method setter = type.getMethod ("set" + suffix, method.getReturnType ());
                        writers.put (dbFieldName, setter);
                        def.setter = setter;
                        mappedGetters.add (method);
                    } catch (NoSuchMethodException ex) {
                        logger.warn (ex.getMessage (), ex);
                        throw new RuntimeException ("cannot find setter for field '" + dbFieldName + "'");
                    }
                }
            }
        }

        return map;
    }

    private static Method findGetter (Class<?> type, String suffix, String fieldName) {
        Method method = null;
        try {
            method = type.getMethod ("get" + suffix);
        } catch (NoSuchMethodException ignored) { }

        if (method == null) {
            try {
                method = type.getMethod ("is" + suffix);
            } catch (NoSuchMethodException ignored) { }
        }

        if (method == null) {
            // 没找到对应的 getter，需要报错，中断流程
            throw new RuntimeException ("cannot find getter for field '" + fieldName + "'");
        }
        return method;
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

    private Set<Field> getAllAnnotatedFields (Class<?> type) {
        Set<Field> fields = new HashSet<> ();
        getAllAnnotatedFields (type, fields);
        return fields;
    }

    private void getAllAnnotatedFields (Class<?> type, Set<Field> fields) {
        if (type == null || type == Object.class || type.isInterface () || type.isEnum ()) {
            return;
        }

        Field[] fs = type.getDeclaredFields ();
        for (Field f : fs) {
            if (f.isAnnotationPresent (ISchemaField.class)) {
                fields.add (f);
            }
        }

        getAllAnnotatedFields (type.getSuperclass (), fields);
    }

    //////////////////////// static methods ///////////////////////////
    public static Class<?> getFieldType (DatabaseFieldDefinition def) {
        if (def.field != null) {
            return def.field.getType ();
        } else if (def.getter != null) {
            return def.getter.getReturnType ();
        }
        throw new RuntimeException ("cannot get field type");
    }

    public void setValue (DatabaseFieldDefinition def, Object item, Object value) throws InvocationTargetException, IllegalAccessException {
        if (def.field != null) {
            def.field.set (item, value);
        } else if (def.setter != null) {
            def.setter.invoke (item, value);
        }
    }

    public Object getValue (DatabaseFieldDefinition def, Object item) throws IllegalAccessException, InvocationTargetException {
        Object o = null;
        if (def.field != null) {
            o = def.field.get (item);
        } else if (def.getter != null) {
            o = def.getter.invoke (item);
        }

        if (o == null) {
            return null;
        }

        Class<?> type = o.getClass ();
        if (type == java.sql.Timestamp.class) {
            return new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss", Locale.getDefault ()).format (o);
        } else if (java.util.Date.class.isAssignableFrom (type)) {
            return new SimpleDateFormat ("yyyy-MM-dd", Locale.getDefault ()).format (o);
        }

        return o;
    }
}