// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import org.apache.commons.digester.Digester;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Root configuration class. Can be initialized with different input objects
 * or be built up programmatically.
 */
public class VJdbcConfiguration {
    private static Log _logger = LogFactory.getLog(VJdbcConfiguration.class);
    private static VJdbcConfiguration _singleton;

    private OcctConfiguration _occtConfiguration = new OcctConfiguration();
    private RmiConfiguration _rmiConfiguration;
    private JBossRemotingConfiguration _jbossRemotingConfiguration;
    private List _connections = new ArrayList();

    /**
     * Initialization with pre-built configuration object.
     * @param customConfig
     */
    public static void init(VJdbcConfiguration customConfig) {
        if(_singleton != null) {
            _logger.warn("VJdbcConfiguration already initialized, init-Call is ignored");
        }
        else {
            _singleton = customConfig;
        }
    }

    /**
     * Initialization with resource.
     * @param resource Resource to be loaded by the ClassLoader
     * @throws ConfigurationException
     */
    public static void init(String resource) throws ConfigurationException {
        if(_singleton != null) {
            _logger.warn("VJdbcConfiguration already initialized, init-Call is ignored");
        } else {
            try {
                _singleton = new VJdbcConfiguration(resource);
                if(_logger.isInfoEnabled()) {
                    _singleton.log();
                }
            } catch(Exception e) {
                throw new ConfigurationException("VJdbc-Configuration failed", e);
            }
        }
    }

    /**
     * Initialization with pre-opened InputStream.
     * @param is InputStream
     * @throws ConfigurationException
     */
    public static void init(InputStream is) throws ConfigurationException {
        if(_singleton != null) {
            _logger.warn("VJdbcConfiguration already initialized, init-Call is ignored");
        } else {
            try {
                _singleton = new VJdbcConfiguration(is);
                if(_logger.isInfoEnabled()) {
                    _singleton.log();
                }
            } catch(Exception e) {
                String msg = "VJdbc-Configuration failed";
                _logger.error(msg, e);
                throw new ConfigurationException(msg, e);
            }
        }
    }

    /**
     * Accessor method to the configuration singleton.
     * @return Configuration object
     * @throws RuntimeException Thrown when accessing without being initialized
     * previously
     */
    public static VJdbcConfiguration singleton() {
        if(_singleton == null) {
            throw new RuntimeException("VJdbc-Configuration is not initialized !");
        }
        return _singleton;
    }

    /**
     * Constructor. Can be used for programmatical building the Configuration object.
     */
    public VJdbcConfiguration() {
    }

    public OcctConfiguration getOcctConfiguration() {
        return _occtConfiguration;
    }

    public void setOcctConfiguration(OcctConfiguration occtConfiguration) {
        _occtConfiguration = occtConfiguration;
    }

    /**
     * Returns the RMI-Configuration.
     * @return RmiConfiguration object or null
     */
    public RmiConfiguration getRmiConfiguration() {
        return _rmiConfiguration;
    }

    /**
     * Sets the RMI-Configuration object.
     * @param rmiConfiguration RmiConfiguration object to be used.
     */
    public void setRmiConfiguration(RmiConfiguration rmiConfiguration) {
        _rmiConfiguration = rmiConfiguration;
    }
    
    public JBossRemotingConfiguration getJBossRemotingConfiguration() {
        return _jbossRemotingConfiguration;
    }

    public void setJBossRemotingConfiguration(JBossRemotingConfiguration jbossRemotingConfiguration) {
        _jbossRemotingConfiguration = jbossRemotingConfiguration;
    }

    /**
     * Returns a ConnectionConfiguration for a specific identifier.
     * @param name Identifier of the ConnectionConfiguration
     * @return ConnectionConfiguration or null
     */
    public ConnectionConfiguration getConnection(String name) {
        for(Iterator it = _connections.iterator(); it.hasNext();) {
            ConnectionConfiguration connectionConfiguration = (ConnectionConfiguration)it.next();
            if(connectionConfiguration.getId().equals(name)) {
                return connectionConfiguration;
            }
        }
        return null;
    }

    /**
     * Adds a ConnectionConfiguration.
     * @param connectionConfiguration
     * @throws ConfigurationException Thrown when the connection identifier already exists
     */
    public void addConnection(ConnectionConfiguration connectionConfiguration) throws ConfigurationException {
        if(getConnection(connectionConfiguration.getId()) == null) {
            _connections.add(connectionConfiguration);
        } else {
            String msg = "Connection configuration for " + connectionConfiguration.getId() + " already exists";
            _logger.error(msg);
            throw new ConfigurationException(msg);
        }
    }

