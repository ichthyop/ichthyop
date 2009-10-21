/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.event.InitializeEvent;
import fr.ird.ichthyop.event.NextStepEvent;
import fr.ird.ichthyop.event.SetupEvent;
import fr.ird.ichthyop.io.BlockType;
import fr.ird.ichthyop.event.ReleaseEvent;
import fr.ird.ichthyop.arch.IReleaseProcess;
import fr.ird.ichthyop.arch.IReleaseManager;
import fr.ird.ichthyop.event.ReleaseListener;
import fr.ird.ichthyop.event.SetupListener;
import fr.ird.ichthyop.io.XBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.EventListenerList;

/**
 *
 * @author pverley
 */
public class ReleaseManager extends AbstractManager implements IReleaseManager, SetupListener {

    private static final ReleaseManager releaseManager = new ReleaseManager();
    private IReleaseProcess releaseProcess;
    private XBlock releaseBlock;
    /** Stores time of the release events */
    private long[] timeEvent;
    /** Index of the current release event */
    private int indexEvent;
    /** Set to true once all released events happened */
    private boolean isAllReleased;
    /** */
    private EventListenerList listeners = new EventListenerList();

    public ReleaseManager() {
        super();
        addReleaseListener(this);
    }

    public static IReleaseManager getInstance() {
        return releaseManager;
    }

    private IReleaseProcess getReleaseProcess() {
        if (releaseProcess == null) {
            try {
                releaseBlock = findActiveReleaseProcess();
                if (releaseBlock != null) {
                    releaseProcess = (IReleaseProcess) Class.forName(releaseBlock.getParameter("class_name").getValue()).newInstance();
                }
            } catch (InstantiationException ex) {
                Logger.getLogger(ReleaseManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                ex.printStackTrace();
                Logger.getLogger(ReleaseManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ReleaseManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return releaseProcess;
    }

    public XBlock getXReleaseProcess(String key) {
        return getSimulationManager().getParameterManager().getBlock(BlockType.RELEASE, key);
    }

    public String getParameter(String key) {
        return releaseBlock.getParameter(key).getValue();
    }

    private XBlock findActiveReleaseProcess() {
        List<XBlock> list = new ArrayList();
        for (XBlock block : getSimulationManager().getParameterManager().getBlocks(BlockType.RELEASE)) {
            if (block.isEnabled()) {
                list.add(block);
            }
        }
        if (list.size() > 0 && list.size() < 2) {
            return list.get(0);
        } else {
            return null;
        }
    }

    public void releaseTriggered(ReleaseEvent event) {
        getReleaseProcess().release(event);
    }

    public int getNbParticles() {
        return getReleaseProcess().getNbParticles();
    }

    public void nextStepTriggered(NextStepEvent e) {

        if (!isAllReleased) {
            long time = e.getSource().getTime();

            while (!isAllReleased && timeEvent[indexEvent] >= time && timeEvent[indexEvent] < (time + e.getSource().get_dt())) {
                fireReleaseTriggered();
                indexEvent++;
                isAllReleased = indexEvent >= timeEvent.length;
            }
        }
    }

    public void setUp() {
        indexEvent = 0;
        isAllReleased = false;
        schedule();
    }

    private void schedule() {

        timeEvent = new long[findNumberReleaseEvents()];
        for (int i = 0; i < timeEvent.length; i++) {
            timeEvent[i] = Integer.valueOf(getSimulationManager().getParameterManager().getParameter("release.schedule", "event" + i));
        }
    }

    private int findNumberReleaseEvents() {
        int i = 0;
        while (!getSimulationManager().getParameterManager().getParameter("release.schedule", "event" + i).isEmpty())
            i++;
        //Logger.getLogger(ReleaseSchedule.class.getName()).log(Level.CONFIG, "Number release events: " + i);
        return i;
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

    private void fireReleaseTriggered() {

        //Logger.getLogger(getClass().getName()).info("Triggered release event " + indexEvent);

        ReleaseListener[] listenerList = (ReleaseListener[]) listeners.getListeners(
                ReleaseListener.class);

        for (ReleaseListener listener : listenerList) {
            listener.releaseTriggered(new ReleaseEvent(this));
        }
    }

    public int getIndexEvent() {
        return indexEvent;
    }

    public int getNbReleaseEvents() {
        return timeEvent.length;
    }

    public long getReleaseDuration() {
        return timeEvent[getNbReleaseEvents() - 1] - timeEvent[0];
    }

    public void setupPerformed(SetupEvent e) {
        indexEvent = 0;
        isAllReleased = false;
        schedule();
    }

    public void initializePerformed(InitializeEvent e) {
        // do nothing
    }
}
