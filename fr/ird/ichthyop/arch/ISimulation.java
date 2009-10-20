/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

/**
 *
 * @author pverley
 */
public interface ISimulation {

    public IPopulation getPopulation();

    public IDataset getDataset();

    public IStep getStep();

    public IActionManager getActionManager();

    public IDatasetManager getDatasetManager();

    public IParameterManager getParameterManager();
    
    public IPropertyManager getPropertyManager(Class aClass);

    public IZoneManager getZoneManager();

    public IReleaseManager getReleaseManager();

    public IOutputManager getOutputManager();

    public void step();

    public void setUp();

    public void init();

}
