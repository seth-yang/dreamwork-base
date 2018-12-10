package org.dreamwork.fs.nio;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Created by seth.yang on 2017/4/17
 */
public interface IFileMonitorListener {
    void onCreate (Path path) throws IOException;
    void onDelete (Path path) throws IOException;
    void onModify (Path path) throws IOException;
}