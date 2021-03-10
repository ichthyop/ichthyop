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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.List;

import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.StageParticleLayer;

/**
 *
 * @author pverley
 */
public class MigrationAction extends AbstractAction {

    /**
     * Current depth at day time.
     */
    private float depthDay;
    /**
     * List of depths at day time, function of age.
     */
    private float[] depthsDay;
    /**
     * List of age thresholds for depths at day time.
     */
    private float[] agesDepthDay;
    /**
     * Current depth at night.
     */
    private float depthNight;
    /**
     * List of depths at night, function of age.
     */
    private float[] depthsNight;
    /**
     * List of age thresholds for depths at night.
     */
    private float[] agesDepthNight;
    /**
     * Time of sunrise.
     */
    private LocalTime sunrise;
    /**
     * Time of sunset.
     */
    private LocalTime sunset;

    /**
     * Whether the depth at day equals the depth at night.
     */
    private boolean isodepth;
    /**
     * Particle minimal age for enabling vertical migration.
     */
    private long minimumAge;
    /**
     * Whether a growth module is enabled. In that case minimal age is ignored
     * and vertical migration is only enabled beyond egg stage.
     */
    private boolean isGrowth;

    /**
     * Read parameters from the configuration file.
     *
     * @throws Exception
     */
    @Override
    public void loadParameters() throws Exception {

        // DVM only in 3D mode
        if (!getSimulationManager().getDataset().is3D()) {
            throw new UnsupportedOperationException("{Migration} Vertical migration cannot operate in 2D simulation. Please deactivate the block or run a 3D simulation.");
        }

        // Check whether the growth module is enabled
        isGrowth = getSimulationManager().getActionManager().isEnabled("action.growth");

        // Otherwise read migration minimal age
        if (!isGrowth) {
            minimumAge = (long) (Float.valueOf(getParameter("age_min")) * 24.f * 3600.f);
        }
        // Check existence of daytime depth as an age function, provided in CSV file
        if (!isNull("daytime_depth_file")) {
            String pathname = IOTools.resolveFile(getParameter("daytime_depth_file"));
            File f = new File(pathname);
            if (!f.isFile()) {
                throw new FileNotFoundException("File of depth at daytime " + pathname + " not found.");
            }
            if (!f.canRead()) {
                throw new IOException("File of depth at daytime " + pathname + " cannot be read.");
            }

            CSVReader reader = new CSVReaderBuilder(new FileReader(pathname)).withCSVParser(new CSVParserBuilder().withSeparator(';').build()).build();
            List<String[]> lines = reader.readAll();
            Iterator<String[]> iter = lines.iterator();
            while (iter.hasNext()) {
                String[] line = (String[]) iter.next();
                if (line.length != 2 || line[0].isEmpty() || line[1].isEmpty()) {
                    iter.remove();
                }
            }
            // init arrays
            agesDepthDay = new float[lines.size() - 1];
            depthsDay = new float[agesDepthDay.length];
            // read ages (days converted to seconds) and depths
            for (int i = 0; i < agesDepthDay.length; i++) {
                // First line of the CSV file is assumed to be headers and is discarded
                String[] line = lines.get(i + 1);
                agesDepthDay[i] = Float.valueOf(line[0]) * 24.f * 3600.f;
                depthsDay[i] = Float.valueOf(line[1]);
            }
        } else {
            // Constant daytime depth if no CSV file is provided
            depthDay = Float.valueOf(getParameter("daytime_depth"));
        }
        // Check existence of night time depth as an age function, provided in CSV file
        if (!isNull("nighttime_depth_file")) {
            String pathname = IOTools.resolveFile(getParameter("nighttime_depth_file"));
            File f = new File(pathname);
            if (!f.isFile()) {
                throw new FileNotFoundException("File of depth at night " + pathname + " not found.");
            }
            if (!f.canRead()) {
                throw new IOException("File of depth at night " + pathname + " cannot be read.");
            }
            CSVReader reader = new CSVReaderBuilder(new FileReader(pathname)).withCSVParser(new CSVParserBuilder().withSeparator(';').build()).build();
            List<String[]> lines = reader.readAll();
            Iterator<String[]> iter = lines.iterator();
            while (iter.hasNext()) {
                String[] line = (String[]) iter.next();
                if (line.length != 2 || line[0].isEmpty() || line[1].isEmpty()) {
                    iter.remove();
                }
            }
            // init arrays
            agesDepthNight = new float[lines.size() - 1];
            depthsNight = new float[agesDepthNight.length];
            // read ages (days converted to seconds) and depths
            for (int i = 0; i < agesDepthNight.length; i++) {
                // First line of the CSV file is assumed to be headers and is discarded
                String[] line = lines.get(i + 1);
                agesDepthNight[i] = Float.valueOf(line[0]) * 24.f * 3600.f;
                depthsNight[i] = Float.valueOf(line[1]);
            }
        } else {
            // Constant night time depth if no CSV file is provided
            depthNight = Float.valueOf(getParameter("nighttime_depth"));
        }
        // Sunset and sunrise definition
        DateTimeFormatter hourFormat = DateTimeFormatter.ofPattern("HH:mm");
        sunset = LocalTime.parse(getParameter("sunset"), hourFormat);
        sunrise = LocalTime.parse(getParameter("sunrise"), hourFormat);
        // Check whether depth at day and depth at night are constant in the
        // case they are not function of age
        isodepth = (null == depthsDay && null == depthsNight) && (depthDay == depthNight);
    }

    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    @Override
    public void execute(IParticle particle) {

        // Migration only applies for larva stages (and beyond)
        boolean isSatisfiedCriterion;
        if (!isGrowth) {
            isSatisfiedCriterion = particle.getAge() > minimumAge;
        } else {
            // stage == 0 means egg, stage > 0 means larvae
            int stage = ((StageParticleLayer) particle.getLayer(StageParticleLayer.class)).getStage();
            isSatisfiedCriterion = stage > 0;
        }

        if (isSatisfiedCriterion) {
            double depth;
            if (isodepth) {
                // constant depth
                depth = depthDay;
            } else {
                // diel vertical migration
                depth = getDepth(particle, getSimulationManager().getTimeManager().getTime());
            }

            double dz = getSimulationManager().getDataset().depth2z(particle.getX(), particle.getY(), depth) - particle.getZ();
            particle.increment(new double[]{0.d, 0.d, dz}, false, true);
        }
    }

