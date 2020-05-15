package org.dreamwork.db;

import java.io.Closeable;
import java.sql.SQLException;

/**
 * 提供有限事务支持
 *
 * Created by seth.yang on 2020/5/15
 */
public interface ITransaction extends IUpdatable, Closeable {
    /**
     * 提交事务
     * @throws SQLException 任何数据库异常
     */
    void commit () throws SQLException;

    /**
     * 回滚事务
     * @throws SQLException 任何数据库异常
     */
    void rollback () throws SQLException;
}