// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc;

import de.simplicit.vjdbc.cache.TableCache;
import de.simplicit.vjdbc.command.*;
import de.simplicit.vjdbc.serial.*;
import de.simplicit.vjdbc.util.ClientInfo;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.sql.*;
import java.util.Map;
import java.util.Iterator;
import java.util.Properties;

public class VirtualConnection extends VirtualBase implements Connection {
    private static Log _logger = LogFactory.getLog(VirtualConnection.class);

    private static TableCache s_tableCache;
    private boolean _cachingEnabled = false;
    private Boolean _isAutoCommit = null;
    private Properties _connectionProperties;
    protected VirtualDatabaseMetaData _databaseMetaData;
    protected boolean _isClosed = false;

    public VirtualConnection(UIDEx reg, DecoratedCommandSink sink, Properties props, boolean cachingEnabled) {
        super(reg, sink);
        _connectionProperties = props;
        _cachingEnabled = cachingEnabled;
    }

    protected void finalize() throws Throwable {
        if(!_isClosed) {
            close();
        }
    }

    public Statement createStatement() throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "createStatement"), true);
        return new VirtualStatement(reg, this, _sink, ResultSet.TYPE_FORWARD_ONLY);
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        PreparedStatement pstmt = null;

        if(_cachingEnabled) {
            if(s_tableCache == null) {
                String cachedTables = _connectionProperties.getProperty(VJdbcProperties.CACHE_TABLES);

                if(cachedTables != null) {
                    try {
                        s_tableCache = new TableCache(this, cachedTables);
                    } catch(SQLException e) {
                        _logger.error("Creation of table cache failed, disable caching", e);
                        _cachingEnabled = false;
                    }
                }
            }

            if(s_tableCache != null) {
                pstmt = s_tableCache.getPreparedStatement(sql);
            }
        }

        if(pstmt == null) {
            UIDEx reg = (UIDEx)_sink.process(_objectUid, new ConnectionPrepareStatementCommand(sql), true);
            pstmt = new VirtualPreparedStatement(reg, this, sql, _sink, ResultSet.TYPE_FORWARD_ONLY);
        }

        return pstmt;
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, new ConnectionPrepareCallCommand(sql), true);
        return new VirtualCallableStatement(reg, this, sql, _sink, ResultSet.TYPE_FORWARD_ONLY);
    }

    public String nativeSQL(String sql) throws SQLException {
        return (String)_sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "nativeSQL",
                new Object[]{sql},
                ParameterTypeCombinations.STR));
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setAutoCommit",
                new Object[]{autoCommit ? Boolean.TRUE : Boolean.FALSE},
                ParameterTypeCombinations.BOL));
        // Remember the auto-commit value to prevent unnecessary remote calls
        _isAutoCommit = Boolean.valueOf(autoCommit);
    }

    public boolean getAutoCommit() throws SQLException {
        if(_isAutoCommit == null) {
            boolean autoCommit = _sink.processWithBooleanResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getAutoCommit"));
            _isAutoCommit = Boolean.valueOf(autoCommit);
        }
        return _isAutoCommit.booleanValue();
    }

    public void commit() throws SQLException {
        _sink.processWithBooleanResult(_objectUid, new ConnectionCommitCommand());
    }

    public void rollback() throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "rollback"));
    }

    public void close() throws SQLException {
        if(_databaseMetaData != null) {
            _sink.process(_databaseMetaData._objectUid, new DestroyCommand(_databaseMetaData._objectUid, JdbcInterfaceType.DATABASEMETADATA));
            _databaseMetaData = null;
        }
        _sink.process(_objectUid, new DestroyCommand(_objectUid, JdbcInterfaceType.CONNECTION));
        _sink.close();
        _isClosed = true;
    }

    public boolean isClosed() throws SQLException {
        return _isClosed;
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        if(_databaseMetaData == null) {
            UIDEx reg = (UIDEx)_sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getMetaData"), true);
            _databaseMetaData = new VirtualDatabaseMetaData(this, reg, _sink);
        }
        return _databaseMetaData;
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setReadOnly",
                new Object[]{readOnly ? Boolean.TRUE : Boolean.FALSE},
                ParameterTypeCombinations.BOL));
    }

    public boolean isReadOnly() throws SQLException {
        return _sink.processWithBooleanResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "isReadOnly"));
    }

    public void setCatalog(String catalog) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setCatalog",
                new Object[]{catalog},
                ParameterTypeCombinations.STR));
    }

    public String getCatalog() throws SQLException {
        return (String)_sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getCatalog"));
    }

    public void setTransactionIsolation(int level) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setTransactionIsolation",
                new Object[]{new Integer(level)},
                ParameterTypeCombinations.INT));
    }

    public int getTransactionIsolation() throws SQLException {
        return _sink.processWithIntResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getTransactionIsolation"));
    }

    public SQLWarning getWarnings() throws SQLException {
        /*
        if(_sink.lastProcessedCommandKindOf(ConnectionCommitCommand.class) && _lastCommitWithoutWarning) {
            _anyWarnings = false;
            return null;
        } else {
            SQLWarning warnings = (SQLWarning)_sink.process(_objectUid, CommandPool.getReflectiveCommand("getWarnings"));
            // Remember if any warnings were reported
            _anyWarnings = warnings != null;
            return warnings;
        }
        */
        return null;
    }

    public void clearWarnings() throws SQLException {
        // Ignore the call if the previous getWarnings()-Call returned null
        /*
        if(_anyWarnings) {
            _sink.process(_objectUid, CommandPool.getReflectiveCommand("clearWarnings"));
        }
        */
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "createStatement",
                new Object[]{new Integer(resultSetType), new Integer(resultSetConcurrency)},
                ParameterTypeCombinations.INTINT), true);
        return new VirtualStatement(reg, this, _sink, resultSetType);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency)
            throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, new ConnectionPrepareStatementCommand(sql, resultSetType, resultSetConcurrency), true);
        return new VirtualPreparedStatement(reg, this, sql, _sink, resultSetType);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency) throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, new ConnectionPrepareCallCommand(sql, resultSetType, resultSetConcurrency), true);
        return new VirtualCallableStatement(reg, this, sql, _sink, resultSetType);
    }

    public Map getTypeMap() throws SQLException {
        return (Map)_sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getTypeMap"));
    }

    public void setTypeMap(Map map) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setTypeMap",
                new Object[]{map},
                ParameterTypeCombinations.MAP));
    }

    public void setHoldability(int holdability) throws SQLException {
        _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setHoldability",
                new Object[]{new Integer(holdability)},
                ParameterTypeCombinations.INT));
    }

    public int getHoldability() throws SQLException {
        return _sink.processWithIntResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getHoldability"));
    }

    public Savepoint setSavepoint() throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setSavepoint"), true);
        return new VirtualSavepoint(reg, _sink);
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setSavepoint",
                new Object[]{name},
                ParameterTypeCombinations.STR), true);
        return new VirtualSavepoint(reg, _sink);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        VirtualSavepoint vsp = (VirtualSavepoint)savepoint;
        _sink.process(_objectUid, new ConnectionRollbackWithSavepointCommand(vsp.getObjectUID().getUID()));
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        VirtualSavepoint vsp = (VirtualSavepoint)savepoint;
        _sink.process(_objectUid, new ConnectionReleaseSavepointCommand(vsp.getObjectUID().getUID()));
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "createStatement",
                new Object[]{new Integer(resultSetType),
                             new Integer(resultSetConcurrency),
                             new Integer(resultSetHoldability)},
                ParameterTypeCombinations.INTINTINT), true);
        return new VirtualStatement(reg, this, _sink, resultSetType);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, new ConnectionPrepareStatementCommand(sql, resultSetType, resultSetConcurrency, resultSetHoldability), true);
        return new VirtualPreparedStatement(reg, this, sql, _sink, resultSetType);
    }

    public CallableStatement prepareCall(String sql, int resultSetType,
                                         int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, new ConnectionPrepareCallCommand(sql, resultSetType, resultSetConcurrency, resultSetHoldability), true);
        return new VirtualCallableStatement(reg, this, sql, _sink, resultSetType);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, new ConnectionPrepareStatementExtendedCommand(sql, autoGeneratedKeys), true);
        return new VirtualPreparedStatement(reg, this, sql, _sink, ResultSet.TYPE_FORWARD_ONLY);
    }

    public PreparedStatement prepareStatement(String sql, int columnIndexes[]) throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, new ConnectionPrepareStatementExtendedCommand(sql, columnIndexes), true);
        return new VirtualPreparedStatement(reg, this, sql, _sink, ResultSet.TYPE_FORWARD_ONLY);
    }

    public PreparedStatement prepareStatement(String sql, String columnNames[]) throws SQLException {
        UIDEx reg = (UIDEx)_sink.process(_objectUid, new ConnectionPrepareStatementExtendedCommand(sql, columnNames), true);
        return new VirtualPreparedStatement(reg, this, sql, _sink, ResultSet.TYPE_FORWARD_ONLY);
    }

    /* start JDBC4 support */
    public Clob createClob() throws SQLException {
        return new SerialClob();
    }

    public Blob createBlob() throws SQLException {
        return new SerialBlob();
    }

    public NClob createNClob() throws SQLException {
        return new SerialNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return new SerialSQLXML();
    }

    class ValidRunnable implements Runnable {
        public volatile boolean finished = false;
        public void run() {
            try {
                _sink.processWithBooleanResult(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "isValid"));
                finished = true;
            } catch (SQLException sqle) {
                _logger.info(sqle.getMessage(), sqle);
            }
        }
    }

    public boolean isValid(int timeout) throws SQLException {

        if (timeout <= 0) {
            throw new SQLException("invalid timeout value " + timeout);
        }

        // Schedule the keep alive timer task
        ValidRunnable task = new ValidRunnable();
        Thread t = new Thread(task);
        long end = System.currentTimeMillis() + timeout;
        long diff = timeout;
        t.start();

        while (!task.finished && diff > 0) {
            try {
                Thread.sleep(diff);
            } catch (Exception e) {
            }
            diff = end - System.currentTimeMillis();
        }

        return !task.finished;
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        Properties clientProps = ClientInfo.getProperties(null);
        clientProps.put(name, value);
        try {
            _sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "setClientInfo",
                new Object[]{ name, value },
                ParameterTypeCombinations.STRSTR), true);
        } catch (SQLClientInfoException e) {
            throw e;
        } catch (SQLException sqle) {
            throw new SQLClientInfoException(null, sqle);
        }
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        Properties clientProps = ClientInfo.getProperties(null);
        Iterator it = properties.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            String value = properties.getProperty(key);
            setClientInfo(key, value);
        }
    }

    public String getClientInfo(String name) throws SQLException {

        Properties clientProps = ClientInfo.getProperties(null);
        String value = clientProps.getProperty(name);
        if (value != null) {
            return value;
        }
        String ret = (String)_sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getClientInfo",
                new Object[]{ name },
                ParameterTypeCombinations.STR), true);
        if (ret != null) {
            clientProps.setProperty(name, ret);
        }
        return ret;
    }

    public Properties getClientInfo() throws SQLException {
        Properties clientProps = ClientInfo.getProperties(null);
        if (clientProps != null && clientProps.size() > 1) {
            return clientProps;
        }
        Properties ret = (Properties)_sink.process(_objectUid, CommandPool.getReflectiveCommand(JdbcInterfaceType.CONNECTION, "getClientInfo"));
        Iterator it = ret.keySet().iterator();
        while (it.hasNext()) {
            String key = (String)it.next();
            String value = ret.getProperty(key);
            clientProps.setProperty(key, value);
        }
        return ret;
    }


    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return new SerialArray(typeName, elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return new SerialStruct(typeName, attributes);
    }

    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isAssignableFrom(VirtualConnection.class);
    }

    public <T> T unwrap(Class<T> iface) throws SQLException {
        return (T)this;
    }
    /* end JDBC4 support */
}
