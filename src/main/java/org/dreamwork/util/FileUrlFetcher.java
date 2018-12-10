package org.dreamwork.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 13-7-30
 * Time: 下午5:13
 */
public class FileUrlFetcher implements IURLFetcher {

    @Override
    public List<Class<?>> fetchClasses (String packageName, URL url, ClassLoader loader, IClassFilter filter) throws Exception {
        String fileName = url.getFile ();
        fileName = URLDecoder.decode (fileName, ENCODING);
        File dir = new File (fileName);
        File[] files = dir.listFiles ();
        List<Class<?>> list = new ArrayList<Class<?>> ();
        if (files != null) for (File file : files) {
            String className = file.getName ();
            if (! className.endsWith (".class")) continue;

            className = className.substring (0, className.lastIndexOf ('.'));
            if (filter == null || filter.accept (className))
                list.add (loader.loadClass (packageName + '.' + className));
        }
        return list;
    }

    @Override
    public File getPhysicalFile (URL url) throws IOException, URISyntaxException {
        String path = url.getPath ();
        if (path.startsWith ("file:")) path = path.substring ("file:".length ());
        return new File (path);
    }
}
