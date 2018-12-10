package org.dreamwork.telnet.command;

import org.dreamwork.cli.text.Alignment;
import org.dreamwork.cli.text.TextFormater;
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

    @Override
    public void parse (String line) {

    }

    @Override
    public boolean isOptionPresent (String name) {
        return false;
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
            console.write (TextFormater.fill (key, ' ', max, Alignment.Left));
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

    /**
     * 根据输入的文本猜测可能合法的后续输入.
     * <ul>
     * <li>如果猜测无结果，返回 null</li>
     * <li>如果能够确定匹配后续输入，返回一条确切记录</li>
     * <li>如果能够猜测出多条可能的输入，返回一个列表</li>
     * </ul>
     *
     * @param text 输入的文本
     * @return 可能合法的后续输入.
     */
    @Override
    public List<String> guess (String text) {
        return null;
    }

    @Override
    public void showHelp (Console console) throws IOException {

    }
}
