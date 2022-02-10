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

package org.previmer.ichthyop.io;

import java.util.List;
import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.ZoneParticleLayer;
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
        Array array = new ArrayInt(new int[] {getNParticle()}, false);
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
            ZoneParticleLayer zparticle = (ZoneParticleLayer) particle.getLayer(ZoneParticleLayer.class);
            getArray().setInt(getIndex().set(particle.getIndex()), zparticle.getNumZone(TypeZone.RELEASE));
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
