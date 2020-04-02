/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Nicolas BARRIER, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
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

package org.previmer.ichthyop.release;

import org.previmer.ichthyop.*;
import org.previmer.ichthyop.particle.IParticle;
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
