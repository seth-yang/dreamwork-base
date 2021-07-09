package org.dreamwork.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * 杂项工具函数
 * Created by seth on 15-12-23.
 */
public class Tools {
    public  static final int PN9       = 0x1ff;
    private static final byte[] CRLF   = {'\r', '\n'};
    private static final char[] LETTER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private static final char[] TABLE  = new char['f' + 1];
    static {
        TABLE ['0'] = '0';
        TABLE ['1'] = '1';
        TABLE ['2'] = '2';
        TABLE ['3'] = '3';
        TABLE ['4'] = '4';
        TABLE ['5'] = '5';
        TABLE ['6'] = '6';
        TABLE ['7'] = '7';
        TABLE ['8'] = '8';
        TABLE ['9'] = '9';
        TABLE ['A'] = 'A';
        TABLE ['B'] = 'B';
        TABLE ['C'] = 'C';
        TABLE ['D'] = 'D';
        TABLE ['E'] = 'E';
        TABLE ['F'] = 'F';
        TABLE ['a'] = 'a';
        TABLE ['b'] = 'b';
        TABLE ['c'] = 'c';
        TABLE ['d'] = 'd';
        TABLE ['e'] = 'e';
        TABLE ['f'] = 'f';
    }
    /**
     * 对数组切片
     * @param buff   原始数组
     * @param start  开始位置
     * @param length 切片长度
     * @return 切片后的新数组
     */
    public static byte[] slice (byte[] buff, int start, int length) {
        if (buff == null || buff.length == 0)
            return new byte[] {};

        if (start < 0 || start + length > buff.length)
            throw new ArrayIndexOutOfBoundsException ("start + length must less than array's length");

        byte[] tmp = new byte[length];
        System.arraycopy (buff, start, tmp, 0, length);
        return tmp;
    }

    /**
     * 反转数组.
     *
     * 在源数组上将数组元素反转。若要返回一个新数组，应使用 {@link #reverseTo(byte[])}
     * @param data 源数组
     */
    public static void reverse (byte[] data) {
        reverse (data, 0, data.length);
    }

    /**
     * 对数组的部分区域进行反转.
     *
     * 在源数组上针对部分区间进行元素的反转。若要返回一个反转后的数据区间切片，应使用 {@link #reverseTo(byte[], int, int)}
     * @param data 源数组
     * @param start 区间开始位置。若无效的区间开始位置（start &lt; 0 || start + length &gt; data.length)，则抛出数组越界异常
     * @param length 区间长度
     */
    public static void reverse (byte[] data, int start, int length) {
        int half = length / 2;
        byte tmp;
        for (int i = 0; i < half; i ++) {
            tmp = data [i + start];
            data [i + start] = data [start + length - i - 1];
            data [start + length - i - 1] = tmp;
        }
    }

    /**
     * 反转数组并返回一个新的数组.
     *
     * 反转不会影响源数组。
     * 参见 {@link #reverse(byte[])}, {@link #reverse(byte[], int, int)}, {@link #reverseTo(byte[], int, int)}
     * @param data 源数组
     * @return 反转后的数组.
     */
    public static byte[] reverseTo (byte[] data) {
        return reverseTo (data, 0, data.length);
    }

    /**
     * 反转源数组的指定区域，并返回反转部分数组的新的拷贝.
     *
     * 注意，该方法不会影响源数组。
     * 参见 {@link #reverse(byte[])}, {@link #reverse(byte[], int, int)}, {@link #reverseTo(byte[])}
     * @param data 源数组
     * @param start 区间开始
     * @param length 长度
     * @return 反转后，新的区间数组
     */
    public static byte[] reverseTo (byte[] data, int start, int length) {
        byte[] buff = slice (data, start, length);
        reverse (buff);
        return buff;
    }

    /**
     * 将字节数组拼装成整数.
     *
     * @param data 字节数组
     * @return 拼装后的整数
     */
    public static int bytesToInt (byte[] data) {
        if (data == null || data.length == 0)
            throw new NumberFormatException ("can't cast empty to int");

        if (data.length > 4)
            throw new RuntimeException ("integer value overflow.");

        int n = 0;
        for (int i = 0; i < data.length; i ++) {
            n |= (data [i] & 0xff) << ((data.length - i - 1) * 8);
        }

        return n;
    }

