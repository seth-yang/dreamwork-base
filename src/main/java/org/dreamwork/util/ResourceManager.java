package org.dreamwork.util;

import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-7-28
 * Time: 2:49:21
 */
public class ResourceManager {
    private static final Map<Object, Object> pool = Collections.synchronizedMap (new HashMap<> ());
    private ResourceBundle res;

    @SuppressWarnings ("unchecked")
    public static ResourceManager instance (String baseName, Locale locale) {
        Map<Serializable, ResourceManager> map =
                (Map<Serializable, ResourceManager>) pool.computeIfAbsent (baseName, k -> new HashMap<> ());

        ResourceManager drm = map.computeIfAbsent ("default", k -> {
            ResourceManager rm = new ResourceManager ();
            rm.res = ResourceBundle.getBundle (baseName);
            return rm;
        });

        ResourceManager rm = (ResourceManager) pool.get (locale);
        if (rm != null) return rm;

        Set<Locale> history = (Set<Locale>) pool.computeIfAbsent ("history", k -> new HashSet<> ());
        if (history.contains (locale)) return drm;

        rm = new ResourceManager ();
        rm.res = ResourceBundle.getBundle (baseName, locale);
        map.put (locale, rm);
        history.add (locale);
        return rm;
    }

    public synchronized String getString (String key) {
        return res.getString (key);
    }

    public String getString (String key, String defaultValue) {
        String value = getString (key);
        if (value == null) return defaultValue;
        return value;
    }

    public String getStringPattern (String key, Object... patterns) {
        String value = getString (key);
        if (value == null) return null;
        for (int i = 0; i < patterns.length; i ++) {
            value = value.replaceAll ("\\{" + i + "}", String.valueOf (patterns [i]));
        }
        return value;
    }

    public int getInteger (String key, int defaultValue) {
        String value = getString (key);
        try {
            return Integer.parseInt (value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public double getDouble (String key, double defaultValue) {
        String value = getString (key);
        try {
            return Double.parseDouble (value);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public boolean getBoolean (String key) {
        return getBoolean (key, false);
    }

    public boolean getBoolean (String key, boolean defaultValue) {
        String value = getString (key);
        if (value == null) return defaultValue;
        value = value.trim ();
        Matcher m = TRUE.matcher (value);
        if (m.matches ()) return true;
        m = FALSE.matcher (value);
        if (m.matches ()) return false;
        return defaultValue;
    }

    private static final Pattern TRUE = Pattern.compile ("^yes|y|true|t|1|on$", Pattern.CASE_INSENSITIVE);
    private static final Pattern FALSE = Pattern.compile ("^no|n|false|f|0|off$", Pattern.CASE_INSENSITIVE);
}