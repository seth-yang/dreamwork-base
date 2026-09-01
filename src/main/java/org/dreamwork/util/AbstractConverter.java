package org.dreamwork.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import static org.dreamwork.util.CollectionHelper.isNotEmpty;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 13-8-19
 * Time: 下午1:55
 */
public abstract class AbstractConverter implements IConverter {
    private static final Map<Class<? extends IConverter>, IConverter> caches = new WeakHashMap<> ();
    private static DefaultConverter defaultConverter;

    @SuppressWarnings ("unchecked")
    public static IConverter getConverter (Class<? extends IConverter> type) {
        List<Throwable> list = new ArrayList<> (1);
        IConverter converter = caches.computeIfAbsent (type, t -> {
            ConverterInfo info = type.getAnnotation (ConverterInfo.class);
            if (info == null) {
                if (defaultConverter == null) {
                    defaultConverter = new DefaultConverter ();
                }
                caches.put (type, defaultConverter);
                return defaultConverter;
            }

            Class<IConverter> converterType = (Class<IConverter>) info.converter ();
            try {
                return converterType.getDeclaredConstructor ().newInstance ();
            } catch (Exception ex) {
                list.add (ex);
                return null;
            }
        });

        if (isNotEmpty (list)) {
            throw new RuntimeException (list.get (0));
        }
        return converter;
    }
}
