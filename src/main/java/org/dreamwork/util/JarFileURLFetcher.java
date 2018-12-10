package org.dreamwork.util;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 13-7-30
 * Time: 下午5:28
 */
public class JarFileURLFetcher implements IURLFetcher {
    @Override
    public List<Class<?>> fetchClasses (String packageName, URL url, ClassLoader loader, IClassFilter filter) throws Exception {
        String name = packageName.replace('.', '/');
        List<Class<?>> list = new ArrayList<Class<?>> ();
        JarFile jar = ((JarURLConnection) url.openConnection ()).getJarFile ();
        for (Enumeration<JarEntry> e = jar.entries (); e.hasMoreElements ();) {
            String s = e.nextElement ().getName ();
            if (s.charAt (0) == '/') s = s.substring (1);
            if (s.startsWith (name) && s.endsWith (".class")) {
                String className = s.substring (0, s.length () - 6).replace ('/', '.');
                if (filter == null || filter.accept (className)) {
                    list.add (loader.loadClass (className));
                }
            }
        }
        return list;
    }

    @Override
    public File getPhysicalFile (URL url) throws IOException, URISyntaxException {
        JarURLConnection conn = (JarURLConnection) url.openConnection ();
        return new File (conn.getJarFile ().getName ());
    }
}
