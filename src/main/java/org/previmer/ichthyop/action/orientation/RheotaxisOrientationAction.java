package org.previmer.ichthyop.action.orientation;

import org.previmer.ichthyop.dataset.IDataset;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.util.VonMisesRandom;

public class RheotaxisOrientationAction extends OrientationVelocity {

    private double vonMisesKappa;
    public static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;

    private VonMisesRandom vonMises;
    private boolean canSwimAgainstCurrent = false;

    double dt;

    @Override
    public void loadParameters() throws Exception {
        super.loadParameters();
        vonMisesKappa = Double.valueOf(getParameter("swimming.von.mises.kappa"));

        if (getParameter("can.swim.against.current") != null) {
            canSwimAgainstCurrent = Boolean.valueOf(getParameter("can.swim.against.current"));
        }


        vonMises = new VonMisesRandom(0, vonMisesKappa);
        dt = getSimulationManager().getTimeManager().get_dt();

    }

    @Override
    public void execute(IParticle particle) {

        if (!this.isActive(particle)) {
            return;
        }

        double[] posIncr = getDXDY(particle);
        particle.increment(posIncr);

    }

    /** Computes the longitude/latitude increment */
    private double[] getDXDY(IParticle particle) {

        IDataset dataset = getSimulationManager().getDataset();
        double time = getSimulationManager().getTimeManager().getTime();

        double[] pGrid;
        if (dataset.is3D()) {
            pGrid = new double[] { particle.getX(), particle.getY(), particle.getZ() };
        } else {
            pGrid = new double[] { particle.getX(), particle.getY() };
        }

        double uf = dataset.get_dUx(pGrid, time, false);
        double vf = dataset.get_dVy(pGrid, time, false);
        double uv = Math.sqrt(uf * uf + vf * vf);

        if (uv < 1e-6) {
            // if current is less than a given value, particles don't
            // move. if not, they will be moving west
            return new double[] { 0, 0 };
        }

        double ti = vonMises.nextDouble();
        // ti = 0;

        // Compute rheotaxis heading
        // i.e. the heading opposite to the current
        double thetaRheo = Math.atan2(vf, uf) + Math.PI;
        double theta = thetaRheo + ti;

        double swimmingSpeed = getVelocity(particle);

        // If particle can't move against the current, set the velocity maximum to uv.
        swimmingSpeed = canSwimAgainstCurrent ? swimmingSpeed : Math.min(swimmingSpeed, uv);

        double uorient = swimmingSpeed * Math.cos(theta);
        double vorient = swimmingSpeed * Math.sin(theta);

        // Convert from m/s to grid displacement
        int i = (int) Math.round(particle.getX());
        int j = (int) Math.round(particle.getY());
        double dx = uorient / dataset.getdxi(j, i) * dt;
        double dy = vorient / dataset.getdeta(j, i) * dt;

        return new double[] { dx, dy };

    }

    @Override
    public void init(IParticle particle) {

        super.init(particle);

    }

}
