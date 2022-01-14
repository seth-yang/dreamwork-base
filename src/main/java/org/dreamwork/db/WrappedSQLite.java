package org.dreamwork.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class WrappedSQLite extends SQLite {
    private final DataSource ds;
    public WrappedSQLite (DataSource ds) {
        this.ds = ds;
    }

    @Override
    public Connection connect () throws SQLException {
        return ds.getConnection ();
    }

    @Override
    public Connection getConnection () throws SQLException {
        return super.getConnection ();
    }
}