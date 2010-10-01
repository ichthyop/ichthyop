/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.util.MTRandom;
import org.previmer.ichthyop.arch.IBasicParticle;

/**
 *
 * @author pverley
 */
public class HDispAction extends AbstractAction {

    /**
     * Turbulent dissipation rate used in the parametrization of Lagrangian
     * horizontal diffusion.
     * @see Monin and Ozmidov, 1981
     */
    private static double epsilon;// = 1e-9;
    private static double epsilon16;
    private MTRandom random;

    public void loadParameters() throws Exception {
        random = new MTRandom();
        epsilon = Double.valueOf(getParameter("epsilon"));
        epsilon16 = Math.pow(epsilon, 1.d / 6.d);
    }

    public void execute(IBasicParticle particle) {
        particle.increment(getHDispersion(particle.getGridCoordinates(), getSimulationManager().getTimeManager().get_dt()));
    }

    public double[] getHDispersion(double[] pGrid, double dt) {

        int i = (int) Math.round(pGrid[0]), j = (int) Math.round(pGrid[1]);
        int nx = getSimulationManager().getDataset().get_nx();
        int ny = getSimulationManager().getDataset().get_ny();
        i = Math.max(Math.min(i, nx - 1), 0);
        j = Math.max(Math.min(j, ny - 1), 0);
        double Rx = 2.d * random.nextDouble() - 1.d;
        double Ry = 2.d * random.nextDouble() - 1.d;
        double dL = 0.5d * (getSimulationManager().getDataset().getdxi(j, i) + getSimulationManager().getDataset().getdeta(j, i));
        double cff = Math.sqrt(2.d * dt) * epsilon16 * Math.pow(dL, 2.d / 3.d);
        double dx = Rx * cff / getSimulationManager().getDataset().getdxi(j, i);
        double dy = Ry * cff / getSimulationManager().getDataset().getdeta(j, i);
        return new double[]{dx, dy};
    }
}
