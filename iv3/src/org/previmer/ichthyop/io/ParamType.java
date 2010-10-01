/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

/**
 *
 * @author pverley
 */
public enum ParamType {

    SINGLE,
    SERIAL;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public static ParamType getType(String typeStr) {
        for (ParamType type : values()) {
            if (type.toString().matches(typeStr))
                return type;
        }
        return null;
    }
}
