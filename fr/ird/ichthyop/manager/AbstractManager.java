/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.SimulationManagerAccessor;
import fr.ird.ichthyop.event.InitializeListener;
import fr.ird.ichthyop.event.SetupListener;

/**
 *
 * @author pverley
 */
public abstract class AbstractManager extends SimulationManagerAccessor implements SetupListener, InitializeListener {

    AbstractManager() {
        getSimulationManager().addSetupListener(this);
        getSimulationManager().addInitializeListener(this);
    }
}
