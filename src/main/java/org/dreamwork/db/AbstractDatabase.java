package org.dreamwork.db;

import org.dreamwork.concurrent.IManagedClosable;
import org.dreamwork.concurrent.ManagedObjectMonitor;
import org.dreamwork.persistence.DatabaseSchema;
import org.dreamwork.persistence.ISchemaField;
import org.dreamwork.persistence.ReflectUtil;
import org.dreamwork.util.ITypedMap;
import org.dreamwork.util.StringUtil;
import org.dreamwork.util.TypedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by seth.yang on 2017/4/19
 */
public abstract class AbstractDatabase implements IDatabase {
    protected ReflectUtil ref = new ReflectUtil ();
    protected boolean debug = false;
    protected boolean limitSupported = false;

    protected static final Logger logger = LoggerFactory.getLogger (AbstractDatabase.class);
    private static final Map<Class<?>, String> INSERT_SQL_MAP = new HashMap<> ();
    private static final ManagedObjectMonitor<ConnectionWrapper> monitor = new ManagedObjectMonitor<> ();
    private static final Set<IManagedClosable<?>> managedConnections = Collections.synchronizedSet (new HashSet<> ());

    static {
        monitor.setListener (new ManagedObjectMonitor.Listener () {
            @Override
            public void onClosed (IManagedClosable<?> imc) {
                if (logger.isTraceEnabled ()) {
                    logger.trace ("unclosed connection = {}", imc);
                    try {
                        logger.trace ("the close flag of database = {}", ((ConnectionWrapper) imc).isClosed ());
                    } catch (Exception ex) {
                        // ignore
                    }
                }
                logger.warn ("the connection is not close. it's very like memory leak.");
                managedConnections.remove (imc);
                monitor.remove ((ConnectionWrapper) imc);

                if (managedConnections.isEmpty ()) {
                    monitor.stop ();
                }
            }

            @Override
            public void onRemoved (IManagedClosable<?> imc) {
                managedConnections.remove (imc);
            }
        });
    }

    protected abstract Connection connect () throws SQLException;

    public boolean isDebug () {
        return debug;
    }

    public void setDebug (boolean debug) {
        this.debug = debug;
    }

    public boolean isLimitSupported () {
        return limitSupported;
    }

    @Override
    public Connection getConnection () throws SQLException {
        Connection conn = connect ();
        if (conn != null) {
            if (managedConnections.isEmpty ()) {
                monitor.start ();
            }

            ConnectionWrapper wrapper = new ConnectionWrapper (conn);
            monitor.add (wrapper);
            managedConnections.add (wrapper);
            wrapper.setMonitor (monitor);
            return wrapper;
        }

        throw new NullPointerException ();
    }

    public Connection getConnection (int timeout, TimeUnit unit) throws SQLException {
        Connection conn = connect ();
        if (conn != null) {
            if (managedConnections.isEmpty ()) {
                monitor.start ();
            }

            ConnectionWrapper wrapper = new ConnectionWrapper (conn, timeout, unit);
            monitor.add (wrapper);
            managedConnections.add (wrapper);
            return wrapper;
        }

        throw new NullPointerException ();
    }

