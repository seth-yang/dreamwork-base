package org.dreamwork.i18n.adapters;

import org.dreamwork.i18n.AbstractResourceAdapter;
import org.dreamwork.i18n.IResourceBundle;

import java.util.Locale;
import java.util.ResourceBundle;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-18
 * Time: 下午4:45
 */
public class JdkResourceAdapter extends AbstractResourceAdapter {
    @Override
    protected IResourceBundle loadResourceByLocale (Locale locale) {
        return new JdkResourceBundle (resourceName, locale);
    }

    @Override
    public String getString (Locale locale, String name, String defaultValue) {
        IResourceBundle bundle = getResourceBundle (locale);
        return bundle == null ? null : bundle.getString (name, defaultValue);
    }

    private static class JdkResourceBundle implements IResourceBundle {
        private ResourceBundle res;

        private JdkResourceBundle (String baseName, Locale locale) {
            res = ResourceBundle.getBundle (baseName, locale);
        }

        public String getString (String name, String defaultValue) {
            String value = res.getString (name);
            return value == null ? defaultValue : value;
        }

        public boolean isResourcePresent (String name) {
            return res.getString (name) != null;
        }
    }
}