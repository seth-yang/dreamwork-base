package org.dreamwork.cli.text;

import org.dreamwork.util.StringUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth.yang on 2018/9/21
 */
public class TextFormater {
    private static final String[] EMPTY = new String[0];

    private static OptionParser parser;

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

    public static String[] parse (String line) {
        if (StringUtil.isEmpty (line)) {
            return EMPTY;
        }

        line = line.trim ();
        if (line.indexOf (' ') < 0) {
            return new String[] {line};
        }

        synchronized (TextFormater.class) {
            if (parser == null)
                parser = new OptionParser ();
        }
        List<String> options = parser.parse (line);
        String[] array = new String[options.size ()];
        int pos = 0;
        for (String option : options) {
            array [pos ++] = option;
        }
        return array;
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

    public static void main (String[] args) throws Exception {
        BufferedReader reader = new BufferedReader (new InputStreamReader (System.in));
        String line;
        while (true) {
            line = reader.readLine ();
            if (line != null) {
                line = line.trim ();
                if ("q".equals (line) || "quit".equals (line)) {
                    break;
                }

                String[] options = TextFormater.parse (line);
                if (options.length == 0) {
                    System.out.println ("[]");
                } else {
                    System.out.printf ("[%s]%n", String.join (", ", options));
                }
            }
        }
    }
}