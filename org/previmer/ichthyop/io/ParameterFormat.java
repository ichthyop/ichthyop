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
    
    TEXT,
    DATE,
    DURATION,
    FLOAT,
    INTEGER,
    LIST,
    BOOLEAN,
    FILE,
    PATH;

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

}
