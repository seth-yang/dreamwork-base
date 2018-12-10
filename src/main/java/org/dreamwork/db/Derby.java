package org.dreamwork.db;

import org.dreamwork.persistence.DatabaseSchema;
import org.dreamwork.util.ITypedMap;
import org.dreamwork.util.StringUtil;

import java.sql.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by seth.yang on 2017/4/19
 */
public class Derby extends AbstractDatabase {
    private static final Map<String, Derby> instances = new HashMap<> ();

    static {
        try {
            Class.forName ("org.apache.derby.jdbc.EmbeddedDriver");
            Class.forName ("org.apache.derby.jdbc.ClientDriver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException (e);
        }
    }

    public static synchronized Derby get (String path) {
        Derby derby = instances.get (path);
        if (derby == null) {
            derby = new Derby (path);
            instances.put (path, derby);
        }

        return derby;
    }

    private String path;
    private boolean network;

    private Derby (String path) {
        this.path = path;
        Pattern p = Pattern.compile ("(.*?):[\\d]+/");
        Matcher m = p.matcher (path);
        network   = m.find ();
    }

    @Override
    public Connection connect () throws SQLException {
        return DriverManager.getConnection ("jdbc:derby:" + path + ";create=true");
    }

    @Override
    public boolean isTablePresent (String tableName) {
        try (Connection conn = connect ()) {
            DatabaseMetaData dmd = conn.getMetaData ();
            ResultSet rs = dmd.getTables ("", null, "%", new String[] {"TABLE"});
            while (rs.next ()) {
                String name = rs.getString ("TABLE_NAME");
                if (tableName.equalsIgnoreCase (name)) {
                    return true;
                }
            }

            return false;
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public List<ITypedMap> list (int pageNo, int pageSize, String sql, Object... args) {
        int start = (pageNo - 1) * pageSize;
        sql += "OFFSET " + start + "ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
        return list (sql, args);
    }

    @Override
    public <T> List<T> list (Class<T> type, int pageNo, int pageSize, String sql, Object... args) {
        int start = (pageNo - 1) * pageSize;
        sql += "OFFSET " + start + "ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
        return super.list (type, sql, args);
    }

    @Override
    public <T> List<T> get (Class<T> type, int pageNo, int pageSize, String selection, String order, Object... args) {
        DatabaseSchema schema = ref.map (type);
        String sql = schema.getQuerySQL ();
        if (!StringUtil.isEmpty (selection)) {
            sql += " WHERE " + selection;
        }
        if (!StringUtil.isEmpty (order)) {
            sql += " ORDER BY " + order;
        }
        int start = (pageNo - 1) * pageSize;
        sql += "OFFSET " + start + "ROWS FETCH NEXT " + pageSize + " ROWS ONLY";
        return get (type, sql, args);
    }

    public void shutdown () {
        if (!network) {
            try {
                DriverManager.getConnection ("jdbc:derby:" + path + ";shutdown=true");
            } catch (SQLException e) {
                e.printStackTrace ();
            }
        }
    }
}