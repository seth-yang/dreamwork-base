package org.dreamwork.ui.concurrent;

import java.util.Map;

/**
 * Created by seth.yang on 2016/10/11
 */
public class Message {
    public int what;
    public Object obj;
    public Map<String, Object> bundle;

    public Message (int what) {
        this.what = what;
    }

    public Message (int what, Object obj) {
        this.what = what;
        this.obj = obj;
    }

    public int getWhat () {
        return what;
    }

    public void setWhat (int what) {
        this.what = what;
    }

    public Object getObj () {
        return obj;
    }

    public void setObj (Object obj) {
        this.obj = obj;
    }

    public Map<String, Object> getBundle () {
        return bundle;
    }

    public void setBundle (Map<String, Object> bundle) {
        this.bundle = bundle;
    }
}