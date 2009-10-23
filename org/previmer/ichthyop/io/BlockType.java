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
    ZONE,
    RELEASE,
    DATASET,
    TRACKER;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
