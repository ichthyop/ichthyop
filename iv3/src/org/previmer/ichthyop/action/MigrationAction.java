/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import java.text.SimpleDateFormat;
import org.previmer.ichthyop.arch.IBasicParticle;
import java.util.Calendar;
import java.util.Date;
import org.previmer.ichthyop.particle.GrowingParticleLayer;
import org.previmer.ichthyop.particle.GrowingParticleLayer.Stage;

/**
 *
 * @author pverley
 */
public class MigrationAction extends AbstractAction {

    private float depthDay;
    private float depthNight;
    private Date sunrise;
    private Date sunset;
    private Calendar calendar;
    private boolean isodepth;
    private long minimumAge;
    private boolean isGrowth;
    private TypeMigration type;

    public void loadParameters() throws Exception {

        if (!getSimulationManager().getDataset().is3D()) {
            throw new UnsupportedOperationException("{Migration} Vertical migration cannot operate in 2D simulation. Please deactivate the block or run a 3D simulation.");
        }

        isGrowth = getSimulationManager().getActionManager().isEnabled("action.growth");
        calendar = (Calendar) getSimulationManager().getTimeManager().getCalendar().clone();
        type = TypeMigration.SUNSET_SUNRISE;
        if (isGrowth) {
            try {
                type = getParameter("type").matches(TypeMigration.SUNSET_SUNRISE.getType())
                        ? TypeMigration.SUNSET_SUNRISE
                        : TypeMigration.LENGTH;
            } catch (Exception ex) {
                // default type will be sunset / sunrise migration
            }
        } else {
            minimumAge = (long) (Float.valueOf(getParameter("age_min")) * 24.f * 3600.f);
            type = TypeMigration.SUNSET_SUNRISE;
        }
        depthDay = Float.valueOf(getParameter("daytime_depth"));
        depthNight = Float.valueOf(getParameter("nighttime_depth"));
        SimpleDateFormat hourFormat = new SimpleDateFormat("HH:mm");
        hourFormat.setCalendar(calendar);
        sunset = hourFormat.parse(getParameter("sunset"));
        sunrise = hourFormat.parse(getParameter("sunrise"));
        isodepth = (depthDay == depthNight);
    }

    public void execute(IBasicParticle particle) {

        /** Ensures larva stage */
        boolean isSatisfiedCriterion = false;
        if (!isGrowth) {
            isSatisfiedCriterion = particle.getAge() > minimumAge;
        } else {
            isSatisfiedCriterion = ((GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class)).getStage() != Stage.EGG;
        }

        if (isSatisfiedCriterion) {
            double depth = 0.d;
            if (isodepth) {
                /** isodepth migration */
                depth = depthDay;
            } else {
                if (type == TypeMigration.SUNSET_SUNRISE) {
                    /** diel vertical migration */
                    depth = getDepth(particle.getX(), particle.getY());
                } else {
                    double length = ((IGrowingParticle) particle.getLayer(GrowingParticleLayer.class)).getLength();
                    depth = getDepth(particle.getX(), particle.getY(), length);
                }
            }
            double dz = getSimulationManager().getDataset().depth2z(particle.getX(), particle.getY(), depth) - particle.getZ();
            particle.increment(new double[]{0.d, 0.d, dz}, false, true);
        }
    }

    /**
     * Computes the depth of the particle according to the diel vertical
     * migration behavior. At <code>DAYTIME</code> the particle stays at
     * <code>DEPTH_DAYTIME</code> depth and jumps to
     * <code>DEPTH_NIGHTTIME</code> at <code>NIGHTTIME</code>.
     *
     * @param x a double, the x grid coordinate of the particle
     * @param y a double, the y grid coordinate of the particle
     * @param time a double, the current time [second] of the simulation
     * @param data the Dataset of the simulation
     * @return a double, the depth [meter] of the particle at this time
     * of the day.
     */
    private double getDepth(double x, double y) {

        double bottom = getSimulationManager().getDataset().z2depth(x, y, 0);
        calendar.setTimeInMillis((long) (getSimulationManager().getTimeManager().getTime() * 1e3));
        long timeDay = getSecondsOfDay(calendar);
        calendar.setTime(sunrise);
        long timeSunrise = getSecondsOfDay(calendar);
        calendar.setTime(sunset);
        long timeSunset = getSecondsOfDay(calendar);

        if (timeDay >= timeSunrise && timeDay < timeSunset) {
            return Math.max(bottom, depthDay);
        } else {
            return Math.max(bottom, depthNight);
        }
    }

    private double getDepth(double x, double y, double length) {

        double bottom = getSimulationManager().getDataset().z2depth(x, y, 0);
        calendar.setTimeInMillis((long) (getSimulationManager().getTimeManager().getTime() * 1e3));
        long time = getSecondsOfDay(calendar);

        if (length >= 2.8 && length <= 15.9) {
            double factor = 0.0333 * length + 0.4667;
            double tpi = Math.PI * ((time + 3600) - 3600) / (24 * 3600);
            double depth_dvm = factor * (-((1 - Math.cos(2 * tpi)) / 2) * (depthDay - depthNight)) - depthNight;
            return Math.max(bottom, depth_dvm);

        } else if (length > 15.9) {
            double factor = 1;
            double tpi = Math.PI * ((time + 3600) - 3600) / (24 * 3600);
            double depth_dvm = factor * (-((1 - Math.cos(2 * tpi)) / 2) * (depthDay - depthNight)) - depthNight;
            return Math.max(bottom, depth_dvm);
        } else {
            double factor = 1;
            double tpi = Math.PI * ((time + 3600) - 3600) / (24 * 3600);
            double depth_dvm = factor * (-((1 - Math.cos(2 * tpi)) / 2) * (depthDay - depthNight)) - depthNight;
            return Math.max(bottom, depth_dvm);
        }
    }

    private long getSecondsOfDay(Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) * 3600 + calendar.get(Calendar.MINUTE) * 60;
    }

    enum Criterion {

        AGE("Age criterion"),
        LENGTH("Length criterion");
        private String name;

        Criterion(String name) {
            this.name = name;
        }

        String getName() {
            return name;
        }
    }

    enum TypeMigration {

        SUNSET_SUNRISE("Based on sunset / sunrise"),
        LENGTH("Based on particle length");
        private String type;

        TypeMigration(String type) {
            this.type = type;
        }

        String getType() {
            return type;
        }
    }
}
