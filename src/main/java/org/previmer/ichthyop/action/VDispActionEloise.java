/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 * 
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 * 
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.action;

import org.previmer.ichthyop.util.MTRandom;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.dataset.IDataset;

/**
 *
 * @author pverley
 */
public class VDispActionEloise extends AbstractAction {

    private MTRandom random;
    private String kv_field;
    
    /** Increase of resolution for the linear interpolation */
    private final int N = 5;
    
    private final int window = 8;
    private final int window2 = window / 2;

    public void loadParameters() throws Exception {
        random = new MTRandom(true);
        kv_field = getParameter("kv_field");
        getSimulationManager().getDataset().requireVariable(kv_field, getClass());
    }
    
     @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    public void execute(IParticle particle) {
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
        
        double dz, diffzKv;
        
        IDataset dataset = getSimulationManager().getDataset();
        double depth = dataset.z2depth(pGrid[0], pGrid[1], pGrid[2]);
        
        // Horizontal mean of the depth and K profiles
        double[][] verticalProfile = this.horizontalMean(pGrid, time, dt);
        
        // Linear interpolation of the vertical profile
        double[][] interpolatedProfile = this.linearInterpolation(verticalProfile);
        
        // Running mean of the interpolated profile
        double[][] runningMeanProfile = this.runningMean(interpolatedProfile);
                
        // Compute the spline values (dK and K) for the particle's depth
        double[] spline = this.compute_spline(runningMeanProfile, depth);
        diffzKv = spline[0];
        
        // Update the spline values for the updated particle's position
        double updatedZ = depth + 0.5d * diffzKv * dt;
        double[] updatedSpline = this.compute_spline(runningMeanProfile, updatedZ);
        double updatedKv = updatedSpline[1];
        
        double R = 2.d * random.nextDouble() - 1.d;

        dz = -(diffzKv * dt + R * Math.sqrt(6.d * updatedKv * dt));    
        double newz = dataset.z2depth(pGrid[0], pGrid[1], pGrid[2]) + dz;   
        double depth_max = dataset.z2depth(pGrid[0], pGrid[1], 0);

        // Reflecting boundary conditions 
        if (newz > 0){
            newz = -newz ; 
        }
        if (newz < depth_max){
            newz = depth_max - newz + depth_max;
        }
        
        // Using sigma coordinates 
        double vgrid = pGrid[2];
        double vgrid_newz = dataset.depth2z(pGrid[0], pGrid[1], newz);
        dz = vgrid_newz - vgrid;
        
        return new double[]{0.d, 0.d, dz};
        
    }

    /** Computes the second derivative */
    private double diff2(double[] X, double[] Z, int k) {

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
        
        return (X[k + 1] - 2.d * X[k] + X[k - 1]) / (Math.pow(Z[k + 1] - Z[k], 2));
        
    }
    
    /** Computes the cubic spline interpolation based on the equations of
     * CUBIC SPLINE INTERPOLATION: A REVIEW, by George Walberg
     * https://core.ac.uk/download/pdf/161439407.pdf */
    private double[] compute_spline(double[][] input, double depth) {
        
        double[] Zk = input[0];
        double[] Kv = input[1];
        int nz = Kv.length;
        int k;
        
        // First, find the k index of the interpolated depths
        for (k = 0; k < nz; k++) { 
            if (depth < Zk[k]) {
                break;
            }
        }
        k--;
        
        double a, b, c, d;
        
        double ddepth = depth - Zk[k];  // ddepth is always positive as well
        /** Compute the polynomial coefficients of the piecewise of the spline
         * contained between [k; k + 1]. Let's take M = Kv''
         * a = (M(k + 1) - M(k)) / 6  ==> A3
         * b = M(k) / 2  ==> A2
         * c = Kv(k + 1) - Kv(k) - (M(k + 1) - M(k)) / 6  ==> A1
         * d = Kv(k);  ==> A0
         */
         
        // depth between two consecutives cells
        
        double deltaZK = Zk[k + 1] - Zk[k];
        d = Kv[k];
        c = (Kv[k + 1] - Kv[k]) / deltaZK - deltaZK * (diff2(Kv, Zk, k + 1) + 2.d * diff2(Kv, Zk, k)) / 6.d;
        b = diff2(Kv, Zk, k) / 2.d;
        a = (diff2(Kv, Zk, k + 1) - diff2(Kv, Zk, k)) / (6.d * deltaZK);

        /** Compute Kv'(z) based on a(dx)^3 + b(dx)^2 + cdx + d
         * Kv'(z) = 3.d * a * dz2 + 2.d * b * dz + c; */
        double diffKv = c + ddepth * (2.d * b + 3.d * a * ddepth);
        double kZ = d + ddepth * (c + ddepth * (b + ddepth * a));
        return new double[] {diffKv, kZ};
        
    }
    
