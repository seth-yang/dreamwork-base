package org.dreamwork.db;

import org.dreamwork.persistence.DatabaseSchema;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by game on 2017/4/15
 */
public class SQLite extends AbstractDatabase {
    private static final String SQL_IF_TABLE_PRESENT =
            "SELECT 1 FROM sqlite_master WHERE type = 'table' and name=?";
    private static final Map<String, SQLite> instances = new HashMap<> ();

    static {
        try {
            Class.forName ("org.sqlite.JDBC");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException (ex);
        }
    }

    public synchronized static SQLite get (String path) {
        SQLite sqLite = instances.get (path);
        if (sqLite == null) {
            sqLite = new SQLite (path);
            instances.put (path, sqLite);
        }

        return sqLite;
    }

    private SQLite (String path) {
        this.path = path;
        limitSupported = true;
    }

    protected SQLite () {
        limitSupported = true;
    }

    private String path;

    @Override
    public Connection connect () throws SQLException {
        return DriverManager.getConnection ("jdbc:sqlite:" + path);
    }

    @Override
    public boolean isTablePresent (String tableName) {
        try (Connection conn = connect ()) {
            PreparedStatement pstmt = conn.prepareStatement (SQL_IF_TABLE_PRESENT);
            pstmt.setString (1, tableName);
            return pstmt.executeQuery ().next ();
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public void clear (String tableName) {
        throw new AbstractMethodError ();
    }

    @Override
    public void clear (Class<?> type) {
        DatabaseSchema schema = ref.map (type);
        if (schema != null) {
            try (Connection conn = connect ()) {
                Statement stmt = conn.createStatement ();
                stmt.execute ("DROP TABLE " + schema.getTableName ());
                stmt.execute (schema.getCreateDDL ());
            } catch (SQLException ex) {
                throw new RuntimeException (ex);
            }
        }
    }
}