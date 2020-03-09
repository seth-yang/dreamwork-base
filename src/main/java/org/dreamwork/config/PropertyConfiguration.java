package org.dreamwork.config;

import org.dreamwork.cli.text.Alignment;
import org.dreamwork.cli.text.TextFormater;
import org.dreamwork.util.StringUtil;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PropertyConfiguration implements IConfiguration {
    private Properties props;
    private Map<String, Integer> int_cache   = new HashMap<> ();
    private Map<String, Long> long_cache     = new HashMap<> ();
    private Map<String, Double> double_cache = new HashMap<> ();
    private Map<String, Boolean> bool_cache  = new HashMap<> ();

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
            value = props.getProperty (key);
        }
        if (null == value) {
            value = System.getenv (key);
        }

        return value;
    }

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
            if (params.length > 0) {
                for (KeyValuePair<?> p : params) {
                    value = value.replace ("${" + p.getName () + "}", String.valueOf (p.getValue ()));
                }
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
//            String value = props.getProperty (key).trim ();
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

    @Override
    public boolean getBoolean (String key, boolean defaultValue) {
        Boolean b = checkDefaultValue (key, bool_cache, defaultValue);
        if (b != null) {
            return b;
        }
        String value = rawProperty (key);
//        String value = props.getProperty (key).trim ();
        try {
            boolean b_value = Boolean.valueOf (value.trim ());
            bool_cache.put (key, b_value);
            return b_value;
        } catch (Exception ex) {
            return defaultValue;
        }
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
        for (int i = 0; i < empty.length; i ++) {
            empty [i] = ' ';
        }
        for (String[] p : values) {
            System.out.print (TextFormater.fill (p[0], ' ', width, Alignment.Right));
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
}