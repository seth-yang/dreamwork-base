package org.dreamwork.db;

import org.dreamwork.util.ITypedMap;

import java.io.File;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URL;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by seth.yang on 2017/4/19
 */
public interface IDatabase extends IUpdatable {
    Connection getConnection () throws SQLException;

    void createSchemas () throws SQLException;

    boolean isTablePresent (String tableName);
    boolean isTablePresent (Class<?> type);

    boolean execute (String sql);
    boolean execute (File script);
    boolean execute (URL url);
    boolean execute (InputStream in);

    <T> boolean exists (Class<T> type, Object pk);
    boolean exists (String sql, Object... args);

    <T> T getSingleField (Class<T> type, String sql, Object... args);

    ResultSet executeQuery (String sql);
    ResultSet executeQuery (String sql, Object... args);

    List<ITypedMap> list (String sql);
    List<ITypedMap> list (String sql, Object... args);
    List<ITypedMap> list (int pageNo, int pageSize, String sql, Object... args);

    <T> List<T> list (Class<T> type, String sql);
    <T> List<T> list (Class<T> type, String sql, Object... args);
    <T> List<T> list(Class<T> type, int pageNo, int pageSize, String sql, Object... args);

    void clear (String tableName);
    void clear (Class<?> type);



    <T> T getByPK (Class<T> type, Serializable key);
    <T> T getSingle (Class<T> type, String sql, Object... args);

    <T> List<T> get (Class<T> type);
    <T> List<T> get (Class<T> type, String selection, Object... args);
    <T> List<T> get (Class<T> type, String selection, String order, Object... args);
    <T> List<T> get (Class<T> type, int pageNo, int pageSize);
    <T> List<T> get (Class<T> type, int pageNo, int pageSize, String selection, Object... args);
    <T> List<T> get (Class<T> type, int pageNo, int pageSize, String selection, String order, Object... args);

    /**
     * 创建简单事务管理器，在 {@code 5分钟} 后若未 {@code commit} 将自动 {@code rollback} 事务
     * @return 事务管理器
     * @exception SQLException 无法获取数据库异常
     * @see #beginTransaction(int, TimeUnit)
     */
    ITransaction beginTransaction () throws SQLException;

    /**
     * 启动简单事务， 在 {@code timeout} 后若未 {@code commit} 事务的话，将自动 {@code rollback} 事务
     * @param timeout 超时时长
     * @param unit    时间单位
     * @return 简单事务管理器
     * @exception SQLException 无法获取数据库异常
     * @see #beginTransaction()
     */
    ITransaction beginTransaction (int timeout, TimeUnit unit) throws SQLException;
}