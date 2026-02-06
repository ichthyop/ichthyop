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
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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

import java.io.IOException;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.particle.ParticleFactory;
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class StainRelease extends AbstractRelease {

    private int nb_particles;
    private double lon_stain, lat_stain, depth_stain;
    private double radius_stain;
    private double thickness_stain;
    private boolean is3D;
    private static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;

    @Override
    public void loadParameters() throws Exception {

        /* Check whether 2D or 3D simulation */
        is3D = getSimulationManager().getDataset().is3D();

        /* retrieve stain parameters */
        nb_particles = Integer.valueOf(getParameter("number_particles"));
        radius_stain = Float.valueOf(getParameter("radius_stain"));
        lon_stain = Double.valueOf(LonLatConverter.convert(getParameter("lon_stain"), LonLatFormat.DecimalDeg));
        lat_stain = Double.valueOf(LonLatConverter.convert(getParameter("lat_stain"), LonLatFormat.DecimalDeg));
        if (is3D) {
            thickness_stain = Float.valueOf(getParameter("thickness_stain"));
            depth_stain = Float.valueOf(getParameter("depth_stain"));
        }
    }

    @Override
    public int release(ReleaseEvent event) throws Exception {

        boolean isStainInWater = getSimulationManager().getDataset().isInWater(getSimulationManager().getDataset().latlon2xy(lat_stain, lon_stain));
        if (!isStainInWater) {
            throw new IOException("{Release stain} Center of the stain [lat: " + lat_stain + "; lon: " + lon_stain + "] is not in water or out of the domain. Fixed that in section Release stain.");
        }

        int DROP_MAX = 2000;
        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size(), 0);
        for (int p = 0; p < nb_particles; p++) {
            IParticle particlePatch = null;
            int counter = 0;
            while (null == particlePatch) {
                if (counter++ > DROP_MAX) {
                    throw new NullPointerException("{Release stain} Unable to release particle. Check out the stain definition.");
                }
                GeoPosition point = getGeoPosition();
                double depth = Double.NaN;
                if (is3D) {
                    depth = depth_stain + thickness_stain * (this.getRandomDraft() - 0.5d);
                }
                if (depth > 0) {
                    depth *= -1.d;
                }
                particlePatch = ParticleFactory.createGeoParticle(index, point.getLongitude(), point.getLatitude(), depth);
            }
            getSimulationManager().getSimulation().getPopulation().add(particlePatch);
            index++;
        }
        return index;
    }

    private GeoPosition getGeoPosition() {

        double lat, one_deg_longitude_meter, lon;
        if (!getSimulationManager().getDataset().isProjected()) {
            lat = lat_stain + 2.d * radius_stain * (this.getRandomDraft() - 0.5d) / ONE_DEG_LATITUDE_IN_METER;
            one_deg_longitude_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * lat_stain / 180.d);
            lon = lon_stain + 2 * radius_stain * (this.getRandomDraft() - 0.5d) / one_deg_longitude_meter;
        } else {
            lat = lat_stain + 2.d * radius_stain * (this.getRandomDraft() - 0.5d);
            lon = lon_stain + 2.d * radius_stain * (this.getRandomDraft() - 0.5d);
        }
        if (getSimulationManager().getDataset().getDistGetter().getDistance(lat, lon, lat_stain, lon_stain) > radius_stain) {
            return getGeoPosition();
        } else {
            return new GeoPosition(lat, lon);
        }
    }

    @Override
    public int getNbParticles() {
        return Integer.valueOf(getParameter("number_particles"));
    }
}
