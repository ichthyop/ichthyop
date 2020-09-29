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

package org.previmer.ichthyop.action;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Level;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.io.IOTools;

/**
 *
 * @author pverley
 */
public class WindAction extends AbstractAction {

    private Properties windprop;
    private HashMap<Integer, WindScenario> scenarios;
    public static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;

    public void loadParameters() throws Exception {

        windprop = new Properties();
        scenarios = new HashMap();

        String filename = IOTools.resolveFile(getParameter("wind_file"));
        File file = new File(filename);
        if (!file.isFile()) {
            throw new FileNotFoundException("Wind file " + filename + " not found.");
        }
        if (!file.canRead()) {
            throw new IOException("Wind file " + filename + " cannot be read.");
        }

        windprop.load(new FileInputStream(file));
        int rank = 0;
        while (null != getProperty("wind.intensity", rank)) {
            scenarios.put(rank, new WindScenario(rank));
            rank++;
        }

    }

    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    public void execute(IParticle particle) {

        /* for 3D simulation, ckeck whether the particle is close surface */
        if (particle.getZ() >= 0) {
            double dz = Math.abs(particle.getZ() - (getSimulationManager().getDataset().get_nz() - 1));
            if (dz > 0.01) {
                return;
            }
        }

        /* wind effect */
        double time = getSimulationManager().getTimeManager().getTime();
        int rank = findCurrentRank(time);
        if (rank >= 0) {
            WindScenario scenario = scenarios.get(rank);
            double intensity = scenario.getIntensity();
            double direction = scenario.getDirection();
            double windage = scenario.getWindage();
            double dt = getSimulationManager().getTimeManager().get_dt();
            double newLon = particle.getLon() + getdlon(intensity, direction, windage, particle.getLat(), dt);
            double newLat = particle.getLat() + getdlat(intensity, direction, windage, dt);
            double[] newPos = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
            double[] windincr = new double[]{newPos[0] - particle.getX(), newPos[1] - particle.getY()};
            particle.increment(windincr);
        }
    }

    private int findCurrentRank(double time) {

        int n = scenarios.size();
        for (int i = 0; i < n - 1; i++) {
            double t1 = scenarios.get(i).getTime();
            double t2 = scenarios.get(i + 1).getTime();
            if (time >= t1 && time < t2) {
                return i;
            }
        }
        if (time >= scenarios.get(n - 1).getTime()) {
            return (n - 1);
        }
        return -1;
    }

    private String getProperty(String key, int rank) {
        return windprop.getProperty(key + "[" + rank + "]");
    }

    private double getdlat(double speed, double direction, double windage, double dt) {

        double dlat = 0.d;
        double alpha = Math.PI * (5.d / 2.d - direction / 180.d);
        double sin = Math.sin(alpha);
        if (Math.abs(sin) < 1E-8) {
            sin = 0.d;
        }
        dlat = windage * speed * sin / ONE_DEG_LATITUDE_IN_METER * dt;
        /*System.out.println(speed + " " + direction);
         System.out.println("sin(alpha): " + sin);
         System.out.println("dlat(m): " + (speed * sin) + " dlat(°): " + dlat);*/
        //System.out.println("dlat " + (float) dlat);
        return dlat;
    }

    private double getdlon(double speed, double direction, double windage, double lat, double dt) {

        double dlon = 0.d;
        double alpha = Math.PI * (5.d / 2.d - direction / 180.d);
        double one_deg_lon_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * lat / 180.d);
        double cos = Math.cos(alpha);
        if (Math.abs(cos) < 1E-8) {
            cos = 0.d;
        }
        dlon = windage * speed * cos / one_deg_lon_meter * dt;
        /*System.out.println(speed + " " + direction);
         System.out.println("cos(alpha): " + cos);
         System.out.println("dlon(m): " + (speed * cos) + " dlon(°): " + dlon + " lon: " + (float) getLon());*/
        //System.out.println("dlon " + (float) dlon);
        return dlon;
    }

    private class WindScenario {

        private float intensity;
        private float direction;
        private float windage;
        private double time;

        WindScenario(int rank) {
            intensity = Float.valueOf(getProperty("wind.intensity", rank));
            direction = Float.valueOf(getProperty("wind.direction", rank));
            windage = Float.valueOf(getProperty("windage", rank)) / 100.f;
            try {
                time = getSimulationManager().getTimeManager().date2seconds(getProperty("from.time", rank));
            } catch (ParseException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        }

        /**
         * @return the intensity
         */
        public float getIntensity() {
            return intensity;
        }

        /**
         * @return the direction
         */
        public float getDirection() {
            return direction;
        }

        /**
         * @return the windage
         */
        public float getWindage() {
            return windage;
        }

        /**
         * @return the time
         */
        public double getTime() {
            return time;
        }
    }
}
