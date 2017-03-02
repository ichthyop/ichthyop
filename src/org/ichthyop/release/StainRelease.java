/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
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

package org.ichthyop.release;

import java.io.IOException;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.ichthyop.particle.IParticle;
import org.ichthyop.dataset.DatasetUtil;
import org.ichthyop.event.ReleaseEvent;
import org.ichthyop.particle.ParticleFactory;
import org.ichthyop.ui.LonLatConverter;
import org.ichthyop.ui.LonLatConverter.LonLatFormat;

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
        nb_particles = getConfiguration().getInt("release.stain.number_particles");
        radius_stain = getConfiguration().getFloat("release.stain.radius_stain");
        lon_stain = Double.valueOf(LonLatConverter.convert(getConfiguration().getString("release.stain.lon_stain"), LonLatFormat.DecimalDeg));
        lat_stain = Double.valueOf(LonLatConverter.convert(getConfiguration().getString("release.stain.lat_stain"), LonLatFormat.DecimalDeg));
        if (is3D) {
            thickness_stain = getConfiguration().getFloat("release.stain.thickness_stain");
            depth_stain = getConfiguration().getFloat("release.stain.depth_stain");
        }
    }

    @Override
    public int release(ReleaseEvent event) throws Exception {

        boolean isStainInWater = getSimulationManager().getDataset().isInWater(getSimulationManager().getDataset().latlon2xy(lat_stain, lon_stain));
        if (!isStainInWater) {
            throw new IOException("[release stain] Center of the stain [lat: " + lat_stain + "; lon: " + lon_stain + "] is not in water or out of the domain. Fixed that in section Release stain.");
        }

        int DROP_MAX = 2000;
        int index = Math.max(getSimulationManager().getSimulation().getPopulation().size(), 0);
        for (int p = 0; p < nb_particles; p++) {
            IParticle particlePatch = null;
            int counter = 0;
            while (null == particlePatch) {

                if (counter++ > DROP_MAX) {
                    throw new NullPointerException("[release stain] Unable to release particle. Check out the stain definition.");
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
        return getConfiguration().getInt("release.stain.number_particles");
    }
}
