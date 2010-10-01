/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.action;

/**
 *
 * @author pverley
 */
public enum ActionPriority {

    HIGHEST(5),
    HIGH(4),
    NORMAL(3),
    LOW(2),
    LOWEST(1);
    
    private final int rank;

    ActionPriority(int rank) {
        this.rank = rank;
    }

    @Override
    public String toString() {
        return name().toLowerCase();
    }

    public Integer rank() {
        return rank;
    }

}
