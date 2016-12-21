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

package org.ichthyop.io;

import java.util.List;
import org.ichthyop.TypeZone;
import org.ichthyop.Zone;
import org.ichthyop.particle.IParticle;
import org.ichthyop.particle.ZoneParticle;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

/**
 *
 * @author pverley
 */
public class ReleaseZoneTracker extends AbstractTracker {

    private int nPopTm1;

    public ReleaseZoneTracker() {
        super(DataType.INT);
        nPopTm1 = 0;
    }

    @Override
    void setDimensions() {
        addDrifterDimension();
    }

    @Override
    Array createArray() {
        Array array = new ArrayInt.D1(getNParticle());
        // Particle not released yet set to -99
        for (int i = 0; i < getNParticle(); i++) {
            array.setInt(i, -99);
        }
        return array;
    }

    @Override
    public void addRuntimeAttributes() {

        List<Zone> zones = getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE);
        if (null != zones) {
            for (Zone zone : zones) {
                addAttribute(new Attribute("release_zone " + zone.getIndex(), zone.getKey()));
            }
        }
        // Particle not released yet set to -99
        addAttribute(new Attribute("not_released_yet", -99));
    }

    @Override
    public void track() {

        int nNow = getSimulationManager().getSimulation().getPopulation().size();
        // Only write release zone when particle is released
        for (int i = nPopTm1; i < nNow; i++) {
            IParticle particle = (IParticle) getSimulationManager().getSimulation().getPopulation().get(i);
            getArray().setInt(getIndex().set(particle.getIndex()), ZoneParticle.getNumZone(particle, TypeZone.RELEASE));
        }
        nPopTm1 = nNow;

        // Disable variable when all particles have been released
        if (nPopTm1 == getNParticle()) {
            disable();
        }
    }
    
    @Override
    public int[] origin(int index_record) {
        // No time dimension, only drifter dimension that starts at zero
        return new int[] {0};
    }
}
