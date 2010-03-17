/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.arch.IMasterParticle;

/**
 *
 * @author pverley
 */
public class MasterParticle extends GridPoint implements IMasterParticle {

    private int index;
    private long age = 0;
    private ParticleMortality deathCause;
    private boolean living = true;
    private boolean locked = false;
    private List<ParticleLayer> layers = new ArrayList();

    public ParticleLayer getLayer(Class layerClass) {
        for (ParticleLayer layer : layers) {
            if (layer.getClass().getCanonicalName().matches(layerClass.getCanonicalName())) {
                return layer;
            }
        }
        try {
            ParticleLayer layer = (ParticleLayer) layerClass.getConstructor(IBasicParticle.class).newInstance(this);
            layers.add(layer);
            return layer;
        } catch (InstantiationException ex) {
            Logger.getLogger(MasterParticle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(MasterParticle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(MasterParticle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(MasterParticle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(MasterParticle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(MasterParticle.class.getName()).log(Level.SEVERE, null, ex);
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

    public void kill(ParticleMortality cause) {

        this.deathCause = cause;
        living = false;
        setLon(Double.NaN);
        setLat(Double.NaN);
        setDepth(Double.NaN);
    }

    public ParticleMortality getDeathCause() {
        if (deathCause != null) {
            return deathCause;
        } else {
            return ParticleMortality.ALIVE;
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

    public void step() {

        if (getAge() > getSimulationManager().getTimeManager().getTransportDuration()) {
            kill(ParticleMortality.OLD);
            return;
        }

        getSimulationManager().getActionManager().executeActions(this);

        if (!isLocked()) {
            applyMove();
            if (isOnEdge()) {
                kill(ParticleMortality.OUT_OF_DOMAIN);
                return;
            } else if (!isInWater()) {
                kill(ParticleMortality.BEACHED);
                return;
            }
            grid2Geo();
        }
        incrementAge();
        unlock();
    }
}
