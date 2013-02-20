/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.io.ReleaseZoneTracker;
import org.previmer.ichthyop.io.ZoneTracker;
import org.previmer.ichthyop.particle.ParticleFactory;

/**
 *
 * @author pverley
 */
public class ZoneRelease extends AbstractReleaseProcess {

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

    @Override
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

        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size(), 0);
        for (int p = 0; p < nParticles; p++) {
            /** Instantiate a new Particle */
            int DROP_MAX = 2000;
            IBasicParticle particle = null;
            int counter = 0;
            while (null == particle) {
                if (counter++ > DROP_MAX) {
                    throw new NullPointerException("{Zone Release} Unable to release particle. Check out the zone definitions.");
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

        return index;
    }

    @Override
    public int getNbParticles() {
        return nParticles;
    }
}