    @Override
    public boolean execute (String sql) {
        try (Connection conn = connect ()) {
            Statement stmt = conn.createStatement ();
            if (debug) {
                logger.debug ("executing sql: " + sql);
            }
            return stmt.execute (sql);
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public boolean execute (File script) {
        try (InputStream in = new FileInputStream (script)) {
            return execute (in);
        } catch (IOException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public boolean execute (URL url) {
        try (InputStream in = url.openStream ()) {
            return execute (in);
        } catch (IOException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public boolean execute (InputStream in) {
        try {
            List<String> list = new ScriptParser (in).parse ();
            try (Connection conn = connect ()) {
                Statement stmt = conn.createStatement ();
                for (String sql : list) {
                    if (debug) {
                        logger.debug ("executing statement: " + sql);
                    }
                    stmt.execute (sql);
                }
            } catch (SQLException ex) {
                throw new RuntimeException (ex);
            }
        } catch (IOException ex) {
            throw new RuntimeException (ex);
        }
        return true;
    }

    @Override
    public int executeScale (String sql) {
        try (Connection conn = connect ()) {
            PreparedStatement pstmt = conn.prepareStatement (sql);
            ResultSet rs = pstmt.executeQuery ();
            if (rs.next ()) {
                return rs.getInt (1);
            }
            return -1;
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public int executeScale (String sql, Object... args) {
        try (Connection conn = connect ()) {
            PreparedStatement pstmt = conn.prepareStatement (sql);
            for (int i = 0; i < args.length; i ++) {
                pstmt.setObject (i + 1, args[i]);
            }
            if (logger.isTraceEnabled ()) {
                logger.trace (pstmt.toString ());
            }
            ResultSet rs = pstmt.executeQuery ();
            if (rs.next ()) {
                return rs.getInt (1);
            }
            return -1;
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public <T> boolean exists (Class<T> type, Object pk) {
        DatabaseSchema schema = ref.map (type);
        if (schema == null || StringUtil.isEmpty (schema.getPrimaryKeyName ())) {
            return false;
        }

        String sql = "SELECT 1 FROM " + schema.getTableName () + " WHERE " + schema.getPrimaryKeyName () + " = ?";
        return exists (sql, pk);
    }

    @Override
    public boolean exists (String sql, Object... args) {
        try (Connection conn = connect ()) {
            PreparedStatement pstmt = conn.prepareStatement (sql);
            for (int i = 0; i < args.length; i ++) {
                pstmt.setObject (i + 1, args[i]);
            }
            if (logger.isTraceEnabled ()) {
                logger.trace (pstmt.toString ());
            }
            ResultSet rs = pstmt.executeQuery ();
            return rs.next ();
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    @SuppressWarnings ("unchecked")
    public <T> T getSingleField (Class<T> type, String sql, Object... args) {
        try (Connection conn = connect ()) {
            if (debug) {
                logger.debug ("executing sql: " + sql + "\n" +
                              "parameters   : [" + Arrays.toString (args) + "]");
            }
            PreparedStatement pstmt = conn.prepareStatement (sql);
            for (int i = 0; i < args.length; i ++) {
                pstmt.setObject (i + 1, args[i]);
            }
            if (logger.isTraceEnabled ()) {
                logger.trace ("executing statement: {}", pstmt);
            }
            ResultSet rs = pstmt.executeQuery ();
            if (rs.next ()) {
                if (type == int.class || type == Integer.class) {
                    return (T) Integer.valueOf (rs.getInt (1));
                }
                if (type == long.class || type == Long.class) {
                    return (T) Long.valueOf (rs.getLong (1));
                }
                if (type == double.class || type == Double.class) {
                    return (T) Double.valueOf (rs.getDouble (1));
                }
                if (type == float.class || type == Float.class) {
                    return (T) Float.valueOf (rs.getFloat (1));
                }
                if (type == boolean.class || type == Boolean.class) {
                    return (T) Boolean.valueOf (rs.getBoolean (1));
                }
                if (type == BigDecimal.class) {
                    return (T) rs.getBigDecimal (1);
                }
                if (type == Date.class || type == Timestamp.class) {
                    return (T) rs.getTimestamp (1);
                }
                if (type == java.sql.Date.class) {
                    return (T) rs.getDate (1);
                }
                if (type.isAssignableFrom (String.class)) {
                    return (T) rs.getString (1);
                }
                if (type.isEnum ()) {
                    String value = rs.getString (1);
                    Class<Enum> et = (Class<Enum>) type;
                    return (T) Enum.valueOf (et, value);
                }
                try {
                    return rs.getObject (1, type);
                } catch (SQLFeatureNotSupportedException ex) {
                    return null;
                }
            }
            return null;
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public void createSchemas () throws SQLException {
        try (Connection conn = connect ()) {
            Statement stmt = conn.createStatement ();
            for (DatabaseSchema schema : DatabaseSchema.MAP.values ()) {
                if (debug) {
                    logger.debug ("executing sql: " + schema.getCreateDDL ());
                }
                stmt.execute (schema.getCreateDDL ());
            }
        }
    }

    @Override
    public boolean isTablePresent (String tableName) {
        try (Connection conn = connect ()) {
            DatabaseMetaData dmd = conn.getMetaData ();
            ResultSet rs = dmd.getTables ("", null, tableName, new String[] {"TABLE"});
            if (rs.next ()) {
                if (debug) {
                    logger.debug (rs.getString ("TABLE_NAME"));
                }
                return true;
            }

            return false;
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public boolean isTablePresent (Class<?> type) {
        if (type == null) {
            throw new NullPointerException ();
        }

        DatabaseSchema schema = ref.map (type);
        return isTablePresent (schema.getTableName ());
    }

    @Override
    public ResultSet executeQuery (String sql) {
        Connection conn;
        try {
            conn = getConnection ();
            Statement stmt = conn.createStatement ();
            if (debug) {
                logger.debug ("executing sql: " + sql);
            }
            return new ResultSetWrapper (conn, stmt.executeQuery (sql));
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public ResultSet executeQuery (String sql, Object... args) {
        try {
            Connection conn = getConnection ();
            if (debug) {
                logger.debug ("\nexecuting sql: " + sql + "\n" +
                        "parameters   : [" + Arrays.toString (args) + "]");
            }
            PreparedStatement pstmt = conn.prepareStatement (sql);
            for (int i = 0; i < args.length; i ++) {
                pstmt.setObject (i + 1, args [i]);
            }
            if (logger.isTraceEnabled ()) {
                logger.trace ("executing prepared statement: {}", pstmt);
            }
            return new ResultSetWrapper (conn, pstmt.executeQuery ());
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public List<ITypedMap> list (String sql) {
        try (Connection conn = connect ()) {
            if (debug) {
                logger.debug ("executing sql: " + sql);
            }
            PreparedStatement pstmt = conn.prepareStatement (sql);
            if (logger.isTraceEnabled ()) {
                logger.trace ("executing prepared statement: {}", pstmt);
            }
            ResultSet rs = pstmt.executeQuery ();
            List<ITypedMap> list = new ArrayList<> ();
            while (rs.next ()) {
                list.add (build (rs));
            }
            return list;
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public List<ITypedMap> list (String sql, Object... args) {
        return list (sql, -1, -1, args);
    }

    @Override
    public List<ITypedMap> list (int pageNo, int pageSize, String sql, Object... args) {
        int start = (pageNo - 1) * pageSize;
        if (isLimitSupported ()) {
            sql += " LIMIT " + pageSize + " OFFSET " + start;
            return list (sql, args);
        } else {
            return list (pageNo, pageSize, sql, args);
        }
    }

    @Override
    public <T> List<T> list (Class<T> type, String sql) {
        try (Connection conn = connect ()) {
            if (debug) {
                logger.debug ("executing sql: " + sql);
            }
            ResultSet rs = executeQuery (conn, sql, false);
            List<T> list = new ArrayList<> ();
            while (rs.next ()) {
                list.add (build (rs, type));
            }
            return list;
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    private <T> List<T> list (Class<T> type, String sql, int pageNo, int pageSize, Object... args) {
        int start = 0;
        boolean scrollable = (pageNo > 0 && pageSize > 0);
        if (scrollable) {
            start = (pageNo - 1) * pageSize;
        }
        try (Connection conn = connect ()) {
            if (debug) {
                logger.debug ("\nexecuting sql: " + sql + "\n" +
                        "parameters   : [" + Arrays.toString (args) + "]");
            }
            ResultSet rs = executeQuery (conn, sql, scrollable, args);
            if (scrollable) {
                rs.absolute (start);
                rs.setFetchSize (pageSize);
            }
            List<T> list = new ArrayList<> ();
            if (scrollable) {
                int current = 0;
                while (rs.next () && current < pageSize) {
                    list.add (build (rs, type));
                    current ++;
                }
            } else {
                while (rs.next ()) {
                    list.add (build (rs, type));
                }
            }
            return list;
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public <T> List<T> list (Class<T> type, String sql, Object... args) {
        return list (type, sql, -1, -1, args);
    }

    @Override
    public <T> List<T> list (Class<T> type, int pageNo, int pageSize, String sql, Object... args) {
        if (isLimitSupported ()) {
            int start = (pageNo - 1) * pageSize;
            sql += " LIMIT " + pageSize + " OFFSET " + start;
            return list (type, sql, args);
        } else {
            return list (type, sql, pageNo, pageSize, args);
        }
    }

    @Override
    public int executeUpdate (String sql) {
        try (Connection conn = connect ()) {
            if (debug) {
                logger.debug ("executing sql: " + sql);
            }
            PreparedStatement pstmt = conn.prepareStatement (sql);
            if (logger.isTraceEnabled ()) {
                logger.trace ("executing prepared statement: {}", pstmt);
            }
            return pstmt.executeUpdate ();
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public int executeUpdate (String sql, Object... args) {
        try (Connection conn = connect ()) {
            if (debug) {
                logger.debug ("\nexecuting sql: " + sql + "\n" +
                        "parameters   : [" + Arrays.toString (args) + "]");
            }
            return executeUpdate (conn, sql, args);
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    protected int executeUpdate (Connection conn, String sql, Object... args) throws SQLException {
        PreparedStatement pstmt = conn.prepareStatement (sql);
        for (int i = 0; i < args.length; i ++) {
            pstmt.setObject (i + 1, args [i]);
        }
        if (logger.isTraceEnabled ()) {
            logger.trace ("executing prepared statement: {}", pstmt);
        }
        return pstmt.executeUpdate ();
    }

    @Override
    public void clear (String tableName) {
        execute ("TRUNCATE TABLE " + tableName);
    }

    @Override
    public void clear (Class<?> type) {
        DatabaseSchema schema = ref.map (type);
        clear (schema.getTableName ());
    }

    @Override
    public void save (Object item) {
        save (item, true);
    }

    protected void doSave (Connection conn, Object item, boolean fetchPK) {
        if (item == null) {
            throw new NullPointerException ();
        }

        try {
            Class<?> type = item.getClass ();
            DatabaseSchema schema = ref.map (type);
            Map<String, Metadata> mds = getMetadata (schema, conn);

            PreparedStatement pstmt;
            String sql = buildInsertSQL (type);
            if (fetchPK) {
                pstmt = conn.prepareStatement (sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                pstmt = conn.prepareStatement (sql);
            }
            setParameter (pstmt, item, mds);
            if (logger.isTraceEnabled ()) {
                logger.trace ("executing prepared statement: {}", pstmt);
            }
            pstmt.executeUpdate ();

            if (fetchPK) {
                fetchPK (pstmt, schema, type, item);
            }
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public void save (Object item, boolean fetchPK) {
        try (Connection conn = connect ()) {
            doSave (conn, item, fetchPK);
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
/*
        if (item == null) {
            throw new NullPointerException ();
        }

        try (Connection conn = connect ()) {
            Class<?> type = item.getClass ();
            DatabaseSchema schema = ref.map (type);
            Map<String, Metadata> mds = getMetadata (schema, conn);

            PreparedStatement pstmt;
            String sql = buildInsertSQL (type);
            if (fetchPK) {
                pstmt = conn.prepareStatement (sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                pstmt = conn.prepareStatement (sql);
            }
            setParameter (pstmt, item, mds);
            if (logger.isTraceEnabled ()) {
                logger.trace ("executing prepared statement: {}", pstmt);
            }
            pstmt.executeUpdate ();

            if (fetchPK) {
                fetchPK (pstmt, schema, type, item);
            }
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
*/
    }

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
    @Override
    public void save (Collection<?> items) {
        save (items, false);
    }

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
    @Override
    public void save (Collection<?> items, boolean fetchPK) {
        if (items == null) {
            throw new NullPointerException ();
        }

        if (items.isEmpty ()) {
            return;
        }

        Connection conn = null;
        try {
            conn = connect ();
            Class<?> type = items.iterator ().next ().getClass ();
            DatabaseSchema schema = ref.map (type);
            String sql = buildInsertSQL (type);
            Map<String, Metadata> mds = getMetadata (schema, conn);

            PreparedStatement pstmt;
            if (fetchPK) {
                conn.setAutoCommit (false);
                pstmt = conn.prepareStatement (sql, Statement.RETURN_GENERATED_KEYS);
            } else {
                pstmt = conn.prepareStatement (sql);
            }

            int index = 0;
            for (Object o : items) {
                if (o == null) {
                    throw new NullPointerException ();
                }

                setParameter (pstmt, o, mds);
                if (fetchPK) {
                    pstmt.executeUpdate ();
                    fetchPK (pstmt, schema, type, o);
                } else {
                    pstmt.addBatch ();
                }

                index ++;

                if (index > 1000) {
                    if (fetchPK) {
                        conn.commit ();
                    } else {
                        pstmt.executeBatch ();
                    }
                    index = 0;
                }
            }

            if (logger.isTraceEnabled ()) {
                logger.trace ("executing prepared statement: {}", pstmt);
            }

            if (!fetchPK && index > 0) {
                pstmt.executeBatch ();
            }
            if (!conn.getAutoCommit ()) {
                conn.commit ();
            }
        } catch (Exception ex) {
            if (fetchPK) {
                try {
                    if (conn != null)
                        conn.rollback ();
                } catch (SQLException e) {
                    e.printStackTrace ();
                }
            }
            throw new RuntimeException (ex);
        } finally {
            if (conn != null) try {
                conn.close ();
            } catch (SQLException ex) {
                ex.printStackTrace ();
            }
        }
    }

    @Override
    public<T> T getByPK (Class<T> type, Serializable key) {
        try (Connection conn = connect ()) {
            DatabaseSchema schema = ref.map (type);
            String sql = schema.getQuerySQL () + " WHERE " + schema.getPrimaryKeyName () + " = ?";
            PreparedStatement pstmt = conn.prepareStatement (sql);
            pstmt.setObject (1, key);
            if (logger.isTraceEnabled ()) {
                logger.trace ("executing prepared statement: {}", pstmt);
            }
            ResultSet rs = pstmt.executeQuery ();
            if (rs.next ())
                return build (rs, type);
            return null;
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public <T> T getSingle (Class<T> type, String sql, Object... args) {
        try (Connection conn = connect ()) {
            PreparedStatement pstmt = conn.prepareStatement (sql);
            if (args != null) for (int i = 0; i < args.length; i ++) {
                pstmt.setObject (i + 1, args[i]);
            }
            if (logger.isTraceEnabled ()) {
                logger.trace ("executing prepared statement: {}", pstmt);
            }
            ResultSet rs = pstmt.executeQuery ();
            if (rs.next ()) {
                return build (rs, type);
            }

            return null;
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public <T> List<T> get (Class<T> type) {
        return get (type, null);
    }

    @Override
    public <T> List<T> get (Class<T> type, String selection, Object... args) {
        return get (type, selection, null, args);
    }

    @Override
    public <T> List<T> get (Class<T> type, String selection, String order, Object... args) {
        return get (type, selection, order, -1, -1, args);
    }

    @Override
    public <T> List<T> get (Class<T> type, int pageNo, int pageSize) {
        return get (type, pageNo, pageSize, null);
    }

    @Override
    public <T> List<T> get (Class<T> type, int pageNo, int pageSize, String selection, Object... args) {
        return get (type, pageNo, pageSize, selection, null, new Object[0]);
    }

    @Override
    public <T> List<T> get (Class<T> type, int pageNo, int pageSize, String selection, String order, Object... args) {
        if (isLimitSupported ()) {
            DatabaseSchema schema = ref.map (type);
            String sql = schema.getQuerySQL ();
            if (!StringUtil.isEmpty (selection)) {
                sql += " WHERE " + selection;
            }
            if (!StringUtil.isEmpty (order)) {
                sql += " ORDER BY " + order;
            }
            if (pageNo >= 0 && pageSize >= 0) {
                int start = (pageNo - 1) * pageSize;
                sql += " LIMIT " + pageSize + " OFFSET " + start;
            }
            return get (type, sql, args);
        } else {
            return get (type, selection, order, pageNo, pageSize, args);
        }
    }

    @Override
    public ITransaction beginTransaction () throws SQLException {
        return new SimpleTransactionImpl (getConnection ());
    }

    @Override
    public ITransaction beginTransaction (int timeout, TimeUnit unit) throws SQLException {
        return new SimpleTransactionImpl (getConnection (timeout, unit));
    }

    @Override
    public void update (Object... o) {
        if (o == null) {
            throw new NullPointerException ();
        }

        if (o.length == 0) {
            return;
        }

        try (Connection conn = connect ()) {
            Class<?> type = o[0].getClass ();
            DatabaseSchema schema = ref.map (type);
            Map<String, Metadata> mds = getMetadata (schema, conn);
            String sql = buildUpdateSql (schema);
            PreparedStatement pstmt = conn.prepareStatement (sql);
            int index = 0;
            for (Object e : o) {
                setParameters (pstmt, schema, e, type, mds);
                pstmt.addBatch ();

                if (++ index > 1000) {
                    if (logger.isTraceEnabled ()) {
                        logger.trace ("executing prepared statement: {}", pstmt);
                    }
                    pstmt.executeBatch ();
                    index = 0;
                }
            }

            if (index > 0) {
                if (logger.isTraceEnabled ()) {
                    logger.trace ("executing prepared statement: {}", pstmt);
                }
                pstmt.executeBatch ();
            }
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public void delete (Object o) {
        if (o == null) {
            throw new NullPointerException ();
        }

        Class<?> type = o.getClass ();
        DatabaseSchema schema = ref.map (type);
        Map<String, Field> map = ref.getTypedList (type);
        String pkName = schema.getPrimaryKeyName ();
        Field field = map.get (pkName);
        if (field == null) {
            throw new RuntimeException ("NoPrimaryKeyException");
        }
        if (!field.isAccessible ()) {
            field.setAccessible (true);
        }

        try {
            Object v = field.get (o);
            if (v != null) {
                if (!(v instanceof Serializable)) {
                    throw new RuntimeException ("only serializable PK supported.");
                }

                delete (type, (Serializable) v);
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    @Override
    public void delete (Class<?> type, Serializable... pk) {
        try (Connection conn = connect ()) {
            delete (conn, type, pk);
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }

    protected void delete (Connection conn, Class<?> type, Serializable... pk) throws SQLException {
        DatabaseSchema schema = ref.map (type);
        String sql = "DELETE FROM " + schema.getTableName () + " WHERE " + schema.getPrimaryKeyName ();
        if (pk.length > 1) {
            sql += " IN (";
            StringBuilder builder = new StringBuilder ();
            for (int i = 0; i < pk.length; i ++) {
                if ( i > 0 ) builder.append (", ");
                builder.append ("?");
            }
            sql += builder + ")";
        } else {
            sql += " = ?";
        }
        PreparedStatement pstmt = conn.prepareStatement (sql);
        for (int i = 0; i < pk.length; i ++) {
            pstmt.setObject (i + 1, pk[i]);
        }
        if (logger.isTraceEnabled ()) {
            logger.trace ("executing prepared statement: {}", pstmt);
        }
        pstmt.executeUpdate ();
    }

    @Override
    public void delete (Class<?> type, Collection<? extends Serializable> pk) {
        Serializable[] a = new Serializable[pk.size ()];
        a = pk.toArray (a);
        delete (type, a);
    }

    protected ITypedMap build (ResultSet rs) throws SQLException {
        ITypedMap map = new TypedHashMap ();
        ResultSetMetaData rsmd = rs.getMetaData ();
        int count = rsmd.getColumnCount ();
        for (int i = 1; i <= count; i ++) {
            String name = rsmd.getColumnName (i);
            int type = rsmd.getColumnType (i);
            switch (type) {
                case Types.BIGINT :
                    map.put (name, rs.getLong (i));
                    break;
                case Types.CHAR:
                case Types.NCHAR:
                case Types.VARCHAR:
                    map.put (name, rs.getString (i));
                    break;
                case Types.INTEGER:
                case Types.SMALLINT:
                case Types.TINYINT:
                    map.put (name, rs.getInt (i));
                    break;
                case Types.DOUBLE:
                    map.put (name, rs.getDouble (i));
                    break;
                case Types.FLOAT:
                case Types.REAL:
                    map.put (name, rs.getFloat (i));
                    break;
                case Types.NUMERIC:
                    map.put (name, rs.getBigDecimal (i));
                    break;
                case Types.BOOLEAN:
                    map.put (name, rs.getBoolean (i));
                    break;
                case Types.DATE:
                    map.put (name, rs.getDate (i));
                    break;
                case Types.TIME:
                    map.put (name, rs.getTime (i));
                    break;
                case Types.TIMESTAMP:
                    map.put (name, rs.getTimestamp (i));
                    break;
                default:
                    map.put (name, rs.getString (i));
                    break;
            }
        }

        return map;
    }

    protected <T> T build (ResultSet rs, Class<T> type) throws Exception {
        T o = type.newInstance ();
        ResultSetMetaData rsmd = rs.getMetaData ();
        int count = rsmd.getColumnCount ();
        Map<String, Field> map = ref.getTypedList (type);
        for (int i = 1; i <= count; i ++) {
            String name = rsmd.getColumnName (i);
            Field field = map.get (name);
            if (field == null) {
                continue;
            }

            Class<?> c = field.getType ();
            if (c.isEnum ()) {
                String v = rs.getString (i);
                if (!StringUtil.isEmpty (v)) {
                    @SuppressWarnings ("unchecked")
                    Object t = Enum.valueOf ((Class<? extends Enum>) c, v);
                    field.set (o, t);
                }
            } else {
                try {
                    field.set (o, rs.getObject (i, c));
                } catch (SQLFeatureNotSupportedException ex) {
                    legacySet (c, field, o, rs, i, rsmd);
                }
            }
        }

        return o;
    }

    private void legacySet (Class<?> c, Field field, Object o, ResultSet rs, int i, ResultSetMetaData rsmd) throws Exception {
            if (c.isAssignableFrom (String.class)) {
                field.set (o, rs.getString (i));
            } else if (c == int.class || c == Integer.class) {
                field.set (o, rs.getInt (i));
            } else if (c == boolean.class || c == Boolean.class) {
                String tmp = rs.getString (i);
                boolean value = false;
                if (!StringUtil.isEmpty (tmp)) {
                    tmp = tmp.trim ().toLowerCase ();
                    if ("t".equalsIgnoreCase (tmp) || "true".equalsIgnoreCase (tmp) ||
                            "yes".equalsIgnoreCase (tmp) || "y".equalsIgnoreCase (tmp)) {
                        value = true;
                    } else {
                        try {
                            int i_value = rs.getInt (i);
                            value = (i_value != 0);
                        } catch (Exception ex) {
                            // ignore
                        }
                    }
                    field.set (o, value);
                }
            } else if (c == long.class || c == Long.class) {
                field.set (o, rs.getLong (i));
            } else if (c == float.class || c == Float.class) {
                field.set (o, rs.getFloat (i));
            } else if (c == short.class || c == Short.class) {
                field.set (o, rs.getShort (i));
            } else if (c == double.class || c == Double.class) {
                field.set (o, rs.getDouble (i));
            } else if (c.isEnum ()) {
                String v = rs.getString (i);
                if (!StringUtil.isEmpty (v)) {
                    @SuppressWarnings ("unchecked")
                    Object t = Enum.valueOf ((Class<? extends Enum>) c, v);
                    field.set (o, t);
                }
            } else if (java.util.Date.class.isAssignableFrom (c)) {
                int sqlType = rsmd.getColumnType (i);
                switch (sqlType) {
                    case Types.TIMESTAMP :
                        field.set (o, rs.getTimestamp (i));
                        break;
                    case Types.DATE :
                        field.set (o, rs.getDate (i));
                        break;
                }
            }
    }

    protected void setParameter (PreparedStatement pstmt, Object item, Map<String, Metadata> mds) throws SQLException, IllegalAccessException {
        int index = 1;
        Class<?> type = item.getClass ();
        DatabaseSchema schema = ref.map (type);
        String[] fieldNames = schema.getFields ();
        Map<String, Field> fields = ref.getTypedList (type);
        for (String name : fieldNames) {
            Field field = fields.get (name);
            Metadata m  = mds.get (name);
            if (field.isAnnotationPresent (ISchemaField.class)) {
                ISchemaField isf = field.getAnnotation (ISchemaField.class);
                if (!isf.autoincrement ()) {
                    setParameter (pstmt, index ++, item, field, m.type);
                }
            }
        }
    }

    protected ResultSet executeQuery (Connection conn, String sql, boolean scrollable, Object... args) throws SQLException {
        PreparedStatement pstmt;
        if (scrollable)
            pstmt = conn.prepareStatement (sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        else
            pstmt = conn.prepareStatement (sql);
        if (args.length > 0) for (int i = 0; i < args.length; i ++) {
            pstmt.setObject (i + 1, args [i]);
        }
        if (logger.isTraceEnabled ()) {
            logger.trace ("executing prepared statement: {}", pstmt);
        }
        return pstmt.executeQuery ();
    }

    protected String buildUpdateSql (DatabaseSchema schema) {
        String[] fields = schema.getFields ();
        String pkName = schema.getPrimaryKeyName ();
        StringBuilder builder = new StringBuilder ();
        for (String name : fields) {
            if (name.equals (pkName)) continue;
            if (builder.length () > 0) builder.append (", ");
            builder.append (name).append (" = ?");
        }
        builder.append (" WHERE ").append (pkName).append (" = ?");
        return "UPDATE " + schema.getTableName () + " SET " + builder;
    }

    private void setParameter (PreparedStatement pstmt, int index, Object item, Field field, int type) throws IllegalAccessException, SQLException {
        Object v = field.get (item);
        if (v == null) {
            pstmt.setNull (index, type);
        } else {
            pstmt.setObject (index, v, type);
        }
    }

    protected void setParameters (PreparedStatement pstmt, DatabaseSchema schema, Object o, Class<?> type, Map<String, Metadata> mds) throws Exception {
        String pkName = schema.getPrimaryKeyName ();
        String[] fieldNames = schema.getFields ();
        List<String> ordered = new ArrayList<> (fieldNames.length);
        for (String name : fieldNames) {
            if (pkName.equals (name)) continue;

            ordered.add (name);
        }
        ordered.add (pkName);
        Map<String, Field> map = ref.getTypedList (type);
        for (int i = 0; i < ordered.size (); i ++) {
            String name = ordered.get (i);
            Field field = map.get (name);
            Metadata m  = mds.get (name);
            if (field == null) {
                pstmt.setObject (i + 1, null, m.type);
            } else {
                if (!field.isAccessible ()) {
                    field.setAccessible (true);
                }

                Object v = field.get (o);
                pstmt.setObject (i + 1, v, m.type);
            }
        }
    }

    protected Map<String, Metadata> getMetadata (DatabaseSchema schema, Connection conn) throws SQLException {
        String sql = "SELECT " + join (schema.getFields (), ',') + " FROM " + schema.getTableName () + " WHERE 1 = 2";
        Statement stmt = conn.createStatement ();
        ResultSet rs = stmt.executeQuery (sql);
        ResultSetMetaData rsmd = rs.getMetaData ();
        Map<String, Metadata> map = new HashMap<> ();
        for (int i = 1; i <= rsmd.getColumnCount (); i ++) {
            Metadata m = new Metadata ();
            m.name = rsmd.getColumnName (i);
            m.type = rsmd.getColumnType (i);
            m.autoIncrement = rsmd.isAutoIncrement (i);

            map.put (m.name, m);
        }
        return map;
    }

    protected void fetchPK (PreparedStatement pstmt, DatabaseSchema schema, Class<?> type, Object item) throws Exception {
        ResultSet rs = pstmt.getGeneratedKeys ();
        if (rs.next ()) {
            String fieldName = schema.getPrimaryKeyName ();
            Map<String, Field> map = ref.getTypedList (type);
            Field field = map.get (fieldName);
            if (!field.isAccessible ()) {
                field.setAccessible (true);
            }

            Class<?> fieldType = field.getType ();
            Object value;
            try {
                value = rs.getObject (1, fieldType);
                field.set (item, value);
            } catch (SQLFeatureNotSupportedException ex) {
                ResultSetMetaData rsmd = rs.getMetaData ();
                legacySet (fieldType, field, item, rs, 1, rsmd);
            }
        }
    }

    protected static final class Metadata {
        int type;
        String name;
        boolean autoIncrement;
    }

    private String join (String[] array, char ch) {
        StringBuilder builder = new StringBuilder ();
        for (String e : array) {
            if (builder.length () > 0) {
                builder.append (ch).append (' ');
            }
            builder.append (e);
        }
        return builder.toString ();
    }

    protected String buildInsertSQL (Class<?> type) {
        if (INSERT_SQL_MAP.containsKey (type)) {
            return INSERT_SQL_MAP.get (type);
        }

        DatabaseSchema schema     = ref.map (type);
        Map<String, Field> fields = ref.getTypedList (type);
        String[] fieldNames       = schema.getFields ();
        int index = 0;
        StringBuilder select = new StringBuilder (), values = new StringBuilder ();
        for (String name : fieldNames) {
            Field field = fields.get (name);
            if (field.isAnnotationPresent (ISchemaField.class)) {
                ISchemaField isf = field.getAnnotation (ISchemaField.class);
                if (isf.autoincrement ()) {
                    continue;
                }

                if (index != 0) {
                    select.append (", ");
                    values.append (", ");
                }
                select.append (isf.name ());
                values.append ("?");
                index ++;
            }
        }

        String sql = "INSERT INTO " + schema.getTableName () + "(" + select + ") VALUES (" + values + ")";
        INSERT_SQL_MAP.put (type, sql);
        return sql;
    }

    private Object[] getInsertParameters (DatabaseSchema schema, Object item, Class<?> type) throws IllegalAccessException {
        Map<String, Field> fields = ref.getTypedList (type);
        String[] fieldNames       = schema.getFields ();
        List<Object> list         = new ArrayList<> ();
        for (String name : fieldNames) {
            Field field = fields.get (name);
            if (field.isAnnotationPresent (ISchemaField.class)) {
                ISchemaField isf = field.getAnnotation (ISchemaField.class);
                if (isf.autoincrement ()) {
                    continue;
                }
            }

            list.add (field.get (item));
        }
        Object[] args = new Object[list.size ()];
        return list.toArray (args);
    }

    private List<ITypedMap> list (String sql, int pageNo, int pageSize, Object... args) {
        int start = 0;
        boolean scrollable = (pageNo > 0 && pageSize > 0);
        if (scrollable) {
            start = (pageNo - 1) * pageSize;
        }
        try (Connection conn = connect ()) {
            List<ITypedMap> list = new ArrayList<> ();
            if (debug) {
                logger.debug ("\nexecuting sql: " + sql + "\n" +
                        "parameters   : [" + Arrays.toString (args) + "]");
            }
            ResultSet rs = executeQuery (conn, sql, scrollable, args);
            if (scrollable) {
                rs.absolute (start);
                rs.setFetchSize (pageSize);
                int current = 0;
                while (rs.next () && current < pageSize) {
                    list.add (build (rs));
                    current ++;
                }
            } else {
                while (rs.next ()) {
                    list.add (build (rs));
                }
            }

            return list;
        } catch (SQLException ex) {
            throw new RuntimeException (ex);
        }
    }

    private  <T> List<T> get (Class<T> type, String selection, String order, int pageNo, int pageSize, Object... args) {
        int start = -1;
        boolean scrollable = (pageNo > 0 && pageSize > 0);
        if (scrollable) {
            start = (pageNo - 1) * pageSize;
        }

        try (Connection conn = connect ()) {
            DatabaseSchema schema = ref.map (type);
            String sql = schema.getQuerySQL ();
            if (!StringUtil.isEmpty (selection)) {
                sql += " WHERE " + selection;
            }
            if (!StringUtil.isEmpty (order)) {
                sql += " ORDER BY " + order;
            }
            PreparedStatement pstmt = scrollable ?
                    conn.prepareStatement (sql, ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY) :
                    conn.prepareStatement (sql);
            if (args != null && args.length > 0) {
                for (int i = 0; i < args.length; i ++) {
                    pstmt.setObject (i + 1, args[i]);
                }
            }
            List<T> list = new ArrayList<> ();
            if (logger.isTraceEnabled ()) {
                logger.trace ("executing prepared statement: {}", pstmt);
            }
            ResultSet rs = pstmt.executeQuery ();

            if (scrollable) {
                rs.setFetchSize (pageSize);
                rs.absolute (start);
                int current = 0;
                while (rs.next () && current < pageSize) {
                    list.add (build (rs, type));
                    current ++;
                }
            } else {
                while (rs.next ()) {
                    list.add (build (rs, type));
                }
            }
            return list;
        } catch (Exception ex) {
            throw new RuntimeException (ex);
        }
    }
}