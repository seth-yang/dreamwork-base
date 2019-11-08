package org.dreamwork.persistence.test;

import org.dreamwork.telnet.Console;
import org.dreamwork.telnet.TerminalIO;
import org.dreamwork.telnet.command.Option;
import org.dreamwork.telnet.command.TextLineCommand;
import org.dreamwork.text.TextParser;
import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.util.*;

/**
 * Created by seth.yang on 2018/9/27.
 *
 * create {device|task} [options]
 *        target         options
 */
public class CreateCommand extends TextLineCommand {
    private String target;
    private CreateCommandParser parser;

    private static final class CreateCommandParser extends TextParser {
        private CreateCommand command;
        private Map<String, Option> byLongName  = new HashMap<> (),
                                    byShortName = new HashMap<> ();
        private Set<Option> options             = new HashSet<> ();

        private void add (Option option) {
            if (!StringUtil.isEmpty (option.longName)) {
                byLongName.put (option.longName, option);
            }
            if (!StringUtil.isEmpty (option.shortName)) {
                byShortName.put (option.shortName, option);
            }
        }

        private Set<Option> parse (String line) {
            options.clear ();
            if (StringUtil.isEmpty (line)) {
                return null;
            }

            line = line.trim ();
            if (line.startsWith ("create ")) {
                line = line.substring ("create ".length ()).trim ();
            }

            char[] stream = line.toCharArray ();
            char[] buff   = new char [stream.length];
            int pos       = 0;      // 全局指针
            int idx       = 0;      // 当前指针
            int quota     = -1;     // 当前引号，-1为不是引号模式
            Option opt    = null;   // 当前是否处在选项模式

            for (char ch : stream) {
                switch (ch) {
                    case '-':
                        buff [idx ++] = ch;
                        break;
                    case ' ':
                    case '\t': // 分隔符
                        if (quota != -1) {
                            // 引号内，继续缓存数据
                            buff [idx ++] = ch;
                        } else {
                            // 一组结束，开始解析
                            if (buff [0] == '-' && buff [1] == '-') {
                                // long option
                                for (int i = 0; i < idx; i ++) {
                                    if (buff[i] == '=') {
                                        String part = new String (buff, 2, i - 2);
                                        String val  = new String (buff, i + 1, idx - i - 1);
                                        Option o = byLongName.get (part);
                                        if (o != null) {
                                            opt = o.copy ();
                                            opt.value = val;
                                            options.add (opt);
                                            opt = null;
                                        }
                                        break;
                                    }
                                }
                            } else if (buff [0] == '-') {
                                // short option
                                String part = new String (buff, 1, idx - 1);
                                opt = byShortName.get (part);
                            } else {
                                // normal part
                                if (opt != null) {
                                    // last part is short option
                                    opt.value = new String (buff, 0, idx);
                                    options.add (opt);
                                    opt = null;
                                } else {
                                    // target
                                    command.target = new String (buff, 0, idx);
                                }
                            }

                            idx = 0;
                        }
                        break;
                    case '"':
                    case '\'':  // 引号
                        if (quota == ch) {
                            // 在引号模式内
                            if (buff [idx - 1] == '\\') {
                                // 转意引号
                                buff [idx - 1] = ch;
                            } else {
                                quota = -1;
                            }
                        } else {
                            quota = ch;
                        }
                        break;
                    default:
                        buff [idx ++] = ch;
                        break;
                }
            }

            if (idx != 0) {
                String value = new String (buff, 0, idx);
                if (opt != null) {
                    opt.value = value;
                    options.add (opt);
                } else {
                    if (buff [0] == '-' && buff [1] == '-') {
                        // long option
                        for (int i = 0; i < idx; i ++) {
                            if (buff [i] == '=') {
                                String longName = new String (buff, 2, i - 2);
                                opt = byLongName.get (longName);
                                if (opt != null) {
                                    opt.value = new String (buff, i + 1, idx - i - 1);
                                    options.add (opt);
                                }
                                break;
                            }
                        }
                    } else if(StringUtil.isEmpty (command.target)) {
                        command.target = value;
                    } else {
                        command.target = null;
                    }
                }
            }

            return options;
        }
    }
    /*
     * create {device|task} [options]
     *        target         options
     *
     * create device [-i --imei] [-h --host] [-p --port] [-P --protocol]
     * create task [-d --device imei][-r --repeat times][-D --data hex]
     */
    public CreateCommand () {
        super ("create", null, "create a new object.");
        parser = new CreateCommandParser ();
        parser.add (new Option ("i", "imei", null, true, true));
        parser.add (new Option ("h", "host", null, true, true));
        parser.add (new Option ("p", "port", "5683", false, true));
        parser.add (new Option ("P", "protocol", "coap", false, true));

        parser.add (new Option ("d", "device", null, true, true));
        parser.add (new Option ("r", "repeat", "1", false, true));
        parser.add (new Option ("D", "data", null, true, true));
    }

    @Override
    public void setContent (String content) {
        parser.command = this;
        parser.parse (content);
    }

