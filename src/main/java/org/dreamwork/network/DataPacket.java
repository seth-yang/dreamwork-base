package org.dreamwork.network;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by seth.yang on 2016/5/19
 *
 * Data Frame:
 * 0      |2      |3      |4    |4 + length |5 + length
 * -------+-------+-------+-----+-----------+----------
 * 0xcafe |action |length |data | CRC       |0xbabe
 *
 * action: bit 7: set mains the body is encrypted.
 *         0x00        - NOOP, a keep-alive signal
 *         0x01 ~ 0x6F - user action
 *         0x70        - OK
 *         0x71        - Fail
 *         0x72        - tunnel is ready
 *         0x73        - request a tunnel
 *         0x74        - match a tunnel
 *         0x75        - replay a tunnel
 *         0x76        - assign the rsa key
 *         0x77 ~ 0x7F - reserved
 */
public class DataPacket {
    public static final int ACTION_NOOP           = 0;
    public static final int ACTION_RESPONSE_OK    = 0x70;
    public static final int ACTION_RESPONSE_FAIL  = 0x71;
    public static final int ACTION_TUNNEL_READY   = 0x72;
    public static final int ACTION_REQUEST_TUNNEL = 0x73;
    public static final int ACTION_MATCH_TUNNEL   = 0x74;
    public static final int ACTION_REPLY_MATCHES  = 0x75;
    public static final int ACTION_ASSIGN_KEY     = 0x76;

    public static final int MASK_ACTION           = 0x7F;
    public static final int MASK_ENCRYPTED        = 0x80;

    public int length, action;
    public boolean encrypted;
    public byte[] data;

    public DataPacket () {}

    public DataPacket (int action) {
        this.action = action & MASK_ACTION;
        this.encrypted = (action & MASK_ENCRYPTED) != 0;
    }

    public DataPacket (int action, boolean encrypted) {
        this.action = action;
        this.encrypted = encrypted;
    }

    public byte[] toByteArray () throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream ();
        DataOutputStream dos = new DataOutputStream (baos);

        writeData (dos);
        byte[] buff = baos.toByteArray ();
        int sum = 0;
        for (byte b : buff) sum += b& 0xff;
        sum &= 0xff;

        baos.reset ();
        dos.writeShort (0xcafe);
        writeData (dos);
        dos.write (sum);
        dos.writeShort (0xbabe);

        return baos.toByteArray ();
    }

    private void writeData (DataOutputStream dos) throws IOException {
        dos.write (action);
        dos.writeShort (data == null ? 0 : data.length);
        if (data != null && data.length > 0) {
            dos.write (data);
        }
    }

    public static final DataPacket NOOP  = new DataPacket (ACTION_NOOP, false);
    public static final DataPacket FAIL  = new DataPacket (ACTION_RESPONSE_FAIL, false);
    public static final DataPacket OK    = new DataPacket (ACTION_RESPONSE_OK, false);
    public static final DataPacket READY = new DataPacket (ACTION_TUNNEL_READY, false);
}