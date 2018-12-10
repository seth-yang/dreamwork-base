package org.dreamwork.misc;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-3-1
 * Time: 下午7:35
 */
public class IPTools {
    public static byte[] getIpByteArrayFromString (String ip) {
        String[] a = ip.split ("\\.");
        byte[] buff = new byte[4];
        for (int i = 0; i < a.length; i ++) {
            buff [i] = (byte) Integer.parseInt (a[i]);
        }
        return buff;
    }

    public static String getIpStringFromBytes (byte[] ip) {
        StringBuilder builder = new StringBuilder ();
        for (int i = 0; i < 4; i ++) {
            if (builder.length () > 0) builder.append ('.');
            builder.append (ip [i] & 0xff);
        }
        return builder.toString ();
    }

    public static String getIpStringFromLong (long ip) {
        return ((ip >> 24) & 0xff) + "." +
               ((ip >> 16) & 0xff) + "." +
               ((ip >>  8) & 0xff) + "." +
               (ip & 0xff);
    }

    public static String getIpStringFromInt (int ip) {
        return getIpStringFromLong (ip);
    }

    public static long getIpLongFromString (String ip) {
        byte[] buff = getIpByteArrayFromString (ip);
        return ((buff[0] << 24) & 0xff000000L) |
               ((buff[1] << 16) & 0xff0000L) |
               ((buff[2] << 8) & 0xff00L) |
               (buff [3] & 0xffL);
    }
}
