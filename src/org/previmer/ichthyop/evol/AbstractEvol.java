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
    private int age_min;
    private int age_max;
    private int spawn_freq;

    public AbstractEvol() {
        evolKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
    }

    public String getParameter(String key) {
        return getSimulationManager().getEvolManager().getParameter(evolKey, key);
    }

    public void loadParameters() throws Exception {    

        /* load common parameters*/
        nb_generations = Integer.valueOf(getParameter("nb_generations"));
        age_min = Integer.valueOf(getParameter("min_maturity"));
        age_max = Integer.valueOf(getParameter("max_maturity"));
        spawn_freq = Integer.valueOf(getParameter("spawn_frequency"));
    }

    // pour garantir qu'une seule stratégie de reproduction a été sélectionnée
    public boolean isEnabled() {
        return getSimulationManager().getEvolManager().isEnabled(evolKey);
    }

    /**
     * @return the nb_generations
     */
    public int getNb_generations() {
        return nb_generations;
    }

    /**
     * @return the age_min
     */
    public int getAge_min() {
        return age_min;
    }

    /**
     * @return the age_max
     */
    public int getAge_max() {
        return age_max;
    }

    /**
     * @return the spawn_freq
     */
    public int getSpawn_freq() {
        return spawn_freq;
    }
}
