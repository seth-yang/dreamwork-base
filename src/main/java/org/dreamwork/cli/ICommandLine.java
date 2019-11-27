package org.dreamwork.cli;

import org.dreamwork.util.StringUtil;

import java.io.IOException;
import java.util.List;

import static org.dreamwork.cli.CommandLineHelper.*;

/**
 * Created by seth.yang on 2018/11/7
 */
public interface ICommandLine {
    String CRLF = "\r\n";

    void error (String message) throws IOException;
    void write (byte[] buff, int offset, int size) throws IOException;
    void print (String message) throws IOException;
    void print (int value) throws IOException;
    int read () throws IOException;
    int read (byte[] buff, int offset, int size) throws IOException;
    String readLine () throws IOException;

    default void write (byte[] buff) throws IOException  {
        write (buff, 0, buff.length);
    }

    default void print (Object message) throws IOException {
        if (message == null) {
            print ("<null>");
        } else {
            print (message.toString ());
        }
    }
    default void println () throws IOException {
        print (CRLF);
    }
    default void println (String message) throws IOException {
        print (message);
        print (CRLF);
    }
    default void println (int value) throws IOException {
        print (value);
        print (CRLF);
    }
    default void println (Object message) throws IOException {
        print (message);
        print (CRLF);
    }
    default void printf (String pattern, Object... args) throws IOException {
        print (String.format (pattern, args));
    }

    default int read (byte[] buff) throws IOException {
        return read (buff, 0, buff.length);
    }

