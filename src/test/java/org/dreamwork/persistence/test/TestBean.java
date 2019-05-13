package org.dreamwork.persistence.test;

import org.dreamwork.persistence.ISchema;
import org.dreamwork.persistence.ISchemaField;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by game on 2017/6/17
 */
@ISchema (TestDatabaseSchema.class)
public class TestBean extends BaseBean {
    @ISchemaField (name = "f_name")
    private String name;

    @ISchemaField (name = "f_int")
    private int intValue;

    @ISchemaField (name = "f_long")
    private long longValue;

    @ISchemaField (name = "f_ts")
    private Date timestamp;

    @ISchemaField (name = "f_txt")
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

    public String getMemo () {
        return memo;
    }

    public void setMemo (String memo) {
        this.memo = memo;
    }
}