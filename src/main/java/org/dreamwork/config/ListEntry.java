package org.dreamwork.config;

import org.dreamwork.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seth
 * Date: 13-1-11
 * Time: 下午6:07
 */
public class ListEntry extends ConfigEntry {
    private List<Object> list = new ArrayList<Object> ();

    @Override
    public void addValue (String name, Object value) {
        if (name == null)
            addValue (value);
        else
            super.addValue (name, value);
    }

    public void addValue (Object value) {
        list.add (value);
    }

    @Override
    public Object getValue (String name) {
        if (StringUtil.isEmpty (name)) return null;
        if ("size".equalsIgnoreCase (name))
            return String.valueOf (list.size ());

        if (values.containsKey (name))
            return values.get (name);

        if (name.charAt (0) == '[')
            name = name.replace ("[", "").replace ("]", "");
        try {
            int index = Integer.parseInt (name);
            if (index < 0 || index >= list.size ())
                throw new ArrayIndexOutOfBoundsException (index);

            return list.get (index);
        } catch (NumberFormatException e) {
            throw new ConfigParseException ("Unknown index: " + name);
        }
    }
}
