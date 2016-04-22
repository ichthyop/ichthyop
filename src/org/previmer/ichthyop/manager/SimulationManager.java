/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import org.jdom.JDOMException;
import org.previmer.ichthyop.Simulation;
import org.previmer.ichthyop.dataset.IDataset;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.InitializeListener;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.event.SetupListener;
import org.previmer.ichthyop.io.ParamType;
import org.previmer.ichthyop.io.XParameter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import org.jdom.input.SAXBuilder;
import org.previmer.ichthyop.calendar.InterannualCalendar;
import org.previmer.ichthyop.ui.logging.SystemOutHandler;
import org.previmer.ichthyop.util.IchthyopLogFormatter;

/**
 *
 * @author pverley
 */
public class SimulationManager {

    final private static SimulationManager simulationManager = new SimulationManager();
    /**
     * Index of the current simulation (always 0 for SINGLE mode)
     */
    private int i_simulation;
    /**
     * *
     * Number of simulations (always 1 for SINGLE mode)
     */
    private int nb_simulations = 1;
    /**
     * Listeners list for SetupEvent and InitializeEvent.
     */
    private final EventListenerList listeners = new EventListenerList();
    /**
     * Computer time when the current simulation starts [millisecond]
     */
    private long cpu_start_current;
    /**
     * Computer time when the simulation starts [millisecond]
     */
    private long cpu_start_global;
    /**
     * A flag indicating wheter the simulation has been interrupted or
     * completed.
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
    private static final SimpleDateFormat dtformatterId = new SimpleDateFormat("yyyyMMddHHmm");
    /*
     * The simulation logger that should be used by all the classes that
     * are allowed to dialog with the SimulationManager
     */
    private static final Logger logger = Logger.getAnonymousLogger();
    /*
     * Whether the simulation has been setup or not 
     */
    private boolean isSetup;

    /**
     *
     * @return
     */
    public static SimulationManager getInstance() {
        return simulationManager;
    }

    public static Logger getLogger() {
        return logger;
    }

    public void setupLogger() {

        // setup the logger
        logger.setUseParentHandlers(false);
        IchthyopLogFormatter formatter = new IchthyopLogFormatter(true);
        SystemOutHandler sh = new SystemOutHandler();
        sh.setFormatter(formatter);
        logger.addHandler(sh);
        logger.setLevel(Level.INFO);
    }

    public void setConfigurationFile(File file) throws Exception {

        cfgFile = null;
        nb_simulations = 1;
        isSetup = false;
        if (file != null) {
            /* Make sure file exists */
            if (!file.isFile()) {
                throw new FileNotFoundException("Configuration file " + file.getPath() + " not found.");
            }
            if (!file.canRead()) {
                throw new IOException("Configuration file " + file.getPath() + " cannot be read");
            }
            /* Make sure file is valid */
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
            return new SAXBuilder().build(file).getRootElement().getName().equals("icstructure");

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
        i_simulation = 0;
    }

    private static String newId() {
        StringBuilder strBfRunId = new StringBuilder("ichthyop-run");
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
     *
     * @return <code>true</code> if a new set of parameters is available;
     * <code>false</code> otherwise
     */
    public boolean hasNextSimulation() {

        if (flagStop) {
            return false;
        }

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
     *
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
     *
     * @return the progress of the current simulation as a percent
     */
    public float progressCurrent() {
        float progress = (getTimeManager().index() + 1) / (float) getTimeManager().getNumberOfSteps();
        return Math.min(Math.max(progress, 0.f), 1.f);
    }

    /**
     * Calculates the progress of the current simulation compared to the whole
     * sets of simulations.
     *
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
        int nbHourLeft = (int) (nbMilliSecLeft / InterannualCalendar.ONE_HOUR);
        int nbMinLeft = (int) ((nbMilliSecLeft - InterannualCalendar.ONE_HOUR * nbHourLeft) / InterannualCalendar.ONE_MINUTE);
        int nbSecLeft = (int) ((nbMilliSecLeft - InterannualCalendar.ONE_HOUR * nbHourLeft - InterannualCalendar.ONE_MINUTE * nbMinLeft) / InterannualCalendar.ONE_SECOND);

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

    /**
     * Order is of primary importance since the setup events and the
     * initialization events will be called in the same order they are called
     * here.
     */
    private void mobiliseManagers() {
        /* the very first one, since most of the other managers will need it
         later on */
        getDatasetManager();

        /* Time manager must come after the release manager because the 
         calculation of the simulation duration required the release schedule */
        getReleaseManager();
        getTimeManager();

        /* It'd better come after TimeManager in case some actions need to
         access some time information */
        getActionManager();

        /* Zone manager must be called  after the action manager and the
         release manager */
        getZoneManager();

        /* the very last one, because it sums up all the setup info in order
         to record it in the NetCDF output file */
        getOutputManager();
    }

    public String indexSimulationToString() {
        return (i_simulation + 1) + " / " + nb_simulations;
    }

    public int getIndexSimulation() {
        return i_simulation;
    }

    public void setup() throws Exception {
        flagStop = false;
        getZoneManager().cleanup();
        fireSetupPerformed();
        isSetup = true;
    }

    public boolean isSetup() {
        return isSetup;
    }

    public void init() throws Exception {
        fireInitializePerformed();
    }

    public void stop() {
        flagStop = true;
        getTimeManager().lastStepTriggered();
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

    public Simulation getSimulation() {
        return Simulation.getInstance();
    }

    public void addSetupListener(SetupListener listener) {
        listeners.add(SetupListener.class, listener);
    }

    public void removeSetupListener(SetupListener listener) {
        listeners.remove(SetupListener.class, listener);
    }

    private void fireSetupPerformed() throws Exception {
        SetupListener[] listenerList = (SetupListener[]) listeners.getListeners(SetupListener.class);
        for (int i = listenerList.length; i-- > 0;) {
            SetupListener listener = listenerList[i];
            listener.setupPerformed(new SetupEvent(this));
        }
    }

    public void addInitializeListener(InitializeListener listener) {
        listeners.add(InitializeListener.class, listener);
    }

    public void removeInitializeListener(InitializeListener listener) {
        listeners.remove(InitializeListener.class, listener);
    }

    private void fireInitializePerformed() throws Exception {
        InitializeListener[] listenerList = (InitializeListener[]) listeners.getListeners(InitializeListener.class);
        for (int i = listenerList.length; i-- > 0;) {
            InitializeListener listener = listenerList[i];
            listener.initializePerformed(new InitializeEvent(this));
        }
    }

    public DatasetManager getDatasetManager() {
        return DatasetManager.getInstance();
    }

    public IDataset getDataset() {
        return getDatasetManager().getDataset();
    }

    public ActionManager getActionManager() {
        return ActionManager.getInstance();
    }

    public ParameterManager getParameterManager() {
        return ParameterManager.getInstance();
    }

    public PropertyManager getPropertyManager(Class forClass) {
        return PropertyManager.getInstance(forClass);
    }

    public ZoneManager getZoneManager() {
        return ZoneManager.getInstance();
    }

    public ReleaseManager getReleaseManager() {
        return ReleaseManager.getInstance();
    }

    public OutputManager getOutputManager() {
        return OutputManager.getInstance();
    }

    public TimeManager getTimeManager() {
        return TimeManager.getInstance();
    }
}
