package org.dreamwork.util;

public class NumberHelper {
    public static double round (double n, int precision) {
        if (precision < 0) {
            throw new IllegalArgumentException ("precision cannot be negative");
        }
        if (precision == 0)
            return Math.round (n);

        int scale = 10;
        double suffix = .05;
        while (--precision > 0) {
            scale *= 10;
            suffix /= 10;
        }
        if (n > 0)
            n += suffix;
        else if (n < 0)
            n -= suffix;
        int temp = (int) (n * scale);
        return ((double) temp / scale);
    }

    public static float round (float n, int precision) {
        return (float) round ((double) n, precision);
    }

    public static String format (double n, int precision) {
        double d = round (n, precision);
        String pattern = "%." + precision + 'f';
        return String.format (pattern, d);
    }

    public static void main (String[] args) {
        System.out.println (Math.PI);
        System.out.println (round (0.07800000160932541, 2));
        System.out.println (round (Math.PI, 3));
        System.out.println (round (Math.PI, 4));
        System.out.println (round (Math.PI, 5));
        System.out.println ("--------------------------------");
        System.out.println (1);
        System.out.println (format (1, 2));
        System.out.println (format (1, 3));
        System.out.println (format (1, 4));
        System.out.println (format (1, 5));
    }
}