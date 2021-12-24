package org.dreamwork.cli.text;

import org.dreamwork.config.KeyValuePair;

/**
 * Created by seth.yang on 2018/9/21
 */
@Deprecated
@SuppressWarnings ("unused")
public class TextFormater {
    public static String fill (String content, char fill, int columns, Alignment align) {
        return TextFormatter.fill (content, fill, columns, align);
    }

    public static String[] parse (String line) {
        return TextFormatter.parse (line);
    }

    public static<T> KeyValuePair<T> parseValue (Class<T> type, String expression) {
        return TextFormatter.parseValue (type, expression);
    }
}