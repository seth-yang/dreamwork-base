package org.dreamwork.config;

import org.dreamwork.cli.text.Alignment;
import org.dreamwork.cli.text.TextFormatter;
import org.dreamwork.util.StringUtil;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@SuppressWarnings ("unused")
public class PropertyConfiguration implements IAppendableConfiguration {
    private final Properties props;
    private final Map<String, Integer> int_cache   = new HashMap<> ();
    private final Map<String, Long> long_cache     = new HashMap<> ();
    private final Map<String, Double> double_cache = new HashMap<> ();
    private final Map<String, Boolean> bool_cache  = new HashMap<> ();

    public PropertyConfiguration (Properties props) {
        this.props = new Properties ();
        this.props.putAll (props);
    }

    public Properties getRawProperties () {
        return props;
    }

    public boolean contains (String key) {
        return props != null && props.containsKey (key);
    }

    private String rawProperty (String key) {
        String value = System.getProperty (key);
        if (null == value) {
            value = System.getenv (key);
        }
        if (null == value) {
            value = props.getProperty (key);
        }

        return value;
    }

    @Override
    public void setRawProperty (String key, String value) {
        if (value != null) {
            props.setProperty (key, value);
        } else {
            props.remove (key);
        }
        int_cache.remove (key);
        double_cache.remove (key);
        long_cache.remove (key);
        bool_cache.remove (key);
    }

    @Override
    public String getString (String key, Object... params) {
        String value = rawProperty (key);
        if (!StringUtil.isEmpty (value)) {
            value = value.trim ();
            if (params.length > 0 && value.contains ("%")) {
                value = String.format (value, params);
            }
        }
        return value;
    }

    @Override
    public String getString (String key, KeyValuePair<?>... params) {
        String value = rawProperty (key);

        if (!StringUtil.isEmpty (value)) {
            value = value.trim ();
            for (KeyValuePair<?> p : params) {
                value = value.replace ("${" + p.getName () + "}", String.valueOf (p.getValue ()));
            }

            if (value.contains ("${")) {
                Pattern p = Pattern.compile ("\\$\\{(.*?)}");
                Matcher m = p.matcher (value);
                Properties sp = System.getProperties ();
                Map<String, String> env = System.getenv ();
                while (m.find ()) {
                    String g = m.group (1);
                    String r = null;
                    if (contains (g)) {
                        r = getString (g);
                    } else if (sp.containsKey (g)) {
                        r = sp.getProperty (g);
                    } else if (env.containsKey (g)) {
                        r = env.get (g);
                    }

                    if (r != null) {
                        value = value.replace ("${" + g + "}", r);
                    }
                }
            }
        }

        return value;
    }

    @Override
    public int getInt (String key, int defaultValue) {
        Integer i = checkDefaultValue (key, int_cache, defaultValue);
        if (i == null) {
            String value = rawProperty (key);
            try {
                int i_value = Integer.parseInt (value.trim ());
                int_cache.put (key, i_value);
                return i_value;
            } catch (Exception ex) {
                return defaultValue;
            }
        } else {
            return i;
        }
    }

    private<T> T checkDefaultValue (String key, Map<String, T> cache, T defaultValue) {
        if (cache.containsKey (key)) {
            return cache.get (key);
        }

        if (!props.containsKey (key)) {
            return defaultValue;
        }

        return null;
    }

