/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.event.InitializeListener;
import org.previmer.ichthyop.event.SetupListener;

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
