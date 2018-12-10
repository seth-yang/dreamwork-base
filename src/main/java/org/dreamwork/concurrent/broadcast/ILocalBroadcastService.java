package org.dreamwork.concurrent.broadcast;

public interface ILocalBroadcastService {
    void register (String category, ILocalBroadcastReceiver receiver);
    void unregister (String category, ILocalBroadcastReceiver receiver);
    void broadcast (String category, LocalMessage message);
    default void broadcast (String category) {
        broadcast (category, null);
    }
    default void broadcast (String category, int what) {
        broadcast (category, new LocalMessage (what));
    }
    default void broadcast (String category, int what, Object arg) {
        broadcast (category, new LocalMessage (what, arg));
    }
}