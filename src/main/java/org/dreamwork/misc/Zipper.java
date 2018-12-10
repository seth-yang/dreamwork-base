package org.dreamwork.misc;

import org.dreamwork.util.IOUtil;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * Created by seth.yang on 2016/10/11
 */
public class Zipper {
    public static byte[] deflate (byte[] data) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        DeflaterOutputStream dos = new DeflaterOutputStream (baos);
        dos.write (data);
        dos.finish ();
        return baos.toByteArray ();
    }

    public static byte[] inflate (byte[] zipped) throws IOException {
        ByteArrayInputStream bais = new ByteArrayInputStream (zipped);
        InflaterInputStream iis = new InflaterInputStream (bais);
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        IOUtil.dump (iis, baos);
/*
        byte[] buff = new byte[1024];
        int length;

        while ((length = iis.read (buff)) != -1) {
            baos.write (buff, 0, length);
        }
*/
        return baos.toByteArray ();
    }
}
