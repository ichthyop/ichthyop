/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.manager;

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.event.NextStepListener;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.LastStepEvent;
import org.previmer.ichthyop.event.LastStepListener;
import org.previmer.ichthyop.event.SetupEvent;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    public static final String datePattern = "'year' yyyy 'month' MM 'day' dd 'at' HH:mm";
    public static final String durationPattern = "DDD 'day(s)' HH 'hour(s)' mm 'minute(s)'";
    public static final DateTimeFormatter NEW_INPUT_DATE_FORMAT = DateTimeFormatter.ofPattern("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");
    public static final SimpleDateFormat INPUT_DATE_FORMAT = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");
    public static final SimpleDateFormat INPUT_DURATION_FORMAT = new SimpleDateFormat("DDD 'day(s)' HH 'hour(s)' mm 'minute(s)'");
    public static final DateTimeFormatter NEW_INPUT_DURATION_FORMAT = DateTimeFormatter.ofPattern("DDD 'day(s)' HH 'hour(s)' mm 'minute(s)'");
    public static final int YEAR_REF = 1900;
    
    // Array containing the latest day of each month. Used only when no leap calendar is used.
    public static final double[] monthEdges = {31,  59,  90, 120, 151, 181, 212, 243, 273, 304, 334, 365};
    
    /** Reference date: 1900-01-01 00:00 */
    public static final LocalDateTime DATE_REF = LocalDateTime.of(YEAR_REF, 1, 1, 0, 0);

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    private final static TimeManager timeManager = new TimeManager();
    /**
     * Current time of the simulation [second]
     */
    private double time;
    /**
     * Beginning of the simulation [second]
     */
    private double t0;
    /**
     * Transport duration [second]
     */
    private double transportDuration;
    /**
     * Simulation duration [second]
     */
    private double simuDuration;
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
     * Determine whether particle should keep drifting when age exceeds
     * transport duration
     */
    private boolean keepDrifting;
    /**
     * The simple date format parses and formats dates in human readable format.
     */
    private SimpleDateFormat outputDateFormat;
    private final EventListenerList listeners = new EventListenerList();
    
    private boolean noLeapCalendarEnabled;
    
    private IchthyopDuration ichthyopDuration;
    
    /** Interface for getting the duration between two dates. Function
     * called depends on the calendar type.
    */
    @FunctionalInterface
    public interface IchthyopDuration {
        public double getDuration(LocalDateTime DATE_REF, LocalDateTime date0);        
    }
    
    public boolean isNoLeapEnabled() {
        return noLeapCalendarEnabled;   
    }
    
