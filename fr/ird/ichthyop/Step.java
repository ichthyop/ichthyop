package fr.ird.ichthyop;

/** import java.text */
import java.text.SimpleDateFormat;

/** import java.util */
import java.util.Calendar;

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
public class Step implements IStep {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * Current time of the simulation [second]
     */
    private long time;
    /**
     * Begining of the simulation [second]
     */
    private long t0;
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

///////////////
// Constructors
///////////////

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Increments the current time of the simulation and checks whether the
     * simulation should go on or end up, function of the simulation duration.
     *
     * @return <code>true</code> if the incremented time is still smaller than
     * the end time of the simulation; <code>false</code> otherwise.
     */
    public boolean next() {

        time += dt;
        if (Math.abs(time - t0) < simuDuration) {
            i_step++;
            calendar.setTimeInMillis(time * 1000L);
            cpu_now = System.currentTimeMillis();
            return true;
        }
        return false;
    }

    /**
     * Creates and returns a copy of this object.
     * @return an Object, a clone of this instance.
     */
    @Override
    public Step clone() {

        Step step = null;
        try {
            step = (Step)super.clone();
        } catch (CloneNotSupportedException ex) {
            ex.printStackTrace();
        }
        step.calendar = (Calendar) calendar.clone();
        step.dateFormat = (SimpleDateFormat) dateFormat.clone();

        return step;
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
        return (int) (100.f * (i_simulation + (i_step + 1) / (float) nb_steps)
                      / nb_simulations);
    }

    /**
     * Gets the current time of the simulation
     * @return a long, the current time [second] of the simulation
     */
    public long getTime() {
        return time;
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    //---------- End of class
}
