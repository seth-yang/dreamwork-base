package org.dreamwork.network.udp;

import org.dreamwork.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Finder {
    private final Logger logger     = LoggerFactory.getLogger (Finder.class);
    private final Object LOCKER     = new byte[0];
    private final int    BUFF_SIZE  = 16;

    public interface IFinderListener {
        void found (InetAddress address);
    }

    private int port, buffSize;
    private DatagramSocket  client;
    private ExecutorService executor;
    private IFinderListener listener;
    private boolean running = true, useLocalExecutor = false;
    private MagicData magic;

    public Finder () {}

    public Finder (ExecutorService executor, int port) {
        this.executor = executor;
        this.port     = port;
    }

    public int getPort () {
        return port;
    }

    public void setPort (int port) {
        this.port = port;
    }

    public ExecutorService getExecutor () {
        return executor;
    }

    public void setExecutor (ExecutorService executor) {
        this.executor = executor;
    }

    public MagicData getMagic () {
        return magic;
    }

    public void setMagic (MagicData magic) {
        this.magic = magic;
    }

    public int getBufferSize () {
        return buffSize;
    }

    public void setBufferSize (int buffSize) {
        if (buffSize <= 0) {
            throw new ArrayIndexOutOfBoundsException (buffSize);
        }
        this.buffSize = buffSize;
    }

    public void setListener (IFinderListener listener) {
        this.listener = listener;
    }

    public void find () {
        if (listener == null) {
            logger.warn ("there's no listener!! terminate process");
            return;
        }

        if (magic == null) {
            logger.error ("magic data is not set!");
            throw new IllegalStateException ("magic data is not set!");
        }

        try {
            InetAddress address   = InetAddress.getByName ("255.255.255.255");
            DatagramPacket packet = new DatagramPacket (magic.ping, magic.ping.length, address, port);

            try (DatagramSocket client = new DatagramSocket ()) {
                client.setBroadcast (true);
                client.setReuseAddress (true);
                client.send (packet);
                client.setSoTimeout (60000);

                DatagramPacket received = new DatagramPacket (new byte[buffSize], buffSize);
                client.receive (received);
                if (logger.isTraceEnabled ()) {
                    logger.trace ("got a response:: {}", new String (received.getData ()).trim ());
                }

                int offset = received.getOffset ();
                int length = received.getLength ();
                byte[] data = Tools.slice (received.getData (), offset, length);
                if (Arrays.equals (data, magic.pong)) {
                    listener.found (received.getAddress ());
                }
            }
        } catch (IOException ex) {
            logger.warn (ex.getMessage (), ex);
        }
    }

    public void find (int port) {
        this.port = port;
        find ();
    }

    public void find (int timeout, TimeUnit unit) {
        if (listener == null) {
            logger.warn ("there's no listener!! terminate process");
            return;
        }

        if (magic == null) {
            logger.error ("magic data is not set!");
            throw new IllegalStateException ("magic data is not set!");
        }

        if (executor == null) {
            executor = Executors.newFixedThreadPool (2);
            useLocalExecutor = true;
        }

        executor.submit (() -> {
            try {
                InetAddress address   = InetAddress.getByName ("255.255.255.255");
                DatagramPacket packet = new DatagramPacket (magic.ping, magic.ping.length, address, port);

                client = new DatagramSocket ();
                client.setBroadcast (true);
                client.setReuseAddress (true);
                client.send (packet);
                while (running) {
                    DatagramPacket received = new DatagramPacket (new byte[buffSize], buffSize);
                    client.receive (received);
                    if (logger.isTraceEnabled ()) {
                        logger.trace ("got a response:: {}", new String (received.getData ()).trim ());
                    }

                    int offset  = received.getOffset ();
                    int length  = received.getLength ();
                    byte[] data = Tools.slice (received.getData (), offset, length);
                    if (Arrays.equals (data, magic.pong)) {
                        listener.found (received.getAddress ());
                    }
                }
                if (logger.isTraceEnabled ()) {
                    logger.trace ("the loop exit");
                }
            } catch (IOException ex) {
                logger.warn (ex.getMessage (), ex);
            }
        });
        executor.submit (() -> {
            synchronized (LOCKER) {
                try {
                    LOCKER.wait (unit.toMillis (timeout));
                    finish ();
                } catch (InterruptedException ex) {
                    logger.warn (ex.getMessage (), ex);
                }
            }
        });

    }

    public void find (int port, int timeout, TimeUnit unit) {
        this.port = port;
        find (timeout, unit);
    }

    public void finish () {
        synchronized (LOCKER) {
            if (running) {
                logger.trace ("shutting down the finder...");
                running = false;
                client.close ();

                LOCKER.notifyAll ();
            } else {
                logger.trace ("the finder has shut down");
            }
        }
        if (useLocalExecutor && executor != null) {
            executor.shutdownNow ();
        }
    }
}