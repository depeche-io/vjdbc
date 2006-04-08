// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.SQLException;
import java.sql.SQLWarning;

/**
 * SQLExceptionHelper wraps driver-specific exceptions in a generic SQLException.
 */
public class SQLExceptionHelper {
    public static SQLException wrap(Throwable t) {
        return wrapThrowable(t, true);
    }

    private static SQLException wrapSQLException(SQLException e) {
        boolean returnOriginalException = true;
        
        // Check here if all chained SQLExceptions can be serialized, there may be
        // vendor specific SQLException classes which can't be delivered to the client
        SQLException loop = e;
        while(loop != null && returnOriginalException) {
            returnOriginalException = e.getClass().equals(SQLException.class) || e.getClass().equals(SQLWarning.class);
            loop = loop.getNextException();
        }
        
        if(returnOriginalException) {
            return e;
        }
        else {
            // When we can't return the original SQLException (as it contains a derived SQLException in its
            // chain) we simply wrap it in a generic way. We must suppress the check for SQLExceptions in
            // wrapThrowable as this causes a endless recursion and thus stack overflow.
            return wrapThrowable(e, false);
        }
    }
    
    private static SQLException wrapThrowable(Throwable t, boolean checkForSQLException) {
        // First check if the exception is already a SQLException
        if(checkForSQLException && t instanceof SQLException) {
            return wrapSQLException((SQLException)t);
        }
        // Then check if a cause is present
        else if(JavaVersionInfo.use14Api && t.getCause() != null) {
            return wrapThrowable(t.getCause(), true);
        }
        // Nothing to do, wrap the thing in a generic SQLException
        else {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            t.printStackTrace(pw);
            return new SQLException(sw.toString());
        }
    }
}
