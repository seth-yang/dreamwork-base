package org.dreamwork.telnet;

import org.apache.log4j.Logger;
import org.dreamwork.telnet.command.Command;
import org.dreamwork.telnet.command.CommandParser;
import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by seth.yang on 2018/9/20
 */
public class Console extends TerminalIO {
    private static final int MAX_HISTORY_SIZE  = 255;
    private static final int DEFAULT_BUFF_SIZE = 1024;

    private static final Logger logger = Logger.getLogger (Console.class);

    private char[] buff;
    private int pos, history_index = 0, cursor = 0;
    private String prompt = "console";
    private boolean running = true, tab_mode;
    private CommandParser commandParser;
    private Map<String, String> env = new HashMap<> (System.getenv ());
    private Map<String, Object> attr = new HashMap<> ();

    private List<String> history = new ArrayList<> (MAX_HISTORY_SIZE);

    public Console (InputStream in, OutputStream out, ConnectionData data) {
        this (in, out, data, DEFAULT_BUFF_SIZE);
    }

    public Console (InputStream in, OutputStream out, ConnectionData data, int buffSize) {
        super (in, out, data);
        if (buffSize > 0) {
            buff = new char[buffSize];
        }
        pos = 0;
    }

    public void setCommandParser (CommandParser parser) {
        this.commandParser = parser;
    }

    public void setPrompt (String prompt) {
        this.prompt = prompt;
    }

    public void setBuffSize (int size) {
        if (size > 0) {
            if (logger.isTraceEnabled ()) {
                logger.trace ("setting buff size to " + size);
            }
            buff = new char[size];
            pos = 0;
        }
    }

    public void setEnv (String key, String name) {
        if (StringUtil.isEmpty (name)) {
            env.remove (key);
        } else
            env.put (key, name);
    }

    public String getEnv (String key) {
        return env.get (key);
    }

    public Map<String, String> getEnvironment () {
        return new HashMap<> (env);
    }

    public<T> void setAttribute (String key, T value) {
        attr.put (key, value);
    }

    @SuppressWarnings ("unchecked")
    public<T> T getAttribute (String key) {
        return (T) attr.get (key);
    }

    public void home () throws IOException {
        homeCursor ();
        if (isAutoflushing ()) {
            flush ();
        }
    }

    public void clear () throws IOException {
        eraseScreen ();
        homeCursor ();
        if (isAutoflushing ()) {
            flush ();
        }
    }

    public void loop () throws IOException {
        if (commandParser == null) {
            throw new IllegalStateException ("command parser is not present");
        }

        int ch;
        showPrompt ();
        while (running) {
            ch = read ();
            switch (ch) {
                case 3: // ctrl-c
                    if (logger.isTraceEnabled ()) {
                        logger.trace ("we got ctrl-c, break input");
                    }
                    write ("^C");
                    println ();
                    pos = cursor = 0;
                    showPrompt ();
                    break;
                case UNRECOGNIZED:
                    if (logger.isTraceEnabled ()) {
                        logger.trace ("unrecognized input: " + String.format ("%02x", ch));
                    }
                    break;
                case ENTER:
                    // here, we got something input.
                    println ();
                    if (pos > 0) {
                        String line = new String (buff, 0, pos).trim ();
                        if (logger.isTraceEnabled ()) {
                            logger.trace ("received line: " + line);
                        }
                        if ("q".equals (line) || "quit".equals (line) || "exit".equals (line)) {
                            running = false;
                            continue;
                        }

                        // save the command anyway
                        if (history.size () >= MAX_HISTORY_SIZE) {
                            history.remove (0);
                        }
                        history.add (line);
                        history_index = history.size ();

                        Command command = commandParser.parse (line);
                        if (command != null) {
                            if (logger.isTraceEnabled ())
                                logger.trace ("command = " + command.name);

                            int idx = line.indexOf (' ');
                            if (idx > 0) {
                                command.parse (line.substring (idx + 1).trim ());
                            }
                            command.perform (this);
                        } else {
                            errorln ("Invalid Command: " + line);
                        }
                    }
                    cursor = pos = 0; // reset pos and cursor
                    showPrompt ();
                    break;
                case BACKSPACE :
                case DELETE:
                case DEL :
                    if (cursor > 0) {
                        if (pos == cursor) {
                            // the normal mode
                            backspace ();
                        } else {
                            // at first, backspace a char
                            super.backspace ();
                            // cursor move left
                            cursor --;
                            // copy the cached chars
                            for (int i = cursor; i < pos; i ++) {
                                buff [i] = buff [i + 1];
                                write ((byte) buff[i]);
                            }
                            // the position move left
                            pos --;
                            // move client cursor left (pos - cursor) times.
                            moveCursor (LEFT, pos - cursor);
/*
                            cursor --;
                            System.arraycopy (buff, cursor + 1, buff, cursor, pos - cursor);
                            pos --;

                            String tmp = new String (buff, 0, pos);
                            int delta = pos - cursor;

                            while (cursor-- > 0) {
                                super.backspace ();
                            }
                            fillBuff (tmp);
                            moveCursor (LEFT, delta);
                            cursor = pos - delta;
*/
                        }
                    }
                    break;
                case TerminalIO.UP:
                    if (history_index > 0) {
                        history_index --;
                        processHistory ();
                    }
                    break;
                case TerminalIO.DOWN :
                    if (history_index < history.size () - 1) {
                        history_index ++;
                        processHistory ();
                    }
                    break;
                case TerminalIO.LEFT:
                    if (cursor > 0) {
                        moveCursor (LEFT, 1);
                        cursor --;
                    }
                    break;
                case TerminalIO.RIGHT:
                    if (cursor < pos) {
                        moveCursor (RIGHT, 1);
                        cursor ++;
                    }
                    break;
                case TABULATOR :
                    processTab ();
                    break;
                default:
                    if (cursor != pos) {
                        insert ((char) ch);
                    } else {
                        buff[pos++] = (char) ch;
                        write ((byte) ch);
                        cursor ++;
                    }
                    break;
            }

            if (ch != TABULATOR) {
                tab_mode = false;
            }
        }

        clear ();
        home ();
    }

