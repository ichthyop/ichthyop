/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.event;

import java.util.EventObject;

/**
 *
 * @author pverley
 */
public class LastStepEvent extends EventObject {

    private boolean interrupted;

    public LastStepEvent(Object source, boolean interrupted) {
        super(source);
        this.interrupted = interrupted;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

}
