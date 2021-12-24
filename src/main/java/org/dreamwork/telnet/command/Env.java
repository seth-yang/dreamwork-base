package org.dreamwork.telnet.command;

import org.dreamwork.cli.text.Alignment;
import org.dreamwork.cli.text.TextFormatter;
import org.dreamwork.telnet.Console;
import org.dreamwork.telnet.TerminalIO;
import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.util.*;

/**
 * Created by seth.yang on 2018/9/30
 */
public class Env extends Command {
    public Env () {
        super ("env", null, "show environment");
    }

    /**
     * 执行命令
     *
     * @param console 当前控制台
     * @throws IOException io exception
     */
    @Override
    public void perform (Console console) throws IOException {
        Map<String, String> env = console.getEnvironment ();
        List<String> keys = new ArrayList<> (env.keySet ());
        Collections.sort (keys);
        int max = 0;
        for (String key : keys) {
            if (key.length () > max) {
                max = key.length ();
            }
        }
        max += 4;
        int width = console.getColumns () - max;
        for (String key : keys) {
            String value = env.get (key);
            console.write (TextFormatter.fill (key, ' ', max, Alignment.Left));
            if (StringUtil.isEmpty (value)) {
                console.println ();
                continue;
            }

            value = value.trim ();
            int length = value.length ();
            if (length < width) {
                console.println (value);
            } else {
                while (value.length () > width) {
                    String text = value.substring (0, width);
                    console.println (text);

                    value = value.substring (width + 1);
                    console.moveCursor (TerminalIO.RIGHT, max);
                }

                if (value.length () > 0) {
                    console.println (value);
                }
            }
        }
    }
}
