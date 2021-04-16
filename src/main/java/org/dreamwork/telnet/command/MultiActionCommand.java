package org.dreamwork.telnet.command;

import org.dreamwork.telnet.Console;
import org.dreamwork.util.StringUtil;

import java.io.IOException;

public abstract class MultiActionCommand extends Command {
    protected String action;
    protected String errorMessage;

    public MultiActionCommand (String name, String alias, String desc) {
        super (name, alias, desc);
    }

    @Override
    public boolean isOptionSupported () {
        return true;
    }

    @Override
    public void parse (String... options) {
        // nat enable nat1
        // net add [-n] nat1 --address=address ...
        // nat set [-n] nat1 -enable
        // -h | --help
        // -s [value]
        // --long-option[=value]


    }

    /**
     * 执行命令
     *
     * @param console 当前控制台
     * @throws IOException io exception
     */
    @Override
    public void perform (Console console) throws IOException {
        if (!StringUtil.isEmpty (errorMessage)) {
            console.errorln (errorMessage);
            return;
        }

        if (StringUtil.isEmpty (action)) {
            action = getDefaultAction ();
        }
        if (!isActionValid (action)) {
            console.errorln ("invalid action: " + action);
            return;
        }

        if ("help".equalsIgnoreCase (action)) {
            showHelp (console);
        } else {
            execute (console);
        }
    }

    protected abstract String getDefaultAction ();

    protected abstract boolean isActionValid (String action);

    protected abstract void execute (Console console) throws IOException;
}
