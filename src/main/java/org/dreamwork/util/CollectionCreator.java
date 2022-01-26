package org.dreamwork.util;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

@SuppressWarnings ("unused")
public class CollectionCreator {
    @SafeVarargs
    public static<K, V> Map<K, V> asMap (Function<V, K> keyTranslator, V... args) {
        if (args.length == 0) return Collections.emptyMap ();
        if (keyTranslator == null)
            throw new NullPointerException ();

        return Arrays.stream (args).collect (Collectors.toMap (keyTranslator, e -> e));
    }

    @SuppressWarnings ("unchecked")
    public static<K, V> Map<K, V> asMap (Object... args) {
        if (args.length == 0) return Collections.emptyMap ();

        Map<K, V> map = new HashMap<> ();
        for (int i = 0, n = args.length; i < n; i += 2) {
            K key = (K) args[i];
            V value = null;
            if (i + 1 < args.length) {
                value = (V) args [i + 1];
            }
            map.put (key, value);
        }
        return map;
    }

    public static<K, V> Map<K, V> asUnmodifiableMap (Object... args) {
        return Collections.unmodifiableMap (asMap (args));
    }

    public static ITypedMap asTypedMap (Object... args) {
        ITypedMap map = new TypedHashMap ();
        if (args.length == 0) return map;

        map.putAll (asMap (args));
        return map;
    }

    public static ITypedMap asUnmodifiableTypedMap (Object... args) {
        UnmodifiableTypedMap map = new UnmodifiableTypedMap ();
        if (args.length == 0) return map;
        return new UnmodifiableTypedMap (asMap (args));
    }

    @SafeVarargs
    public static<T> List<T> asList (T... items) {
        return Arrays.asList (items);
    }

    @SafeVarargs
    public static<T> List<T> asUnmodifiableList (T... items) {
        return Collections.unmodifiableList (asList (items));
    }

    @SafeVarargs
    public static<T> Set<T> asSet (T... items) {
        return Arrays.stream(items).collect (Collectors.toSet ());
    }

    @SafeVarargs
    public static<T> Set<T> asUnmodifiableSet (T... items) {
        return Collections.unmodifiableSet (asSet (items));
    }

    private static final class UnmodifiableTypedMap extends TypedHashMap {
        private UnmodifiableTypedMap () {}

        private UnmodifiableTypedMap (Map<String, Object> map) {
            super.putAll (map);
        }

        private Object _put (String key, Object value) {
            return super.put (key, value);
        }

        @Override
        public Object put (String key, Object value) {
            throw new UnsupportedOperationException ();
        }

        @Override
        public void putAll (Map<? extends String, ?> m) {
            throw new UnsupportedOperationException ();
        }

        @Override
        public Object remove (Object key) {
            throw new UnsupportedOperationException ();
        }

        @Override
        public void clear () {
            throw new UnsupportedOperationException ();
        }

        @Override
        public Object putIfAbsent (String key, Object value) {
            throw new UnsupportedOperationException ();
        }

        @Override
        public boolean remove (Object key, Object value) {
            throw new UnsupportedOperationException ();
        }

        @Override
        public boolean replace (String key, Object oldValue, Object newValue) {
            throw new UnsupportedOperationException ();
        }

        @Override
        public Object replace (String key, Object value) {
            throw new UnsupportedOperationException ();
        }

        @Override
        public Object computeIfAbsent (String key, Function<? super String, ?> mappingFunction) {
            throw new UnsupportedOperationException ();
        }

        @Override
        public Object computeIfPresent (String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
            throw new UnsupportedOperationException ();
        }

        @Override
        public Object compute (String key, BiFunction<? super String, ? super Object, ?> remappingFunction) {
            throw new UnsupportedOperationException ();
        }

        @Override
        public Object merge (String key, Object value, BiFunction<? super Object, ? super Object, ?> remappingFunction) {
            throw new UnsupportedOperationException ();
        }

        @Override
        public void replaceAll (BiFunction<? super String, ? super Object, ?> function) {
            throw new UnsupportedOperationException ();
        }
    }
}