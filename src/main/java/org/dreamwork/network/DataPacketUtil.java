package org.dreamwork.network;

import org.dreamwork.secure.SecureUtil;
import org.dreamwork.util.Tools;

import java.io.*;
import java.security.Key;

/**
 * Created by seth.yang on 2016/4/29
 */
public class DataPacketUtil {
    public static void writePacket (OutputStream out, DataPacket packet) throws IOException {
        if ((packet.action & DataPacket.MASK_ENCRYPTED) != 0) {
            throw new IOException ("The packet is encrypted, but no secure util present.");
        }

        DataOutputStream dos = new DataOutputStream (out);
        int length = packet.data == null ? 0 : packet.data.length;
        byte[] tmp = Tools.intToBytes (length, 2);

        int sum = packet.action + (tmp [0] & 0xff) + (tmp [1] & 0xff);
        if (length > 0) {
            for (byte b : packet.data) {
                sum += b & 0xff;
            }
        }
        sum &= 0xff;

        dos.writeShort (0xcafe);
        dos.write (packet.action);
        dos.writeShort (length);
        if (packet.data != null && packet.data.length > 0) {
            dos.write (packet.data);
        }
        dos.write (sum);
        dos.writeShort (0xbabe);
    }

    public static void writePacket (OutputStream out, DataPacket packet, SecureUtil security, Key kek) throws Exception {
        packet.action &= DataPacket.MASK_ACTION;
        packet.action |= DataPacket.MASK_ENCRYPTED;
        packet.data    = security.encrypt (packet.data, kek);
        packet.length  = packet.data.length;
        writePacket (out, packet);
    }

    @Deprecated
    public static void writePacket (OutputStream out, byte[] buff) throws IOException {
        writePacket (out, buff, 0, buff.length);
    }

    @Deprecated
    public static void writePacket (OutputStream out, byte[] buff, int index, int length) throws IOException {
        DataOutputStream dos = new DataOutputStream (out);
        int sum = 0;
        for (int i = index; i < index + length; i ++) {
            sum += buff [i] & 0xff;
        }
        sum &= 0xff;

        dos.writeShort (0xcafe);
        dos.writeShort (length);
        dos.write (buff, index, length);
        dos.write (sum);
        dos.writeShort (0xbabe);
        dos.flush ();
        out.flush ();
    }

/*
    public static void writePacket (OutputStream out, byte[] buff, SecureUtil util, Key kek) throws Exception {
        writePacket (out, buff, 0, buff.length, util, kek);
    }

    public static void writePacket (OutputStream out, byte[] buff, int index, int length, SecureUtil util, Key kek) throws Exception {
        if (index != 0 || length != buff.length) {
            buff = Tools.slice (buff, index, length);
        }
        buff = util.encrypt (buff, kek);
        writePacket (out, buff);
    }
*/

/*
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
    public static DataPacket readPacket (InputStream in) throws IOException {
        DataInputStream dis = new DataInputStream (in);
        int head    = dis.readUnsignedShort ();
        int action  = dis.readUnsignedByte ();
        int size    = dis.readUnsignedShort ();
        byte[] buff = new byte[size];
        int length  = dis.read (buff);
        int crc     = dis.readUnsignedByte ();
        int tail    = dis.readUnsignedShort ();

        if (head != 0xcafe) {
            throw new IOException ("Invalid header");
        }
        if (size != length) {
            throw new IOException ("Invalid data. expect " + size + " bytes, but read " + length + " bytes. ");
        }
        int sum = action;
        byte[] tmp = Tools.intToBytes (size, 2);
        for (byte b : tmp)  sum += b & 0xff;
        for (byte b : buff) sum += b & 0xff;
        sum &= 0xff;
        if (sum != crc) {
            throw new IOException ("Invalid CRC");
        }
        if (tail != 0xbabe) {
            throw new IOException ("Invalid tail");
        }
        DataPacket packet = new DataPacket ();
        packet.action     = action & DataPacket.MASK_ACTION;
        packet.encrypted  = ((action & DataPacket.MASK_ENCRYPTED) != 0);
        packet.length     = length;
        packet.data       = buff;
        return packet;
    }

    public static DataPacket readPacket (InputStream in, SecureUtil security, Key kek) throws Exception {
        DataPacket packet = readPacket (in);
        if ((packet.action & DataPacket.MASK_ENCRYPTED) != 0) {
            packet.data   = security.decrypt (packet.data, kek);
            packet.length = packet.data.length;
        }
        return packet;
    }
/*

    public static byte[] readPacket (InputStream in, SecureUtil util, Key kek) throws Exception {
        byte[] buff = readPacket (in);
        return util.decrypt (buff, kek);
    }
*/
}