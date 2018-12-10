package org.dreamwork.util;

import java.util.SortedMap;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2009-1-30
 * Time: 23:33:07
 */
public class NameValueCollection extends Collection<String> {
    public NameValueCollection () {
        super ();
    }

    public NameValueCollection (int cap) {
        super (cap);
    }

    public NameValueCollection (SortedMap<String, String> map) {
        super (map);
    }

    public String getValue (int index) {
        return super.get (index);
    }

    public String getValue (String key) {
        return super.get (key.toUpperCase ());
    }

    public String add (String key, String value) {
        return super.add (key.toUpperCase (), value);
    }
}