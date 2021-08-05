package org.dreamwork.telnet.command;

import org.dreamwork.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class Session {
    public final String id;
    private final Map<String, Object> map = new HashMap<> ();

    public Session () {
        this.id = StringUtil.uuid ();
    }

    public void set (String name, Object value) {
        if (value == null || name == null) {
            throw new IllegalArgumentException ("name or value is null");
        }

        map.put (name, value);
    }

    @SuppressWarnings ("unchecked")
    public<T> T get (String name) {
        return (T) map.get (name);
    }

    public void clear () {
        map.clear ();
    }

    public void remove (String name) {
        map.remove (name);
    }
}
