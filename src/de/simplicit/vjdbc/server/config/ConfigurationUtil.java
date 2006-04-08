// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.server.config;

class ConfigurationUtil {
    private static final long MILLIS_PER_SECOND = 1000;
    private static final long MILLIS_PER_MINUTE = MILLIS_PER_SECOND * 60;
    
    static boolean getBooleanFromString(String value) {
        return value.equalsIgnoreCase("true") || value.equalsIgnoreCase("on");
    }

    static long getMillisFromString(String value) {
        if(value.endsWith("s")) {
            return Long.parseLong(value.substring(0, value.length()-1)) * 1000;
        }
        else if(value.endsWith("m")) {
            return Long.parseLong(value.substring(0, value.length()-1)) * 1000 * 60;
        }
        else {
            return Long.parseLong(value);
        }
    }
    
    static String getTimeFromMillis(long value) {
        if( (value % MILLIS_PER_MINUTE) == 0) {
            return "" + (value / MILLIS_PER_MINUTE) + " min";
        }
        else if( (value % MILLIS_PER_SECOND) == 0) {
            return "" + (value / MILLIS_PER_SECOND) + " sec";
        }
        else {
            return "" + value + " ms";
        }
    }
}