    private void insert (char ch) throws IOException {
        int length = pos - cursor;
        char[] tmp = new char[length];
        System.arraycopy (buff, cursor, tmp, 0, length);
        buff [cursor ++] = ch;
        write (ch);
        for (int i = 0; i < length; i ++) {
            buff [cursor + i] = tmp [i];
            write (tmp [i]);
        }
        pos ++;
        moveCursor (LEFT, length);
    }

    private void processTab () throws IOException {
        String tmp = new String (buff, 0, pos);
        if (!StringUtil.isEmpty (tmp)) {
            tmp = tmp.trim ();

            List<Command> list = commandParser.guess (tmp);

            if (list != null && list.size () > 0) {
                // something valid.
                if (list.size () == 1) {
                    // goal. show the command guess
                    Command cmd = list.get (0);

                    String target = cmd.name.length () > tmp.length () ? cmd.name : tmp;

                    List<String> result = cmd.guess (target);
                    if (result != null && result.size () > 0) {
                        if (result.size () == 1) {
                            clearBuffer ();
                            fillBuff (cmd.name + " " + result.get (0));
                        } else {
                            println ();
                            int i = 0;
                            for (String text : result) {
                                if (i ++ > 0) {
                                    println ();
                                }
                                write ("  ");
                                write (text);
                            }
                            println ();
                            clearBuffer ();
                            showPrompt ();
                            fillBuff (tmp);
                        }
                    } else { // match single command, show it.
                        clearBuffer ();
                        fillBuff (cmd.name + ' ');
                    }
                    tab_mode = false;
                } else {
                    if (tab_mode) {
                        println ();
                        clearBuffer ();
                        int i = 0;
                        for (Command cmd : list) {
                            if (i++ > 0) {
                                this.write ("\t\t");
                            }
                            write (cmd.name);
                        }
                        println ();
                        showPrompt ();
                        fillBuff (tmp);
                        tab_mode = false;
                    } else {
                        tab_mode = true;
                    }
                }
            }
        }
    }

    public void backspace () throws IOException {
        super.backspace ();
        if (pos == cursor) {
            cursor --;
        }
        pos --;
    }

    public void clearBuffer () throws IOException {
        while (pos > 0) {
            backspace ();
        }
        cursor = pos;
    }

    private void fillBuff (String command) throws IOException {
        pos = 0;
        cursor = 0;
        char[] tmp = command.toCharArray ();
        for (char ch : tmp) {
            buff [pos ++] = ch;
            write ((byte) ch);
            cursor ++;
        }
    }

    private void processHistory () throws IOException {
        String text = history.get (history_index);
        clearBuffer ();
        fillBuff (text);
    }

    private void showPrompt () throws IOException {
        write (prompt);
        write ("> ");
    }
}