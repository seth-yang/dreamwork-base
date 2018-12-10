package org.dreamwork.telnet.command;

import org.dreamwork.telnet.Console;
import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by seth.yang on 2018/9/30
 */
public class Echo extends Command {
    private String pattern;
    private static final Pattern P1 = Pattern.compile ("^\\$([a-zA-Z_][a-zA-Z_0-9]*)$");
    private static final Pattern P2 = Pattern.compile ("^\\$\\{(.*?)}$");

    public Echo () {
        super ("echo", null, "shows something");
    }

    @Override
    public void parse (String line) {
        this.pattern = line;
    }

    @Override
    public boolean isOptionPresent (String name) {
        return false;
    }

    /**
     * 执行命令
     *
     * @param console 当前控制台
     * @throws IOException io exception
     */
    @Override
    public void perform (Console console) throws IOException {
        if (StringUtil.isEmpty (pattern)) {
            return;
        }

        pattern = pattern.trim ();
        Matcher m = P2.matcher (pattern);
        String name = null;
        if (m.matches ()) {
            name  = m.group (1);
        }

        if (name == null) {
            m = P1.matcher (pattern);
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
        char[] data = pattern.toCharArray ();
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

    /**
     * 根据输入的文本猜测可能合法的后续输入.
     * <ul>
     * <li>如果猜测无结果，返回 null</li>
     * <li>如果能够确定匹配后续输入，返回一条确切记录</li>
     * <li>如果能够猜测出多条可能的输入，返回一个列表</li>
     * </ul>
     *
     * @param text 输入的文本
     * @return 可能合法的后续输入.
     */
    @Override
    public List<String> guess (String text) {
        return null;
    }

    @Override
    public void showHelp (Console console) {

    }
}
