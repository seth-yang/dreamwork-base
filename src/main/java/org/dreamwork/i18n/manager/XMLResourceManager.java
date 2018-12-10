package org.dreamwork.i18n.manager;

import org.dreamwork.i18n.AbstractResourceManager;
import org.dreamwork.i18n.IResourceAdapter;
import org.dreamwork.i18n.LocaleUtil;
import org.dreamwork.i18n.MissingResourceException;
import org.dreamwork.i18n.adapters.XMLResourceAdapter;
import org.dreamwork.util.FileInfo;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-10-19
 * Time: 下午5:44
 */
public class XMLResourceManager extends AbstractResourceManager {
    private File base;
    private Locale defaultLocale;

    public XMLResourceManager (String baseDir, Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
        try {
            base = new File (baseDir);
            if (!base.exists ())
                throw new MissingResourceException ("Base folder: " + base.getCanonicalPath () + " not exists!");
        } catch (IOException ex) {
            throw new MissingResourceException (ex);
        }
    }

    @Override
    protected IResourceAdapter createResourceAdapter (String baseName) {
        Set<URL> set = new HashSet<URL> ();
        File[] files = base.listFiles (new NamedResourceFilter (baseName, true));
        for (File file : files) {
            try {
                if (file.isFile () && file.canRead ()) set.add (file.toURI().toURL ());
                else if (file.isDirectory ()) {
                    File[] fs = file.listFiles (new NamedResourceFilter (baseName));
                    for (File f : fs) {
                        if (f.isFile () && f.canRead ()) set.add (f.toURI().toURL ());
                    }
                }
            } catch (IOException ex) {
                // ignore
            }
        }
        try {
            URL[] urls = new URL[set.size ()];
            return new XMLResourceAdapter (this, defaultLocale, set.toArray (urls));
        } catch (Exception ex) {
            throw new MissingResourceException (ex);
        }
    }

    private static class NamedResourceFilter implements FileFilter {
        private String baseName;
        private boolean includeDirs;

        public NamedResourceFilter (String baseName) {
            this (baseName, false);
        }

        public NamedResourceFilter (String baseName, boolean includeDirs) {
            this.baseName = baseName;
            this.includeDirs = includeDirs;
        }

        public boolean accept (File file) {
            if (file.isDirectory ()) {
                if (!includeDirs) return false;
                String name = file.getName ();
                return LocaleUtil.isValidLocale (name);
            } else {
                String fileName = file.getName ();
                String ext = FileInfo.getExtension (fileName);
                fileName = FileInfo.getFileNameWithoutExtension (fileName);
                return baseName.equals (fileName) && "xml".equalsIgnoreCase (ext);
            }
        }
    }
}