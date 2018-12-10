package org.dreamwork.misc;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2014/12/15
 * Time: 23:20
 */
public class SerialNumber {
    private long max;
    private String prefix;
    private boolean cycle;
    private ISerialNumberSequence sequence;

    private DecimalFormat format;

    private final Object locker = new byte[0];

    private static final Map<String, SerialNumber> cache = new WeakHashMap<String, SerialNumber> ();

    public static SerialNumber newSerialNumber (String prefix, int length) {
        return newSerialNumber (prefix, length, 1);
    }

    public static SerialNumber newSerialNumber (String prefix, int length, long value) {
        return newSerialNumber (prefix, length, value, true, new DefaultSerialNumberSequence ());
    }

    public static SerialNumber newSerialNumber (String prefix, int length, ISerialNumberSequence sequence) {
        return newSerialNumber (prefix, length, 1, true, sequence);
    }

    public static SerialNumber newSerialNumber (String prefix, int length, long value, boolean cycle, ISerialNumberSequence sequence) {
        synchronized (cache) {
            if (cache.containsKey (prefix))
                return cache.get (prefix);

            SerialNumber sn = new SerialNumber (prefix, length, value, cycle, sequence);
            cache.put (prefix, sn);
            return sn;
        }
    }

    private SerialNumber (String prefix, int length, long value, boolean cycle, ISerialNumberSequence sequence) {
        this.prefix = prefix;
        this.cycle = cycle;
        this.sequence = sequence;

        if (length <= 0)
            throw new IllegalArgumentException ();
        max = 1;
        StringBuilder builder = new StringBuilder ();
        for (int i = 0; i < length; i ++) {
            max *= 10;
            builder.append ('0');
        }
        max = max - 1;
        format = new DecimalFormat (builder.toString ());
        this.sequence.set (value);
        this.sequence.restore ();
    }

    public String next () {
        synchronized (locker) {
            long value = sequence.get ();
            if (max <= value) {
                if (cycle) {
                    sequence.set (1);
                    value = 1;
                } else {
                    throw new SNCycleException ();
                }
            } else {
                value = sequence.next ();
            }
            return prefix + format.format (value);
        }
    }
}