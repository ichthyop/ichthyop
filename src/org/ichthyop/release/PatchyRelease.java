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
package org.ichthyop.release;

import java.io.IOException;
import org.ichthyop.Zone;
import org.ichthyop.particle.IParticle;
import org.ichthyop.event.ReleaseEvent;
import org.ichthyop.output.ZoneTracker;
import org.ichthyop.particle.ParticleFactory;

/**
 *
 * @author pverley
 */
public class PatchyRelease extends AbstractRelease {

    private int npatch;
    private int nagregated;
    private double radius_patch, thickness_patch;
    private int nbReleaseZones;
    private boolean is3D;
    private static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    private String zonePrefix;

    @Override
    public void loadParameters() throws Exception {

        /* retrieve patches parameters */
        npatch = getConfiguration().getInt("release.patches.number_patches");
        nagregated = getConfiguration().getInt("release.patches.number_agregated");
        radius_patch = getConfiguration().getFloat("release.patches.radius_patch");
        thickness_patch = getConfiguration().getFloat("release.patches.thickness_patch");

        /* Check whether 2D or 3D simulation */
        is3D = getSimulationManager().getDataset().getGrid().is3D();

        /* Load release zones*/
        zonePrefix = getConfiguration().getString("release.zone.zone_prefix");
        getSimulationManager().getZoneManager().loadZones(zonePrefix);
        nbReleaseZones = (null != getSimulationManager().getZoneManager().getZones(zonePrefix))
                ? getSimulationManager().getZoneManager().getZones(zonePrefix).size()
                : 0;
        getSimulationManager().getOutputManager().addPredefinedTracker(ZoneTracker.class);
    }
    
    /**
     * Computes and returns the number of particles per release zone,
     * proportionally to zone extents.
     *
     * @return the number of particles per release zone.
     */
    int[] dispatchParticles() {

        double areaTot = 0;
        for (Zone zone : getSimulationManager().getZoneManager().getZones(zonePrefix)) {
            areaTot += zone.getArea();
        }

        // assign number of particles per zone proportionnaly to zone extents
        int[] nPatchPerZone = new int[nbReleaseZones];
        int nPatchesSum = 0;
        int i_zone = 0;
        for (Zone zone : getSimulationManager().getZoneManager().getZones(zonePrefix)) {
            nPatchPerZone[i_zone] = (int) Math.round(npatch * zone.getArea() / areaTot);
            nPatchesSum += nPatchPerZone[i_zone];
            i_zone++;
        }

        // adjust number of particles per zones in case rounding did not match
        // exactly expected number of particles.
        int sign = (int) Math.signum(npatch - nPatchesSum);
        if (sign != 0) {
            for (int i = 0; i < Math.abs(npatch - nPatchesSum); i++) {
                nPatchPerZone[i % nbReleaseZones] += sign;
            }
        }

        for (i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
            if (nPatchPerZone[i_zone] == 0) {
                warning("Release zone {0} has not been attributed any particle. It may be too small compared to other release zones or its definition may be flawed.", i_zone);
            }
        }

        return nPatchPerZone;
    }

    @Override
    public int release(ReleaseEvent event) throws Exception {

        int[] nParticlePerZone = dispatchParticles();
        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size(), 0);
        int i_zone = 0;
        for (Zone zone : getSimulationManager().getZoneManager().getZones(zonePrefix)) {
            // release particles randomly within the zone
            for (int p = 0; p < nParticlePerZone[i_zone]; p++) {
                IParticle particle = ParticleFactory.getInstance().createZoneParticle(index, zone);
                getSimulationManager().getSimulation().getPopulation().add(particle);
                index++;
                for (int f = 0; f < nagregated - 1; f++) {
                    IParticle particlePatch = null;
                    int counter = 0;
                    while (null == particlePatch) {
                        if (counter++ > 2000) {
                            error("Unable to release particle in zone " + zone.getKey(), new IOException("Too many failed attempts"));
                        }
                        double lat = particle.getLat() + radius_patch * (Math.random() - 0.5d) / ONE_DEG_LATITUDE_IN_METER;
                        double one_deg_longitude_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * particle.getLat() / 180.d);
                        double lon = particle.getLon() + radius_patch * (Math.random() - 0.5d) / one_deg_longitude_meter;
                        double depth = Double.NaN;
                        if (is3D) {
                            depth = particle.getDepth() + thickness_patch * (Math.random() - 0.5d);
                        }
                        particlePatch = ParticleFactory.getInstance().createGeoParticle(index, lon, lat, depth);
                    }
                    getSimulationManager().getSimulation().getPopulation().add(particlePatch);
                    index++;
                }
            }
        }
        return index;
    }

    @Override
    public int getNbParticles() {
        return npatch * nagregated;
    }
}
