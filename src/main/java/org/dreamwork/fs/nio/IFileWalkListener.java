package org.dreamwork.fs.nio;

/**
 * Created by seth.yang on 2017/4/18
 */
public interface IFileWalkListener<T extends FileIndex> {
    void onCreate (T index);
    void onDelete (T index);
    void onModify (T index);
}
