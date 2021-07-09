package org.dreamwork.db;

import java.io.Serializable;
import java.util.Collection;

/**
 * Created by seth.yang on 2020/5/15
 */
public interface IUpdatable {
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
     * 特别地，当 <i>items.size() &gt; 1000</i>时，效率下降严重，
     * 当<i>items.size() &gt; 10000</i>时，可能造成数据的长时间阻塞。</p>
     * <p>当数量巨大时，应避免<code>fetchPK</code>参数取值<code>true</code></p>
     * @param items   需保存的对象
     * @param fetchPK 是否获取数据库生成的主键.
     * @see #save(Object)
     * @see #save(Object, boolean)
     * @see #save(Collection)
     */
    void save (Collection<?> items, boolean fetchPK);

    void update (Object... o);

    void delete (Object o);
    void delete (Class<?> type, Serializable... pk);
    void delete (Class<?> type, Collection<? extends Serializable> pk);

    int executeUpdate (String sql);
    int executeUpdate (String sql, Object... args);
}
