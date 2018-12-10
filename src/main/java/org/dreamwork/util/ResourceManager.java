package org.dreamwork.util;

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
    private static Map<Object, Object> pool =
            Collections.synchronizedMap (new HashMap<Object, Object> ());

//    private static ResourceManager defaultResourceManager;

    private ResourceBundle res;

    @SuppressWarnings ("unchecked")
    public static ResourceManager instance (String baseName, Locale locale) {
        Map map = (Map) pool.get (baseName);
        if (map == null) {
            map = new HashMap ();
            pool.put (baseName, map);
        }

        ResourceManager drm = (ResourceManager) map.get ("default");
        if (drm == null) {
            drm = new ResourceManager ();
            drm.res = ResourceBundle.getBundle (baseName);
        }

        ResourceManager rm = (ResourceManager) pool.get (locale);
        if (rm != null) return rm;

        Set history = (Set) pool.get ("history");
        if (history == null) {
            history = new HashSet ();
            pool.put ("history", history);
        }
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
            value = value.replaceAll ("\\{" + i + "\\}", String.valueOf (patterns [i]));
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
        Matcher m = p_yes.matcher (value);
        if (m.matches ()) return true;
        m = p_true.matcher (value);
        if (m.matches ()) return true;
        try {
            int i_value = Integer.parseInt (value);
            return i_value != 0;
        } catch (NumberFormatException ex) {
            return defaultValue;
        }
    }

    private static Pattern p_yes = Pattern.compile ("^\\s*y(es)?\\s*$", Pattern.CASE_INSENSITIVE);
    private static Pattern p_true = Pattern.compile ("^\\s*t(rue)?\\s*$", Pattern.CASE_INSENSITIVE);
}