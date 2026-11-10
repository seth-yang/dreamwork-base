package org.dreamwork.config;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.dreamwork.util.StringUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 流式 JSON 配置实现
 * <ul>
 *   <li>无 DOM，内存占用极低</li>
 *   <li>原生 Object[] 存储数组</li>
 *   <li>${prop} 循环引用检测</li>
 * </ul>
 */
@SuppressWarnings ("unused")
public class StreamingJsonConfiguration implements IAppendableConfiguration {
    private static final Object $$EMPTY$$ = new Object () {
        @Override
        public String toString () {
            return "$$EMPTY$$";
        }
    };
    private static final JsonFactory FACTORY = new JsonFactory ();
    private static final Pattern PLACEHOLDER = Pattern.compile ("\\$\\{([^}]+)}");

    /**
     * 扁平存储：value 类型仅为 String / Number / Boolean / Object[]
     */
    private final Map<String, Object> flat = new LinkedHashMap<> ();

    private final Map<String, Long> mapOfLongs = new ConcurrentHashMap<> ();
    private final Map<String, Double> mapOfDoubles = new ConcurrentHashMap<> ();
    private final Map<String, Object> mapOfStrings = new ConcurrentHashMap<> ();
    private final Map<String, Boolean> mapOfBooleans = new ConcurrentHashMap<> ();

    /**
     * 循环引用检测（单次 getString 调用内有效）
     */
    private static final ThreadLocal<Set<String>> RESOLVING =
            ThreadLocal.withInitial (LinkedHashSet::new);

    public StreamingJsonConfiguration (File file) throws IOException {
        try (JsonParser p = FACTORY.createParser (file)) {
            doParse (p);
        }
    }

    public StreamingJsonConfiguration (String json) throws IOException {
        try (JsonParser p = FACTORY.createParser (json)) {
            doParse (p);
        }
    }

    public StreamingJsonConfiguration (InputStream in) throws IOException {
        try (JsonParser p = FACTORY.createParser (in)) {
            doParse (p);
        }
    }

    /* ==================== IConfiguration ==================== */

    @Override
    public String getString (String key, Object... params) {
        String raw;
        if (!mapOfStrings.containsKey (key)) {
            raw = System.getProperty (key);
            if (raw == null) {
                raw = System.getenv (key);
            }
            if (raw == null) {
                raw = resolve (key);
            }

            if (StringUtil.isNotEmpty (raw)) {
                mapOfStrings.put (key, raw);
            }
        }

        Object value = mapOfStrings.get (key);
        if (value == $$EMPTY$$) {
            // 打过标记的，意味着之前处理过，但是计算结果为空
            return null;
        }

        raw = (String) value;
        if (StringUtil.isNotEmpty (raw)) {
            if (raw.contains ("%") && params.length > 0) {
                raw = String.format (raw, params);
                mapOfStrings.put (key, raw);
            }
            if (raw.contains ("${")) {
                raw = expandPlaceholders (raw, null);
                mapOfStrings.put (key, raw);
            }
        }
        if (StringUtil.isEmpty (raw)) {
            // 一通操作下来还是空，要打个标记了
            mapOfStrings.put (key, $$EMPTY$$);
            return null;
        }

        return raw;
    }

    @Override
    public String getString (String key, KeyValuePair<?>... params) {
        Object value = mapOfStrings.get (key);
        if (value == $$EMPTY$$) {
            return null;
        }

        String raw = (String) value;
        if (value == null) {
            // 没有
            raw = System.getProperty (key);
            if (raw == null) {
                raw = System.getenv (key);
            }
            if (raw == null) {
                raw = resolve (key);
            }
        }
        if (StringUtil.isNotEmpty (raw)) {
            if (raw.contains ("${")) {
                raw = expandPlaceholders (raw, params);
            }
            mapOfStrings.put (key, raw);
            value = raw;
        }

        if (value == null) {
            // 还是空的
            mapOfStrings.put (key, $$EMPTY$$);
        }
        return (String) value;
    }

    @Override
    public int getInt (String key, int defaultValue) {
        long n = getLong (key, defaultValue);
        return Long.valueOf (n).intValue ();
    }

    @Override
    public long getLong (String key, long defaultValue) {
        Long value = mapOfLongs.get (key);
        if (value != null) {
            return value;
        }

        String v = getString (key);
        if (v != null) {
            try {
                long n = Long.parseLong (v.trim ());
                mapOfLongs.put (key, n);
                return n;
            } catch (NumberFormatException ignored) {
            }
        }
        // fallback
        mapOfLongs.put (key, defaultValue);
        return defaultValue;
    }

    @Override
    public double getDouble (String key, double defaultValue) {
        Double value = mapOfDoubles.get (key);
        if (value != null) {
            return value;
        }

        String v = getString (key);
        if (v != null) {
            try {
                double d = Double.parseDouble (v.trim ());
                mapOfDoubles.put (key, d);
                return d;
            } catch (NumberFormatException ignored) {
            }
        }

        mapOfDoubles.put (key, defaultValue);
        return defaultValue;
    }

