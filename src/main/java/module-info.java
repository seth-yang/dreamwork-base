module dreamwork.base {
    requires com.fasterxml.jackson.databind;
    requires java.desktop;
    requires java.management;
    requires java.naming;
    requires java.scripting;
    requires java.sql;
    requires java.xml;
    requires org.slf4j;

    exports org.dreamwork.ansy.processors;
    exports org.dreamwork.ansy.progress;
    exports org.dreamwork.cli;
    exports org.dreamwork.cli.text;
    exports org.dreamwork.compilation;
    exports org.dreamwork.concurrent;
    exports org.dreamwork.concurrent.broadcast;
    exports org.dreamwork.config;
    exports org.dreamwork.db;
    exports org.dreamwork.fs;
    exports org.dreamwork.fs.nio;
    exports org.dreamwork.i18n;
    exports org.dreamwork.i18n.adapters;
    exports org.dreamwork.i18n.manager;
    exports org.dreamwork.misc;
    exports org.dreamwork.network;
    exports org.dreamwork.network.udp;
    exports org.dreamwork.persistence;
    exports org.dreamwork.secure;
    exports org.dreamwork.telnet;
    exports org.dreamwork.telnet.command;
    exports org.dreamwork.text;
    exports org.dreamwork.text.sql;
    exports org.dreamwork.ui.concurrent;
    exports org.dreamwork.util;
    exports org.dreamwork.util.cal;
    exports org.dreamwork.util.sort;
}