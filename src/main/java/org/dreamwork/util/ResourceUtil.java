package org.dreamwork.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 13-7-30
 * Time: 上午11:37
 */
public class ResourceUtil {
    private static final Map<String, IURLFetcher> caches = new WeakHashMap<String, IURLFetcher> ();

    public static List<Class<?>> getClasses (String packageName) throws Exception {
        return getClasses (packageName, ResourceUtil.class.getClassLoader (), null);
    }

    public static List<Class<?>> getClasses (String packageName, IClassFilter filter) throws Exception {
        return getClasses (packageName, ResourceUtil.class.getClassLoader (), filter);
    }

    public static List<Class<?>> getClasses (String packageName, ClassLoader loader) throws Exception {
        return getClasses (packageName, loader, null);
    }

    public static List<Class<?>> getClasses (String packageName, ClassLoader loader, IClassFilter filter) throws Exception {
        String name = packageName.replace('.', '/');
        Enumeration<URL> e = loader.getResources (name);
        if (e == null)
            return new ArrayList<Class<?>> ();

        List<Class<?>> list = new ArrayList<Class<?>> ();
        for (; e.hasMoreElements (); ) {
            URL url = e.nextElement ();
            IURLFetcher fetcher = findURLFetcher (url, loader);
            List<Class<?>> ret = fetcher.fetchClasses (packageName, url, loader, filter);
            if (ret != null && ret.size () > 0)
                list.addAll (ret);
        }

        return list;
    }

    public static File getPhysicalFile (Class clazz) throws Exception {
        ClassLoader loader = clazz.getClassLoader ();
        String classFileName = clazz.getName ().replace ('.', '/') + ".class";
        URL url = loader.getResource (classFileName);
        if (url != null) {
            IURLFetcher fetcher = findURLFetcher (url, loader);
            if (fetcher != null)
                return fetcher.getPhysicalFile (url);
        }
        return null;
    }

    private static IURLFetcher findURLFetcher (URL url, ClassLoader loader) throws Exception {
        String protocol = url.getProtocol ();
        if (caches.containsKey (protocol))
            return caches.get (protocol);

        IURLFetcher fetcher;
        if ("file".equalsIgnoreCase (protocol))
            fetcher = new FileUrlFetcher ();
        else if ("jar".equalsIgnoreCase (protocol))
            fetcher = new JarFileURLFetcher ();
        else {
            String fetcherClassName = findURLFetcherName (loader, protocol);
            if (StringUtil.isEmpty (fetcherClassName))
                throw new RuntimeException ("Can't find URLFetcher for protocol: " + protocol);

            Class<?> c = loader.loadClass (fetcherClassName.trim ());
            fetcher = (IURLFetcher) c.newInstance ();
        }

        caches.put (protocol, fetcher);
        return fetcher;
    }

    private static String findURLFetcherName (ClassLoader loader, String protocol) throws IOException {
        String propertyName = "url.fetcher." + protocol;
        if (System.getProperties ().containsKey (propertyName))
            return System.getProperty (propertyName);

        Enumeration<URL> urls = loader.getResources ("META-INF/url-fetcher.properties");
        while (urls.hasMoreElements ()) {
            URL url = urls.nextElement ();
            Properties props = new Properties ();
            props.load (url.openStream ());

            if (props.containsKey (propertyName))
                return props.getProperty (propertyName);
        }

        return null;
    }
}