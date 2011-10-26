/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.util.MTRandom;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.arch.IDataset;

/**
 *
 * @author pverley
 */
public class VDispAction extends AbstractAction {

    private MTRandom random;
    private String kv_field;

    public void loadParameters() throws Exception {
        random = new MTRandom(true);
        kv_field = getParameter("kv_field");
        getSimulationManager().getDataset().requireVariable(kv_field, getClass());
    }

    public void execute(IBasicParticle particle) {
        particle.increment(getVDispersion(particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime(), getSimulationManager().getTimeManager().get_dt()));
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

        double[] kvSpline = getKv(pGrid, time, dt);
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

    private double[] getKv(double[] pGrid, double time, double dt) {

        IDataset dataset = getSimulationManager().getDataset();
        double co, CO = 0.d, Kv = 0.d, diffKv = 0.d, Hz = 0.d;
        double x, y, z, dx, dy;
        int i, j, k;
        int n = dataset.isCloseToCost(pGrid) ? 1 : 2;
        double[] kvSpline;
        double depth;

        x = pGrid[0];
        y = pGrid[1];
        z = Math.max(0.d, Math.min(pGrid[2], dataset.get_nz() - 1.00001f));
        depth = dataset.z2depth(x, y, z);

        i = (int) x;
        j = (int) y;
        k = (int) Math.round(z);
        dx = x - Math.floor(x);
        dy = y - Math.floor(y);

        for (int ii = 0; ii < n; ii++) {
            for (int jj = 0; jj < n; jj++) {
                co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
                CO += co;
                kvSpline = getKv(i + ii, j + jj, depth, time, dt);
                diffKv += kvSpline[0] * co;
                Kv += kvSpline[1] * co;
                Hz += co * (dataset.z2depth(i + ii, j + jj, k + 1.5) - dataset.z2depth(i + ii, j + jj, Math.max(k - 1.5, 0)));
            }
        }
        if (CO != 0) {
            diffKv /= CO;
            Kv /= CO;
            Hz /= CO;
        }

        return new double[]{diffKv, Kv, Hz};
    }

    private double[] getKv(int i, int j, double depth, double time, double dt) {

        IDataset dataset = getSimulationManager().getDataset();
        double diffzKv, Kvzz, ddepth, dz, zz;
        double[] Kv = new double[dataset.get_nz()];
        double a, b, c, d;
        int k;
        double z;
        for (k = dataset.get_nz(); k-- > 0;) {
            Kv[k] = dataset.get(kv_field, new double[] {i, j, k}, time).doubleValue();
        }

        z = Math.min(dataset.depth2z(i, j, depth), dataset.get_nz() - 1.00001f);
        k = (int) z;
        //dz = z - Math.floor(z);
        ddepth = depth - dataset.z2depth(k, j, i);
        /** Compute the polynomial coefficients of the piecewise of the spline
         * contained between [k; k + 1]. Let's take M = Kv''
         * a = (M(k + 1) - M(k)) / 6
         * b = M(k) / 2
         * c = Kv(k + 1) - Kv(k) - (M(k + 1) - M(k)) / 6
         * d = Kv(k);
         */
        a = (diff2(Kv, k + 1) - diff2(Kv, k)) / 6.d;
        b = diff2(Kv, k) / 2.d;
        c = (Kv[k + 1] - Kv[k]) - (diff2(Kv, k + 1) + 2.d * diff2(Kv, k)) / 6.d;
        d = Kv[k];

        /** Compute Kv'(z)
         * Kv'(z) = 3.d * a * dz2 + 2.d * b * dz + c; */
        diffzKv = c + ddepth * (2.d * b + 3.d * a * ddepth);

        zz = Math.min(dataset.depth2z(i, j, depth + 0.5d * diffzKv * dt), dataset.get_nz() - 1.00001f);
        dz = zz - Math.floor(z);
        if (dz >= 1.f || dz < 0) {
            k = (int) zz;
            a = (diff2(Kv, k + 1) - diff2(Kv, k)) / 6.d;
            b = diff2(Kv, k) / 2.d;
            c = (Kv[k + 1] - Kv[k])
                    - (diff2(Kv, k + 1) + 2.d * diff2(Kv, k)) / 6.d;
            d = Kv[k];
        }
        ddepth = depth + 0.5d * diffzKv * dt - dataset.z2depth(k, j, i);
        /** Compute Kv(z)
         * Kv(z) = a * dz3 + b * dz2 + c * dz + d;*/
        Kvzz = d + ddepth * (c + ddepth * (b + ddepth * a));
        Kvzz = Math.max(0.d, Kvzz);

        return new double[]{diffzKv, Kvzz};
    }

    private double diff2(double[] X, int k) {

        int length = X.length;
        /** Returns NaN if size <= 2 */
        if (length < 3) {
            return Double.NaN;
        }

        /** This return statement traduces the natural spline hypothesis
         * M(0) = M(nz - 1) = 0 */
        if ((k <= 0) || (k >= (length - 1))) {
            return 0.d;
        }

        return (X[k + 1] - 2.d * X[k] + X[k - 1]);
    }
}
