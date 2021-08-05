package org.dreamwork.telnet.command;

import org.dreamwork.util.StringUtil;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by seth.yang on 2018/9/20
 */
public abstract class Command implements ICommand, Serializable {
    public final String alias;
    public final String name;
    public final String desc;

    protected String content;

    public ThreadLocal<Session> threadLocal = new ThreadLocal<> ();

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

    protected Session getSession () {
        return threadLocal.get ();
    }

    public void setSession (Session session) {
        threadLocal.set (session);
    }

    public void clearSession () {
        threadLocal.remove ();
    }

    @Override
    public int hashCode () {
        return name.hashCode ();
    }

    @Override
    public boolean equals (Object o) {
        if (this == o) return true;
        if (o == null || getClass () != o.getClass ()) return false;
        Command command = (Command) o;
        return name != null && name.equals (command.name);
    }
}