package org.dreamwork.text.sql;

/**
 * Created by IntelliJ IDEA.
 * User: <a href = "mailto:seth_yang@21cn.com">seth yang</a>
 * Date: 2007-4-23
 * Time: 17:19:39
 */
public class SQLStatement {
    private Clause select;
    private Clause from;
    private Clause where;
    private Clause orderby;
    private Clause group;

    public Clause getSelect () {
        return select;
    }

    public void setSelect (Clause select) {
        this.select = select;
    }

    public Clause getFrom () {
        return from;
    }

    public void setFrom (Clause from) {
        this.from = from;
    }

    public Clause getWhere () {
        return where;
    }

    public void setWhere (Clause where) {
        this.where = where;
    }

    public Clause getOrderby () {
        return orderby;
    }

    public void setOrderby (Clause orderby) {
        this.orderby = orderby;
    }

    public Clause getGroup () {
        return group;
    }

    public void setGroup (Clause group) {
        this.group = group;
    }
}