package org.dreamwork.db;

//import org.dreamwork.concurrent.Looper;
/*
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
*/

import org.dreamwork.concurrent.IManagedClosable;

import java.sql.*;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * Created by seth.yang on 2017/7/4
 */
public class ConnectionWrapper implements Connection, IManagedClosable {
    private final Connection conn;

    long timestamp;
    long timeout;

    ConnectionWrapper (Connection conn) {
        this (conn, 300, TimeUnit.SECONDS);
    }

    ConnectionWrapper (Connection conn, int timeout, TimeUnit unit) {
        this.conn    = conn;
        this.timeout = unit.toMillis (timeout);
        timestamp    = System.currentTimeMillis ();
    }

    public Statement createStatement () throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.createStatement ();
    }

    public boolean getAutoCommit () throws SQLException {
        return conn.getAutoCommit ();
    }

    public CallableStatement prepareCall (String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.prepareCall (sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public Statement createStatement (int resultSetType, int resultSetConcurrency) throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.createStatement (resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement (String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.prepareStatement (sql, resultSetType, resultSetConcurrency);
    }

    public void setNetworkTimeout (Executor executor, int milliseconds) throws SQLException {
        conn.setNetworkTimeout (executor, milliseconds);
    }

    public Map<String, Class<?>> getTypeMap () throws SQLException {
        return conn.getTypeMap ();
    }

    public SQLXML createSQLXML () throws SQLException {
        return conn.createSQLXML ();
    }

    public boolean isValid (int timeout) throws SQLException {
        return conn.isValid (timeout);
    }

    public boolean isClosed () throws SQLException {
        return conn.isClosed ();
    }

    public void abort (Executor executor) throws SQLException {
        conn.abort (executor);
    }

    public Savepoint setSavepoint (String name) throws SQLException {
        return conn.setSavepoint (name);
    }

    public void setCatalog (String catalog) throws SQLException {
        conn.setCatalog (catalog);
    }

    public String getCatalog () throws SQLException {
        return conn.getCatalog ();
    }

    public PreparedStatement prepareStatement (String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.prepareStatement (sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public SQLWarning getWarnings () throws SQLException {
        return conn.getWarnings ();
    }

    public String nativeSQL (String sql) throws SQLException {
        return conn.nativeSQL (sql);
    }

    public int getHoldability () throws SQLException {
        return conn.getHoldability ();
    }

    public void rollback (Savepoint savepoint) throws SQLException {
        timestamp = System.currentTimeMillis ();
        conn.rollback (savepoint);
    }

    public Blob createBlob () throws SQLException {
        return conn.createBlob ();
    }

    public void rollback () throws SQLException {
        timestamp = System.currentTimeMillis ();
        conn.rollback ();
    }

    public PreparedStatement prepareStatement (String sql, String[] columnNames) throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.prepareStatement (sql, columnNames);
    }

    public DatabaseMetaData getMetaData () throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.getMetaData ();
    }

    public Savepoint setSavepoint () throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.setSavepoint ();
    }

    public PreparedStatement prepareStatement (String sql, int[] columnIndexes) throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.prepareStatement (sql, columnIndexes);
    }

    public void setClientInfo (Properties properties) throws SQLClientInfoException {
        conn.setClientInfo (properties);
    }

    public Struct createStruct (String typeName, Object[] attributes) throws SQLException {
        return conn.createStruct (typeName, attributes);
    }

    public String getSchema () throws SQLException {
        return conn.getSchema ();
    }

    public Array createArrayOf (String typeName, Object[] elements) throws SQLException {
        return conn.createArrayOf (typeName, elements);
    }

    public void commit () throws SQLException {
        timestamp = System.currentTimeMillis ();
        conn.commit ();
    }

    public void clearWarnings () throws SQLException {
        conn.clearWarnings ();
    }

    public PreparedStatement prepareStatement (String sql) throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.prepareStatement (sql);
    }

    public Properties getClientInfo () throws SQLException {
        return conn.getClientInfo ();
    }

    public Statement createStatement (int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.createStatement (resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall (String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.prepareCall (sql, resultSetType, resultSetConcurrency);
    }

    public NClob createNClob () throws SQLException {
        return conn.createNClob ();
    }

    public void close () throws SQLException {
        conn.close ();
    }

    public CallableStatement prepareCall (String sql) throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.prepareCall (sql);
    }

    public void setAutoCommit (boolean autoCommit) throws SQLException {
        conn.setAutoCommit (autoCommit);
    }

    public PreparedStatement prepareStatement (String sql, int autoGeneratedKeys) throws SQLException {
        timestamp = System.currentTimeMillis ();
        return conn.prepareStatement (sql, autoGeneratedKeys);
    }

    public boolean isReadOnly () throws SQLException {
        return conn.isReadOnly ();
    }

    public Clob createClob () throws SQLException {
        return conn.createClob ();
    }

    public void setClientInfo (String name, String value) throws SQLClientInfoException {
        conn.setClientInfo (name, value);
    }

    public void setSchema (String schema) throws SQLException {
        conn.setSchema (schema);
    }

    public void setTypeMap (Map<String, Class<?>> map) throws SQLException {
        conn.setTypeMap (map);
    }

    public int getTransactionIsolation () throws SQLException {
        return conn.getTransactionIsolation ();
    }

    public <T> T unwrap (Class<T> iface) throws SQLException {
        return conn.unwrap (iface);
    }

    public void releaseSavepoint (Savepoint savepoint) throws SQLException {
        conn.releaseSavepoint (savepoint);
    }

    public void setTransactionIsolation (int level) throws SQLException {
        conn.setTransactionIsolation (level);
    }

    public int getNetworkTimeout () throws SQLException {
        return conn.getNetworkTimeout ();
    }

    public void setHoldability (int holdability) throws SQLException {
        conn.setHoldability (holdability);
    }

    public boolean isWrapperFor (Class<?> iface) throws SQLException {
        return conn.isWrapperFor (iface);
    }

    public void setReadOnly (boolean readOnly) throws SQLException {
        conn.setReadOnly (readOnly);
    }

    public String getClientInfo (String name) throws SQLException {
        return conn.getClientInfo (name);
    }

    @Override
    public boolean isTimedOut () {
        return System.currentTimeMillis () - timestamp > timeout;
    }
}