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
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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

package org.previmer.ichthyop.dataset;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDatasets;

/**
 *
 * @author pverley
 */
public class Mercator_3D extends AbstractDataset {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Grid dimension
     */
    private int nx, ny, nz;
    /**
     * Origin for grid index
     */
    private int ipo, jpo;
    /**
     * Number of time records in current NetCDF file
     */
    private int nbTimeRecords;
    /**
     * Longitude at rho point.
     */
    private float[] longitude;
    /**
     * Latitude at rho point.
     */
    private float[] latitude;
    /**
     * Mask: water = 1, cost = 0
     */
    private int[][][] maskRho;//, masku, maskv;
    /**
     * Zonal component of the velocity field at current time
     */
    private Array u_tp0;
    /**
     * Zonal component of the velocity field at time t + dt
     */
    private Array u_tp1;
    /**
     * Meridional component of the velocity field at current time
     */
    private Array v_tp0;
    /**
     * Meridional component of the velocity field at time t + dt
     */
    private Array v_tp1;
    /**
     * Vertical component of the velocity field at current time
     */
    private Array w_tp0;
    /**
     * Vertical component of the velocity field at time t + dt
     */
    private Array w_tp1;
    /**
     * Depth at rho point
     */
    private double[][][] gdepT;
    /**
     * Depth at w point. The free surface elevation is disregarded.
     */
    private double[][][] gdepW;
    /**
     * Geographical boundary of the domain
     */
    private double latMin, lonMin, latMax, lonMax;
    /**
     * Maximum depth [meter] of the domain
     */
    private double depthMax;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    private double dt_HyMo;
    /**
     * List on NetCDF input files in which dataset is read.
     */
    //private ArrayList<String> listInputFiles;
    /**
     * Index of the current file read in the {@code listInputFiles}
     */
    private int indexFile;
    /**
     * Time t + dt expressed in seconds
     */
    private double time_tp1;
    /**
     * Current rank in NetCDF dataset
     */
    private int rank;
    /**
     * Time arrow: forward = +1, backward = -1
     */
    private int time_arrow;
    /**
     * Name of the Dimension in NetCDF file
     */
    private String strXDim, strYDim, strZDim, strTimeDim;
    /**
     * Name of the Variable in NetCDF file
     */
    private String strU, strV, strW, strTime;
    /**
     * Name of the Variable in NetCDF file
     */
    private String strLon, strLat, strMask;
    /**
     *
     */
    private double[][][] e3t, e3u, e3v;
    private double[][] e1t, e2t, e1v, e2u;
    private String stre1t, stre2t, stre3t;
    private List<String> listUFiles, listVFiles, listWFiles, listTFiles;
    private NetcdfFile ncU, ncV, ncW, ncT;
    private String file_hgr, file_zgr, file_mask;
    private boolean isGridInfoInOneFile;
    // Whether vertical velocity should be read from NetCDF or calculated from U & V
    private boolean readW;

////////////////////////////
// Definition of the methods
////////////////////////////
    @Override
    public boolean is3D() {
        return true;
    }

