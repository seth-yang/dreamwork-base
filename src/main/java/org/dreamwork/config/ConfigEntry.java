package org.dreamwork.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-3-29
 * Time: 下午4:47
 */
public class ConfigEntry implements Serializable {
    protected Map<String, Object> values = new HashMap<String, Object> ();

    public ConfigEntry () {}

    public void addValue (String name, Object value) {
        values.put (name, value);
    }

    public Object getValue (String name) {
        return values.get (name);
    }
}