/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.Simulation;
import fr.ird.ichthyop.arch.IActionManager;
import fr.ird.ichthyop.arch.IDataset;
import fr.ird.ichthyop.arch.IDatasetManager;
import fr.ird.ichthyop.arch.IOutputManager;
import fr.ird.ichthyop.arch.IParameterManager;
import fr.ird.ichthyop.arch.IPropertyManager;
import fr.ird.ichthyop.arch.IReleaseManager;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.ISimulationManager;
import fr.ird.ichthyop.arch.ITimeManager;
import fr.ird.ichthyop.arch.IZoneManager;
import fr.ird.ichthyop.event.InitializeEvent;
import fr.ird.ichthyop.event.InitializeListener;
import fr.ird.ichthyop.event.SetupEvent;
import fr.ird.ichthyop.event.SetupListener;
import fr.ird.ichthyop.io.ParamType;
import fr.ird.ichthyop.io.XParameter;
import java.io.File;
import javax.swing.event.EventListenerList;

/**
 *
 * @author pverley
 */
public class SimulationManager implements ISimulationManager {

    final private static SimulationManager simulationManager = new SimulationManager();
    /**
     * Index of the current simulation (always 0 for SINGLE mode)
     */
    private int i_simulation;
    /***
     * Number of simulations (always 1 for SERIAL mode)
     */
    private int nb_simulations = 1;
    /**
     *
     */
    private EventListenerList listeners = new EventListenerList();

    public static SimulationManager getInstance() {
        return simulationManager;
    }

    public void setConfigurationFile(File file) {
        getParameterManager().setConfigurationFile(file);
        for (XParameter xparam : getParameterManager().getParameters(ParamType.SERIAL)) {
            nb_simulations *= xparam.getLength();
        }
    }

    /**
     * Checks for the existence of a new set of parameters. Systematically
     * returns false for SINGLE mode.
     * @return <code>true</code> if a new set of parameters is available;
     * <code>false</code> otherwise
     */
    public boolean hasNextSimulation() {

        for (XParameter xparam : getParameterManager().getParameters(ParamType.SERIAL)) {
            if (xparam.hasNext()) {
                xparam.increment();
                this.reset();
                return true;
            } else {
                xparam.reset();
            }
        }
        return false;
    }

    /**
     * Gets the number of simulations (which equals the number of sets of
     * parameters predefined by the user). Returns 1 for SINGLE mode.
     * @return the number of simulations.
     */
    public int getNumberOfSimulations() {
        return nb_simulations;
    }

    /**
     * Resets indexes and time values at the beginning of a new simulation.
     */
    private void reset() {
        i_simulation++;
    }

    private void mobiliseManagers() {
        getActionManager();
        getDatasetManager();
        getOutputManager();
        getReleaseManager();
        getTimeManager();
        getZoneManager();
    }

    private String indexSimulation() {
        return (i_simulation + 1) + " / " + nb_simulations;
    }

    public void run() {
        mobiliseManagers();
        do {
            System.out.println("=========================");
            System.out.println("Simulation " + indexSimulation());
            System.out.println("=========================");
            fireSetupPerformed();
            fireInitializePerformed();
            getTimeManager().firstStepTriggered();
            do {
                System.out.println("-----< " + getTimeManager().timeToString() + " >-----");
                getSimulation().step();
            } while (getTimeManager().hasNextStep());
            System.out.println("End of simulation");
        } while (hasNextSimulation());
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }

    public void addSetupListener(SetupListener listener) {
        listeners.add(SetupListener.class, listener);
    }

    public void removeSetupListener(SetupListener listener) {
        listeners.remove(SetupListener.class, listener);
    }

    private void fireSetupPerformed() {
        System.out.println("Setup...");
        SetupListener[] listenerList = (SetupListener[]) listeners.getListeners(SetupListener.class);

        for (SetupListener listener : listenerList) {
            //System.out.println("fire setup to " + listener.getClass().getCanonicalName());
            listener.setupPerformed(new SetupEvent(this));
        }
    }

    public void addInitializeListener(InitializeListener listener) {
        listeners.add(InitializeListener.class, listener);
    }

    public void removeInitializeListener(InitializeListener listener) {
        listeners.remove(InitializeListener.class, listener);
    }

    private void fireInitializePerformed() {
        System.out.println("Initialization...");
        InitializeListener[] listenerList = (InitializeListener[]) listeners.getListeners(InitializeListener.class);

        for (InitializeListener listener : listenerList) {
            listener.initializePerformed(new InitializeEvent(this));
        }
    }

     public IDatasetManager getDatasetManager() {
        return DatasetManager.getInstance();
    }

    public IDataset getDataset() {
        return getDatasetManager().getDataset();
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

    public IOutputManager getOutputManager() {
        return OutputManager.getInstance();
    }

    public ITimeManager getTimeManager() {
        return TimeManager.getInstance();
    }
}
