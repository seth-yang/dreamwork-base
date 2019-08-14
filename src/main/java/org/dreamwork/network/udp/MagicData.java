package org.dreamwork.network.udp;

public class MagicData {
    public final byte[] ping, pong;

    public MagicData (byte[] ping, byte[] pong) {
        this.ping = ping;
        this.pong = pong;

        if (ping == null || ping.length == 0) {
            throw new NullPointerException ("ping is null");
        }
        if (pong == null || pong.length == 0) {
            throw new NullPointerException ("pong is null");
        }
    }
}
