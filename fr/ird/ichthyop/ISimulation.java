/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public interface ISimulation {

    public Population getPopulation();

    public IDataset getDataset();

    public double getTime();

    public double getDt();

    public long getTransportDuration();

    public Step getStep();

}
