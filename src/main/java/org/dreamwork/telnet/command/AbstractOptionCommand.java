package org.dreamwork.telnet.command;

import org.dreamwork.cli.Argument;
import org.dreamwork.cli.text.Alignment;
import org.dreamwork.telnet.Console;
import org.dreamwork.util.IOUtil;
import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.dreamwork.telnet.command.CommandUtilities.*;

/**
 * 提供一个固定模式的，支持 posix 风格的命令.
 * <p>一般形式为<pre>
command [target] [options]</pre>
 *
 */
public abstract class AbstractOptionCommand extends Command {
    /** 用于保存解析过程的错误 */
    protected String errorMessage;
    /** 选项解析器 */
    protected OptionParser parser;

    /** 指示命令的 target 是否必须 */
    protected boolean targetIsMandatory;

    /**
     * 真正的命令处理代码.
     * <p>当解析过程没有错误，并且不存在 {@code help} 选项时，将执行这部分代码</p>
     * @param console 控制台
     * @throws IOException io 异常
     */
    protected abstract void doPerform (Console console) throws IOException;

    /**
     * 构造函数
     * @param name  命令名称
     * @param alias 别名，通常为了简化输入设置的简短别名
     * @param desc  简单的命令解释
     * @param targetIsMandatory 指示命令的目标是否必须
     */
    public AbstractOptionCommand (String name, String alias, String desc, boolean targetIsMandatory) {
        super (name, alias, desc);
        this.targetIsMandatory = targetIsMandatory;
    }

    /**
     * 针对用户在控制台的输入内容进行解析
     * @param options 控制台的输入内容
     */
    @Override
    public void parse (String... options) {
        try {
            parser.parse (targetIsMandatory, options);
        } catch (ParseOptionException ex) {
            errorMessage = ex.getMessage ();
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    /**
     * 是否支持命令行选项
     * @return 对于支持选项的命令，永远为真
     */
    @Override
    public boolean isOptionSupported () {
        return true;
    }

    /**
     * 命令的处理接口
     * <p>覆盖的父类的方法，首先判断在解析过程中是否有错误，若有则显示错误信息；然后判断用户是否指定了
     * {@code help} 选项，若有则显示帮助信息后并退出；最后调用抽象方法 {@link #doPerform(Console)} 进行真正的业务处理</p>
     * @param console   当前控制台
     * @throws IOException io 异常
     */
    @Override
    public void perform (Console console) throws IOException {
        try {
            if (!StringUtil.isEmpty (errorMessage)) {
                console.error (errorMessage);
            } else if (parser.present ("help")) {
                showHelp (console);
            } else {
                doPerform (console);
            }
        } finally {
            reset ();
        }
    }

    /**
     * 显示帮助信息
     * @param console 当前控制台
     * @throws IOException io 异常
     */
    @Override
    public void showHelp (Console console) throws IOException {
        if (parser != null) {
            List<Argument> defs = parser.getDefinitions ();
            if (!defs.isEmpty ()) {
                example (console, "Usage: " + name + " <target> [options].");
                help (console, "The valid options are:");
                List<HelpItem> items = new ArrayList<> (defs.size ());
                defs.forEach (a -> items.add (new HelpItem (a)));
                Matrix matrix = CommandUtilities.matrix (items, null, new String[] {"<empty>", "short", "long", "desc"});
                printTable (console, matrix, new Alignment[] {Alignment.Left, Alignment.Left, Alignment.Left, Alignment.Left}, null);
            }
        } else {
            help (console, desc == null ? "<no help message>" : desc);
        }
    }

    /**
     * 从 {@code json} 中加载命令的配置.
     * <pre>[
     {
         "shortOption": "o",
         "longOption": "option1",
         "requireValue": true,
         "description": "the description of this option"
     },
     {
         "shortOption": "p",
         "longOption": "option2",
         "requireValue": true
     },
     {
         "shortOption": "t",
         "longOption": "option3"
     },
     {
         "shortOption": "h",
         "longOption": "help"
     }
 ]</pre>
     * 其中：<ul>
     *   <li>{@code shortOption} 属性定义短选项名称</li>
     *   <li>{@code longOption} 属性定义长选项名称</li>
     *   <li>{@code requireValue} 属性定义该选项是否必须有值</li>
     * </ul>
     * 对于任意一个选项，{@code shortOption} 和 {@code longOption} 必须至少出现一个。
     * @param json 命令定义json
     */
    protected void loadFromJson (String json) {
        parser = new OptionParser (json);
    }

    /**
     * 从 {@code url} 获取命令定义的 {@code json} 结构，并装载。
     * @param url 命令定义的获取资源路径
     *
     * @see #loadFromJson(String)
     * @throws IOException io 异常
     */
    protected void loadFromJson (URL url) throws IOException {
        try (InputStream in = url.openStream ()) {
            byte[] data = IOUtil.read (in);
            loadFromJson (new String (data, 0, data.length, StandardCharsets.UTF_8));
        }
    }

    /**
     * 复位命令状态
     */
    private void reset () {
        parser.reset ();
        errorMessage = null;
    }

    public static class HelpItem implements IIndexable {
        private final Argument argument;

        HelpItem (Argument argument) { this.argument = argument; }

        @Override
        @SuppressWarnings ("unchecked")
        public <T> T get (String name) {
            if (StringUtil.isEmpty (name)) {
                throw new IllegalArgumentException ("empty name");
            }
            Object value;
            switch (name) {
                case "<empty>":
                    value = "    ";
                    break;

                case "short":
                    value = argument.shortOption == null ? "" : "-" + argument.shortOption;
                    break;

                case "long":
                    if (argument.longOption == null) value = "";
                    else {
                        value = "--" + argument.longOption;
                        if (argument.requireValue) {
                            value += "=<value>";
                        }
                    }
                    break;

                case "desc":
                    value = argument.description;
                    break;

                default:
                    throw new IllegalArgumentException ("unsupported key: " + name);
            }

            return (T) value;
        }
    }
}