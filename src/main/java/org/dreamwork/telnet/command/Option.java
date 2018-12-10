package org.dreamwork.telnet.command;

/**
 * Created by seth.yang on 2018/9/27
 */
public class Option {
    public String shortName, longName, value, defaultValue;
    public boolean required, requireValue;

    public Option () {}

    public Option (String shortName, String longName, String defaultValue, boolean required, boolean requireValue) {
        this.shortName = shortName;
        this.longName = longName;
        this.defaultValue = defaultValue;
        this.required = required;
        this.requireValue = requireValue;
    }

    public Option copy () {
        Option opt       = new Option ();
        opt.shortName    = shortName;
        opt.longName     = longName;
        opt.value        = value;
        opt.defaultValue = defaultValue;
        opt.required     = required;
        opt.requireValue = requireValue;

        return opt;
    }
}