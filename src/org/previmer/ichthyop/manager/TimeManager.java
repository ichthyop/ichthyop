/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.event.NextStepListener;
import org.previmer.ichthyop.calendar.InterannualCalendar;
import org.previmer.ichthyop.calendar.ClimatoCalendar;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.LastStepEvent;
import org.previmer.ichthyop.event.LastStepListener;
import org.previmer.ichthyop.event.SetupEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.event.EventListenerList;
import org.previmer.ichthyop.util.Constant;

/**
 *
 * @author pverley
 */
public class TimeManager extends AbstractManager {
    
///////////////////////////////
// Declaration of the constants
///////////////////////////////
    public static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");
    public static final SimpleDateFormat INPUT_DURATION_FORMAT = new SimpleDateFormat("DDDD 'day(s)' HH 'hour(s)' mm 'minute(s)'");

///////////////////////////////
// Declaration of the variables
///////////////////////////////
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
     * A Calendar for time management
     */
    private Calendar calendar;
    /**
     * Determine whether particle should keep drifting when age exceeds transport duration
     */
    private boolean keepDrifting;
    /**
     * The simple date format parses and formats dates in human readable format.
     */
    private SimpleDateFormat outputDateFormat;
    private EventListenerList listeners = new EventListenerList();

////////////////////////////
// Definition of the methods
////////////////////////////
    public static TimeManager getInstance() {
        return timeManager;
    }

    public void firstStepTriggered() throws Exception {
        fireNextStepTriggered();
    }

    private void loadParameters() throws Exception {

        /* time step */
        dt = Integer.valueOf(getParameter("time_step"));

        /* time direction */
        boolean isForward = getParameter("time_arrow").matches(TimeDirection.FORWARD.toString());
        if (!isForward) {
            dt *= -1;
        }

        /* transport duration */
        try {
            transportDuration = duration2seconds(getParameter("transport_duration"));
        } catch (ParseException ex) {
            IOException pex = new IOException("Error converting transport duration into seconds ==> " + ex.toString());
            pex.setStackTrace(ex.getStackTrace());
            throw pex;
        }

        /* keep drifting ?*/
        keepDrifting = Boolean.valueOf(getParameter("keep_drifting"));

        if (getParameter("calendar_type").matches(TypeCalendar.CLIMATO.toString())) {
            calendar = new ClimatoCalendar();
        } else {
            calendar = new InterannualCalendar(getParameter("time_origin"), INPUT_DATE_FORMAT);
        }

        /* initial time */
        try {
            t0 = date2seconds(getParameter("initial_time"));
        } catch (ParseException ex) {
            IOException pex = new IOException("Error converting initial time into seconds ==> " + ex.toString());
            pex.setStackTrace(ex.getStackTrace());
            throw pex;
        }
        calendar.setTimeInMillis(t0 * 1000L);

        /* output date format */
        outputDateFormat = new SimpleDateFormat(
                (calendar.getClass() == InterannualCalendar.class)
                ? "yyyy/MM/dd HH:mm:ss"
                : "yy/MM/dd HH:mm:ss");
        outputDateFormat.setCalendar(calendar);
    }

    public boolean keepDrifting() {
        return keepDrifting;
    }

    /**
     * 
     * @param duration format: getInputDurationFormat()
     * @return
     */
    public long duration2seconds(String duration) throws ParseException {
        long seconds = 0L;
        NumberFormat nbFormat = NumberFormat.getInstance();
        nbFormat.setParseIntegerOnly(true);
        nbFormat.setGroupingUsed(false);

        seconds = nbFormat.parse(duration.substring(duration.indexOf("hour") + 8, duration.indexOf("minute"))).longValue()
                * Constant.ONE_MINUTE
                + nbFormat.parse(duration.substring(duration.indexOf("day") + 7,
                duration.indexOf("hour")).trim()).longValue()
                * Constant.ONE_HOUR
                + nbFormat.parse(duration.substring(0, duration.indexOf("day")).trim()).longValue()
                * Constant.ONE_DAY;
        //System.out.println("seconds " + seconds);
        return seconds;
    }

