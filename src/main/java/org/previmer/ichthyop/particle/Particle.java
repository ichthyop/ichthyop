/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.particle;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.*;

/**
 *
 * @author pverley
 */
public class Particle extends GridPoint implements IParticle {

    private int index;
    private long age = 0;
    private ParticleMortality deathCause;
    private boolean living = true;
    private boolean locked = false;
    private final List<ParticleLayer> layers = new ArrayList();

    @Override
    public ParticleLayer getLayer(Class layerClass) {
        for (ParticleLayer layer : layers) {
            if (layer.getClass().getCanonicalName().equals(layerClass.getCanonicalName())) {
                return layer;
            }
        }
        try {
            ParticleLayer layer = (ParticleLayer) layerClass.getConstructor(IParticle.class).newInstance(this);
            layer.init();
            layers.add(layer);
            return layer;
        } catch (InstantiationException ex) {
            Logger.getLogger(Particle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            Logger.getLogger(Particle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IllegalArgumentException ex) {
            Logger.getLogger(Particle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(Particle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchMethodException ex) {
            Logger.getLogger(Particle.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(Particle.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public boolean isLiving() {
        return living;
    }

    @Override
    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public long getAge() {
        return age;
    }

    public void incrementAge() {
        age += Math.abs(getSimulationManager().getTimeManager().get_dt());
    }

    @Override
    public void kill(ParticleMortality cause) {

        /* The method is only called once per time-step.
         * Therefore the first action to call method kill() will
         * permanently set the death cause.
         */
        if (!living) {
            return;
        }
        this.deathCause = cause;
        living = false;
    }

    @Override
    public ParticleMortality getDeathCause() {
        if (deathCause != null) {
            return deathCause;
        } else {
            return ParticleMortality.ALIVE;
        }
    }

    @Override
    public boolean isLocked() {
        return locked;
    }

    @Override
    public void lock() {
        locked = true;
    }

    @Override
    public void unlock() {
        locked = false;
    }

    /**
     * Applies all the user defined actions
     * ({@link org.previmer.ichthyop.arch.IAction}) and then applies all the
     * system actions ({@link org.previmer.ichthyop.arch.ISysAction})
     */
    public void step() {
        getSimulationManager().getActionManager().executeActions(this);
    }
    
    @Override
    public void init() {
        getSimulationManager().getActionManager().initActions(this);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("Particle ");
        str.append(getIndex());
        str.append('\n');
        str.append("  lat: ");
        str.append((float) getLat());
        str.append(" lon: ");
        str.append((float) getLon());
        if (is3D()) {
            str.append(" depth: ");
            str.append((float) getDepth());
        }
        str.append('\n');
        str.append("  x: ");
        str.append((float) getX());
        str.append(" y: ");
        str.append((float) getY());
        if (is3D()) {
            str.append(" z: ");
            str.append((float) getZ());
        }
        str.append('\n');
        str.append("  status: ");
        str.append(getDeathCause().toString());
        str.append('\n');
        return str.toString();
    }
}
