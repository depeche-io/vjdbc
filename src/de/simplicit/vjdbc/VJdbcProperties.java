// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc;

public final class VJdbcProperties {
    // System properties to transfer to the server when opening a connection 
    public static final String CLIENTINFO_PROPERTIES = "vjdbc.clientinfo.properties";
    // Tables to be cached, property must be in the format "Table[:Refresh-Interval],Table..."
    public static final String CACHE_TABLES = "vjdbc.cache.tables";
    // Login-Handler-Class which authenticates the user
    public static final String LOGIN_USER = "vjdbc.login.user";
    public static final String LOGIN_PASSWORD = "vjdbc.login.password";
    // Signaling using of SSL sockets for RMI communication (true or false, default: false)
    public static final String RMI_SSL = "vjdbc.rmi.ssl";
}
