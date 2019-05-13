package org.dreamwork.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.Collection;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 11-8-4
 * Time: 下午2:59
 */
public class ReferenceUtil {
    private static WeakHashMap<Class<?>, WeakHashMap<String, WeakHashMap<String, AccessibleObject>>> map =
            new WeakHashMap<Class<?>, WeakHashMap<String, WeakHashMap<String, AccessibleObject>>> ();

    private static void cachetype (Class type) {
        if (map.containsKey (type)) return;
        WeakHashMap<String, WeakHashMap<String, AccessibleObject>> classMap =
                new WeakHashMap<String, WeakHashMap<String, AccessibleObject>> ();
        WeakHashMap<String, AccessibleObject> setterMap = new WeakHashMap<String, AccessibleObject> ();
        WeakHashMap<String, AccessibleObject> getterMap = new WeakHashMap<String, AccessibleObject> ();
        WeakHashMap<String, AccessibleObject> methodMap = new WeakHashMap<String, AccessibleObject> ();
        WeakHashMap<String, AccessibleObject> fieldMap  = new WeakHashMap<String, AccessibleObject> ();

        map.put (type, classMap);
        classMap.put ("setter", setterMap);
        classMap.put ("getter", getterMap);
        classMap.put ("method", methodMap);
        classMap.put ("field", fieldMap);

        Method[] methods = type.getMethods ();
        for (Method method : methods) {
            String name = method.getName ();
            Class[] pts = method.getParameterTypes ();
            if (name.startsWith ("set") && pts.length == 1)
                setterMap.put (name, method);
            else if (name.startsWith ("get") && !"getClass".equals (name) && pts.length == 0)
                getterMap.put (name, method);
            else if (name.startsWith ("is") && pts.length == 0)
                getterMap.put (name, method);
        }

        Field[] fields = type.getDeclaredFields ();
        for (Field field : fields) {
            field.setAccessible (true);
            fieldMap.put (field.getName (), field);
        }
    }

    public static Method getSetter (Class type, String name) {
        String methodName = "set" + Character.toUpperCase (name.charAt (0)) + name.substring (1);
        return getMethod (type, methodName, "setter");
    }

    public static Method getGetter (Class type, String name) {
        String methodName = "get" + Character.toUpperCase (name.charAt (0)) + name.substring (1);
        Method method = getMethod (type, methodName, "getter");
        if (method != null) return method;
        methodName = "is" + Character.toUpperCase (name.charAt (0)) + name.substring (1);
        return getMethod (type, methodName, "getter");
    }

    public static Method getMethod (Class type, String name) {
        return getMethod (type, name, "method");
    }

    public static java.util.Collection<Field> getFields (Class type) {
        cachetype (type);
        WeakHashMap<String, WeakHashMap<String, AccessibleObject>> classMap = map.get (type);
        WeakHashMap<String, AccessibleObject> fields = classMap.get ("field");
        List<Field> list = new ArrayList<Field> (fields.size ());
        for (AccessibleObject ao : fields.values ())
            list.add ((Field) ao);
        return list;
    }

    public static Object get (Object o, String property) throws InvocationTargetException, IllegalAccessException {
        if (o == null || StringUtil.isEmpty (property)) return null;
        Method method = getGetter (o.getClass (), property);
        if (method != null) {
            if (!method.isAccessible ())
                method.setAccessible (true);
            return method.invoke (o);
        }

        Field field = findField (o.getClass (), property);
        if (field != null) {
            if (!field.isAccessible ())
                field.setAccessible (true);
            return field.get (o);
        }

        return null;
    }

    public static void set (Object o, String property, Object value) throws InvocationTargetException, IllegalAccessException {
        if (o == null || StringUtil.isEmpty (property) || value == null) return;

        Method setter = getSetter (o.getClass (), property);
        if (setter != null)
            setter.invoke (o, value);
        else {
            Field field = findField (o.getClass (), property);
            if (field != null) {
                if (field.isAccessible ())
                    field.setAccessible (true);
                field.set (o, value);
            }
        }
    }

    public static Collection<String> getPropertyNames (Class type) {
        cachetype (type);
        WeakHashMap<String, WeakHashMap<String, AccessibleObject>> classMap = map.get (type);
        WeakHashMap<String, AccessibleObject> getters = classMap.get ("getter");
        WeakHashMap<String, AccessibleObject> fields = classMap.get ("field");

        List<String> fieldNames = new ArrayList<String> ();
        fieldNames.addAll (fields.keySet ());

        for (String methodName : getters.keySet ()) {
            String propName = getPropertyName (methodName);
            if (fieldNames.indexOf (propName) == -1)
                fieldNames.add (propName);
        }
        return fieldNames;
    }

