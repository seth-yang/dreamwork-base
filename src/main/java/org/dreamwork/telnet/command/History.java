package org.dreamwork.telnet.command;

import org.dreamwork.telnet.Console;

import java.io.IOException;

/**
 * Created by seth.yang on 2019/11/14
 */
public class History extends Command {
    public History () {
        super ("history", null, "show history");
    }

    @Override
    public void perform (Console console) throws IOException {
        for (String cmd : console.getHistory ()) {
            console.write ("  ");
            console.println (cmd);
        }
    }
}
