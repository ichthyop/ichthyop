package org.previmer.ichthyop.action.orientation;

import java.util.logging.Level;

import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.particle.IParticle;

public abstract class OrientationVelocity extends AbstractAction {

    private double swimmingSpeedHatch;
    private double swimmingSpeedSettle;
    private double secs_in_day = 86400;
    double PLD;

    @FunctionalInterface
    public interface InnerOrientationVelocity {
        double getVelocity(IParticle particle);
    }

    private InnerOrientationVelocity velocityMethod;

    @Override
    public void loadParameters() throws Exception {
        swimmingSpeedHatch = Double.valueOf(getParameter("swimming.speed.hatch"));
        swimmingSpeedSettle = Double.valueOf(getParameter("swimming.speed.settle"));

        if (swimmingSpeedHatch > swimmingSpeedSettle) {
            getLogger().log(Level.WARNING, "Hatch and Settle velocity have been swapped");
            double temp = swimmingSpeedHatch;
            swimmingSpeedHatch = swimmingSpeedSettle;
            swimmingSpeedSettle = temp;
        }

        double timeMax = getSimulationManager().getTimeManager().getSimulationDuration();
        PLD = timeMax / (secs_in_day);
        velocityMethod = (IParticle particle) -> getVelocityPLD(particle);

    }

    @Override
    public void init(IParticle particle) {

    }

    public double getVelocity(IParticle particle) {
        return velocityMethod.getVelocity(particle);
    }

    public double getVelocityPLD(IParticle particle) {

        double age = particle.getAge() / (secs_in_day) + Float.MIN_VALUE;
        double swimmingSpeed = swimmingSpeedHatch + Math.pow(10,
                (Math.log10(age) / Math.log10(PLD)) * Math.log10(swimmingSpeedSettle - swimmingSpeedHatch));
        swimmingSpeed = swimmingSpeed / 100;
        return swimmingSpeed;
    }

}
