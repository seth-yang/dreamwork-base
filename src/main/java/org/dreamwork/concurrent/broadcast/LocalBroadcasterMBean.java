package org.dreamwork.concurrent.broadcast;

import java.util.Set;

/**
 * Created by seth.yang on 2018/4/13
 */
public interface LocalBroadcasterMBean {
    String JMX_BEAN_NAME = "LocalBroadcaster";
    Set<String> getRegisteredWorkerNames ();
    Set<String> getRegisteredReceiverNames ();
    Set<String> getRegisteredReceivers (String name);
}