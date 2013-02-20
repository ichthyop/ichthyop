/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.arch.IReleaseProcess;

/**
 *
 * @author pverley
 */
public abstract class AbstractReleaseProcess extends SimulationManagerAccessor implements IReleaseProcess {

    private String releaseKey;

    public AbstractReleaseProcess() {
        releaseKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
    }

    String getParameter(String key) {
        return getSimulationManager().getReleaseManager().getParameter(releaseKey, key);
    }
}
