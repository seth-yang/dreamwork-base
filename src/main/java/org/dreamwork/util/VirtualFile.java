package org.dreamwork.util;

import java.io.*;
import java.util.Properties;

public class VirtualFile {
    private String clientPath;
    private String name;
    private String type;
    private byte[] content;
    private long length = -1;
    private VirtualOutputStream baos;

    private boolean useCach = false;
    private String tempFileName;

    private static long MAX_LENGTH = 1024 * 1024 * 2;

    private static final String KEY_FILE_LENGTH = "jasmine.upload.filelength";
    private static final File tempDir;
    private static final char[] Chars = {'~', '!', '@', '#', '$', '%', '^', '&', '-', '_', '+', '='};
    private static final int CHAR_LENGTH = Chars.length;

    static {
        // prepare temp dir.
        String dir = System.getProperty ("user.home") + "/.dreamwork/cach/jasmine/virtual-files";
        tempDir = new File (dir);
        if (!tempDir.exists () && !tempDir.mkdirs ())
            try {
                throw new RuntimeException ("Can't mkdir: " + tempDir.getCanonicalPath ());
            } catch (IOException e) {
                e.printStackTrace ();
            }

        // calculate max length.
        String fLength = System.getProperty (KEY_FILE_LENGTH);
        if (fLength != null) {
            MAX_LENGTH = calculateFileLength (fLength);
        } else {
            InputStream in = VirtualFile.class.getClassLoader ().getResourceAsStream ("jasmine.properties");
            if (in != null) {
                Properties props = new Properties ();
                try {
                    props.load (in);
                    fLength = props.getProperty (KEY_FILE_LENGTH);
                    if (fLength != null)
                        MAX_LENGTH = calculateFileLength (fLength);
                } catch (IOException ignore) {}
            }
        }
    }

    private static long calculateFileLength (String s) {
        if (s == null) return MAX_LENGTH;
        s = s.trim ();
        int length = s.length ();
        char ch = s.charAt (length - 1);
        if (ch > '9') {
            switch (ch) {
                case 'M':
                case 'm':
                    try {
                        String number = s.substring (0, length - 1).trim ();
                        return Long.parseLong (number) * 1024 * 1024;
                    } catch (Exception ignore) {}
                    break;
                case 'k':
                case 'K':
                    try {
                        String number = s.substring (0, length - 1).trim ();
                        return Long.parseLong (number) * 1024;
                    } catch (Exception ignore) {}
            }
        } else {
            try { return Long.parseLong (s); } catch (Exception ignore) {}
        }
        return MAX_LENGTH;
    }

    public VirtualFile () {
    }

    public VirtualFile (int length) {
        this.length = length;
        if (this.length > MAX_LENGTH) initCach ();
    }

    public VirtualFile (boolean useCach) {
        if (useCach) initCach ();
    }

    public String getClientPath () {
        return clientPath;
    }

    public void setClientPath (String clientPath) {
        this.clientPath = clientPath;
    }

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getType () {
        return type;
    }

    public void setType (String type) {
        this.type = type;
    }

    public byte[] getContent () {
        if (useCach) throw new IllegalStateException ("too large data to load in memory");
        return content;
    }

    private void initCach () {
        if (tempFileName == null) {
            useCach = true;
            tempFileName = getTempFileName ();
        }
    }

    public void setContent (byte[] content) throws IOException {
        length = content.length;
        if (content.length > MAX_LENGTH) {
            File file = new File (tempDir, tempFileName);
            FileOutputStream fos = new FileOutputStream (file);
            try {
                fos.write (content);
            } finally {
                fos.flush ();
                fos.close ();
            }
            useCach = true;
        } else
            this.content = content;
    }

    public long getLength () {
        if (length != -1) return length;
        if (useCach) {
            File file = new File (tempDir, tempFileName);
            if (file.exists () && file.canRead ())
                return length = file.length ();
        } else {
            if (content != null)
                return length = content.length;
        }
        return length;
    }

