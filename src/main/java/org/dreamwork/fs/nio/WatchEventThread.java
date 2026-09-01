package org.dreamwork.fs.nio;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

/**
 * Created by seth.yang on 2017/4/17
 */
final class WatchEventThread implements Runnable {
    private final IFileMonitorListener listener;
    private final WatchEvent<Path> event;
    private final Path path;
    private final Logger logger = LoggerFactory.getLogger (WatchEventThread.class);

    public WatchEventThread (IFileMonitorListener listener, WatchEvent<Path> event, Path path) {
        this.listener = listener;
        this.event    = event;
        this.path     = path;
    }

    @Override
    public void run () {
        try {
            WatchEvent.Kind<Path> kind = event.kind ();
            switch (kind.name ()) {
                case "ENTRY_CREATE":
                    listener.onCreate (path);
                    break;
                case "ENTRY_MODIFY":
                    listener.onModify (path);
                    break;
                case "ENTRY_DELETE":
                    listener.onDelete (path);
                    break;
            }
        } catch (IOException ex) {
            logger.warn (ex.getMessage (), ex);
            throw new RuntimeException (ex);
        }
    }
}