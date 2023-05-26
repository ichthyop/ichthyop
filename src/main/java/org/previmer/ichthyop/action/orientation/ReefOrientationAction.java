package org.previmer.ichthyop.action.orientation;

import java.util.Random;

import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.particle.IParticle;

public class ReefOrientationAction extends AbstractAction {

    private double maximumDistance;
    private double horDiffOrient;
    private double dtturb;

    private Random randomGenerator;
    double dt;

    @Override
    public void loadParameters() throws Exception {

        maximumDistance = Double.valueOf(getParameter("maximum.distance"));
        randomGenerator = new Random();
        dt = getSimulationManager().getTimeManager().get_dt();
    }

    @Override
    public void execute(IParticle particle) {

        double uorient, vorient;
        int N = 5;
        double[] latBarycenter = new double[N];
        double[] lonBarycenter = new double[N];
        double[] distance = this.computeReefDistance(particle, lonBarycenter, latBarycenter);

        int closestReefIndex = this.findSmallestDistance(distance);
        double closestReefDistance = distance[closestReefIndex];

        if(closestReefDistance > maximumDistance) {

            double htvelscl = Math.sqrt((2*horDiffOrient)/dtturb);
            double norm1 = randomGenerator.nextGaussian();
            double norm2 = randomGenerator.nextGaussian();

            uorient = norm1 * htvelscl;
            vorient = norm2 * htvelscl;

        } else {

            uorient = 0;
            vorient = 0;

        }

        double dx = uorient * dt;
        double dy = vorient * dt;
        particle.increment(new double[]{dx, dy});

    }

    @Override
    public void init(IParticle particle) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'init'");
    }

    public int findSmallestDistance(double[] distance) {

        int p = -1;
        double minDistance = Double.MAX_VALUE;
        for (int k = 0; k < distance.length; k++) {
            if (distance[k] <= minDistance) {
                p = k;
                minDistance = distance[k];
            }
        }

        return p;

    }

    public double[] computeReefDistance(IParticle particle, double[] lonBarycenter, double[] latBarycenter) {

        int NReefs = lonBarycenter.length;
        double[] distance = new double[NReefs];

        double lonParticle = particle.getLon();
        double latParticle = particle.getLat();

        for(int k = 0; k < NReefs; k++) {
            distance[k] = getSimulationManager().getDataset().getDistGetter().getDistance(latParticle, lonParticle,
                    latBarycenter[k], lonBarycenter[k]);
        }

        return distance;

    }

}