    public boolean isUseCach () {
        return useCach;
    }

    public InputStream getInputStream () throws IOException {
        InputStream in;
        if (useCach) {
            File file = new File (tempDir, tempFileName);
            in = new FileInputStream (file);
        } else {
            in = new ByteArrayInputStream (content);
        }

        return in;
    }

    public OutputStream getOutputStream () throws IOException {
        if (useCach) {
            File file = new File (tempDir, tempFileName);
            return new FileOutputStream (file);
        } else {
            if (baos == null) baos = new VirtualOutputStream ();
            return baos;
        }
    }

    public boolean deleteTempFile () {
        if (!useCach) return true;

        File file = new File (tempDir, tempFileName);
        return file.delete ();
    }

    public void save (String path) throws IOException {
        File dir = new File (path);
        if (!dir.exists () && !dir.mkdirs ())
            throw new IOException ("Can't mkdir: " + dir.getCanonicalPath ());
        File file = new File (dir, name);
        if (useCach) {
            File tempFile = new File (tempDir, tempFileName);
//            tempFile.renameTo (file);
            FileInfo.renameTo (tempFile, file, true);
        } else {
            FileOutputStream fos = new FileOutputStream (file);
            try {
                fos.write (content);
            } finally {
                fos.flush ();
                fos.close ();
            }
        }
    }

    public void saveAs (String name) throws IOException {
        File file = new File (name);
        File dir = file.getParentFile ();
        if (!dir.exists ()&& !dir.mkdirs ())
            throw new IOException ("Can't mkdir: " + dir.getCanonicalPath ());
//        File file = new File (dir, name);
        if (useCach) {
            File tempFile = new File (tempDir, tempFileName);
//            tempFile.renameTo (file);
            FileInfo.renameTo (tempFile, file, true);
        } else {
            FileOutputStream fos = new FileOutputStream (file);
            try {
                fos.write (content);
            } finally {
                fos.flush ();
                fos.close ();
            }
        }
    }

    public void load (String path) throws IOException {
        File dir = new File (path);
        if (!dir.exists ()) throw new IOException (path + " not exist");
        File file = new File (dir, name);
        FileInputStream fis = new FileInputStream (file);
        content = new byte[(int) file.length ()];
        try {
            length = fis.read (content);
        } finally {
            fis.close ();
        }
    }

    private static String getTempFileName () {
        long now = System.currentTimeMillis ();
        String temp = String.valueOf (now);
        int length = temp.length () * 2 + CHAR_LENGTH;
        StringBuffer sb = new StringBuffer ();
        int i = 0;
        while (i < length) {
            double first = Math.random ();
            if (first < .5) {
                int index = (int) (Math.random () * CHAR_LENGTH);
                sb.append (Chars[index]);
            } else {
                double d = Math.random () * 62;
                if (d < 10)
                    sb.append ((char) ('0' + d));
                else if (d < 36)
                    sb.append ((char) ('a' + d - 10));
                else
                    sb.append ((char) ('A' + d - 36));
            }

            i++;
        }

        return sb.toString ();
    }

    private class VirtualOutputStream extends ByteArrayOutputStream {
        public void flush () throws IOException {
            content = toByteArray ();
            super.flush ();
        }

        public void close () throws IOException {
            flush ();
            super.close ();
        }
    }

    public static void main (String[] args) throws Exception {
        VirtualFile vfile = new VirtualFile (true);
        OutputStream out = vfile.getOutputStream ();
        File file = new File ("D:/Downloads/README.TXT");
        FileInputStream in = new FileInputStream (file);
        byte[] buff = new byte[1024];
        int len;
        while ((len = in.read (buff)) != -1) out.write (buff, 0, len);
        in.close ();
        out.flush ();

        System.out.println (vfile.getContent ().length);
    }
}