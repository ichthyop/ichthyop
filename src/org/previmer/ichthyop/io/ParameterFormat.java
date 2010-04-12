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
    DATE("0"),
    DURATION("3600"),
    FLOAT("0.0"),
    INTEGER("0"),
    COMBO("value not set yet"),
    BOOLEAN("true"),
    FILE("file not set yet"),
    PATH(System.getProperty("user.dir")),
    TEXTFILE(System.getProperty("user.dir")),
    CLASS("class not set yet"),
    LIST("value not set yet"),
    ZONEFILE(System.getProperty("user.dir"));
    
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
            if (type.toString().matches(formatStr))
                return type;
        }
        return null;
    }

    public String getDefault() {
        return defaultValue;
    }

}
