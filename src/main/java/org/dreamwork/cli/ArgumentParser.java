package org.dreamwork.cli;

import com.google.gson.Gson;
import org.dreamwork.cli.text.TextFormatter;
import org.dreamwork.gson.GsonHelper;
import org.dreamwork.util.StringUtil;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.*;

import static org.dreamwork.cli.text.Alignment.Left;

/**
 * Created by seth.yang on 2017/7/10
 */
public class ArgumentParser {
    private static final int MAX_WIDTH = 120;
    private final Set<Argument> defs;
    private Set<Argument> parsedArguments;

    public ArgumentParser (List<Argument> defs) {
        this.defs = new TreeSet<> (defs);
        init ();
    }

    public ArgumentParser (String defs) {
        Gson g = GsonHelper.getGson ();
        List<Argument> list = g.fromJson (defs, Argument.AS_LIST);
        this.defs = new TreeSet<> (list);
        init ();
    }

    public Argument getByShortOption (String opt) {
        return findByShortOption (parsedArguments, opt);
    }

    public Argument getByLongOption (String opt) {
        return findByLongOption (parsedArguments, opt);
    }

    public void showHelp (PrintWriter out) {
        int[] column_width = new int[4];
        for (Argument arg : defs) {
            if (!StringUtil.isEmpty (arg.longOption) && arg.longOption.length () > column_width[2]) {
                column_width [2] = arg.longOption.length ();
            }
        }
        column_width[0]  = 4;
        column_width[1]  = 4;
        column_width[2] += 4;
        column_width[3]  = MAX_WIDTH - column_width[0] - column_width[1] - column_width[2];
        for (Argument arg : defs) {
            String shortOption = arg.shortOption;
            if (!StringUtil.isEmpty (shortOption)) {
                shortOption = "-" + shortOption;
            }
            String longOption = arg.longOption;
            if (!StringUtil.isEmpty (longOption)) {
                longOption = "--" + longOption;
            }
            String m = arg.required ? "<*>" : "";

            out.print (TextFormatter.fill (m, ' ', column_width[0], Left));
            out.print (TextFormatter.fill (shortOption, ' ', column_width[1], Left));
            out.print (TextFormatter.fill (longOption, ' ', column_width[2], Left));
            out.print (TextFormatter.fill (arg.description, ' ', column_width[3], Left));
            out.println ();
            if (arg.values != null) for (ArgumentValue av : arg.values) {
                out.print (TextFormatter.fill (" ", ' ', column_width[0], Left));
                out.print (TextFormatter.fill (" ", ' ', column_width[1], Left));
                out.print (TextFormatter.fill (" ", ' ', column_width[2], Left));
                out.print (TextFormatter.fill ("  " + av.value + " - " + av.desc, ' ', column_width[3], Left));
                out.println ();
            }
        }
    }

    public void showHelp (PrintStream out) {
        showHelp (new PrintWriter (out, true));
    }

    public void showHelp () {
        showHelp (System.out);
        System.exit (-1);
    }

    public List<Argument> parse (String... args) {
        List<Argument> list = new ArrayList<> ();
        for (int i = 0; i < args.length; i ++) {
            String p = args [i];
            if (p.startsWith ("--")) { // long option
                p = p.substring (2);
                int pos = p.indexOf ('=');

                String argName;
                if (pos == -1) { // no value
                    argName = p;
                } else {
                    argName = p.substring (0, pos);
                }
                Argument arg = findByLongOption (defs, argName);
                if (arg != null) {
                    if (pos < 0) {
                        if (arg.requireValue) {
                            throw new IllegalArgumentException ("option --" + argName + " needs value");
                        } else {
                            // the argument does not require a value.
                            // in this case, it just care about the argument is present or not
                            // true if the argument is present, otherwise false
                            arg.value = "true";
                        }
                    } else {
                        arg.value = p.substring (pos + 1);
                    }
                    checkValue (arg, argName);
/*
                    if (arg.values != null) {
                        boolean find = false;
                        for (ArgumentValue av : arg.values) {
                            if (arg.value.equals (av.value)) {
                                find = true;
                                break;
                            }
                        }

                        if (!find) {
                            StringBuilder builder = new StringBuilder ("the value of option --")
                                    .append (argName)
                                    .append (" is not valid. it must be one of: {");
                            int x = 0;
                            for (ArgumentValue av : arg.values) {
                                if (x > 0) builder.append (", ");
                                builder.append ('"').append (av.value).append ('"');
                                x ++;
                            }
                            throw new IllegalArgumentException (builder.toString ());
                        }
                    }
*/

                    list.add (arg);
                }
            } else if (p.startsWith ("-")) { // short option
                p = p.substring (1).trim ();
                if (p.length () == 1) {
                    Argument arg = findByShortOption (defs, p);
                    if (arg != null) {
                        int index = i + 1;
                        if (index >= args.length) {
                            if (arg.requireValue)
                                throw new IllegalArgumentException ("option -" + p + " needs value");
                            else
                                list.add (arg);
                        } else {
                            String e = args[i + 1];
                            if (!e.startsWith ("-")) {
                                arg.value = e.trim ();
                                checkValue (arg, arg.shortOption);
                                list.add (arg);
                                i++;
                            } else {
                                if (arg.requireValue) {
                                    throw new IllegalArgumentException ("option -" + p + " needs value");
                                } else {
                                    list.add (arg);
                                }
                            }
                        }
                    }
                } else if (p.length () > 1) {
                    for (char ch :p.toCharArray ()) {
                        Argument arg = findByShortOption (defs, String.valueOf (ch));
                        if (arg != null) {
                            if (arg.requireValue) {
                                throw new IllegalArgumentException ("option -" + ch + " needs value");
                            }
                            list.add (arg);
                        }
                    }
                }
            }
        }
        parsedArguments = new TreeSet<> (list);
/*
        Function<Argument, String> key = a -> {
            if (!StringUtil.isEmpty (a.shortOption)) return a.shortOption;
            if (!StringUtil.isEmpty (a.longOption)) return a.longOption;
            return "";
        };
        Function<Argument, Argument> value = a -> a;

        Map<String, Argument> standards = defs.stream()
                .filter (a -> a.required)
                .collect (Collectors.toMap (key, value));
        Map<String, Argument> provides = parsedArguments.stream ()
                .filter (a -> a.required)
                .collect (Collectors.toMap (key, value));

        for (Map.Entry<String, Argument> e : standards.entrySet ()) {
            String option = e.getKey ();
            if (!provides.containsKey (option)) {
                System.err.println ("the mandatory option: " + option + " is not provided.");
                showHelp ();
                throw new IllegalArgumentException ();
            }
        }
*/

/*
        int count = 0, p = 0;
        for (Argument a : defs) {
            if (a.required) count ++;
        }
        for (Argument a : parsedArguments) {
            if (a.required) p ++;
        }
        if (count != p) {
            showHelp ();
            throw new IllegalArgumentException ();
        }
*/
        return list;
    }

