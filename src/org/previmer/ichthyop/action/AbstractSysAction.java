package org.previmer.ichthyop.action;

import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.arch.ISysAction;

/**
 *
 * @author pverley
 */
abstract class AbstractSysAction extends SimulationManagerAccessor implements ISysAction {

    public String getParameter(String block, String key) {
        return getSimulationManager().getParameterManager().getParameter(block, key);
    }

}
