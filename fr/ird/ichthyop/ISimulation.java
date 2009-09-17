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

    public IPopulation getPopulation();

    public IDataset getDataset();

    public IStep getStep();

    public IActionManager getActionManager();

    public IParameterManager getParameterManager();

    public IParameterManager getParameterManager(Class aClass);

    public void step();

}