    private VJdbcConfiguration(String resource) throws IOException, SAXException, ConfigurationException {
        createDigester().parse(resource);
        validateConnections();
    }

    private VJdbcConfiguration(InputStream resource) throws IOException, SAXException, ConfigurationException {
        createDigester().parse(resource);
        validateConnections();
    }

    private void validateConnections() throws ConfigurationException {
        // Call the validation method of the configuration
        for(Iterator it = _connections.iterator(); it.hasNext();) {
            ConnectionConfiguration connectionConfiguration = (ConnectionConfiguration)it.next();
            connectionConfiguration.validate();
        }
    }

    private Digester createDigester() {
        Digester digester = new Digester();

        digester.push(this);
        
        digester.addObjectCreate("vjdbc-configuration/occt", DigesterOcctConfiguration.class);
        digester.addSetProperties("vjdbc-configuration/occt");
        digester.addSetNext("vjdbc-configuration/occt",
                "setOcctConfiguration",
                OcctConfiguration.class.getName());

        digester.addObjectCreate("vjdbc-configuration/rmi", DigesterRmiConfiguration.class);
        digester.addSetProperties("vjdbc-configuration/rmi");
        digester.addSetNext("vjdbc-configuration/rmi",
                "setRmiConfiguration",
                RmiConfiguration.class.getName());

        digester.addObjectCreate("vjdbc-configuration/jbossremoting", JBossRemotingConfiguration.class);
        digester.addSetProperties("vjdbc-configuration/jbossremoting");
        digester.addSetNext("vjdbc-configuration/jbossremoting",
                "setJBossRemotingConfiguration",
                JBossRemotingConfiguration.class.getName());

        digester.addObjectCreate("vjdbc-configuration/connection", DigesterConnectionConfiguration.class);
        digester.addSetProperties("vjdbc-configuration/connection");
        digester.addSetNext("vjdbc-configuration/connection",
                "addConnection",
                ConnectionConfiguration.class.getName());
        
        digester.addObjectCreate("vjdbc-configuration/connection/connection-pool", ConnectionPoolConfiguration.class);
        digester.addSetProperties("vjdbc-configuration/connection/connection-pool");
        digester.addSetNext("vjdbc-configuration/connection/connection-pool",
                "setConnectionPoolConfiguration",
                ConnectionPoolConfiguration.class.getName());
        
        // Named-Queries
        digester.addObjectCreate("vjdbc-configuration/connection/named-queries", NamedQueryConfiguration.class);
        digester.addCallMethod("vjdbc-configuration/connection/named-queries/entry", "addEntry", 2);
        digester.addCallParam("vjdbc-configuration/connection/named-queries/entry", 0, "id");
        digester.addCallParam("vjdbc-configuration/connection/named-queries/entry", 1);
        digester.addSetNext("vjdbc-configuration/connection/named-queries",
                "setNamedQueries",
                NamedQueryConfiguration.class.getName());
        
        // Query-Filters
        digester.addObjectCreate("vjdbc-configuration/connection/query-filters", QueryFilterConfiguration.class);
        digester.addCallMethod("vjdbc-configuration/connection/query-filters/deny", "addDenyEntry", 2);
        digester.addCallParam("vjdbc-configuration/connection/query-filters/deny", 0);
        digester.addCallParam("vjdbc-configuration/connection/query-filters/deny", 1, "type");
        digester.addCallMethod("vjdbc-configuration/connection/query-filters/allow", "addAllowEntry", 2);
        digester.addCallParam("vjdbc-configuration/connection/query-filters/allow", 0);
        digester.addCallParam("vjdbc-configuration/connection/query-filters/allow", 1, "type");
        digester.addSetNext("vjdbc-configuration/connection/query-filters",
                "setQueryFilters",
                QueryFilterConfiguration.class.getName());

        return digester;
    }

    private void log() {
        if(_rmiConfiguration != null) {
            _rmiConfiguration.log();
        }
        if(_jbossRemotingConfiguration != null) {
            _jbossRemotingConfiguration.log();
        }
        _occtConfiguration.log();
        for(Iterator it = _connections.iterator(); it.hasNext();) {
            ConnectionConfiguration connectionConfiguration = (ConnectionConfiguration)it.next();
            connectionConfiguration.log();
        }
    }
}
