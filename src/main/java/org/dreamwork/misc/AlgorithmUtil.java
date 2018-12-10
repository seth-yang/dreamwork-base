package org.dreamwork.misc;

import java.io.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2010-7-8
 * Time: 13:51:34
 */
public class AlgorithmUtil {
    private static final int BUFF_SIZE = 1 << 20;
    public static byte[] md5 (byte[] data) throws NoSuchAlgorithmException {
        MessageDigest mdTemp = MessageDigest.getInstance("MD5");
        mdTemp.update(data);
        return mdTemp.digest ();
    }

    public static byte[] md5 (File file) throws NoSuchAlgorithmException, IOException {
        if (!file.exists ())
            throw new FileNotFoundException ();

        long size = file.length ();
        long length = Math.min (BUFF_SIZE, size);
        return md5 (file, (int) length);
    }

    public static byte[] md5 (File file, int size) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] buff = new byte[size];
        int length;
        try (InputStream in = new FileInputStream (file)) {
            while ((length = in.read (buff)) != -1) {
                md.update(buff, 0, length);
            }

            return md.digest ();
        }
    }

    @Deprecated
    public static String md5 (String data) throws UnsupportedEncodingException, NoSuchAlgorithmException {
        byte[] ret = md5 (data.getBytes ("iso-8859-1"));
        StringBuilder builder = new StringBuilder ();
        for (byte i : ret) builder.append (String.format ("%02x", i));
        return builder.toString ();
    }
}