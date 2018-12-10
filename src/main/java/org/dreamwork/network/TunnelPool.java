package org.dreamwork.network;

import org.apache.log4j.Logger;
import org.dreamwork.concurrent.PausableThread;

import java.io.IOException;
import java.util.*;

/**
 * Created by game on 2016/4/18
 */
public class TunnelPool {
    public static final int STATE_CREATED = 0, STATE_WAITING = 1, STATE_BOUND = 2, STATE_DISPOSED = 3;
    private final static Map<String, List<Tunnel>> pool = new HashMap<String, List<Tunnel>> ();
    private static final Logger logger = Logger.getLogger (TunnelPool.class);

    private static PausableThread monitor = new PausableThread (true, "Tunnel Monitor") {
        @Override
        protected void doWork () {
            Map<String, List<Tunnel>> temp = new HashMap<String, List<Tunnel>> ();
            synchronized (pool) {
                for (String name : pool.keySet ()) {
                    List<Tunnel> list = pool.get (name);
                    for (Tunnel tunnel : list) {
                        long timeout = -1;
                        if (tunnel.state == STATE_WAITING) {
                            timeout = tunnel.waitingTimeout;
                        } else if (tunnel.state == STATE_BOUND) {
                            timeout = tunnel.timeout;
                        }

                        if (System.currentTimeMillis () - tunnel.touch > timeout) {
                            if (logger.isDebugEnabled ()) {
                                logger.debug ("The tunnel [" + tunnel.name + "] times out, it'll be removed from the pool");
                            }

                            List<Tunnel> set = temp.get (name);
                            if (set == null) {
                                set = new ArrayList<Tunnel> ();
                                temp.put (name, set);
                            }
                            set.add (tunnel);
                        }
                    }
                }

                if (!temp.isEmpty ()) {
                    for (String key : temp.keySet ()) {
                        List<Tunnel> set  = temp.get (key);
                        for (Tunnel tunnel : set) {
                            try {
                                tunnel.dismiss ();
                                remove (tunnel);
                            } catch (IOException e) {
                                e.printStackTrace ();
                            }
                        }
                    }
                    if (logger.isDebugEnabled ()) {
                        logger.debug ("now, the pool contains: " + pool.keySet ());
                    }
                }
            }

            try {
                sleep (100);
            } catch (InterruptedException e) {
                e.printStackTrace ();
            }
        }
    };

    public synchronized static void add (Tunnel tunnel) {
        if (logger.isDebugEnabled ()) {
            logger.debug ("add a new tunnel[" + tunnel.name + "] into pool");
        }

        List<Tunnel> list = pool.get (tunnel.name);
        if (list == null) {
            list = new ArrayList<Tunnel> ();
            pool.put (tunnel.name, list);
        }
        list.add (tunnel);
        if (monitor.isPaused ()) {
            monitor.proceed ();
            if (logger.isDebugEnabled ()) {
                logger.debug ("The monitor active.");
            }
        }
        if (logger.isDebugEnabled ()) {
            logger.debug ("now, the pool contains: " + pool);
        }
    }

    private synchronized static void remove (Tunnel tunnel) {
        if (pool.containsKey (tunnel.name)) {
            List<Tunnel> list = pool.get (tunnel.name);
            list.remove (tunnel);
            if (list.isEmpty ())
                pool.remove (tunnel.name);
        }
        if (logger.isDebugEnabled ()) {
            logger.debug ("now, the pool contains: " + pool.keySet ());
        }
        if (pool.isEmpty () && !monitor.isPaused ()) {
            if (logger.isDebugEnabled ()) {
                logger.debug ("There's no tunnel in pool, halt the monitor up.");
            }
            monitor.pause ();
        }
    }

    public synchronized static Tunnel match (String name) {
        List<Tunnel> list = pool.get (name);
        if (list == null) {
            return null;
        }
        if (list.isEmpty ()) {
            pool.remove (name);
            return null;
        }

        Tunnel tunnel = list.get (0);
        remove (tunnel);
        return tunnel;
    }

    public synchronized static int count (String name) {
        if (pool.containsKey (name)) {
            return pool.get (name).size ();
        }
        return 0;
    }

    public static void cancel () {
        synchronized (pool) {
            for (List<Tunnel> list : pool.values ()) {
                for (Tunnel tunnel : list) {
                    try {
                        tunnel.dismiss ();
                    } catch (IOException e) {
                        e.printStackTrace ();
                    }
                }
            }
            pool.clear ();
        }
        monitor.cancel (true);
    }
}