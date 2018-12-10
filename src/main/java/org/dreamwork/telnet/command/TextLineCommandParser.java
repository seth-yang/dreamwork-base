package org.dreamwork.telnet.command;

import org.dreamwork.text.TextParser;
import org.dreamwork.util.StringUtil;

/**
 * Created by seth.yang on 2018/9/27
 */
public class TextLineCommandParser extends TextParser {
    private Command command;

    public TextLineCommandParser (Command cmd, String pattern) {
        if (StringUtil.isEmpty (pattern) || cmd == null) {
            throw new NullPointerException ();
        }

        stream  = pattern.trim ().toCharArray ();
        command = cmd;
    }

    public void parse (String line) {
        int ch;
        for (ch = nextChar (); ch != -1; ch = nextChar ()) {

        }
    }
}
