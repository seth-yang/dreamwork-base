package org.dreamwork.telnet.command;

import org.dreamwork.telnet.Console;

import java.io.IOException;
import java.util.List;

/**
 * Created by seth.yang on 2018/9/20
 */
public class Quit extends Command {
    public Quit () {
        super ("quit", "q", "exit");
    }

    public Quit (String name, String alias, String desc) {
        super (name, alias, desc);
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
