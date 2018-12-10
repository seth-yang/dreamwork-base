package org.dreamwork.fs.nio;

/**
 * Created by seth.yang on 2017/4/17
 */
public abstract class FileIndex {
    protected String path;
    protected long   timestamp;

    protected FileIndex (String path) {
        this.path = path;
    }

    public String getPath () {
        return path;
    }

    public long getTimestamp () {
        return timestamp;
    }

    public void setTimestamp (long timestamp) {
        this.timestamp = timestamp;
    }
}