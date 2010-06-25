/*
 *  Copyright (C) 2010 Philippe Verley <philippe dot verley at ird dot fr>
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

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.particle.ParticleFactory;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class StainRelease extends AbstractReleaseProcess {

    private int nb_particles, nbReleasedNow;
    private double lon_stain, lat_stain, depth_stain;
    private double radius_stain;
    private int nbReleaseEvents;
    private boolean is3D;
    private static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;

    public void loadParameters() throws Exception {

        /* Check whether 2D or 3D simulation */
        is3D = getSimulationManager().getDataset().is3D();

        /* retrieve stain parameters */
        nb_particles = Integer.valueOf(getParameter("number_particles"));
        radius_stain = Float.valueOf(getParameter("radius_stain"));
        lon_stain = Double.valueOf(getParameter("lon_stain"));
        lat_stain = Double.valueOf(getParameter("lat_stain"));
        if (is3D) {
            depth_stain = Float.valueOf(getParameter("depth_stain"));
        }
    }

    public int release(ReleaseEvent event) throws Exception {

        int indexEvent = event.getSource().getIndexEvent();
        nbReleaseEvents = getSimulationManager().getReleaseManager().getNbReleaseEvents();

        nbReleasedNow = nb_particles / nbReleaseEvents;
        int mod = nb_particles % nbReleaseEvents;
        nbReleasedNow += (indexEvent < mod) ? 1 : 0;

        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size() - 1, 0);
        for (int p = 0; p < nbReleasedNow; p++) {
            double lat = lat_stain + 2.d * radius_stain * (Math.random() - 0.5d) / ONE_DEG_LATITUDE_IN_METER;
            double one_deg_longitude_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * lat_stain / 180.d);
            double lon = lon_stain + 2 * radius_stain * (Math.random() - 0.5d) / one_deg_longitude_meter;
            double depth = Double.NaN;
            if (is3D) {
                depth = depth_stain + 2 * radius_stain * (Math.random() - 0.5d);
            }
            IBasicParticle particlePatch = ParticleFactory.createParticle(index, lon, lat, depth);
            getSimulationManager().getSimulation().getPopulation().add(particlePatch);
            index++;
        }
        return index;
    }

    public int getNbParticles() {
        return nb_particles;
    }
}
