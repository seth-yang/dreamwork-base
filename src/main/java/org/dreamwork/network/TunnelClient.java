package org.dreamwork.network;

import org.dreamwork.concurrent.CancelableThread;
import org.dreamwork.secure.SecureUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.Key;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by seth.yang on 2016/5/19
 */
public abstract class TunnelClient extends CancelableThread {
    private static final int MAX_THREAD_COUNT = 5;
    private static final ExecutorService service = Executors.newFixedThreadPool (MAX_THREAD_COUNT);

    protected int port;
    protected String host, name;
    protected Socket socket;
    protected SecureUtil security;
    protected Key kek;

    protected TunnelClient (String host, int port, String name) {
        super ("Tunnel Client");
        this.host = host;
        this.port = port;
        this.name = name;
    }

    @Override
    protected void beforeCancel () {
        if (socket != null) {
            try {
                socket.close ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
        }
    }

    public void setSecurity (SecureUtil security) {
        this.security = security;
    }

    public void setKek (Key kek) {
        this.kek = kek;
    }

    @Override
    public void run () {
        try {
            socket = new Socket (host, port);
            InputStream in   = null;
            OutputStream out = null;
        } catch (UnknownHostException e) {
            e.printStackTrace ();
        } catch (IOException e) {
            e.printStackTrace ();
        }
    }
}