    /**
     * 将字节数组的指定部分拼装成整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param data 字节数组
     * @param start 开始位置
     * @param length 长度
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static int bytesToInt (byte[] data, int start, int length, boolean reverse) {
        byte[] tmp = slice (data, start, length);
        if (reverse)
            reverse (tmp);

        return bytesToInt (tmp);
    }

    /**
     * 将字节数组的指定部分拼装成整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param data 字节数组
     * @param length 长度
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static int bytesToInt (byte[] data, int length, boolean reverse) {
        return bytesToInt (data, 0, length, reverse);
    }

    /**
     * 将字节数组的指定部分拼装成整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param buff    数据
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static int bytesToInt (byte[] buff, boolean reverse) {
        return bytesToInt (buff, 0, buff.length, reverse);
    }

    /**
     * 将整数拆分位字节数组.
     *
     * 返回的数组长度为 4.若要指定返回的数组的长度，请使用 {@link #intToBytes(int, int)},
     * 若需要将返回的字节数组反转，则使用 {@link #intToBytes(int, int, boolean)}
     *
     * @param n 整数值
     * @return 拆分后的字节数组
     */
    public static byte[] intToBytes (int n) {
        return intToBytes (n, 4);
    }

    /**
     * 将整数拆分位字节数组.
     *
     * 若需要将返回的字节数组反转，则使用 {@link #intToBytes(int, int, boolean)}
     *
     * @param n 整数值
     * @param length 指定的返回的字节数组的长度，其值不能超过4。
     * @return 拆分后的字节数组
     */
    public static byte[] intToBytes (int n, int length) {
        if (length > 4)
            length = 4;
        byte[] buff = new byte[length];
        for (int i = 0; i < buff.length; i ++) {
            buff [i] = (byte) ((n >> ((length - i - 1) * 8)) & 0xff);
        }
        return buff;
    }

    /**
     * 将整数拆分位字节数组.
     *
     * 若参数 reverse 为 真，则对返回的字节数组进行反转
     *
     * @param n 整数值
     * @param length 指定的返回的字节数组的长度，其值不能超过4。
     * @param reverse 是否反转数据
     * @return 拆分后的字节数组
     */
    public static byte[] intToBytes (int n, int length, boolean reverse) {
        byte[] buff = intToBytes (n, length);
        if (reverse)
            reverse (buff);
        return buff;
    }

    /**
     * 将字节数组的指定部分拼装成长整数.
     *
     * @param data 字节数组
     * @return 拼装后的整数值
     */
    public static long bytesToLong (byte[] data) {
        if (data == null || data.length == 0)
            throw new NumberFormatException ("can't cast empty to long");

        if (data.length > 8)
            throw new RuntimeException ("long value overflow.");

        long n = 0;
        for (int i = 0; i < data.length; i ++) {
            n |= (data [i] & 0xffL) << ((data.length - i - 1) * 8);
        }
        return n;
    }

    /**
     * 将字节数组的指定部分拼装成长整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param data 字节数组
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static long bytesToLong (byte[] data, boolean reverse) {
        return bytesToLong (data, 0, 8, reverse);
    }

    /**
     * 将字节数组的指定部分拼装成长整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param data 字节数组
     * @param length 长度
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static long bytesToLong (byte[] data, int length, boolean reverse) {
        return bytesToLong (data, 0, length, reverse);
    }

    /**
     * 将字节数组的指定部分拼装成整数.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param data 字节数组
     * @param start 起始位置
     * @param length 长度
     * @param reverse 是否反转字节数组
     * @return 拼装后的整数值
     */
    public static long bytesToLong (byte[] data, int start, int length, boolean reverse) {
        byte[] buff = slice (data, start, length);
        if (reverse)
            reverse (buff);

        return bytesToLong (buff);
    }

    /**
     * 将长整数拆分位字节数组.
     *
     * 若需要指定返回数组的长度，请使用 {@link #longToBytes(long, int)}
     * 若需要将返回的字节数组反转，则使用 {@link #intToBytes(int, int, boolean)}
     *
     * @param n 整数值
     * @return 拆分后的字节数组
     */
    public static byte[] longToBytes (long n) {
        return longToBytes (n, 8);
    }

    /**
     * 将长整数拆分位字节数组.
     *
     * 若需要将返回的字节数组反转，则使用 {@link #intToBytes(int, int, boolean)}
     *
     * @param n 整数值
     * @param length 指定返回数组的长度
     * @return 拆分后的字节数组
     */
    public static byte[] longToBytes (long n, int length) {
        if (length < 0 || length > 8)
            throw new RuntimeException ("Long value overflow.");

        byte[] buff = new byte[length];
        for (int i = 0; i < length; i ++) {
            buff [i] = (byte) ((n >> ((length - i - 1) * 8)) & 0xff);
        }

        return buff;
    }

