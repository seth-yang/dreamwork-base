package org.dreamwork.util;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 13-7-30
 * Time: 下午5:07
 */
public interface IURLFetcher {
    String ENCODING = "utf-8";
    List<Class<?>> fetchClasses (String packageName, URL url, ClassLoader loader, IClassFilter filter) throws Exception;
    File getPhysicalFile (URL url) throws IOException, URISyntaxException;
}