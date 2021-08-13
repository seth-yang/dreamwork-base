package org.dreamwork.telnet.command;

public class Matrix {
    public final int[] sizes;
    public final String[][] data;

    public Matrix (int[] sizes, String[][] data) {
        this.sizes = sizes;
        this.data = data;
    }
}
