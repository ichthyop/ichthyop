/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.dataset;

import org.previmer.ichthyop.event.NextStepListener;
import org.previmer.ichthyop.arch.IDataset;
import org.previmer.ichthyop.SimulationManagerAccessor;

/**
 *
 * @author pverley
 */
public abstract class AbstractDataset extends SimulationManagerAccessor implements IDataset, NextStepListener {

    private String datasetKey;

    abstract void loadParameters();

    public AbstractDataset() {
        datasetKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
        getSimulationManager().getTimeManager().addNextStepListener(this);
    }

    public String getParameter(String key) {
        return getSimulationManager().getDatasetManager().getParameter(datasetKey, key);
    }
}
