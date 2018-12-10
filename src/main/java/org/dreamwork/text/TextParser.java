package org.dreamwork.text;

import java.io.*;
import java.net.URL;

public abstract class TextParser {
    private static final int MAX_SIZE = 1024 * 1024 * 10;

    protected final class Segment {
        public int start, length;
        public int row, column;

        public Segment (int start, int length) {
            this.start = start;
            this.length = length;
            this.row = line;
            this.column = col;
        }

        public void reset () {
            this.start = cursor;
            this.length = 0;
        }

        public String toString () {
            if (start >= 0 && length > 0)
                return new String (stream, start, length);
            else if (start >= 0 && length == 0)
                return "";
            return null;
        }
    }

    protected char[] stream;
    public int cursor, line, col;

    public TextParser () {}

    public TextParser (File file) throws IOException {
        this (new FileReader (file), file.length () > MAX_SIZE ? MAX_SIZE : (int) file.length ());
    }

    public TextParser (URL url) throws IOException {
        this (url.openStream ());
    }

    public TextParser (InputStream in) throws IOException {
        this (new InputStreamReader (in));
    }

    public TextParser (InputStream in, String charset) throws IOException {
        this (new InputStreamReader (in, charset));
    }

    public TextParser (String src) {
        this.stream = src.toCharArray ();
    }

    public TextParser (Reader reader) throws IOException {
        this (reader, MAX_SIZE);
    }

    private TextParser (Reader reader, int size) throws IOException {
        char[] buff = new char [size];
        CharArrayWriter caw = new CharArrayWriter ();
        int length;
        try {
            while ((length = reader.read (buff)) > 0) caw.write (buff, 0, length);
        } finally {
            reader.close ();
            caw.close ();
        }
        stream = caw.toCharArray ();
    }

    public int nextChar () {
        if (cursor >= stream.length) return -1;
        int ch = stream[cursor];
        if (ch == '\n') {
            line ++;
            col = 0;
        } else
            col ++;
        cursor ++;
        return ch;
    }

    public void skip (int n) {
        if (n > 0)
            for (int i = 0; i < n; i ++) nextChar ();
    }

    public int peek () {
        if (cursor >= stream.length) return -1;
        return stream[cursor];
    }

    public boolean isspace () {
        return peek () <= ' ' && peek () >= 0;
    }

    public int skipSpace () {
        int i = 0;
        while (isspace ()) {
            i ++;
            nextChar ();
        }
        return i;
    }

    public Segment skipUntil (String s) {
        int length = s.length ();
        Segment seg = new Segment (cursor, 0);
        SKIP:
        for (int ch = nextChar (); ch != -1; ch = nextChar ()) {
            if (ch == s.charAt (0)) {
                for (int i = 1; i < length; i ++) {
                    if (peek () == s.charAt (i)) nextChar ();
                    else
                        continue SKIP;
                }

                seg.length = cursor - seg.start - length;
                return seg;
            }
        }

        return null;
    }

    public Segment skipUntilOutQuot (String limit, boolean ignoreCase) {
        Segment seg = new Segment (cursor, 0);
        int length = limit.length ();
        int quot_char = -2;
        skip1:
        for (int ch = nextChar (); ch != -1; ch = nextChar ()) {
            switch (ch) {
                case '\'' :
                    if (quot_char == '\'') {
                        if (stream[cursor - 1] != '\\') {
                            quot_char = -2;
                        }
                    } else if (quot_char != '"') {
                        quot_char = '\'';
                    }
                    break;
                case '"' :
                    if (quot_char == '"') {
                        if (stream[cursor - 1] != '\\') {
                            quot_char = -2;
                        }
                    } else if (quot_char != '\'') {
                        quot_char = '"';
                    }
                    break;
                default  :
                    if (quot_char < 0) { 
                        if (ch == '<') {
                            for (int i = 1; i < length; i ++) {
                                if (ignoreCase && Character.toLowerCase ((char) peek ()) == limit.charAt (i)) nextChar ();
                                else if (peek () == limit.charAt (i)) nextChar ();
                                else
                                    continue skip1;
                            }

                            seg.length = cursor - seg.start - length;
                            return seg;
                        }
                    }
            }
        }
        return null;
    }

    public void reset (Segment s) {
        s.start = cursor;
        s.length = 0;
    }

    public int getLineNumber () {
        return line;
    }

    public int getColumnNumber () {
        return col;
    }
}