    public long date2seconds(String date) throws ParseException {
        Calendar lcalendar = (Calendar) calendar.clone();
        INPUT_DATE_FORMAT.setCalendar(lcalendar);
        lcalendar.setTime(INPUT_DATE_FORMAT.parse(date));
        return lcalendar.getTimeInMillis() / 1000L;
    }
    
    public long day2seconds(String day) throws ParseException {
        long x= Long.valueOf(day);
        x= x* 24 * 3600;
        return x;
    }

    private String getParameter(String key) {
        return getSimulationManager().getParameterManager().getParameter("app.time", key);
    }

    /**
     * Increments the current time of the simulation and checks whether the
     * simulation should go on or end up, function of the simulation duration.
     *
     * @return <code>true</code> if the incremented time is still smaller than
     * the end time of the simulation; <code>false</code> otherwise.
     */
    public boolean hasNextStep() throws Exception {

        time += dt;
        calendar.setTimeInMillis(time * 1000L);
        if (Math.abs(time - t0) < simuDuration) {
            fireNextStepTriggered();
            i_step++;
            return true;
        }
        lastStepTriggered();
        return false;
    }

    /**
     * Gets the current time of the simulation as a String
     * @return the current time of the simulation, formatted in a String
     */
    public String timeToString() {
        return outputDateFormat.format(calendar.getTime());
    }

    /**
     * Gets the index of the current step
     * @return the index of the current step
     */
    public int index() {
        return i_step;
    }

    public String stepToString() {
        StringBuffer strBf = new StringBuffer("Step ");
        strBf.append(index() + 1);
        strBf.append(" / ");
        strBf.append(getNumberOfSteps());
        return strBf.toString();
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

    private void cleanNextStepListener() {
        NextStepListener[] listenerList = (NextStepListener[]) listeners.getListeners(
                NextStepListener.class);

        for (NextStepListener listener : listenerList) {
            removeNextListenerListener(listener);
        }
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

    private void cleanLastStepListener() {
        LastStepListener[] listenerList = (LastStepListener[]) listeners.getListeners(
                LastStepListener.class);

        for (LastStepListener listener : listenerList) {
            removeLastListenerListener(listener);
        }
    }

    private void fireNextStepTriggered() throws Exception {

        //Logger.getAnonymousLogger().info("-----< " + timeManager.timeToString() + " >-----");

        NextStepListener[] listenerList = (NextStepListener[]) listeners.getListeners(NextStepListener.class);

        for (int i = listenerList.length; i-- > 0;) {
            NextStepListener listener = listenerList[i];
            listener.nextStepTriggered(new NextStepEvent(this, getSimulationManager().isStopped()));
        }
    }

    public void lastStepTriggered() {
        fireLastStepTriggered();
    }

    private void fireLastStepTriggered() {

        LastStepListener[] listenerList = (LastStepListener[]) listeners.getListeners(LastStepListener.class);

        for (LastStepListener listener : listenerList) {
            listener.lastStepOccurred(new LastStepEvent(this, getSimulationManager().isStopped()));
        }
    }

    public void setupPerformed(SetupEvent e) throws Exception {
        cleanNextStepListener();
        cleanLastStepListener();
        loadParameters();
        getLogger().info("Time manager setup [OK]");
    }

    public void initializePerformed(InitializeEvent e) throws Exception {
        simuDuration = transportDuration + getSimulationManager().getReleaseManager().getReleaseDuration();
        i_step = 0;
        time = t0;
        nb_steps = (int) Math.abs(simuDuration / dt);
        getLogger().info("Time manager initialization [OK]");
    }

    public SimpleDateFormat getInputDurationFormat() {
        return INPUT_DURATION_FORMAT;
    }

    public SimpleDateFormat getInputDateFormat() {
        return INPUT_DATE_FORMAT;
    }
    //---------- End of class

    public enum TimeDirection {

        FORWARD,
        BACKWARD;

        @Override
        public String toString() {
            return name().toLowerCase();
        }
    }

    public enum TypeCalendar {

        CLIMATO("Climatology calendar"),
        GREGORIAN("Gregorian calendar");
        private String name;

        TypeCalendar(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