    /** Computes a linear interpolation of the Kv field stored at the Zk depth. 
     * The output grid is regular and resolution is increased by a N factor. 
     * Output is returned as a 2D array.
     * output[0][] = interpolated depths
     * output[1][] = interpolated K
     * */
    public double[][] linearInterpolation(double[][] input) { 
        
        int p;
        double[] Zk = input[0];
        double[] Kv = input[1];
        int nz = Zk.length;
        double[][] output = new double[2][this.N * nz];
        
        // creation of the interpolated depth
        double zMin = Zk[0];
        double zMax = Zk[nz - 1];
        double steps = (zMax - zMin) / (this.N * nz - 1);
        for (int k = 0; k < this.N * nz; k++) {
            output[0][k] = zMin + k * steps;
        }
        
        // interpolation of data
        for (int k = 0; k < this.N * nz; k++) {
            
            // Interpolated depth
            double zTemp = output[0][k];
            
            // Looking for the input k index used for the interpolation.
            // p is the index of the right most interpolation depth
            for (p = 0; p < nz; p++) { 
                if(zTemp < Zk[p]) { 
                    break;
                }   
            }
            // move p index to the left
            p--;
            double frac = (zTemp - Zk[p]) / (Zk[p + 1] - Zk[p]);
            output[1][k] = (1 - frac) * Kv[p] + frac * Kv[p + 1];
            
        }
        
        return output;
    }
    
    /** Computes a linear interpolation of the Kv field stored at the Zk depth. 
     * The output grid is regular and resolution is increased by a N factor. 
     * Output is returned as a 2D array.
     * output[0][] = interpolated depths
     * output[1][] = interpolated K
     * */
    public double[][] runningMean(double[][] input) { 

        int p;
        int nz = input[0].length;
        int newN = nz - 2 * window2 + 1;
        
        double[][] output = new double[2][newN];

        for (p = this.window2; p < nz - this.window2 + 1; p++) {
            for (int i = 0; i < window; i++) {
                output[0][p - window2] += input[0][i + p - window2] / window;
                output[1][p - window2] += input[1][i + p - window2] / window;
            }
        }

        return output;

    }
    
    
    /** Spatial interpolation of the Kv and diffKv values.
     * 
     * Returns a 2D array of dimension (2, nz) with 
     * output[0] = interpolated depth
     * output[1] = interpolated Kz
     * 
     * @param pGrid
     * @param time
     * @param dt
     * @return
     */
    private double[][] horizontalMean(double[] pGrid, double time, double dt) {

        int nz = getSimulationManager().getDataset().get_nz();
        
        // init the output for the spatially interpolated depths (first row) and Kv
        // (second row)
        double[][] output = new double[2][nz];
        IDataset dataset = getSimulationManager().getDataset();
        double co;
        double x, y, dx, dy;
        int i, j;
        int n = dataset.isCloseToCost(pGrid) ? 1 : 2;

        double[] CO = new double[nz];

        x = pGrid[0];
        y = pGrid[1];
        i = (int) x;
        j = (int) y;
        dx = x - Math.floor(x);
        dy = y - Math.floor(y);

        for (int ii = 0; ii < n; ii++) {
            for (int jj = 0; jj < n; jj++) {
                // Interpolation weight for horizontal interpolation
                co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
                for (int kk = 0; kk < nz; kk++) {
                    double tempKv = dataset.get(kv_field, new double[] { i + ii, j + jj, kk }, time).doubleValue();
                    double tempZ = dataset.z2depth(i + ii, j + jj, kk);
                    if (!Double.isNaN(tempKv)) {
                        output[0][kk] += tempZ * co;
                        output[1][kk] += tempKv * co;
                        CO[kk] += co;
                    }
                }
            }
        }

        for (int kk = 0; kk < nz; kk++) {
            if (CO[kk] != 0) {
                output[0][kk] /= CO[kk];
                output[1][kk] /= CO[kk];
            }
        }

        return output;
    }
}
