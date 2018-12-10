package org.dreamwork.util;

import java.io.Serializable;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-1-20
 * Time: 17:26:39
 */
public interface ICollection<T> extends Iterable<T>, Serializable {
    int count ();
    String getKey (int index);
    T get (int index);
    T get (String key);
    @Deprecated
    Iterator<T> iter ();
    T add (String key, T value);
    T insert (int index, String key, T value);
    T remove (int index);
    T remove (String key);
    void clear ();
    boolean contains (T t);
    boolean containsKey (String key);
}