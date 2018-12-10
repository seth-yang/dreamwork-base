package org.dreamwork.text.sql;

import java.util.HashMap;

/**
 * Created by IntelliJ IDEA.
 * User: <a href = "mailto:seth_yang@21cn.com">seth yang</a>
 * Date: 2007-4-23
 * Time: 18:53:29
 */
public class Clause {
    protected HashMap<String, Part> parts = new HashMap<String, Part> ();

    public void addPart (Part part) {
        parts.put (part.getName (), part);
    }

    public Part getPart (String name) {
        return parts.get (name);
    }
}