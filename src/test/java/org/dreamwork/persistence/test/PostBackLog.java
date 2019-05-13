package org.dreamwork.persistence.test;

import org.dreamwork.persistence.*;

@ISchema (PostBackLogSchema.class)
public class PostBackLog {
	@ISchemaField (name = "id", id = true)
	private String id;

	@ISchemaField (name = "ts")
	private java.sql.Timestamp ts;

	@ISchemaField (name = "msg_type")
	private String msgType;

	@ISchemaField (name = "raw_data")
	private String rawData;

	public String getId () {
		return id;
	}

	public void setId (String id) {
		this.id = id;
	}

	public java.sql.Timestamp getTs () {
		return ts;
	}

	public void setTs (java.sql.Timestamp ts) {
		this.ts = ts;
	}

	public String getMsgType () {
		return msgType;
	}

	public void setMsgType (String msgType) {
		this.msgType = msgType;
	}

	public String getRawData () {
		return rawData;
	}

	public void setRawData (String rawData) {
		this.rawData = rawData;
	}

	@Override
	public boolean equals (Object o) {
		if (this == o) return true;
		if (o == null || getClass () != o.getClass ()) return false;
		PostBackLog that = (PostBackLog) o;
		return id != null && id.equals (that.id);
	}

	@Override
	public int hashCode () {
		return id != null ? id.hashCode () : 0;
	}
}