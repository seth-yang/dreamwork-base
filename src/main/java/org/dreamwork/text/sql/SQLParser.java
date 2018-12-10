package org.dreamwork.text.sql;

import org.dreamwork.text.TextParser;

/**
 * Created by IntelliJ IDEA.
 * User: <a href = "mailto:seth_yang@21cn.com">seth yang</a>
 * Date: 2007-4-23
 * Time: 17:09:42
 */
public class SQLParser extends TextParser {
    private String sql;
    private char[] backup = null;

    public SQLParser (String sql) {
        this.sql = sql;
        this.stream = sql.toLowerCase ().toCharArray ();
        backup = sql.toCharArray ();
    }

    public String getSQL () {
        return sql;
    }

    public void parse () {
        SQLStatement stmt = new SQLStatement ();
        int start = cursor;
        for (int ch = nextChar (); ch != -1; ch = nextChar ()) {
            switch (ch) {
                case ' ' :
                    System.out.println (new String (stream, start, cursor - start));
                    start = cursor;
                    break;
            }
        }
        System.out.println (new String (stream, start, stream.length - start));
    }

    public static void main (String[] args) throws Exception {
        SQLParser parser = new SQLParser ("select t.*, a.* from t, a where a.id = t.id");
        parser.parse ();
    }
}