package org.dreamwork.telnet.command;

import org.dreamwork.telnet.Console;

import java.io.IOException;
import java.util.List;

/**
 * Created by seth.yang on 2019/11/8
 */
public interface ICommand {
    default void parse (String... options) {
    }

    void setContent (String content);

    /**
     * 命令是否支持选项
     */
    default boolean isOptionSupported () {
        return false;
    }

    /**
     * 执行命令
     * @param console   当前控制台
     * @throws IOException io exception
     */
    void perform (Console console) throws IOException;

    /**
     * 根据输入的文本猜测可能合法的后续输入.
     * <ul>
     *     <li>如果猜测无结果，且不希望改变当前输入，返回 null</li>
     *     <li>如果猜测无结果，但当前输入有无效，希望控制台修正为命令本身，返回0元素的列表</li>
     *     <li>如果能够确定匹配后续输入，返回一条确切记录</li>
     *     <li>如果能够猜测出多条可能的输入，返回一个列表</li>
     * </ul>
     * @param text 输入的文本
     * @return 可能合法的后续输入.
     */
    default List<String> guess (String text) {
        return null;
    }

    default void showHelp (Console console) throws IOException {
    }
}
