package org.dreamwork.config;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 12-3-29
 * Time: 下午3:28
 */
public class FileMonitor extends Thread {
    private static final Logger logger = Logger.getLogger (FileMonitor.class);
    private static final long INTERVAL = 20000; // 5 minutes

    private final List<FileChangeListener> listeners = new ArrayList<FileChangeListener> ();

    private long timestamp;
    private String path;
    private boolean running = true;


    public synchronized void addFileChangedListener (FileChangeListener listener) {
        listeners.add (listener);
    }

    public synchronized void removeFileChangedListener (FileChangeListener listener) {
        listeners.remove (listener);
    }

    public List<FileChangeListener> getListeners () {
        List<FileChangeListener> backup = new ArrayList<FileChangeListener> ();
        synchronized (listeners) {
            backup.addAll (listeners);
        }
        return backup;
    }

    public FileMonitor (File file) throws IOException {
        this.timestamp = file.lastModified ();
        this.path = file.getCanonicalPath ();
    }

    public void terminate () {
        running = false;
    }

    @Override
    public void run () {
        while (running) {
            try {
                sleep (INTERVAL);
            } catch (InterruptedException e) {
                //
            }

            File file = new File (path);
            if (file.lastModified () > timestamp) {
                timestamp = file.lastModified ();
                List<FileChangeListener> list = getListeners ();
                for (FileChangeListener l : list) try {
                    l.fileChanged (file);
                } catch (IOException e) {
                    logger.warn (e.getMessage (), e);
                }
            }
        }
    }
}