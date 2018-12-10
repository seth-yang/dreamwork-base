package org.dreamwork.telnet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: seth.yang
 * Date: 11-11-10
 * Time: 下午4:01
 */
public class Shell {
    private TerminalIO io;

    public static final int STYLE_BOLD = 0x01;
    public static final int STYLE_ITALIC = 0x02;
    public static final int STYLE_UNDERLINE = 0x04;
    public static final int STYLE_REVERSE = 0X08;

    public Shell (InputStream in, OutputStream out) {
        ConnectionData connectionData = new ConnectionData ();
        connectionData.setNegotiatedTerminalType ("vt100");
        io = new TerminalIO (in, out, connectionData);
    }

    public void println () throws IOException {
        io.println ();
        io.flush ();
    }

    public void println (String message) throws IOException {
        io.println (message);
        io.flush ();
    }

    public void print (String message) throws IOException {
        io.write (message);
        io.flush ();
    }

    public void print (byte b) throws IOException {
        io.write (b);
        io.flush ();
    }

    public void print (char c) throws IOException {
        io.write (c);
        io.flush ();
    }

    public void error (String message) throws IOException {
        io.error (message);
        io.flush ();
    }

    public void errorln (String message) throws IOException {
        io.errorln (message);
        io.flush ();
    }

    public void clear () throws IOException {
        io.eraseScreen ();
        io.flush ();
    }

    public void home () throws IOException {
        io.homeCursor ();
        io.flush ();
    }

/*
    public String readInput (boolean mark) throws IOException {
        int in = io.read ();
        StringBuffer strBuf = new StringBuffer ();
        while (in != TerminalIO.ENTER) {
            if (in == TerminalIO.DELETE || in == TerminalIO.BACKSPACE) {
                if (strBuf.length () > 0) {
                    io.backspace ();
                    strBuf.deleteCharAt (strBuf.length () - 1);
                }
            } else {
                if (!mark) {
                    io.write ((byte) in);
                } else {
                    io.write ("*");
                }

                strBuf.append ((char) in);
            }
            in = io.read ();
        }
        io.write (TerminalIO.CRLF);
        return strBuf.toString ();
    }
*/

/*
    private void backspace () throws IOException {
        io.write (new String (new byte[]{27, '[', '1', 'D'}));//光标左移一位
        io.write (new String (new byte[]{27, '[', 'K'}));//删除光标到行尾部分的内容
        io.flush ();
    }
*/

/*
    public char confirmChangePassword () throws IOException {
        String message = ResourceManager.getString ("sass2-server-resources.tip.confirm.change_pwd");
        print (message);

        while (true) {
            String input = readInput (false);
            System.out.println ("input = " + input);
            if (isValidInput (input)) return input.charAt (0);
            String errorMessage = ResourceManager.getString ("sass2-server-resources.err.choose");
            errorln (errorMessage);
            println ();
            print (message);
        }
    }

    public boolean changePassword (String userId, String userName) throws IOException {
        boolean inputing = true;
        println (ResourceManager.getStringPattern ("sass2-server-resources.tip.change_password.title", userName));
        int times = ResourceUtil.getInt ("LOGIN_RETRY_MAX_TIMES", 3);
        for (int i = 0; i < times && inputing; i ++) {
            print (ResourceManager.getString ("sass2-server-resources.tip.change_password.current"));
            String current = readInput (true);
            print (ResourceManager.getString ("sass2-server-resources.tip.change_password.new_pwd"));
            String new_pwd = readInput (true);
            print (ResourceManager.getString ("sass2-server-resources.tip.change_password.con_pwd"));
            String con_pwd = readInput (true);
            println ();

            if (new_pwd.equals (con_pwd)) {
                try {
                    AuthCheckUtil.changePasswd(userId, current, new_pwd);
                    println (ResourceManager.getString ("sass2-server-resources.tip.change_password.success"));
                    println (ResourceManager.getString ("sass2-server-resources.tip.change_password.return"));
                    readInput (false);
                    return true;
                } catch (SassError error) {
                    int code = error.getErrorCode ();
                    String em = ErrorManager.getErrorMessage (code);
                    errorln (em);
                    println ();
                }
            }
        }

        errorln (ResourceManager.getString ("sass2-server-resources.err.cpwd.overflow"));
        println (ResourceManager.getString ("sass2-server-resources.tip.auth.exit"));
        readInput (false);

        return false;
    }

    private static boolean isValidInput (String input) {
        if (input == null) return false;
        input = input.trim ();
        return "yes".equalsIgnoreCase (input) || "y".equalsIgnoreCase (input) ||
                "no".equalsIgnoreCase (input) || "n".equalsIgnoreCase (input);
    }
*/

}