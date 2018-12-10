package org.dreamwork.config;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-3-29
 * Time: 下午4:12
 */
public class KeyValuePair<T> implements Serializable {
    private String name;
    private T value;

    public KeyValuePair (String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public T getValue () {
        return value;
    }

    public void setValue (T value) {
        this.value = value;
    }
}