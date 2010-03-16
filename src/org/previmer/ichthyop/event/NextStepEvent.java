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

    public NextStepEvent(Object source) {
        super(source);
    }

    @Override
    public ITimeManager getSource() {
        return (ITimeManager) source;
    }

}