    /**
     * 将长整数拆分位字节数组.
     *
     * 若参数 reverse 为真，则先将指定的区域进行反转，然后再进行拼装.
     *
     * @param n 整数值
     * @param length 指定返回数组的长度
     * @param reverse 是否反转
     * @return 拆分后的字节数组
     */
    public static byte[] longToBytes (long n, int length, boolean reverse) {
        byte[] tmp = longToBytes (n, length);
        if (reverse)
            reverse (tmp);

        return tmp;
    }

    public static String toHex (byte[] buff) {
        char[] chs = new char [buff.length * 3 - 1];
        int pos = 0, n, i;
        for (i = 0; i < buff.length; i ++) {
            n = buff [i];
            if (i != 0) {
                chs [pos ++] = ' ';
            }
            chs [pos ++] = LETTER [(n >> 4) & 0x0f];
            chs [pos ++] = LETTER [n & 0x0f];
        }
        return new String (chs);
    }

    public static byte[] fromHex (String hex) {
        if (hex == null || hex.trim ().length () == 0)
            return new byte[0];
        hex = hex.trim ().replaceAll ("\\s+", "");
        if (hex.length () % 2 != 0)
            hex = '0' + hex;

        char[] chars = hex.toCharArray ();
        byte[] buff  = new byte[chars.length/2];
        int i = 0, j = 0;
        while (i < chars.length) {
            buff [j ++] = (byte) (convert (chars [i ++], 4) | convert (chars [i ++], 0));
        }
        return buff;
    }

    public static byte[] fromAscii (String ascii) {
        char[] a = ascii.toCharArray ();
        byte[] buff = new byte[a.length / 4];
        int pos = 0, i = 0;
        while (i < a.length) {
            buff [pos ++] = (byte) (convert (TABLE [(convert (a [i ++], 4) |
                    convert (a [i ++], 0))], 4) |
                    convert (TABLE [(convert (a [i ++], 4) |
                            convert (a [i ++], 0))], 0));
        }
        return buff;
    }

    private static int convert (char ch, int shift) {
        int n;
        if (ch >= '0' && ch <= '9') {
            n = ch - '0';
        } else if (ch >= 'A' && ch <= 'F') {
            n = ch - 'A' + 10;
        } else {
            n = ch - 'a' + 10;
        }

        if (shift > 0)
            n <<= shift;
        return n;
    }

    /**
     * 将整数形式的IP地址值转成字符串形式.
     *
     * 逆操作参见 {@link #stringToIp(String)}
     *
     * @param ip ip地址的整数形式
     * @return ip地址的字符串形式
     */
    public static String ipToString (int ip) {
/*
        byte[] buff = intToBytes (ip);
        StringBuilder builder = new StringBuilder ();
        for (byte b : buff) {
            if (builder.length () > 0) builder.append ('.');
            builder.append (b & 0xff);
        }
        return builder.toString ();
*/
        char[] buff = new char[16];
        int pos     = 15, offset, n, m, r;
        for (int i = 3; i >= 0; i --) {
            if (i < 3) {
                buff [pos --] = '.';
            }
            offset = (3 - i) << 3;
            m = n = (ip >> offset) & 0xff;
            while (m > 10) {
                while (n > 10) {
                    r = n / 10;
                    n = n - ((r << 3) + (r << 1));
                }
                buff [pos --] = (char) (n + '0');
                n = m /= 10;
            }
            buff [pos --] = (char) (m + '0');
        }
        return new String (buff, pos, 16 - pos);
    }

    /**
     * 将字符串形式的IP地址表达式转成整数形式.
     * 逆操作参见 {@link #ipToString(int)}
     * @param ip 字符串形式的IP地址表达式
     * @return IP地址的整数值
     */
    public static int stringToIp (String ip) {
        int start, n, i, j, t;
        char[] buff = ip.toCharArray ();
        start = n = t = 0;
        for (i = 0; i < buff.length; i ++) {
            t = 0;
            if (buff [i] == '.') {
                for (j = 0; j < (i - start); j ++) {
                    t = (t << 3) + (t << 1) + (buff [j + start] - '0');
                }
                n = (n << 8) | t;
                start = i + 1;
            }
        }
        for (j = 0; j < (buff.length - start); j ++) {
            t = (t << 3) + (t << 1) + (buff [j + start] - '0');
        }
        n = (n << 8) | t;
        return n;
/*
        String[] tmp = ip.split ("\\.");
        byte[] buff = new byte[4];
        for (int i = 0; i < 4; i ++) {
            buff [i] = (byte) (Integer.parseInt (tmp [i]) & 0xff);
        }
        return bytesToInt (buff);
*/
    }