    /**
     * Reads time non-dependant fields in NetCDF dataset
     */
    private void readConstantField() throws Exception {

        NetcdfFile nc;
        nc = NetcdfDatasets.openDataset(file_hgr, enhanced(), null);
        getLogger().log(Level.INFO, "read lon, lat & mask from {0}", nc.getLocation());
        longitude = new float[nx];
        latitude = new float[ny];
        Array arrLon = nc.findVariable(strLon).read().reduce();
        Array arrLat = nc.findVariable(strLat).read().reduce();
        Index indexLon = arrLon.getIndex();
        Index indexLat = arrLat.getIndex();

        for (int i = 0; i < nx; i++) {
            longitude[i] = arrLon.getFloat(indexLon.set(ipo + i));
        }

        for (int j = 0; j < ny; j++) {
            latitude[j] = arrLat.getFloat(indexLat.set(jpo + j));
        }

        if (!isGridInfoInOneFile) {
            nc.close();
            nc = NetcdfDatasets.openDataset(file_mask, enhanced(), null);
        }
        maskRho = new int[nz][ny][nx];
        Array arrMask = nc.findVariable(strMask).read().reduce().flip(0);
        Index indexMask = arrMask.getIndex();
        for (int k = 0; k < nz; k++) {
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    maskRho[k][j][i] = arrMask.getInt(indexMask.set(k + 1, j + jpo, i + ipo));
                }
            }
        }

        if (!isGridInfoInOneFile) {
            nc.close();
            nc = NetcdfDatasets.openDataset(file_zgr, enhanced(), null);
        }

        // phv 20150319 - patch for e3t that can be found in NEMO output spread
        // into three variables e3t_0, e3t_ps and mbathy
        e3t = read_e3_field(nc, stre3t);
        e3u = compute_e3u(e3t);
        e3v = compute_e3v(e3t);

        if (!isGridInfoInOneFile) {
            nc.close();
            nc = NetcdfDatasets.openDataset(file_hgr, enhanced(), null);
        }
        //System.out.println("read e1t e2t " + nc.getLocation());
        // fichier *mesh*h*
        e1t = read_e1_e2_field(nc, stre1t);
        e2t = read_e1_e2_field(nc, stre2t);

        e1v = e1t;
        e2u = e2t;

        nc.close();

        //System.out.println("read bathy gdept gdepw e3t " + nc.getLocation());
        //fichier *mesh*z*
        get_gdep_fields(nc);

    }

    private double[][][] compute_e3u(double[][][] e3t) {

        double[][][] e3u_calc = new double[nz][ny][nx];

        for (int k = 0; k < nz; k++) {
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx - 1; i++) {
                    /*
                     * In NEMO domzgr.F90
                     * e3u (ji,jj,jk) = MIN( e3t(ji,jj,jk), e3t(ji+1,jj,jk))
                     */
                    //e3u_calc[k][j][i] = 0.5d * (e3t[k][j][i] + e3t[k][j][i + 1]);
                    e3u_calc[k][j][i] = Math.min(e3t[k][j][i], e3t[k][j][i + 1]);
                }
                e3u_calc[k][j][nx - 1] = e3t[k][j][nx - 1];
            }
        }
        return e3u_calc;
    }

    private double[][][] compute_e3v(double[][][] e3t) {

        double[][][] e3v_calc = new double[nz][ny][nx];

        for (int k = 0; k < nz; k++) {
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny - 1; j++) {
                    /*
                     * In Nemo domzgr.F90
                     * e3v (ji,jj,jk) = MIN( e3t(ji,jj,jk), e3t(ji,jj+1,jk))
                     */
                    //e3v_calc[k][j][i] = 0.5d * (e3t[k][j][i] + e3t[k][j + 1][i]);
                    e3v_calc[k][j][i] = Math.min(e3t[k][j][i], e3t[k][j + 1][i]);
                }
                e3v_calc[k][ny - 1][i] = e3t[k][ny - 1][i];
            }
        }
        return e3v_calc;
    }

    private double[][][] read_e3_field(NetcdfFile nc, String varname) throws InvalidRangeException, IOException {

        Array array = nc.findVariable(varname).read().reduce().flip(0);
        Index index = array.getIndex();
        double[][][] field = new double[nz][ny][nx];

        int[] shape = array.getShape();

        if (shape.length == 1) {

            for (int k = 0; k < nz; k++) {
                index.set(k + 1);
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        field[k][j][i] = Double.isNaN(array.getDouble(index)) ? 0.d : array.getDouble(index);
                    }
                }
            }

        } else {

            for (int k = 0; k < nz; k++) {
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        index.set(k + 1, j + jpo, i + ipo);
                        field[k][j][i] = Double.isNaN(array.getDouble(index)) ? 0.d : array.getDouble(index);
                    }
                }
            }
        }

        return field;
    }

    private void get_gdep_fields(NetcdfFile nc) throws InvalidRangeException, IOException {

        gdepT = new double[nz][ny][nx];
        gdepW = new double[nz + 1][ny][nx];

        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {

                gdepW[nz][j][i] = 0.d;   // init.

                for (int k = nz; k-- > 0;) {

                    gdepW[k][j][i] = gdepW[k + 1][j][i] + e3t[k][j][i];
                    gdepT[k][j][i] = 0.5 * (gdepW[k][j][i] + gdepW[k + 1][j][i]);

                }
            }
        }

        /*
        array = nc.findVariable(str_gdepT).read().reduce().flip(0);
        index = array.getIndex();
        gdepT = new double[nz];
        for (int k = 0; k < nz; k++) {
            index.set(k + 1);
            gdepT[k] = array.getDouble(index);
        }

        gdepW = new double[nz + 1];
        if (null != nc.findVariable(str_gdepW)) {
            getLogger().log(Level.INFO, "Depth of W points is read from NetCDF file");
            // Read gdepw
            array = nc.findVariable(str_gdepW).read().reduce().flip(0);
            index = array.getIndex();
            for (int k = 0; k < nz + 1; k++) {
                index.set(k); // barrier.n
                gdepW[k] = array.getDouble(index);
            }
        } else {
            // Compute gdepw (approximation)
            // Reads the T depth from NetCDF file
            getLogger().log(Level.INFO, "Depth of W points is computed from depth at T points");
            array = nc.findVariable(str_gdepT).read().reduce().flip(0);
            for (int k = 0; k < nz; k++) {
                index.set(k);
                gdepW[k] = array.getDouble(index);
                index.set(k + 1);
                gdepW[k] += array.getDouble(index);
                gdepW[k] *= 0.5;
            }
            gdepW[nz] = 0.;
        }
        */
    }

    private double[][] read_e1_e2_field(NetcdfFile nc, String varname) throws InvalidRangeException, IOException {

        Array array = nc.findVariable(varname).read().reduce();
        double[][] field = new double[ny][nx];
        Index index = array.getIndex();
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                index.set(j + jpo, i + ipo);
                field[j][i] = array.getDouble(index);
            }
        }
        return field;
    }

    /**
     * Advects the particle with the model velocity vector, using a Forwardf
     * Euler scheme. Let's see how it works with the example of the Zonal
     * component.
     * <pre>
     * ROMS and MARS uses an Arakawa C grid.
     * Here is the scheme (2D) of cells (i, j) and (i + 1, j):
     *
     *     +-----V(i, j)-----+---V(i + 1, j)---+
     *     |                 |                 |
     *     |            *X   |                 |
     * U(i - 1, j)  +      U(i, j)    +     U(i + 1, j)
     *     |                 |                 |
     *     |                 |                 |
     *     +---V(i, j - 1)---+-V(i + 1, j - 1)-+
     *
     * Particle current location: X(x, y, z)
     * Let's take i = round(x), j = truncate(y) and k = truncate(z)
     * dx = x - i, dy = y - j, dz = z - k
     * Let's call t, the current time of the simulation, and t0 and t1 the
     * values of the time NetCDF variable bounding t: t0 <= t < t1
     * We first interpolate the model velocity field at t0:
     * U(t0) = U(t0, i, j, k) * |(0.5 - dx) * (1 - dy) * (1 - dz)|
     *       + U(t0, i, j, k + 1) * |(0.5 - dx) * (1 - dy) * dz|
     *       + U(t0, i, j + 1, k) * |(0.5 - dx) * dy * (1 - dz)|
     *       + U(t0, i, j + 1, k + 1) * |(0.5 - dx) * dy * dz|
     *       + U(t0, i + 1, j, k) * |(0.5 + dx) * (1 - dy) * (1 - dz)|
     *       + U(t0, i + 1, j, k + 1) * |(0.5 + dx) * (1 - dy) * dz|
     *       + U(t0, i + 1, j + 1, k) * |(0.5 + dx) * dy * (1 - dz)|
     *       + U(t0, i + 1, j + 1, k + 1) * |(0.5 + dx) * dy * dz|
     *
     * This large expression can be written:
     *
     * U(t0) = U(t0, i + ii, j + jj, k + kk)
     *         * |(0.5 - ii - dx) * (1 - jj - dy) * (1 - kk - dz)|
     *
     * with ii, jj and kk integers varying from zero to one.
     * U is expressed in meter per second and we would like to express the move
     * in grid unit. Therefore it has to be adimensionalized.
     * Let's call Ua the velocity in grid unit per second
     * Let's take dXI(i, j) the length of the cell in the zonal direction.
     *
     * Ua(t0) = U(t0, i + ii, j + jj, k + kk)
     *          / [dXI(i + ii , j + jj) + dXI(i + ii + 1, j + jj)]
     *          * |(0.5 - ii - dx) * (1 - jj - dy) * (1 - kk - dz)|
     *
     * Same with U(t1).
     * Let's take frac = (t - t0) / (t1 - t0)
     * Then Ua(t) = (1 - frac) * Ua(t0) + frac * Ua(t1)
     *
     * x(t + dt) = x(t) + Ua(t) * dt
     * </pre>
     *
     * pverley pour chourdin: ici il faudra revoir les adimensionalisations de u
     * et v par e2u et e1v
     *
     * @param pGrid
     * @param time
     * @return
     */
    @Override
    public double get_dUx(double[] pGrid, double time, boolean normalize) {

        double du = 0.d;
        double kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        int k = (int) kz;

        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double dz = kz - (double) k;
        double CO = 0.d;
        double co, x;
        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

        Index index = u_tp0.getIndex();
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < n; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    co = Math.abs((1.d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    index.set(k + kk, j + jj, i + ii);
                    if (!(Double.isNaN(u_tp0.getDouble(index)))) {
                        x = (1.d - x_euler) * u_tp0.getDouble(index)
                                + x_euler * u_tp1.getDouble(index);
                        if (normalize) {
                            du += x * co / e2u[j + jj][i + ii];
                        } else {
                            du += x * co;
                        }
                    }
                }
            }
        }
        if (CO != 0) {
            du /= CO;
        }
        return du;
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {

        double dw = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (n == 1) ? (int) Math.round(ix) : (int) ix;
        int j = (n == 1) ? (int) Math.round(jy) : (int) jy;
        int k = (int) Math.round(kz);
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double dz = kz - (double) k;
        double CO = 0.d;
        double co, x;
        Index index = w_tp0.getIndex();
        for (int ii = 0; ii < n; ii++) {
            for (int jj = 0; jj < n; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    index.set(k + kk, j + jj, i + ii);
                    // Patch for Eliot (issues 122)
                    // Check whether the value is NaN for the vertical velocity.
                    // If so, we don't read it.
                    if (!Double.isNaN(w_tp0.getDouble(index))) {
                        co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (.5d - (double) kk - dz));
                        CO += co;
                        x = (1.d - x_euler) * w_tp0.getDouble(index) + x_euler * w_tp1.getDouble(index);
                        dw += 2.d * x * co / (gdepW[Math.max(k + kk - 1, 0)][j + jj][i + ii]
                                - gdepW[Math.min(k + kk + 1, nz)][j + jj][i + ii]);
                    }
                }
            }
        }
        if (CO != 0) {
            dw /= CO;
        }

        /*double dwr = get_dWrz(pGrid, time);
         float err = (float) Math.abs((dwr - dw) / dwr);
         System.out.println("dw: " + dw + " - dwr: " + dwr + " - err: " + err);*/
        return dw;
    }

    @Override
    public double get_dVy(double[] pGrid, double time, boolean normalize) {

        double dv = 0.d;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double CO = 0.d;
        double x;
        double co;

        double kz;
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        int k = (int) kz;

        double dz = kz - (double) k;
        Index index = v_tp0.getIndex();
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < 2; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    co = Math.abs((1.d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    index.set(k + kk, j + jj, i + ii);
                    if (!Double.isNaN(v_tp0.getDouble(index))) {
                        x = (1.d - x_euler) * v_tp0.getDouble(index)
                                + x_euler * v_tp1.getDouble(index);
                        if (normalize) {
                            dv += x * co / e1v[j + jj][i + ii];
                        } else {
                            dv += x * co;
                        }
                    }
                }
            }
        }
        if (CO != 0) {
            dv /= CO;
        }
        return dv;
    }

    /**
     * Computes the vertical velocity vector.
     * <pre>
     * Drill:
     *
     *     +-----V(i, j, k)-----+
     *     |                    |
     *     |                    |
     * U(i - 1, j, k)  +      U(i, j, k)
     *     |                    |
     *     |                    |
     *     +---V(i, j - 1, k)---+
     *
     * Lets call Su(i, j, k) the grid face for which U(i, j, k) is the normal.
     * Same definition for Sv(i, j, k) and Sw(i, j, k)
     * Mass conservation means that:
     *   U(i, j, k) * Su(i, j, k) - U(i - 1, j, k) * Su(i - 1, j, k)
     * + V(i, j, k) * Sv(i, j, k) - V(i, j - 1, k) * Sv(i, j - 1, k)
     * + W(i, j, k) * Sw(i, j, k) - W(i, j, k - 1) * Sw(i, j, k - 1)
     * = 0
     * Bottom conditions: W(i, j, 0) = 0
     * Reading U and V in the NetCDF dataset, we can therefore compute the
     * vertical flux W(i, j, k) * Sw(i, j, k) from bottom to surface and
     * then the vertical velocity.
     * This last result is the abolute vertical velocity. Nonetheless, ROMS and
     * MARS use a sigma level vertical grid, that dilates and contracts function
     * of the free surface elevation. So we must substract to the vertical
     * velocity the grid vertical velocity.
     *
     * Let's take Zw(i, j, k) the depth at w-point and let's call Wgrid the
     * vertical velocity of the grid at the sigma levels.
     * Wgrid(i, j, k) = W(i, j, N)
     *                  * [Zw(i, j, k) - Zw(i, j, 0)]
     *                  / [Zw(i, j, N) - Zw(i, j, 0)];
     *
     * So the water vertical velocity at sigma levels is
     * Ws(i, j, k) = W(i, j, k) - Wgrid(i, j, k)
     * </pre>
     */
    private Array computeW() {

        Index index, indexbis;

        double[][][] Huon = new double[nz][ny][nx];
        double[][][] Hvom = new double[nz][ny][nx];

        // Array containing the interpolated speed on the eastern/western faces.
        double[][][] u_east = new double[nz][ny][nx];
        double[][][] v_north = new double[nz][ny][nx];

        index = u_tp1.getIndex();
        indexbis = u_tp1.getIndex();

        for (int k = 0; k < nz; k++) {
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx - 1; i++) {
                    index.set(k, j, i);
                    indexbis.set(k, j, i + 1);
                    // If umask = 0, interpolated speed = 0.
                    // else, ueast = mean over T points
                    u_east[k][j][i] = (Double.isNaN(u_tp1.getDouble(index)) || Double.isNaN(u_tp1.getDouble(indexbis)))
                            ? 0.d : 0.5 * (u_tp1.getDouble(index) + u_tp1.getDouble(indexbis));
                }
            }
        }

        index = v_tp1.getIndex();
        indexbis = v_tp1.getIndex();

        for (int k = 0; k < nz; k++) {
            for (int j = 0; j < ny - 1; j++) {
                for (int i = 0; i < nx; i++) {
                    index.set(k, j, i);
                    indexbis.set(k, j + 1, i);
                    // If umask = 0, interpolated speed = 0.
                    // else, ueast = mean over T points
                    v_north[k][j][i] = (Double.isNaN(v_tp1.getDouble(index)) || Double.isNaN(v_tp1.getDouble(indexbis)))
                            ? 0.d : 0.5 * (v_tp1.getDouble(index) + v_tp1.getDouble(indexbis));

                }
            }
        }

        //---------------------------------------------------
        // Calculation Coeff Huon & Hvom
        for (int k = nz; k-- > 0;) {
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny; j++) {
                    Huon[k][j][i] = u_east[k][j][i] * e2u[j][i] * e3u[k][j][i];
                }
            }
        }

        for (int k = nz; k-- > 0;) {
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny - 1; j++) {
                    Hvom[k][j][i] = v_north[k][j][i] * e1v[j][i] * e3v[k][j][i];
                }
            }
        }

        //---------------------------------------------------
        // Calcultaion of w(i, j, k)
        double[][][] w_double = new double[nz + 1][ny][nx];

        for (int j = 1; j < ny; j++) {
            for (int i = 1; i < nx; i++) {
                /*
                 * pverley 15/02/2011
                 * I must start integrating the vertical velocity at the bottom
                 * of the water column wich is not necessarily k = 0;
                 * So first I look for k0 such as (k0 - 1) is bottom, k0 is first
                 * cell of the column in water.
                 */
                int k0 = 0;

                /*
                 * pverley 15/02/2011
                 * Ensured that w(0:k0, :, :) = 0;
                 */
                for (int k = 0; k < k0 + 1; k++) {
                    w_double[k][j][i] = 0.d;
                    //System.out.println("k: " + k + " k0: " + k0 + " wr: " + wr_tp1[k][j][i] + " " + isInWater(k, j, i));
                }
                for (int k = k0 + 1; k < nz; k++) {
                    w_double[k][j][i] = w_double[k - 1][j][i]
                            - (Huon[k - 1][j][i] - Huon[k - 1][j][i - 1] + Hvom[k - 1][j][i] - Hvom[k - 1][j - 1][i]);
                }
                w_double[nz][j][i] = 0.d;
            }
        }
        //---------------------------------------------------
        // Boundary Conditions
        for (int k = nz + 1; k-- > 0;) {
            for (int j = ny; j-- > 0;) {
                w_double[k][j][0] = w_double[k][j][1];
                w_double[k][j][nx - 1] = w_double[k][j][nx - 2];
            }
        }
        for (int k = nz + 1; k-- > 0;) {
            for (int i = nx; i-- > 0;) {
                w_double[k][0][i] = w_double[k][1][i];
                w_double[k][ny - 1][i] = w_double[k][ny - 2][i];
            }
        }

        //---------------------------------------------------
        // w * dxu * dyv
        //float[][][] w = new float[nz + 1][ny][nx];
        Array w = new ArrayDouble.D3(nz + 1, ny, nx);
        index = w.getIndex();
        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                for (int k = nz + 1; k-- > 0;) {
                    w.setDouble(index.set(k, j, i), (w_double[k][j][i] / (e1t[j][i] * e2t[j][i])));
                }
            }
        }

        //---------------------------------------------------
        // Return w
        return w;
    }

    /*
     * Adimensionalizes the given magnitude at the specified grid location.
     */
    public double adimensionalize(double number, double xRho, double yRho) {

        int i = (int) Math.round(xRho);
        int j = (int) Math.round(yRho);
        return 2.d * number / (e1t[j][i] + e2t[j][i]);
    }

    /**
     * Reads longitude and latitude fields in NetCDF dataset
     *
     * pverley pour chourdin: même remarque que chaque fois. Les infos dans OPA
     * se trouvent dans différents fichiers, donc selon la méthode appelée, je
     * dois recharger le fichier NetCDF correspondant. Au lieu d'utiliser la
     * variable ncIn globale.
     */
    private void readLonLat() throws IOException {

        try (NetcdfFile nc = NetcdfDatasets.openDataset(file_hgr, enhanced(), null)) {
            longitude = new float[nx];
            latitude = new float[ny];
            Array arrLon = nc.findVariable(strLon).read().reduce();
            Array arrLat = nc.findVariable(strLat).read().reduce();
            Index indexLon = arrLon.getIndex();
            Index indexLat = arrLat.getIndex();

            for (int i = 0; i < nx; i++) {
                longitude[i] = arrLon.getFloat(indexLon.set(ipo + i));
            }

            for (int j = 0; j < ny; j++) {
                latitude[j] = arrLat.getFloat(indexLat.set(jpo + j));
            }

        }
    }

    /*
     * Gets cell dimension [meter] in the XI-direction.
     *
     * pverley pour chourdin: vérifier avec Steph que je ne me trompe pas dans
     * la définition de e1t et e2t
     */
    @Override
    public double getdxi(int j, int i) {
        return e1t[j][i];
    }

    /*
     * Gets cell dimension [meter] in the ETA-direction.
     */
    @Override
    public double getdeta(int j, int i) {
        return e2t[j][i];
    }

    /*
     * Sets up the {@code Dataset}. The method first sets the appropriate
     * variable names, loads the first NetCDF dataset and extract the time
     * non-dependant information, such as grid dimensions, geographical
     * boundaries, depth at sigma levels.
     *
     * @throws an IOException if an error occurs while setting up the
     * {@code Dataset}
     */
    @Override
    public void setUp() throws Exception {

        loadParameters();
        clearRequiredVariables();
        sortInputFiles();
        getDimNC();
        shrinkGrid();
        readConstantField();
        getDimGeogArea();
    }

    public void shrinkGrid() {

        if (findParameter("shrink_domain") && Boolean.valueOf(getParameter("shrink_domain"))) {
            try {
                float lon1 = Float.valueOf(LonLatConverter.convert(getParameter("north-west-corner.lon"), LonLatFormat.DecimalDeg));
                float lat1 = Float.valueOf(LonLatConverter.convert(getParameter("north-west-corner.lat"), LonLatFormat.DecimalDeg));
                float lon2 = Float.valueOf(LonLatConverter.convert(getParameter("south-east-corner.lon"), LonLatFormat.DecimalDeg));
                float lat2 = Float.valueOf(LonLatConverter.convert(getParameter("south-east-corner.lat"), LonLatFormat.DecimalDeg));
                range(lat1, lon1, lat2, lon2);
            } catch (IOException | NumberFormatException ex) {
                getLogger().log(Level.WARNING, "Failed to resize domain. " + ex.toString(), ex);
            }
        }
    }

    /*
     * Gets the names of the NetCDF variables from the configuration file.
     *
     * pverley pour chourdin : "Configuration" c'est la classe qui lit le
     * fichier de configuration. Tu vois que normallement, le nom des variables
     * est lu dans le fichier cfg. Temporairement je courcircuite cette
     * opération et je renseigne à la main le nom des variables des sorties OPA.
     */
    @Override
    public void loadParameters() {

        strXDim = getParameter("field_dim_x");
        strYDim = getParameter("field_dim_y");
        strZDim = getParameter("field_dim_z");
        strTimeDim = getParameter("field_dim_time");
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strMask = getParameter("field_var_mask");
        strU = getParameter("field_var_u");
        strV = getParameter("field_var_v");
        if (findParameter("read_var_w")) {
            readW = Boolean.valueOf(getParameter("read_var_w"));
        } else {
            readW = false;
            getLogger().warning("Ichthyop will recalculate W variable from U and V");
        }
        if (readW) {
            strW = getParameter("field_var_w");
        }
        strTime = getParameter("field_var_time");
        stre3t = getParameter("field_var_e3t");
        stre1t = getParameter("field_var_e1t");
        stre2t = getParameter("field_var_e2t");
        if (!findParameter("enhanced_mode")) {
            getLogger().warning("Ichthyop assumes that by default the NEMO NetCDF files must be opened in enhanced mode (with scale, offset and missing attributes).");
        }
        time_arrow = timeArrow();
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     *
     * @throws an IOException if an error occurs while reading the dimensions.
     *
     * pverley pour chourdin: Pour ROMS ou MARS je lisais les dimensions à
     * partir de la variable ncIn qui est le premier fichier sortie qui me tombe
     * sous la main. Avec OPA, les dimensions se lisent dans un fichier
     * particulier *byte*mask*. A déterminer si toujours vrai ?
     */
    private void getDimNC() throws IOException {

        NetcdfFile nc = NetcdfDatasets.openDataset(file_mask, enhanced(), null);
        try {
            nx = nc.findDimension(strXDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset X dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            ny = nc.findDimension(strYDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset Y dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            nz = nc.findDimension(strZDim).getLength() - 1;
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset Z dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        ipo = jpo = 0;
    }

    /**
     * Resizes the domain and determines the range of the grid indexes taht will
     * be used in the simulation. The new domain is limited by the Northwest and
     * the Southeast corners.
     *
     * @param pGeog1 a float[], the geodesic coordinates of the domain Northwest
     * corner
     * @param pGeog2 a float[], the geodesic coordinates of the domain Southeast
     * corner
     * @throws an IOException if the new domain is not strictly nested within
     * the NetCDF dataset domain.
     */
    private void range(double lat1, double lon1, double lat2, double lon2) throws IOException {

        double[] pGrid1, pGrid2;
        int ipn, jpn;

        readLonLat();

        pGrid1 = latlon2xy(lat1, lon1);
        pGrid2 = latlon2xy(lat2, lon2);
        if (pGrid1[0] < 0 || pGrid2[0] < 0) {
            throw new IOException(
                    "Impossible to proportion the simulation area : points out of domain");
        }

        longitude = null;
        latitude = null;

        //System.out.println((float)pGrid1[0] + " " + (float)pGrid1[1] + " " + (float)pGrid2[0] + " " + (float)pGrid2[1]);
        ipo = (int) Math.min(Math.floor(pGrid1[0]), Math.floor(pGrid2[0]));
        ipn = (int) Math.max(Math.ceil(pGrid1[0]), Math.ceil(pGrid2[0]));
        jpo = (int) Math.min(Math.floor(pGrid1[1]), Math.floor(pGrid2[1]));
        jpn = (int) Math.max(Math.ceil(pGrid1[1]), Math.ceil(pGrid2[1]));

        nx = Math.min(nx, ipn - ipo + 1);
        ny = Math.min(ny, jpn - jpo + 1);
        //System.out.println("ipo " + ipo + " nx " + nx + " jpo " + jpo + " ny " + ny);
    }

    /**
     * Determines the geographical boundaries of the domain in longitude,
     * latitude and depth.
     */
    private void getDimGeogArea() {

        /* NEMO way.
        //--------------------------------------
        // Calculate the Physical Space extrema
        lonMin = Double.MAX_VALUE;
        lonMax = -lonMin;
        latMin = Double.MAX_VALUE;
        latMax = -latMin;
        depthMax = 0.d;
        int i = nx;

        while (i-- > 0) {
            int j = ny;
            while (j-- > 0) {
                if (lonRho[j][i] >= lonMax) {
                    lonMax = lonRho[j][i];
                }
                if (lonRho[j][i] <= lonMin) {
                    lonMin = lonRho[j][i];
                }
                if (latRho[j][i] >= latMax) {
                    latMax = latRho[j][i];
                }
                if (latRho[j][i] <= latMin) {
                    latMin = latRho[j][i];
                }
                double depth = getBathy(i, j);
                if (depth > depthMax) {
                    depthMax = depth;
                }
            }
        }

        double double_tmp;
        if (lonMin > lonMax) {
            double_tmp = lonMin;
            lonMin = lonMax;
            lonMax = double_tmp;
        }

        if (latMin > latMax) {
            double_tmp = latMin;
            latMin = latMax;
            latMax = double_tmp;
        }
         */
        //--------------------------------------
        // Calculate the Physical Space extrema
        lonMin = Double.MAX_VALUE;
        lonMax = -lonMin;
        latMin = Double.MAX_VALUE;
        latMax = -latMin;

                depthMax = 0.d;

        int i = nx;
        while (i-- > 0) {
            if (longitude[i] >= lonMax) {
                lonMax = longitude[i];
            }
            if (longitude[i] <= lonMin) {
                lonMin = longitude[i];
            }
        }
        int j = ny;
        while (j-- > 0) {
            if (latitude[j] >= latMax) {
                latMax = latitude[j];
            }
            if (latitude[j] <= latMin) {
                latMin = latitude[j];
            }
        }

        //System.out.println("lonmin " + lonMin + " lonmax " + lonMax + " latmin " + latMin + " latmax " + latMax);
        double double_tmp;
        if (lonMin > lonMax) {
            double_tmp = lonMin;
            lonMin = lonMax;
            lonMax = double_tmp;
        }

        if (latMin > latMax) {
            double_tmp = latMin;
            latMin = latMax;
            latMax = double_tmp;
        }

        for (j = 0; j < ny; j++) {
            for (i = 0; i < nx; i++) {
                double depth = getBathy(i, j);
                if (depth > depthMax) {
                    depthMax = depth;
                }
            }
        }
    }

    /*
     * Initializes the {@code Dataset}. Opens the file holding the first time of
     * the simulation. Checks out the existence of the fields required by the
     * current simulation. Sets all fields at time for the first time step.
     *
     * @throws an IOException if a required field cannot be found in the NetCDF
     * dataset.
     */
    @Override
    public void init() throws Exception {

        double t0 = getSimulationManager().getTimeManager().get_tO();
        open(indexFile = DatasetUtil.index(listUFiles, t0, time_arrow, strTime));
        checkRequiredVariable(ncT);
        setAllFieldsTp1AtTime(rank = DatasetUtil.rank(t0, ncU, strTime, time_arrow));
        time_tp1 = t0;
    }

    /**
     * Reads time dependant variables in NetCDF dataset at specified rank.
     *
     * @param rank an int, the rank of the time dimension in the NetCDF dataset.
     * @throws an IOException if an error occurs while reading the variables.
     *
     * pverley pour chourdin: la aussi je fais du provisoire en attendant de
     * voir si on peut dégager une structure systématique des input.
     */
    void setAllFieldsTp1AtTime(int rank) throws Exception {

        getLogger().info("Reading NetCDF variables...");

        int[] origin = new int[]{rank, 0, jpo, ipo};
        double time_tp0 = time_tp1;

        try {
            u_tp1 = ncU.findVariable(strU).read(origin, new int[]{1, nz, ny, nx}).flip(1).reduce();
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading U velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            v_tp1 = ncV.findVariable(strV).read(origin, new int[]{1, nz, ny, nx}).flip(1).reduce();
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading V velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            time_tp1 = DatasetUtil.timeAtRank(ncU, strTime, rank);
        } catch (IOException ex) {
            IOException ioex = new IOException("Error reading time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
        for (RequiredVariable variable : requiredVariables.values()) {
            variable.nextStep(readVariable(ncT, variable.getName(), rank), time_tp1, dt_HyMo);
        }
        if (readW) {
            try {
                w_tp1 = ncW.findVariable(strW).read(origin, new int[]{1, nz + 1, ny, nx}).flip(1).reduce();
            } catch (IOException | InvalidRangeException ex) {
                IOException ioex = new IOException("Error reading W variable. " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
        } else {
            w_tp1 = computeW();
        }
    }

    /**
     * Determines whether or not the specified grid cell(i, j) is in water.
     *
     * @param i an int, i-coordinate of the cell
     * @param j an intn the j-coordinate of the cell
     * @return <code>true</code> if cell(i, j) is in water, <code>false</code>
     * otherwise.
     */
    private boolean isInWater(int i, int j, int k) {
        //System.out.println(i + " " + j + " " + k + " - "  + (maskRho[k][j][i] > 0));
        try {
            return (maskRho[k][j][i] > 0);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    @Override
    public boolean isInWater(int i, int j) {
        return isInWater(i, j, nz - 1);
    }

    /*
     * Determines whether the specified {@code RohPoint} is in water.
     *
     * @param ptRho the RhoPoint
     * @return <code>true</code> if the {@code RohPoint} is in water,
     * <code>false</code> otherwise.
     * @see #isInWater(int i, int j)
     */
    @Override
    public boolean isInWater(double[] pGrid) {
        if (pGrid.length > 2) {
            return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]), (int) Math.round(pGrid[2]));
        } else {
            return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
        }
    }

    /**
     * Determines whether or not the specified grid point is close to cost line.
     * The method first determines in which quater of the cell the grid point is
     * located, and then checks wether or not its cell and the three adjacent
     * cells to the quater are in water.
     *
     * @param pGrid a double[] the coordinates of the grid point
     * @return <code>true</code> if the grid point is close to cost,
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isCloseToCost(double[] pGrid) {

        int i, j, k, ii, jj;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        k = (int) (Math.round(pGrid[2]));
        ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
        jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
        return !(isInWater(i + ii, j, k) && isInWater(i + ii, j + jj, k) && isInWater(i, j + jj, k));

    }

    /*
     * Transforms the depth at specified x-y particle location into z coordinate
     *
     * @param xRho a double, the x-coordinate
     * @param yRho a double, the y-coordinate
     * @param depth a double, the depth of the particle
     * @return a double, the z-coordinate corresponding to the depth
     *
     * pverley pour chourdin: méthode à tester.
     */
    @Override
    public double depth2z(double x, double y, double depth) {

        if (depth > 0) {
            depth = 0.d;
        }
        depth = Math.abs(depth);

        int i = (int) Math.floor(x);
        int j = (int) Math.floor(y);

        //-----------------------------------------------
        // Return z[grid] corresponding to depth[meters]
        double zRho = 0.d;


        /* case particle is going straight up to surface, due to strong
         * buoyancy for instance.
         */
        if (depth < gdepT[nz - 1][j][i]) {
            //System.out.println("depth: " + depth + " ==> z: " + (nz - 1) + " gdepT[nz - 1]: " + gdepT[nz - 1]);
            return (nz - 1);
        }
        for (int k = nz - 1; k > 0; k--) {
            //System.out.println("t1 " + z_w[k] + " " + (float)depth + " " + z_rho[k]);
            if (depth <= gdepW[k][j][i] && depth > gdepT[k][j][i]) {
                zRho = k + 0.d - 0.5d * Math.abs((gdepT[k][j][i] - depth) / (gdepT[k][j][i] - gdepW[k][j][i]));
                //System.out.println("depth: " + depth + " ==> z: " + zRho + " - k: " + k + " gdepW[k]: " + gdepW[k] + " gdepT[k]: " + gdepT[k]);
                return zRho;
            }
            //System.out.println("t2 " + z_rho[k] + " " + (float)depth + " " + z_w[k + 1]);
            if (depth <= gdepT[k][j][i] && depth > gdepW[k + 1][j][i]) {
                zRho = k + 0.d
                        + 0.5d
                        * Math.abs((gdepT[k][j][i] - depth)
                                / (gdepW[k + 1][j][i] - gdepT[k][j][i]));
                //System.out.println("depth: " + depth + " ==> z: " + zRho + " - k: " + k + " gdepW[k + 1]: " + gdepW[k + 1] + " gdepT[k]: " + gdepT[k]);
                return zRho;
            }
        }
        //System.out.println("depth: " + depth + " ==> z: " + zRho);
        return zRho;
    }

    /*
     * Transdorms the specified z-location into depth
     *
     * pverley pour chourdin: j'ai testé z2depth et depth2z, ça à l'air de
     * marcher mais il faudra faire une validation plus sérieuse.
     *
     * @param x double
     * @param y double
     * @param z double
     * @return double
     */
    @Override
    public double z2depth(double x, double y, double z) {

        double depth;
        double dz;

        double kz = Math.max(0.d, Math.min(z, (double) nz - 1.00001f));
        int k = (int) Math.round(kz);
        dz = z - k;

        int i = (int) Math.floor(x);
        int j = (int) Math.floor(y);

        if (dz < 0) { // >= ?
            depth = gdepT[k][j][i]
                    + 2 * Math.abs(dz * (gdepT[k][j][i] - gdepW[k][j][i]));
            //System.out.println("z: " + z + " => depth: " + depth + " k: " + k + " gdepT[k]: " + gdepT[k] + " gdepW[k]: " + gdepW[k]) ;
        } else {
            depth = gdepT[k][j][i]
                    - 2 * Math.abs(dz * (gdepT[k][j][i] - gdepW[k + 1][j][i]));
            //System.out.println("z: " + z + " => depth: " + depth + " k: " + k + " gdepT[k]: " + gdepT[k] + " gdepW[k + 1]: " + gdepW[k + 1]);
        }
        return -depth;
    }

    /*
     * * Transforms the specified 2D grid coordinates into geographical
     * coordinates. It merely does a bilinear spatial interpolation of the
     * surrounding grid nods geographical coordinates.
     *
     * @param xRho a double, the x-coordinate
     * @param yRho a double, the y-coordinate
     * @return a double[], the corresponding geographical coordinates (latitude,
     * longitude)
     *
     * @param xRho double
     * @param yRho double
     * @return double[]
     */
    @Override
    public double[] xy2latlon(double xRho, double yRho) {

        /* NEMO way
        //--------------------------------------------------------------------
        // Computational space (x, y , z) => Physical space (lat, lon, depth)
        final double ix = Math.max(0.00001f,
                Math.min(xRho, (double) nx - 1.00001f));
        final double jy = Math.max(0.00001f,
                Math.min(yRho, (double) ny - 1.00001f));

        final int i = (int) Math.floor(ix);
        final int j = (int) Math.floor(jy);
        double latitude = 0.d;
        double longitude = 0.d;
        final double dx = ix - (double) i;
        final double dy = jy - (double) j;
        double co;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                latitude += co * latRho[j + jj][i + ii];
                longitude += co * lonRho[j + jj][i + ii];
            }
        }
        return (new double[]{latitude, longitude});
         */
        //--------------------------------------------------------------------
        // Computational space (x, y , z) => Physical space (lat, lon, depth)
        final double jy = Math.max(0.00001f,
                Math.min(yRho, (double) ny - 1.00001f));

        final int i = (int) Math.floor(xRho);
        final int j = (int) Math.floor(jy);
        double lat = 0.d;
        double lon = 0.d;
        final double dx = xRho - (double) i;
        final double dy = jy - (double) j;
        double co;
        for (int ii = 0; ii < 2; ii++) {
            int ci = i;
            if (i < 0) {
                ci = nx - 1;
            }
            int cii = i + ii;
            if (cii > nx - 1) {
                cii = 0;
            }
            if (cii < 0) {
                cii = nx - 1;
            }
            for (int jj = 0; jj < 2; jj++) {
                co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                lat += co * latitude[j + jj];
                if (Math.abs(longitude[cii] - longitude[ci]) < 180) {
                    lon += co * longitude[cii];
                } else {
                    double dlon = Math.abs(360.d - Math.abs(longitude[cii] - longitude[ci]));
                    if (longitude[ci] < 0) {
                        lon += co * (longitude[ci] - dlon);
                    } else {
                        lon += co * (longitude[ci] + dlon);
                    }
                }
            }
        }

        return (new double[]{lat, lon});
    }

    /**
     * Transforms the specified 2D geographical coordinates into a grid
     * coordinates.
     *
     * The algorithme has been adapted from a function in ROMS/UCLA code,
     * originally written by Alexander F. Shchepetkin and Hernan G. Arango.
     * Please find below an extract of the ROMS/UCLA documention.
     *
     * <pre>
     *  Checks the position to find if it falls inside the whole domain.
     *  Once it is established that it is inside, find the exact cell to which
     *  it belongs by successively dividing the domain by a half (binary
     *  search).
     * </pre>
     *
     * @param lon a double, the longitude of the geographical point
     * @param lat a double, the latitude of the geographical point
     * @return a double[], the corresponding grid coordinates (x, y)
     * @see #isInsidePolygone
     */
    @Override
    public double[] latlon2xy(double lat, double lon) {

        /* NEMO way
        //--------------------------------------------------------------------
        // Physical space (lat, lon) => Computational space (x, y)
        boolean found;
        int imin, imax, jmin, jmax, i0, j0;
        double dx1, dy1, dx2, dy2, c1, c2, deltax, deltay, xgrid, ygrid;

        xgrid = -1.;
        ygrid = -1.;
        found = isInsidePolygone(0, nx - 1, 0, ny - 1, lon, lat);

        //-------------------------------------------
        // Research surrounding grid-points
        if (found) {
            imin = 0;
            imax = nx - 1;
            jmin = 0;
            jmax = ny - 1;
            while (((imax - imin) > 1) | ((jmax - jmin) > 1)) {
                if ((imax - imin) > 1) {
                    i0 = (imin + imax) / 2;
                    found = isInsidePolygone(imin, i0, jmin, jmax, lon, lat);
                    if (found) {
                        imax = i0;
                    } else {
                        imin = i0;
                    }
                }
                if ((jmax - jmin) > 1) {
                    j0 = (jmax + jmin) / 2;
                    found = isInsidePolygone(imin, imax, jmin, j0, lon, lat);
                    if (found) {
                        jmax = j0;
                    } else {
                        jmin = j0;
                    }
                }
            }

            //--------------------------------------------
            // Trilinear interpolation
            dy1 = latRho[jmin + 1][imin] - latRho[jmin][imin];
            dx1 = lonRho[jmin + 1][imin] - lonRho[jmin][imin];
            dy2 = latRho[jmin][imin + 1] - latRho[jmin][imin];
            dx2 = lonRho[jmin][imin + 1] - lonRho[jmin][imin];

            c1 = lon * dy1 - lat * dx1;
            c2 = lonRho[jmin][imin] * dy2 - latRho[jmin][imin] * dx2;
            deltax = (c1 * dx2 - c2 * dx1) / (dx2 * dy1 - dy2 * dx1);
            deltax = (deltax - lonRho[jmin][imin]) / dx2;
            xgrid = (double) imin + Math.min(Math.max(0.d, deltax), 1.d);

            c1 = lonRho[jmin][imin] * dy1 - latRho[jmin][imin] * dx1;
            c2 = lon * dy2 - lat * dx2;
            deltay = (c1 * dy2 - c2 * dy1) / (dx2 * dy1 - dy2 * dx1);
            deltay = (deltay - latRho[jmin][imin]) / dy1;
            ygrid = (double) jmin + Math.min(Math.max(0.d, deltay), 1.d);
        }
        return (new double[]{xgrid, ygrid});
         */
        //--------------------------------------------------------------------
        // Physical space (lat, lon) => Computational space (x, y)
        boolean found1 = false;
        boolean found2 = false;

        int ci = nx / 2;
        int cj = ny / 2;
        int di = ci / 2;
        int dj = cj / 2;

        // Find the closet grid point to {lat, lon}
        while (!(found1 && found2)) {
            int i = ci;
            int j = cj;
            double dmin = DatasetUtil.geodesicDistance(lat, lon, latitude[j], longitude[i]);
            for (int ii = -di; ii <= di; ii += di) {
                if ((i + ii >= 0) && (i + ii < nx)) {
                    double d = DatasetUtil.geodesicDistance(lat, lon, latitude[j], longitude[i + ii]);
                    if (d < dmin) {
                        dmin = d;
                        ci = i + ii;
                        cj = j;
                    }
                }
            }
            for (int jj = -dj; jj <= dj; jj += dj) {
                if ((j + jj >= 0) && (j + jj < ny)) {
                    double d = DatasetUtil.geodesicDistance(lat, lon, latitude[j + jj], longitude[i]);
                    if (d < dmin) {
                        dmin = d;
                        ci = i;
                        cj = j + jj;
                    }
                }
            }
            if (i == ci && j == cj) {
                found1 = true;
                if (dj == 1 && di == 1) {
                    found2 = true;
                } else {
                    di = (int) Math.max(1, di / 2);
                    dj = (int) Math.max(1, di / 2);
                    found1 = false;
                }
            }
        }

        // Refine within cell (ci, cj) by linear interpolation
        int cip1 = ci + 1 > nx - 1 ? 0 : ci + 1;
        int cim1 = ci - 1 < 0 ? nx - 1 : ci - 1;
        int cjp1 = cj + 1 > ny - 1 ? ny - 1 : cj + 1;
        int cjm1 = cj - 1 < 0 ? 0 : cj - 1;
        // xgrid
        double xgrid;
        if (lon >= longitude[ci]) {
            double dx = (Math.abs(longitude[cip1] - longitude[ci]) > 180.d)
                    ? 360.d + (longitude[cip1] - longitude[ci])
                    : longitude[cip1] - longitude[ci];
            double deltax = (lon - longitude[ci]) / dx;
            xgrid = xTore(ci + deltax);
        } else {
            double dx = (Math.abs(longitude[ci] - longitude[cim1]) > 180.d)
                    ? 360.d + (longitude[ci] - longitude[cim1])
                    : longitude[ci] - longitude[cim1];
            double deltax = (lon - longitude[cim1]) / dx;
            xgrid = xTore(cim1 + deltax);
        }
        // ygrid
        double ygrid;
        if (lat >= latitude[cj]) {
            double dy = latitude[cjp1] - latitude[cj];
            double deltay = (lat - latitude[cj]) / dy;
            ygrid = (double) cj + deltay;
        } else {
            double dy = latitude[cj] - latitude[cjm1];
            double deltay = (lat - latitude[cjm1]) / dy;
            ygrid = (double) cjm1 + deltay;
        }

        return (new double[]{xgrid, ygrid});

    }

    /**
     * Sort OPA input files. First make sure that there is at least and only one
     * file matching the hgr, zgr and byte mask patterns. Then list the gridU,
     * gridV and gridT files.
     *
     * @param path
     * @throws java.io.IOException
     */
    private void sortInputFiles() throws IOException {

        String path = IOTools.resolvePath(getParameter("input_path"));
        File file = new File(path);

        file_mask = checkExistenceAndUnicity(file, getParameter("byte_mask_pattern"));
        file_hgr = checkExistenceAndUnicity(file, getParameter("hgr_pattern"));
        file_zgr = checkExistenceAndUnicity(file, getParameter("zgr_pattern"));

        isGridInfoInOneFile = (new File(file_mask).equals(new File(file_hgr)))
                && (new File(file_mask).equals(new File(file_zgr)));

        listUFiles = DatasetUtil.list(path, getParameter("gridu_pattern"));
        if (listUFiles.isEmpty()) {
            throw new IOException("{Dataset} " + path + " contains no file matching pattern " + getParameter("gridu_pattern"));
        }
        listVFiles = DatasetUtil.list(getParameter("input_path"), getParameter("gridv_pattern"));
        if (listVFiles.isEmpty()) {
            throw new IOException("{Dataset} " + path + " contains no file matching pattern " + getParameter("gridv_pattern"));
        }
        listTFiles = DatasetUtil.list(getParameter("input_path"), getParameter("gridt_pattern"));
        if (readW) {
            listWFiles = DatasetUtil.list(getParameter("input_path"), getParameter("gridw_pattern"));
            if (listWFiles.isEmpty()) {
                throw new IOException("{Dataset} " + path + " contains no file matching pattern " + getParameter("gridw_pattern"));
            }
        }
        if (!skipSorting()) {
            DatasetUtil.sort(listUFiles, strTime, time_arrow);
            DatasetUtil.sort(listVFiles, strTime, time_arrow);
            DatasetUtil.sort(listTFiles, strTime, time_arrow);
            if (readW) {
                DatasetUtil.sort(listWFiles, strTime, time_arrow);
            }
        }
    }

    private String checkExistenceAndUnicity(File file, String pattern) throws IOException {

        File[] listFiles = file.listFiles(new MetaFilenameFilter(pattern));
        int nbFiles = listFiles.length;

        if (nbFiles == 0) {
            throw new IOException("No file matching pattern " + pattern);
        } else if (nbFiles > 1) {
            throw new IOException("More than one file matching pattern " + pattern);
        }

        return listFiles[0].toString();
    }

    /**
     * Loads the NetCDF dataset from the specified filename.
     *
     * @param filename a String that can be a local pathname or an OPeNDAP URL.
     * @throws IOException
     */
    private void open(int index) throws IOException {

        getLogger().info("Opening NEMO dataset");
        if (ncU != null) {
            ncU.close();
        }
        ncU = DatasetUtil.openFile(listUFiles.get(index), enhanced());
        if (ncV != null) {
            ncV.close();
        }
        if (listUFiles.get(index).equals(listVFiles.get(index))) {
            ncV = ncU;
        } else {
            ncV = DatasetUtil.openFile(listVFiles.get(index), enhanced());
        }
        if (readW) {
            if (listUFiles.get(index).equals(listWFiles.get(index))) {
                ncW = ncU;
            } else {
                ncW = DatasetUtil.openFile(listWFiles.get(index), enhanced());
            }
        }
        if (ncT != null) {
            ncT.close();
        }
        if (!listTFiles.isEmpty()) {
            if (listUFiles.get(index).equals(listTFiles.get(index))) {
                ncT = ncU;
            } else {
                ncT = DatasetUtil.openFile(listTFiles.get(index), enhanced());
            }
        }
        nbTimeRecords = ncU.findDimension(strTimeDim).getLength();
    }

    /*
     * Determines whether or not the x-y particle location is on edge of the
     * domain.
     *
     * @param x a double, the x-coordinate
     * @param y a double, the y-coordinate
     * @return <code>true</code> if the particle is on edge of the domain
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isOnEdge(double[] pGrid) {

        /* NEMO way
        return ((pGrid[0] > (nx - 3.0f))
                || (pGrid[0] < 2.0f)
                || (pGrid[1] > (ny - 3.0f))
                || (pGrid[1] < 2.0f));
         */
        // barrier.n, 2017-08-02> adding the last two lines for zonal checking */
        return ((pGrid[0] > (nx - 2.d)) || ((pGrid[0] < 1.d))
                || (pGrid[1] > (ny - 2.d)) || (pGrid[1] < 1.d));

    }

//////////
// Getters
//////////
    /**
     * Gets the grid dimension in the XI-direction
     *
     * @return an int, the grid dimension in the XI-direction (Zonal)
     */
    @Override
    public int get_nx() {
        return nx;
    }

    /**
     * Gets the grid dimension in the ETA-direction
     *
     * @return an int, the grid dimension in the ETA-direction (Meridional)
     */
    @Override
    public int get_ny() {
        return ny;
    }

    /**
     * Gets the grid dimension in the vertical direction
     *
     * @return an int, the grid dimension in the vertical direction
     */
    @Override
    public int get_nz() {
        return nz;
    }

    /**
     * Gets domain minimum latitude.
     *
     * @return a double, the domain minimum latitude [north degree]
     */
    @Override
    public double getLatMin() {
        return latMin;
    }

    /**
     * Gets domain maximum latitude.
     *
     * @return a double, the domain maximum latitude [north degree]
     */
    @Override
    public double getLatMax() {
        return latMax;
    }

    /**
     * Gets domain minimum longitude.
     *
     * @return a double, the domain minimum longitude [east degree]
     */
    @Override
    public double getLonMin() {
        return lonMin;
    }

    /**
     * Gets domain maximum longitude.
     *
     * @return a double, the domain maximum longitude [east degree]
     */
    @Override
    public double getLonMax() {
        return lonMax;
    }

    /**
     * Gets domain maximum depth.
     *
     * @return a float, the domain maximum depth [meter]
     */
    @Override
    public double getDepthMax() {
        return depthMax;
    }

    /**
     * Gets the latitude at (i, j) grid point.
     *
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the latitude [north degree] at (i, j) grid point.
     */
    @Override
    public double getLat(int i, int j) {
        return latitude[j];
    }

    /**
     * Gets the longitude at (i, j) grid point.
     *
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the longitude [east degree] at (i, j) grid point.
     */
    @Override
    public double getLon(int i, int j) {
        return longitude[i];
    }

    /**
     * Gets the bathymetry at (i, j) grid point.
     *
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the bathymetry [meter] at (i, j) grid point if is in
     * water, return NaN otherwise.
     */
    @Override
    public double getBathy(int i, int j) {

        double bathy = 0.d;
        if (isInWater(i, j, nz - 1)) {
            for (int k = 0; k < nz; k++) {
                bathy += Double.isNaN(maskRho[k][j][i] * e3t[k][j][i])
                        ? 0.d
                        : maskRho[k][j][i] * e3t[k][j][i];
                //System.out.println("k: " + k + " " + maskRho[k][j][i] + " " + e3t[k][j][i] + " " + bathy);
            }
            return bathy;
        }
        return Double.NaN;
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        //wr_tp0 = wr_tp1;
        rank += time_arrow;

        if (rank > (nbTimeRecords - 1) || rank < 0) {
            open(indexFile = DatasetUtil.next(listUFiles, indexFile, time_arrow));
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }

        setAllFieldsTp1AtTime(rank);

    }

    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        Variable variable = nc.findVariable(name);
        int[] origin = null, shape = null;
        boolean hasVerticalDim = false;
        switch (variable.getShape().length) {
            case 4:
                origin = new int[]{rank, 0, jpo, ipo};
                shape = new int[]{1, nz, ny, nx};
                hasVerticalDim = true;
                break;
            case 2:
                origin = new int[]{jpo, ipo};
                shape = new int[]{ny, nx};
                break;
            case 3:
                if (!variable.isUnlimited()) {
                    origin = new int[]{0, jpo, ipo};
                    shape = new int[]{nz, ny, nx};
                    hasVerticalDim = true;
                } else {
                    origin = new int[]{rank, jpo, ipo};
                    shape = new int[]{1, ny, nx};
                }
                break;
        }

        Array array = variable.read(origin, shape).reduce();
        if (hasVerticalDim) {
            array = array.flip(0);
        }
        return array;
    }

    @Override
    public double xTore(double x) {
        return x;
    }

    @Override
    public double yTore(double y) {
        return y;
    }
}
