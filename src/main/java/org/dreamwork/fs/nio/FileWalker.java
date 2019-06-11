package org.dreamwork.fs.nio;

import org.dreamwork.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by seth.yang on 2017/4/17
 */
public class FileWalker<T extends FileIndex> implements IFileMonitorListener {
    private FileMonitor monitor;
    private IFileIndexAdapter<T> adapter;
    private Set<String> categories = new HashSet<> ();
    private List<IFileWalkListener<T>> listeners = new ArrayList<> ();

    private static final Logger logger = LoggerFactory.getLogger (FileWalker.class);

    public FileWalker (IFileIndexAdapter<T> adapter) throws IOException {
        this.adapter = adapter;
        this.monitor = new FileMonitor ();
        monitor.addListener (this);
        monitor.monitor ();

        Set<String> set = adapter.getCategories ();
        if (set != null && !set.isEmpty ()) {
            categories.addAll (set);
            for (String p : set) {
                monitor.addPath (Paths.get (p));
            }
        }
    }

    public void dispose () {
        if (monitor != null) {
            monitor.removeListener (this);
            monitor.dispose ();
        }
    }

    public IFileIndexAdapter<T> getAdapter () {
        return adapter;
    }

    public Set<String> getMappedCategories () {
        return categories;
    }

    public void addFileWalkListener (IFileWalkListener<T> listener) {
        listeners.add (listener);
    }

    public void removeFileWalkListener (IFileWalkListener<T> listener) {
        listeners.remove (listener);
    }

    public void add (Path path) throws IOException {
        if (path == null) {
            throw new NullPointerException ();
        }
        path = path.toAbsolutePath ();
        categories.add (path.toString ());

        if (!Files.isDirectory (path)) {
            throw new NotDirectoryException (path.toString ());
        }

        if (!Files.exists (path)) {
            throw new FileNotFoundException (path.toString ());
        }

        if (!Files.isReadable (path)) {
            throw new SecurityException ("directory cannot read.");
        }

        monitor.addPath (path);
        Files.walkFileTree (path, new CategoryFileVisitor<> (path.toString (), adapter));
    }

    @Override
    public void onCreate (Path path) throws IOException {
        if (logger.isTraceEnabled ()) {
            logger.trace ("{} is create.", path);
        }
        if (Files.isRegularFile (path) && adapter.accept (path, null)) {
            if (logger.isTraceEnabled ()) {
                logger.trace ("creating the index of {}", path);
            }
            String category = null;
            for (String c : categories) {
                if (path.startsWith (c)) {
                    category = c;
                    break;
                }
            }

            if (!StringUtil.isEmpty (category)) {
                T index = adapter.save (category, path);
                List<IFileWalkListener<T>> copy = copyListeners ();
                for (IFileWalkListener<T> fwl : copy) {
                    fwl.onCreate (index);
                }
            }
        }
    }

    @Override
    public void onDelete (Path path) throws IOException {
        if (logger.isTraceEnabled ()) {
            logger.trace ("the path {} deleted.", path);
        }
        if (adapter.accept (path, null)) {
            String category = findCategory (path);
            if (category != null) {
                T index = adapter.remove (category, path);
                if (index != null) {
                    List<IFileWalkListener<T>> copy = copyListeners ();
                    for (IFileWalkListener<T> fwl : copy) {
                        fwl.onDelete (index);
                    }
                }
            }
        }
    }

    @Override
    public void onModify (Path path) throws IOException {
        if (logger.isTraceEnabled ()) {
            logger.trace ("the path {} modified", path);
        }
        if (adapter.accept (path, null)) {
            String category = findCategory (path);
            if (!StringUtil.isEmpty (category)) {
                T index = adapter.update (category, path);
                if (index != null) {
                    List<IFileWalkListener<T>> copy = copyListeners ();
                    for (IFileWalkListener<T> fwl : copy) {
                        fwl.onModify (index);
                    }
                }
            }
        }
    }

    private String findCategory (Path path) {
        for (String c : categories) {
            if (path.startsWith (c)) {
                return c;
            }
        }

        return null;
    }

    private synchronized List<IFileWalkListener<T>> copyListeners () {
        return new ArrayList<> (listeners);
    }
}