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

import java.io.IOException;
import org.ichthyop.IchthyopLinker;

/**
 *
 * @author pverley
 */
public class ParticleFactory extends IchthyopLinker {

    public static IParticle createGeoParticle(int index, double lon, double lat, double depth, ParticleMortality mortality) throws IOException {

        Particle particle = new Particle();
        particle.setIndex(index);
        boolean living = mortality.equals(ParticleMortality.ALIVE);

        double lonmin = getSimulationManager().getDataset().getLonMin();
        double lonmax = getSimulationManager().getDataset().getLonMax();
        if (inside(lon, lonmin, lonmax)) {
            particle.setLon(lon);
        } else if (inside(lon + 360, lonmin, lonmax)) {
            particle.setLon(lon + 360);
        } else if (inside(lon - 360, lonmin, lonmax)) {
            particle.setLon(lon - 360);
        } else {
            throw new IOException("Particle longitude " + lon + " not comprised inside lonmin " + lonmin + " lonmax " + lonmax);
        }
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
                if (getSimulationManager().getDataset().getDepthMax(particle.getX(), particle.getY()) > depth || depth > 0) {
                    return null;
                }
            }
        } else {
            particle.kill(mortality);
        }
        return particle;
    }

    public static IParticle createGeoParticle(int index, double lon, double lat, double depth) throws IOException {
        return createGeoParticle(index, lon, lat, depth, ParticleMortality.ALIVE);
    }

    public static IParticle createGeoParticle(int index, double lon, double lat) throws IOException {
        return createGeoParticle(index, lon, lat, Double.NaN, ParticleMortality.ALIVE);
    }

    public static IParticle createSurfaceParticle(int index, double x, double y, boolean is3D) {
        Particle particle = new Particle();
        particle.setIndex(index);
        particle.setX(x);
        particle.setY(y);
        if (is3D) {
            particle.setZ(getSimulationManager().getDataset().depth2z(x, y, 0.));
        } else {
            particle.make2D();
        }
        if (!particle.isInWater() || particle.isOnEdge()) {
            return null;
        }
        particle.grid2Geo();
        return particle;
    }

    public static IParticle createZoneParticle(int index, double x, double y, double depth, String zoneprefix) {
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
        if (!Double.isNaN(depth)) {
            if (getSimulationManager().getDataset().getDepthMax(particle.getX(), particle.getY()) > depth || depth > 0) {
                return null;
            }
        }
        int numReleaseZone = ZoneParticle.getNumZone(particle, zoneprefix);
        if (numReleaseZone == -1) {
            return null;
        }
        particle.grid2Geo();
        particle.geo2Grid();
        return particle;
    }

    public static IParticle createBottomParticle(int index, double x, double y, String zoneprefix) {

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
        int numReleaseZone = ZoneParticle.getNumZone(particle, zoneprefix);
        if (numReleaseZone == -1) {
            return null;
        }
        /*
         * Converts (x, y, z) into (lon, lat, depth)
         */
        particle.grid2Geo();
        return particle;
    }

    private static boolean inside(double d, double dmin, double dmax) {
        return d >= dmin && d <= dmax;
    }
}
