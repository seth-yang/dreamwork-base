package org.dreamwork.text.sql;

/**
 * Created by IntelliJ IDEA.
 * User: <a href = "mailto:seth_yang@21cn.com">seth yang</a>
 * Date: 2007-4-23
 * Time: 18:53:48
 */
public class Part {
    private String name;
    private String alias;
    private String expression;

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public String getAlias () {
        return alias;
    }

    public void setAlias (String alias) {
        this.alias = alias;
    }

    public String getExpression () {
        return expression;
    }

    public void setExpression (String expression) {
        this.expression = expression;
    }
}