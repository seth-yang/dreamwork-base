package org.dreamwork.concurrent.broadcast;

public interface ILocalBroadcastReceiver {
    @Deprecated
    default void received (LocalMessage message) {
        received (null, message);
    }

    default void received (String category, LocalMessage message) {}
}
