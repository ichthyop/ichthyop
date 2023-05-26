package org.previmer.ichthyop.action.orientation;

import org.previmer.ichthyop.action.AbstractAction;
import org.previmer.ichthyop.particle.IParticle;

public class ReefOrientationAction extends AbstractAction {

    @Override
    public void loadParameters() throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'loadParameters'");
    }

    @Override
    public void execute(IParticle particle) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'execute'");
    }

    @Override
    public void init(IParticle particle) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'init'");
    }



    public int findClosestReef(IParticle particle, double[] lonBarycenter, double[] latBarycenter) {

        int NReefs = lonBarycenter.length;
        double[] distance = new double[NReefs];

        double lonParticle = particle.getLon();
        double latParticle = particle.getLat();



        for(int k = 0; k < NReefs; k++) {
            distance[k] = getSimulationManager().getDataset().getDistGetter().getDistance(latParticle, lonParticle,
                    latBarycenter[k], lonBarycenter[k]);
        }

        double minDistance = Double.MAX_VALUE;
        int p = -1;
        for (int k = 0; k < NReefs; k++) {
            if (distance[k] <= minDistance) {
                p = k;
                minDistance = distance[k];
            }
        }

        return p;

    }

}
