package org.dreamwork.util;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 13-8-19
 * Time: 下午1:55
 */
public abstract class AbstractConverter implements IConverter {
    private static Map<Class, IConverter> caches = new WeakHashMap<Class, IConverter> ();
    private static DefaultConverter defaultConverter = new DefaultConverter ();

    @SuppressWarnings ("unchecked")
    public static IConverter getConverter (Class type) {
        IConverter converter = caches.get (type);
        if (converter != null)
            return converter;

        ConverterInfo info = (ConverterInfo) type.getAnnotation (ConverterInfo.class);
        if (info == null) {
            caches.put (type, defaultConverter);
            return defaultConverter;
        }

        Class<IConverter> converterType = (Class<IConverter>) info.converter ();
        try {
            return converterType.newInstance ();
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }
}
