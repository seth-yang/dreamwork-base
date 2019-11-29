package org.dreamwork.cli;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by seth.yang on 2018/11/7
 */
public class CommandLineHelper {
    public static void showOption (String prompt, List<String> options, ICommandLine cli) throws IOException {
        cli.println (prompt + ":");
        for (int i = 0; i < options.size (); i++) {
            if (options.size () > 10) {
                cli.printf ("  [%02d] %s%n", i, options.get (i));
            } else {
                cli.printf ("  [%d] %s%n", i, options.get (i));
            }
        }
    }
    public static boolean checkOption (int pos, String prompt, List<String> options, ICommandLine cli) {
        try {
            if (pos >= 0 && pos < options.size ()) {
                return true;
            } else {
                cli.error ("Invalid option: " + pos);
                showOption (prompt, options, cli);
                return false;
            }
        } catch (Exception ex) {
            return false;
        }
    }
    public static Double readDouble (ICommandLine cli, String line, IComplexValidator<Double> validator, String...messages) throws IOException {
        try {
            double d = Double.parseDouble (line.trim ());
            int code = validator.validate (d);
            if (code < 0) {
                return d;
            } else {
                cli.error (messages [code]);
            }
        } catch (Exception ex) {
            cli.error ("Invalid double.");
        }
        return null;
    }
    public static Double readDouble (ICommandLine cli, String line, IValidator<Double> validator) throws IOException {
        try {
            double d = Double.parseDouble (line.trim ());
            if (validator.validate (d)) {
                return d;
            }
        } catch (Exception ex) {
            cli.error ("Invalid double.");
        }
        return null;
    }
    public static Float readFloat (ICommandLine cli, String line, IComplexValidator<Float> validator, String... messages) throws IOException {
        try {
            float n = Float.parseFloat (line.trim ());
            int code = validator.validate (n);
            if (code < 0) {
                return n;
            } else {
                cli.error (messages [code]);
            }
        } catch (Exception ex) {
            cli.error ("Invalid float");
        }
        return null;
    }
    public static Float readFloat (ICommandLine cli, String line, IValidator<Float> validator) throws IOException {
        try {
            float n = Float.parseFloat (line.trim ());
            if (validator.validate (n)) {
                return n;
            }
        } catch (Exception ex) {
            cli.error ("Invalid float");
        }
        return null;
    }

    public static Properties initLogger (ClassLoader loader, String logLevel, String logFile, String... packages) throws IOException {
        File file = new File (logFile);
        File parent = file.getParentFile ();
        if (!parent.exists () && !parent.mkdirs ()) {
            throw new IOException ("Can't create dir: " + parent.getCanonicalPath ());
        }

        try (InputStream in = loader.getResourceAsStream ("internal-log4j.properties")) {
            Properties props = new Properties ();
            props.load (in);

            System.out.println ("### setting log level to " + logLevel + " ###");
            if (!"trace".equalsIgnoreCase (logLevel) && !"debug".equalsIgnoreCase (logLevel)) {
                props.setProperty ("log4j.rootLogger", logLevel + ", stdout, FILE");
            } else {
                props.setProperty ("log4j.rootLogger", "INFO, stdout, FILE");
            }
            if ("trace".equalsIgnoreCase (logLevel)) {
//                props.setProperty ("log4j.rootLogger", "INFO, stdout, FILE");
                props.setProperty ("log4j.appender.stdout.Threshold", logLevel);
                props.setProperty ("log4j.appender.FILE.File", logFile);
                props.setProperty ("log4j.appender.FILE.Threshold", logLevel);
                if (packages.length > 0) {
                    for (String name : packages) {
                        props.setProperty ("log4j.logger." + name, "trace");
                    }
                }
            } else {
//                props.setProperty ("log4j.rootLogger", logLevel + ", stdout, FILE");
                props.setProperty ("log4j.appender.FILE.File", logFile);
                props.setProperty ("log4j.appender.FILE.Threshold", logLevel);
            }
            return props;
        }
    }

    public static Properties parseConfig (String configFile) throws IOException {
        System.out.println ("parsing config file ...");
        configFile = configFile.trim ();
        System.out.println ("found config file: " + configFile);
        File file;
        if (configFile.startsWith ("file:/") || configFile.startsWith ("/")) {
            file = new File (configFile);
        } else {
            file = new File (".", configFile);
        }

        if (!file.exists ()) {
            System.err.println ("can't find config file: " + configFile);
            System.exit (-1);
        }

        Properties props = new Properties ();
        try (InputStream in = new FileInputStream (file)) {
            props.load (in);
        }

        prettyTrace (props);
        return props;
    }

    private static void prettyTrace (Properties props) {
        System.out.println ("### global configuration ###");
        int length = 0;
        List<String> list = new ArrayList<> ();
        for (String key : props.stringPropertyNames ()) {
            list.add (key);
            if (key.length () > length) {
                length = key.length ();
            }
        }
        list.sort (String::compareTo);
        for (String key : list) {
            StringBuilder builder = new StringBuilder (key);
            if (key.length () < length) {
                int d = length - key.length ();
                for (int i = 0; i < d; i ++) {
                    builder.append (' ');
                }
            }
            builder.append (" : ").append (props.getProperty (key));
            System.out.println (builder);
        }
        System.out.println ("############################");
    }
}