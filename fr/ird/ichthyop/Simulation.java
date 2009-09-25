/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import fr.ird.ichthyop.arch.IReleaseManager;
import fr.ird.ichthyop.manager.ZoneManager;
import fr.ird.ichthyop.manager.ParameterManager;
import fr.ird.ichthyop.manager.ActionManager;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.IDataset;
import fr.ird.ichthyop.arch.IZoneManager;
import fr.ird.ichthyop.arch.IActionManager;
import fr.ird.ichthyop.arch.IParameterManager;
import fr.ird.ichthyop.arch.IPropertyManager;
import fr.ird.ichthyop.manager.PropertyManager;

/**
 *
 * @author pverley
 */
public class Simulation implements ISimulation {

    private static Simulation simulation = new Simulation();
    private Population population;
    
    public void setUp() {}

    public void init() {}

    public Population getPopulation() {
        return population;
    }

    public static Simulation getInstance() {
        return simulation;
    }

    public IDataset getDataset() {
        return null;
    }

    public Step getStep() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void step() {
        getPopulation().step();
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
}
