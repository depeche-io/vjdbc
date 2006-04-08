// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * This class holds configuration information for JBoss-Remoting.
 */
public class JBossRemotingConfiguration {
    private static Log _logger = LogFactory.getLog(JBossRemotingConfiguration.class);
    
    private String _locatorUrl;

    public JBossRemotingConfiguration() {
    }
    
    public String getLocatorUrl() {
        return _locatorUrl;
    }

    public void setLocatorUrl(String locatorUrl) {
        _locatorUrl = locatorUrl;
    }

    void log() {
        if(_locatorUrl != null) {
            _logger.info("JBoss-Remoting-Configuration");
            _logger.info("  Locator-URL.......... " + _locatorUrl);
        }
    }
}
