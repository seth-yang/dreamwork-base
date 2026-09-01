package org.dreamwork.persistence.test;

import org.dreamwork.persistence.DatabaseSchema;

/**
 * Created by game on 2017/6/17
 */
public class TestDatabaseSchema extends DatabaseSchema {
    public TestDatabaseSchema () {
        tableName = "test_table";
        fields = new String[] {
                "id", "f_name", "f_int", "f_long", "f_ts", "f_txt"
        };
    }

    @Override
    public String getCreateDDL () {
        return """
                CREATE table test_table (
                    id          serial8              not null primary key,
                    f_name      varchar(32),
                    f_int       integer,
                    f_long      bigint,
                    f_ts        timestamp,
                    f_txt       text
                )""";
    }

    @Override
    public String getPrimaryKeyName () {
        return "id";
    }
}