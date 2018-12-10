package org.dreamwork.db;

import org.dreamwork.util.StringUtil;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import java.sql.*;

/**
 * Created by seth.yang on 2017/8/1
 */
public class PostgreSQL extends AbstractDatabase {
    private static final String SQL_IS_TABLE_PRESENT = "SELECT 1 FROM pg_tables WHERE tablename = ?";

    private String jndiName, userName, password, url;
    private DataSource ds;

    public PostgreSQL (DataSource dataSource) {
        this.ds = dataSource;
    }

    public PostgreSQL (String jndiName) {
        this.jndiName = jndiName;
        try {
            Context ctx = new InitialContext ();
            ds = (DataSource) ctx.lookup (jndiName);
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
        limitSupported = true;
    }

    public PostgreSQL (String url, String userName, String password) {
        this.userName = userName;
        this.password = password;
        this.url = url;
        try {
            Class.forName ("org.postgresql.Driver");
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException (ex);
        }
        limitSupported = true;
    }

    public String getJndiName () {
        return jndiName;
    }

    public String getUserName () {
        return userName;
    }

    public String getPassword () {
        return password;
    }

    public String getUrl () {
        return url;
    }

    @Override
    protected Connection connect () throws SQLException {
        return ds != null ? ds.getConnection () :
               !StringUtil.isEmpty (url) ?
                       DriverManager.getConnection (url, userName, password) :
                       null;
    }

    @Override
    public boolean isTablePresent (String tableName) {
        try (Connection conn = connect ()) {
            if (debug) {
                logger.debug ("executing sql: " + SQL_IS_TABLE_PRESENT);
            }
            PreparedStatement pstmt = conn.prepareStatement (SQL_IS_TABLE_PRESENT);
            pstmt.setString (1, tableName);
            ResultSet rs = pstmt.executeQuery ();
            return rs.next ();
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }
}
