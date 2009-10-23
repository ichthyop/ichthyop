/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop;

import org.previmer.ichthyop.manager.*;
import org.previmer.ichthyop.arch.ISimulationManager;

/**
 *
 * @author pverley
 */
public class SimulationManagerAccessor {

    public ISimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

}
