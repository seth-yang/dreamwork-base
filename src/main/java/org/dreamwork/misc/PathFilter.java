package org.dreamwork.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created with IntelliJ IDEA.
 * User: seth.yang
 * Date: 13-8-22
 * Time: 下午6:33
 */
public class PathFilter {
    public static final int TYPE_COMPLETE_MATCH = 0,
            TYPE_EXT = 1,
            TYPE_OTHER = 2,
            TYPE_PATTERN = 3;

    private String expression;
    private Pattern pattern;
    private int type;

    private List<PathFilter> stack;

    public PathFilter (String expression) {
        this.expression = expression;
        if (expression.indexOf ('*') < 0) {
            type = TYPE_COMPLETE_MATCH;
        } else if (expression.charAt (0) == '*') {
            type = TYPE_EXT;
            this.expression = expression.substring (1);
        } else if (expression.indexOf ('/') < 0) {
            type = TYPE_PATTERN;
            String e = expression.replace (".", "\\.").replace ("*", ".*").replace ("$", "\\$");
            pattern = Pattern.compile (e);
        } else {
            stack = new ArrayList<PathFilter> ();
            for (String part : expression.split ("/")) {
                stack.add (new PathFilter (part));
            }

            type = TYPE_OTHER;
        }
    }

    public String getExpression () {
        return expression;
    }

    public int getType () {
        return type;
    }

    public boolean hit (String path) {
        switch (type) {
            case TYPE_EXT :
                return path.endsWith (expression);
            case TYPE_COMPLETE_MATCH :
                return expression.equals (path);
            case TYPE_PATTERN :
                return pattern.matcher (path).matches ();
            default :
                List<String> parts = Arrays.asList (path.split ("/"));
                if (parts.size () != stack.size ()) return false;

                for (int i = 0; i < parts.size (); i ++) {
                    PathFilter pf = stack.get (i);
                    String part2 = parts.get (i);
                    if (!pf.hit (part2))
                        return false;
                }
                return true;
        }
    }
}