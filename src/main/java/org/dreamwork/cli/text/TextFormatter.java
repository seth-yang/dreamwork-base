package org.dreamwork.cli.text;

import org.dreamwork.config.KeyValuePair;
import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings ("unused")
public class TextFormatter {
    private static final String[] EMPTY = new String[0];
    private static final String CRLF = String.format ("%n");

    private static OptionParser parser;

    public static String fill (String content, char fill, int columns, Alignment align) {
        if (content == null) {
            content = "";
        }
        if (content.length () >= columns) {
            return content.substring (0, columns);
        }

        int d = columns - getWidth (content);
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

    public static String[] parse (String line) {
        if (StringUtil.isEmpty (line)) {
            return EMPTY;
        }

        line = line.trim ();
        if (line.indexOf (' ') < 0) {
            return new String[] {line};
        }

        synchronized (TextFormatter.class) {
            if (parser == null)
                parser = new OptionParser ();
        }
        List<String> options = parser.parse (line);
        return options.toArray (new String[0]);
    }

    private static int getWidth (String content) {
        int w = 0;
        for (char ch : content.toCharArray ()) {
            if (ch >= 0x4e00 && ch <= 0x9fa5) {
                // chinese characters
                w += 2;
            }
            else w ++;
        }
        return w;
    }

    @SuppressWarnings ("unchecked")
    public static<T> KeyValuePair<T> parseValue (Class<T> type, String expression) {
        if (!expression.startsWith ("--")) {
            return null;
        }

        expression = expression.substring (2);
        int pos = expression.indexOf ('=');
        if (pos <= 0) {
            return null;
        }

        Object value;
        String name = expression.substring (0, pos);
        String s_value = expression.substring (pos + 1);

        if (type == int.class || type == Integer.class) {
            try {
                value = Integer.parseInt (s_value);
            } catch (Exception ex) {
                throw new IllegalArgumentException ("invalid number format: " + s_value);
            }
        } else if (type == String.class) {
            value = s_value;
        } else {
            throw new IllegalArgumentException ("unknown type: " + type.getName ());
        }
        return new KeyValuePair<> (name, (T) value);
    }

    private static Wrapper arrange (Object[][] data, String[] header, Alignment[] alignments) {
        Wrapper w = new Wrapper ();
        int columns = 0;
        if (header != null && header.length > 0) {
            columns = header.length;
        } else if (data != null) {
            for (Object[] line : data) {
                if (line != null) {
                    columns = Math.max (columns, line.length);
                }
            }
        }
        if (columns == 0) {
            return w;
        }

        if (alignments == null) {
            alignments = new Alignment[columns];
            for (int i = 0; i < columns; i ++) {
                alignments[i] = Alignment.Left;
            }
        } else if (alignments.length < columns) {
            Alignment[] _new = new Alignment[columns];
            System.arraycopy (alignments, 0, _new, 0, alignments.length);
            for (int i = alignments.length; i < columns; i ++) {
                _new[i] = Alignment.Left;
            }
            alignments = _new;
        }

        int[] widths = new int[columns];
        if (header != null)
            for (int i = 0; i < columns; i ++) widths[i] = getWidth (header[i]);

        if (data != null) {
            for (Object[] line : data) {
                for (int i = 0; i < columns; i ++) {
                    if (i < line.length) {
                        Object o = line[i];
                        String part = o == null ? "" : o.toString ();
                        widths[i] = Math.max (widths[i], getWidth (part));
                    }
                }
            }
        }

        w.columns = columns;
        w.alignments = alignments;
        w.widths = widths;
        return w;
    }

    public static void printTable (Object[][] data) throws IOException {
        printTable (System.out, data, null, null, false);
    }

    public static void printTable (List<List<?>> data) throws IOException {
        printTable (System.out, data, null, null, false);
    }

    public static void printTable (Object[][] data, String[] header) throws IOException {
        printTable (System.out, data, header, null, true);
    }

    public static void printTable (List<List<?>> data, List<String> header) throws IOException {
        printTable (System.out, data, header, null, true);
    }

    public static void printTable (Appendable printer, List<List<?>> data, List<String> header, List<Alignment> alignments, boolean showDivider) throws IOException {
        Object[][] _data = null;
        String[]   _header = null;
        Alignment[] _alignments = null;
        if (data != null) {
            _data = new Object[data.size ()][];
            for (int i = 0; i < data.size (); i ++) {
                List<?> list = data.get (i);
                if (list == null || list.isEmpty ()) {
                    _data[i] = new Object[0];
                } else {
                    _data[i] = list.toArray (new Object[0]);
                }
            }
        }
        if (header != null) _header = header.toArray (new String[0]);
        if (alignments != null) _alignments = alignments.toArray (new Alignment[0]);
        printTable (printer, _data, _header, _alignments, showDivider);
    }

    public static void printTable (Appendable printer, Object[][] data, String[] header, Alignment[] alignments, boolean showDivider) throws IOException {
        Wrapper w = arrange (data, header, alignments);
        int columns = w.columns;
        alignments = w.alignments;
        int[] widths = w.widths;

        // print the header
        if (header != null) {
            StringBuilder div = new StringBuilder ();
            for (int i = 0; i < columns; i ++) {
                String text = header[i];
                Alignment a = alignments[i];
                int width = widths[i];
                printer.append (TextFormatter.fill (text, ' ', width, a));
                if (i != columns - 1) {
                    if (showDivider) {
                        printer.append (" | ");
                        div.append (TextFormatter.fill ("-", '-', width, a));
                        div.append ("-+-");
                    } else {
                        printer.append ("   ");
                    }
                } else {
                    printer.append (CRLF);
                    if (showDivider)
                        div.append (TextFormatter.fill ("-", '-', width, a));
                }
            }
            if (showDivider) {
                printer.append (div);
                printer.append (CRLF);
            }
        }
        if (data != null) {
            for (Object[] line : data) {
                if (line != null) {
                    for (int i = 0; i < columns; i ++) {
                        String text = "";
                        Alignment a = alignments[i];
                        int width = widths[i];
                        if (i < line.length) {
                            Object o = line[i];
                            text = o == null ? "" : o.toString ();
                        }

                        printer.append (TextFormatter.fill (text, ' ', width, a));
                        if (i != columns - 1) {
                            if (showDivider)
                                printer.append (" | ");
                            else
                                printer.append ("   ");
                        } else {
                            printer.append (CRLF);
                        }
                    }
                }
            }
        }
    }

    private static final class OptionParser {
        private List<String> parse (String line) {
            char[] stream = line.toCharArray ();
            List<String> options = new ArrayList<> (32);
            int quot = -1, escape = -1, pos = 0;
            char[] buff = new char[stream.length];

            for (char ch : stream) {
                switch (ch) {
                    case ' ':
                        if (quot > 0) {                         // 引号开启
                            buff [pos ++] = ch;
                        } else if (pos > 0) {                   // 引号外, 代表一个部分已经结束
                            options.add (new String (buff, 0, pos));
                            pos = 0;
                        }
                        break;
                    case '"':
                    case '\'':
                        if (quot == ch) {                       // 记录的引号和当前字符一致
                            if (buff[pos - 1] == '\\') {        // 上个字符是 '\'，应当转意这个引号
                                if (escape > 0) {
                                    buff[pos - 1] = ch;
                                    escape = -1;
                                } else {
                                    buff [pos ++] = ch;
                                }
                            } else {                            // 关闭引号
                                quot = -1;
                            }
                        } else if (quot < 0) {                  // 首次碰到引号
                            quot = ch;
                        } else {                                // 引号已开启，但和当前字符不一致
                            if (escape > 0 && buff [pos -1] == '\\') {
                                buff[pos - 1] = ch;
                                escape = -1;
                            } else {
                                buff[pos++] = ch;
                            }
                        }
                        break;
                    case '\\':
                        if (escape < 0) {                       // 未转意，先记录该字符，再记录转意
                            buff[pos ++] = ch;
                            escape = ch;
                        } else {                                // 释放转意
                            escape = -1;
                        }
                        break;
                    default:
                        buff[pos++] = ch;
                        break;
                }
            }
            if (pos > 0) {
                options.add (new String (buff, 0, pos));
            }

            return options;
        }
    }

    private static final class Wrapper {
        int columns;
        int[] widths;
        Alignment[] alignments;
    }
}
