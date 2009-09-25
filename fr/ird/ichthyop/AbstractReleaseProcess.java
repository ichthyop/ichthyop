/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

import fr.ird.ichthyop.arch.ISimulation;

/**
 *
 * @author pverley
 */
public abstract class AbstractReleaseProcess implements IReleaseProcess {

    String releaseKey;

    public AbstractReleaseProcess() {
        releaseKey = getSimulation().getPropertyManager(getClass()).getProperty("release.key");
    }

    public String getParameter(String key) {
        return getSimulation().getReleaseManager().getXReleaseProcess(releaseKey).getParameter(key).getValue();
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }

}
