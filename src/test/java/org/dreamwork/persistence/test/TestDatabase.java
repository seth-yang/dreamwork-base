package org.dreamwork.persistence.test;

import org.dreamwork.db.AbstractDatabase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by game on 2017/6/17
 */
public class TestDatabase extends AbstractDatabase {
    @Override
    public Connection connect () throws SQLException {
        try {
            Class.forName ("org.postgresql.Driver");
            return DriverManager.getConnection ("jdbc:postgresql://192.168.2.29/rpi_cam", "nb", "nb");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace ();

            throw new SQLException (ex);
        }
    }
}