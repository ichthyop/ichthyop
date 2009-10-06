/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import fr.ird.ichthyop.manager.ReleaseManager;
import fr.ird.ichthyop.arch.IActionPool;
import fr.ird.ichthyop.arch.IReleaseManager;
import fr.ird.ichthyop.manager.ZoneManager;
import fr.ird.ichthyop.manager.ParameterManager;
import fr.ird.ichthyop.manager.ActionManager;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.IDataset;
import fr.ird.ichthyop.arch.IZoneManager;
import fr.ird.ichthyop.arch.IActionManager;
import fr.ird.ichthyop.arch.IParameterManager;
import fr.ird.ichthyop.arch.IPopulation;
import fr.ird.ichthyop.arch.IPropertyManager;
import fr.ird.ichthyop.arch.IStep;
import fr.ird.ichthyop.manager.DatasetManager;
import fr.ird.ichthyop.manager.PropertyManager;

/**
 *
 * @author pverley
 */
public class Simulation implements ISimulation {

    final private static Simulation simulation = new Simulation();

    public IPopulation getPopulation() {
        return Population.getInstance();
    }

    public static Simulation getInstance() {
        return simulation;
    }

    public DatasetManager getDatasetManager() {
        return DatasetManager.getInstance();
    }

    public IDataset getDataset() {
        return getDatasetManager().getDataset();
    }

    public IStep getStep() {
        return Step.getInstance();
    }

    public void step() {
        getPopulation().step();
        getStep().next();
    }

    public IActionManager getActionManager() {
        return ActionManager.getInstance();
    }

    public IParameterManager getParameterManager() {
        return ParameterManager.getInstance();
    }

    public IPropertyManager getPropertyManager(Class forClass) {
        return PropertyManager.getInstance(forClass);
    }

    public IZoneManager getZoneManager() {
        return ZoneManager.getInstance();
    }

    public IReleaseManager getReleaseManager() {
        return ReleaseManager.getInstance();
    }

    public IActionPool getActionPool() {
       return ActionPool.getInstance();
    }
}
