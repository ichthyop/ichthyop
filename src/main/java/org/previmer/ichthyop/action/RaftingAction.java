package org.previmer.ichthyop.action;

import org.previmer.ichthyop.particle.IParticle;

public class RaftingAction extends AbstractAction {

    @Override
    public void loadParameters() throws Exception {

    }

    @Override
    public void execute(IParticle particle) {
        if (getSimulationManager().getDataset().is3D()) {
            if (this.isActive(particle)) {
                double depth = particle.getDepth();
                double[] move = new double[] {0, 0, -depth};
                particle.increment(move);
            }
        }
    }

    @Override
    public void init(IParticle particle) {

    }

}
