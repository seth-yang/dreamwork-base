package org.dreamwork.db;

import org.dreamwork.text.TextParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by game on 2017/7/28
 */
public class ScriptParser extends TextParser {
    public ScriptParser () {
    }

    public ScriptParser (File file) throws IOException {
        super (file);
    }

    public ScriptParser (URL url) throws IOException {
        super (url);
    }

    public ScriptParser (InputStream in) throws IOException {
        super (in);
    }

    public ScriptParser (InputStream in, String charset) throws IOException {
        super (in, charset);
    }

    public ScriptParser (String src) {
        super (src);
    }

    public ScriptParser (Reader reader) throws IOException {
        super (reader);
    }

    public List<String> parse () {
        List<String> list = new ArrayList<> ();
        StringBuilder builder = new StringBuilder ();
        boolean in_quota = false;
        for (int ch = nextChar (); ch != -1; ch = nextChar ()) {
            switch (ch) {
                case ';' :
                    if (!in_quota) {
                        list.add (builder.toString ());
                        builder.setLength (0);
                    } else {
                        builder.append ((char) ch);
                    }
                    break;
                case '\'' :
                    builder.append ((char) ch);
                    if (!in_quota) {
                        in_quota = true;
                    } else if (peek () != '\'' && stream [cursor - 2] != '\'') {
                        in_quota = false;
                    }
                    break;
                case '-' :
                    if (in_quota) {
                        builder.append ((char) ch);
                    } else {
                        if (peek () == '-') {
                            // line comment, skip to EOL
                            skipUntil ("\n");
                            builder.append ('\n');
                        } else {
                            builder.append ((char) ch);
                        }
                    }
                    break;
                case '/' :
                    if (in_quota) {
                        builder.append ((char) ch);
                    } else {
                        if (peek () == '*') {
                            // block comment, skip to */
                            skipUntil ("*/");
                        } else {
                            builder.append ((char) ch);
                        }
                    }
                    break;
                default :
                    builder.append ((char) ch);
                    break;
            }
        }
        if (builder.length () > 0)
            list.add (builder.toString ());

        return list;
    }
}