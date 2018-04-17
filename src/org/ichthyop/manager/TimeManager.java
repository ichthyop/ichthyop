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
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.LastStepEvent;
import org.ichthyop.event.LastStepListener;
import org.ichthyop.event.SetupEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.event.EventListenerList;
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

    final private static String TIME_KEY = "time";

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    private final static TimeManager TIME_MANAGER = new TimeManager();
    /**
     * Beginning of the simulation [second]
     */
//    private Date t0;
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

    private double elapsed;
    /**
     * Number of simulated steps
     */
    private int nsteps;
    /**
     * Determine whether particle should keep drifting when age exceeds
     * transport duration
     */
    private boolean keepDrifting;
    /**
     * Computer time when the current simulation starts [millisecond]
     */
    private long cpu_start;

    private final EventListenerList listeners = new EventListenerList();

////////////////////////////
// Definition of the methods
////////////////////////////
    public static TimeManager getInstance() {
        return TIME_MANAGER;
    }

    private void loadParameters() throws Exception {

        // time step
        dt = getConfiguration().getInt(TIME_KEY + ".time_step");

        // time direction */
        boolean isForward = getConfiguration().getString(TIME_KEY + ".time_arrow").equals(TimeDirection.FORWARD.toString());
        if (!isForward) {
            dt *= -1;
        }

        // transport duration */
        try {
            transportDuration = duration2seconds(getConfiguration().getString(TIME_KEY + ".transport_duration"));
        } catch (ParseException ex) {
            IOException pex = new IOException("Error converting transport duration into seconds ==> " + ex.toString());
            pex.setStackTrace(ex.getStackTrace());
            throw pex;
        }

        // keep drifting ?
        keepDrifting = getConfiguration().getBoolean(TIME_KEY + ".keep_drifting");

        // ellapsed time
        elapsed = 0.d;
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

    public double date2seconds(String date, Calendar calendar) throws ParseException {

        INPUT_DATE_FORMAT.setCalendar(calendar);
        calendar.setTime(INPUT_DATE_FORMAT.parse(date));
        return calendar.getTimeInMillis() / 1000L;
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

        if ((elapsed + dt) < simuDuration) {
            fireNextStepTriggered();
            i_step++;
            elapsed += dt;
            return true;
        } else {
            lastStepTriggered();
            return false;
        }
    }

    /**
     * Gets the current time of the simulation as a String
     *
     * @param seconds
     * @return the current time of the simulation, formatted in a String
     */
    private String timeToString(double seconds) {

        if (0 == seconds) {
            return "0 second";
        }
        
        double one_day = 3600 * 24;
        int nd = (int) (seconds / one_day);
        int nh = (int) (seconds - nd * one_day) / 3600;
        int nm = (int) (seconds - nd * one_day - nh * 3600) / 60;
        int ns = (int) (seconds - nd * one_day - nh * 3600 - nm * 60);
        StringBuilder sb = new StringBuilder();
        if (nd > 0) {
            sb.append(nd).append(" day");
            sb.append(nd > 1 ? "s " : " ");
        }
        if (nh > 0) {
            sb.append(nh).append(" hour");
            sb.append(nh > 1 ? "s " : " ");
        }
        if (nm > 0) {
            sb.append(nm).append(" minute");
            sb.append(nm > 1 ? "s " : " ");
        }
        if (ns > 0) {
            sb.append(ns).append(" second");
            if (ns > 1) {
                sb.append("s");
            }
        }
        return sb.toString();
    }

    public String timeToString() {
        return timeToString(elapsed);
    }

    public String printProgress() {

        StringBuilder msg = new StringBuilder();
        msg.append(i_step + 1);
        msg.append(" / ");
        msg.append(nsteps);
        float progress = progress();
        if (i_step > 0 && ((i_step + 1) % 10 == 0)) {
            msg.append(" (Simulated time ");
            msg.append(timeToString());
            msg.append(", simulation progress ");
            msg.append((int) (progress * 100));
            msg.append("%");
            if (progress > 0.01) {
                msg.append(", time left ");
                double seconds = 1e-3d * (System.currentTimeMillis() - cpu_start) * (100.d - progress) / progress;
                msg.append(timeToString(seconds));
            }
            msg.append(")");
        }
        return msg.toString();

    }

    /**
     * Calculates the progress of the current simulation
     *
     * @return the progress of the current simulation as a percent
     */
    public int progress() {
        int progress = (int) (100.f * i_step / (float) (nsteps - 1));
        return Math.min(Math.max(progress, 0), 100);
    }

    public void resetTimer() {
        cpu_start = System.currentTimeMillis();
    }

    /**
     * Gets the current time of the simulation
     *
     * @return a long, the current time [second] of the simulation
     */
    public double getTime() {
        return elapsed;
    }

    public double get_tO(Calendar calendar) {
        try {
            return date2seconds(getConfiguration().getString(TIME_KEY + ".initial_time"), calendar);
        } catch (ParseException ex) {
            error("Failed to convert parameter " + TIME_KEY + ".initial_time into seconds", ex);
            return Double.NaN;
        }
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
        info("Time manager setup [OK]");
    }

    @Override
    public void initializePerformed(InitializeEvent e) throws Exception {
        simuDuration = transportDuration + getSimulationManager().getReleaseManager().getReleaseDuration();
        i_step = 0;
        elapsed = 0;
        nsteps = (int) Math.ceil(Math.abs(simuDuration / (double) dt));
        info("Time manager initialization [OK]");
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
}
