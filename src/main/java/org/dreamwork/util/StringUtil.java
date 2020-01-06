package org.dreamwork.util;

import java.security.SecureRandom;

/**
 * Created by IntelliJ IDEA.
 * User: seth
 * Date: 2010-4-24
 * Time: 22:41:57
 */
public class StringUtil {
    private static final char[] letter = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final char[] LETTER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    public static boolean isEmpty (String str) {
        return str == null || str.trim ().length () == 0;
    }

    public static boolean inArray (String[] array, String text) {
        return inArray (array, text, false);
    }

    public static boolean inArray (String[] array, String text, boolean ignoreCase) {
        for (String s : array) {
            if (ignoreCase && s.equalsIgnoreCase (text)) return true;
            else if (s.equals (text)) return true;
        }
        return false;
    }

    public static String join (String[] array, String join) {
        return join (array, join, 0, array.length - 1);
    }

    public static String join (String[] array, String join, int start, int end) {
        StringBuilder builder = new StringBuilder ();
        for (int i = start; i <= end; i++) {
            if (builder.length () > 0) builder.append (join);
            builder.append (array[i]);
        }
        return builder.toString ();
    }

    public static String dump (byte[] buff) {
        return byte2hex (buff, true);
    }

    public static String byte2hex (byte[] buff, boolean upperCase) {
        char[] a = upperCase ? LETTER : letter;
        StringBuilder builder = new StringBuilder ();
        for (byte b : buff) {
            builder.append (a[(b >> 4 & 0xF)]).append (a[(b & 0xF)]);
        }
        return builder.toString ();
    }

    public static String format (byte[] buff) {
        return format (buff, 0, buff.length);
    }

    public static String format (byte[] buff, int start, int length) {
        int count = (length * 3) + (length / 8), index = 0, pos = 0;
        char[] x = new char[count];
        byte b;
        for (int i = start; i < start + length; i ++) {
            if ((index != 0) && (index % 16 == 0)) {
                x [pos ++] = '\r';
                x [pos ++] = '\n';
            } else if ((index != 0) && (index % 8 == 0)) {
                x [pos ++] = ' ';
                x [pos ++] = ' ';
            } else if (index != 0) {
                x [pos ++] = ' ';
            }

            b = buff [i];
            x [pos ++] = LETTER[(b >> 4 & 0xF)];
            x [pos ++] = LETTER[(b & 0xF)];

            index++;
        }

        return new String (x, 0, pos);
    }

    public static String escapeXML (String text) {
        return text.replaceAll ("&", "&amp;").replaceAll ("<", "&lt;").replaceAll (">", "&gt;");
    }

    public static String toJavaPropertyName (String name) {
        return Character.toLowerCase (name.charAt (0)) + name.substring (1);
    }

    private static SecureRandom ng;

    public static String uuid () {
        if (ng == null) {
            ng = new SecureRandom();
        }

        byte[] data = new byte[16];
        ng.nextBytes(data);
        data[6]  &= 0x0f;  /* clear version        */
        data[6]  |= 0x40;  /* set to version 4     */
        data[8]  &= 0x3f;  /* clear variant        */
        data[8]  |= 0x80;  /* set to IETF variant  */

        long msb = 0;
        long lsb = 0;
        for (int i=0; i<8; i++)
            msb = (msb << 8) | (data[i] & 0xff);
        for (int i=8; i<16; i++)
            lsb = (lsb << 8) | (data[i] & 0xff);

        char[] id = new char[32];
        long hi = 1L << (8 << 2);
        hi = hi | ((msb >> 32) & (hi - 1));
        int pos;

        pos = toHex (id, 0, Tools.longToBytes (hi, 4), 0, 4);

        hi = 1L << (4 << 2);
        hi = hi | ((msb >> 16) & (hi - 1));
        pos = toHex (id, pos, Tools.longToBytes (hi, 2), 0, 2);

        hi = 1L << (4 << 2);
        hi = hi | (msb & (hi - 1));
        pos = toHex (id, pos, Tools.longToBytes (hi, 2), 0, 2);

        hi = 1L << (4 << 2);
        hi = hi | ((lsb >> 48) & (hi - 1));
        pos = toHex (id, pos, Tools.longToBytes (hi, 2), 0, 2);

        hi = 1L << (12 << 2);
        hi = hi | (lsb & (hi - 1));
        toHex (id, pos, Tools.longToBytes (hi, 6), 0, 6);

        return new String (id);
    }

    private static int toHex (char[] target, int offset, byte[] data, int pos, int length) {
        int idx = offset;
        for (int i = pos; i < pos + length; i ++) {
            int n = data [i] & 0xff;
            try {
                target[idx++] = letter[(n >> 4) & 0x0f];
                target[idx++] = letter[(n & 0x0f)];
            } catch (Exception ex) {
                ex.printStackTrace ();
            }
        }
        return idx;
    }

    public static String camelEncode (String word) {
        return camelEncode (word, '_');
    }

    public static String camelEncode (String word, char delimiter) {
        char[] src = word.toCharArray (), buff = new char[word.length ()];
        int pos = 0;
        for (int i = 0; i < src.length; i ++) {
            char ch = src[i];
            if (ch == delimiter) {
                ch = src [++ i];
                ch = Character.toUpperCase (ch);
            } else {
                ch = Character.toLowerCase (ch);
            }
            buff [pos ++] = ch;
        }
        return new String (buff, 0, pos);
    }

    public static String camelDecode (String word) {
        return camelDecode (word, '_');
    }

    public static String camelDecode (String word, char delimiter) {
        char[] buff = new char[word.length () + 32], src = word.toCharArray ();
        int pos = 0;
        for (char ch : src) {
            if (Character.isUpperCase (ch)) {
                ch = Character.toLowerCase (ch);
                if (pos != 0) {
                    buff [pos ++] = delimiter;
                }
            }
            buff [pos ++] = ch;
        }
        return new String (buff, 0, pos);
    }

    public static void main (String[] args) {
        String name = "hello_world";
        System.out.println (name = camelEncode (name));
        System.out.println (name = camelDecode (name, '-'));
        System.out.println (name = camelEncode (name, '-'));
        System.out.println (camelDecode (name, '.'));
    }
}