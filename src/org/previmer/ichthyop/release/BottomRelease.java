/*
 *  Copyright (C) 2011 lysel
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.previmer.ichthyop.release;

import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IParticle;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.io.ZoneTracker;
import org.previmer.ichthyop.particle.ParticleFactory;

/**
 *
 * @author lysel
 */
public class BottomRelease extends AbstractRelease {

 private int nbReleaseZones, nParticles;
    private boolean is3D;

    @Override
    public void loadParameters() throws Exception {

        /* Get number of particles to release */
        nParticles = Integer.valueOf(getParameter("number_particles"));

        /* Check whether 2D or 3D simulation */
        is3D = true;

        /* Load release zones*/
        getSimulationManager().getZoneManager().loadZonesFromFile(getParameter("zone_file"), TypeZone.RELEASE);
        nbReleaseZones = (null != getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE))
                ? getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).size()
                : 0;
        getSimulationManager().getOutputManager().addPredefinedTracker(ZoneTracker.class);
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
        }

        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size(), 0);
        for (int p = 0; p < nParticles; p++) {
            /** Instantiate a new Particle */
            int DROP_MAX = 2000;
            IParticle particle = null;
            int counter = 0;
            while (null == particle) {
                if (counter++ > DROP_MAX) {
                    throw new NullPointerException("{Zone Release} Unable to release particle. Check out the zone definitions.");
                }
                double x = xmin + Math.random() * (xmax - xmin);
                double y = ymin + Math.random() * (ymax - ymin);
                particle = ParticleFactory.createBottomParticle(index, x, y);
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
