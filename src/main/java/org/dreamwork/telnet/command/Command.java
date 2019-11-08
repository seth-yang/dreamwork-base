package org.dreamwork.telnet.command;

import org.dreamwork.util.StringUtil;

/**
 * Created by seth.yang on 2018/9/20
 */
public abstract class Command implements ICommand {
    public final String alias;
    public final String name;
    public final String desc;

    protected String content;

    public Command (String name, String alias, String desc/*, String def*/) {
        if (StringUtil.isEmpty (name)) {
            throw new NullPointerException ("the name would not be null!");
        }
        this.name  = name;
        this.alias = alias;
        this.desc  = desc;
    }

    @Override
    public void setContent (String content) {
        this.content = content;
    }
}