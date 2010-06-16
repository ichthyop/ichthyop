/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import java.io.IOException;
import java.text.ParseException;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.arch.IReleaseProcess;
import org.previmer.ichthyop.arch.IReleaseManager;
import org.previmer.ichthyop.event.ReleaseListener;
import org.previmer.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;

/**
 *
 * @author pverley
 */
public class ReleaseManager extends AbstractManager implements IReleaseManager {

    private static final ReleaseManager releaseManager = new ReleaseManager();
    private IReleaseProcess releaseProcess;
    /** Stores time of the release events */
    private long[] timeEvent;
    /** Index of the current release event */
    private int indexEvent;
    /** Set to true once all released events happened */
    private boolean isAllReleased;
    /** */
    private EventListenerList listeners = new EventListenerList();

    public static IReleaseManager getInstance() {
        return releaseManager;
    }

    private void instantiateReleaseProcess() throws Exception {

        XBlock releaseBlock = findActiveReleaseProcess();
        String className = getParameter(releaseBlock.getKey(), "class_name");
        if (releaseBlock != null) {
            try {
                releaseProcess = (IReleaseProcess) Class.forName(className).newInstance();
                releaseProcess.loadParameters();
            } catch (Exception ex) {
                StringBuffer sb = new StringBuffer();
                sb.append("Release process instantiation failed ==> ");
                sb.append(ex.toString());
                InstantiationException ieex = new InstantiationException(sb.toString());
                ieex.setStackTrace(ex.getStackTrace());
                throw ieex;
            }
        }
    }

    private IReleaseProcess getReleaseProcess() {
        return releaseProcess;
    }

    public String getParameter(String releaseKey, String key) {
        return getSimulationManager().getParameterManager().getParameter(BlockType.RELEASE, releaseKey, key);
    }

    private XBlock findActiveReleaseProcess() throws Exception {
        List<XBlock> list = new ArrayList();
        for (XBlock block : getSimulationManager().getParameterManager().getBlocks(BlockType.RELEASE)) {
            if (block.isEnabled()) {
                list.add(block);
            }
        }
        if (list.isEmpty()) {
            throw new NullPointerException("Could not find any enabled " + BlockType.RELEASE.toString() + " block in the configuration file.");
        }
        if (list.size() > 1) {
            throw new IOException("Found several " + BlockType.RELEASE.toString() + " blocks enabled in the configuration file. Please only keep one enabled.");
        }
        return list.get(0);
    }

    public void releaseTriggered(ReleaseEvent event) throws Exception {
        getReleaseProcess().release(event);
    }

    public int getNbParticles() {
        return getReleaseProcess().getNbParticles();
    }

    public void nextStepTriggered(NextStepEvent e) throws Exception {

        if (!isAllReleased) {
            long time = e.getSource().getTime();

            while (!isAllReleased && timeEvent[indexEvent] >= time && timeEvent[indexEvent] < (time + e.getSource().get_dt())) {
                fireReleaseTriggered();
                indexEvent++;
                isAllReleased = indexEvent >= timeEvent.length;
            }
        }
    }

    private long get_t0() throws Exception {
        String iniTime = getSimulationManager().getParameterManager().getParameter("app.time", "initial_time");
        try {
            return getSimulationManager().getTimeManager().date2seconds(iniTime);
        } catch (ParseException ex) {
            StringBuffer sb = new StringBuffer();
            sb.append("Release schedule - error converting initial time into seconds ==> ");
            sb.append(ex.toString());
            IOException ioex = new IOException(sb.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    private void schedule() throws Exception {

        boolean isEnabled = false;
        try {
            String isScheduleEnabled = getSimulationManager().getParameterManager().getParameter("release.schedule", "is_enabled");
            isEnabled = Boolean.valueOf(isScheduleEnabled);
        } catch (NullPointerException ex) {
            getLogger().log(Level.WARNING, ex.toString() + ". By default, particles will all be released at simulation initial time.");
        }
        if (isEnabled) {
            String[] events = getReleaseEvents();
            timeEvent = new long[events.length];
            long t0 = get_t0();
            String st0 = getSimulationManager().getParameterManager().getParameter("app.time", "initial_time");
            for (int i = 0; i < timeEvent.length; i++) {
                try {
                    timeEvent[i] = getSimulationManager().getTimeManager().date2seconds(events[i]);
                    if (timeEvent[i] < t0) {
                        throw new IndexOutOfBoundsException("Release event " + events[i] + " cannot occur prior to simulation initial time " + st0);
                    }
                } catch (ParseException ex) {
                    StringBuffer sb = new StringBuffer();
                    sb.append("Release schedule - error converting release time into seconds ==> ");
                    sb.append(ex.toString());
                    IOException ioex = new IOException(sb.toString());
                    ioex.setStackTrace(ex.getStackTrace());
                    throw ioex;
                }
            }
        } else {
            timeEvent = new long[]{get_t0()};
        }
    }

    private String[] getReleaseEvents() throws Exception {
        String[] tokens = getSimulationManager().getParameterManager().getParameter("release.schedule", "events").split("\"");
        List<String> events = new ArrayList();
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                events.add(token.trim());
            }
        }
        return events.toArray(new String[events.size()]);
    }

    /**
     * Adds the specified value listener to receive ValueChanged events from
     * the paremeter.
     *
     * @param listener the ValueListener
     */
    public void addReleaseListener(ReleaseListener listener) {
        listeners.add(ReleaseListener.class, listener);
    }

    /**
     * Removes the specified listener from the parameter
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

    public long getReleaseDuration() {
        return timeEvent[getNbReleaseEvents() - 1] - timeEvent[0];
    }

    public void setupPerformed(SetupEvent e) throws Exception {
        cleanReleaseListener();
        indexEvent = 0;
        isAllReleased = false;
        releaseProcess = null;
        instantiateReleaseProcess();
        getLogger().info("Release manager setup [OK]");
    }

    public void initializePerformed(InitializeEvent e) throws Exception {
        addReleaseListener(this);
        getSimulationManager().getTimeManager().addNextStepListener(this);
        schedule();
        getLogger().info("Release manager initialization [OK]");
    }
}
