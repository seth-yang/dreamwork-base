package org.dreamwork.telnet.command;

/**
 * Created by seth.yang on 2018/9/20
 */
public class Exit extends Quit {
    public Exit () {
        super ("exit", null, "alias for command 'quit'");
    }
}
