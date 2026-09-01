package org.dreamwork.db;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public class GenericDatabase extends AbstractDatabase {
    private final DataSource datasource;

    public GenericDatabase (DataSource datasource) {
        this.datasource = datasource;
    }

    @Override
    protected Connection connect () throws SQLException {
        return datasource == null ? null : datasource.getConnection ();
    }
}