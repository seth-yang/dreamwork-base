package org.dreamwork.telnet;

import org.dreamwork.cli.ICommandLine;
import org.dreamwork.cli.text.TextFormater;
import org.dreamwork.telnet.command.Command;
import org.dreamwork.telnet.command.CommandParser;
import org.dreamwork.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by seth.yang on 2018/9/20
 */
public class Console extends TerminalIO implements ICommandLine {
    private static final int MAX_HISTORY_SIZE  = 255;
    private static final int DEFAULT_BUFF_SIZE = 1024;

    private static final Logger logger = LoggerFactory.getLogger (Console.class);
    private static final Pattern PATTERN = Pattern.compile ("^\\s*[a-zA-Z]([a-zA-Z\\-_$.]+)?\\s+(.*?)$");

    private char[] buff;
    private int pos, history_index = 0, cursor = 0;
    private String prompt = "console";
    private boolean running = true, tab_mode;
    private CommandParser commandParser;
    private Map<String, String> env = new HashMap<> (System.getenv ());
    private Map<String, Object> attr = new HashMap<> ();

    private List<String> history = new ArrayList<> (MAX_HISTORY_SIZE);

    public Console (InputStream in, OutputStream out, ConnectionData data, boolean ssh) {
        this (in, out, data, ssh, DEFAULT_BUFF_SIZE);
    }

    public Console (InputStream in, OutputStream out, ConnectionData data) {
        this (in, out, data, DEFAULT_BUFF_SIZE);
    }

    public Console (InputStream in, OutputStream out, ConnectionData data, int buffSize) {
        this (in, out, data, false, buffSize);
    }

    public Console (InputStream in, OutputStream out, ConnectionData data, boolean ssh, int buffSize) {
        super (in, out, data, ssh);
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

    public void registerCommand (Command... commands) {
        if (commandParser != null) {
            commandParser.registerCommand (commands);
        } else {
            throw new NullPointerException ();
        }
    }

    public CommandParser getCommandParser () {
        return commandParser;
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

    public List<String> getHistory () {
        return new ArrayList<> (history);
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

                            Matcher m = PATTERN.matcher (line);
                            if (m.matches ()) {
                                String content = m.group (2);
                                command.setContent (content);
                                if (command.isOptionSupported ()) {
                                    command.parse (TextFormater.parse (content));
                                }
                            }
/*
                            int idx = line.indexOf (' ');
                            if (idx > 0) {
                                String content = line.substring (idx + 1).trim ();
                                command.setContent (content);
                                if (command.isOptionSupported ()) {
                                    command.parse (content);
                                }
                            }
*/
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

            try {
                Thread.sleep (1);
            } catch (InterruptedException e) {
                e.printStackTrace ();
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

    @Override
    public void write (byte[] buff, int offset, int size) throws IOException {
        if (buff == null)
            throw new NullPointerException ();
        if (offset < 0 || (offset + size > buff.length))
            throw new ArrayIndexOutOfBoundsException ();

        for (int i = offset; i < offset + size; i ++) {
            write (buff [i]);
        }
    }

    @Override
    public void print (String message) throws IOException {
        write (message);
    }

    @Override
    public void print (int value) throws IOException {
        write (String.valueOf (value));
    }

    @Override
    public int read (byte[] buff, int offset, int size) throws IOException {
        if (buff == null)
            throw new NullPointerException ();
        if (offset < 0 || (offset + size > buff.length))
            throw new ArrayIndexOutOfBoundsException ();

        int index = 0;
        while (index < size) {
            int ch = read ();
            if (ch == -1)
                return index > 0 ? index : -1;
            buff[index ++] = (byte) ch;
        }
        return -1;
    }

    @Override
    public String readLine () throws IOException {
        return readInput (false);
    }

    public String readPassword () throws IOException {
        return readPassword ("Please input password");
    }

    public String readPassword (String prompt) throws IOException {
        for (int i = 0; i < 3; i ++) {
            write (prompt + ": ");
            String p1 = readInput (true);
            write (prompt + " again: ");
            String p2 = readInput (true);

            if (!Objects.equals (p1, p2)) {
                errorln ("password not matched.");
                println ();
            } else {
                return p1;
            }
        }

        return null;
    }

    public Boolean option (String prompt, boolean defaultValue) throws IOException {
        String expression = defaultValue ? "[Y/n]: " : "[y/N]: ";
        for (int i = 0; i < 3; i ++) {
            write (prompt);
            write (" ");
            write (expression);

            String answer = readInput (false);
            if (StringUtil.isEmpty (answer)) {
                return defaultValue;
            } else if ("y".equalsIgnoreCase (answer) || "yes".equalsIgnoreCase (answer)) {
                return true;
            } else if ("n".equalsIgnoreCase (answer) || "no".equalsIgnoreCase (answer)) {
                return false;
            }
        }

        return null;
    }
}