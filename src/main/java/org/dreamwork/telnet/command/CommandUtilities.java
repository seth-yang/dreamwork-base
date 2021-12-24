package org.dreamwork.telnet.command;

import org.dreamwork.cli.text.Alignment;
import org.dreamwork.cli.text.TextFormatter;
import org.dreamwork.telnet.Console;
import org.dreamwork.telnet.TerminalIO;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Objects;

public class CommandUtilities {
    public static final Charset CHARSET = Charset.forName (System.getProperty ("file.encoding"));
    public static final boolean NEED_CONVERT = !("UTF-8".equals (CHARSET.displayName ()));

    public static String readPassword (String prompt, Console console) throws IOException {
        for (int i = 0; i < 3; i ++) {
            console.write (prompt + ": ");
            String p1 = console.readInput (true);
            console.write (prompt + " again: ");
            String p2 = console.readInput (true);

            if (!Objects.equals (p1, p2)) {
                console.errorln ("password not matched.");
                console.println ();
            } else {
                return p1;
            }
        }

        return null;
    }

    public static void help (Console console, String line) throws IOException {
        console.setForegroundColor (TerminalIO.YELLOW);
        console.println (line);
    }

    public static void example (Console console, String line) throws IOException {
        console.setForegroundColor (TerminalIO.CYAN);
        console.println (line);
    }

    public static void error (Console console, String line) throws IOException {
        console.setForegroundColor (TerminalIO.RED);
        console.println ("[Error] " + line);
    }

    public static void success (Console console, String line) throws IOException {
        console.setForegroundColor (TerminalIO.GREEN);
        console.println ("[Success] " + line);
    }

    public static void printTable (Console console, Matrix matrix, Alignment[] alignments, String[] headers) throws IOException {
        if (headers != null && headers.length > 0) {
            // print the table header
            for (int i = 0; i < headers.length; i ++) {
                String header = headers[i];
                if (NEED_CONVERT) {
                    header = new String (header.getBytes (), CHARSET);
                }
                if (i > 0) {
                    console.print ("    ");
                }
                console.print (TextFormatter.fill (header, ' ', matrix.sizes[i], alignments[i]));
            }
            console.println ();

            StringBuilder builder = new StringBuilder ();
            for (int i = 0; i < headers.length; i ++) {
                if (i > 0) {
                    builder.append ("----");
                }
                int w = matrix.sizes[i];
                while (w-- > 0) builder.append ('-');
            }
            console.println (builder.toString ());
        }
        for (String[] line: matrix.data) {
            for (int i = 0; i < line.length; i ++) {
                if (i > 0) {
                    console.print ("    ");
                }
                console.print (TextFormatter.fill (line[i], ' ', matrix.sizes[i], alignments[i]));
            }
            console.println ();
        }
    }

    public static void printLine (Console console, int width) throws IOException {
        byte[] buff = new byte[width];
        for (int i = 0; i < width; i ++) {
            buff[i] = '-';
        }
        console.write (buff);
        console.println ();
    }

    public static int textLength (String text) {
        int width = 0;
        char[] buff = text.toCharArray ();
        for (char ch : buff) {
            if (ch > 255) width ++;
            width ++;
        }
        return width;
    }

    public static Matrix matrix (List<? extends IIndexable> list, String[] headers, String[] indexers) {
        if (indexers == null || indexers.length == 0) {
            throw new IllegalArgumentException ("invalid indexers");
        }
        int[] sizes = new int[indexers.length];
        if (headers != null && headers.length > 0) {
            for (int i = 0; i < headers.length; i ++) {
                sizes[i] = textLength (headers[i]);
            }
        }
        String[][] data = new String[list.size ()][indexers.length];
        for (int i = 0; i < list.size (); i ++) {
            IIndexable line = list.get (i);
            for (int j = 0; j < indexers.length; j ++) {
                String key = indexers[j];
                Object o = line.get (key);
                String text = o == null ? "" : o.toString ();
                if (NEED_CONVERT) {
                    text = new String (text.getBytes (), CHARSET);
                }
                int width = textLength (text);
                if (sizes[j] < width) {
                    sizes[j] = width;
                }
                data [i][j] = text;
            }
        }

        return new Matrix (sizes, data);
    }
}