    public static Field getField (Class type, String name) {
        cachetype (type);
        WeakHashMap<String, WeakHashMap<String, AccessibleObject>> classMap = map.get (type);
        WeakHashMap<String, AccessibleObject> setterMap = classMap.get ("field");
        return (Field) setterMap.get (name);
    }

    public static WeakHashMap<String, AccessibleObject> getGetters (Class type) {
        return getAccessibles (type, "getter");
    }

    public static WeakHashMap<String, AccessibleObject> getSetters (Class type) {
        return getAccessibles (type, "setter");
    }

    @SuppressWarnings ("unchecked")
    public static<T> T getAnnotation (Class<?> type, Class<? extends Annotation> annnotationType) {
        return (T) type.getAnnotation (annnotationType);
    }

    @SuppressWarnings ("unchecked")
    public static<T> T getAnnotation (Class<?> type, Class<? extends Annotation> annnotationType, String fieldName) {
        String propertyName = getPropertyName (fieldName);
        Field field = getField (type, propertyName);
        T a = null;
        if (field != null)
            a = (T) field.getAnnotation (annnotationType);
        if (a == null) {
            Method getter = getGetter (type, propertyName);
            if (getter != null)
                a = (T) getter.getAnnotation (annnotationType);
        }

        if (a == null) {
            Method setter = getSetter (type, propertyName);
            if (setter != null)
                a = (T) setter.getAnnotation (annnotationType);
        }
        return a;
    }

    public static String getPropertyName (String methodName) {
        if (methodName.startsWith ("set") || methodName.startsWith ("get")) {
            String name = methodName.substring (3);
            return Character.toLowerCase (name.charAt (0)) + name.substring (1);
        }
        if (methodName.startsWith ("is")) {
            String name = methodName.substring (2);
            return Character.toLowerCase (name.charAt (0)) + name.substring (1);
        }
        return Character.toLowerCase (methodName.charAt (0)) + methodName.substring (1);
    }

    public static String getGetterName (String propName) {
        return "get" + Character.toUpperCase (propName.charAt (0)) + propName.substring (1);
    }

    public static String getSetterName (String propName) {
        return "set" + Character.toUpperCase (propName.charAt (0)) + propName.substring (1);
    }

    private static WeakHashMap<String, AccessibleObject> getAccessibles (Class type, String methodType) {
        cachetype (type);
        WeakHashMap<String, WeakHashMap<String, AccessibleObject>> classMap = map.get (type);
        return classMap.get (methodType);
    }

    private static Method getMethod (Class type, String methodName, String methodType) {
        cachetype (type);
        WeakHashMap<String, WeakHashMap<String, AccessibleObject>> classMap = map.get (type);
        WeakHashMap<String, AccessibleObject> setterMap = classMap.get (methodType);
        return (Method) setterMap.get (methodName);
    }

    public static boolean isKnownType (Class<?> type) {
        return type == Integer.class || type == int.class ||
                type == Double.class || type == double.class ||
                type == Long.class || type == long.class ||
                type == Short.class || type == short.class ||
                type == Character.class || type == char.class ||
                type == Byte.class || type == byte.class ||
                type == Boolean.class || type == boolean.class ||
                Date.class.isAssignableFrom (type) ||
                type == BigDecimal.class ||
                type == BigInteger.class;
    }

    public static Field findField (Class c, String name) {
        Field f = null;
        try {
            f = c.getDeclaredField (name);
        } catch (NoSuchFieldException sfe) {
            // ignore;
        }
        if (f != null) return f;

        if (c == Object.class) return null;

        return findField (c.getSuperclass (), name);
    }

    @SuppressWarnings ("unchecked")
    public static<T extends Enum> T parse (Class<T> type, String text) {
        try {
            Method method = type.getMethod ("values");
            Enum[] values = (Enum[]) method.invoke (null);
            for (Enum e : values) {
                if (e.name ().equalsIgnoreCase (text))
                    return (T) e;
            }
        } catch (Exception ex) {
            //
        }
        return null;
    }

    @SuppressWarnings ("unchecked")
    public static<T extends Enum> T parse (Class<T> type, int index) {
        try {
            Method method = type.getMethod ("values");
            Enum[] values = (Enum[]) method.invoke (null);
            for (Enum e : values) {
                if (e.ordinal () == index)
                    return (T) e;
            }
        } catch (Exception ex) {
            //
        }
        return null;
    }
}
