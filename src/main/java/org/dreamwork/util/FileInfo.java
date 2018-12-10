package org.dreamwork.util;

import org.dreamwork.misc.MimeType;
import org.dreamwork.misc.MimeTypeManager;

import java.io.*;
import java.util.Stack;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 11-11-28
 * Time: 下午6:03
 */
public class FileInfo {
    public static final int MAX_LENGTH = 1024 * 1024 * 2; // 2M

    public static boolean exists (String path) {
        return new File (path).exists ();
    }

    public static File getCanonicalFolder (String path) {
        try {
            return new File (path).getCanonicalFile ().getParentFile ();
        } catch (IOException e) {
            return null;
        }
    }

    public static String getFolder (String path) {
        int pos = path.lastIndexOf ('/');
        if (pos > 0)
            return path.substring (0, pos);
        else if (pos == 0)
            return "/";
        return path;
    }

    public static String getCanonicalParent (String path) {
        try {
            return getCanonicalFolder (path).getParentFile ().getCanonicalPath ();
        } catch (IOException ex) {
            return "";
        }
    }

    public static MimeType getMimeType (String path) {
        return MimeTypeManager.getMimeType (getExtension (path));
    }

    public static String getExtension (String path) {
        int index = path.lastIndexOf ('.');
        if (index == -1) return "";

        return path.substring (index + 1);
    }

    public static String getFileNameWithoutExtension (String path) {
        File file = new File (path);
        String name = file.getName ();
        int index = name.lastIndexOf ('.');
        return index == -1 ? name : name.substring (0, index);
    }

    public static String getFileNameWithoutPath (String path) {
        return new File (path).getName ();
    }

    // base = /dir
    // path = ../javascripts/languages/message.js
    public static String getAbsolutePath (String base, String path) {
        String[] entries = base.split ("/");
        String[] folders = path.split ("/");
        Stack<String> stack = new Stack<String> ();
        for (String entry : entries) {
            stack.push (entry);
        }
        for (String entry : folders) {
            if ("..".equals (entry)) stack.pop ();
            else stack.push (entry);
        }
        StringBuilder builder = new StringBuilder ();
        for (String entry : stack) {
            if (builder.length () != 0) builder.append ('/');
            builder.append (entry);
        }

        if (path.charAt (path.length () - 1) == '/') builder.append ('/');
        return base.charAt (0) == '/' ? "/" + builder : builder.toString ();
    }

    public static void renameTo (File source, File target) throws IOException {
        renameTo (source, target, false);
    }

    public static void renameTo (File source, File target, boolean force) throws IOException {
        if (target.exists () && !force)
            throw new IOException ("The target file: " + target.getCanonicalPath () + " has exists");

        File parent = target.getParentFile ();

        if (!parent.exists () && !parent.mkdirs ())
            throw new IOException ("Can't mkdir: " + parent.getCanonicalPath ());

        if (!source.renameTo (target)) {
            OutputStream out = new FileOutputStream (target);
            InputStream in = new FileInputStream (source);
            long fileLength = source.length ();
            int size = MAX_LENGTH > fileLength ? (int) fileLength : MAX_LENGTH;

            byte[] buff = new byte[size];
            int length;

            try {
                while ((length = in.read (buff)) != -1) {
                    out.write (buff, 0, length);
                    out.flush ();
                }

                if (!source.delete ()) {
                    System.err.println ("Can't delete source file: " + source.getCanonicalPath ());
                }
            } finally {
                in.close ();

                out.flush ();
                out.close ();
            }
        }
    }
}