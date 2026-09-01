package org.dreamwork.persistence.test;

import org.dreamwork.persistence.ISchema;
import org.dreamwork.persistence.ISchemaField;

import java.util.Date;

/**
 * Created by game on 2017/6/17
 */
@ISchema (TestDatabaseSchema.class)
public class TestBean extends BaseBean {
    @ISchemaField ("f_name")
    private String name;

    @ISchemaField ("f_int")
    private int intValue;

    @ISchemaField ("f_long")
    private long longValue;

    @ISchemaField ("f_ts")
    private Date timestamp;

    private String memo;

    public String getName () {
        return name;
    }

    public void setName (String name) {
        this.name = name;
    }

    public int getIntValue () {
        return intValue;
    }

    public void setIntValue (int intValue) {
        this.intValue = intValue;
    }

    public long getLongValue () {
        return longValue;
    }

    public void setLongValue (long longValue) {
        this.longValue = longValue;
    }


    public Date getTimestamp () {
        return timestamp;
    }

    public void setTimestamp (Date timestamp) {
        this.timestamp = timestamp;
    }

    @ISchemaField ("f_txt")
    public String getMemo () {
        return memo;
    }

    public void setMemo (String memo) {
        this.memo = memo;
    }
}