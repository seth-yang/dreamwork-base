package org.dreamwork.util;

import java.io.*;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterOutputStream;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 14-11-28
 * Time: 下午6:26
 */
public class IOUtil {
    private static final int MAX_LENGTH = 1024 * 1024;

    public static byte[] deflate (byte[] data) {
        Deflater deflater = new Deflater (8, true);
        deflater.setInput (data);
        byte[] buff = new byte [data.length];
        deflater.finish ();
        int length = deflater.deflate (buff);
        byte[] a = new byte[length];
        System.arraycopy (buff, 0, a, 0, length);
        return a;
    }

    public static void deflate (InputStream in, OutputStream out) throws IOException {
        Deflater deflater = new Deflater (8, true);
        DeflaterOutputStream dout = new DeflaterOutputStream (out, deflater);
        dump (in, dout);
        dout.finish ();
    }

    public static byte[] inflate (byte[] data) {
        try {
            Inflater inflater = new Inflater (true);
            inflater.setInput (data);
            byte[] buff = new byte[1024];
            ByteArrayOutputStream baos = new ByteArrayOutputStream ();
            while (!inflater.finished ()) {
                int length = inflater.inflate (buff);
                baos.write (buff, 0, length);
            }
            return baos.toByteArray ();
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    public static void inflate (InputStream in, OutputStream out) throws IOException {
        Inflater inflater = new Inflater (true);
        InflaterOutputStream iout = new InflaterOutputStream (out, inflater);
        dump (in, iout);
        iout.finish ();
    }

    public static void dump (InputStream in, OutputStream out) throws IOException {
        byte[] buff = new byte[1024];
        int length;
        while ((length = in.read (buff)) != -1) {
            out.write (buff, 0, length);
        }
        out.flush ();
    }

    public static void dump (InputStream in, OutputStream out, long length) throws IOException {
        int buffSize = (int) Math.min (MAX_LENGTH, length), size = 0, readed;
        byte[] buff = new byte[buffSize];
        while ((readed = in.read (buff, 0, buffSize)) != -1) {
            out.write (buff, 0, readed);
            size += readed;
            buffSize = (int) Math.min (MAX_LENGTH, length - size);
            if (buffSize <= 0) break;
        }
        out.flush ();
    }

    public static void dump (File file, OutputStream out) throws IOException {
        int size = (int) Math.max (file.length (), MAX_LENGTH);
        InputStream in = new FileInputStream (file);
        try {
            dump (in, out, size);
        } finally {
            in.close ();
        }
    }

    private static void dump (InputStream in, OutputStream out, int size) throws IOException {
        int length;
        byte[] buff = new byte [size];
        while ((length = in.read (buff)) != -1) {
            out.write (buff, 0, length);
            out.flush ();
        }
    }

    public static byte[] read (InputStream in, int length, long timeout, boolean throwException) throws IOException {
        long timestamp = System.currentTimeMillis ();

        while (true) {
            int n = in.available ();
            if (n >= length) {
                byte[] buff = new byte[length];
                in.read (buff);
                return buff;
            } else {
                long delta = System.currentTimeMillis () - timestamp;
                if (delta > timeout) {
                    if (throwException)
                        throw new IOException ("read time out");
                    else
                        return null;
                }
            }

            try {
                Thread.sleep (200);
            } catch (InterruptedException ex) {
                throw new IOException (ex);
            }
        }
    }

    public static byte[] read (InputStream in) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        byte[] buff = new byte[1024];
        int length;
        while ((length = in.read (buff)) != -1)
            baos.write (buff, 0, length);
        return baos.toByteArray ();
    }

    public static byte[] read (File file) throws IOException {
        FileInputStream fis = null;
        try{
            fis = new FileInputStream(file);
            return read(fis);
        }finally{
            if (fis != null) fis.close ();
        }
    }

    public static void write (byte[] buff, OutputStream out) throws IOException {
        out.write (buff, 0, buff.length);
    }

    public static void write (byte[] buff, File file) throws IOException {
        FileOutputStream fos = new FileOutputStream (file);
        try {
            fos.write (buff, 0, buff.length);
        } finally {
            fos.close ();
        }
    }
}