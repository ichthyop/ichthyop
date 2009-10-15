/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.io;

/**
 *
 * @author pverley
 */
public enum TypeBlock {

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
