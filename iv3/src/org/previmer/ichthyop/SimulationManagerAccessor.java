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

    public static ISimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }
    
    public static Logger getLogger() {
     return SimulationManager.getLogger();
    }

}
