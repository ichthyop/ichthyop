/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.dataset;

import fr.ird.ichthyop.event.NextStepEvent;
import fr.ird.ichthyop.event.NextStepListener;
import fr.ird.ichthyop.Simulation;
import fr.ird.ichthyop.arch.IDataset;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.ISimulationAccessor;

/**
 *
 * @author pverley
 */
public abstract class AbstractDataset implements IDataset, ISimulationAccessor, NextStepListener {

    private String datasetKey;

    abstract void loadParameters();

    public AbstractDataset() {
        datasetKey = getSimulation().getPropertyManager(getClass()).getProperty("dataset.key");
    }

    public String getParameter(String key) {
        return getSimulation().getDatasetManager().getParameter(key);
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }

}
