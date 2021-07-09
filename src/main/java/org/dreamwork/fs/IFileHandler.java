package org.dreamwork.fs;

import java.io.File;
import java.util.List;

/**
 * 文件处理器接口
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 2010-4-12
 * Time: 13:30:25
 */
public interface IFileHandler {
    /**
     * 处理文件。
     * <p>当 FolderWalker 对一个文件验证通过后，会调用这个方法进行对文件的处理
     * @param monitor 监视器
     * @param file    待处理的文件
     */
    void processFile (FSMonitor monitor, File file);

    /**
     * 处理目录
     * <p>当 FolderWalker 遍历到一个目录时，将调用这个方法进行对目录的处理
     * @param monitor 监视器
     * @param dir 待处理的目录
     */
    void processDir  (FSMonitor monitor, File dir);
}