/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IBasicParticle;

/**
 *
 * @author pverley
 */
public abstract class BasicParticle extends RhoPoint implements IBasicParticle {

    private int index;
    private long age = 0;
    private String deathCause;
    private boolean living = true;
    private boolean locked = false;

    public boolean isLiving() {
        return living;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public long getAge() {
        return age;
    }

    public void incrementAge() {
        age += getSimulationManager().getTimeManager().get_dt();
    }

    public void kill(String cause) {

        this.deathCause = cause;
        living = false;
        setLon(Double.NaN);
        setLat(Double.NaN);
        setDepth(Double.NaN);
    }

    public String getDeathCause() {
        if (deathCause != null && !deathCause.isEmpty()) {
            return deathCause;
        } else {
            return "not-dead-yet";
        }
    }

    public boolean isLocked() {
        return locked;
    }

    public void lock() {
        locked = true;
    }

    public void unlock() {
        locked = false;
    }
}
