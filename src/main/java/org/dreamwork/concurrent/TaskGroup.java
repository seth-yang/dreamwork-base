package org.dreamwork.concurrent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicInteger;

public class TaskGroup<T> {
    private final Logger logger = LoggerFactory.getLogger (TaskGroup.class);
    private final BlockingQueue<Object> queue;
    private final ExecutorService executor;
    private final ThreadGroup group;
    private final AtomicInteger counter = new AtomicInteger (1);
    private final int workerCount;

    private Monitor monitor;

    private static final Object QUIT = new byte[0];
    private static final String KEY_MONITOR_ENABLED = "org.dreamwork.task.group.monitor.enabled";
    private static final String KEY_MONITOR_PORT    = "org.dreamwork.task.group.monitor.port";
    private static final int DEFAULT_MONITOR_PORT   = 43210;

    public TaskGroup (String name) {
        this (name, 64, 8);
    }

    public TaskGroup (String name, int capacity, int workers) {
        workerCount = workers;
        group = new ThreadGroup (name);
        queue = new LinkedBlockingQueue<> (capacity);
        executor = Executors.newFixedThreadPool (workers, r -> new Thread (group, r, name + ".worker#" + counter.getAndIncrement ()));
        logger.info ("group {} created.", name);
    }

    public boolean offer (T target) {
        return queue.offer (target);
    }

    public void put (T target) throws InterruptedException {
        queue.put (target);
    }

    public void start (ITaskProcessor<T> processor) {
        for (int i = 0; i < workerCount; i ++) {
            executor.execute (new TaskWorker<> (queue, processor));
        }
        logger.info ("task group [{}] started.", group.getName ());

        String enabled = System.getProperty (KEY_MONITOR_ENABLED);
        if (null != enabled) {
            enabled = enabled.toLowerCase ().trim ();
            if ("true".startsWith (enabled)) {
                int port = DEFAULT_MONITOR_PORT;
                String s_port = System.getProperty (KEY_MONITOR_PORT);
                try {
                    port = Integer.parseInt (s_port);
                } catch (Exception ignore) {}

                startMonitor (port);
            }
        }
    }

    public void destroy () {
        for (int i = 0; i < workerCount; i ++) {
            try {
                queue.put (QUIT);
            } catch (InterruptedException ignore) {
            }
        }
        if (monitor != null) {
            try {
                monitor.unbind ();
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
        }
        executor.shutdown ();
    }

    private void startMonitor (int port) {
        monitor = new Monitor (port);
        try {
            monitor.bind ();
            logger.debug ("monitor listen on: {}", port);
        } catch (IOException ex) {
            logger.warn (ex.getMessage (), ex);
        }
    }

    private static final class TaskWorker<T> implements Runnable {
        private final BlockingQueue<Object> queue;
        private final ITaskProcessor<T> processor;
        private final Logger logger = LoggerFactory.getLogger (TaskWorker.class);

        private volatile boolean running = true;

        TaskWorker (BlockingQueue<Object> queue, ITaskProcessor<T> processor) {
            this.queue = queue;
            this.processor = processor;
        }

        @Override
        @SuppressWarnings ("unchecked")
        public void run () {
            while (running) {
                Object o;
                try {
                    o = queue.take ();
                } catch (InterruptedException ignore) {
                    logger.warn ("an interrupted exception occurred, ignore it");
                    continue;
                }

                if (o == QUIT) {
                    if (logger.isTraceEnabled ()) {
                        logger.trace ("got a quit signal, break the loop");
                    }
                    running = false;
                    break;
                }

                if (processor != null) {
                    try {
                        processor.perform ((T) o);
                    } catch (Exception ex) {
                        logger.warn (ex.getMessage (), ex);
                    }
                }
            }
            logger.info ("work completed.");
        }
    }

    private static final class Monitor {
        private final int port;
        private volatile boolean running = true;
        private DatagramSocket server;
        private Thread thread;

        private final List<DatagramSocket> clients = new ArrayList<> ();

        private Monitor (int port) {
            this.port = port;
        }

        private void bind () throws IOException {
            server = new DatagramSocket (port);
            thread = new Thread (() -> {
                while (running) {
                    byte[] buff = new byte [128];
                    DatagramPacket p = new DatagramPacket (buff, 0, buff.length);
                    try {
                        server.receive (p);

                    } catch (IOException ex) {
                        ex.printStackTrace ();
                    }
                }
            });
            thread.start ();
        }

        private void unbind () throws IOException {
            clients.forEach (client -> {
                client.disconnect ();
                client.close ();
            });

            if (server != null) {
                server.disconnect ();
                server.close ();
            }
            if (thread != null) {
                thread.interrupt ();
            }
        }
    }
}