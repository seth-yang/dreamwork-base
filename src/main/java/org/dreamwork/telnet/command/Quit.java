package org.dreamwork.telnet.command;

import org.dreamwork.telnet.Console;

import java.io.IOException;
import java.util.List;

/**
 * Created by seth.yang on 2018/9/20
 */
public class Quit extends Command {
    public Quit () {
        super ("quit", "q", "exit");
    }

    public Quit (String name, String alias, String desc) {
        super (name, alias, desc);
    }

    /**
     * 执行命令
     *
     * @param console 当前控制台
     */
    @Override
    public void perform (Console console) {
    }
}