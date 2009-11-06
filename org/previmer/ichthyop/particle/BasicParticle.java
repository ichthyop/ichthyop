/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IBasicParticle;

/**
 *
 * @author pverley
 */
public abstract class BasicParticle extends GridPoint implements IBasicParticle {

    private int index;
    private long age = 0;
    private String deathCause;
    private boolean living = true;
    private boolean locked = false;
    private List<ParticleLayer> layers;

    public ParticleLayer getLayer(Class layerClass) {
        for (ParticleLayer layer : layers) {
            if (layer.getClass().getCanonicalName().matches(layerClass.getCanonicalName())) {
                return layer;
            }
        }
        try {
            return (ParticleLayer) layerClass.getConstructor(IBasicParticle.class).newInstance(this);
        } catch (InstantiationException ex) {
            Logger.getLogger(BasicParticle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(BasicParticle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(BasicParticle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(BasicParticle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(BasicParticle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(BasicParticle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

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
