package fr.ird.ichthyop;

/** import java.text */
import fr.ird.ichthyop.event.NextStepEvent;
import fr.ird.ichthyop.event.NextStepListener;
import fr.ird.ichthyop.calendar.Calendar1900;
import fr.ird.ichthyop.calendar.ClimatoCalendar;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.ISimulationAccessor;
import fr.ird.ichthyop.arch.IStep;
import fr.ird.ichthyop.event.LastStepEvent;
import fr.ird.ichthyop.event.LastStepListener;
import java.text.SimpleDateFormat;

/** import java.util */
import java.util.Calendar;
import javax.swing.event.EventListenerList;

/**
 * The class handles the march of the simulation throught time. It deals with
 * the timing of the simulation: beginning, time-step, current time, index of
 * the current step, existence of next step, time to refresh the display,
 * time to write a new record in output file, etc...
 * <p>The class is connected to the Configuration fil and informs the MainFrame
 * about the number of sets of parameters predefined by the user (a single
 * set for SINGLE mode and several sets for the SERIAL). It returns
 * the appropriate set of parameters for each simulation.</p>
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
public class Step implements IStep, ISimulationAccessor {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    private final static Step step = new Step();
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
     * Time between two records in the output file [second]
     */
    private int dt_record;
    /**
     * Time between two refreshes of the screen [second]
     */
    private int dt_refresh;
    /**
     * Computer time when the simulation starts [millisecond]
     */
    private long cpu_start;
    /**
     * Computer time at the current step of the simulation [millisecond]
     */
    private long cpu_now;
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
    /***
     * Number of simulations (always 1 for SERIAL mode)
     */
    private int nb_simulations;
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
    Step() {
        loadParameters();
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    public static Step getInstance() {
        return step;
    }

    public void setUp() {

        i_step = 0;
        time = t0;
        nb_steps = (int) (simuDuration / dt);
        cpu_start = System.currentTimeMillis();
        addNextStepListener(getSimulation().getReleaseManager().getSchedule());
        fireNextStepTriggered();
    }

    private void loadParameters() {
        dt = Integer.valueOf(getParameter("app.time", "time_step"));
        boolean isForward = getParameter("app.time", "time_arrow").matches("forward");
        if (!isForward) {
            dt *= -1;
        }
        t0 = Long.valueOf(getParameter("app.time", "initial_time"));
        transportDuration = Long.valueOf(getParameter("app.time", "transport_duration"));
        simuDuration = transportDuration + getSimulation().getReleaseManager().getSchedule().getReleaseDuration();
        calendar = new ClimatoCalendar();
        calendar.setTimeInMillis(t0 * 1000L);
        dateFormat = new SimpleDateFormat(
                (calendar.getClass() == Calendar1900.class)
                ? "yyyy/MM/dd HH:mm:ss"
                : "yy/MM/dd HH:mm:ss");
        dateFormat.setCalendar(calendar);
    }

    private String getParameter(String blockName, String key) {
        return getSimulation().getParameterManager().getValue(blockName, key);
    }

    /**
     * Increments the current time of the simulation and checks whether the
     * simulation should go on or end up, function of the simulation duration.
     *
     * @return <code>true</code> if the incremented time is still smaller than
     * the end time of the simulation; <code>false</code> otherwise.
     */
    public boolean hasNext() {

        time += dt;
        if (Math.abs(time - t0) < simuDuration) {
            i_step++;
            calendar.setTimeInMillis(time * 1000L);
            cpu_now = System.currentTimeMillis();
            fireNextStepTriggered();
            return true;
        }
        fireLastStepTriggered();
        return false;
    }

    /**
     * Creates and returns a copy of this object.
     * @return an Object, a clone of this instance.
     */
    @Override
    public Step clone() {

        Step clone = null;
        try {
            clone = (Step) super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        clone.calendar = (Calendar) calendar.clone();
        clone.dateFormat = (SimpleDateFormat) dateFormat.clone();

        return clone;
    }

    /**
     * Gets the current time of the simulation as a String
     * @return the current time of the simulation, formatted in a String
     */
    public String timeToString() {
        return dateFormat.format(calendar.getTime());
    }

    /**
     * Checks whether it is time to write down a new record in the output file,
     * function of <code>dt_record</code>
     * @return <code>true</code>if a new record should be added to the output
     * file;<code>false</code> otherwise.
     */
    public boolean hasToRecord() {
        return ((time - t0) % dt_record) == 0;
    }

    /**
     * Checks whether it is time to refresh the screen, function of
     * <code>dt_refresh</code>
     * @return <code>true</code>if the screen should be refreshed;
     * <code>false</code> otherwise.
     */
    public boolean hasToRefresh() {
        return ((time - t0) % dt_refresh) == 0;
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
     * Gets the number of simulations (which equals the number of sets of
     * parameters predefined by the user). Returns 1 for SINGLE mode.
     * @return the number of simulations.
     */
    public int getNumberOfSimulations() {
        return nb_simulations;
    }

    /**
     * The number of steps of the current simulation.
     * @return the number of steps of the current simulation.
     */
    public int getNumberOfSteps() {
        return nb_steps;
    }

    /**
     * Calculates the progress of the current simulation
     * @return the progress of the current simulation as a percent
     */
    public int progressCurrent() {
        return (int) (100.f * (i_step + 1) / nb_steps);
    }

    /**
     * Calculates the progress of the current simulation compared to the whole
     * sets of simulations.
     * @return the progress of the whole sets of simulations as a percent
     */
    public int progressGlobal() {
        return (int) (100.f * (i_simulation + (i_step + 1) / (float) nb_steps) / nb_simulations);
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

    public void next() {
        /*if (this.hasToRefresh()) {
        //fireRefreshUIEvent();
        }
        if (this.hasToRecord()) {
        //fireRecordEvent();
        }*/
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
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

        NextStepListener[] listenerList = (NextStepListener[]) listeners.getListeners(
                NextStepListener.class);

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
    //---------- End of class
}
