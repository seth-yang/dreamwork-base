package org.dreamwork.fs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: seth
 * Date: 13-1-15
 * Time: 上午10:44
 */
public class FSWatcher implements Runnable {
    private FolderWalker walker;
    private long interval = 60000L;
    private boolean running, asynchronous;
    private FSMonitor monitor;

    private static final Object locker = new Object ();
    private static final Logger logger = LoggerFactory.getLogger (FSWatcher.class);

    public FSWatcher (File basedir) throws IOException {
        monitor = new FSMonitor (basedir);
        walker = new FolderWalker ();
        walker.setMonitor (monitor);
        walker.setBaseDir (basedir.getCanonicalPath ());
    }

    public boolean isAsynchronous () {
        return asynchronous;
    }

    public void setAsynchronous (boolean asynchronous) {
        this.asynchronous = asynchronous;
        walker.setSynchronous (!asynchronous);
    }

    /**
     * 添加文件处理器
     *
     * @param handler 文件处理器
     */
    public void addFileHandler (IFileHandler handler) {
        walker.addFileHandler (handler);
    }

    /**
     * 删除一个文件处理器
     *
     * @param handler 文件处理器
     */
    public void removeFileHandler (IFileHandler handler) {
        walker.removeFileHandler (handler);
    }

    /**
     * 获取当前所有的文件处理器列表
     *
     * @return 文件处理器列表
     */
    public List<IFileHandler> getFileHandlers () {
        return walker.getFileHandlers ();
    }

    /**
     * 获取文件名过滤器规则
     *
     * @return 过滤器规则
     */
    public String getFileFilter () {
        return walker.getFileFilter ();
    }

    /**
     * 设置文件名过滤器规则
     *
     * @param fileFilter 过滤器规则
     */
    public void setFileFilter (String fileFilter) {
        walker.setFileFilter (fileFilter);
    }

    public long getInterval () {
        return interval;
    }

    public void setInterval (long interval) {
        this.interval = interval;
    }

    public void watch () {
        if (logger.isTraceEnabled ())
            logger.trace ("Trying to start watcher...");
        synchronized (locker) {
            running = true;
        }
        new Thread (this).start ();
    }

    public void shutdown () {
        if (logger.isTraceEnabled ())
            logger.trace ("Trying to shutdown watcher...");
        synchronized (locker) {
            running = false;
        }
    }

    @Override
    public void run () {
        try {
            monitor.restoreIndices ();

            if (asynchronous)
                new Thread (monitor).start ();

            while (running) {
                walker.walk ();
                if (!asynchronous)
                    monitor.flushIndices ();
            }
        } catch (Exception ex) {
            logger.warn ("Can't start watcher");
            logger.warn (ex.getMessage (), ex);
        }
    }

    public static void main (String[] args) throws Exception {
        FSWatcher watcher = new FSWatcher (new File ("/home/seth/poc"));
        watcher.setInterval (10000L);
//        watcher.setFileFilter ("*.*");
        watcher.addFileHandler (new IFileHandler () {
            @Override
            public void processFile (FSMonitor monitor, File file) {
                try {
                    System.out.println ("process " + file.getCanonicalPath ());
                } catch (IOException e) {
                    e.printStackTrace ();  //To change body of catch statement use File | Settings | File Templates.
                }
            }

            @Override
            public void processDir (FSMonitor monitor, File dir) {
                try {
                    System.out.println ("process dir: " + dir.getCanonicalPath ());
                } catch (IOException e) {
                    e.printStackTrace ();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
        });
        watcher.watch ();
    }
}