/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.arch;

import org.previmer.ichthyop.event.InitializeListener;
import org.previmer.ichthyop.event.SetupListener;
import java.io.File;

/**
 *
 * @author pverley
 */
public interface ISimulationManager {

    public IActionManager getActionManager();

    public IDatasetManager getDatasetManager();

    public IDataset getDataset();

    public IParameterManager getParameterManager();

    public IPropertyManager getPropertyManager(Class aClass);

    public IZoneManager getZoneManager();

    public IReleaseManager getReleaseManager();

    public IOutputManager getOutputManager();

    public ITimeManager getTimeManager();

    public ISimulation getSimulation();

    public void addSetupListener(SetupListener listener);

    public void removeSetupListener(SetupListener listener);

    public void addInitializeListener(InitializeListener listener);

    public void removeInitializeListener(InitializeListener listener);

    public void setConfigurationFile(File file);

    public void runBatch();
}
