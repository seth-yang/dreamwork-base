package org.dreamwork.cli.text;

/**
 * Created by seth.yang on 2018/9/21
 */
public class TextFormater {
    public static String fill (String content, char fill, int columns, Alignment align) {
        if (content.length () >= columns) {
            return content.substring (0, columns);
        }

        int d = columns - content.length ();
        StringBuilder builder = new StringBuilder ();
        if (align == Alignment.Left) {
            builder.append (content);
            for (int i = 0; i < d; i ++)
                builder.append (fill);
            return builder.toString ();
        } else if (align == Alignment.Center) {
            int start = d / 2;
            int end   = d - start;
            for (int i = 0; i < start; i ++) {
                builder.append (fill);
            }
            builder.append (content);
            for (int i = 0; i < end; i ++) {
                builder.append (fill);
            }
            return builder.toString ();
        } else {
            for (int i = 0; i < d; i ++) {
                builder.append (fill);
            }
            builder.append (content);
            return builder.toString ();
        }
    }
}