    @Override
    public boolean getBoolean (String key, boolean defaultValue) {
        Boolean value = mapOfBooleans.get (key);
        if (value != null) {
            return value;
        }

        String v = getString (key);
        if (v != null) {
            boolean b = toBoolean (v.trim (), defaultValue);
            mapOfBooleans.put (key, b);
            return b;
        }

        mapOfBooleans.put (key, defaultValue);
        return defaultValue;
    }

    @Override
    @SuppressWarnings ("unchecked")
    public <T> T[] getArray (String key, Class<T> typeOfElement) {
        Object value = flat.get (key);
        if (value instanceof List<?> arr) {
            T[] tmp = (T[]) Array.newInstance (typeOfElement, arr.size ());
            for (int i = 0; i < arr.size (); i ++) {
                tmp[i] = cast (typeOfElement, arr.get (i));
            }
            return tmp;
        } else if (value == null) {
            return null;
        }

        throw new IllegalArgumentException ("unsupported array element type: " + typeOfElement);
    }

    @Override
    public void setRawProperty (String key, String value) {
        if (value == null) {
            flat.remove (key);
        } else {
            flat.put (key, value);
        }
        // 清除相关缓存，以保证下次重建缓存
        mapOfBooleans.remove (key);
        mapOfDoubles.remove (key);
        mapOfLongs.remove (key);
        mapOfStrings.remove (key);
    }

    @SuppressWarnings ("unchecked")
    private<T> T cast (Class<T> type, Object value) {
        if (value == null) {
            return null;
        }
        Class<?> typeOfValue = value.getClass ();
        if (type.isAssignableFrom (typeOfValue)) {
            return (T) value;
        }
        if (value instanceof Number n) {
            if (type == byte.class || type == Byte.class) {
                return (T) Byte.valueOf (n.byteValue ());
            } else if (type == int.class || type == Integer.class) {
                return (T) Integer.valueOf (n.intValue ());
            } else if (type == short.class || type == Short.class) {
                return (T) Short.valueOf (n.shortValue ());
            } else if (type == long.class || type == Long.class) {
                return (T) Long.valueOf (n.longValue ());
            } else if (type == float.class || type == Float.class) {
                return (T) Float.valueOf (n.floatValue ());
            } else if (type == double.class || type == Double.class) {
                return (T) Double.valueOf (n.doubleValue ());
            } else if (type == boolean.class || type == Boolean.class) {
                return (T) Boolean.valueOf (n.intValue () != 0);
            } else if (type == BigDecimal.class) {
                return (T) BigDecimal.valueOf (n.doubleValue ());
            } else if (type == BigInteger.class) {
                return (T) BigInteger.valueOf (n.longValue ());
            }
        } else if (value instanceof CharSequence) {
            String expr = value.toString ().trim ();
            if (type.isAssignableFrom (String.class)) {
                return (T) expr;
            } else if (type == boolean.class || type == Boolean.class) {
                if (TRUE_PAT.matcher (expr).matches ()) {
                    return (T) Boolean.TRUE;
                } else if (FALSE_PAT.matcher (expr).matches ()) {
                    return (T) Boolean.FALSE;
                }
            } else if (type == int.class || type == Integer.class) {
                return (T) Integer.valueOf (expr);
            } else if (type == byte.class || type == Byte.class) {
                return (T) Byte.valueOf (expr);
            } else if (type == short.class || type == Short.class) {
                return (T) Short.valueOf (expr);
            } else if (type == long.class || type == Long.class) {
                return (T) Long.valueOf (expr);
            } else if (type == float.class || type == Float.class) {
                return (T) Float.valueOf (expr);
            } else if (type == double.class || type == Double.class) {
                return (T) Double.valueOf (expr);
            } else if (type == BigDecimal.class) {
                return (T) new BigDecimal (expr);
            } else if (type == BigInteger.class) {
                return (T) new BigInteger (expr);
            }
        }
        throw new IllegalArgumentException ("unsupported type cast: " + typeOfValue + " -> " + type);
    }

