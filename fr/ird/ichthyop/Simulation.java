/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public class Simulation implements ISimulation {

    private Population population;

    public void step(long time) {

        population.step(time);
    }

    public void setUp() {}

    public void init() {}

    public Population getPopulation() {
        return population;
    }

    public static Simulation getInstance() {
        return null;
    }

    public IDataset getDataset() {
        return null;
    }

    public double getTime() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public double getDt() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getTransportDuration() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Step getStep() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
