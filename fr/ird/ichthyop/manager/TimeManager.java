/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.arch.ITimeManager;
import fr.ird.ichthyop.event.NextStepEvent;
import fr.ird.ichthyop.event.NextStepListener;
import fr.ird.ichthyop.calendar.Calendar1900;
import fr.ird.ichthyop.calendar.ClimatoCalendar;
import fr.ird.ichthyop.event.InitializeEvent;
import fr.ird.ichthyop.event.LastStepEvent;
import fr.ird.ichthyop.event.LastStepListener;
import fr.ird.ichthyop.event.SetupEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;

/**
 *
 * @author pverley
 */
public class TimeManager extends AbstractManager implements ITimeManager {

    private final static TimeManager timeManager = new TimeManager();
    /**
     * Current time of the simulation [second]
     */
    private long time;
    /**
     * Begining of the simulation [second]
     */
    private long t0;
    /**
     * Transport duration [second]
     */
    private long transportDuration;
    /**
     * Simulation duration [second]
     */
    private long simuDuration;
    /**
     * Simulation time step [second]
     */
    private int dt;
    /**
     * Index of the current step
     */
    private int i_step;
    /**
     * Number of simulated steps
     */
    private int nb_steps;
    /**
     * Index of the current simulation (always 0 for SINGLE mode)
     */
    private int i_simulation;
    /**
     * A Calendar for time management
     */
    private Calendar calendar;
    /**
     * The simple date format parses and formats dates in human readable format.
     */
    private SimpleDateFormat dateFormat;
    private EventListenerList listeners = new EventListenerList();

///////////////
// Constructors
///////////////
////////////////////////////
// Definition of the methods
////////////////////////////
    public static TimeManager getInstance() {
        return timeManager;
    }

    public void setUp() {

        loadParameters();
    }

    public void init() {
        simuDuration = transportDuration + getSimulationManager().getReleaseManager().getReleaseDuration();
        i_step = 0;
        time = t0;
        nb_steps = (int) (simuDuration / dt);
    }

    public void firstStepTriggered() {
        fireNextStepTriggered();
    }

    private void loadParameters() {
        dt = Integer.valueOf(getParameter("app.time", "time_step"));
        boolean isForward = getParameter("app.time", "time_arrow").matches("forward");
        if (!isForward) {
            dt *= -1;
        }
        t0 = Long.valueOf(getParameter("app.time", "initial_time"));
        //Logger.getAnonymousLogger().info("time-step: " + dt + " - t0: " + t0);
        transportDuration = Long.valueOf(getParameter("app.time", "transport_duration"));
        calendar = new ClimatoCalendar();
        calendar.setTimeInMillis(t0 * 1000L);
        dateFormat = new SimpleDateFormat(
                (calendar.getClass() == Calendar1900.class)
                ? "yyyy/MM/dd HH:mm:ss"
                : "yy/MM/dd HH:mm:ss");
        dateFormat.setCalendar(calendar);
    }

    private String getParameter(String blockName, String key) {
        return getSimulationManager().getParameterManager().getParameter(blockName, key);
    }

    /**
     * Increments the current time of the simulation and checks whether the
     * simulation should go on or end up, function of the simulation duration.
     *
     * @return <code>true</code> if the incremented time is still smaller than
     * the end time of the simulation; <code>false</code> otherwise.
     */
    public boolean hasNextStep() {

        time += dt;
        if (Math.abs(time - t0) < simuDuration) {
            i_step++;
            calendar.setTimeInMillis(time * 1000L);
            fireNextStepTriggered();
            return true;
        }
        fireLastStepTriggered();
        return false;
    }

    /**
     * Gets the current time of the simulation as a String
     * @return the current time of the simulation, formatted in a String
     */
    public String timeToString() {
        return dateFormat.format(calendar.getTime());
    }

    /**
     * Gets the index of the current step
     * @return the index of the current step
     */
    public int index() {
        return i_step;
    }

    /**
     * Gets the index of the current simulation
     * @return the index of the current simulation. Systematically returns
     * zero for SINGLE mode.
     */
    public int indexSimulation() {
        return i_simulation;
    }

    /**
     * The number of steps of the current simulation.
     * @return the number of steps of the current simulation.
     */
    public int getNumberOfSteps() {
        return nb_steps;
    }

    /**
     * Gets the current time of the simulation
     * @return a long, the current time [second] of the simulation
     */
    public long getTime() {
        return time;
    }

    public long get_tO() {
        return t0;
    }

    /**
     * Gets the calendar used for time management
     * @return the Calendar of the simulation
     */
    public Calendar getCalendar() {
        return calendar;
    }

    /**
     * Gets the simulation duration
     * @return a long, the simulation duration [second]
     */
    public long getSimulationDuration() {
        return simuDuration;
    }

    /**
     * Gets the computational time step.
     * @return the time step [second] used in the model
     */
    public int get_dt() {
        return dt;
    }

    public long getTransportDuration() {
        return transportDuration;
    }

    public void addNextStepListener(NextStepListener listener) {
        listeners.add(NextStepListener.class, listener);
    }

    /**
     * Removes the specified listener from the parameter
     * @param listener the ValueListener
     */
    public void removeNextListenerListener(NextStepListener listener) {
        listeners.remove(NextStepListener.class, listener);
    }

    public void addLastStepListener(LastStepListener listener) {
        listeners.add(LastStepListener.class, listener);
    }

    /**
     * Removes the specified listener from the parameter
     * @param listener the ValueListener
     */
    public void removeLastListenerListener(LastStepListener listener) {
        listeners.remove(LastStepListener.class, listener);
    }

    private void fireNextStepTriggered() {

        //Logger.getAnonymousLogger().info("-----< " + timeManager.timeToString() + " >-----");

        NextStepListener[] listenerList = (NextStepListener[]) listeners.getListeners(NextStepListener.class);

        for (NextStepListener listener : listenerList) {
            listener.nextStepTriggered(new NextStepEvent(this));
        }
    }

    private void fireLastStepTriggered() {

        LastStepListener[] listenerList = (LastStepListener[]) listeners.getListeners(LastStepListener.class);

        for (LastStepListener listener : listenerList) {
            listener.lastStepOccurred(new LastStepEvent(this));
        }
    }

    public void setupPerformed(SetupEvent e) {
        loadParameters();
    }

    public void initializePerformed(InitializeEvent e) {
        simuDuration = transportDuration + getSimulationManager().getReleaseManager().getReleaseDuration();
        i_step = 0;
        time = t0;
        nb_steps = (int) (simuDuration / dt);
    }
    //---------- End of class
}