    /**
     * 执行命令
     *
     * @param console 当前控制台
     * @throws IOException io exception
     */
    @Override
    public void perform (Console console) throws IOException {
        if ("device".equals (target)) {
            createDevice (console);
        } else if ("task".equals (target)) {
            createTask (console);
        } else {
            console.errorln ("Invalid command parameters");
            showHelp (console);
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
        if (text.startsWith ("create")) {
            text = text.substring ("create".length ()).trim ();
        }
        List<String> list = new ArrayList<> ();
        int pos = text.indexOf (' ');
/*
        if (pos > 0) {
            target = text.substring (0, pos);
            if ("device".equals (target)) {

            } else if ("task".equals (target)) {

            }
        } else {
*/
        if (pos < 0 && !StringUtil.isEmpty (text)) {
            if ("device".startsWith (text)) {
                list.add ("device");
            } else if ("task".startsWith (text)) {
                list.add ("task");
            }
        }
        return list;
    }

    private void createDevice (Console console) throws IOException {
        Map<String, Map<String, Option>> map = groupOptions (parser.options);
        Option imei = findOption ("i", "imei", map);
        if (imei == null) {
            console.errorln ("option \"imei\" is missing");
            showDeviceHelp (console);
            return;
        }

        if (StringUtil.isEmpty (imei.value)) {
            console.errorln ("option \"imei\" required a value");
            showDeviceHelp (console);
            return;
        }

        Option host = findOption ("h", "host", map);
        if (host == null) {
            console.errorln ("option \"host\" is missing");
            showDeviceHelp (console);
            return;
        }

        if (StringUtil.isEmpty (host.value)) {
            console.errorln ("option \"host\" required a value");
            showDeviceHelp (console);
            return;
        }

        Option port = findOption ("p", "port", map);
        if (port == null) {
            port = parser.byShortName.get ("p").copy ();
        }
        if (StringUtil.isEmpty (port.value)) {
            port.value = port.defaultValue;
        }

        int host_port;
        try {
            host_port = Integer.parseInt (port.value);
            if (host_port < 0 || host_port > 65535) {
                throw new RuntimeException ();
            }
        } catch (Exception ex) {
            console.errorln ("\"port\" must be an integer between 1 and 65535");
            return;
        }

        Option protocol = findOption ("P", "protocol", map);
        if (protocol == null) {
            protocol = parser.byShortName.get ("P").copy ();
        }
        if (StringUtil.isEmpty (protocol.value)) {
            protocol.value = protocol.defaultValue;
        }
        if (!"coap".equalsIgnoreCase (protocol.value) &&
            !"udp".equalsIgnoreCase (protocol.value) &&
            !"lwm2m".equalsIgnoreCase (protocol.value)) {
            console.errorln ("unsupported protocol: " + protocol.value);
            return;
        }

        console.println ("you're create a device with parameters: {");
        console.println ("    imei: " + imei.value);
        console.println ("    host: " + host.value);
        console.println ("    port: " + host_port);
        console.println ("protocol: " + protocol.value);
        console.println ("}");
    }

    private void createTask (Console console) throws IOException {
        Map<String, Map<String, Option>> groups = groupOptions (parser.options);
        Option device = findOption ("d", "device", groups);
        if (device == null) {
            console.errorln ("option \"device\" is missing");
            showTaskHelp (console);
            return;
        }

        Option repeat = findOption ("r", "repeat", groups);
        if (repeat == null) {
            repeat = parser.byShortName.get ("r").copy ();
        }
        if (StringUtil.isEmpty (repeat.value)) {
            repeat.value = repeat.defaultValue;
        }
        int repeat_times;
        try {
            repeat_times = Integer.parseInt (repeat.value);
        } catch (Exception ex) {
            console.errorln ("repeat must be an integer");
            showTaskHelp (console);
            return;
        }

        Option data = findOption ("D", "data", groups);
        if (data == null) {
            console.errorln ("option \"data\" is missing");
            showTaskHelp (console);
            return;
        }
        if (StringUtil.isEmpty (data.value)) {
            console.errorln ("option \"data\" require value");
            return;
        }

        console.println ("you're creating a task {");
        console.println ("  device: " + device.value);
        console.println ("  repeat: " + repeat_times);
        console.println ("    data: " + data.value);
        console.println ("}");
//        create task [-d --device imei][-r --repeat times][-D --data hex]
    }

    private Map<String, Map<String, Option>> groupOptions (Set<Option> options) {
        Map<String, Option> byLongName = new HashMap<> (options.size ());
        Map<String, Option> byShortName = new HashMap<> (options.size ());
        for (Option o : options) {
            if (!StringUtil.isEmpty (o.shortName)) {
                byShortName.put (o.shortName, o);
            }
            if (!StringUtil.isEmpty (o.longName)) {
                byLongName.put (o.longName, o);
            }
        }
        Map<String, Map<String, Option>> map = new HashMap<> (2);
        map.put ("long", byLongName);
        map.put ("short", byShortName);
        return map;
    }

    private Option findOption (String shortName, String longName, Map<String, Map<String, Option>> groups) {
        Map<String, Option> byLongName = groups.get ("long");
        Map<String, Option> byShortName = groups.get ("short");
        if (byShortName.containsKey (shortName)) {
            return byShortName.get (shortName);
        }
        if (byLongName.containsKey (longName)) {
            return byLongName.get (longName);
        }
        return null;
    }

    public void showHelp (Console console) throws IOException {
        console.setForegroundColor (TerminalIO.YELLOW);
        console.println ("create {device|task} <options> create a new object");
        showDeviceHelp (console);
        console.println ("create task       create a new task");
        showTaskHelp (console);
        console.setForegroundColor (TerminalIO.COLORINIT);
    }

    private void showDeviceHelp (Console console) throws IOException {
        console.println ("create device     create a new device");
        console.println ("       -i --imei=<IMEI>       device's imei");
        console.println ("       -h --host=<HOST_IP>    remote server's ip address.");
        console.println ("       -p --port=<PORT>       remote server's port. 0~65535");
        console.println ("       -P --protocol=<PRO>    protocol, CoAP, UDP, LWM2M");
    }

    private void showTaskHelp (Console console) throws IOException {
        console.println ("       -d --device=<IMEI>     refer to an exist device.");
        console.println ("       -r --repeat=<TIMES>    repeat times, default 1");
        console.println ("       -D --data=<HEX_STRING> the data what will be sent.");
    }
}