    public String getValue (String option) {
        Argument arg = findByLongOption (parsedArguments, option);
        return arg == null ? null : arg.value;
    }

    public String getDefaultValue (String option) {
        Argument arg = findByLongOption (defs, option);
        return arg == null ? null : arg.defaultValue;
    }

    public String getValue (char option) {
        Argument arg = findByShortOption (parsedArguments, String.valueOf (option));
        return arg == null ? null : arg.value;
    }

    public String getDefaultValue (char option) {
        Argument arg = findByShortOption (defs, String.valueOf (option));
        return arg == null ? null : arg.defaultValue;
    }

    public boolean isArgPresent (String option) {
        return findByLongOption (parsedArguments, option) != null;
    }

    public boolean isArgPresent (char option) {
        return findByShortOption (parsedArguments, String.valueOf (option)) != null;
    }

    public Collection<Argument> getAllArguments () {
        return new HashSet<> (defs);
    }

    private void init () {
        Argument arg = findByLongOption (defs, "help");
        if (arg == null) {
            arg = findByShortOption (defs, "h");
        }
        if (arg == null) {
            Argument help = new Argument ();
            help.shortOption = "h";
            help.longOption  = "help";
            help.description = "show this help list.";
            defs.add (help);
        }
    }

    private static Argument findByShortOption (Collection<Argument> args, String shortOption) {
        if (StringUtil.isEmpty (shortOption))
            return null;

        for (Argument arg : args) {
            if (shortOption.equals (arg.shortOption))
                return arg;
        }

        return null;
    }

    private static Argument findByLongOption (Collection<Argument> args, String longOption) {
        if (StringUtil.isEmpty (longOption))
            return null;

        for (Argument arg : args) {
            if (longOption.equals (arg.longOption))
                return arg;
        }

        return null;
    }

/*
    public static String printFix (String text, int length, int align) {
        if (StringUtil.isEmpty (text)) {
//            String ret = "";
            char[] buff = new char[length];
            for (int i = 0; i < length; i ++) {
//                ret += ' ';
                buff [i] = ' ';
            }
            return new String (buff);
        }
        if (text.length () > length) {
            return text.substring (0, length - 4) + "... ";
        }
        String left_padding = "", right_padding = "";
        char[] left_buff, right_buff;
        if (align < 0) { // align left
            right_buff = new char[length - text.length ()];
            for (int i = 0; i< length - text.length (); i ++) {
//                right_padding += ' ';
                right_buff [i] = ' ';
            }
            right_padding = new String (right_buff);
        } else if (align == 0) { // align center
            int d = (length - text.length ()) / 2;
            left_buff = new char[d];
            right_buff = new char[length - d - text.length ()];
            for(int i = 0; i < d; i ++) {
//                left_padding += " ";
                left_buff [i] = ' ';
            }
            left_padding = new String (left_buff);
            for (int i = 0; i < length - d - text.length (); i ++) {
//                right_padding += ' ';
                right_buff [i] = ' ';
            }
            right_padding = new String (right_buff);
        } else if (align > 0) { // align right
            left_buff = new char[length - text.length ()];
            for (int i = 0; i < length - text.length () ; i++) {
//                left_padding += ' ';
                left_buff [i] = ' ';
            }
            left_padding = new String (left_buff);
        }
        return left_padding + text + right_padding;
    }
*/

    private void checkValue (Argument arg, String argName) {
        if (arg.values != null) {
            boolean find = false;
            for (ArgumentValue av : arg.values) {
                if (arg.value.equals (av.value)) {
                    find = true;
                    break;
                }
            }

            if (!find) {
                StringBuilder builder = new StringBuilder ("the value of option --")
                        .append (argName)
                        .append (" is not valid. it must be one of: {");
                int x = 0;
                for (ArgumentValue av : arg.values) {
                    if (x > 0) builder.append (", ");
                    builder.append ('"').append (av.value).append ('"');
                    x ++;
                }
                builder.append ("}");
                throw new IllegalArgumentException (builder.toString ());
            }
        }
    }
}