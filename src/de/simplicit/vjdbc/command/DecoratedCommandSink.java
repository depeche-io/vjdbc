// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import de.simplicit.vjdbc.rmi.KeepAliveTimerTask;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.serial.UIDEx;

import java.sql.SQLException;
import java.util.Properties;
import java.util.Timer;

/**
 * The DecoratedCommandSink makes it easier to handle the CommandSink. It contains a number
 * of different utility methods which wrap parameters, unwrap results and so on. Additionally
 * it supports a Listener which is called before and after execution of the command.
 */
public class DecoratedCommandSink {
    private UIDEx _connectionUid;
    private CommandSink _targetSink;
    private CommandSinkListener _listener = new NullCommandSinkListener();
    private CallingContextFactory _callingContextFactory;
    private Timer _timer;

    public DecoratedCommandSink(UIDEx connuid, CommandSink sink, CallingContextFactory ctxFactory) {
        _connectionUid = connuid;
        _targetSink = sink;
        _callingContextFactory = ctxFactory;
        _timer = new Timer(true);
        
        // Schedule the keep alive timer task
        KeepAliveTimerTask task = new KeepAliveTimerTask(this);
        //TODO: make this configurable
        _timer.scheduleAtFixedRate(task, 10000, 10000);
    }
    
    public void close() {
        // Stop the keep-alive timer
        _timer.cancel();
        // Close down the sink
        _targetSink.close();
    }

    public void setListener(CommandSinkListener listener) {
        if(listener != null) {
            _listener = listener;
        } else {
            _listener = new NullCommandSinkListener();
        }
    }

    public UIDEx connect(String url, Properties props, Properties clientInfo, CallingContext ctx) throws SQLException {
        return _targetSink.connect(url, props, clientInfo, ctx);
    }

    public Object process(UIDEx reg, Command cmd) throws SQLException {
        return process(reg, cmd, false);
    }
    
    public Object process(UIDEx reg, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = _callingContextFactory.create();
            }
            _listener.preExecution(cmd);
            return _targetSink.process(_connectionUid != null ? _connectionUid.getUID() : null,
                                       reg != null ? reg.getUID() : null, cmd, ctx);
        } finally {
            _listener.postExecution(cmd);
        }
    }

    public int processWithIntResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithIntResult(uid, cmd, false);
    }
    
    public int processWithIntResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = _callingContextFactory.create();
            }
            _listener.preExecution(cmd);
            Integer n = (Integer)_targetSink.process(_connectionUid.getUID(), uid.getUID(), cmd, ctx);
            return n.intValue();
        } finally {
            _listener.postExecution(cmd);
        }
    }

    public boolean processWithBooleanResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithBooleanResult(uid, cmd, false);
    }
    
    public boolean processWithBooleanResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = _callingContextFactory.create();
            }
            _listener.preExecution(cmd);
            Boolean b = (Boolean)_targetSink.process(_connectionUid.getUID(), uid.getUID(), cmd, ctx);
            return b.booleanValue();
        } finally {
            _listener.postExecution(cmd);
        }
    }

    public byte processWithByteResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithByteResult(uid, cmd, false);
    }
    
    public byte processWithByteResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = _callingContextFactory.create();
            }
            _listener.preExecution(cmd);
            Byte b = (Byte)_targetSink.process(_connectionUid.getUID(), uid.getUID(), cmd, ctx);
            return b.byteValue();
        } finally {
            _listener.postExecution(cmd);
        }
    }

    public short processWithShortResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithShortResult(uid, cmd, false);
    }
    
    public short processWithShortResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = _callingContextFactory.create();
            }
            _listener.preExecution(cmd);
            Short b = (Short)_targetSink.process(_connectionUid.getUID(), uid.getUID(), cmd, ctx);
            return b.shortValue();
        } finally {
            _listener.postExecution(cmd);
        }
    }

    public long processWithLongResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithLongResult(uid, cmd, false);
    }
    
    public long processWithLongResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = _callingContextFactory.create();
            }
            _listener.preExecution(cmd);
            Long b = (Long)_targetSink.process(_connectionUid.getUID(), uid.getUID(), cmd, ctx);
            return b.longValue();
        } finally {
            _listener.postExecution(cmd);
        }
    }

    public float processWithFloatResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithFloatResult(uid, cmd, false);
    }
    
    public float processWithFloatResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = _callingContextFactory.create();
            }
            _listener.preExecution(cmd);
            Float b = (Float)_targetSink.process(_connectionUid.getUID(), uid.getUID(), cmd, ctx);
            return b.floatValue();
        } finally {
            _listener.postExecution(cmd);
        }
    }

    public double processWithDoubleResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithDoubleResult(uid, cmd, false);
    }
    
    public double processWithDoubleResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = _callingContextFactory.create();
            }
            _listener.preExecution(cmd);
            Double b = (Double)_targetSink.process(_connectionUid.getUID(), uid.getUID(), cmd, ctx);
            return b.doubleValue();
        } finally {
            _listener.postExecution(cmd);
        }
    }
    
    public String processWithStringResult(UIDEx uid, Command cmd) throws SQLException {
        return processWithStringResult(uid, cmd, false);
    }
    
    public String processWithStringResult(UIDEx uid, Command cmd, boolean withCallingContext) throws SQLException {
        try {
            CallingContext ctx = null;
            if(withCallingContext) {
                ctx = _callingContextFactory.create();
            }
            _listener.preExecution(cmd);
            return (String)_targetSink.process(_connectionUid.getUID(), uid.getUID(), cmd, ctx);
        } finally {
            _listener.postExecution(cmd);
        }
    }
}
