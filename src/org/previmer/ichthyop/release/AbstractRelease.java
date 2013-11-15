/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.event.ReleaseEvent;

/**
 *
 * @author pverley
 */
public abstract class AbstractRelease extends SimulationManagerAccessor {

    private final String releaseKey;
    
    abstract public void loadParameters() throws Exception;

    abstract public int release(ReleaseEvent event) throws Exception;

    abstract public int getNbParticles();

    public AbstractRelease() {
        releaseKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
    }

    String getParameter(String key) {
        return getSimulationManager().getReleaseManager().getParameter(releaseKey, key);
    }
}
