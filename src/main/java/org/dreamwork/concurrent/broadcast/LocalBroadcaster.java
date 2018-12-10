package org.dreamwork.concurrent.broadcast;

import org.apache.log4j.Logger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;

/**
 * 本地消息信使
 *
 * Created by seth.yang on 2018/2/9
 */
public class LocalBroadcaster implements Runnable, ILocalBroadcastService, LocalBroadcasterMBean {
    private final Logger logger = Logger.getLogger (LocalBroadcaster.class);
    private final Map<String, List<ILocalBroadcastReceiver>> handlers = new HashMap<> ();
    private final Map<String, LocalBroadcastWorker> workers = new HashMap<> ();
    private final BlockingQueue<MessageWrapper> queue = new ArrayBlockingQueue<> (16);
    private final LocalMessage QUIT = new LocalMessage ();
    private final ExecutorService executor;

    private static final String JMX_GROUP_NAME = "org.dreamwork.jmx";

    @Override
    public Set<String> getRegisteredWorkerNames () {
        return new HashSet<> (workers.keySet ());
    }

    @Override
    public Set<String> getRegisteredReceiverNames () {
        return new HashSet<> (handlers.keySet ());
    }

    @Override
    public Set<String> getRegisteredReceivers (String name) {
        List<ILocalBroadcastReceiver> receivers = handlers.get (name);
        Set<String> set = new HashSet<> ();
        for (ILocalBroadcastReceiver receiver : receivers) {
            set.add (receiver.toString ());
        }
        return set;
    }

    private static final class MessageWrapper {
        String       name;
        LocalMessage message;

        MessageWrapper (String name, LocalMessage message) {
            this.message = message;
            this.name    = name;
        }
    }

    public LocalBroadcaster (ExecutorService executor) {
        this.executor = executor;

        try {
            MBeanServer server = ManagementFactory.getPlatformMBeanServer ();
            ObjectName name = new ObjectName (JMX_GROUP_NAME, "name", JMX_BEAN_NAME);
            server.registerMBean (this, name);
        } catch (Exception ex) {
            logger.warn (ex.getMessage (), ex);
        }
    }

    @Override
    public void register (String category, ILocalBroadcastReceiver listener) {
        synchronized (handlers) {
            List<ILocalBroadcastReceiver> list = handlers.get (category);
            if (list == null) {
                list = new ArrayList<> ();
                handlers.put (category, list);

                LocalBroadcastWorker worker = new LocalBroadcastWorker (category + ".worker");
                workers.put (category, worker);
                executor.execute (worker);
            }

            list.add (listener);
        }
    }

    @Override
    public void unregister (String category, ILocalBroadcastReceiver receiver) {
        synchronized (handlers) {
            if (handlers.containsKey (category)) {
                List<ILocalBroadcastReceiver> list = handlers.get (category);
                list.remove (receiver);

                if (list.isEmpty ()) {
                    handlers.remove (category);

                    // stop the worker
                    LocalBroadcastWorker worker = workers.get (category);
                    worker.shutdown ();
                    workers.remove (category);
                }
            }
        }
    }

    @Override
    public void broadcast (String category, LocalMessage message) {
        queue.offer (new MessageWrapper (category, message));
    }

    public void shutdown () {
        queue.offer (new MessageWrapper (null, QUIT));
    }

    @Override
    public void run () {
        Thread.currentThread ().setName (JMX_GROUP_NAME);
        logger.info ("starting local broadcaster ...");
        while (true) {
            try {
                MessageWrapper msg = queue.take ();
                if (msg.message == QUIT) {
                    if (logger.isTraceEnabled ()) {
                        logger.trace ("received a quit command.");
                    }
                    break;
                }

                dispatch (msg);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }

        if (logger.isTraceEnabled ()) {
            logger.trace ("cleaning resources.");
        }
        if (!queue.isEmpty ()) {
            queue.clear ();
        }
        if (!handlers.isEmpty ()) {
            handlers.values ().forEach (List::clear);
            handlers.clear ();
        }
        if (!workers.isEmpty ()) {
            workers.values ().forEach (LocalBroadcastWorker::shutdown);
            workers.clear ();
        }
    }

    private void dispatch (MessageWrapper wrapper) {
        List<ILocalBroadcastReceiver> copy = null;
        synchronized (handlers) {
            if (handlers.containsKey (wrapper.name)) {
                copy = new ArrayList<> (handlers.get (wrapper.name));
            }
        }

        if (copy != null && copy.size () > 0) {
            LocalBroadcastWorker worker = workers.get (wrapper.name);
            for (ILocalBroadcastReceiver receiver : copy) {
                worker.add (() -> receiver.received (wrapper.message));
            }
        }
    }
}