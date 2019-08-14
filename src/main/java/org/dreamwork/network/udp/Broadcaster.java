package org.dreamwork.network.udp;

import org.dreamwork.util.StringUtil;
import org.dreamwork.util.Tools;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.MulticastSocket;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * UDP 广播类.
 *
 * <p>这个类通常用于在一个 DHCP 子网内用于主机发现.</p>
 *
 */
public class Broadcaster {
    private final Logger logger     = LoggerFactory.getLogger (Broadcaster.class);
    private final int    BUFF_SIZE  = 16;

    private int port, buffSize = BUFF_SIZE;
    private MulticastSocket server;
    private ExecutorService executor;
    private boolean running = true, useLocalExecutor = false;
    private MagicData magic;

    public Broadcaster () {}

    public Broadcaster (ExecutorService executor, int port) {
        this.executor = executor;
        this.port     = port;
    }

    public int getPort () {
        return port;
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

    public void bind (int port) throws IOException {
        this.port = port;
        bind ();
    }

    public void bind () throws IOException {
        if (logger.isTraceEnabled ()) {
            logger.trace ("binding the broadcaster...");
        }
        server = new MulticastSocket (port);
        logger.info ("the broadcaster bound on {}:{}", server.getInterface ().getHostAddress (), port);

        if (magic == null) {
            logger.error ("magic data is not set");
            throw new IllegalStateException ("magic data is not set");
        }

        if (executor == null) {
            executor = Executors.newFixedThreadPool (16);
            useLocalExecutor = true;
        }

        executor.submit (() -> {
            while (running) {
                try {
                    byte[] buff = new byte[buffSize];
                    DatagramPacket packet = new DatagramPacket (buff, buffSize);
                    server.receive (packet);
                    byte[] data = packet.getData ();
                    int length = packet.getLength ();
                    int offset = packet.getOffset ();

                    if (logger.isTraceEnabled ()) {
                        logger.trace ("got a message: {}", StringUtil.format (data, offset, length));
                        logger.trace (new String (data, offset, length));
                    }
                    data = Tools.slice (data, offset, length);
                    if (checkMagic (data)) {
                        // magic data matched, reply another magic
                        SocketAddress address = packet.getSocketAddress ();
                        if (logger.isTraceEnabled ()) {
                            logger.trace ("response to {}", address);
                        }
                        DatagramPacket dp = new DatagramPacket (magic.pong, magic.pong.length, address);
                        new DatagramSocket ().send (dp);
                    }
                } catch (IOException ex) {
                    logger.warn (ex.getMessage (), ex);
                }
            }
        });
    }

    public void unbind () {
        running = false;

        if (logger.isTraceEnabled ()) {
            logger.trace ("unbinding the broadcaster...");
        }
        if (server != null) {
            server.close ();
            logger.info ("the broadcaster unbind");
        }

        if (useLocalExecutor && executor != null) {
            executor.shutdownNow ();
        }
    }

    private boolean checkMagic (byte[] data) {
        return Arrays.equals (data, magic.ping);
    }
}