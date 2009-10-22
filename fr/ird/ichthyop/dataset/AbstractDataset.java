/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.dataset;

import fr.ird.ichthyop.event.NextStepListener;
import fr.ird.ichthyop.arch.IDataset;
import fr.ird.ichthyop.SimulationManagerAccessor;

/**
 *
 * @author pverley
 */
public abstract class AbstractDataset extends SimulationManagerAccessor implements IDataset, NextStepListener {

    private String datasetKey;

    abstract void loadParameters();

    public AbstractDataset() {
        datasetKey = getSimulationManager().getPropertyManager(getClass()).getProperty("dataset.key");
        getSimulationManager().getTimeManager().addNextStepListener(this);
    }

    public String getParameter(String key) {
        return getSimulationManager().getDatasetManager().getParameter(datasetKey, key);
    }
}
