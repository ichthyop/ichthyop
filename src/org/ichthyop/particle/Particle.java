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
package org.ichthyop.particle;

import org.ichthyop.GridPoint;
import java.util.HashMap;
import java.util.Map;

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
    private final Map<String, Object> attributes = new HashMap();

    @Override
    public double getDouble(String key) {
        return (double) attributes.get(key);
    }

    @Override
    public int getInt(String key) {
        return (int) attributes.get(key);
    }

    @Override
    public Object get(String key) {
        return attributes.get(key);
    }
    
    @Override
    public void set(String key, Object value) {
        attributes.put(key, value);
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
        age += getSimulationManager().getTimeManager().get_dt();
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
     * Applies all the user defined actions ({@link org.ichthyop.arch.IAction})
     * and then applies all the system actions
     * ({@link org.ichthyop.arch.ISysAction})
     */
    public void step() {
        getSimulationManager().getActionManager().executeActions(this);
    }

    @Override
    public void init() {
        attributes.clear();
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
