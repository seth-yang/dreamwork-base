package org.dreamwork.concurrent.broadcast;

public interface ILocalBroadcastReceiver {
    void received (String category, LocalMessage message);
}