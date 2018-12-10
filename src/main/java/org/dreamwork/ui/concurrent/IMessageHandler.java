package org.dreamwork.ui.concurrent;

/**
 * Created by seth.yang on 2016/10/11
 */
public interface IMessageHandler {
    void handleUIMessage (Message message);
    void handleNonUIMessage (Message message);
}