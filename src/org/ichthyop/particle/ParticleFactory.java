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
import org.ichthyop.Zone;

/**
 *
 * @author pverley
 */
public class ParticleFactory extends IchthyopLinker {

    final private static ParticleFactory PARTICLE_FACTORY = new ParticleFactory();
    final private int ATTEMPT_MAX = 2000;

    public static ParticleFactory getInstance() {
        return PARTICLE_FACTORY;
    }

    public IParticle createGeoParticle(int index, double lon, double lat, double depth, ParticleMortality mortality) throws IOException {

        Particle particle = new Particle();
        particle.setIndex(index);
        boolean living = mortality.equals(ParticleMortality.ALIVE);

        double lonmin = getSimulationManager().getGrid().getLonMin();
        double lonmax = getSimulationManager().getGrid().getLonMax();
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
                if (getSimulationManager().getGrid().getDepthMax(particle.getX(), particle.getY()) > depth || depth > 0) {
                    return null;
                }
            }
        } else {
            particle.kill(mortality);
        }
        return particle;
    }

    public IParticle createGeoParticle(int index, double lon, double lat, double depth) throws IOException {
        return createGeoParticle(index, lon, lat, depth, ParticleMortality.ALIVE);
    }

    public IParticle createGeoParticle(int index, double lon, double lat) throws IOException {
        return createGeoParticle(index, lon, lat, Double.NaN, ParticleMortality.ALIVE);
    }

    public IParticle createSurfaceParticle(int index) {
        Particle particle = new Particle();
        particle.setIndex(index);
        if (!getSimulationManager().getGrid().is3D()) {
            particle.make2D();
        }
        int nx = getSimulationManager().getGrid().get_nx();
        int ny = getSimulationManager().getGrid().get_ny();
        int attempt = 0;
        while (attempt++ < ATTEMPT_MAX) {
            double[] xy = new double[]{Math.random() * (nx - 1), Math.random() * (ny - 1)};
            if (getSimulationManager().getGrid().isInWater(xy)
                    && !getSimulationManager().getGrid().isOnEdge(xy)) {
                particle.setX(xy[0]);
                particle.setY(xy[1]);
                if (getSimulationManager().getGrid().is3D()) {
                    particle.setZ(getSimulationManager().getGrid().depth2z(xy[0], xy[1], 0.));
                }
                particle.grid2Geo();
                return particle;
            }
        }

        error("Unable to release particle at surface", new IOException("Too many failed attempts"));
        return null;
    }

    public IParticle createZoneParticle(int index, Zone zone) {

        Particle particle = new Particle();
        particle.setIndex(index);
        if (!getSimulationManager().getGrid().is3D()) {
            particle.make2D();
        }
        int attempt = 0;
        double lat, lon;
        while (attempt++ < ATTEMPT_MAX) {
            lat = zone.getLatMin() + Math.random() * (zone.getLatMax() - zone.getLatMin());
            lon = zone.getLonMin() + Math.random() * (zone.getLonMax() - zone.getLonMin());
            double[] xy = getSimulationManager().getGrid().latlon2xy(lat, lon);
            boolean valid = getSimulationManager().getGrid().isInWater(xy)
                    && !getSimulationManager().getGrid().isOnEdge(xy)
                    && getSimulationManager().getZoneManager().isInside(lat, lon, zone.getKey());
            if (valid) {
                particle.setLat(lat);
                particle.setLon(lon);
                double upperdepth, lowerdepth;
                double depthmax = Math.abs(getSimulationManager().getGrid().getDepthMax(xy[0], xy[1]));
                if (zone.isEnabledDepthMask()) {
                    upperdepth = Math.min(depthmax, zone.getUpperDepth());
                    lowerdepth = Math.min(depthmax, zone.getLowerDepth());
                } else {
                    upperdepth = 0.d;
                    lowerdepth = depthmax;
                }
                particle.setDepth(-1.d * (upperdepth + Math.random() * (lowerdepth - upperdepth)));
                particle.geo2Grid();
                return particle;
            }
        }
        error("Unable to release particle in zone " + zone.getKey(), new IOException("Too many failed attempts"));
        return null;
    }

    private static boolean inside(double d, double dmin, double dmax) {
        return d >= dmin && d <= dmax;
    }
}
