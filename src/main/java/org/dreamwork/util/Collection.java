package org.dreamwork.util;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: Administrator
 * Date: 2004-8-8
 * Time: 3:04:48
 */
public class Collection<T> extends HashMap<String, T> implements ICollection<T> {
    private List<String> indexes;

    public Collection () {
        indexes = new ArrayList<> ();
    }

    public Collection (int cap) {
        super (cap);
        indexes = new ArrayList<> (cap);
    }

    public Collection (SortedMap<String, T> map) {
        this (map.size ());
        SortedMap<String, T> sm = map;
        for (int i = 0; i < map.size (); i ++) {
            String key = sm.firstKey (), KEY = key.toUpperCase ();
            indexes.add (KEY);
            this.put (KEY, sm.get (key));
            sm = sm.tailMap (key);
        }
    }

    public int count () {
        return indexes.size ();
    }

    public String getKey (int index) {
        return index >= indexes.size () || index < 0 ? null : indexes.get (index);
    }

    public T get (int index) {
        String key = getKey (index);
        if (key == null) return null;

        return get (key);
    }

    public T get (String key) {
        return super.get (key.toUpperCase ());
    }

    public synchronized Iterator<T> iter () {
        List<T> list = new ArrayList<> (count ());
        for (int i = 0; i < count (); i ++) {
            list.add (this.get (i));
        }
        return list.iterator ();
    }

    public T add (String key, T value) {
        key = key.toUpperCase ();
        if (indexes.indexOf (key) == -1) indexes.add (key);
        return put (key, value);
    }

    public T insert (int index, String key, T value) {
        key = key.toUpperCase ();
        if (size () == 0) return add (key, value);
        else {
            if (indexes.indexOf (key) != -1) indexes.remove (key);
            indexes.add (index, key);
            return put (key, value);
        }
    }

    public T remove (int index) {
        if (index >= 0 && index < indexes.size ()) {
            String key = getKey (index);
            indexes.remove (index);
            return super.remove (key);
        }

        return null;
    }

    public T remove (String key) {
        key = key.toUpperCase ();
        if (containsKey (key)) {
            indexes.remove (key);
            return super.remove (key);
        }

        return null;
    }

    public void clear () {
        super.clear ();
        indexes.clear ();
    }

    public boolean contains (T t) {
        return this.containsValue (t);
    }

    public boolean containsKey (String key) {
        return super.containsKey (key);
    }

    public Iterator<T> iterator () {
        return iter ();
    }
}