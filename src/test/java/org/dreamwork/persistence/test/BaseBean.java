package org.dreamwork.persistence.test;

import org.dreamwork.persistence.ISchemaField;

/**
 * Created by seth.yang on 2017/7/24
 * that's greate!
 */
public class BaseBean {
    @ISchemaField(name = "id", id = true, autoincrement = true)
    protected Long id;

    public Long getId () {
        return id;
    }

    public void setId (Long id) {
        this.id = id;
    }
}