////////////////////////////
// Definition of the methods
////////////////////////////
    public static TimeManager getInstance() {
        return timeManager;
    }
    
    /** Method for getting the time in seconds since reference date for gregorian 
     * calendar. */
    public static double getDurationLeap(LocalDateTime DATE_REF, LocalDateTime date0) {
        return Duration.between(DATE_REF, date0).getSeconds();
    }
    
    /**
     * Method for getting the time in seconds since reference date for noleap
     * calendar.
     */
    public static double getDurationNoLeap(LocalDateTime DATE_REF, LocalDateTime date0) {
        
        int year = date0.getYear();
        int month = date0.getMonth().getValue();
        int day = date0.getDayOfMonth();
        int hour = date0.getHour();
        int minute = date0.getMinute();
        int seconds = date0.getSecond();
        double month_offset = (month == 1) ? 0 : monthEdges[month - 2] * Constant.ONE_DAY;
        double duration = month_offset + (year - YEAR_REF) * Constant.ONE_YEAR + (day - 1) * Constant.ONE_DAY
                + hour * Constant.ONE_HOUR + minute * Constant.ONE_MINUTE + seconds;
        return duration;
    
    }

    public void firstStepTriggered() throws Exception {
        fireNextStepTriggered();
    }

    private void loadParameters() throws Exception {
        
        // Define whether to use the no leap calendar or not.
        // If no parameter is found, assume that gregorian calendar is considered.
        noLeapCalendarEnabled = false;
        try {
            String calendar = getParameter("calendar");
            if (calendar.toLowerCase() == "noleap") {
                noLeapCalendarEnabled = true;
            } else {
                noLeapCalendarEnabled = false;
            }
        } catch (NullPointerException ex) {
            noLeapCalendarEnabled = false;
        }
        
        if (noLeapCalendarEnabled) {
            ichthyopDuration = (date1, date2) -> getDurationNoLeap(date1, date2);
        } else {
            ichthyopDuration = (date1, date2) -> getDurationLeap(date1, date2);
        }
                
        /* time step */
        dt = Integer.valueOf(getParameter("time_step"));

        /* time direction */
        boolean isForward = getParameter("time_arrow").equals(TimeDirection.FORWARD.toString());
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
        LocalDateTime date0 = LocalDateTime.parse(getParameter("initial_time"), NEW_INPUT_DATE_FORMAT);

        // Conversion of date0 as t0, i.e. the number of seconds between the reference date and the current date.
        t0 = ichthyopDuration.getDuration(DATE_REF, date0);

        /* output date format */
        outputDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        outputDateFormat.setCalendar(calendar);
        
    }

    public boolean keepDrifting() {
        return keepDrifting;
    }

    /**
     *
     * @param duration format: getInputDurationFormat()
     * @return
     * @throws java.text.ParseException
     */
    public double duration2seconds(String duration) throws ParseException {
        double seconds;
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

    /** Converts the date as number of seconds since 1900-01-01 */
    public double date2seconds(String date) throws ParseException {
        LocalDateTime dateTime = LocalDateTime.parse(date, NEW_INPUT_DATE_FORMAT);
        double duration = ichthyopDuration.getDuration(DATE_REF, dateTime);
        return (double) duration;
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
     * @throws java.lang.Exception
     */
    public boolean hasNextStep() throws Exception {

        time += dt;
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
     *
     * @return the current time of the simulation, formatted in a String
     */
    public String timeToString() {
        LocalDateTime currentDate = DATE_REF.plusSeconds((long) time);
        return currentDate.toString();
    }

    /**
     * Gets the index of the current step
     *
     * @return the index of the current step
     */
    public int index() {
        return i_step;
    }

    public String stepToString() {
        StringBuilder strBf = new StringBuilder("Step ");
        strBf.append(index() + 1);
        strBf.append(" / ");
        strBf.append(getNumberOfSteps());
        return strBf.toString();
    }

    /**
     * The number of steps of the current simulation.
     *
     * @return the number of steps of the current simulation.
     */
    public int getNumberOfSteps() {
        return nb_steps;
    }

    /**
     * Gets the current time of the simulation
     *
     * @return a long, the current time [second] of the simulation
     */
    public double getTime() {
        return time;
    }

    public double get_tO() {
        return t0;
    }

    /**
     * Gets the calendar used for time management
     *
     * @return the Calendar of the simulation
     */
    public Calendar getCalendar() {
        return calendar;
    }

    /**
     * Gets the simulation duration
     *
     * @return a long, the simulation duration [second]
     */
    public double getSimulationDuration() {
        return simuDuration;
    }

    /**
     * Gets the computational time step.
     *
     * @return the time step [second] used in the model
     */
    public int get_dt() {
        return dt;
    }

    public double getTransportDuration() {
        return transportDuration;
    }

    public void addNextStepListener(NextStepListener listener) {
        listeners.add(NextStepListener.class, listener);
    }

    /**
     * Removes the specified listener from the parameter
     *
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
     *
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

    @Override
    public void setupPerformed(SetupEvent e) throws Exception {
        cleanNextStepListener();
        cleanLastStepListener();
        loadParameters();
        getLogger().info("Time manager setup [OK]");
    }

    @Override
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
    
    public DateTimeFormatter getNewInputDurationFormat() {
        return NEW_INPUT_DURATION_FORMAT;
    }

    public DateTimeFormatter getNewInputDateFormat() {
        return NEW_INPUT_DATE_FORMAT;
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
        private final String name;

        TypeCalendar(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
