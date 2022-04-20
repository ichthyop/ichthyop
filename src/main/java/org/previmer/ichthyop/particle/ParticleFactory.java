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

import org.previmer.ichthyop.*;
import org.previmer.ichthyop.dataset.FvcomDataset;

/**
 *
 * @author pverley
 */
public class ParticleFactory extends SimulationManagerAccessor {

    public static IParticle createGeoParticle(int index, double lon, double lat, double depth, ParticleMortality mortality) {

        Particle particle = new Particle();
        particle.setIndex(index);
        boolean living = mortality.equals(ParticleMortality.ALIVE);

        particle.setLon(lon);
        particle.setLat(lat);
        particle.setDepth(depth);
        if (Double.isNaN(depth)) {
            particle.make2D();
        }
        particle.geo2Grid();
        if (living) {
            if (!particle.isInWater() || particle.isOnEdge()) {
                return null;
            }
            if (!Double.isNaN(depth)) {
                if (getSimulationManager().getDataset().z2depth(particle.getX(), particle.getY(), 0) > depth || depth > 0) {
                    return null;
                }
            }
        } else {
            particle.kill(mortality);
        }
        return particle;
    }

    public static IParticle createGeoParticle(int index, double lon, double lat, double depth) {
        return createGeoParticle(index, lon, lat, depth, ParticleMortality.ALIVE);
    }

    public static IParticle createGeoParticle(int index, double lon, double lat) {
        return createGeoParticle(index, lon, lat, Double.NaN, ParticleMortality.ALIVE);
    }

    public static IParticle createSurfaceParticle(int index, double x, double y, boolean is3D) {
        Particle particle = new Particle();
        particle.setIndex(index);
        particle.setX(x);
        particle.setY(y);
        if (is3D) {
            particle.setZ(getSimulationManager().getDataset().get_nz() - 1);
        } else {
            particle.make2D();
        }
        if (!particle.isInWater() || particle.isOnEdge()) {
            return null;
        }
        particle.grid2Geo();
        return particle;
    }

    public static IParticle createZoneParticle(int index, double x, double y, double depth) {
        Particle particle = new Particle();
        particle.setIndex(index);
        particle.setX(x);
        particle.setY(y);
        particle.setDepth(depth);
        /* bugfixing 2011/06/28
         * setDepth but z unset and then calling isInWater ==> crash
         * phv 2011/09/25: wondering wether the make2D should not occur before
         * the geo2grid, to be checked...
         */
        if (Double.isNaN(depth)) {
            particle.make2D();
        }
        particle.geo2Grid();
        if (!particle.isInWater() || particle.isOnEdge()) {
            return null;
        }

        if (!(getSimulationManager().getDataset() instanceof FvcomDataset)) {
            if (!Double.isNaN(depth)) {
                if (getSimulationManager().getDataset().z2depth(particle.getX(), particle.getY(), 0) > depth
                        || depth > 0) {
                    return null;
                }
            }
        }
        int numReleaseZone = ((ZoneParticleLayer) particle.getLayer(ZoneParticleLayer.class)).getNumZone(TypeZone.RELEASE);
        if (numReleaseZone == -1) {
            return null;
        }
        particle.grid2Geo();
        particle.geo2Grid();
        return particle;
    }

    public static IParticle createBottomParticle(int index, double x, double y) {

        Particle particle = new Particle();
        particle.setIndex(index);
        particle.setX(x);
        particle.setY(y);
        /*
         * Make sure the particle is released at the bottom, ie z = 0
         */
        particle.setZ(0);
        /*
         * Test wether the grid point is on land or at the edge of the domain
         */
        if (!particle.isInWater() || particle.isOnEdge()) {
            return null;
        }
        /*
         * Test wether the grid point is inside one of the release zones
         */
        int numReleaseZone = ((ZoneParticleLayer) particle.getLayer(ZoneParticleLayer.class)).getNumZone(TypeZone.RELEASE);
        if (numReleaseZone == -1) {
            return null;
        }
        /*
         * Converts (x, y, z) into (lon, lat, depth)
         */
        particle.grid2Geo();
        return particle;
    }
}
