// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.servlet;

import de.simplicit.vjdbc.command.Command;
import de.simplicit.vjdbc.serial.CallingContext;
import de.simplicit.vjdbc.server.command.CommandProcessor;
import de.simplicit.vjdbc.server.config.ConfigurationException;
import de.simplicit.vjdbc.server.config.ConnectionConfiguration;
import de.simplicit.vjdbc.server.config.VJdbcConfiguration;
import de.simplicit.vjdbc.servlet.ServletCommandSinkIdentifier;
import de.simplicit.vjdbc.util.SQLExceptionHelper;
import de.simplicit.vjdbc.util.StreamCloser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class EnvServletCommandSink extends HttpServlet {
    public static final String ENV_PREFIX = "VJDBC_";

    private static final long serialVersionUID = 3257570624301249846L;
    private static Log _logger = LogFactory.getLog(EnvServletCommandSink.class);

    private CommandProcessor _processor;

    public EnvServletCommandSink() {
    }

    public void init(ServletConfig servletConfig) throws ServletException {
        super.init(servletConfig);

        final String checkRaw = System.getenv(ENV_PREFIX + "CONNECTIONS");
        assert checkRaw != null : ENV_PREFIX + "CONNECTION is not defined";

        final VJdbcConfiguration conf = new VJdbcConfiguration();
        for (String connectionName : checkRaw.split(",")) {
            final ConnectionConfiguration conn = new ConnectionConfiguration();

            final String connPrefix = ENV_PREFIX + connectionName + "_";

            conn.setId(connectionName);
            conn.setDriver(System.getenv(connPrefix + "DRIVER"));
            conn.setUrl(System.getenv(connPrefix + "URL"));
            conn.setUser(System.getenv(connPrefix + "USER"));
            conn.setPassword(System.getenv(connPrefix + "PASSWORD"));
            conn.setConnectionPooling(false);
            try {
                conf.addConnection(conn);
            } catch (ConfigurationException e) {
                throw new RuntimeException("Configuration problem for connection " + connectionName, e);
            }
        }

        _logger.info("Initialize VJDBC-Configuration");
        VJdbcConfiguration.init(conf);
        _processor = CommandProcessor.getInstance();
    }

    public void destroy() {
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        handleRequest(httpServletRequest, httpServletResponse);
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        handleRequest(httpServletRequest, httpServletResponse);
    }

    private void handleRequest(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {
        ObjectInputStream ois = null;
        ObjectOutputStream oos = null;

        try {
            // Get the method to execute
            String method = httpServletRequest.getHeader(ServletCommandSinkIdentifier.METHOD_IDENTIFIER);

            if(method != null) {
                ois = new ObjectInputStream(httpServletRequest.getInputStream());
                // And initialize the output
                OutputStream os = httpServletResponse.getOutputStream();
                oos = new ObjectOutputStream(os);
                Object objectToReturn = null;

                try {
                    // Some command to process ?
                    if(method.equals(ServletCommandSinkIdentifier.PROCESS_COMMAND)) {
                        // Read parameter objects
                        Long connuid = (Long) ois.readObject();
                        Long uid = (Long) ois.readObject();
                        Command cmd = (Command) ois.readObject();
                        CallingContext ctx = (CallingContext) ois.readObject();
                        // Delegate execution to the CommandProcessor
                        objectToReturn = _processor.process(connuid, uid, cmd, ctx);
                    } else if(method.equals(ServletCommandSinkIdentifier.CONNECT_COMMAND)) {
                        String url = ois.readUTF();
                        Properties props = (Properties) ois.readObject();
                        Properties clientInfo = (Properties) ois.readObject();
                        CallingContext ctx = (CallingContext) ois.readObject();

                        ConnectionConfiguration connectionConfiguration = VJdbcConfiguration.singleton().getConnection(url);

                        if(connectionConfiguration != null) {
                            Connection conn = connectionConfiguration.create(props);
                            objectToReturn = _processor.registerConnection(conn, connectionConfiguration, clientInfo, ctx);
                        } else {
                            objectToReturn = new SQLException("VJDBC-Connection " + url + " not found");
                        }
                    }
                } catch (Throwable t) {
                    // Wrap any exception so that it can be transported back to
                    // the client
                    objectToReturn = SQLExceptionHelper.wrap(t);
                }

                // Write the result in the response buffer
                oos.writeObject(objectToReturn);
                oos.flush();

                httpServletResponse.flushBuffer();
            } else {
                // No VJDBC-Method ? Then we redirect the stupid browser user to
                // some information page :-)
                httpServletResponse.sendRedirect("index.html");
            }
        } catch (Exception e) {
            _logger.error("Unexpected Exception", e);
            throw new ServletException(e);
        } finally {
            StreamCloser.close(ois);
            StreamCloser.close(oos);
        }
    }
}