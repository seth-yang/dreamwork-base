package org.dreamwork.telnet.command;

import org.dreamwork.telnet.Console;
import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.util.List;

/**
 * Created by seth.yang on 2018/9/20
 */
public abstract class Command {
    public final String alias;
    public final String name;
    public final String desc;
//    public final ArgumentParser parser;

    private int argumentCount;

    public Command (String name, String alias, String desc/*, String def*/) {
        if (StringUtil.isEmpty (name)) {
            throw new NullPointerException ("the name would not be null!");
        }
        this.name  = name;
        this.alias = alias;
        this.desc  = desc;
/*
        if (!StringUtil.isEmpty (def))
            this.parser = new ArgumentParser (def);
        else
            this.parser = null;
*/
    }

    /*
     *
     * 通过 json 定义及文本来构建命令
     * @param name 命令名称
     * @param def  命令定义
     * @param line 文本
     */
/*
    public Command (String name, String alias, String desc, String def, String line) {
        this (name, alias, desc, def);
        parse (line);
    }
*/

    public abstract void parse (String line);
/*
    {
        if (parser != null) {
            String[] args = line.split ("\\s+");
            List<Argument> list = parser.parse (args);
            argumentCount = (list != null) ? list.size () : 0;
        }
    }
*/

    public boolean hasOptions () {
        return argumentCount > 0;
    }

    public abstract boolean isOptionPresent (String name);
/*
    {
        if (StringUtil.isEmpty (name)) {
            throw new NullPointerException ();
        }

        name = name.trim ();
        if (name.length () == 1) {
            return parser.isArgPresent (name.charAt (0));
        } else {
            return parser.isArgPresent (name);
        }
    }
*/

    /**
     * 执行命令
     * @param console   当前控制台
     * @throws IOException io exception
     */
    public abstract void perform (Console console) throws IOException;

    /**
     * 根据输入的文本猜测可能合法的后续输入.
     * <ul>
     *     <li>如果猜测无结果，返回 null</li>
     *     <li>如果能够确定匹配后续输入，返回一条确切记录</li>
     *     <li>如果能够猜测出多条可能的输入，返回一个列表</li>
     * </ul>
     * @param text 输入的文本
     * @return 可能合法的后续输入.
     */
    public abstract List<String> guess (String text);

    public abstract void showHelp (Console console) throws IOException;
}