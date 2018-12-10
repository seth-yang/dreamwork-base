package org.dreamwork.persistence;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by game on 2016/9/7
 */
public abstract class DatabaseSchema {
    public static final Map<Class<? extends DatabaseSchema>, DatabaseSchema> MAP = new HashMap<> ();

    public static void register (DatabaseSchema schema) {
        if (schema == null) {
            throw new NullPointerException ("schema is null");
        }

        MAP.put (schema.getClass (), schema);
    }

    public static void register (Class<? extends DatabaseSchema> schema) {
        if (schema == null) {
            throw new NullPointerException ("schema is null");
        }

        try {
            DatabaseSchema instance = schema.newInstance ();
            MAP.put (schema, instance);
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    protected String tableName;
    protected String[] fields;

    public abstract String getCreateDDL ();
    public abstract String getPrimaryKeyName ();

    protected DatabaseSchema () {
        register (this);
    }

    public String getInsertSQL () {
        StringBuilder builder = new StringBuilder ("INSERT INTO ").append (tableName).append (" (");
        StringBuilder clause  = new StringBuilder ();
        for (String field : fields) {
            if (clause.length () != 0) {
                builder.append (", ");
                clause.append  (", ");
            }
            builder.append (field);
            clause.append ('?');
        }

        return builder.append (") VALUES (").append (clause).append (')').toString ();
    }

    public String getQuerySQL  () {
        return "SELECT * FROM " + tableName;
    }

    public String getTableName () {
        return tableName;
    }

    public String[] getFields () {
        return fields;
    }
}
