package org.dreamwork.network;

import org.dreamwork.secure.SecureContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by seth.yang on 2016/5/19
 *<pre>
 * Action 1B | Encrypt 1B | Length 2B | body
 *  action   |  encrypt?  |   length  | body
 *
 * action: bit 7: set mains the body is encrypted.
 *         0x00        - NOOP, a keep-alive signal
 *         0x01 ~ 0x3F - user action
 *         0x40 ~ 0x7F - reserved action
 *         0x41        - request a tunnel
 *         0x42        - match a tunnel
 *</pre>
 */
public abstract class TunnelNorthBridge extends SecureContextServer {
    private static final Logger logger = LoggerFactory.getLogger (TunnelNorthBridge.class);

    public TunnelNorthBridge (String name, SecureContext context, Key kek) throws InvalidKeySpecException, NoSuchAlgorithmException {
        super (name, context, kek);
    }

    public TunnelNorthBridge (String name) {
        super (name);
    }

    protected abstract boolean isValidRequest (byte[] data);
    protected abstract boolean isValidTunnelRequest (String name);
    protected abstract void process (byte[] data);

    private final class Worker implements Runnable {
        Socket socket;

        Worker (Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run () {
            InputStream in = null;
            OutputStream out = null;
            try {
                in = socket.getInputStream ();
                DataPacket packet = read (in);

                out = socket.getOutputStream ();
                switch (packet.action) {
                    case DataPacket.ACTION_NOOP:
                        DataPacketUtil.writePacket (out, DataPacket.NOOP);
                        out.flush ();
                        break;
                    case DataPacket.ACTION_REQUEST_TUNNEL :
                        throw new RuntimeException ("Invalid action");
                    case DataPacket.ACTION_MATCH_TUNNEL :
                        String uuid = new String (packet.data, "utf-8");
                        if (isValidTunnelRequest (uuid)) {
                            int count = TunnelPool.count (uuid);
                            if (logger.isTraceEnabled ()) {
                                logger.trace ("There's " + count + " waiting tunnel");
                            }
                            write (out, count);
                        }
                        break;
                    default :
                        if (isValidRequest (packet.data)) {
                            process (packet.data);
                        } else {
                            throw new RuntimeException ("Invalid request");
                        }
                        break;
                }
            } catch (Exception ex) {
                logger.warn (ex.getMessage (), ex);
            } finally {
                if (in != null) try {
                    in.close ();
                } catch (IOException ex) {
                    logger.warn (ex.getMessage (), ex);
                }

                if (out != null) try {
                    out.close ();
                } catch (IOException ex) {
                    logger.warn (ex.getMessage (), ex);
                }
            }
        }

        private void write (OutputStream out, int count) throws IOException {
            if (count > 0) {
                DataPacket packet = new DataPacket ();
                packet.action = DataPacket.ACTION_REPLY_MATCHES;
                packet.data   = new byte[] {(byte) count};
                DataPacketUtil.writePacket (out, packet);
            } else {
                DataPacketUtil.writePacket (out, DataPacket.NOOP);
            }
        }
    }
}