package org.dreamwork.network;

import org.dreamwork.secure.SecureContext;
import org.dreamwork.secure.SecureUtil;

import java.io.*;
import java.net.ServerSocket;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by game on 2016/4/19
 */
public abstract class SecureContextServer extends Server {
    protected static final ExecutorService service = Executors.newCachedThreadPool ();
    protected SecureUtil security;
    protected Key kek;
    protected SecureContext context;

    protected ServerSocket server;

    public SecureContextServer (String name) {
        super (name);
        setDaemon (true);
    }

    public SecureContextServer (String name, SecureContext context, Key kek) throws InvalidKeySpecException, NoSuchAlgorithmException {
        super (name);
        this.context = context;
        this.security = new SecureUtil (context);
        this.kek = kek;
        setDaemon (true);
    }

    @Override
    protected void beforeBind () throws Exception {
        server = new ServerSocket (port);
    }

    @Override
    protected void beforeCancel () {
        if (server != null) try {
            server.close ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }

    protected DataPacket read (InputStream in) throws Exception {
        DataPacket packet = DataPacketUtil.readPacket (in);
        if (packet.encrypted) {
            if (security == null) {
                throw new RuntimeException ("Invalid request. missing security and kek");
            }
            packet.data   = security.decrypt (packet.data, kek);
            packet.length = packet.data.length;
        }

        return packet;
    }

    protected void write (OutputStream out, DataPacket packet) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        DataOutputStream dos       = new DataOutputStream (baos);
        int action = packet.action;
        if (packet.encrypted) {
            action |= 0x80;
        }
        dos.write (action);
        dos.write (packet.data == null ? 0 : packet.data.length);
        if (packet.data != null && packet.length > 0) {
            dos.write (packet.data);
        }
        byte[] buff = baos.toByteArray ();
        if (packet.encrypted) {
            if (security == null) {
                throw new RuntimeException ("I've no idea how to encrypt the data without security");
            }
            buff = security.encrypt (buff, kek);
        }
        DataPacketUtil.writePacket (out, buff);
    }
}