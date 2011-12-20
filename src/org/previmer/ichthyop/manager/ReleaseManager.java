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
import org.previmer.ichthyop.event.ReleaseListener;
import org.previmer.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.swing.event.EventListenerList;
import org.previmer.ichthyop.event.NextStepListener;

/**
 *
 * @author pverley
 */
public class ReleaseManager extends AbstractManager implements ReleaseListener, NextStepListener {

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

    public static ReleaseManager getInstance() {
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
        // le seul cas que j'ai fait est le release par défaut, ichthyop evol ne fait pas encore les autres modes de release.
        int nbReleased = getReleaseProcess().release(event);
        StringBuffer sb = new StringBuffer();
        sb.append("Release event (");
        sb.append(getClass().getSimpleName());
        sb.append(") time = ");
        sb.append((long) event.getSource().getTime());
        sb.append(" seconds. Released ");
        sb.append(nbReleased);
        sb.append(" particles.");
        getLogger().info(sb.toString());
    }

    public int getNbParticles() {
        return getNbReleaseEvents() * getReleaseProcess().getNbParticles();
    }

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

        long time = e.getSource().getTime();
        int dt = e.getSource().get_dt();
        boolean isForward = Math.signum(dt) > 0;

        boolean canRelease = isForward
                ? !isAllReleased && timeEvent[indexEvent] >= time && timeEvent[indexEvent] < (time + e.getSource().get_dt())
                : !isAllReleased && timeEvent[indexEvent] <= time && timeEvent[indexEvent] > (time + e.getSource().get_dt());

        return canRelease;
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
                IOException ioex = new IOException("{Release schedule} Error converting release time into seconds ==> " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
        }
    }
    
    private String[] getReleaseEvents() throws Exception {

        try {
            String isScheduleEnabled = getSimulationManager().getParameterManager().getParameter("release.schedule", "is_enabled");
            boolean isEnabled = Boolean.valueOf(isScheduleEnabled);
            if (isEnabled) {
                String[] tokens = getSimulationManager().getParameterManager().getParameter("release.schedule", "events").split("\"");
                List<String> events = new ArrayList();
                for (String token : tokens) {
                    if (!token.trim().isEmpty()) {
                        events.add(token.trim());
                    }
                }
                return events.toArray(new String[events.size()]);
            }
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Failed to read the release schedule. By default, particles will all be released at simulation initial time. " + ex.toString());
        }
        String st0 = getSimulationManager().getParameterManager().getParameter("app.time", "initial_time");
        return new String[]{st0};
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

    /**
     * Release duration is defined as time elapsed between the begining of the
     * simulation and the last release event.
     * @return a long, the release duration in seconds.
     */
    public long getReleaseDuration() {
        return timeEvent[getNbReleaseEvents() - 1] - getSimulationManager().getTimeManager().get_tO();
    }

    public void setupPerformed(SetupEvent e) throws Exception {
        cleanReleaseListener();
        indexEvent = 0;
        isAllReleased = false;
        releaseProcess = null;
        timeEvent = new long[getReleaseEvents().length];
        instantiateReleaseProcess();
        getLogger().info("Release manager setup [OK]");
    }

    public void initializePerformed(InitializeEvent e) throws Exception {
        addReleaseListener(this);
        getSimulationManager().getTimeManager().addNextStepListener(this);
        // if (SimulationManager.getInstance().testEvol()
        //         && SimulationManager.getInstance().getTimeManager().getTime()
        //         <= SimulationManager.getInstance().getTimeManager().get_tO() + 31449600) {
        //     evolSchedule();
        // } else {
        schedule();
        // }
        getLogger().info("Release manager initialization [OK]");
    }

    public double getMinDepth() {
        return Double.parseDouble(getSimulationManager().getParameterManager().getParameter("release.schedule", "depth_min"));

    }

    public double getMaxDepth() {
        return Double.parseDouble(getSimulationManager().getParameterManager().getParameter("release.schedule", "depth_max"));

    }
    
    public int getReleaseFrequency(){
        return Integer.parseInt(getSimulationManager().getParameterManager().getParameter("release.schedule", "release_frequency"));
    }
    
    public String getTimeBeginning(){
        return getSimulationManager().getParameterManager().getParameter("app.time", "initial_time");
    }
    
}
