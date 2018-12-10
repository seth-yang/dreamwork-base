package org.dreamwork.telnet.command;

/**
 * Created by seth.yang on 2018/9/27
 */
public abstract class TextLineCommand extends Command {
    protected TextLineCommandParser parser;

    public TextLineCommand (String name, String alias, String desc) {
        super (name, alias, desc);
    }

    @Override
    public boolean isOptionPresent (String name) {
        return false;
    }
}
