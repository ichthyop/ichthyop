/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

import fr.ird.ichthyop.manager.*;
import fr.ird.ichthyop.arch.ISimulationManager;

/**
 *
 * @author pverley
 */
public class SimulationManagerAccessor {

    public ISimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

}
