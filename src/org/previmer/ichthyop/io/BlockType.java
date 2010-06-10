/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.io;

/**
 *
 * @author pverley
 */
public enum BlockType {

    OPTION,
    ACTION,
    RELEASE,
    DATASET;

    static BlockType getType(String value) {
        for (BlockType type : values()) {
            if (type.toString().matches(value))
                return type;
        }
        return null;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }
}
