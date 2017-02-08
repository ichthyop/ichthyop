/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package org.ichthyop.manager;

import org.jdom2.JDOMException;
import org.ichthyop.Simulation;
import org.ichthyop.dataset.IDataset;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.InitializeListener;
import org.ichthyop.event.SetupEvent;
import org.ichthyop.event.SetupListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;
import org.jdom2.input.SAXBuilder;
import org.ichthyop.calendar.InterannualCalendar;
import org.ichthyop.ui.logging.SystemOutHandler;
import org.ichthyop.util.IchthyopLogFormatter;

/**
 *
 * @author pverley
 */
public class SimulationManager {

    final private static SimulationManager simulationManager = new SimulationManager();
    /**
     * Listeners list for SetupEvent and InitializeEvent.
     */
    private final EventListenerList listeners = new EventListenerList();
    /**
     * Computer time when the current simulation starts [millisecond]
     */
    private long cpu_start;
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

        } catch (JDOMException | IOException ex) {
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
        return id;
    }

    public void resetId() {
        id = null;
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
     * Calculates the progress of the current simulation
     *
     * @return the progress of the current simulation as a percent
     */
    public float progress() {
        float progress = (getTimeManager().index() + 1) / (float) getTimeManager().getNumberOfSteps();
        return Math.min(Math.max(progress, 0.f), 1.f);
    }

    /**
     * Estimates the time left to end up the current simulation.
     *
     * @return the time left, formatted in a String
     */
    public String timeLeft() {

        return timeLeft(progress(), cpu_start);
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

    public void resetTimer() {
        cpu_start = System.currentTimeMillis();
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
