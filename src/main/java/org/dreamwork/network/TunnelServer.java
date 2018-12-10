package org.dreamwork.network;

import org.apache.log4j.Logger;
import org.dreamwork.secure.SecureContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.Key;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by seth.yang on 2016/5/19
 */
public class TunnelServer extends SecureContextServer {
    private static final Logger logger = Logger.getLogger (TunnelServer.class);
    private static final ExecutorService service = Executors.newCachedThreadPool ();

    private long waitTimeout = 120000L, // default to 2 minutes
                 timeout = 900000L;     // default to 15 minutes

    public TunnelServer (String name) {
        super (name);
    }

    public TunnelServer (String name, SecureContext context, Key kek) throws InvalidKeySpecException, NoSuchAlgorithmException {
        super (name, context, kek);
    }

    public void setWaitTimeout (long waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public void setTimeout (long timeout) {
        this.timeout = timeout;
    }

    @Override
    protected void doWork () {
        try {
            Socket socket = server.accept ();
            service.execute (new Worker (socket));
        } catch (IOException ex) {
            logger.warn (ex.getMessage (), ex);
        }
    }

    @Override
    protected void beforeBind () throws Exception {
        server = new ServerSocket (port);
    }

    @Override
    protected void beforeCancel () {
        service.shutdownNow ();
        try {
            server.close ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
        super.beforeCancel ();
    }

    private final class Worker implements Runnable {
        Socket socket;

        Worker (Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run () {
            try {
                InputStream in = socket.getInputStream ();
                DataPacket packet = read (in);
                switch (packet.action) {
                    case DataPacket.ACTION_REQUEST_TUNNEL :
                        requestTunnel (socket, packet);
                        break;
                    case DataPacket.ACTION_MATCH_TUNNEL :
                        String name = new String (packet.data, "utf-8");
                        matchTunnel (socket, name);
                        break;
                }
            } catch (Exception ex) {
                logger.warn (ex.getMessage (), ex);
            }
        }
    }

/*
    Data Frame:
    0    |1     2     |3
    0x41 |body-length |tunnel-name in utf-8
 */
    private void requestTunnel (Socket socket, DataPacket packet) throws IOException {
        String name = new String (packet.data, "utf-8");
        if (logger.isDebugEnabled ()) {
            logger.debug ("requesting a tunnel to " + name);
        }
        Tunnel tunnel = new Tunnel (name, waitTimeout, timeout, packet.encrypted);
        tunnel.waiting (socket);
    }

/*
    input Data Frame:
    0    |1     2     |3
    0x42 |body-length |tunnel-name in utf-8
 */
    private void matchTunnel (Socket socket, String name) throws Exception {
        Tunnel tunnel = TunnelPool.match (name);
        if (tunnel != null) {
            if (tunnel.encrypt) {
                assignKey (socket, tunnel.a);
            }

            ready (socket);
            ready (tunnel.a);

            tunnel.bind (socket);
        }
    }

    private void assignKey (Socket pri, Socket pub) throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance ("RSA");
        KeyPair pair = kpg.generateKeyPair ();
        assignKey (pri.getOutputStream (), pair.getPrivate ().getEncoded ());
        assignKey (pub.getOutputStream (), pair.getPublic ().getEncoded ());
    }

    private void assignKey (OutputStream out, byte[] data) throws Exception {
        DataPacket packet = new DataPacket ();
        packet.action = DataPacket.ACTION_ASSIGN_KEY;
        packet.encrypted = true;
        packet.data = data;
        DataPacketUtil.writePacket (out, packet, security, kek);
    }

/*
    0    |
    0x40 |
 */
    private void ready (Socket socket) throws Exception {
        DataPacketUtil.writePacket (socket.getOutputStream (), DataPacket.READY);
    }
}