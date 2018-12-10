package org.dreamwork.concurrent.broadcast;

import java.util.Map;

public class LocalMessage {
    public int what;
    public Object arg;
    public Map<String, Object> payload;

    public LocalMessage () {}

    public LocalMessage (int what) {
        this.what = what;
    }

    public LocalMessage (int what, Object arg) {
        this (what);
        this.arg = arg;
    }
}