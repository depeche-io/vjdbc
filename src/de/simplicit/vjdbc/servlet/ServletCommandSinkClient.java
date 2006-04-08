// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.servlet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.util.Properties;

import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.command.CommandSink;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.serial.UIDEx;
import de.simplicit.vjdbc.util.SQLExceptionHelper;

public class ServletCommandSinkClient implements CommandSink {
    private URL _url;

    public static String METHOD_IDENTIFIER = "vjdbc-method";
    public static String CONNECT_COMMAND = "connect";
    public static String PROCESS_COMMAND = "process";    

    public ServletCommandSinkClient(String url) throws SQLException {
        try {
            _url = new URL(url);
        } catch(IOException e) {
            throw SQLExceptionHelper.wrap(e);
        }
    }

    public UIDEx connect(String database, Properties props, Properties clientInfo, CallingContext ctx) throws SQLException {
        HttpURLConnection conn = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            // Open connection and adjust the Input/Output
            conn = (HttpURLConnection)_url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setAllowUserInteraction(false); // system may not ask the user
            conn.setUseCaches(false);
            conn.setRequestProperty( "Content-type", "binary/x-java-serialized" );
            conn.setRequestProperty(ServletCommandSinkClient.METHOD_IDENTIFIER,
                                    ServletCommandSinkClient.CONNECT_COMMAND);
            // Write the parameter objects to the ObjectOutputStream
            oos = new ObjectOutputStream(conn.getOutputStream());
            oos.writeUTF(database);
            oos.writeObject(props);
            oos.writeObject(clientInfo);
            oos.writeObject(ctx);
            oos.flush();
            // Connect ...
            conn.connect();
            // Read the result object from the InputStream
            ois = new ObjectInputStream(conn.getInputStream());
            Object result = ois.readObject();
            // This might be a SQLException which must be rethrown
            if(result instanceof SQLException) {
                throw (SQLException)result;
            }
            else {
                return (UIDEx)result;
            }
        } catch(SQLException e) {
            throw e;
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        } finally {
            // Cleanup resources
            if(oos != null) {
                try {
                    oos.close();
                } catch (IOException e) { ; }
            }
            if(ois != null) {
                try {
                    ois.close();
                } catch (IOException e) { ; }
            }
            if(conn != null) {
                conn.disconnect();
            }
        }
    }

    public Object process(Long connuid, Long uid, Command cmd, CallingContext ctx) throws SQLException {
        HttpURLConnection conn = null;
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;

        try {
            conn = (HttpURLConnection)_url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod("POST");
            conn.setRequestProperty(ServletCommandSinkClient.METHOD_IDENTIFIER, ServletCommandSinkClient.PROCESS_COMMAND);
            conn.connect();

            oos = new ObjectOutputStream(conn.getOutputStream());
            oos.writeObject(connuid);
            oos.writeObject(uid);
            oos.writeObject(cmd);
            oos.writeObject(ctx);
            oos.flush();

            ois = new ObjectInputStream(conn.getInputStream());
            Object result = ois.readObject();
            if(result instanceof SQLException) {
                throw (SQLException)result;
            }
            else {
                return result;
            }
        } catch(SQLException e) {
            throw e;
        } catch(Exception e) {
            throw SQLExceptionHelper.wrap(e);
        } finally {
            // Cleanup resources
            if(oos != null) {
                try {
                    oos.close();
                } catch (IOException e) { ; }
            }
            if(ois != null) {
                try {
                    ois.close();
                } catch (IOException e) { ; }
            }
            if(conn != null) {
                conn.disconnect();
            }
        }
    }

    public void close() {
        // Nothing to do
    }
}
