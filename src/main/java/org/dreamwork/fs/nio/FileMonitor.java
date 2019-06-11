package org.dreamwork.fs.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by seth.yang on 2017/4/17
 */
public class FileMonitor implements Runnable {
    private boolean running = true;
    private WatchService monitor;
    private Map<WatchKey, Path> cache = new HashMap<> ();
    private Set<Path> paths = new HashSet<> ();
    private ExecutorService service = Executors.newFixedThreadPool (1);
    private ExecutorService wet     = Executors.newCachedThreadPool ();
    private List<IFileMonitorListener> listeners = new ArrayList<> ();

    private static final Logger logger = LoggerFactory.getLogger (FileMonitor.class);

    public FileMonitor () throws IOException {
        monitor = FileSystems.getDefault ().newWatchService ();
    }

    public void addPath (final Path path) throws IOException {
        Files.walkFileTree (path, visitor);
    }

    public synchronized void addListener (IFileMonitorListener listener) {
        if (!listeners.contains (listener)) {
            listeners.add (listener);
        }
    }

    public synchronized boolean removeListener (IFileMonitorListener listener) {
        return listeners.remove (listener);
    }

    private synchronized List<IFileMonitorListener> getListeners () {
        return new ArrayList<> (listeners);
    }

    public void monitor () {
        service.execute (this);
    }

    public void dispose () {
        running = false;
        service.shutdownNow ();

        if (monitor != null) {
            try {
                monitor.close ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }

        wet.shutdown ();
        try {
            wet.awaitTermination (1, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            e.printStackTrace ();
        }
    }

    private void register (Path path) throws IOException {
        path = path.toAbsolutePath ();
        if (logger.isTraceEnabled ())
            logger.trace ("registering the path: " + path);
        if (!paths.contains (path)) {
            paths.add (path);

            WatchKey key = path.register (
                    monitor,
                    StandardWatchEventKinds.ENTRY_CREATE,
                    StandardWatchEventKinds.ENTRY_MODIFY,
                    StandardWatchEventKinds.ENTRY_DELETE
            );
            cache.put (key, path);
        }
    }

    @Override
    public void run () {
        while (running) {
            WatchKey key;
            try {
                if (logger.isTraceEnabled ())
                    logger.trace ("waiting for new watch key.");
                key = monitor.take ();
                if (logger.isTraceEnabled ()) {
                    logger.trace ("got a watch key: " + key);
                    logger.trace ("checking for the key whether valid or not");
                }
                if (!key.isValid ()) {
                    cache.remove (key);
                    continue;
                }

                Path path = cache.get (key);
                if (path == null) {
                    continue;
                }

                if (logger.isTraceEnabled ())
                    logger.trace ("processing the watch key");
                for (WatchEvent<?> e : key.pollEvents ()) {
                    @SuppressWarnings ("unchecked")
                    WatchEvent<Path> event = (WatchEvent<Path>) e;
                    Path context  = event.context ();
                    Path target   = path.resolve (context);
                    String name   = event.kind ().name ();
                    boolean isDir = Files.isDirectory (target, LinkOption.NOFOLLOW_LINKS);

                    if (logger.isTraceEnabled ()) {
                        logger.trace ("poll a watch event");
                        logger.trace ("context = " + event.context ());
                        logger.trace ("kind    = " + event.kind ().name ());
                        logger.trace ("is dir  = " + isDir);
                    }

                    if ("ENTRY_CREATE".equals (name) && isDir) {
                        try {
                            if (logger.isTraceEnabled ())
                                logger.trace ("the new context is a directory, register it");
                            register (target);
                        } catch (IOException e1) {
                            e1.printStackTrace ();
                            logger.error ("can't register monitor on {}", target);
                        }
                    }

                    if (!("ENTRY_MODIFY".equals (name) && isDir)) {
                        if (logger.isTraceEnabled ())
                            logger.trace ("raising the watch events");
                        // ignore dir's modify event
                        List<IFileMonitorListener> list = getListeners ();
                        for(IFileMonitorListener listener : list) {
                            wet.execute (new WatchEventThread (listener, event, target));
                        }
                    }
                }

                if (!key.reset ()) {
                    if (logger.isTraceEnabled ())
                        logger.trace (key + " reset fail, remove it from cache");
                    cache.remove (key);
                }
            } catch (InterruptedException ex) {
                ex.printStackTrace ();
            }
        }
    }

    private SimpleFileVisitor<Path> visitor = new SimpleFileVisitor<Path> () {
        @Override
        public FileVisitResult preVisitDirectory (Path dir, BasicFileAttributes attrs) throws IOException {
            register (dir);
            return super.preVisitDirectory (dir, attrs);
        }
    };
}