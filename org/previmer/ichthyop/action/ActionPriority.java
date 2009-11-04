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

    HIGHEST,
    HIGH,
    NORMAL,
    LOW,
    LOWEST;

    @Override
    public String toString() {
        return name().toLowerCase();
    }

}
