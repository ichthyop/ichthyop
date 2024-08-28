package org.previmer.ichthyop.action.orientation;

import java.util.logging.Level;

import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.util.VonMisesRandom;

public class RheotaxisOrientationAction extends OrientationVelocity {

    private double vonMisesKappa;
    public static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    private double ageMin, ageMax;

    private VonMisesRandom vonMises;
    private double secs_in_day = 86400;

    double dt;
    private double PLD;

    @Override
    public void loadParameters() throws Exception {
        super.loadParameters();
        vonMisesKappa = Double.valueOf(getParameter("swimming.von.mises.kappa"));
        double secs_in_day = 86400;

        // Provides age in days
        if (getParameter("age.min") != null) {
            ageMin = Double.valueOf(getParameter("age.min"));
        } else {
            ageMin = 0;
        }

        if (getParameter("age.max") != null) {
            ageMax = Double.valueOf(getParameter("age.max"));
        } else {
            ageMax = Double.MAX_VALUE;
        }

        ageMin *= secs_in_day;
        ageMax *= secs_in_day;

    }

    @Override
    public void execute(IParticle particle) {

        if(particle.getAge() < ageMin || particle.getAge() >= ageMax) {
            return;
        }

        double[] mvt = getDlonDlat(particle);
        double newLon = particle.getLon() + mvt[0];
        double newLat = particle.getLat() + mvt[1];
        double[] newPos = getSimulationManager().getDataset().latlon2xy(newLat, newLon);
        double[] posIncr = new double[]{newPos[0] - particle.getX(), newPos[1] - particle.getY()};
        particle.increment(posIncr);
    }

    /** Computes the longitude/latitude increment */
    private double[] getDlonDlat(IParticle particle) {

        double swimmingSpeed = getVelocity(particle);

        double[] pGrid;
        if(getSimulationManager().getDataset().is3D()) {
            pGrid = new double[] {particle.getX(), particle.getY(), particle.getZ()};
        } else {
            pGrid = new double[] { particle.getX(), particle.getY() };
        }

        double uf = getSimulationManager().getDataset().get_dUx(pGrid, getSimulationManager().getTimeManager().getTime(), false);
        double vf = getSimulationManager().getDataset().get_dVy(pGrid, getSimulationManager().getTimeManager().getTime(), false);
        double uv = Math.sqrt(uf * uf + vf * vf);

        double ti = vonMises.nextDouble();
        // ti = 0;

        // Compute rheotaxis heading
        // i.e. the heading opposite to the current
        double thetaRheo = Math.atan2(vf, uf) + Math.PI;
        double theta = thetaRheo + ti;

        // Larvae cannot swim against the current. Therefore,
        // if the swimming speed is greater that the current, we
        // set the swimming speed as equal to the current
        swimmingSpeed = Math.min(swimmingSpeed, uv);

        double uorient = swimmingSpeed * Math.cos(theta);
        double vorient = swimmingSpeed * Math.sin(theta);

        double dx = uorient * dt;
        double dy = vorient * dt;

        double[] latlon = getSimulationManager().getDataset().xy2latlon(particle.getX(), particle.getY());
        double one_deg_lon_meter = ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * latlon[0] / 180.d);
        double dLon = dx / one_deg_lon_meter;
        double dLat = dy / ONE_DEG_LATITUDE_IN_METER;

        return new double[] {dLon, dLat};

    }

    @Override
    public void init(IParticle particle) {

        vonMises = new VonMisesRandom(0, vonMisesKappa);
        dt = getSimulationManager().getTimeManager().get_dt();

        double timeMax = getSimulationManager().getTimeManager().getSimulationDuration();
        PLD = timeMax / (secs_in_day);

    }

}
