/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.event;

import java.util.EventObject;
import org.previmer.ichthyop.manager.TimeManager;

/**
 *
 * @author pverley
 */
public class NextStepEvent extends EventObject {

    private boolean interrupted;

    public NextStepEvent(Object source, boolean interrupted) {
        super(source);
        this.interrupted = interrupted;
    }

    @Override
    public TimeManager getSource() {
        return (TimeManager) source;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

}
