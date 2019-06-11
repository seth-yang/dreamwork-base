package org.dreamwork.network;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import static org.dreamwork.network.TunnelPool.*;

/**
 * Created by seth.yang on 2016/4/18
 */
public class Tunnel {
    private static long globalId = 0;
    private static Logger logger = LoggerFactory.getLogger (Tunnel.class);

    private long id;
    String name;
    Socket a, b;
    long touch, timeout, waitingTimeout;
    int state = STATE_CREATED;
    boolean encrypt = false;

    private TunnelWorker w1, w2;

    public Tunnel (String name, long waitingTimeout, long timeout, boolean encrypt) {
        this.name = name;
        this.waitingTimeout = waitingTimeout;
        this.timeout = timeout;
        id = ++ globalId;
        this.encrypt = encrypt;
    }

    public void waiting (Socket socket) {
        // checking the state
        if (state != STATE_CREATED) {
            throw new IllegalStateException ("Waiting operation only allowed in state STATE_CREATED.");
        }

        this.a = socket;
        state = STATE_WAITING;
        // reset the timestamp
        touch = System.currentTimeMillis ();
        TunnelPool.add (this);
    }

    public void bind (Socket other) throws IOException {
        if (a == null) {
            throw new IllegalStateException ("you should invoke waiting method first");
        }

        this.b = other;
        state = STATE_BOUND;

        // reset the timestamp
        touch = System.currentTimeMillis ();
        w1 = new TunnelWorker (a.getInputStream (), b.getOutputStream (), "a -> b");
        w2 = new TunnelWorker (b.getInputStream (), a.getOutputStream (), "b -> a");
        w1.peer = w2;
        w2.peer = w1;
        w1.start ();
        w2.start ();
    }

    public long getId () {
        return id;
    }

    public void dismiss () throws IOException {
        if (w1 != null) {
            w1.dispose ();
            w1 = null;
        }
        if (w2 != null) {
            w2.dispose ();
            w2 = null;
        }
        if (a != null) {
            a.close ();
            a = null;
        }
        if (b != null) {
            b.close ();
            b = null;
        }
        w1 = null;
        w2 = null;
        state = STATE_DISPOSED;
    }

    public int getState () {
        return state;
    }

    private final class TunnelWorker extends Thread {
        private InputStream in;
        private OutputStream out;
        private boolean running = true;
        private TunnelWorker peer;
        String name;

        TunnelWorker (InputStream in, OutputStream out, String name) {
            this.in = in;
            this.out = out;
            touch = System.currentTimeMillis ();
            setName (this.name = name);
            setDaemon (true);
        }

        @Override
        public void run () {
            byte[] buff = new byte[512];
            try {
                long now = System.currentTimeMillis ();
                while (running) {
                    int n = in.available ();
                    if (n > 0) {
                        int length = in.read (buff, 0, n);
                        out.write (buff, 0, length);
                        out.flush ();
                    } else {
                        if (System.currentTimeMillis () - now > timeout) {
                            running = false;
                            logger.warn (name + " timeout. close it.");
                        }
                    }
                }
            } catch (Exception ex) {
                System.err.println ("thread = " + name);
                ex.printStackTrace ();
            } finally {
                try {
                    dismiss ();
                } catch (IOException e) {
                    e.printStackTrace ();
                }
            }

            if (peer != null) {
                peer.dispose ();
            }
        }

        void dispose () {
            if (logger.isTraceEnabled ()) {
                logger.trace (name + " disposing...");
            }
            running = false;
            if (in != null) try {
                in.close ();
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
            in = null;

            if (out != null) try {
                out.close ();
            } catch (IOException ex) {
                ex.printStackTrace ();
            }
            out = null;
            logger.info (name + " disposed.");
            peer = null;
        }
    }
}