    public static byte[] memset (byte value, int length) {
        byte[] buff = new byte[length];
        memset (buff, value);
        return buff;
    }

    public static void memset (byte[] buff, byte value) {
        for (int i = 0; i < buff.length; i ++) {
            buff [i] = value;
        }
    }

    public static void memset (byte[] buff, byte value, int start, int length) {
        for (int i = start; i < start + length; i ++) {
            buff [i] = value;
        }
    }

    public static String toString (int[] array) {
        return Arrays.toString (array);
    }

    private static void println (byte[] buff) throws IOException {
        println (buff, System.out);
    }

    private static void println (byte[] buff, OutputStream out) throws IOException {
        out.write (toHex (buff).getBytes ());
        out.write ('\r');
        out.write ('\n');
        out.flush ();
    }

    public static byte[] crc16 (byte[] data) {
        int register = 0xffff, mirror = 0xa001, tmp;

        for (int n : data) {
            register ^= n & 0xff;
            for (int i = 0; i < 8; i ++) {
                tmp = register & 1;
                register >>= 1;
                if (tmp == 1) {
                    register ^= mirror;
                }
            }
        }
        return intToBytes (register, 2, true);
    }

    public static int checksum8 (byte[] data) {
        int sum = 0;
        for (int b : data) {
            sum += b & 0xff;
        }

        return sum & 0xff;
    }

    public static byte[] checksum16 (byte[] data) {
        int sum = 0;
        for (int b : data) {
            sum += b & 0xff;
        }
        sum &= 0xffff;
        return Tools.intToBytes (sum, 2);
    }

    public static int xor (byte[] data) {
        int n = 0;
        for (byte b : data) {
            n ^= b & 0xff;
        }
        return n & 0xff;
    }

    public static void whitening (byte[] data, int offset, int length, int pn9) {
        int c;
        for (int i = offset; i < offset + length; i ++) {
            data[i] ^= (pn9 & 0xff);
            for (int j = 0; j < 8; j ++) {
                c = pn9 & 0x21;
                pn9 >>= 1;
                if ((c == 0x21) || (c == 0)) {
                    pn9 &= 0xff;
                } else {
                    pn9 |= 0x100;
                }
            }
        }
    }

    @Deprecated
    public static void whiting (byte[] data, int offset, int length, int pn9) {
        whitening (data, offset, length, pn9);
    }

    public static int toBinaryString (InputStream in, OutputStream out) throws IOException {
        return toBinaryString (in, out, 8, -1);
    }

    public static int toBinaryString (InputStream in, OutputStream out, int offset, int length) throws IOException {
        if (offset < 0) {
            offset = 0;
        }
        if (offset > 0) {
            in.skip (offset);
        }

        if (length < 0) {
            length = Integer.MAX_VALUE;
        }

        int b, i = 0, j = 0, k, m;
        while (((b = in.read ()) > -1) && (j < length)) {
            if (j != 0 && j % 8 == 0) {
                out.write (CRLF);
                i += 2;
            }
            for (k = 0; k < 8; k ++) {
                m = 1 << (7 - k);
                if ((b & m) != 0) {
                    out.write ('1');
                } else {
                    out.write ('0');
                }
                i ++;
            }
            j ++;
            if (j != 0 && j % 8 != 0) {
                out.write (' ');
                i ++;
            }
        }

        return i;
    }

    public static String toBinaryString (byte[] buff) {
        return toBinaryString (buff, 0, buff.length);
    }

    public static String toBinaryString (byte[] buff, int offset, int length) {
        if (buff == null) {
            throw new NullPointerException ();
        }

        if (offset < 0 || (offset + length > buff.length)) {
            throw new ArrayIndexOutOfBoundsException ();
        }

        int count = length * 8;
        int lines = count / 8;
        if (count % 8 != 0) {
            lines ++;
        }
        count += lines * 2;
        char[] data = new char[count];

        int b, i = 0, j = 0, k, m, n;
        for (n = 0; n < length; n ++) {
            b = buff [offset + n];
            if (j != 0 && j % 8 == 0) {
                data [i ++] = '\r';
                data [i ++] = '\n';
            }
            for (k = 0; k < 8; k ++) {
                m = 1 << (7 - k);
                if ((b & m) != 0) {
                    data [i ++] = '1';
                } else {
                    data [i ++] = '0';
                }
            }
            j ++;
            if (j != 0 && j % 8 != 0) {
                data [i ++] = ' ';
            }

        }

        return new String (data, 0, i);
    }
}