    /**
     * Computes the depth of the particle according to the diel vertical
     * migration behaviour. At daytime the particles stays at a certain depth
     * (either constant or function of age) and jumps to an other depth (either
     * constant or function of age) at night.
     *
     * @param particle, the migrating particle
     * @param time a double, the current time (second) of the simulation
     * @return the depth (metre) of the particle at current time of the
     * simulation.
     */
    private double getDepth(IParticle particle, double time) {

        double realHour = (time / (60 * 60)) % 24;
        int hour = (int) Math.floor(realHour);
        double minute = (int) ((realHour - hour) * 60) ;
        
        LocalTime currentTime = LocalTime.of(hour, (int) minute);

        // get bathy in meter (<0)
        double bottom = getSimulationManager().getDataset().z2depth(particle.getX(), particle.getY(), 0);
        double output;

        if ((currentTime.compareTo(sunrise) >= 0) && (currentTime.compareTo(sunset) < 0)) {
            // day time
            if (null != depthsDay) {
                // Update the depth as function of age
                depthDay = depthsDay[agesDepthDay.length - 1];
                float age = particle.getAge();
                for (int i = 0; i < agesDepthDay.length - 1; i++) {
                    if (agesDepthDay[i] <= age && age < agesDepthDay[i + 1]) {
                        depthDay = depthsDay[i];
                        break;
                    }
                }
            }
            output = depthDay;
        } else {
            // night time
            if (null != depthsNight) {
                // Update the depth as function of age
                depthNight = depthsNight[agesDepthNight.length - 1];
                float age = particle.getAge();
                for (int i = 0; i < agesDepthNight.length - 1; i++) {
                    if (agesDepthDay[i] <= age && age < agesDepthNight[i + 1]) {
                        depthNight = depthsNight[i];
                        break;
                    }
                }
            }
            output = depthNight;
        }

        output = (output < bottom) ? particle.getDepth() : output;

        return output;
    }
}
