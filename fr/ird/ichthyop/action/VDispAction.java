/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.action;

import fr.ird.ichthyop.util.MTRandom;
import fr.ird.ichthyop.arch.IBasicParticle;

/**
 *
 * @author pverley
 */
public class VDispAction extends AbstractAction {

    private MTRandom random;

    public void loadParameters() {
        random = new MTRandom();
    }

    public void execute(IBasicParticle particle) {
        particle.increment(getVDispersion(particle.getGridPoint(), getSimulationManager().getTimeManager().getTime(), getSimulationManager().getTimeManager().get_dt()));
    }

    /**
     * Simulates vertical dispersion.
     * We used the Random walk model thoroughly described by André W Visser
     * with reflecting boundary conditions.
     * The equation includes a random component and a non random advective
     * component from areas of low diffusivity to areas of high diffusivity.
     * This second term represents the gradient of diffusivity
     * and prevents form artificial accumlation of particles in areas of low
     * diffusivity.
     *
     * <p>
     * The equation requires the diffusivity K at any point z and the first
     * derivative of K at point z.
     * In paper North et all 2006, it is shown that a mere linear interpolation
     * of the diffusivity leads to artificial aggregation of particles where
     * abrupt changes in vertical diffusivity occured. She suggested to fit a
     * continuous function (tension spline) to a smoothed profile of vertical
     * diffusivities. In ROMS and MARS NetCDF dataset, the vertical diffusivity
     * has already been average in time so we decided to skip the preliminary
     * spatial smoothing of the diffusivity profile. Please see {@link #getKv}
     * for details about the computation of the diffusivity at any depth.
     * </p>
     * <p>
     * Reference:
     * <ul>
     * <li>Visser 1997. Using random walk models to simulate the vertical
     * distribution of particles in a turbulent water column.
     * Paper can be download from
     * {@link http://www.int-res.com/articles/meps/158/m158p275.pdf}
     * <li>North, E. W., R. R. Hood, S.-Y. Chao, and L. P. Sanford. 2006.
     * Using a random displacement model to simulate turbulent particle motion
     * in a baroclinic frontal zone: a new implementation scheme and model
     * performance tests. Journal of Marine Systems 60: 365-380.
     * </ul>
     * </p>
     *
     * @param pGrid a double[] grid coordinates (x, y, z) of the particle
     * @param dt a double, simulation time step [second]
     * @return a double[], the move of the particle (0, 0, dz) due to vertical
     * dispersion.
     * @see #getKv for details about the calculation of the diffusivity and the
     * first derivative.
     */
    public double[] getVDispersion(double[] pGrid, double time, double dt) {

        double[] kvSpline = getSimulationManager().getDataset().getKv(pGrid, time, dt);
        double R = 2.d * random.nextDouble() - 1.d;
        double dz = kvSpline[0] * dt + R * Math.sqrt(6.d * kvSpline[1] * dt);

        /** adimensionalize */
        dz /= kvSpline[2];

        kvSpline = null;

        /** Reflecting boundary conditions */
        double newz = pGrid[2] + dz;
        if (newz < 0) {
            dz = -(2.d * pGrid[2] + dz);
        }
        int nz = getSimulationManager().getDataset().get_nz();
        if (newz >= nz - 1) {
            dz = 2.d * (nz - 1 - pGrid[2]) - dz;
        }
        return new double[]{0.d, 0.d, dz};
    }
}
