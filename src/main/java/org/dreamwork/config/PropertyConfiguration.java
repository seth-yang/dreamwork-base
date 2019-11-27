package org.dreamwork.config;

import org.dreamwork.util.StringUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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
        String value = props.getProperty (key);
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
        String value = props.getProperty (key);
        if (!StringUtil.isEmpty (value)) {
            value = value.trim ();
            if (params.length > 0) {
                for (KeyValuePair<?> p : params) {
                    value = value.replace ("${" + p.getName () + "}", String.valueOf (p.getValue ()));
                }
            }
        }

        return value;
    }

    @Override
    public int getInt (String key, int defaultValue) {
        Integer i = checkDefaultValue (key, int_cache, defaultValue);
        if (i == null) {
            String value = props.getProperty (key).trim ();
            try {
                int i_value = Integer.parseInt (value);
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
        String value = props.getProperty (key).trim ();
        try {
            long l_value = Long.parseLong (value);
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
        String value = props.getProperty (key).trim ();
        try {
            double d_value = Double.parseDouble (value);
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
        String value = props.getProperty (key).trim ();
        try {
            boolean b_value = Boolean.valueOf (value);
            bool_cache.put (key, b_value);
            return b_value;
        } catch (Exception ex) {
            return defaultValue;
        }
    }
}