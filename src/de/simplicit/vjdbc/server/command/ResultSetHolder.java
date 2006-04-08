//VJDBC - Virtual JDBC
//Written by Michael Link
//Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.command;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

import de.simplicit.vjdbc.serial.RowPacket;
import de.simplicit.vjdbc.serial.SerializableTransport;
import de.simplicit.vjdbc.server.config.ConnectionConfiguration;

/**
 * The ResultSetHolder is responsible to hold a reference to an open ResultSet.
 * It reads succeeding RowPackets in a Worker-Thread to immediately return a
 * result when nextRowPacket is called.
 */
public class ResultSetHolder {
    private Object _lock = new Object();
    private boolean _readerThreadIsRunning = false;

    private ResultSet _resultSet;
    private SerializableTransport _currentSerializedRowPacket;
    private ConnectionConfiguration _connectionConfiguration;
    private boolean _lastPartReached;
    private SQLException _lastOccurredException = null;

    ResultSetHolder(ResultSet resultSet, ConnectionConfiguration config, boolean lastPartReached) {
        _resultSet = resultSet;
        _connectionConfiguration = config;
        _lastPartReached = lastPartReached;
        if(!_lastPartReached) {
            readNextRowPacket();
        }
    }

    public ResultSetMetaData getMetaData() throws SQLException {
        synchronized (_lock) {
            return _resultSet.getMetaData();
        }
    }

    public void close() throws SQLException {
        synchronized (_lock) {
            _resultSet.close();
            _resultSet = null;
        }
    }

    public SerializableTransport nextRowPacket() throws SQLException {
        synchronized (_lock) {
            // If the reader thread is still running we must wait
            // for the lock to be released by the reader
            if(_readerThreadIsRunning) {
                try {
                    // Wait for the reader thread to finish
                    _lock.wait();
                } catch (InterruptedException e) {
                    _lastOccurredException = new SQLException("Reader thread interrupted");
                }
            }

            // If any exception occured in the worker thread it will
            // be delivered to the client as a normal SQL exception
            if(_lastOccurredException != null) {
                throw _lastOccurredException;
            }

            // Remember current row packet as the result
            SerializableTransport result = _currentSerializedRowPacket;
            // Start next reader thread
            readNextRowPacket();
            // Return the result
            return result;
        }
    }

    private void readNextRowPacket() {
        if(_resultSet != null && !_lastPartReached) {
            // Start the thread
            Thread readerThread = new Thread(new Runnable() {
                public void run() {
                    // Aquire lock immediately
                    synchronized (_lock) {
                        try {
                            // When the ResultSet is null here, the client closed the ResultSet concurrently right 
                            // after the upper check "_resultSet != null".
                            if(_resultSet != null) {
                                RowPacket rowPacket = new RowPacket(_connectionConfiguration.getRowPacketSize(), false);
                                // Populate the new RowPacket using the ResultSet
                                _lastPartReached = rowPacket.populate(_resultSet);
                                _currentSerializedRowPacket = new SerializableTransport(rowPacket, _connectionConfiguration.getCompressionModeAsInt(),
                                        _connectionConfiguration.getCompressionThreshold());
                            }
                        } catch (SQLException e) {
                            // Just remember the exception, it will be thrown at
                            // the next call to nextRowPacket
                            _lastOccurredException = e;
                        } finally {
                            _readerThreadIsRunning = false;
                            // Notify possibly waiting subsequent Readers
                            _lock.notify();
                        }
                    }
                }
            });
            // Start the reader immediately
            readerThread.start();
            // Set the flag that the reader thread is considered to be running.
            _readerThreadIsRunning = true;
        } else {
            _currentSerializedRowPacket = null;
        }
    }
}