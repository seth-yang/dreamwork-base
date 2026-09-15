package org.dreamwork.persistence.test;

import org.dreamwork.db.ScriptParser;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Created by game on 2017/7/29
 */
public class TestScriptExecutor {
    private File script;

    private static final String STATEMENTS = """
        INSERT INTO MQTT_SERVER (address, port, protocol) VALUES ('10.247.1.90', 1883, 'tcp');
        
        INSERT INTO NODE (name, type, status) VALUES ('esp8266-01', 'socket', 'online');
        
        INSERT INTO subscription (server_id, topic) VALUES (1, 'home/uplink/#');
        
        INSERT INTO sensor (node_id, name, type) VALUES (1, 'DS18B20', 'temp');
        INSERT INTO sensor (node_id, name, type) VALUES (1, 'wifi socket', 'relay');
        INSERT INTO sensor (node_id, name, type) VALUES (1, 'led', 'led');
        INSERT INTO sensor (node_id, name, type) VALUES (1, 'fan', 'fan');
        """;

    @Before
    public void setup () throws IOException {
        script = new File (System.getProperty ("java.io.tmpdir"), "init.sql");
        Path path = script.toPath ();
        Files.writeString (path, STATEMENTS);
    }

    @After
    public void cleanup () throws IOException {
        var path = script.toPath ();
        Files.deleteIfExists (path);
    }

    @Test
    public void testParse () throws IOException {
        List<String> list = new ScriptParser (script).parse ();
        for (String stmt : list) {
            System.out.println (stmt);
            System.out.println ("___________________________________");
        }
    }
}