    @Override
    public long getLong (String key, long defaultValue) {
        Long L = checkDefaultValue (key, long_cache, defaultValue);
        if (L != null) {
            return L;
        }
        String value = rawProperty (key);
//        String value = props.getProperty (key).trim ();
        try {
            long l_value = Long.parseLong (value.trim ());
            long_cache.put (key, l_value);
            return l_value;
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    public double getDouble (String key, double defaultValue) {
        Double d = checkDefaultValue (key, double_cache, defaultValue);
        if (d != null) {
            return d;
        }
        String value = rawProperty (key);
//        String value = props.getProperty (key).trim ();
        try {
            double d_value = Double.parseDouble (value.trim ());
            double_cache.put (key, d_value);
            return d_value;
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    private static final Pattern PATTERN_TRUE = Pattern.compile ("^(true|on|1|yes|y|t|是)$", Pattern.CASE_INSENSITIVE);
    private static final Pattern PATTERN_FALSE = Pattern.compile ("^(false|off|0|no|n|f|否)$", Pattern.CASE_INSENSITIVE);

    @Override
    public boolean getBoolean (String key, boolean defaultValue) {
        Boolean b = checkDefaultValue (key, bool_cache, defaultValue);
        if (b != null) {
            return b;
        }
        String value = rawProperty (key);

        try {
            Matcher m = PATTERN_TRUE.matcher (value.trim ());
            if (m.matches ()) {
                bool_cache.put (key, true);
                return true;
            }
            m = PATTERN_FALSE.matcher (value.trim ());
            if (m.matches ()) {
                bool_cache.put (key, false);
                return false;
            }
            boolean b_value = Boolean.parseBoolean (value.trim ());
            bool_cache.put (key, b_value);
            return b_value;
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    @Override
    @SuppressWarnings ("unchecked")
    public <T> T[] getArray (String key, Class<T> typeOfElement) {
        // 不知道如何做一级缓存，这个接口就不做一级缓存了
        var raw = getString (key);
        if (StringUtil.isEmpty (raw)) {
            return null;
        }
        raw = raw.trim ();
        if (raw.charAt (0) == '[') {
            raw = raw.substring (1);
        }
        if (raw.endsWith ("]")) {
            raw = raw.substring (0, raw.length () - 1);
        }
        raw = raw.trim ();
        String[] array = raw.split (",");
        if (typeOfElement == String.class || typeOfElement == Object.class) {
            return (T[]) array;
        }

        T[] tmp = (T[]) java.lang.reflect.Array.newInstance (typeOfElement, array.length);
        for (int i = 0; i < array.length; i ++) {
            String item = array[i].trim ();
            if (typeOfElement == byte.class || typeOfElement == Byte.class) {
                tmp[i] = (T) Byte.valueOf (item);
            } else if (typeOfElement == int.class || typeOfElement == Integer.class) {
                tmp[i] = (T) Integer.valueOf (item);
            } else if (typeOfElement == short.class || typeOfElement == Short.class) {
                tmp[i] = (T) Short.valueOf (item);
            } else if (typeOfElement == long.class || typeOfElement == Long.class) {
                tmp[i] = (T) Long.valueOf (item);
            } else if (typeOfElement == float.class || typeOfElement == Float.class) {
                tmp[i] = (T) Float.valueOf (item);
            } else if (typeOfElement == double.class || typeOfElement == Double.class) {
                tmp[i] = (T) Double.valueOf (item);
            } else if (typeOfElement == boolean.class || typeOfElement == Boolean.class) {
                tmp[i] = (T) ((Boolean) toBoolean (item));
            } else if (typeOfElement == BigDecimal.class) {
                tmp[i] = (T) new BigDecimal (item);
            } else if (typeOfElement == BigInteger.class) {
                tmp[i] = (T) new BigInteger (item);
            } else {
                throw new IllegalArgumentException ("unsupported array item type: " + typeOfElement);
            }
        }
        return tmp;
    }

    public void prettyShow () {
        System.out.println ("=============== PropertyConfiguration =============");
        String[][] values = new String[props.size ()][2];
        List<String> list = new ArrayList<> (props.stringPropertyNames ());
        list.sort (String::compareTo);
        int width = 0, max = 120;
        for (int i = 0; i < list.size (); i ++) {
            String key   = list.get (i);
            values[i][0] = key;
            values[i][1] = getString (key);

            if (width < key.length ()) {
                width = key.length ();
            }
        }

        char[] line = new char[max], empty = new char[width + 2];
        Arrays.fill (empty, ' ');
        for (String[] p : values) {
            System.out.print (TextFormatter.fill (p[0], ' ', width, Alignment.Right));
            System.out.print ("  ");
            String value = p[1];
            if (value.length () > max) {
                char[] buff = value.toCharArray ();
                int pos = 0, count = 0;
                while (count < buff.length) {
                    line[pos ++] = buff [count ++];
                    if (pos >= max) {
                        System.out.println (new String (line, 0, pos));
                        pos = 0;

                        if (count < buff.length) {
                            System.out.print (empty);
                        }
                    }
                }
                if (pos != 0) {
                    System.out.println (new String (line, 0, pos));
                }
            } else {
                System.out.println (value);
            }
        }
        System.out.println ("===================================================");
    }

    private boolean toBoolean (String expr) {
        if (expr == null) {
            return false;
        }
        return PATTERN_TRUE.matcher (expr).matches ();
    }
}