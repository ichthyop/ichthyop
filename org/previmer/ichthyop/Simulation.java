/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop;

import org.previmer.ichthyop.arch.IOutputManager;
import org.previmer.ichthyop.manager.ReleaseManager;
import org.previmer.ichthyop.arch.IReleaseManager;
import org.previmer.ichthyop.manager.ZoneManager;
import org.previmer.ichthyop.manager.ParameterManager;
import org.previmer.ichthyop.manager.ActionManager;
import org.previmer.ichthyop.arch.ISimulation;
import org.previmer.ichthyop.arch.IDataset;
import org.previmer.ichthyop.arch.IZoneManager;
import org.previmer.ichthyop.arch.IActionManager;
import org.previmer.ichthyop.arch.IDatasetManager;
import org.previmer.ichthyop.arch.IParameterManager;
import org.previmer.ichthyop.arch.IPopulation;
import org.previmer.ichthyop.arch.IPropertyManager;
import org.previmer.ichthyop.arch.IStep;
import org.previmer.ichthyop.manager.DatasetManager;
import org.previmer.ichthyop.manager.OutputManager;
import org.previmer.ichthyop.manager.PropertyManager;

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
