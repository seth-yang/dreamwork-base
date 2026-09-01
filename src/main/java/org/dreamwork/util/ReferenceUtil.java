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

    record ClassInfo (
            WeakHashMap<String, AccessibleObject> setters,
            WeakHashMap<String, AccessibleObject> getters,
            WeakHashMap<String, AccessibleObject> methods,
            WeakHashMap<String, AccessibleObject> fields
    ) {}

    private static final WeakHashMap<Class<?>, ClassInfo> classInfoMap = new WeakHashMap<> ();

    private static void cacheType (Class<?> type) {
        if (classInfoMap.containsKey (type)) return;
        WeakHashMap<String, AccessibleObject> setterMap = new WeakHashMap<> ();
        WeakHashMap<String, AccessibleObject> getterMap = new WeakHashMap<> ();
        WeakHashMap<String, AccessibleObject> methodMap = new WeakHashMap<> ();
        WeakHashMap<String, AccessibleObject> fieldMap  = new WeakHashMap<> ();
        ClassInfo classInfo = new ClassInfo (setterMap, getterMap, methodMap, fieldMap);

        classInfoMap.put (type, classInfo);

        Method[] methods = type.getMethods ();
        for (Method method : methods) {
            String name = method.getName ();
            Class<?>[] pts = method.getParameterTypes ();
            if (name.startsWith ("set") && pts.length == 1)
                setterMap.put (name, method);
            else if (name.startsWith ("get") && !"getClass".equals (name) && pts.length == 0)
                getterMap.put (name, method);
            else if (name.startsWith ("is") && pts.length == 0)
                getterMap.put (name, method);
        }

        Field[] fields = type.getFields ();
        for (Field field : fields) {
            fieldMap.put (field.getName (), field);
        }
    }

    public static Method getSetter (Class<?> type, String name) {
        String methodName = "set" + Character.toUpperCase (name.charAt (0)) + name.substring (1);
        return getMethod (type, methodName, "setter");
    }

    public static Method getGetter (Class<?> type, String name) {
        String methodName = "get" + Character.toUpperCase (name.charAt (0)) + name.substring (1);
        Method method = getMethod (type, methodName, "getter");
        if (method != null) return method;
        methodName = "is" + Character.toUpperCase (name.charAt (0)) + name.substring (1);
        return getMethod (type, methodName, "getter");
    }

    public static Method getMethod (Class<?> type, String name) {
        return getMethod (type, name, "method");
    }

    public static java.util.Collection<Field> getFields (Class<?> type) {
        cacheType (type);
        ClassInfo classInfo = classInfoMap.get (type);
        if (classInfo != null) {
            List<Field> list = new ArrayList<> (classInfo.fields ().size ());
            for (AccessibleObject ao : classInfo.fields ().values ())
                list.add ((Field) ao);
            return list;
        }
        return Collections.emptyList ();
    }

    public static Object get (Object o, String property) throws InvocationTargetException, IllegalAccessException {
        if (o == null || StringUtil.isEmpty (property)) return null;
        Method method = getGetter (o.getClass (), property);
        if (method != null) {
            return method.invoke (o);
        }

        Field field = findField (o.getClass (), property);
        if (field != null) {
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
                field.set (o, value);
            }
        }
    }

    public static Collection<String> getPropertyNames (Class<?> type) {
        cacheType (type);
        ClassInfo info = classInfoMap.get (type);
        if (info != null) {
            Set<String> set = new HashSet<> (info.setters ().size () + info.getters ().size () + info.fields ().size ());
            set.addAll (info.setters ().keySet ());
            set.addAll (info.getters ().keySet ());
            set.addAll (info.fields ().keySet ());
            return set;
        }
        return Collections.emptyList ();
    }

    public static Field getField (Class<?> type, String name) {
        cacheType (type);
        ClassInfo info = classInfoMap.get (type);
        if (info != null) {
            return (Field) info.fields ().get (name);
        }
        return null;
    }

    public static WeakHashMap<String, AccessibleObject> getGetters (Class<?> type) {
        return getAccessibles (type, "getter");
    }

    public static WeakHashMap<String, AccessibleObject> getSetters (Class<?> type) {
        return getAccessibles (type, "setter");
    }

    @SuppressWarnings ("unchecked")
    public static<T> T getAnnotation (Class<?> type, Class<? extends Annotation> annotationType) {
        return (T) type.getAnnotation (annotationType);
    }

    @SuppressWarnings ("unchecked")
    public static<T> T getAnnotation (Class<?> type, Class<? extends Annotation> annotationType, String fieldName) {
        String propertyName = getPropertyName (fieldName);
        Field field = getField (type, propertyName);
        T a = null;
        if (field != null)
            a = (T) field.getAnnotation (annotationType);
        if (a == null) {
            Method getter = getGetter (type, propertyName);
            if (getter != null)
                a = (T) getter.getAnnotation (annotationType);
        }

        if (a == null) {
            Method setter = getSetter (type, propertyName);
            if (setter != null)
                a = (T) setter.getAnnotation (annotationType);
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

    private static WeakHashMap<String, AccessibleObject> getAccessibles (Class<?> type, String methodType) {
        cacheType (type);
        ClassInfo info = classInfoMap.get (type);
        if (info != null) {
            return switch (methodType) {
                case "getter" -> info.getters ();
                case "setter" -> info.setters ();
                default -> new WeakHashMap<> ();
            };
        }
        return new WeakHashMap<> ();
    }

    private static Method getMethod (Class<?> type, String methodName, String methodType) {
        cacheType (type);
        ClassInfo info = classInfoMap.get (type);
        if (info != null) {
            return switch (methodType) {
                case "getter" -> (Method) info.getters ().get (methodName);
                case "setter" -> (Method) info.setters ().get (methodName);
                case "method" -> (Method) info.methods ().get (methodName);
                default -> null;
            };
        }
        return null;
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

    public static Field findField (Class<?> c, String name) {
        if (c == Object.class || c == null) return null;

        Field f = null;
        try {
            f = c.getDeclaredField (name);
        } catch (NoSuchFieldException sfe) {
            // ignore;
        }
        if (f != null) return f;

        return findField (c.getSuperclass (), name);
    }

    @SuppressWarnings ("unchecked")
    public static<T extends Enum<?>> T parse (Class<T> type, String text) {
        try {
            Method method = type.getMethod ("values");
            Enum<?>[] values = (Enum<?>[]) method.invoke (null);
            for (Enum<?> e : values) {
                if (e.name ().equalsIgnoreCase (text))
                    return (T) e;
            }
        } catch (Exception ex) {
            //
        }
        return null;
    }

    @SuppressWarnings ("unchecked")
    public static<T extends Enum<?>> T parse (Class<T> type, int index) {
        try {
            Method method = type.getMethod ("values");
            Enum<?>[] values = (Enum<?>[]) method.invoke (null);
            for (Enum<?> e : values) {
                if (e.ordinal () == index)
                    return (T) e;
            }
        } catch (Exception ex) {
            //
        }
        return null;
    }
}
