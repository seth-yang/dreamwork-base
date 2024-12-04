package org.dreamwork.concurrent.broadcast;

import org.dreamwork.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicInteger;

import static org.dreamwork.util.ThreadHelper.delay;

/**
 * 本地消息信使
 * Created by seth.yang on 2018/2/9
 */
@SuppressWarnings ("unused")
public class LocalBroadcaster implements Runnable, ILocalBroadcastService, LocalBroadcasterMBean {
    private final Logger logger = LoggerFactory.getLogger (LocalBroadcaster.class);
    private final Map<String, List<ILocalBroadcastReceiver>> handlers = Collections.synchronizedMap (new HashMap<> ());
    private final Map<String, LocalBroadcastWorker> workers = Collections.synchronizedMap (new HashMap<> ());
    private final BlockingQueue<MessageWrapper> queue = new ArrayBlockingQueue<> (16);
    private final LocalMessage QUIT = new LocalMessage ();
    private final ExecutorService executor;
    private final AtomicInteger counter = new AtomicInteger (0);

    private static final String JMX_GROUP_NAME = "org.dreamwork.jmx";

    private volatile boolean running = true;

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

        if (System.getProperty ("disable-jmx") == null) try {
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
            List<ILocalBroadcastReceiver> list = handlers.computeIfAbsent (category, key -> {
                List<ILocalBroadcastReceiver> receivers = new ArrayList<> ();

                LocalBroadcastWorker worker = new LocalBroadcastWorker (category + ".worker");
                workers.put (category, worker);
                executor.execute (worker);
                return receivers;
            });
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
        if (!handlers.containsKey (category)) {
            if (logger.isTraceEnabled ()) {
                logger.warn ("there's no listener registered for category: {}, ignore this request", category);
            }
            return;
        }
        queue.offer (new MessageWrapper (category, message));
    }

    public void shutdown () {
        long start = System.currentTimeMillis ();
        running = false;
        queue.offer (new MessageWrapper (null, QUIT));

        while (!workers.isEmpty ()) {
            if (System.currentTimeMillis () - start > 30000) {
                logger.warn ("shutdown timeout");
                break;
            }
            delay (1);
        }
        if (logger.isTraceEnabled ()) {
            logger.trace ("shutdown takes {} ms.", System.currentTimeMillis () - start);
        }
    }

    public void shutdownNow () {
        queue.clear ();
        shutdown ();
    }

    @Override
    public void run () {
        Thread.currentThread ().setName (JMX_GROUP_NAME);
        logger.info ("starting local broadcaster ...");
        while (running) {
            try {
                MessageWrapper msg = queue.take ();
                if (msg.message == QUIT) {
                    if (logger.isTraceEnabled ()) {
                        logger.trace ("received a quit command. waiting for all workers shutdown ...");
                    }
                    if (!workers.isEmpty ()) {
                        workers.forEach ((key, value) -> {
                            if (logger.isTraceEnabled ()) {
                                logger.trace (">>>>>>>>>>>>>>>>>>>>>>>>> shutdown worker:: {}", key);
                            }
                            value.shutdown ();
                            if (logger.isTraceEnabled ()) {
                                logger.trace ("<<<<<<<<<<<<<<<<<<<<<<<<< worker[{}] stopped.", key);
                            }
                        });
                        workers.clear ();
                    }
                    break;
                }

                dispatch (msg);
            } catch (InterruptedException ex) {
                if (logger.isTraceEnabled ()) {
                    // 仅trace模式才打印这个错误堆栈
                    logger.warn (ex.getMessage (), ex);
                }

                if (!running) {
                    break;
                }
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
            workers.clear ();
        }
    }

    private void dispatch (MessageWrapper wrapper) {
        if (wrapper == null || StringUtil.isEmpty (wrapper.name)) {
            if (logger.isTraceEnabled ()) {
                logger.warn ("category is empty. ignore this request");
            }
            return;
        }
        if (!handlers.containsKey (wrapper.name)) {
            if (logger.isTraceEnabled ()) {
                logger.warn ("there's no listener registered for category: {}, ignore this request", wrapper.name);
            }
            return;
        }
        if (!workers.containsKey (wrapper.name)) {
            if (logger.isTraceEnabled ()) {
                logger.warn ("there's no worker named {}, ignore this request", wrapper.name);
            }
        }

        List<ILocalBroadcastReceiver> copy = new ArrayList<> ();
        synchronized (handlers) {
            for (Map.Entry<String, List<ILocalBroadcastReceiver>> entry : handlers.entrySet ()) {
                String category = entry.getKey ();
                if (wrapper.name.startsWith (category)) {
                    List<ILocalBroadcastReceiver> receivers = entry.getValue ();
                    for (ILocalBroadcastReceiver receiver : receivers) {
                        if (!copy.contains (receiver)) {
                            copy.add (receiver);
                        }
                    }
                }
            }
        }

        if (!copy.isEmpty ()) {
            LocalBroadcastWorker worker = workers.get (wrapper.name);
            for (ILocalBroadcastReceiver receiver : copy) {
                worker.add (() -> receiver.received (wrapper.name, wrapper.message));
            }
        }
    }
}