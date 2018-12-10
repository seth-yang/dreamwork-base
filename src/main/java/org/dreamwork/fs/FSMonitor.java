package org.dreamwork.fs;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created with IntelliJ IDEA.
 * User: seth
 * Date: 13-1-15
 * Time: 上午11:28
 */
public class FSMonitor implements Runnable {
    private File index;
    private Map<String, Long> map = new ConcurrentHashMap<String, Long> ();
    private boolean running = true;
    private long interval = 60000L;

    private static final Object locker = new Object ();
    private static final String INDEX_FILE = ".index";

    public FSMonitor (File basedir) {
        this.index = new File (basedir, INDEX_FILE);
    }

    public Map<String, Long> dumpIndices () {
        Map<String, Long> copy = new HashMap<String, Long> ();
        synchronized (locker) {
            copy.putAll (map);
        }
        return copy;
    }

    public void restoreIndices () throws IOException {
        if (index.exists ()) {
            FileReader fr = null;
            try {
                fr = new FileReader (index);
                BufferedReader reader = new BufferedReader (fr);
                String line;
                while ((line = reader.readLine ()) != null) {
                    String[] a = line.split ("\\|");
                    map.put (a[0], Long.parseLong (a[1]));
                }
            } finally {
                if (fr != null) fr.close ();
            }
        }
    }

    public void flushIndices () throws IOException {
        OutputStream out = new FileOutputStream (index);
        try {
            Map<String, Long> indices = dumpIndices ();

            Writer writer = new OutputStreamWriter (out, "UTF-8");
            for (String fileName : indices.keySet ()) {
                writer.write (fileName);
                writer.append ('|');
                writer.append (String.valueOf (map.get (fileName)));
                writer.append ('\n');
                writer.flush ();
                out.flush ();
            }
        } finally {
            out.flush ();
            out.close ();
        }
    }

    public void commit (File file) throws IOException {
        synchronized (locker) {
            map.put (file.getCanonicalPath (), file.lastModified ());
        }
    }

    @Override
    public void run () {
        while (running) {
            try {
                Thread.sleep (interval);

                flushIndices ();
            } catch (Exception e) {
                e.printStackTrace ();
            }
        }
    }
}