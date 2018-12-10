package org.dreamwork.i18n;

import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-21
 * Time: 下午4:48
 */
public class LocaleUtil {
    public static Set<String> VALID_LOCALE_NAME = new HashSet<String> ();
    static {
        for (Locale locale : Locale.getAvailableLocales ())
            VALID_LOCALE_NAME.add (locale.toString ());
    }

    public static boolean isValidLocale (String name) {
        return VALID_LOCALE_NAME.contains (name);
    }

    public static Locale findClosetLocale (Collection<Locale> c, Locale locale) {
        if (c == null || c.size () == 0) return null;
        if (c.contains (locale)) return locale;

        for (Locale l : c) {
            if (l.getLanguage ().equals (locale.getLanguage ()))
                return l;
        }
        return null;
    }

    public static Locale parseLocale (String language) {
        if (language.contains ("_")) {
            String[] a = language.split ("_");
            if (a.length == 2) return new Locale (a[0], a[1]);
            return new Locale (a[0], a[1], a[2]);
        }
        return new Locale (language);
    }
}
