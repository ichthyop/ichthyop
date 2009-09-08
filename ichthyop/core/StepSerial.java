package ichthyop.core;

/** import java.text */
import java.text.SimpleDateFormat;

/** import java.util */
import java.util.Calendar;

/** local import */
import ichthyop.io.Configuration;
import ichthyop.util.SerialParameter;
import ichthyop.util.Constant;
import ichthyop.util.calendar.Calendar1900;
import ichthyop.util.calendar.ClimatoCalendar;

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
public class StepSerial implements Cloneable {

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

    /**
     * Creates a new Step
     *
     * @param time_arrow Backward = -1 Forward = +1
     * @param dt_refresh the time between two refreshes of the screen [second]
     */
    public StepSerial() {

        nb_simulations = 1;
        SerialParameter[] parameter = SerialParameter.values();
        for (int i = 0; i < parameter.length; i++) {
            nb_simulations *= parameter[i].length();
        }
        parameter = null;

        calendar = Configuration.getTypeCalendar() == Constant.CLIMATO
                   ? new ClimatoCalendar()
                   : new Calendar1900(Configuration.getTimeOrigin(Calendar.YEAR),
                                      Configuration.getTimeOrigin(Calendar.
                MONTH),
                                      Configuration.getTimeOrigin(Calendar.
                DAY_OF_MONTH));

        dateFormat = new SimpleDateFormat(
                (calendar.getClass() == Calendar1900.class)
                ? "yyyy/MM/dd HH:mm:ss"
                : "yy/MM/dd HH:mm:ss");
        dateFormat.setCalendar(calendar);

        i_simulation = -1;
        dt = Configuration.get_dt();
        dt_record = Configuration.getDtRecord();
        reset();
        cpu_start = System.currentTimeMillis();
    }

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
     * Checks for the existence of a new set of parameters. Systematically
     * returns false for SINGLE mode.
     * @return <code>true</code> if a new set of parameters is available;
     * <code>false</code> otherwise
     */
    public boolean nextSimulation() {

        for (SerialParameter param : SerialParameter.values()) {
            if (param.hasNext()) {
                param.increment();
                this.reset();
                return true;
            } else {
                param.reset();
            }
        }
        return false;
    }

    /**
     * Resets indexes and time values at the beginning of a new simulation.
     */
    private void reset() {

        i_simulation++;
        i_step = 0;
        t0 = Configuration.get_t0(SerialParameter.TO.index());
        time = t0;
        calendar.setTimeInMillis(t0 * 1000L);
        long releaseDuration = Configuration.getReleaseDt(SerialParameter.
                PULSATION.
                index()) *
                               (Configuration.getNbReleaseEvents(
                                       SerialParameter.
                                       PULSATION.index()) - 1);
        simuDuration = Configuration.getTransportDuration() + releaseDuration;
        nb_steps = (int) (simuDuration / dt);
    }

    /**
     * Estimates the time left to end up the run.
     *
     * @return the time left, formatted in a String
     */
    public String timeLeft() {

        StringBuffer strBf;

        int offset = RunBatch.getIndexToStart();
        //long nbMilliSec = cpu_now - cpu_start;
        float x = ((i_simulation - offset) + (i_step + 1) / (float) nb_steps) /
                  (nb_simulations - offset);
        long nbMilliSecLeft = 0L;
        if (x != 0) {
            nbMilliSecLeft = (long) ((cpu_now - cpu_start) * (1 - x) / x);
        }
        int nbDayLeft = (int) (nbMilliSecLeft / Calendar1900.ONE_DAY);
        int nbHourLeft = (int) ((nbMilliSecLeft
                                 - Calendar1900.ONE_DAY * nbDayLeft)
                                / Calendar1900.ONE_HOUR);
        int nbMinLeft = (int) ((nbMilliSecLeft
                                - Calendar1900.ONE_DAY * nbDayLeft
                                - Calendar1900.ONE_HOUR * nbHourLeft)
                               / Calendar1900.ONE_MINUTE);

        strBf = new StringBuffer("Time left ");
        strBf.append(nbDayLeft);
        strBf.append("d ");
        strBf.append(nbHourLeft);
        strBf.append("h ");
        strBf.append(nbMinLeft);
        strBf.append("min");

        return strBf.toString();
    }

    /**
     * Creates and returns a copy of this object.
     * @return an Object, a clone of this instance.
     */
    @Override
    public StepSerial clone() {

        StepSerial step = null;
        try {
            step = (StepSerial)super.clone();
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

    //---------- End of class
}
