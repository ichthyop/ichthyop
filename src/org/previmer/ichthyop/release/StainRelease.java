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

import java.io.IOException;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.dataset.DatasetUtil;
import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.particle.ParticleFactory;
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class StainRelease extends AbstractReleaseProcess {

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
            IBasicParticle particlePatch = null;
            int counter = 0;
            while (null == particlePatch) {

                if (counter++ > DROP_MAX) {
                    throw new NullPointerException("{Release stain} Unable to release particle. Check out the stain definition.");
                }
                GeoPosition point = getGeoPosition();
                double depth = Double.NaN;
                if (is3D) {
                    depth = depth_stain + thickness_stain * (Math.random() - 0.5d);
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

        double lat = lat_stain + 2.d * radius_stain * (Math.random() - 0.5d) / ONE_DEG_LATITUDE_IN_METER;
        double one_deg_longitude_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * lat_stain / 180.d);
        double lon = lon_stain + 2 * radius_stain * (Math.random() - 0.5d) / one_deg_longitude_meter;
        if (DatasetUtil.geodesicDistance(lat, lon, lat_stain, lon_stain) > radius_stain) {
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
