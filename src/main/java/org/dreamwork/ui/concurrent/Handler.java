package org.dreamwork.ui.concurrent;

import org.dreamwork.concurrent.Looper;

import javax.swing.*;

/**
 * Created by seth.yang on 2016/10/11
 */
@SuppressWarnings ("all")
public class Handler {
    private IMessageHandler handler;
    private static final String THREAD_NAME = "NonUIThread";

    public Handler (IMessageHandler handler) {
        this.handler = handler;
    }

    public Message obtain (int what) {
        return new Message (what);
    }

    public Message obtain (int what, Object object) {
        return new Message (what, object);
    }

    /**
     * replaced by {@link #sendUIMessage(int)}
     * @see #sendUIMessage(int)
     * @param what the command
     */
    @Deprecated
    public void sendMessage (int what) {
        sendUIMessage (what);
    }

    /**
     * replaced by {@link #sendUIMessage(int, Object)}
     * @see #sendUIMessage(int, Object)
     * @param what   command
     * @param object the parameter object
     */
    @Deprecated
    public void sendMessage (int what, Object object) {
        sendUIMessage (what, object);
    }

    /**
     * replaced by {@link #sendUIMessage(Message)}
     * @see #sendUIMessage(Message)
     * @param message the command parameter
     */
    @Deprecated
    public void sendMessage (Message message) {
        sendUIMessage (message);
    }

    public void sendUIMessage (int what) {
        sendUIMessage (obtain (what));
    }

    public void sendUIMessage (int what, Object object) {
        sendUIMessage (obtain (what, object));
    }

    public void sendUIMessage (Message message) {
        SwingUtilities.invokeLater (new Runner (handler, message, 0));
    }

    public void sendNonUIMessage (int what) {
        sendNonUIMessage (what, false);
    }

    public void sendNonUIMessage (int what, boolean queued) {
        sendNonUIMessage (obtain (what), queued);
    }

    public void sendNonUIMessage (int what, Object obj) {
        sendNonUIMessage (what, obj, false);
    }

    public void sendNonUIMessage (int what, Object obj, boolean queued) {
        sendNonUIMessage (obtain (what, obj), queued);
    }

    public void sendNonUIMessage (Message message) {
        sendNonUIMessage (message, false);
    }

    public void sendNonUIMessage (Message message, boolean queued) {
        Runner r = new Runner (handler, message, 1);
        if (queued) {
            if (!Looper.exists (THREAD_NAME)) {
                Looper.create (THREAD_NAME, 16);
            }

            Looper.runInLoop (THREAD_NAME, r);
        } else {
            Looper.invokeLater (r);
        }
    }

    private static class Runner implements Runnable {
        private IMessageHandler handler;
        private Message message;
        private int target;

        private Runner (IMessageHandler handler, Message message, int target) {
            this.handler = handler;
            this.message = message;
            this.target  = target;
        }

        @Override
        public void run () {
            if (target == 0) {
                handler.handleUIMessage (message);
            } else if (target == 1) {
                handler.handleNonUIMessage (message);
            }
        }
    }
}