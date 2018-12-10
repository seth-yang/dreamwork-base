package org.dreamwork.network;

import org.apache.log4j.Logger;
import org.dreamwork.concurrent.CancelableThread;

import java.io.IOException;

/**
 * Created by seth on 16-1-11
 */
public abstract class Server extends CancelableThread implements IServer {
    private static final Logger logger = Logger.getLogger (Server.class);

    protected int port;

    public Server () {
    }

    public Server (String name) {
        super (name);
    }

    @Override
    public void bind (int port) throws IOException {
        this.port = port;
        running = true;
        try {
            beforeBind ();
            super.start ();
            logger.info ("Server [" + getName () + "] listen on: " + port);
        } catch (Exception ex) {
            logger.warn (ex.getMessage (), ex);
            beforeCancel ();
        }
    }

    protected abstract void beforeBind () throws Exception;
}