    default String readString (String prompt) throws IOException {
        print (prompt);
        print (": ");
        return readLine ();
    }
    default String readString (String prompt, boolean canEmpty) throws IOException {
        while (true) {
            String line = readString (prompt);
            if (canEmpty || !StringUtil.isEmpty (line)) {
                return line.trim ();
            }
            error ("Can't be empty");
        }
    }
    default String readString (String prompt, String defaultValue) throws IOException {
        String line = readString (prompt + "[" + defaultValue + "]");
        return StringUtil.isEmpty (line) ? defaultValue : line.trim ();
    }
    default String readString (String promp, IValueGenerator<String> g) throws IOException {
        if (g == null) {
            return readString (promp);
        }

        String line = readString (promp + "[random]");
        return StringUtil.isEmpty (line) ? g.generate (null) : line;
    }
    default String readString (String prompt, List<String> options) throws IOException {
        if (options == null || options.isEmpty ()) {
            return readString (prompt);
        }
        showOption (prompt, options, this);
        int index = readInt (
                "Please select an item from from the list above",
                (IValidator<Integer>) pos -> checkOption (pos, prompt, options, ICommandLine.this)
        );
        return options.get (index);
    }
    default String readString (String prompt, IValueGenerator<Integer> g, List<String> options) throws IOException {
        if (g == null) {
            return readString (prompt, options);
        }

        while (true) {
            showOption (prompt + "[random]", options, this);
            String text = readLine ();
            if (StringUtil.isEmpty (text)) {
                while (true) {
                    int index = g.generate (null);
                    if (index >= 0 && index < options.size ()) {
                        return options.get (index);
                    }
                }
            } else {
                try {
                    int index = Integer.parseInt (text.trim ());
                    if (index >= 0 && index < options.size ()) {
                        return options.get (index);
                    } else {
                        error ("Please select an item from the list above, or <empty> for random.");
                    }
                } catch (Exception ex) {
                    error ("Please select an item from the list above, or <empty> for random.");
                }
            }
        }
    }
    default String readString (String prompt, String defaultValue, List<String> options) throws IOException {
        if (options == null || options.isEmpty ()) {
            return readString (prompt, defaultValue);
        }

        int INDEX = options.indexOf (defaultValue);
        if (INDEX < 0) {
            throw new RuntimeException ("default value [" + defaultValue + "] not presents in options!");
        }
        showOption (prompt, options, this);
        int index = readInt (
                "Please select an item from the list above", INDEX,
                pos-> checkOption (pos, prompt, options, this)
        );
        return options.get (index);
    }
    default String readString (String prompt, IValidator<String> validator) throws IOException {
        if (validator == null) {
            return readString (prompt);
        }

        String text;
        do {
            text = readString (prompt);
        } while (!validator.validate (text));
        return text;
    }
    default String readString (String prompt, IComplexValidator<String> validator, String... messages) throws IOException {
        if (validator == null) {
            return readString (prompt);
        }
        while (true) {
            String text = readString (prompt).trim ();
            int code = validator.validate (text);
            if (code >= 0) {
                error (messages [code]);
            } else {
                return text;
            }
        }
    }
    default String readString (String prompt, String defaultValue, IValidator<String> validator) throws IOException {
        if (validator == null) {
            return readString (prompt, defaultValue);
        }

        String text;
        do {
            text = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (text)) {
                return defaultValue;
            }
        } while (!validator.validate (text));
        return text.trim ();
    }
    default String readString (String prompt, String defaultValue, IComplexValidator<String> validator, String... messages) throws IOException {
        if (validator == null) {
            return readString (prompt, defaultValue);
        }

        while (true) {
            String text = readString (prompt + "[" + defaultValue + "]").trim ();
            if (StringUtil.isEmpty (text)) {
                return defaultValue;
            }
            int code = validator.validate (text);
            if (code >= 0) {
                error (messages [code]);
            } else {
                return text;
            }
        }
    }

    default int readInt () throws IOException {
        while (true) {
            try {
                String line = readLine ();
                return Integer.parseInt (line);
            } catch (Exception ex) {
                error ("Invalid integer!");
            }
        }
    }
    default int readInt (String prompt) throws IOException {
        while (true) {
            String line = null;
            try {
                line = readString (prompt);
                return Integer.parseInt (line.trim ());
            } catch (Exception ex) {
                error (line + " is not a valid integer");
            }
        }
    }
    default int readInt (String prompt, int defaultValue) throws IOException {
        while (true) {
            try {
                String line = readString (prompt + "[" + defaultValue + "]");
                if (StringUtil.isEmpty (line)) {
                    return defaultValue;
                }
                return Integer.parseInt (line.trim ());
            } catch (Exception ex) {
                error ("Invalid integer!");
            }
        }
    }
    default int readInt (String prompt, IValidator<Integer> validator) throws IOException {
        if (validator == null) {
            return readInt (prompt);
        }

        String text;
        int value;
        while (true) {
            text = readString (prompt);
            try {
                value = Integer.parseInt (text.trim ());
            } catch (Exception ex) {
                error ("Invalid integer!");
                continue;
            }
            if (validator.validate (value)) {
                return value;
            }
        }
    }
    default int readInt (String prompt, IValueGenerator<Integer> g) throws IOException {
        if (g == null) {
            return readInt (prompt);
        }

        String text;
        while (true) {
            text = readString (prompt + "[random]");
            if (StringUtil.isEmpty (text)) {
                return g.generate (null);
            }

            try {
                return Integer.parseInt (text.trim ());
            } catch (Exception ex) {
                error ("Invalid integer");
            }
        }
    }

    default int readInt (String prompt, IComplexValidator<Integer> validator, String... messages) throws IOException {
        if (validator == null) {
            return readInt (prompt);
        }

        while (true) {
            String text = readString (prompt);
            try {
                int value = Integer.parseInt (text.trim ());
                int code  = validator.validate (value);
                if (code >= 0) {
                    error (messages [code]);
                } else {
                    return value;
                }
            } catch (Exception ex) {
                error ("Invalid integer");
            }
        }
    }

    default int readInt (String prompt, int defaultValue, IValidator<Integer> validator) throws IOException {
        if (validator == null) {
            return readInt (prompt, defaultValue);
        }

        String text;
        int value;
        while (true) {
            text = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (text)) {
                return defaultValue;
            }

            try {
                value = Integer.parseInt (text.trim ());
            } catch (Exception ex) {
                error ("Invalid integer");
                continue;
            }

            if (validator.validate (value)) {
                return value;
            }
        }
    }
    default int readInt (String prompt, int defaultValue, IComplexValidator<Integer> validator, String... messages) throws IOException {
        if (validator == null) {
            return readInt (prompt, defaultValue);
        }

        String text;
        int value;
        while (true) {
            text = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (text)) {
                return defaultValue;
            }

            try {
                value = Integer.parseInt (text.trim ());
            } catch (Exception ex) {
                error ("Invalid integer");
                continue;
            }

            int code = validator.validate (value);
            if (code < 0) {
                return value;
            } else {
                error (messages [code]);
            }
        }
    }

    default long readLong () throws IOException {
        while (true) {
            try {
                String line = readLine ();
                return Long.parseLong (line);
            } catch (Exception ex) {
                error ("Invalid long!");
            }
        }
    }
    default long readLong (String prompt) throws IOException {
        while (true) {
            String line = null;
            try {
                line = readString (prompt);
                return Long.parseLong (line.trim ());
            } catch (Exception ex) {
                error (line + " is not a valid long");
            }
        }
    }
    default long readLong (String prompt, long defaultValue) throws IOException {
        while (true) {
            try {
                String line = readString (prompt + "[" + defaultValue + "]");
                if (StringUtil.isEmpty (line)) {
                    return defaultValue;
                }
                return Long.parseLong (line.trim ());
            } catch (Exception ex) {
                error ("Invalid long!");
            }
        }
    }
    default long readLong (String prompt, IValueGenerator<Long> g) throws IOException {
        if (g == null) {
            return readLong (prompt);
        }

        String text;
        while (true) {
            text = readString (prompt);
            if (StringUtil.isEmpty (text)) {
                return g.generate (null);
            }

            try {
                return Long.parseLong (text.trim ());
            } catch (Exception ex) {
                error ("Invalid long");
            }
        }
    }
    default long readLong (String prompt, IValidator<Long> validator) throws IOException {
        if (validator == null) {
            return readLong (prompt);
        }

        String text;
        long value;
        while (true) {
            text = readString (prompt);
            try {
                value = Long.parseLong (text.trim ());
            } catch (Exception ex) {
                error ("Invalid long");
                continue;
            }
            if (validator.validate (value)) {
                return value;
            }
        }
    }
    default long readLong (String prompt, IComplexValidator<Long> validator, String... messages) throws IOException {
        if (validator == null) {
            return readLong (prompt);
        }

        String text;
        long value;
        while (true) {
            text = readString (prompt);
            try {
                value = Long.parseLong (text.trim ());
            } catch (Exception ex) {
                error ("Invalid long");
                continue;
            }
            int code = validator.validate (value);
            if (code < 0) {
                return value;
            } else {
                error (messages [code]);
            }
        }
    }
    default long readLong (String prompt, long defaultValue, IValidator<Long> validator) throws IOException {
        if (validator == null) {
            return readLong (prompt, defaultValue);
        }

        String text;
        long value;
        while (true) {
            text = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (text)) {
                return defaultValue;
            }

            try {
                value = Long.parseLong (text.trim ());
            } catch (Exception ex) {
                error ("Invalid long");
                continue;
            }

            if (validator.validate (value)) {
                return value;
            }
        }
    }
    default long readLong (String prompt, long defaultValue, IComplexValidator<Long> validator, String... messages) throws IOException {
        if (validator == null) {
            return readLong (prompt, defaultValue);
        }

        String text;
        long value;
        while (true) {
            text = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (text)) {
                return defaultValue;
            }

            try {
                value = Long.parseLong (text.trim ());
            } catch (Exception ex) {
                error ("Invalid long");
                continue;
            }

            int code = validator.validate (value);
            if (code < 0) {
                return value;
            } else {
                error (messages [code]);
            }
        }
    }

    default float readFloat () throws IOException {
        String line = readLine ();
        return Float.parseFloat (line);
    }
    default float readFloat (String prompt) throws IOException {
        String line = readString (prompt);
        return Float.parseFloat (line);
    }
    default float readFloat (String prompt, float defaultValue) throws IOException {
        while (true) {
            String text = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (text)) {
                return defaultValue;
            }

            float value;
            try {
                value = Float.parseFloat (text.trim ());
                return value;
            } catch (Exception ex) {
                error ("Invalid float.");
            }
        }
    }
    default float readFloat (String prompt, IValueGenerator<Float> g) throws IOException {
        if (g == null) {
            return readFloat (prompt);
        }

        String text;
        while (true) {
            text = readString (prompt);
            if (StringUtil.isEmpty (text)) {
                return g.generate (null);
            }

            try {
                return Float.parseFloat (text.trim ());
            } catch (Exception ex) {
                error ("Invalid float");
            }
        }
    }
    default float readFloat (String prompt, IValidator<Float> validator) throws IOException {
        if (validator == null) {
            return readFloat (prompt);
        }

        while (true) {
            String text = readString (prompt);
            if (StringUtil.isEmpty (text)) {
                error ("Invalid float.");
                continue;
            }
            Float f = CommandLineHelper.readFloat (this, text, validator);
            if (f != null) {
                return f;
            }
        }
    }
    default float readFloat (String prompt, IComplexValidator<Float> validator, String... messages) throws IOException {
        if (validator == null) {
            return readFloat (prompt);
        }

        while (true) {
            String text = readString (prompt);
            if (StringUtil.isEmpty (text)) {
                error ("Invalid float.");
                continue;
            }
            Float f = CommandLineHelper.readFloat (this, text, validator, messages);
            if (f != null) {
                return f;
            }
        }
    }
    default float readFloat (String prompt, float defaultValue, IValidator<Float> validator) throws IOException {
        if (validator == null) {
            return readFloat (prompt, defaultValue);
        }

        while (true) {
            String line = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (line)) {
                return defaultValue;
            }

            Float f = CommandLineHelper.readFloat (this, line, validator);
            if (f != null) {
                return f;
            }
        }
    }
    default float readFloat (String prompt, float defaultValue, IComplexValidator<Float> validator, String... messages) throws IOException {
        if (validator == null) {
            return readFloat (prompt, defaultValue);
        }

        while (true) {
            String line = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (line)) {
                return defaultValue;
            }

            Float f = CommandLineHelper.readFloat (this, line, validator, messages);
            if (f != null) {
                return f;
            }
        }
    }

    default double readDouble () throws IOException {
        while (true) {
            try {
                String line = readLine ();
                return Double.parseDouble (line);
            } catch (Exception ex) {
                error ("Invalid double");
            }
        }
    }
    default double readDouble (String prompt) throws IOException {
        while (true) {
            try {
                String line = readString (prompt);
                return Double.parseDouble (line);
            } catch (Exception ex) {
                error ("Invalid double");
            }
        }
    }
    default double readDouble (String prompt, double defaultValue) throws IOException {
        while (true) {
            String text = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (text)) {
                return defaultValue;
            }

            double value;
            try {
                value = Double.parseDouble (text.trim ());
                return value;
            } catch (Exception ex) {
                error ("Invalid float.");
            }
        }
    }
    default double readDouble (String prompt, IValueGenerator<Double> g) throws IOException {
        if (g == null) {
            return readDouble (prompt);
        }

        String text;
        while (true) {
            text = readString (prompt);
            if (StringUtil.isEmpty (text)) {
                return g.generate (null);
            }

            try {
                return Double.parseDouble (text.trim ());
            } catch (Exception ex) {
                error ("Invalid double.");
            }
        }
    }
    default double readDouble (String prompt, IValidator<Double> validator) throws IOException {
        if (validator == null) {
            return readDouble (prompt);
        }

        while (true) {
            String text = readString (prompt);
            if (StringUtil.isEmpty (text)) {
                error ("Invalid double.");
                continue;
            }
            Double d = CommandLineHelper.readDouble (this, text, validator);
            if (d != null) {
                return d;
            }
        }
    }
    default double readDouble (String prompt, IComplexValidator<Double> validator, String... messages) throws IOException {
        if (validator == null) {
            return readDouble (prompt);
        }

        while (true) {
            String text = readString (prompt);
            if (StringUtil.isEmpty (text)) {
                error ("Invalid double.");
                continue;
            }
            Double value = CommandLineHelper.readDouble (this, text, validator, messages);
            if (value != null) {
                return value;
            }
        }
    }
    default double readDouble (String prompt, double defaultValue, IValidator<Double> validator) throws IOException {
        if (validator == null) {
            return readDouble (prompt, defaultValue);
        }

        while (true) {
            String line = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (line)) {
                return defaultValue;
            }

            Double value = CommandLineHelper.readDouble (this, line, validator);
            if (value != null) {
                return value;
            }
        }
    }
    default double readDouble (String prompt, double defaultValue, IComplexValidator<Double> validator, String... messages) throws IOException {
        if (validator == null) {
            return readDouble (prompt, defaultValue);
        }

        while (true) {
            String line = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (line)) {
                return defaultValue;
            }
            Double d = CommandLineHelper.readDouble (this, line, validator, messages);
            if (d != null) {
                return d;
            }
        }
    }

    default boolean readBoolean () throws IOException {
        return readBoolean (null);
    }
    default boolean readBoolean (String prompt) throws IOException {
        while (true) {
            if (!StringUtil.isEmpty (prompt)) {
                print (prompt + ": ");
            }
            String line = readLine ();
            if (!StringUtil.isEmpty (line)) {
                try {
                    return Boolean.valueOf (line.trim ());
                } catch (Exception ex) {
                    error ("Invalid boolean!");
                }
            }
            error ("Invalid boolean!");
        }
    }
    default boolean readBoolean (String prompt, boolean defaultValue) throws IOException {
        while (true) {
            String line = readString (prompt + "[" + defaultValue + "]");
            if (StringUtil.isEmpty (line)) {
                return defaultValue;
            }

            line = line.trim ();
            if ("y".equalsIgnoreCase (line) || "yes".equalsIgnoreCase (line) ||
                    "t".equalsIgnoreCase (line) || "true".equalsIgnoreCase (line)) {
                return true;
            } else if ("n".equalsIgnoreCase (line) || "no".equalsIgnoreCase (line) ||
                    "f".equalsIgnoreCase (line) || "false".equalsIgnoreCase (line)) {
                return false;
            } else {
                error ("Invalid boolean");
            }
        }
    }
}