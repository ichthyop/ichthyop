/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import java.util.logging.Level;
import org.previmer.ichthyop.*;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.io.ReleaseZoneTracker;
import org.previmer.ichthyop.io.ZoneTracker;
import org.previmer.ichthyop.particle.ParticleFactory;

/**
 *
 * @author pverley
 */
public class ZoneRelease extends AbstractRelease {

    private int nbReleaseZones, nParticles;
    private boolean is3D;

    @Override
    public void loadParameters() throws Exception {

        /* Get number of particles to release */
        nParticles = Integer.valueOf(getParameter("number_particles"));

        /* Check whether 2D or 3D simulation */
        is3D = getSimulationManager().getDataset().is3D();

        /* Load release zones*/
        getSimulationManager().getZoneManager().loadZonesFromFile(getParameter("zone_file"), TypeZone.RELEASE);
        nbReleaseZones = (null != getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE))
                ? getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).size()
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
    private int[] dispatchParticles() {

        double areaTot = 0;
        for (int i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
            Zone zone = getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).get(i_zone);
            areaTot += zone.getArea();
        }

        // assign number of particles per zone proportionnaly to zone extents
        int[] nParticlePerZone = new int[nbReleaseZones];
        int nParticleSum = 0;
        for (int i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
            Zone zone = getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).get(i_zone);
            nParticlePerZone[i_zone] = (int) Math.round(nParticles * zone.getArea() / areaTot);
            nParticleSum += nParticlePerZone[i_zone];
        }

        // adjust number of particles per zones in case rounding did not match
        // exactly expected number of particles.
        int sign = (int) Math.signum(nParticles - nParticleSum);
        if (sign != 0) {
            for (int i = 0; i < Math.abs(nParticles - nParticleSum); i++) {
                nParticlePerZone[i % nbReleaseZones] += sign;
            }
        }
        
        for (int i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
            if (nParticlePerZone[i_zone] == 0) getLogger().log(Level.WARNING, "Release zone {0} has not been attributed any particle. It may be too small compared to other release zones or its definition may be flawed.", i_zone);
        }

        return nParticlePerZone;
    }

    @Override
    public int release(ReleaseEvent event) throws Exception {

        int[] nParticlePerZone = dispatchParticles();
        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size(), 0);

        for (int i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
            Zone zone = getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).get(i_zone);
            double xmin = zone.getXmin();
            double xmax = zone.getXmax();
            double ymin = zone.getYmin();
            double ymax = zone.getYmax();
            double upDepth = 0, lowDepth = 0;
            if (is3D) {
                upDepth = zone.getUpperDepth();
                lowDepth = zone.getLowerDepth();
            }
            // release particles randomly within the zone
            for (int p = 0; p < nParticlePerZone[i_zone]; p++) {
                int DROP_MAX = 2000;
                IParticle particle = null;
                int counter = 0;
                while (null == particle) {
                    if (counter++ > DROP_MAX) {
                        throw new NullPointerException("Unable to release particle in release zone " + zone.getIndex() + ". Check out the zone definitions.");
                    }
                    double x = xmin + Math.random() * (xmax - xmin);
                    double y = ymin + Math.random() * (ymax - ymin);
                    double depth = Double.NaN;
                    if (is3D) {
                        depth = -1.d * (upDepth + Math.random() * (lowDepth - upDepth));
                    }
                    particle = ParticleFactory.createZoneParticle(index, x, y, depth);
                }
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
