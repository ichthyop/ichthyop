/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.ReleaseManager;

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
    
    public IPropertyManager getPropertyManager(Class aClass);

    public IZoneManager getZoneManager();

    public IReleaseManager getReleaseManager();

    public void step();

}
