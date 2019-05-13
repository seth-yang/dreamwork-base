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

/**
 * Created by seth.yang on 2017/4/19
 */
public interface IDatabase {
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

    int executeUpdate (String sql);
    int executeUpdate (String sql, Object... args);

    void clear (String tableName);
    void clear (Class<?> type);

    /**
     * {@link #save(Object, boolean) save(Object, true)}的快捷版本
     * @param item 需要保存的对象
     * @see #save(Object, boolean)
     */
    void save (Object item);

    /**
     * 保存对象.
     * <p>若<code>fetchPK</code>取值<code>true</code>，<strong>且</strong>, 数据库中主键字段为自增类型时，
     * 保存后将数据库产生的主键值自动填充到对象中。
     * </p>
     * @param item    需保存的对象
     * @param fetchPK 是否获取自动生成的主键值
     */
    void save (Object item, boolean fetchPK);

    /**
     * 批量保存对象.
     *
     * <p>该方法是{@link #save(Collection, boolean) save (items, false)}的快捷方式</p>
     * @param items 需保存的对象
     *
     * @see #save(Object)
     * @see #save(Object, boolean)
     * @see #save(Collection, boolean)
     */
    void save (Collection<?> items);

    /**
     * 批量保存对象.
     *
     * <p>若 <code>fetchPK</code> 取值为 <code>true</code>时，将影响该功能的执行效率
     * 特别地，当 <i>items.size() > 1000</i>时，效率下降严重，
     * 当<i>items.size() > 10000</i>时，可能造成数据的长时间阻塞。</p>
     * <p>当数量巨大时，应避免<code>fetchPK</code>参数取值<code>true</code></p>
     * @param items   需保存的对象
     * @param fetchPK 是否获取数据库生成的主键.
     * @see #save(Object)
     * @see #save(Object, boolean)
     * @see #save(Collection)
     */
    void save (Collection<?> items, boolean fetchPK);

    <T> T getByPK (Class<T> type, Serializable key);
    <T> T getSingle (Class<T> type, String sql, Object... args);

    <T> List<T> get (Class<T> type);
    <T> List<T> get (Class<T> type, String selection, Object... args);
    <T> List<T> get (Class<T> type, String selection, String order, Object... args);
    <T> List<T> get (Class<T> type, int pageNo, int pageSize);
    <T> List<T> get (Class<T> type, int pageNo, int pageSize, String selection, Object... args);
    <T> List<T> get (Class<T> type, int pageNo, int pageSize, String selection, String order, Object... args);

    void update (Object... o);

    void delete (Object o);
    void delete (Class<?> type, Serializable... pk);
    void delete (Class<?> type, Collection<? extends Serializable> pk);
}