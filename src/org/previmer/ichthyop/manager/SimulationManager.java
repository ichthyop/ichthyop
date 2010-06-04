/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import java.util.logging.Level;
import org.jdom.JDOMException;
import org.previmer.ichthyop.Simulation;
import org.previmer.ichthyop.arch.IActionManager;
import org.previmer.ichthyop.arch.IDataset;
import org.previmer.ichthyop.arch.IDatasetManager;
import org.previmer.ichthyop.arch.IOutputManager;
import org.previmer.ichthyop.arch.IParameterManager;
import org.previmer.ichthyop.arch.IPropertyManager;
import org.previmer.ichthyop.arch.IReleaseManager;
import org.previmer.ichthyop.arch.ISimulation;
import org.previmer.ichthyop.arch.ISimulationManager;
import org.previmer.ichthyop.arch.ITimeManager;
import org.previmer.ichthyop.arch.IZoneManager;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.InitializeListener;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.event.SetupListener;
import org.previmer.ichthyop.io.ParamType;
import org.previmer.ichthyop.io.XParameter;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import org.jdom.input.SAXBuilder;
import org.previmer.ichthyop.calendar.Calendar1900;

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
     * Number of simulations (always 1 for SINGLE mode)
     */
    private int nb_simulations = 1;
    /**
     * Listeners list for SetupEvent and InitializeEvent.
     */
    private EventListenerList listeners = new EventListenerList();
    /**
     * Computer time when the current simulation starts [millisecond]
     */
    private long cpu_start_current;
    /**
     * Computer time when the simulation starts [millisecond]
     */
    private long cpu_start_global;
    /**
     * A flag indicating wheter the simulation has been interrupted
     * or completed.
     */
    private boolean flagStop = false;
    /*
     * The configuration file
     */
    private File cfgFile;
    /*
     * The id of the current simulation ichthyop-run_yyyyMMddHHmm
     */
    private String id;
    /**
     * The date format used for generating the id of the simulation
     */
    private static SimpleDateFormat dtformatterId = new SimpleDateFormat("yyyyMMddHHmm");
    /*
     * The simulation logger that should be used by all the classes that
     * are allowed to dialog with the SimulationManager
     */
    private static final Logger logger = Logger.getLogger(SimulationManager.class.getName());

    /**
     * 
     * @return
     */
    public static SimulationManager getInstance() {
        return simulationManager;
    }

    public Logger getLogger() {
        return logger;
    }

    public void setConfigurationFile(File file) throws IOException {

        cfgFile = null;
        nb_simulations = 1;
        if (file != null) {
            /* Make sure file exists ans is valid */
            if (isValidXML(file)) {
                if (!isValidConfigFile(file)) {
                    throw new IOException(file.getName() + " is not a valid Ichthyop configuration file.");
                }
            }
            cfgFile = file;
            getParameterManager().setConfigurationFile(file);
            for (XParameter xparam : getParameterManager().getParameters(ParamType.SERIAL)) {
                nb_simulations *= xparam.getLength();
            }
            mobiliseManagers();
        }
    }

    private boolean isValidXML(File file) throws IOException {
        try {
            new SAXBuilder().build(file).getRootElement();
        } catch (JDOMException ex) {
            IOException ioex = new IOException("Error occured reading " + file.getName() + " \n" + ex.getMessage(), ex);
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        return true;
    }

    private boolean isValidConfigFile(File file) {
        try {
            return new SAXBuilder().build(file).getRootElement().getName().matches("icstructure");

        } catch (JDOMException ex) {
            return false;
        } catch (IOException ex) {
            return false;
        }
    }

    public File getConfigurationFile() {
        return cfgFile;
    }

    public String getId() {

        if (null == id) {
            id = newId();
        }
        if (this.getNumberOfSimulations() > 1) {
            return id + "_s" + (getIndexSimulation() + 1);
        } else {
            return id;
        }
    }

    public void resetId() {
        id = null;
    }

    private static String newId() {
        StringBuffer strBfRunId = new StringBuffer("ichthyop-run");
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        dtformatterId.setCalendar(calendar);
        strBfRunId.append(dtformatterId.format(calendar.getTime()));
        return strBfRunId.toString();
    }

    public static String getIdFromFile(File file) {
        String filename = file.getName();
        return filename;
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

    /**
     * Calculates the progress of the current simulation
     * @return the progress of the current simulation as a percent
     */
    public float progressCurrent() {
        float progress = (getTimeManager().index() + 1) / (float) getTimeManager().getNumberOfSteps();
        return Math.min(Math.max(progress, 0.f), 1.f);
    }

    /**
     * Calculates the progress of the current simulation compared to the whole
     * sets of simulations.
     * @return the progress of the whole sets of simulations as a percent
     */
    public float progressGlobal() {
        float progress = (i_simulation + (getTimeManager().index() + 1) / (float) getTimeManager().getNumberOfSteps()) / nb_simulations;
        return Math.min(Math.max(progress, 0.f), 1.f);
    }

    /**
     * Estimates the time left to end up the current simulation.
     *
     * @return the time left, formatted in a String
     */
    public String timeLeftCurrent() {

        return timeLeft(progressCurrent(), cpu_start_current);
    }

    /**
     * Estimates the time left to end up the current simulation.
     *
     * @return the time left, formatted in a String
     */
    public String timeLeftGlobal() {

        return timeLeft(progressGlobal(), cpu_start_global);
    }

    private String timeLeft(float progress, long cpu_start) {

        StringBuffer strBf;

        long nbMilliSecLeft = 0L;
        if (progress != 0) {
            nbMilliSecLeft = (long) ((System.currentTimeMillis() - cpu_start) * (1 - progress) / progress);
        }
        int nbHourLeft = (int) (nbMilliSecLeft / Calendar1900.ONE_HOUR);
        int nbMinLeft = (int) ((nbMilliSecLeft - Calendar1900.ONE_HOUR * nbHourLeft) / Calendar1900.ONE_MINUTE);
        int nbSecLeft = (int) ((nbMilliSecLeft - Calendar1900.ONE_HOUR * nbHourLeft - Calendar1900.ONE_MINUTE * nbMinLeft) / Calendar1900.ONE_SECOND);

        strBf = new StringBuffer("Time left ");
        if (nbHourLeft == 0) {
            strBf.append(nbMinLeft);
            strBf.append("min ");
            strBf.append(nbSecLeft);
            strBf.append("s");
        } else {
            strBf.append(nbHourLeft);
            strBf.append("h ");
            strBf.append(nbMinLeft);
            strBf.append("min");
        }

        return strBf.toString();
    }

    private void mobiliseManagers() {
        getActionManager();
        getDatasetManager();
        getOutputManager();
        getReleaseManager();
        getTimeManager();
        getZoneManager();
    }

    public String indexSimulationToString() {
        return (i_simulation + 1) + " / " + nb_simulations;
    }

    public int getIndexSimulation() {
        return i_simulation;
    }

    public void run() {
        resetTimerGlobal();
        do {
            try {
                System.out.println("=========================");
                System.out.println("Simulation " + indexSimulationToString());
                System.out.println("=========================");
                setup();
                init();
                getTimeManager().firstStepTriggered();
                resetTimerCurrent();
                do {
                    System.out.println("-----< " + getTimeManager().timeToString() + " >-----");
                    getSimulation().step();
                    System.out.println(timeLeftGlobal());
                } while (!isStopped() && getTimeManager().hasNextStep());
            } catch (Exception ex) {
                logger.log(Level.SEVERE, null, ex);
            }
        } while (!isStopped() && hasNextSimulation());
        System.out.println("End of simulation");
    }

    public void setup() throws Exception {
        flagStop = false;
        getZoneManager().cleanup();
        fireSetupPerformed();
    }

    public void init() {
        fireInitializePerformed();
    }

    public void stop() {
        getTimeManager().lastStepTriggered();
        flagStop = true;
    }

    public boolean isStopped() {
        return flagStop;
    }

    public void resetTimerCurrent() {
        cpu_start_current = System.currentTimeMillis();
    }

    public void resetTimerGlobal() {
        cpu_start_global = System.currentTimeMillis();
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

    private void fireSetupPerformed() throws Exception {
        getLogger().info("Setting up simulation");
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
       getLogger().info("Initializing simulation");
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
