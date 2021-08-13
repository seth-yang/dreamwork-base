package org.dreamwork.telnet.command;

import com.google.gson.Gson;
import org.dreamwork.cli.Argument;
import org.dreamwork.util.StringUtil;

import java.util.*;

/**
 * 命令行选项解析器
 */
public class OptionParser {
    /** 选项定义 */
    private final List<Argument> definition;
    /** 用短选项名索引的选项定义 */
    private final Map<String, Argument> byShort  = new HashMap<> ();
    /** 用长选项名索引的选项定义 */
    private final Map<String, Argument> byLong   = new HashMap<> ();
    /** 解析过的选项 */
    private final java.util.Set<Argument> parsed = new HashSet<> ();

    /** 命令目标 */
    private String target;
    /** 是否严格模式 */
    public final boolean strict;

    /**
     * 解析过的目标
     * @return 解析过的目标
     */
    public String getTarget () { return target; }

    /**
     * 构造函数.
     * <p>使用默认的严格方式构造</p>
     * @param spec 选项规格定义
     */
    public OptionParser (String spec) {
        this (spec, true);
    }

    /**
     * 构造函数
     * @param spec   选项规格定义
     * @param strict 是否使用严格模式
     */
    public OptionParser (String spec, boolean strict) {
        this.strict = strict;
        Gson g = new Gson ();
        definition = g.fromJson (spec, Argument.AS_LIST);
        definition.forEach (a -> {
            if (!StringUtil.isEmpty (a.shortOption)) {
                byShort.put (a.shortOption, a);
            }
            if (!StringUtil.isEmpty (a.longOption)) {
                byLong.put (a.longOption, a);
            }
        });
    }

    /**
     * 解析命令行选项
     * @param needTarget 指示命令目标是否必须
     * @param options 命令行输入的选项
     * @throws ParseOptionException 解析过程中发送的错误
     */
    public void parse (boolean needTarget, String... options) throws ParseOptionException {
        int i = 0;
        while (i < options.length) {
            String part = options[i];
            if (part.startsWith ("--")) {   // 长选项
                int index = part.indexOf ('=');
                String name, value;
                if (index < 0) {
                    // 标志性长选项，无值
                    name = part.substring (2);
                    value = null;
                } else {
                    // 有值长选项
                    name = part.substring (2, index);
                    value = part.substring (index + 1);
                }

                Argument a = byLong.get (name);
                if (a == null) {    // 未定义的选项
                    if (strict) {
                        // 严格模式下，抛出错误
                        throw new ParseOptionException ("unsupported option: --" + name);
                    }
                } else {
                    if (StringUtil.isEmpty (value)) {   // 空值
                        if (a.requireValue) {           // 若定义要求有值，抛出错误
                            throw new ParseOptionException ("option --" + name + " missing value");
                        }
                    } else {
                        a.value = value;
                    }

                    parsed.add (a);
                }
            } else if (part.startsWith ("-")) {
                String name = part.substring (1);
                if (name.length () == 1) {
                    Argument a = byShort.get (name);
                    if (a == null) {
                        throw new ParseOptionException ("unsupported option: -" + name);
                    }
                    if (a.requireValue) {
                        i++;   // 短选项的后一个元素为选项值
                        if (i < options.length) {
                            a.value = options[i];
                        } else {
                            throw new ParseOptionException ("option -" + name + " missing value");
                        }
                    }
                    parsed.add (a);
                } else if (name.length () > 1) {
                    char[] buff = name.toCharArray ();
                    for (char ch : buff) {
                        name = String.valueOf (ch);
                        Argument a = byShort.get (name);
                        if (a == null) {
                            throw new ParseOptionException ("unsupported option: -" + ch);
                        }
                        parsed.add (a);
                    }
                } else {
                    throw new ParseOptionException ("invalid option");
                }
            } else {
                if (StringUtil.isEmpty (target)) {
                    target = part;
                } else {
                    throw new ParseOptionException ("Ambiguous targets: " + target + " and " + part);
                }
            }

            i ++;
        }

        if (needTarget && target == null && !present ("help")) {
            throw new ParseOptionException ("target is missing");
        }

        for (Argument a : parsed) {
            if (a.requireValue && StringUtil.isEmpty (a.value)) {
                throw new ParseOptionException ("option " + a.shortOption + " missing its value");
            }
        }

        definition.forEach (a -> {
            if (!parsed.contains (a) && a.defaultValue != null) {
                a.value = a.defaultValue;
                parsed.add (a);
            }
        });
    }

    /**
     * 查询短选项 {@code option} 是否存在
     * @param option 短选项名称
     * @return 若存在返回 true，否则返回 false
     */
    public boolean present (char option) {
        String key = String.valueOf (option);
        if (byShort.containsKey (key)) {
            for (Argument a : parsed) {
                if (a.shortOption != null && a.shortOption.equals (key)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 查询长选项 {@code option} 是否存在
     * @param option 长选项名称
     * @return 若存在返回 true，否则返回 false
     */
    public boolean present (String option) {
        if (byLong.containsKey (option)) {
            for (Argument a : parsed) {
                if (a.longOption != null && a.longOption.equals (option)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取短选项的值
     * @param option 短选项
     * @return 选项值
     */
    public String get (char option) {
        String key = String.valueOf (option);
        if (byShort.containsKey (key)) {
            for (Argument a : parsed) {
                if (a.shortOption.equals (key)) {
                    return a.value;
                }
            }
        }

        return null;
    }

    /**
     * 获取长选项的值
     * @param option 长选项
     * @return 选项值
     */
    public String get (String option) {
        if (byLong.containsKey (option)) {
            for (Argument a : parsed) {
                if (a.longOption.equals (option)) {
                    return a.value;
                }
            }
        }
        return null;
    }

    public void reset () {
        definition.forEach (a -> a.value = null);
        parsed.clear ();
        target = null;
    }

    public List<Argument> getDefinitions () {
        return Collections.unmodifiableList (definition);
    }
}