/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.ichthyop.release;

import org.ichthyop.*;
import org.ichthyop.particle.IParticle;
import org.ichthyop.event.ReleaseEvent;
import org.ichthyop.output.ReleaseZoneTracker;
import org.ichthyop.output.ZoneTracker;
import org.ichthyop.particle.ParticleFactory;

/**
 *
 * @author pverley
 */
public class ZoneRelease extends AbstractRelease {

    private int nbReleaseZones, nParticles;
    private String zonePrefix;
    private boolean bottom;

    @Override
    public void loadParameters() throws Exception {

        // get number of particles to release
        nParticles = getConfiguration().getInt("release.zone.number_particles");

        // bottom release
        bottom = getConfiguration().getBoolean("release.zone.bottom");

        // load release zones
        zonePrefix = getConfiguration().getString("release.zone.zone_prefix");
        getSimulationManager().getZoneManager().loadZones(zonePrefix);
        nbReleaseZones = (null != getSimulationManager().getZoneManager().getZones(zonePrefix))
                ? getSimulationManager().getZoneManager().getZones(zonePrefix).size()
                : 0;
        getSimulationManager().getOutputManager().addPredefinedTracker(ZoneTracker.class);
        getSimulationManager().getOutputManager().addPredefinedTracker(ReleaseZoneTracker.class);
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
        int[] nParticlePerZone = new int[nbReleaseZones];
        int nParticleSum = 0;
        int i_zone = 0;
        for (Zone zone : getSimulationManager().getZoneManager().getZones(zonePrefix)) {
            nParticlePerZone[i_zone] = (int) Math.round(nParticles * zone.getArea() / areaTot);
            nParticleSum += nParticlePerZone[i_zone];
            i_zone++;
        }

        // adjust number of particles per zones in case rounding did not match
        // exactly expected number of particles.
        int sign = (int) Math.signum(nParticles - nParticleSum);
        if (sign != 0) {
            for (int i = 0; i < Math.abs(nParticles - nParticleSum); i++) {
                nParticlePerZone[i % nbReleaseZones] += sign;
            }
        }

        for (i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
            if (nParticlePerZone[i_zone] == 0) {
                warning("Release zone {0} has not been attributed any particle. It may be too small compared to other release zones or its definition may be flawed.", i_zone);
            }
        }

        return nParticlePerZone;
    }

    @Override
    public int release(ReleaseEvent event) throws Exception {

        int[] nParticlePerZone = dispatchParticles();
        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size(), 0);
        int i_zone = 0;
        for (Zone zone : getSimulationManager().getZoneManager().getZones(zonePrefix)) {
            // release particles randomly within the zone
            for (int p = 0; p < nParticlePerZone[i_zone]; p++) {
                IParticle particle = ParticleFactory.getInstance().createZoneParticle(index, zone, bottom);
                getSimulationManager().getSimulation().getPopulation().add(particle);
                index++;
            }
        }
        return index;
    }

    @Override
    public int getNbParticles() {
        return nParticles;
    }
}
