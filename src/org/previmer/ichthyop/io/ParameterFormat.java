/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.io;

/**
 *
 * @author pverley
 */
public enum ParameterFormat {
    
    TEXT("value not set yet"),
    DATE("year 2000 month 01 day 01 at 00:00"),
    DURATION("0000 day(s) 01 hour(s) 00 minute(s)"),
    HOUR("12:00"),
    FLOAT("0.0"),
    INTEGER("0"),
    COMBO("value not set yet"),
    BOOLEAN("true"),
    FILE("file not set yet"),
    PATH(System.getProperty("user.dir")),
    TEXTFILE(System.getProperty("user.dir")),
    CLASS("class not set yet"),
    LIST("value not set yet"),
    ZONEFILE(System.getProperty("user.dir")),
    LONLAT("0.0");
    
    private String defaultValue;

    ParameterFormat(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static ParameterFormat getFormat(String formatStr) {
        for (ParameterFormat type : values()) {
            if (type.toString().equals(formatStr))
                return type;
        }
        return null;
    }

    public String getDefault() {
        return defaultValue;
    }

}
