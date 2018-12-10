package org.dreamwork.telnet.command;

import org.dreamwork.cli.text.Alignment;
import org.dreamwork.cli.text.TextFormater;
import org.dreamwork.telnet.*;
import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by seth.yang on 2018/9/20
 */
public class Help extends Command {
    private CommandParser commandParser;
    private Command target;

    public Help (CommandParser commandParser) {
        super ("help", "h", "show this help list");
        this.commandParser = commandParser;
        if (commandParser == null) {
            throw new NullPointerException ();
        }
    }

    /**
     * 执行命令
     *
     * @param console 当前控制台
     * @throws IOException io exception
     */
    @Override
    public void perform (Console console) throws IOException {
        if (target == null) {
            showHelp (console);
        } else {
            showCommandHelp (console, target);
            target = null;
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
        if (!StringUtil.isEmpty (text)) {
            text = text.trim ();
            if (text.startsWith ("help")) {
                text = text.substring (4).trim ();
            } else if (text.startsWith ("h") || text.startsWith ("?")) {
                text = text.substring (1).trim ();
            }
        }

        List<Command> list = commandParser.getValidCommands ();
        List<String> ret  = new ArrayList<> ();
        if (StringUtil.isEmpty (text)) {
            for (Command cmd : list) {
                if (cmd != this) {
                    ret.add (cmd.name);
                }
            }
        } else {
            for (Command cmd : list) {
                if (cmd != this) {
                    if (cmd.name.startsWith (text) || (!StringUtil.isEmpty (cmd.alias) && cmd.alias.startsWith (text))) {
                        ret.add (cmd.name);
                    }
                }
            }
        }
        return ret;
    }

    @Override
    public void parse (String line) {
        target = commandParser.getByName (line);
        if (target == null) {
            target = commandParser.getByAlias (alias);
        }

        if (target == this) {
            target = null;
        }
    }

    @Override
    public boolean isOptionPresent (String name) {
        return false;
    }

    public void showHelp (Console console) throws IOException {
        int columns = console.getColumns ();
        List<Command> list = commandParser.getValidCommands ();

        int[] fields = new int[3];
        for (Command cmd : list) {
            String name  = cmd.name;
            String alias = cmd.alias;

            if (!StringUtil.isEmpty (name)) {
                name = name.trim ();
                if (name.length () > fields [0]) {
                    fields [0] = name.length ();
                }
            }

            if (!StringUtil.isEmpty (alias)) {
                alias = this.alias.trim ();
                if (alias.length () > fields [1]) {
                    fields [1] = alias.length ();
                }
            }
        }

        fields [0] += 4;
        fields [1] += 4;
        fields [2]  = columns - fields [0] - fields [1];
        int skip    = fields [0] + fields [1];
        for (Command cmd : list) {
            String name  = cmd.name;
            String alias = cmd.alias;
            String desc  = cmd.desc;

            if (StringUtil.isEmpty (name)) {
                name = "";
            }
            name = TextFormater.fill (name.trim (), ' ', fields [0], Alignment.Left);
            console.write (name);

            if (StringUtil.isEmpty (alias)) {
                alias = "";
            }
            alias = TextFormater.fill (alias.trim (), ' ', fields[1], Alignment.Left);
            console.write (alias);

            if (!StringUtil.isEmpty (desc)) {
                desc = desc.trim ();
                if (desc.length () <= fields [2]) {
                    console.println (desc);
                } else {
                    while (desc.length () > fields [2]) {
                        String temp = desc.substring (0, fields [2]);
                        console.println (temp);

                        desc = desc.substring (fields [2]);
                        console.moveCursor (TerminalIO.RIGHT, skip);
                    }

                    if (desc.length () > 0) {
                        console.println (desc);
                    }
                }
            } else {
                console.println ();
            }
        }
    }

    private void showCommandHelp (Console console, Command command) throws IOException {
        console.write (command.name);
        console.write ("\t");
        if (!StringUtil.isEmpty (command.alias)) {
            console.write (command.alias);
            console.write ("\t");
        }
        if (!StringUtil.isEmpty (command.desc)) {
            console.write (command.desc);
        }
        console.println ();
        command.showHelp (console);
    }
}
