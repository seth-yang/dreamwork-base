package org.dreamwork.cli;

import com.google.gson.reflect.TypeToken;
import org.dreamwork.util.StringUtil;

import java.lang.reflect.Type;
import java.util.List;

/**
 * Created by seth.yang on 2017/7/10
 */
public class Argument implements Comparable<Argument> {
    public String shortOption, longOption, description, value, defaultValue, propKey;
    public boolean required, requireValue;
    public ArgumentValue[] values;

    private String buildKey () {
        String key = StringUtil.isEmpty (shortOption) ? "" : shortOption;
        key += "." + (StringUtil.isEmpty (longOption) ? "" : longOption);
        return key;
    }

    @Override
    public int compareTo (Argument o) {
        if (o == null) return 1;
        if (!StringUtil.isEmpty (shortOption) && !StringUtil.isEmpty (o.shortOption)) {
            int code = shortOption.toUpperCase ().compareTo (o.shortOption.toUpperCase ());
            if (code != 0) {
                return code;
            }
        }
        if (!StringUtil.isEmpty (longOption) && !StringUtil.isEmpty (o.longOption)) {
            int code = longOption.toUpperCase ().compareTo (o.longOption.toUpperCase ());
            if (code != 0) {
                return code;
            }
        }
        return buildKey ().toUpperCase ().compareTo (o.buildKey ().toUpperCase ());
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;

        Argument argument = (Argument) o;
        return buildKey ().equals (argument.buildKey ());
    }

    @Override
    public int hashCode () {
        return buildKey ().hashCode ();
    }

    public static final Type AS_LIST = new TypeToken<List<Argument>> () {}.getType ();
}