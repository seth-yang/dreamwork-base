package org.dreamwork.telnet.command;

import org.dreamwork.telnet.Console;
import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.util.List;

/**
 * Created by seth.yang on 2018/9/30
 */
public class Unset extends Command {
    private String name;
    public Unset () {
        super ("unset", null, "unset a console env");
    }

    @Override
    public void setContent (String content) {
        if (StringUtil.isEmpty (content)) {
            return;
        }

        name = content.trim ();
    }

    /**
     * 执行命令
     *
     * @param console 当前控制台
     */
    @Override
    public void perform (Console console) {
        if (!StringUtil.isEmpty (name)) {
            console.setEnv (name, null);
        }
    }

    @Override
    public void showHelp (Console console) throws IOException {
        console.println ("unset NAME");
    }
}
