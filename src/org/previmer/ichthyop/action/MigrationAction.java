/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import au.com.bytecode.opencsv.CSVReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import org.previmer.ichthyop.particle.IParticle;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import org.previmer.ichthyop.io.IOTools;
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
    private Date sunrise;
    /**
     * Time of sunset.
     */
    private Date sunset;
    /**
     * Clone of the simulation calendar for assessing whether current time is
     * day time or night.
     */
    private Calendar calendar;
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
        calendar = (Calendar) getSimulationManager().getTimeManager().getCalendar().clone();
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
            CSVReader reader = new CSVReader(new FileReader(pathname), ';');
            List<String[]> lines = reader.readAll();
            Iterator iter = lines.iterator();
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
            CSVReader reader = new CSVReader(new FileReader(pathname), ';');
            List<String[]> lines = reader.readAll();
            Iterator iter = lines.iterator();
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
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        hourFormat.setCalendar(calendar);
        sunset = hourFormat.parse(getParameter("sunset"));
        sunrise = hourFormat.parse(getParameter("sunrise"));
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
    private double getDepth(IParticle particle, long time) {
        // Time in milliseconds
        calendar.setTimeInMillis(time * 1000L);
        long timeDay = getSecondsOfDay(calendar);
        calendar.setTime(sunrise);
        long timeSunrise = getSecondsOfDay(calendar);
        calendar.setTime(sunset);
        long timeSunset = getSecondsOfDay(calendar);

        double bottom = getSimulationManager().getDataset().z2depth(particle.getX(), particle.getY(), 0);
        if (timeDay >= timeSunrise && timeDay < timeSunset) {
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
            return Math.max(bottom, depthDay);
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
            return Math.max(bottom, depthNight);
        }
    }

    /**
     * Express current time as a number of seconds since midnight.
     *
     * @param calendar the calendar set to current time
     * @return the number of seconds elapsed since midnight
     */
    private long getSecondsOfDay(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60;
    }
}
