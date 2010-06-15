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

    public void loadParameters() throws Exception {

        calendar = getSimulationManager().getTimeManager().getCalendar();
        isAgeLimitation = getParameter("criterion").matches(Criterion.AGE.getName());
        if (isAgeLimitation) {
            limitAge = Float.valueOf(getParameter("limit_age"));
        } else {
            limitLength = Float.valueOf(getParameter("limit_length"));
        }
        depthDay = Float.valueOf(getParameter("daytime_depth"));
        depthNight = Float.valueOf(getParameter("nighttime_depth"));
        sunset = Integer.valueOf(getParameter("sunset"));
        sunrise = Integer.valueOf(getParameter("sunrise"));

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
        int hour = calendar.get(Calendar.HOUR_OF_DAY);

        if (hour >= sunrise && hour < sunset) {
            return Math.max(bottom, depthDay);
        } else {
            return Math.max(bottom, depthNight);
        }
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
}