    /* ==================== 解析 ==================== */
    private void doParse (JsonParser parser) throws IOException {
        Stack<String> stack = new Stack<> ();
        String field = null;

        while (true) {
            JsonToken token = parser.nextToken ();
            if (token == null) break;

            switch (token) {
                case START_OBJECT -> {
                    if (field != null) {
                        stack.push (field);
                        field = null;
                    }
                }
                case END_OBJECT, END_ARRAY -> {
                    if (!stack.isEmpty ()) stack.pop ();
                }
                case START_ARRAY -> {
                    if (field != null) {
                        stack.push (field);
                        List<Object> list = new ArrayList<> ();
                        parseArray (parser, list);
                        String path = String.join (".", stack);
                        flat.put (path, list);
                        stack.pop ();
                        field = null;
                    }
                }
                case FIELD_NAME -> field = parser.currentName ();
                case VALUE_STRING, VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT,
                     VALUE_TRUE, VALUE_FALSE, VALUE_NULL -> {
                    stack.push (field);
                    String path = String.join (".", stack);
                    var value = parseScalar (path, parser, token);
                    flat.put (path, Objects.requireNonNullElse (value, $$EMPTY$$));
                    stack.pop ();
                    field = null;
                }
                default -> {
                }
            }
        }
    }

    private void parseArray (JsonParser parser, List<Object> result) throws IOException {
        JsonToken token;
        while (true) {
            token = parser.nextToken ();
            if (token == null) {
                break;
            }

            switch (token) {
                case START_OBJECT ->
                    throw new IllegalStateException ("Nested objects within array are not supported.");
                case END_ARRAY -> {
                    System.out.println ("end of array");
                    return;
                }
                case START_ARRAY ->
                    throw new IllegalStateException ("Nested array within array are not supported.");
                case VALUE_STRING, VALUE_NUMBER_INT, VALUE_NUMBER_FLOAT,
                     VALUE_TRUE, VALUE_FALSE, VALUE_NULL -> {
                    var value = parseScalar (null, parser, token);
                    result.add (value);
                }
                default -> {
                }
            }
        }
    }

    private Object parseScalar (String path, JsonParser parser, JsonToken token) throws IOException {
        Object value = null;
        switch (token) {
            case VALUE_NUMBER_INT -> {
                long n = parser.getLongValue ();
                if (StringUtil.isNotEmpty (path))
                    mapOfLongs.put (path, n);   // 统一为 Long
                value = n;
            }
            case VALUE_NUMBER_FLOAT -> {
                double n = parser.getDoubleValue ();
                if (StringUtil.isNotEmpty (path))
                    mapOfDoubles.put (path, n);
                value = n;
            }
            case VALUE_TRUE, VALUE_FALSE -> {
                boolean b = parser.getBooleanValue ();
                if (StringUtil.isNotEmpty (path))
                    mapOfBooleans.put (path, b);
                value = b;
            }
            case VALUE_NULL -> {}
            default -> {
                value = parser.getValueAsString ();
                if (StringUtil.isNotEmpty (path))
                    mapOfStrings.put (path, value);
            }
        }
        return value;
    }

    /* ==================== 占位符展开 ==================== */

    private String resolve (String key) {
        Object v = flat.get (key);
        if (v == $$EMPTY$$) {
            return null;
        }
        if (!(v instanceof String)) return v == null ? null : String.valueOf (v);
        return expandPlaceholders ((String) v, null);
    }

    private String expandPlaceholders (String value, KeyValuePair<?>[] params) {
        Set<String> resolving = RESOLVING.get ();
        if (resolving.contains (value)) {
            throw new IllegalStateException ("Circular placeholder detected: " + resolving);
        }
        resolving.add (value);
        try {
            Matcher m = PLACEHOLDER.matcher (value);
            StringBuilder builder = new StringBuilder ();
            while (m.find ()) {
                String name = m.group (1);
                String replacement = null;

                // 1. params
                if (params != null) {
                    for (KeyValuePair<?> p : params) {
                        if (p.getName ().equals (name)) {
                            replacement = String.valueOf (p.getValue ());
                            break;
                        }
                    }
                }
                // 2. self
                if (replacement == null) {
                    Object o = flat.get (name);
                    if (o instanceof String s) replacement = expandPlaceholders (s, params);
                    else if (o != null) replacement = String.valueOf (o);
                }
                // 3. system property
                if (replacement == null) replacement = System.getProperty (name);
                // 4. env
                if (replacement == null) replacement = System.getenv (name);
                // 5. fallback
                if (replacement == null) replacement = m.group (0);

                m.appendReplacement (builder, Matcher.quoteReplacement (replacement));
            }
            m.appendTail (builder);
            return builder.toString ();
        } finally {
            resolving.remove (value);
            if (resolving.isEmpty ()) RESOLVING.remove (); // 防止 ThreadLocal 泄漏
        }
    }

    /* ==================== Boolean 规则 ==================== */

    private static final Pattern TRUE_PAT =
            Pattern.compile ("^\\s*(true|t|1|on|yes|y|是)\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FALSE_PAT =
            Pattern.compile ("^\\s*(false|f|0|off|no|n|否)\\s*$", Pattern.CASE_INSENSITIVE);

    private boolean toBoolean (String v, boolean dft) {
        if (TRUE_PAT.matcher (v).matches ()) return true;
        if (FALSE_PAT.matcher (v).matches ()) return false;
        return dft;
    }
}