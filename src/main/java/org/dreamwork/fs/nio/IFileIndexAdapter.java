package org.dreamwork.fs.nio;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Map;
import java.util.Set;

/**
 * Created by game on 2017/4/17
 */
public interface IFileIndexAdapter<T extends FileIndex> {
    T save (String category, Path path) throws IOException;
    T update (String category, Path path) throws IOException;
    T get (String category, Path path) throws IOException;
    T remove (String category, Path path) throws IOException;
    boolean accept (Path path, BasicFileAttributes attrs) throws IOException;
    Map<String, Map<Path, T>> restore () throws IOException;
    Set<String> getCategories () throws IOException;
}