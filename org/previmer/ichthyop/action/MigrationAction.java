/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IGrowingParticle;
import org.previmer.ichthyop.arch.IBasicParticle;
import java.util.Calendar;
import org.previmer.ichthyop.particle.GrowingParticleLayer;

/**
 *
 * @author pverley
 */
public class MigrationAction extends AbstractAction {

    private float depthDay;
    private float depthNight;
    private int sunrise;
    private int sunset;
    private Calendar calendar;
    private boolean isodepth;
    private boolean isAgeLimitation;
    private float limitAge;
    private double limitLength;

    public void loadParameters() {

        calendar = getSimulationManager().getTimeManager().getCalendar();
        isAgeLimitation = getParameter("migration_criterion").matches("age_criterion");
        if (isAgeLimitation) {
            limitAge = Float.valueOf(getParameter("migration_limit_age"));
        } else {
            limitLength = Float.valueOf(getParameter("migration_limit_length"));
        }
        depthDay = Float.valueOf(getParameter("migration_depth_day"));
        depthNight = Float.valueOf(getParameter("migration_depth_night"));
        sunset = Integer.valueOf(getParameter("sunset_hour"));
        sunrise = Integer.valueOf(getParameter("sunrise_hour"));

        isodepth = (depthDay == depthNight);
    }

    public void execute(IBasicParticle particle) {

        /** Ensures larva stage */
        boolean isSatisfiedCriterion = false;
        if (isAgeLimitation) {
            isSatisfiedCriterion = particle.getAge() > limitAge;
        } else {
            isSatisfiedCriterion = ((IGrowingParticle) particle.getLayer(GrowingParticleLayer.class)).getLength() > limitLength;
        }

        if (isSatisfiedCriterion) {
            double depth = 0.d;
            if (isodepth) {
                /** isodepth migration */
                depth = depthDay;
            } else {
                /** diel vertical migration */
                depth = getDepth(particle.getX(), particle.getY());
            }
            double dz = getSimulationManager().getDataset().depth2z(particle.getX(), particle.getY(), depth) - particle.getZ();
            particle.increment(new double[] {0.d, 0.d, dz});
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
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= sunrise && hour < sunset) {
            return Math.max(bottom, depthDay);
        } else {
            return Math.max(bottom, depthNight);
        }
    }

    /**
     * Determines whether the specified time is daytime or nighttime.
     *
     * @param time a double, the specified time [second]
     * @return <code>true</code> if daytime, <code>false</code> otherwise.
     */
    private boolean isDaytime(double time) {

        if (calendar == null) {
            return false;
        }
        calendar.setTimeInMillis((long) (time * 1e3));
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        return (hour >= sunrise && hour < sunset);
    }
}
