package org.dreamwork.telnet;

import org.dreamwork.telnet.command.*;
import org.dreamwork.util.StringUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth.yang on 2018/9/20
 */
public class SimpleCommandParser extends CommandParser {
    public SimpleCommandParser (boolean includeBaseCommands) {
        if (includeBaseCommands) {
            registerCommand (BASE_COMMANDS);
            registerCommand (new Help (this));
        }
    }
    /**
     * 将文本解析成合法的 Command 对象
     *
     * @param text 原始文本
     * @return Command 实例
     */
    @Override
    public Command parse (String text) {
        if (StringUtil.isEmpty (text)) {
            return null;
        }

        if (mappedByAlias.isEmpty () && mappedByName.isEmpty ()) {
            return null;
        }

        text = text.trim ();
        int pos = text.indexOf (' ');

        String part/*, line = null*/;
        if (pos > 0) {
            part = text.substring (0, pos);
//            line = text.substring (pos + 1);
        } else {
            part = text;
        }

        Command cmd = null;
        if (mappedByName.containsKey (part)) {
            cmd = mappedByName.get (part);
        } else if (mappedByAlias.containsKey (part)){
            cmd = mappedByAlias.get (part);
        }
        return cmd;
    }

    /**
     * 猜测输入的文本可能的有效的命令列表
     *
     * @param text 输入文本
     * @return 可能的命令
     */
    @Override
    public List<Command> guess (String text) {
        if (StringUtil.isEmpty (text))
            return null;

        text = text.trim ();
        int pos = text.indexOf (' ');
        if (pos > 0) { // 已超出一个单词
            text = text.substring (0, pos).trim ();
        }

        List<Command> list = new ArrayList<> ();
        for (String name : mappedByName.keySet ()) {
            if (name.startsWith (text)) {
                list.add (mappedByName.get (name));
            }
        }

        for (String alias : mappedByAlias.keySet ()) {
            if (alias.startsWith (text)) {
                Command cmd = mappedByAlias.get (alias);
                if (!list.contains (cmd)) {
                    list.add (cmd);
                }
            }
        }

        list.sort (C);
        return list;
    }

    public static final Command[] BASE_COMMANDS = {
            new Exit (), new Quit (),
            new Env (),
            new Set (), new Unset (),
            new Echo (), new Clear (),
            new History ()
    };
}