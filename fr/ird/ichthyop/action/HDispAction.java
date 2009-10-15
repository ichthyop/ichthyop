/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.action;

import fr.ird.ichthyop.util.MTRandom;
import fr.ird.ichthyop.*;
import fr.ird.ichthyop.arch.IBasicParticle;

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

    public void loadParameters() {
        random = new MTRandom();
        epsilon = Double.valueOf(getParameter("dissipation_rate"));
        epsilon16 = Math.pow(epsilon, 1.d / 6.d);
    }

    public void execute(IBasicParticle particle) {
        particle.increment(getHDispersion(particle.getGridPoint(), getSimulation().getStep().get_dt()));
    }

    public double[] getHDispersion(double[] pGrid, double dt) {

        int i = (int) Math.round(pGrid[0]), j = (int) Math.round(pGrid[1]);
        double R = 2.d * random.nextDouble() - 1.d;

        if (DEBUG_HDISP) {
            double my_epsilon16 = Math.pow(1e-6, 1.d / 6.d);
            return new double[]{
                        R * Math.sqrt(2.d * dt) * my_epsilon16 *
                        Math.pow(getSimulation().getDataset().getdxi(j, i), -1.d / 3.d),
                        R * Math.sqrt(2.d * dt) * my_epsilon16 *
                        Math.pow(getSimulation().getDataset().getdeta(j, i), -1.d / 3.d)};
        }

        return new double[]{
                    R * Math.sqrt(2.d * dt) * epsilon16 *
                    Math.pow(getSimulation().getDataset().getdxi(j, i), -1.d / 3.d),
                    R * Math.sqrt(2.d * dt) * epsilon16 *
                    Math.pow(getSimulation().getDataset().getdeta(j, i), -1.d / 3.d)};
    }
}
