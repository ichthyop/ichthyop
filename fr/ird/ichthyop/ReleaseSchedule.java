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

    /** Stores time of the release events */
    private long[] timeEvent;
    /** Index of the current release event */
    private int indexEvent;
    /** Set to true once all released events happened */
    private boolean isAllReleased;
    EventListenerList listeners;

    ReleaseSchedule() {

        indexEvent = 0;
        isAllReleased = false;
        schedule();
    }

    private void schedule() {
        timeEvent = new long[1];
        timeEvent[0] = 0;
    }

    private void step() {

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
}
