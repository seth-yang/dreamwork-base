package org.dreamwork.persistence.test;

import org.dreamwork.db.ScriptParser;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by game on 2017/7/29
 */
public class TestScriptExecutor {
    private File script;

    @Before
    public void setup () {
        script = new File ("E:\\workspace\\home_center\\java\\modules\\center-web\\web\\WEB-INF\\init-data.sql");
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