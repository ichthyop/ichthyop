/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import org.previmer.ichthyop.arch.IReleaseProcess;
import org.previmer.ichthyop.SimulationManagerAccessor;

/**
 *
 * @author pverley
 */
public abstract class AbstractReleaseProcess extends SimulationManagerAccessor implements IReleaseProcess {

    private String releaseKey;

    public AbstractReleaseProcess() {
        releaseKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
        loadParameters();
    }

    String getParameter(String key) {
        return getSimulationManager().getReleaseManager().getParameter(releaseKey, key);
    }
}
