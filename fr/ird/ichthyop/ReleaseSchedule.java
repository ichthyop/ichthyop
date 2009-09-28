/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.ISimulationAccessor;
import javax.swing.event.EventListenerList;

/**
 *
 * @author pverley
 */
public class ReleaseSchedule implements ISimulationAccessor {

    private final static ReleaseSchedule releaseSchedule = new ReleaseSchedule();
    /** Stores time of the release events */
    private long[] timeEvent;
    /** Index of the current release event */
    private int indexEvent;
    /** Set to true once all released events happened */
    private boolean isAllReleased;
    /** */
    private EventListenerList listeners;

    ReleaseSchedule() {

        indexEvent = 0;
        isAllReleased = false;
        schedule();
        addReleaseListener(getSimulation().getReleaseManager());
    }

    public static ReleaseSchedule getInstance() {
        return releaseSchedule;
    }

    private void schedule() {
        
        timeEvent = new long[findNumberReleaseEvents()];
        for (int i = 0; i < timeEvent.length; i++) {
            timeEvent[i] = Integer.valueOf(getSimulation().getParameterManager().getValue("release.schedule", "event" + i));
        }
    }

    private int findNumberReleaseEvents() {
        int i = 0;
        while (!getSimulation().getParameterManager().getValue("release.schedule", "event" + i).isEmpty())
            i++;
        return i;
    }

    public void step() {

        if (!isAllReleased) {
            long time = getSimulation().getStep().getTime();

            while (!isAllReleased && timeEvent[indexEvent] >= time && timeEvent[indexEvent] < (time + getSimulation().getStep().get_dt())) {
                fireReleaseTriggered();
                indexEvent++;
                isAllReleased = indexEvent >= timeEvent.length;
            }
        }
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
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
}
