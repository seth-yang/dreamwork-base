package org.dreamwork.i18n;

import java.io.Serializable;
import java.util.Locale;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-22
 * Time: 下午8:38
 */
public class LocaleWarp implements Serializable, Comparable<LocaleWarp> {
    private Locale locale;

    public LocaleWarp (Locale locale) {
        this.locale = locale;
    }

    public Locale getLocale () {
        return locale;
    }

    public String getDisplayLanguage () {
        return locale.getDisplayLanguage (locale);
    }

    public String getDisplayCountry () {
        return locale.getDisplayCountry (locale);
    }

    public String getDisplayName () {
        return locale.getDisplayName (locale);
    }

    public int compareTo (LocaleWarp o) {
        if (o == null) return 1;
        int result = locale.getLanguage ().compareTo (o.locale.getLanguage ());
        if (result != 0) return result;

        result = locale.getCountry ().compareTo (o.locale.getCountry ());
        if (result != 0) return result;

        return locale.getVariant ().compareTo (o.locale.getVariant ());
    }
}
