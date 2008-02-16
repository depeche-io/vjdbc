// VJDBC - Virtual JDBC
// Written by Michael Link
// Website: http://vjdbc.sourceforge.net

package de.simplicit.vjdbc.command;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.Date;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;
import java.util.Properties;

/**
 * This class holds an array of class arrays which can be efficiently used for Reflection
 * on the serverside by just transporting the array identifier instead of big objects.
 */
public class ParameterTypeCombinations {
    public static final Class[][] _typeCombinations = new Class[][]{
        new Class[0], // 0
        new Class[]{Boolean.TYPE}, // 1
        new Class[]{Integer.TYPE}, // 2
        new Class[]{String.class}, // 3
        new Class[]{Map.class}, // 4
        new Class[]{Properties.class}, // 5
        // Pairs
        new Class[]{Integer.TYPE, Integer.TYPE}, // 6
        new Class[]{Integer.TYPE, Calendar.class}, // 7
        new Class[]{String.class, Boolean.TYPE}, // 8
        new Class[]{String.class, Byte.TYPE}, // 9
        new Class[]{String.class, Short.TYPE}, // 10
        new Class[]{String.class, Integer.TYPE}, // 11
        new Class[]{String.class, Long.TYPE}, // 12
        new Class[]{String.class, Float.TYPE}, // 13
        new Class[]{String.class, Double.TYPE}, // 14
        new Class[]{String.class, String.class}, // 15
        new Class[]{String.class, Date.class}, // 16
        new Class[]{String.class, Time.class}, // 17
        new Class[]{String.class, Timestamp.class}, // 18
        new Class[]{String.class, Calendar.class}, // 19
        new Class[]{String.class, URL.class}, // 20
        new Class[]{String.class, BigDecimal.class}, // 21
        new Class[]{String.class, byte[].class}, // 22
        new Class[]{String.class, int[].class}, // 23
        new Class[]{String.class, String[].class}, // 24
        // Triples
        new Class[]{Integer.TYPE, Integer.TYPE, Integer.TYPE}, // 25
        new Class[]{String.class, Integer.TYPE, Integer.TYPE}, // 26
        new Class[]{Integer.TYPE, Integer.TYPE, String.class}, // 27
        new Class[]{String.class, Integer.TYPE, String.class}, // 28
        new Class[]{String.class, String.class, String.class}, // 29
        new Class[]{String.class, Date.class, Calendar.class}, // 30
        new Class[]{String.class, Time.class, Calendar.class}, // 31
        new Class[]{String.class, Timestamp.class, Calendar.class}, // 32
        // Quad
        new Class[]{String.class, Integer.TYPE, Integer.TYPE, Integer.TYPE}, // 33
        new Class[]{String.class, String.class, String.class, String.class}, // 34
        new Class[]{String.class, String.class, String.class, String[].class}, // 35
        new Class[]{String.class, String.class, String.class, int[].class}, // 36
        // Five
        new Class[]{String.class, String.class, String.class, Integer.TYPE, Boolean.TYPE}, // 37
        new Class[]{String.class, String.class, String.class, Boolean.TYPE, Boolean.TYPE}, // 38
        // Six
        new Class[]{String.class, String.class, String.class, String.class, String.class, String.class}// 39
    };

    // Single
    public static final int BOL = 1;
    public static final int INT = 2;
    public static final int STR = 3;
    public static final int MAP = 4;
    public static final int PRP = 5;
    // Pairs
    public static final int INTINT = 6;
    public static final int INTCAL = 7;
    public static final int STRBOL = 8;
    public static final int STRBYT = 9;
    public static final int STRSHT = 10;
    public static final int STRINT = 11;
    public static final int STRLNG = 12;
    public static final int STRFLT = 13;
    public static final int STRDBL = 14;
    public static final int STRSTR = 15;
    public static final int STRDAT = 16;
    public static final int STRTIM = 17;
    public static final int STRTMS = 18;
    public static final int STRCAL = 19;
    public static final int STRURL = 20;
    public static final int STRBID = 21;
    public static final int STRBYTA = 22;
    public static final int STRINTA = 23;
    public static final int STRSTRA = 24;
    // Triple
    public static final int INTINTINT = 25;
    public static final int STRINTINT = 26;
    public static final int INTINTSTR = 27;
    public static final int STRINTSTR = 28;
    public static final int STRSTRSTR = 29;
    public static final int STRDATCAL = 30;
    public static final int STRTIMCAL = 31;
    public static final int STRTMSCAL = 32;
    // Quad
    public static final int STRINTINTINT = 33;
    public static final int STRSTRSTRSTR = 34;
    public static final int STRSTRSTRSTRA = 35;
    public static final int STRSTRSTRINTA = 36;
    // Five
    public static final int STRSTRSTRINTBOL = 37;
    public static final int STRSTRSTRBOLBOL = 38;
    // Six
    public static final int STRSTRSTRSTRSTRSTR = 39;
}
