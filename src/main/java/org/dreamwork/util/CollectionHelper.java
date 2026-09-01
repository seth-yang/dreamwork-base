package org.dreamwork.util;

import java.util.Map;

public class CollectionHelper {
    public static boolean isEmpty (IDataCollection<?> c) {
        return c == null || c.getTotalRows () == 0;
    }

    public static boolean isEmpty (java.util.Collection<?> c) {
        return c == null || c.isEmpty ();
    }

    public static boolean isEmpty (Map<?, ?> m) {
        return m == null || m.isEmpty ();
    }

    public static<T> boolean isEmpty (T[] a) {
        return a == null || a.length == 0;
    }

    public static boolean isEmpty (byte[] a) {
        return a == null || a.length == 0;
    }

    public static boolean isEmpty (short[] a) {
        return a == null || a.length == 0;
    }

    public static boolean isEmpty (int[] a) {
        return a == null || a.length == 0;
    }

    public static boolean isEmpty (long[] a) {
        return a == null || a.length == 0;
    }

    public static boolean isEmpty (double[] a) {
        return a == null || a.length == 0;
    }

    public static boolean isEmpty (float[] a) {
        return a == null || a.length == 0;
    }

    public static boolean isNotEmpty (IDataCollection<?> c) {
        return c != null && c.getTotalRows () > 0;
    }

    public static boolean isNotEmpty (java.util.Collection<?> c) {
        return c != null && !c.isEmpty ();
    }

    public static boolean isNotEmpty (Map<?, ?> m) {
        return m != null && !m.isEmpty ();
    }

    public static<T> boolean isNotEmpty (T[] a) {
        return a != null && a.length > 0;
    }

    public static boolean isNotEmpty (byte[] a) {
        return a != null && a.length > 0;
    }

    public static boolean isNotEmpty (int[] a) {
        return a != null && a.length > 0;
    }

    public static boolean isNotEmpty (short[] a) {
        return a != null && a.length > 0;
    }

    public static boolean isNotEmpty (long[] a) {
        return a != null && a.length > 0;
    }

    public static boolean isNotEmpty (float[] a) {
        return a != null && a.length > 0;
    }

    public static boolean isNotEmpty (double[] a) {
        return a != null && a.length > 0;
    }
}