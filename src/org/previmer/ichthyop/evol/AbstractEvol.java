package org.previmer.ichthyop.evol;

/**
 *
 * @author mariem
 */

import org.previmer.ichthyop.arch.IEvol;
import org.previmer.ichthyop.SimulationManagerAccessor;

public class AbstractEvol extends SimulationManagerAccessor implements IEvol {

    private String evolKey;
    private int nb_generations;

    public AbstractEvol() {
        evolKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
    }

    public String getParameter(String key) {
        return getSimulationManager().getEvolManager().getParameter(evolKey, key);
    }

    public void loadParameters() throws Exception {    

        // load common parameters
        nb_generations = Integer.valueOf(getParameter("nb_generations"));
    }

    // pour garantir qu'une seule stratégie de reproduction a été sélectionnée
    public boolean isEnabled() {
        return getSimulationManager().getEvolManager().isEnabled(evolKey);
    }

    public int getNbGenerations() {
        return nb_generations;
    }
}
