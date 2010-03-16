/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.arch.ITimeManager;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.event.NextStepListener;
import org.previmer.ichthyop.calendar.Calendar1900;
import org.previmer.ichthyop.calendar.ClimatoCalendar;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.LastStepEvent;
import org.previmer.ichthyop.event.LastStepListener;
import org.previmer.ichthyop.event.SetupEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import javax.swing.JFormattedTextField;
import javax.swing.event.EventListenerList;
import javax.xml.datatype.Duration;

/**
 *
 * @author pverley
 */
public class TimeManager extends AbstractManager implements ITimeManager {

///////////////////////////////
// Declaration of the constants
///////////////////////////////

    private static final int ONE_SECOND = 1;
    private static final int ONE_MINUTE = 60 * ONE_SECOND;
    private static final int ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;
    private static final long ONE_YEAR = 365 * ONE_DAY;
    
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
     * The simple date format parses and formats dates in human readable format.
     */
    private SimpleDateFormat outputDateFormat;
    private SimpleDateFormat inputDateFormat = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");
    private SimpleDateFormat inputDurationFormat = new SimpleDateFormat("DDDD 'day(s)' HH 'hour(s)' mm 'minute(s)'");
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
        transportDuration = duration2seconds(getParameter("app.time", "transport_duration"));
        if (getParameter("app.time", "calendar_type").matches("climato")) {
            calendar = new ClimatoCalendar();
        } else {
            String time_origin = getParameter("app.time", "time_origin");
            calendar = new Calendar1900(Calendar1900.getTimeOrigin(time_origin, Calendar.YEAR),
                    Calendar1900.getTimeOrigin(time_origin, Calendar.MONTH),
                    Calendar1900.getTimeOrigin(time_origin, Calendar.DAY_OF_MONTH));
        }
        t0 = date2seconds(getParameter("app.time", "initial_time"));
        calendar.setTimeInMillis(t0 * 1000L);
        outputDateFormat = new SimpleDateFormat(
                (calendar.getClass() == Calendar1900.class)
                ? "yyyy/MM/dd HH:mm:ss"
                : "yy/MM/dd HH:mm:ss");
        outputDateFormat.setCalendar(calendar);
    }

    /**
     * 
     * @param duration format: getInputDurationFormat()
     * @return
     */
    public long duration2seconds(String duration) {
        long seconds = 0L;
        NumberFormat nbFormat = NumberFormat.getInstance();
        nbFormat.setParseIntegerOnly(true);
        nbFormat.setGroupingUsed(false);
        try {
            seconds = nbFormat.parse(duration.substring(duration.indexOf("hour") + 8, duration.indexOf("minute"))).longValue()
                    * ONE_MINUTE
                    + nbFormat.parse(duration.substring(duration.indexOf("day") + 7,
                    duration.indexOf("hour")).trim()).longValue()
                    * ONE_HOUR
                    + nbFormat.parse(duration.substring(0, duration.indexOf("day")).trim()).longValue()
                    * ONE_DAY;
            //System.out.println("seconds " + seconds);
            return seconds;
        } catch (ParseException ex) {
            // Voluntarily ignore the exception
        }
        return 0;
    }

    public long date2seconds(String date) {
        Calendar lcalendar = (Calendar) calendar.clone();
        inputDateFormat.setCalendar(lcalendar);
        try {
            lcalendar.setTime(inputDateFormat.parse(date));
            return lcalendar.getTimeInMillis() / 1000L;
        } catch (ParseException ex) {
            Logger.getLogger(TimeManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return 0;
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
        calendar.setTimeInMillis(time * 1000L);
        if (Math.abs(time - t0) < simuDuration) {
            fireNextStepTriggered();
            i_step++;
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

    public void lastStepTriggered() {
        fireLastStepTriggered();
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

    public SimpleDateFormat getInputDurationFormat() {
        return inputDurationFormat;
    }

    public SimpleDateFormat getInputDateFormat() {
        return inputDateFormat;
    }
    //---------- End of class
}