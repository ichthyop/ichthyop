/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.util.MTRandom;
import org.previmer.ichthyop.*;
import org.previmer.ichthyop.arch.IBasicParticle;

/**
 *
 * @author pverley
 */
public class HDispAction extends AbstractAction {

    private static final boolean DEBUG_HDISP = false;
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
        double R = 2.d * random.nextDouble() - 1.d;

        if (DEBUG_HDISP) {
            double my_epsilon16 = Math.pow(1e-6, 1.d / 6.d);
            return new double[]{
                        R * Math.sqrt(2.d * dt) * my_epsilon16 *
                        Math.pow(getSimulationManager().getDataset().getdxi(j, i), -1.d / 3.d),
                        R * Math.sqrt(2.d * dt) * my_epsilon16 *
                        Math.pow(getSimulationManager().getDataset().getdeta(j, i), -1.d / 3.d)};
        }

        return new double[]{
                    R * Math.sqrt(2.d * dt) * epsilon16 *
                    Math.pow(getSimulationManager().getDataset().getdxi(j, i), -1.d / 3.d),
                    R * Math.sqrt(2.d * dt) * epsilon16 *
                    Math.pow(getSimulationManager().getDataset().getdeta(j, i), -1.d / 3.d)};
    }
}
