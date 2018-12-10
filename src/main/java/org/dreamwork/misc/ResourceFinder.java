package org.dreamwork.misc;

import org.dreamwork.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by seth.yang on 2014/12/6
 */
public class ResourceFinder {
    public static InputStream findResource(String resourceName, String resourceFileName)
            throws IOException
    {
        if (!StringUtil.isEmpty(resourceName)) {
            String name = System.getProperty(resourceName);
            if (!StringUtil.isEmpty (name)) {
                File file = new File (name);
                if (file.exists()) {
                    return new FileInputStream (file);
                }
            }
        }
        ClassLoader loader = ResourceFinder.class.getClassLoader();
        URL url = loader.getResource(resourceFileName);
        if (url != null) {
            return url.openStream();
        }
        url = loader.getResource("META-INF/" + resourceFileName);
        if (url != null) {
            return url.openStream();
        }
        return null;
    }
}
