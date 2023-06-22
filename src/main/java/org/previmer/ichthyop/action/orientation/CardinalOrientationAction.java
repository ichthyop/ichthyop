package org.previmer.ichthyop.action.orientation;

import java.util.Random;

import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.util.VonMisesRandom;

public class CardinalOrientationAction extends AbstractAction {

    private double swimmingSpeedHatch;
    private double swimmingSpeedSettle;
    private double thetaCard;
    public static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    private double dt;
    private double vonMisesKappa;

    private VonMisesRandom vonMises;
    private double secs_in_day = 86400;

    @Override
    public void loadParameters() throws Exception {
        swimmingSpeedHatch = Double.valueOf(getParameter("swimming.speed.hatch"));
        swimmingSpeedSettle = Double.valueOf(getParameter("swimming.speed.settle"));
        thetaCard = Math.toRadians(Double.valueOf(getParameter("swimming.cardinal.heading")));
        vonMisesKappa = Math.toRadians(Double.valueOf(getParameter("swimming.von.mises.kappa")));
    }

    @Override
    public void execute(IParticle particle) {
        double[] mvt = getDlonDlat(particle);
        double newLon = particle.getLon() + mvt[0];
        double newLat = particle.getLat() + mvt[1];
        double[] newPos = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
        double[] posIncr = new double[]{newPos[0] - particle.getX(), newPos[1] - particle.getY()};
        particle.increment(posIncr);
    }

    @Override
    public void init(IParticle particle) {
        vonMises = new VonMisesRandom(0, vonMisesKappa);
        dt = getSimulationManager().getTimeManager().get_dt();
    }

    private double[] getDlonDlat(IParticle particle) {

	// Random sampling of Von Mises
	double ti = vonMises.nextDouble();
	// Preferred direction plus stochastic behavior
	double theta = thetaCard + ti;

    double age = particle.getAge() / (secs_in_day);
    double timeMax = getSimulationManager().getTimeManager().getSimulationDuration() / (secs_in_day);
    double PLD = timeMax / (secs_in_day);
    double swimmingSpeed = swimmingSpeedHatch + Math.pow(10,
            ((Math.log10(age) / Math.log10(PLD)) * Math.log10(swimmingSpeedSettle - swimmingSpeedHatch)));
    swimmingSpeed = swimmingSpeed / 100;

	// Compute u and v orientation velocity
	double uorient = swimmingSpeed * Math.cos(theta);
	double vorient = swimmingSpeed * Math.sin(theta);

    double dx = uorient * dt;
    double dy = vorient * dt;

    double[] latlon = getSimulationManager().getDataset().xy2latlon(particle.getX(), particle.getY());
    double one_deg_lon_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * latlon[0] / 180.d);
    double dLon = dx / one_deg_lon_meter;
    double dLat = dy / ONE_DEG_LATITUDE_IN_METER;

    return new double[] { dLon, dLat };

    }


}
