/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop;

import java.util.logging.Logger;
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
    
    public Logger getLogger() {
     return Logger.getLogger(ISimulationManager.class.getName());
    }

}
