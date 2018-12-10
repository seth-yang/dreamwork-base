package org.dreamwork.telnet.command;

import org.dreamwork.util.StringUtil;

import java.util.*;

/**
 * Created by seth.yang on 2018/9/20
 */
public abstract class CommandParser {
    protected Map<String, Command> mappedByName  = new HashMap<> ();
    protected Map<String, Command> mappedByAlias = new HashMap<> ();

    protected static final Comparator<Command> C = new Comparator<Command> () {
        @Override
        public int compare (Command o1, Command o2) {
            return o1.name.compareTo (o2.name);
        }
    };

    /**
     * 将文本解析成合法的 Command 对象
     * @param text  原始文本
     * @return  Command 实例
     */
    public abstract Command parse (String text);

    /**
     * 猜测输入的文本可能的有效的命令列表
     * <ul>
     *     <li>如果猜测无结果，返回 null</li>
     *     <li>如果能够确定匹配后续输入，返回一条确切记录</li>
     *     <li>如果能够猜测出多条可能的输入，返回一个列表</li>
     * </ul>
     * @param text 输入文本
     * @return  可能的命令
     */
    public abstract List<Command> guess (String text);

    /**
     * 获取所有合法的命令集合
     * @return 命令集合
     */
    public List<Command> getValidCommands () {
        Collection<Command> c = mappedByName.values ();
        if (c.isEmpty ()) {
            return null;
        }

        List<Command> list = new ArrayList<> (c);
        Collections.sort (list, C);
        return list;
    }

    public synchronized void registerCommand (Command... commands) {
        if (commands == null) {
            throw new NullPointerException ();
        }

        if (commands.length > 0) {
            for (Command command : commands) {
                if (!StringUtil.isEmpty (command.alias))
                    mappedByAlias.put (command.alias, command);
                if (!StringUtil.isEmpty (command.name))
                    mappedByName.put (command.name, command);
            }
        }
    }

    public synchronized Command getByName (String name) {
        return mappedByName.get (name);
    }

    public synchronized Command getByAlias (String alias) {
        return mappedByAlias.get (alias);
    }
}