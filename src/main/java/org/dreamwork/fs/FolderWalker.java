package org.dreamwork.fs;

import java.io.*;
import java.util.*;

/**
 * 文件系统遍历工具
 *
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2010-4-12
 * Time: 13:30:10
 */
public class FolderWalker {
    private String baseDir;
    private boolean createIndex = true, synchronous = true;

    private Map<String, Long> map;
    private FSMonitor monitor;

    private List<IFileHandler> handlers = Collections.synchronizedList (new ArrayList<IFileHandler> ());

    private FileFilter dirFilter = new FileFilter() {
        public boolean accept (File pathname) {
            return pathname.isDirectory ();
        }
    };
    private WalkerFileFilter fileFilter = new WalkerFileFilter ();

    private static IFileHandler synchronousHandler = new IFileHandler () {
        @Override
        public void processFile (FSMonitor monitor, File file) {
            try {
                monitor.commit (file);
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }

        @Override
        public void processDir (FSMonitor monitor, File dir) {
            try {
                monitor.commit (dir);
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
    };

    /*
     * 创建一个指定路径的文件遍历工具类
     * @param baseDir 指定的路径
     */
/*
    public FolderWalker (String baseDir) throws IOException {
        this (new File (baseDir));
    }

    public FolderWalker (File file) throws IOException {
        this.baseDir = file.getCanonicalPath ();
    }
*/

    /**
     * 是否创建文件索引
     * @return 如果创建文件索引返回 true， 否则返回 false
     */
    public boolean isCreateIndex () {
        return createIndex;
    }

    /**
     * 设置 FolderWalker 是否创建文件索引
     * @param createIndex 是否创建文件索引
     */
    public void setCreateIndex (boolean createIndex) {
        this.createIndex = createIndex;
    }

    /**
     * 添加文件处理器
     * @param handler 文件处理器
     */
    public void addFileHandler (IFileHandler handler) {
        handlers.add (handler);
    }

    /**
     * 删除一个文件处理器
     * @param handler 文件处理器
     */
    public void removeFileHandler (IFileHandler handler) {
        handlers.remove (handler);
    }

    /**
     * 获取当前所有的文件处理器列表
     * @return 文件处理器列表
     */
    public List<IFileHandler> getFileHandlers () {
        List<IFileHandler> list = new ArrayList<IFileHandler> ();
        list.addAll (handlers);
        return list;
    }

    /**
     * 获取基础目录
     * @return 基础目录名称
     */
    public String getBaseDir () {
        return baseDir;
    }

    /**
     * 设置基础目录名称
     * @param baseDir 基础目录名称
     */
    public void setBaseDir (String baseDir) {
        this.baseDir = baseDir;
    }

    public boolean isSynchronous () {
        return synchronous;
    }

    public void setSynchronous (boolean synchronous) {
        this.synchronous = synchronous;
    }

    /**
     * 获取文件名过滤器规则
     * @return 过滤器规则
     */
    public String getFileFilter () {
        return fileFilter == null ? null : fileFilter.getFilter ();
    }

    /**
     * 设置文件名过滤器规则
     * @param fileFilter 过滤器规则
     */
    public void setFileFilter (String fileFilter) {
        this.fileFilter.setFilter (fileFilter);
    }

    public void setMonitor (FSMonitor monitor) {
        this.monitor = monitor;
    }

    /**
     * 遍历基础目录，并递归。
     * <p>工作流程:<ol>
     * <li>检查 createIndex 标志</li>
     * <li>若 createIndex 为 true:<ol>
     *  <li>检查在 baseDir 下是否存在 .index 文件</li>
     *  <li>若存在，读取 .index 文件中的索引</li>
     *  <li>否则， 在 baseDir 下创建 .index 索引文件</li>
     * </ol>
     * </li>
     * <li>遍历当前工作目录下的所有子目录，依次调用 IFileHandler.processDir 对目录进行处理，并递归</li>
     * <li>遍历当前工作目录下的所有符合过滤器规则的文件，检查索引文件中针对当前工作文件的的索引条目，若需要处理，
     * 依次调用 IFileHandler.processFile 对文件进行处理，若 IFileHandler.processFile 调用成功，
     * 在索引文件中添加/更新对当前工作文件的索引条目</li>
     * </ol>
     * @throws IOException IO 异常
     */
    public void walk () throws IOException {
        map = monitor.dumpIndices ();
        File file = new File (baseDir);
        listDir (file);

/*
        List<IFileHandler> handlers = getFileHandlers ();
        Set<File> set = new HashSet<File> ();
        for (IFileHandler handler : handlers) {
            set.addAll (handler.getFailFiles ());
        }
*/
    }

    private void processDirectory (File dir) {
        List<IFileHandler> list = getFileHandlers ();
        for (IFileHandler handler : list) {
            handler.processDir (monitor, dir);
        }
        if (synchronous)
            synchronousHandler.processDir (monitor, dir);
    }

    private void processFile (File file) {
        List<IFileHandler> list = getFileHandlers ();
        for (IFileHandler handler : list) {
            handler.processFile (monitor, file);
        }
        if (synchronous)
            synchronousHandler.processFile (monitor, file);
    }

    private void listDir (File dir) throws IOException {
        System.out.println ("dir = " + dir.getCanonicalPath ());
        String key = dir.getCanonicalPath ();
        if (!map.containsKey (key)) {
            processDirectory (dir);
        }


        File[] dirs = dir.listFiles (dirFilter);
        for (File di : dirs) {
            System.out.println ("dir = " + di.getCanonicalPath ());
            listDir (di);
        }
        File[] files = dir.listFiles (fileFilter);
        for (File file : files) {
            System.out.println ("file = " + file.getCanonicalPath ());
            recordFile (file);
        }
    }

    private void recordFile (File file) throws IOException {
        String key = file.getCanonicalPath ();
        if (map.containsKey (key)) {
            long last = map.get (key);
            if (last < file.lastModified ()) {
                processFile (file);
            }
        } else {
            processFile (file);
        }
    }

/*
    private void processAndRecordFile (File file) throws IOException {
        String key = file.getCanonicalPath ();
        processFile (file);
        map.put (key, file.lastModified ());
    }
*/
}