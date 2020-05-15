package org.dreamwork.db;

import org.dreamwork.persistence.DatabaseSchema;

import java.io.IOException;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

/**
 * Created by seth.yang on 2020/5/15
 */
class SimpleTransactionImpl implements ITransaction {
    private final Connection conn;
    private final InternalDatabaseImpl db;

    SimpleTransactionImpl (Connection conn) throws SQLException {
        this.conn = conn;
        this.db   = new InternalDatabaseImpl (conn);
        conn.setAutoCommit (false);
    }

    @Override
    public void commit () throws SQLException {
        conn.commit ();
    }

    @Override
    public void rollback () throws SQLException {
        conn.rollback ();
    }

    @Override
    public void save (Object item) {
        db.save (item);
    }

    @Override
    public void save (Object item, boolean fetchPK) {
        db.save (item, fetchPK);
    }

    @Override
    public void save (Collection<?> items) {
        db.save (items);
    }

    @Override
    public void save (Collection<?> items, boolean fetchPK) {
        db.save (items, fetchPK);
    }

    @Override
    public void update (Object... o) {
        db.update (o);
    }

    @Override
    public void delete (Object o) {
        db.update (o);
    }

    @Override
    public void delete (Class<?> type, Serializable... pk) {
        db.delete (type, pk);
    }

    @Override
    public void delete (Class<?> type, Collection<? extends Serializable> pk) {
        db.delete (type, pk);
    }

    @Override
    public int executeUpdate (String sql) {
        return db.executeUpdate (sql);
    }

    @Override
    public int executeUpdate (String sql, Object... args) {
        return db.executeUpdate (sql, args);
    }

    @Override
    public void close () throws IOException {
        try {
            conn.close ();
        } catch (SQLException ex) {
            throw new IOException (ex);
        }
    }

    private static final class InternalDatabaseImpl extends AbstractDatabase {
        private final Connection conn;
        private InternalDatabaseImpl (Connection conn) {
            this.conn = conn;
        }

        @Override
        protected Connection connect () {
            return conn;
        }

        @Override
        public void save (Object item, boolean fetchPK) {
            doSave (conn, item, fetchPK);
        }

        @Override
        public void save (Collection<?> items, boolean fetchPK) {
            if (items == null) {
                throw new NullPointerException ();
            }

            if (items.isEmpty ()) {
                return;
            }

            try {
                Class<?> type = items.iterator ().next ().getClass ();
                DatabaseSchema schema = ref.map (type);
                String sql = buildInsertSQL (type);
                Map<String, AbstractDatabase.Metadata> mds = getMetadata (schema, conn);

                PreparedStatement pstmt;
                if (fetchPK) {
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
                        pstmt.executeBatch ();
                        index = 0;
                    }
                }

                if (logger.isTraceEnabled ()) {
                    logger.trace ("executing prepared statement: {}", pstmt);
                }

                if (!fetchPK && index > 0) {
                    pstmt.executeBatch ();
                }
            } catch (Exception ex) {
                throw new RuntimeException (ex);
            }
        }

        @Override
        public void update (Object... o) {
            if (o == null) {
                throw new NullPointerException ();
            }

            if (o.length == 0) {
                return;
            }

            try {
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
        public void delete (Class<?> type, Serializable... pk) {
            try {
                delete (conn, type, pk);
            } catch (Exception ex) {
                throw new RuntimeException (ex);
            }
        }

        @Override
        public int executeUpdate (String sql, Object... args) {
            try {
                if (logger.isTraceEnabled ()) {
                    logger.trace ("\nexecuting sql: " + sql + "\n" +
                            "parameters   : [" + Arrays.toString (args) + "]");
                }
                return executeUpdate (conn, sql, args);
            } catch (SQLException ex) {
                throw new RuntimeException (ex);
            }
        }

        @Override
        public int executeUpdate (String sql) {
            try {
                logger.debug ("executing sql: " + sql);
                PreparedStatement pstmt = conn.prepareStatement (sql);
                if (logger.isTraceEnabled ()) {
                    logger.trace ("executing prepared statement: {}", pstmt);
                }
                return pstmt.executeUpdate ();
            } catch (SQLException ex) {
                throw new RuntimeException (ex);
            }
        }
    }
}