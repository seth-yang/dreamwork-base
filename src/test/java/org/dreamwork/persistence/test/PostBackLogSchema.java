package org.dreamwork.persistence.test;

import org.dreamwork.persistence.*;

public class PostBackLogSchema extends DatabaseSchema {
	public  PostBackLogSchema () {
		tableName = "post_back_log";
		fields = new String[] {"id", "ts", "msg_type", "raw_data"};
	}

	@Override
	public String getPrimaryKeyName () {
		return "id";
	}

	@Override
	public String getCreateDDL () {
		return "create table post_back_log (" +
"    id           varchar(32)       not null primary key," +
"    ts           timestamp," +
"    msg_type     varchar(32)," +
"    raw_data     text" +
")";
	}
}