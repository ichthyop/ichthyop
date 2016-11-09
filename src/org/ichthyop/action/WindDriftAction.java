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

package org.ichthyop.action;

import org.ichthyop.particle.IParticle;

public class WindDriftAction extends AbstractAction {

    static double wind_factor;
    public static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strUW, strVW, strTime;

    /**
     * Depth of wind drift application
     */
    static float depth_application;

    /**
     * Angle deviation imposed by wind
     */
    static double angle;
    /**
     * Wind convention used
     */
    static double convention;

    @Override
    public void loadParameters() throws Exception {
        wind_factor = Double.valueOf(getParameter("wind_factor"));
        strUW = getParameter("wind_u");
        strVW = getParameter("wind_v");

        depth_application = Float.valueOf(getParameter("depth_application"));
        angle = Math.PI / 2.0 - Double.valueOf(getParameter("angle")) * Math.PI / 180.0;
        getSimulationManager().getDataset().requireVariable(strUW, getClass());
        getSimulationManager().getDataset().requireVariable(strVW, getClass());
        convention = "wind to".equals(getParameter("wind_convention")) ? 1 : -1;
    }

    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    @Override
    public void execute(IParticle particle) {

        double[] mvt = getDLonLat(particle.getGridCoordinates(), -particle.getDepth(), getSimulationManager().getTimeManager().getTime(), getSimulationManager().getTimeManager().get_dt());
        double newLon = particle.getLon() + mvt[0];
        double newLat = particle.getLat() + mvt[1];
        double[] newPos = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
        double[] windincr = new double[]{newPos[0] - particle.getX(), newPos[1] - particle.getY()};
        particle.increment(windincr);

    }

    private double[] getDLonLat(double[] pgrid, double depth, double time, double dt) {
        double[] dWi = new double[2];
        if (getSimulationManager().getDataset().is3D()) {
            if (depth > depth_application) {
                dWi[0] = 0;
                dWi[1] = 0;
                return dWi;
            }
        }
        double dx, dy;
        double[] latlon = getSimulationManager().getDataset().xy2latlon(pgrid[0], pgrid[1]);
        double one_deg_lon_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * latlon[0] / 180.d);
        dx = dt * getSimulationManager().getDataset().get(strUW, pgrid, time).doubleValue() / one_deg_lon_meter;
        dy = dt * getSimulationManager().getDataset().get(strVW, pgrid, time).doubleValue() / ONE_DEG_LATITUDE_IN_METER;
        dWi[0] = convention * wind_factor * (dx * Math.cos(angle) - dy * Math.sin(angle));
        dWi[1] = convention * wind_factor * (dx * Math.sin(angle) + dy * Math.cos(angle));
        return dWi;
    }
    
    double skipSeconds(double time) {
        return 100.d * Math.floor(time / 100.d);
    }
}
