package org.dreamwork.cli;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by seth.yang on 2018/11/7
 */
public class StdIO implements ICommandLine {
    private BufferedReader reader;

    @Override
    public void error (String message) {
        System.err.println (message);
    }

    @Override
    public void write (byte[] buff, int offset, int size) {
        System.out.write (buff, offset, size);
    }

    @Override
    public void print (String message) {
        System.out.print (message);
    }

    @Override
    public void print (int value) {
        System.out.print (value);
    }

    @Override
    public int read () {
        try {
            return System.in.read ();
        } catch (IOException e) {
            throw new RuntimeException (e);
        }
    }

    @Override
    public int read (byte[] buff, int offset, int size) {
        try {
            return System.in.read (buff, offset, size);
        } catch (IOException e) {
            throw new RuntimeException (e);
        }
    }

    @Override
    public synchronized String readLine () {
        try {
            if (reader == null) {
                reader = new BufferedReader (new InputStreamReader (System.in, "utf-8"));
            }

            return reader.readLine ();
        } catch (IOException ex) {
            throw new RuntimeException (ex);
        }
    }
}
