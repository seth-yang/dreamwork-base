package org.dreamwork.cli;

import java.util.List;

/**
 * Created by seth.yang on 2018/11/7
 */
class CommandLineHelper {
    static void showOption (String prompt, List<String> options, ICommandLine cli) {
        cli.println (prompt + ":");
        for (int i = 0; i < options.size (); i++) {
            if (options.size () > 10) {
                cli.printf ("  [%02d] %s%n", i, options.get (i));
            } else {
                cli.printf ("  [%d] %s%n", i, options.get (i));
            }
        }
    }
    static boolean checkOption (int pos, String prompt, List<String> options, ICommandLine cli) {
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
    static Double readDouble (ICommandLine cli, String line, IComplexValidator<Double> validator, String...messages) {
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
    static Double readDouble (ICommandLine cli, String line, IValidator<Double> validator) {
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
    static Float readFloat (ICommandLine cli, String line, IComplexValidator<Float> validator, String... messages) {
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
    static Float readFloat (ICommandLine cli, String line, IValidator<Float> validator) {
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
}
