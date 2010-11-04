/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.particle.ParticleFactory;
import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.io.ZoneTracker;

/**
 *
 * @author pverley
 */
public class ZoneRelease extends AbstractReleaseProcess {

    private int nbReleaseZones, nbParticles;
    private boolean is3D, isGlobalReleaseDepth;
    private float lowerReleaseDepth, upperReleaseDepth;

    public void loadParameters() throws Exception {

        /* Get number of particles to release */
        nbParticles = Integer.valueOf(getParameter("number_particles"));

        /* Check whether 2D or 3D simulation */
        is3D = getSimulationManager().getDataset().is3D();

        /* Retrocompatibility v2 where user could set global release depths
         * whereas in v3 release depth is given by the thickness of the zone.
         * If release depths are not defined here, ichthyop will consider the
         * zones thickness instead.
         */
        try {
            isGlobalReleaseDepth = Boolean.valueOf(getParameter("global_release_depth"));
            
        } catch (Exception ex) {
            isGlobalReleaseDepth = false;
        }
        if (isGlobalReleaseDepth) {
            lowerReleaseDepth = Float.valueOf(getParameter("lower_depth"));
            upperReleaseDepth = Float.valueOf(getParameter("upper_depth"));
        }

        /* Load release zones*/
        getSimulationManager().getZoneManager().loadZonesFromFile(getParameter("zone_file"), TypeZone.RELEASE);
        if (null == getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE)) {
            throw new NullPointerException("There is not any release zone defined.");
        }
        nbReleaseZones = getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).size();

        /* Reset zone thickness when user defined global lower & upper release depths */
        if (isGlobalReleaseDepth) {
            for (int i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
                getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).get(i_zone).setLowerDepth(lowerReleaseDepth);
                getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).get(i_zone).setUpperDepth(upperReleaseDepth);
            }
        }

        getSimulationManager().getOutputManager().addPredefinedTracker(ZoneTracker.class);
    }

    public int release(ReleaseEvent event) throws Exception {

        double xmin, xmax, ymin, ymax;
        double upDepth = Double.MAX_VALUE, lowDepth = 0.d;
        /** Reduces the release area function of the user-defined zones */
        xmin = Double.MAX_VALUE;
        ymin = Double.MAX_VALUE;
        xmax = 0.d;
        ymax = 0.d;
        for (int i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
            Zone zone = getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).get(i_zone);
            xmin = Math.min(xmin, zone.getXmin());
            xmax = Math.max(xmax, zone.getXmax());
            ymin = Math.min(ymin, zone.getYmin());
            ymax = Math.max(ymax, zone.getYmax());
            if (is3D) {
                upDepth = Math.min(upDepth, zone.getUpperDepth());
                lowDepth = Math.max(lowDepth, zone.getLowerDepth());
            } else {
                upDepth = lowDepth = Double.NaN;
            }
        }

        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size() - 1, 0);
        for (int p = 0; p < nbParticles; p++) {
            /** Instantiate a new Particle */
            IBasicParticle particle = ParticleFactory.createParticle(index, xmin, xmax, ymin, ymax, upDepth, lowDepth);
            getSimulationManager().getSimulation().getPopulation().add(particle);
            index++;
        }

        return index;
    }

    public int getNbParticles() {
        return nbParticles;
    }
}
