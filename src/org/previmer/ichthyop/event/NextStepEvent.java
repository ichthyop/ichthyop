/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.event;

import org.previmer.ichthyop.arch.ITimeManager;
import java.util.EventObject;

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
    public ITimeManager getSource() {
        return (ITimeManager) source;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

}
