package org.previmer.ichthyop.evol;
/**
 *
 * @author mariem
 */

import org.previmer.ichthyop.arch.IEvol;
import org.previmer.ichthyop.SimulationManagerAccessor;

public class AbstractEvol extends SimulationManagerAccessor implements IEvol{

    private String evolKey;

    public AbstractEvol() {
        evolKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
    }

    public String getParameter(String key) {
        return getSimulationManager().getActionManager().getParameter(evolKey, key);
    }

    public void loadParameters() throws Exception {
        int nb_generations, age_min, age_max,spawn_freq;

        /* load common parameters*/
        nb_generations = Integer.valueOf(getParameter("nb_generations"));
        age_min= Integer.valueOf("min_maturity");
        age_max= Integer.valueOf("max_maturity");
        spawn_freq= Integer.valueOf("spawn_frequency");
    }

    // pour garantir qu'une seule stratégie de reproduction a été sélectionnée
    public boolean isEnabled() {
        return getSimulationManager().getActionManager().isEnabled(evolKey);
    }

    public int getNbParticles() {
        return Integer.valueOf(getParameter("number_particles"));
    }

}
