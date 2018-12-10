package org.dreamwork.text;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-23
 * Time: 下午8:12
 */
public class TextFormat {
    public static final String[] HTML_INVALID_CHARS = {"&", "<", ">"};
    public static final String[] HTML_ATTRIBUTE_INVALID_CHAR = {"<", ">", "&", "'", "\""};

    public static String htmlEscape (String html) {
/*
        for (String text : HTML_INVALID_CHARS) {
            if (html.indexOf (text) >= -1)
                html = html.replace (text, "&#" + (int) text.charAt (0) + ';');
        }
        return html;
*/
        StringBuilder builder = new StringBuilder ();
        for (int ch : html.toCharArray ()) {
            switch (ch) {
                case '&' :
                case '<' :
                case '>' :
                    builder.append ("&#").append (ch).append (';');
                    break;
                default:
                    builder.append ((char) ch);
                    break;
            }
        }
        return builder.toString ();
    }

    public static String htmlAttributeEscape (String html) {
/*
        for (String text : HTML_ATTRIBUTE_INVALID_CHAR) {
            if (html.indexOf (text) >= -1)
                html = html.replace (text, "&#" + (int) text.charAt (0) + ';');
        }
        return html;
*/
        StringBuilder builder = new StringBuilder ();
        for (int ch : html.toCharArray ()) {
            switch (ch) {
                case '&' :
                case '<' :
                case '>' :
                case '"' :
                case '\'' :
                    builder.append ("&#").append (ch).append (';');
                    break;
                default:
                    builder.append ((char) ch);
                    break;
            }
        }
        return builder.toString ();
    }

    public static String cStyleEscape (String code) {
        return cStyleEscape (code, false);
    }

    public static String cStyleEscape (String code, boolean inQuot) {
        StringBuilder builder = new StringBuilder ();
        int i = 0, length;
        char[] buff = code.toCharArray ();
        if (inQuot) {
            builder.append (buff [0]);
            i ++;
            length = buff.length - 1;
        } else {
            length = buff.length;
        }
        for (; i < length; i ++) {
//        for (int ch : code.toCharArray ()) {
            int ch = buff [i];
            switch (ch) {
                case '"'  :
                    builder.append ("\\\"");
                    break;
                case '\\' :
                    builder.append ("\\\\");
                    break;
                case '\t' :
                    builder.append ("\\t");
                    break;
                case '\n' :
                    builder.append ("\\n");
                    break;
                case '\r' :
                    builder.append ("\\r");
                    break;
                default :
                    builder.append ((char) ch);
                    break;
            }
        }
        if (inQuot) builder.append (buff [buff.length - 1]);
        return builder.toString ();
    }
}