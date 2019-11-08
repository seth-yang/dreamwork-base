package org.dreamwork.telnet.command;

import org.dreamwork.telnet.Console;
import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by seth.yang on 2018/9/30
 */
public class Echo extends Command {
    private static final Pattern P1 = Pattern.compile ("^\\$([a-zA-Z_][a-zA-Z_0-9]*)$");
    private static final Pattern P2 = Pattern.compile ("^\\$\\{(.*?)}$");

    public Echo () {
        super ("echo", null, "shows something");
    }

    /**
     * 执行命令
     *
     * @param console 当前控制台
     * @throws IOException io exception
     */
    @Override
    public void perform (Console console) throws IOException {
        if (StringUtil.isEmpty (content)) {
            return;
        }

        content = content.trim ();
        Matcher m = P2.matcher (content);
        String name = null;
        if (m.matches ()) {
            name  = m.group (1);
        }

        if (name == null) {
            m = P1.matcher (content);
            if (m.matches ()) {
                name = m.group (1);
            }
        }

        if (!StringUtil.isEmpty (name)) {
            String value = console.getEnv (name);
            if (!StringUtil.isEmpty (value)) {
                console.println (value.trim ());
            } else {
                console.println ();
            }

            return;
        }

        char[] buff = new char[1024];
        char[] data = content.toCharArray ();
        int pos     = 0;
        for (int i = 0; i < data.length; i ++) {
            char ch = data [i];
            switch (ch) {
                case '\\':
                    ch = data [++ i];
                    if (ch != '\\' && ch != '$' && ch != '\'' && ch != '"') {
                        console.errorln ("invalid escape char, only \\$'\" allows.");
                        return;
                    } else {
                        buff [pos ++] = ch;
                    }
                    break;
                case '$':
                    ch = data [++ i];
                    char[] tmp = new char[64];
                    int idx = 0;
                    int terminal = -1;
                    if (ch == '{') {
                        terminal = '}';
                        i ++;
                    }
                    for (; i < data.length; i ++) {
                        ch = data [i];
                        if (terminal == '}') {
                            if (ch != '}') {
                                tmp [idx ++] = ch;
                            } else {
                                break;
                            }
                        } else {
                            if ((ch >= '0' && ch <= '9') ||
                                (ch >= 'a' && ch <= 'z') ||
                                (ch >= 'A' && ch <= 'Z') ||
                                (ch == '_')) {
                                tmp [idx ++] = ch;
                            } else {
                                break;
                            }
                        }
                    }
                    String word = new String (tmp, 0, idx);
                    String value = console.getEnv (word);
                    if (!StringUtil.isEmpty (value)) {
                        value = value.trim ();
                        for (char c : value.toCharArray ()) {
                            buff [pos ++] = c;
                        }
                    }
                    if (terminal != '}' && i != data.length) {
                        buff [pos ++] = ch;
                    }
                    break;
                default:
                    buff [pos ++] = ch;
                    break;
            }
        }

        console.println (new String (buff, 0, pos));
    }
}
