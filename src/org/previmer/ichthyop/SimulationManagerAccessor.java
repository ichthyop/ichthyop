/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop;

import java.util.logging.Logger;
import org.previmer.ichthyop.manager.*;

/**
 *
 * @author pverley
 */
public class SimulationManagerAccessor {

    public static SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }
    
    public static Logger getLogger() {
     return SimulationManager.getLogger();
    }

}
