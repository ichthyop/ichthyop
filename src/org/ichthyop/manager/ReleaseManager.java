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
import java.text.ParseException;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.NextStepEvent;
import org.ichthyop.event.SetupEvent;
import org.ichthyop.event.ReleaseEvent;
import org.ichthyop.event.ReleaseListener;
import javax.swing.event.EventListenerList;
import org.ichthyop.event.NextStepListener;
import org.ichthyop.release.AbstractRelease;

/**
 *
 * @author pverley
 */
public class ReleaseManager extends AbstractManager implements ReleaseListener, NextStepListener {

    private static final ReleaseManager RELEASE_MANAGER = new ReleaseManager();
    private AbstractRelease releaseProcess;
    /**
     * Stores time of the release events
     */
    private double[] timeEvent;
    /**
     * Index of the current release event
     */
    private int indexEvent;
    /**
     * Set to true once all released events happened
     */
    private boolean isAllReleased;
    /**
     *
     */
    private final EventListenerList listeners = new EventListenerList();

    public static ReleaseManager getInstance() {
        return RELEASE_MANAGER;
    }

    private void instantiateReleaseProcess() throws Exception {

        String[] keys = getConfiguration().getParameterSets();
        for (String key : keys) {
            if (getConfiguration().canFind(key + ".type")
                    && getConfiguration().getString(key + ".type").equalsIgnoreCase("release")) {
                if (getConfiguration().getBoolean(key + ".enabled")) {
                    String className = getConfiguration().getString(key + ".class_name");
                    try {
                        releaseProcess = (AbstractRelease) Class.forName(className).newInstance();
                        releaseProcess.loadParameters();
                    } catch (Exception ex) {
                        StringBuilder sb = new StringBuilder();
                        sb.append("Release process instantiation failed ==> ");
                        sb.append(ex.toString());
                        InstantiationException ieex = new InstantiationException(sb.toString());
                        ieex.setStackTrace(ex.getStackTrace());
                        throw ieex;
                    }
                }
            }
        }
    }

    @Override
    public void releaseTriggered(ReleaseEvent event) throws Exception {
        int nbReleased = releaseProcess.release(event);
        StringBuilder sb = new StringBuilder();
        sb.append("Release event (");
        sb.append(getClass().getSimpleName());
        sb.append(") time = ");
        sb.append((long) event.getSource().getTime());
        sb.append(" seconds. Released ");
        sb.append(nbReleased);
        sb.append(" particles.");
        info(sb.toString());
    }

    public int getNbParticles() {
        return getNbReleaseEvents() * releaseProcess.getNbParticles();
    }

    @Override
    public void nextStepTriggered(NextStepEvent event) throws Exception {

        if (!isAllReleased) {

            while (canRelease(event)) {
                fireReleaseTriggered();
                indexEvent++;
                isAllReleased = indexEvent >= timeEvent.length;
            }
        }
    }

    private boolean canRelease(NextStepEvent e) {

        double time = e.getSource().getTime();
        int dt = e.getSource().get_dt();
        boolean isForward = Math.signum(dt) > 0;

        boolean canRelease = isForward
                ? !isAllReleased && timeEvent[indexEvent] >= time && timeEvent[indexEvent] < (time + e.getSource().get_dt())
                : !isAllReleased && timeEvent[indexEvent] <= time && timeEvent[indexEvent] > (time + e.getSource().get_dt());

        return canRelease;
    }

    private double get_t0() throws Exception {
        String iniTime = getSimulationManager().getParameterManager().getString("app.time.initial_time");
        try {
            return getSimulationManager().getTimeManager().date2seconds(iniTime);
        } catch (ParseException ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("Release schedule - error converting initial time into seconds ==> ");
            sb.append(ex.toString());
            IOException ioex = new IOException(sb.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    private void schedule() throws Exception {

        String[] events = getReleaseEvents();
        timeEvent = new double[events.length];
        double t0 = get_t0();
        int arrow = (getSimulationManager().getTimeManager().get_dt()) > 0 ? 1 : -1;
        String st0 = getSimulationManager().getParameterManager().getString("app.time.initial_time");
        for (int i = 0; i < timeEvent.length; i++) {
            try {
                timeEvent[i] = getSimulationManager().getTimeManager().date2seconds(events[i]);
                if (arrow * timeEvent[i] < arrow * t0) {
                    throw new IndexOutOfBoundsException("Release event " + events[i] + " cannot occur prior to simulation initial time " + st0);
                }
            } catch (ParseException ex) {
                IOException ioex = new IOException("{Release schedule} Error converting release time into seconds ==> " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
        }
    }

    private String[] getReleaseEvents() throws Exception {
        if (getConfiguration().getBoolean("release.schedule.is_enabled")) {
            return getConfiguration().getArrayString("release.schedule.events");
        } else {
            return new String[]{getConfiguration().getString("app.time.initial_time")};
        }
    }

    /**
     * Adds the specified value listener to receive ValueChanged events from the
     * parameter.
     *
     * @param listener the ValueListener
     */
    public void addReleaseListener(ReleaseListener listener) {
        listeners.add(ReleaseListener.class, listener);
    }

    /**
     * Removes the specified listener from the parameter
     *
     * @param listener the ValueListener
     */
    public void removeReleaseListener(ReleaseListener listener) {
        listeners.remove(ReleaseListener.class, listener);
    }

    private void cleanReleaseListener() {
        ReleaseListener[] listenerList = (ReleaseListener[]) listeners.getListeners(
                ReleaseListener.class);

        for (ReleaseListener listener : listenerList) {
            removeReleaseListener(listener);
        }
    }

    private void fireReleaseTriggered() throws Exception {

        //Logger.getLogger(getClass().getName()).info("Triggered release event " + indexEvent);
        ReleaseListener[] listenerList = (ReleaseListener[]) listeners.getListeners(ReleaseListener.class);

        for (ReleaseListener listener : listenerList) {
            listener.releaseTriggered(new ReleaseEvent(this));
        }
    }

    public int getIndexEvent() {
        return indexEvent;
    }

    public double getTime() {
        return timeEvent[indexEvent];
    }

    public int getNbReleaseEvents() {
        return timeEvent.length;
    }

    /**
     * Release duration is defined as time elapsed between the begining of the
     * simulation and the last release event.
     *
     * @return a long, the release duration in seconds.
     */
    public double getReleaseDuration() {
        return timeEvent[getNbReleaseEvents() - 1] - getSimulationManager().getTimeManager().get_tO();
    }

    @Override
    public void setupPerformed(SetupEvent e) throws Exception {
        cleanReleaseListener();
        indexEvent = 0;
        isAllReleased = false;
        releaseProcess = null;
        timeEvent = new double[getReleaseEvents().length];
        instantiateReleaseProcess();
        info("Release manager setup [OK]");
    }

    @Override
    public void initializePerformed(InitializeEvent e) throws Exception {
        addReleaseListener(this);
        getSimulationManager().getTimeManager().addNextStepListener(this);
        schedule();
        info("Release manager initialization [OK]");
    }
}
