package org.dreamwork.telnet.command;

import org.dreamwork.telnet.Console;

import java.io.IOException;

/**
 * Created by seth.yang on 2018/9/30
 */
public class Clear extends Command {
    public Clear () {
        super ("clear", null, "clear the screen and home cursor");
    }

    /**
     * 执行命令
     *
     * @param console 当前控制台
     * @throws IOException io exception
     */
    @Override
    public void perform (Console console) throws IOException {
        console.eraseScreen ();
        console.homeCursor ();
    }
}
