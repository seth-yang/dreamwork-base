package org.dreamwork.util;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by game on 2017/7/3
 */
public class TypedHashMap extends HashMap<String, Object> implements ITypedMap {
    @Override
    @SuppressWarnings ("unchecked")
    public <T> T value (String key) {
        return (T) get (key);
    }

    @Override
    public Object put (String key, Object value) {
        if (key == null || value == null) {
            return value;
        }
        return super.put (key.toUpperCase (), value);
    }

    @Override
    public void putAll (Map<? extends String, ?> m) {
        if (m == null || m.isEmpty ()) {
            return;
        }

        for (String key : m.keySet ()) {
            put (key.toUpperCase (), m.get (key));
        }
    }

    @Override
    public Object remove (Object key) {
        if (key == null) {
            throw new NullPointerException ();
        }
        return super.remove (((String) key).toUpperCase ());
    }

    @Override
    public Object get (Object key) {
        if (key == null) {
            throw new NullPointerException ();
        }
        return super.get (((String) key).toUpperCase ());
    }
}