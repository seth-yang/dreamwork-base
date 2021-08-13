package org.dreamwork.util;

import java.lang.reflect.Array;

public class GroupJob {
    public interface Processor<R, T> {
        R process (T[] data);
    }

    public static<R, T> R groupJob (Class<T> type, T[] raw, int groupSize, R defaultValue, Processor<R, T> processor) {
        int index = 0;
        while (index < raw.length) {
            int count = Math.min (raw.length - index, groupSize);
            @SuppressWarnings ("unchecked")
            T[] group = (T[]) Array.newInstance (type, count);
            System.arraycopy (raw, 0, group, 0, count);
            R result = processor.process (group);
            if (result == null || !result.equals (defaultValue)) {
                return result;
            }

            index += groupSize;
        }

        return defaultValue;
    }

    public static<R> R groupJob (int[] raw, int groupSize, R defaultValue, Processor<R, Integer> processor) {
        int index = 0;
        while (index < raw.length) {
            int count = Math.min (raw.length - index, groupSize);
            Integer[] part = new Integer[count];
            for (int i = 0; i < count; i ++) {
                part[i] = raw[index + i];
            }
            R result = processor.process (part);
            if (result == null || !result.equals (defaultValue)) {
                return result;
            }

            index += groupSize;
        }
        return defaultValue;
    }
}