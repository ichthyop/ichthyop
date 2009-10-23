/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import fr.ird.ichthyop.arch.IOutputManager;
import fr.ird.ichthyop.manager.ReleaseManager;
import fr.ird.ichthyop.arch.IReleaseManager;
import fr.ird.ichthyop.manager.ZoneManager;
import fr.ird.ichthyop.manager.ParameterManager;
import fr.ird.ichthyop.manager.ActionManager;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.IDataset;
import fr.ird.ichthyop.arch.IZoneManager;
import fr.ird.ichthyop.arch.IActionManager;
import fr.ird.ichthyop.arch.IDatasetManager;
import fr.ird.ichthyop.arch.IParameterManager;
import fr.ird.ichthyop.arch.IPopulation;
import fr.ird.ichthyop.arch.IPropertyManager;
import fr.ird.ichthyop.arch.IStep;
import fr.ird.ichthyop.manager.DatasetManager;
import fr.ird.ichthyop.manager.OutputManager;
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

    public void step() {
        getPopulation().step();
    }

    /*public void setUp() {
        getReleaseManager().setUp();
        getOutputManager().setUp();
        getDataset().setUp();
        getActionManager().setUp();
        getStep().setUp();
    }

    public void init() {
        getStep().init();
        getOutputManager().init();
        getDataset().init();
        getZoneManager().init();
    }*/
}
