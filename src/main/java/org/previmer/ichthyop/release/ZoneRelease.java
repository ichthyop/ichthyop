/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
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
     * Dispatch cells according to the user definition.
     *
     * @return the number of particles per release zone.
     */
    private int[] dispatchUserDefParticles() {

        double totalPercentage = 0;
        int nParticleSum = 0; 
        // assign number of particles per zone proportionnaly to zone extents
        int[] nParticlePerZone = new int[nbReleaseZones];
        for (int i_zone = 0; i_zone < nbReleaseZones; i_zone++) {
            Zone zone = getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).get(i_zone);
            int nParticulesZone = (int) zone.getProportionParticles() * nParticles;
            nParticlePerZone[i_zone] = nParticulesZone;
            nParticleSum += nParticlePerZone[i_zone];
            totalPercentage += zone.getProportionParticles();
        }
        
        if (totalPercentage != 1) {
            StringBuilder sb = new StringBuilder();
            sb.append("Total proportion must be less equan than 100. ");
            sb.append("Actual proportion is ");
            sb.append(totalPercentage * 100);
            throw new IllegalArgumentException(sb.toString());
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

    /**
     * Computes and returns the number of particles per release zone,
     * proportionally to zone extents.
     *
     * @return the number of particles per release zone.
     */
    private int[] dispatchParticlesArea() {

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
        
        boolean userDefinedNParticles = false;
        
        if(isNull("user_defined_nparticles")) {
            userDefinedNParticles = false;
        } else {
            userDefinedNParticles = Boolean.valueOf(getParameter("user_defined_nparticles"));
        }
        
        int[] nParticlePerZone;
        if(userDefinedNParticles) { 
            nParticlePerZone = this.dispatchUserDefParticles();
        } else {
            nParticlePerZone = dispatchParticlesArea();
        }
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
