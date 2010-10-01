/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop;

import org.previmer.ichthyop.arch.ISimulation;
import org.previmer.ichthyop.arch.IPopulation;

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
}
