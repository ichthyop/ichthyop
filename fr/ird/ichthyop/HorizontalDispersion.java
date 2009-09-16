/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public class HorizontalDispersion extends AbstractAction {

    private static final boolean DEBUG_HDISP = false;
    /**
     * Turbulent dissipation rate used in the parametrization of Lagrangian
     * horizontal diffusion.
     * @see Monin and Ozmidov, 1981
     */
    private final static double EPSILON = 1e-9;
    private final static double EPSILON16 = Math.pow(EPSILON, 1.d / 6.d);
    private double dt;
    private MTRandom random;

    public void loadParameters() {
        dt = Integer.valueOf(getParameter("time.timeStep"));
        random = new MTRandom();
    }

    public void execute(IBasicParticle particle) {
        particle.increment(getHDispersion(particle.getGridPoint(), dt));
    }

    public double[] getHDispersion(double[] pGrid, double dt) {

        int i = (int) Math.round(pGrid[0]), j = (int) Math.round(pGrid[1]);
        double R = 2.d * random.nextDouble() - 1.d;

        if (DEBUG_HDISP) {
            double epsilon16 = Math.pow(1e-6, 1.d / 6.d);
            return new double[]{
                        R * Math.sqrt(2.d * dt) * epsilon16 *
                        Math.pow(getSimulation().getDataset().getdxi(j, i), -1.d / 3.d),
                        R * Math.sqrt(2.d * dt) * epsilon16 *
                        Math.pow(getSimulation().getDataset().getdeta(j, i), -1.d / 3.d)};
        }

        return new double[]{
                    R * Math.sqrt(2.d * dt) * EPSILON16 *
                    Math.pow(getSimulation().getDataset().getdxi(j, i), -1.d / 3.d),
                    R * Math.sqrt(2.d * dt) * EPSILON16 *
                    Math.pow(getSimulation().getDataset().getdeta(j, i), -1.d / 3.d)};
    }
}
