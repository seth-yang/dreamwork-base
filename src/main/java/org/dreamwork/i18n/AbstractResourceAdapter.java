package org.dreamwork.i18n;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.MessageFormat;
import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-18
 * Time: 下午4:14
 */
public abstract class AbstractResourceAdapter implements IResourceAdapter {
    private final Object locker = new Object ();

    protected final Map<Locale, IResourceBundle> softCache = new HashMap<> ();
    protected String resourceName;
    protected IResourceBundle defaultResourceBundle;

    private static final Logger logger = LoggerFactory.getLogger (AbstractResourceAdapter.class);

    public void loadResources (String baseName, Locale defaultLocale) throws MissingResourceException {
        this.resourceName = baseName;
        defaultResourceBundle = loadResourceByLocale (defaultLocale);
        if (defaultResourceBundle == null)
            throw new MissingResourceException ("Default locale: " + defaultLocale + " can't find!");
        softCache.put (defaultLocale, defaultResourceBundle);
    }

    public String getString (Locale locale, String name) {
        return getString (locale, name, name);
    }

    public String getString (Locale locale, String name, Object... parameters) {
        return getString (locale, name, "", parameters);
    }

    public String getString (Locale locale, String name, String defaultValue) {
        if (locale == null)
            return defaultResourceBundle.getString (name, defaultValue);

        IResourceBundle res;
        synchronized (locker) {
            res = findResourceBundle (locale);
        }

        if (res == null) // 没有对应的区域设置，返回默认语言
            return defaultResourceBundle.getString (name, defaultValue);

        String value = res.getString (name, null);
        if (value == null) { // 在对应的区域语言中未找到对应的资源，寻找最接近的语言
            if (logger.isTraceEnabled ())
                logger.trace ("Can't find resource [" + name + "] in locale [" + locale.getDisplayName () + "], trying to find a closet one.");
            for (Locale l : softCache.keySet ()) {
                if (l.equals (locale)) continue;

                if (l.getLanguage ().equals (locale.getLanguage ())) {
                    if (logger.isTraceEnabled ())
                        logger.trace ("Trying find resource in locale [" + l.getDisplayName () + "]");
                    IResourceBundle bundle = softCache.get (l);
                    if (bundle.isResourcePresent (name)) {
                        if (logger.isTraceEnabled ())
                            logger.trace ("Resource [" + name + "] found in locale [" + l.getDisplayName () + "]");
                        value = bundle.getString (name, null);
                        break;
                    }
                }
            }
        }

        if (value == null) {
            if (logger.isTraceEnabled ())
                logger.trace ("Can't find resource in language [" + locale.getDisplayLanguage () + "], returning resource in default locale");
            return defaultResourceBundle.getString (name, defaultValue);
        }
        return value;
    }

    public String getString (Locale locale, String name, String defaultValue, Object... parameters) {
        String value = getString (locale, name, defaultValue);
        if (parameters.length > 0 && !isEmpty (value)) {
            return MessageFormat.format (value, parameters);
        }
        return value;
    }

    protected static boolean isEmpty (String text) {
        return text == null || text.trim ().length () == 0;
    }

    protected boolean isLocaleSupport (Locale locale) {
        return softCache.containsKey (locale);
    }

    protected IResourceBundle getResourceBundle (Locale locale) {
        IResourceBundle bundle = softCache.get (locale);
        if (bundle == null) {
            bundle = loadResourceByLocale (locale);
            if (bundle != null)
                softCache.put (locale, bundle);
        }
        return bundle;
    }

    protected IResourceBundle findResourceBundle (Locale locale) {
        IResourceBundle bundle = softCache.get (locale);
        if (bundle == null) {
            if (logger.isTraceEnabled ())
                logger.trace ("Can't match locale [" + locale + "], trying to match a closet one");
            for (Locale l : softCache.keySet ()) {
                if (l.getLanguage ().equals (locale.getLanguage ())) {
                    if (logger.isDebugEnabled ())
                        logger.debug ("match [" + locale + "] to [" + l + "]");
                    return softCache.get (l);
                }
            }
        }
        return bundle;
    }

    /**
     * 装载指定名称和区域设置的资源绑定器.
     * @param locale 区域设置
     * @return 资源绑定器
     */
    protected abstract IResourceBundle loadResourceByLocale (Locale locale);

    public Collection<LocaleWarp> getSupportedLocales () {
        SortedSet<LocaleWarp> set = new TreeSet<> ();
        for (Locale locale : softCache.keySet ())
            set.add (new LocaleWarp (locale));
        return set;
    }
}