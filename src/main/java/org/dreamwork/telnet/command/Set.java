package org.dreamwork.telnet.command;

import org.dreamwork.telnet.Console;
import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by seth.yang on 2018/9/30
 */
public class Set extends Command {
    private String name, value;

    private static final Pattern P = Pattern.compile ("^(set\\s+)?(.*?)=(.*?)$");
    // set name=val
    public Set () {
        super ("set", null, "setting console env");
    }

    @Override
    public void setContent (String content) {
        if (StringUtil.isEmpty (content)) {
            return;
        }

        content = content.trim ();
        Matcher m = P.matcher (content);
        if (m.matches ()) {
            name = m.group (2);
            value = m.group (3);
        }
    }

    /**
     * 执行命令
     *
     * @param console 当前控制台
     */
    @Override
    public void perform (Console console) {
        if (!StringUtil.isEmpty (name) && !StringUtil.isEmpty (value)) {
            console.setEnv (name, value);
        }
    }

    @Override
    public void showHelp (Console console) throws IOException {
        console.println ("set NAME=VALUE");
    }
}
