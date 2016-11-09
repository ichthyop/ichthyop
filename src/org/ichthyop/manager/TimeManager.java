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

import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import org.ichthyop.event.NextStepEvent;
import org.ichthyop.event.NextStepListener;
import org.ichthyop.calendar.InterannualCalendar;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.LastStepEvent;
import org.ichthyop.event.LastStepListener;
import org.ichthyop.event.SetupEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.event.EventListenerList;
import org.ichthyop.calendar.Day360Calendar;
import org.ichthyop.util.Constant;

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

        // Calendar en origin of time
        Calendar calendar_o = Calendar.getInstance();
        INPUT_DATE_FORMAT.setCalendar(calendar_o);
        calendar_o.setTime(INPUT_DATE_FORMAT.parse(getParameter("time_origin")));
        int year_o = calendar_o.get(Calendar.YEAR);
        int month_o = calendar_o.get(Calendar.MONTH);
        int day_o = calendar_o.get(Calendar.DAY_OF_MONTH);
        int hour_o = calendar_o.get(Calendar.HOUR_OF_DAY);
        int minute_o = calendar_o.get(Calendar.MINUTE);
        if (getParameter("calendar_type").equals(TypeCalendar.CLIMATO.toString())) {
            calendar = new Day360Calendar(year_o, month_o, day_o, hour_o, minute_o);
        } else {
            calendar = new InterannualCalendar(year_o, month_o, day_o, hour_o, minute_o);
        }

        /* initial time */
        try {
            t0 = date2seconds(getParameter("initial_time"));
        } catch (ParseException ex) {
            IOException pex = new IOException("Error converting initial time into seconds ==> " + ex.toString());
            pex.setStackTrace(ex.getStackTrace());
            throw pex;
        }
        calendar.setTimeInMillis((long) (t0 * 1000));
        
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

    public double date2seconds(String date) throws ParseException {
        INPUT_DATE_FORMAT.setCalendar(calendar);
        calendar.setTime(INPUT_DATE_FORMAT.parse(date));
        return calendar.getTimeInMillis() / 1000L;
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
        calendar.setTimeInMillis((long) (time * 1000));
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
        return outputDateFormat.format(calendar.getTime());
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
