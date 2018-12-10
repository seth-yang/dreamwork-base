package org.dreamwork.config;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-3-29
 * Time: 下午3:28
 */
public interface FileChangeListener {
    void fileChanged (File file) throws IOException;
}
