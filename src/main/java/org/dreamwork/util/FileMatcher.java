package org.dreamwork.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-4-13
 * Time: 下午7:27
 */
public class FileMatcher<T> {
    private FileFilter filter;
    private File basedir;
    private String includes, excludes;
    private Class<T> type;

    private Map<String, Pattern> cache = new HashMap<String, Pattern> ();

    private static final Pattern P = Pattern.compile ("[,\\s;\\|]");

    public FileMatcher (File basedir, Class<T> type) {
        this.basedir = basedir;

        if (type != File.class && type != URL.class && type != URI.class)
            throw new IllegalArgumentException ("Unspport type: " + type.getCanonicalName ());

        this.type = type;
    }

    public FileFilter getFilter () {
        return filter;
    }

    public void setFilter (FileFilter filter) {
        this.filter = filter;
    }

    public String getIncludes () {
        return includes;
    }

    public void setIncludes (String includes) {
        this.includes = includes;
    }

    public String getExcludes () {
        return excludes;
    }

    public void setExcludes (String excludes) {
        this.excludes = excludes;
    }

    @SuppressWarnings ("unchecked")
    public java.util.Collection<T> getMatcheFiles () throws IOException {
        if (!basedir.exists ()) return new HashSet<T> ();
        File[] files;
        if (filter == null) files = basedir.listFiles ();
        else files = basedir.listFiles (filter);

        Set<String> includeNames = new HashSet<String> ();
        Set<String> excludeNames = new HashSet<String> ();
        if (!StringUtil.isEmpty (includes)) {
            String[] a = P.split (includes);
            includeNames.addAll (Arrays.asList (a));
        }
        if (!StringUtil.isEmpty (excludes)) {
            String[] a = P.split (excludes);
            excludeNames.addAll (Arrays.asList (a));
        }

        Map<File, T> libs = new HashMap<File, T> ();
        if (includeNames.size () == 0) { // 未指定 includes
            Set<File> excludeFiles = new HashSet<File> ();
            for (File jar : files) {
                jar = jar.getCanonicalFile ();
                if (type == URL.class)
                    libs.put (jar, (T) jar.toURI ().toURL ());
                else if (type == File.class)
                    libs.put (jar, (T) jar);
                else
                    libs.put (jar, (T) jar.toURI ());
                if (match (jar.getName (), excludeNames)) excludeFiles.add (jar);
            }

            if (excludeFiles.size () == 0) return libs.values ();
            for (File jar : excludeFiles) if (libs.containsKey (jar))
                libs.remove (jar);
        } else {  // 指定 includes
            Set<File> excludeFiles = new HashSet<File> ();
            for (File jar : files) {
                jar = jar.getCanonicalFile ();
                String name = jar.getName ();
                if (match (name, includeNames)) {
                    if (type == URL.class)
                        libs.put (jar, (T) jar.toURI ().toURL ());
                    else if (type == File.class)
                        libs.put (jar, (T) jar);
                    else
                        libs.put (jar, (T) jar.toURI ());
                }
                if (match (name, excludeNames))
                    excludeFiles.add (jar);
            }

            if (excludeFiles.size () == 0) return libs.values ();
            for (File jar : excludeFiles)
                if (libs.containsKey (jar))
                    libs.remove (jar);
        }

        return libs.values ();
    }

    private boolean match (String name, Set<String> set) {
        for (String text : set) {
            Pattern p = cache.get (text);
            if (p == null) {
                String s = text.replace ("-", "\\-").replace (".", "\\.").replace ("?", ".").replace ("*", "(.*?)");
                p = Pattern.compile ("^" + s + "$", Pattern.CASE_INSENSITIVE);
                cache.put (text, p);
            }

            Matcher m = p.matcher (name);
            if (m.matches ()) return true;
        }
        return false;
    }
}