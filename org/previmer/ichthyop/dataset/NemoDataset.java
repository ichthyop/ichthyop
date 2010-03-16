/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import java.util.logging.Logger;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
import org.previmer.ichthyop.event.NextStepEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import org.previmer.ichthyop.util.MTRandom;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public class NemoDataset extends AbstractDataset {

    ////////////////
// Debug purpose
////////////////
    /**
     * Constant for debugging vertical dispersion
     */
    public final static boolean DEBUG_VDISP = false;
    /**
     * Constant for debugginf horizontal dispersion
     */
    public final static boolean DEBUG_HDISP = false;
///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Grid dimension
     */
    static int nx, ny, nz;
    /**
     * Origin for grid index
     */
    static int ipo, jpo;
    /**
     * Number of time records in current NetCDF file
     */
    private static int nbTimeRecords;
    /**
     * The NetCDF dataset
     *
     * pverley pour chourdin: avec ROMS ou MARS, je n'ai besoin de lire qu'un
     * seul fichier à la fois. Je le lis grace à la variable ncIn. A priori pas
     * possible de fonctionner comme ça avec OPA puiqu'il y a différents
     * fichiers où puiser l'information. Donc une des étapes c'est de remplacer
     * ncIn dans le code par le fichier spécifique à ouvrir. Au final cette
     * variable va disparaitre, je pense.
     */
    //static NetcdfFile ncIn;
    /**
     * Longitude at rho point.
     */
    static float[][] lonRho;
    /**
     * Latitude at rho point.
     */
    static float[][] latRho;
    /**
     * Bathymetry
     */
    static float[][] hRho;
    /**
     * Mask: water = 1, cost = 0
     *
     * pverley pour chourdin: attention ici le masque devient 3D
     */
    static byte[][][] maskRho;
    /**
     * Ocean free surface elevetation at current time
     */
    static float[][] zeta_tp0;
    /**
     * /**
     * Ocean free surface elevetation at time t + dt
     */
    static float[][] zeta_tp1;
    /**
     * Zonal component of the velocity field at current time
     */
    static float[][][] u_tp0;
    /**
     * Zonal component of the velocity field at time t + dt
     */
    static float[][][] u_tp1;
    /**
     * Meridional component of the velocity field at current time
     */
    static float[][][] v_tp0;
    /**
     *  Meridional component of the velocity field at time t + dt
     */
    static float[][][] v_tp1;
    /**
     * Vertical component of the velocity field at current time
     */
    static float[][][] w_tp0;
    /**
     * Vertical component of the velocity field at time t + dt
     */
    static float[][][] w_tp1;
    /**
     * Water salinity at time t + dt
     */
    private static float[][][] salt_tp1;
    /**
     * Water salinity at current time
     */
    private static float[][][] salt_tp0;
    /**
     * Water temperature at current time
     */
    private static float[][][] temp_tp0;
    /**
     * Water temperature at time t + dt
     */
    private static float[][][] temp_tp1;
    /**
     * Large zooplankton concentration at current time
     */
    private float[][][] largeZoo_tp0;
    /**
     * Large zooplankton concentration at time t + dt
     */
    private float[][][] largeZoo_tp1;
    /**
     * Small zooplankton concentration at current time
     */
    private float[][][] smallZoo_tp0;
    /**
     * Small zooplankton concentration at time t + dt
     */
    private float[][][] smallZoo_tp1;
    /**
     * Large phytoplankton concentration at current time
     */
    private float[][][] largePhyto_tp0;
    /**
     * Large phytoplankton concentration at time t + dt
     */
    private float[][][] largePhyto_tp1;
    /**
     * Vertical diffusion coefficient at time t + dt
     */
    private float[][][] kv_tp1;
    /**
     * Vertical diffusion coefficient at current time
     */
    private float[][][] kv_tp0;
    /**
     * Depth at rho point
     */
    static float[] z_rho_cst;
    /**
     * Depth at w point at current time.
     * Takes account of free surface elevation.
     */
    static double[][][] z_w_tp0;
    /**
     * Depth at w point at time t + dt
     * Takes account of free surface elevation.
     */
    static double[][][] z_w_tp1;
    /**
     * Depth at w point. The free surface elevation is disregarded.
     */
    static float[] z_w_cst;
    /**
     * Geographical boundary of the domain
     */
    private static double latMin, lonMin, latMax, lonMax;
    /**
     * Maximum depth [meter] of the domain
     */
    private static double depthMax;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    static double dt_HyMo;
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
    static double time_tp1;
    /**
     * Current rank in NetCDF dataset
     */
    private static int rank;
    /**
     * Time arrow: forward = +1, backward = -1
     */
    private static int time_arrow;
    /**
     * Mersenne Twister pseudo random number generator
     * @see ichthyop.util.MTRandom
     */
    private MTRandom random;
    /**
     * Name of the Dimension in NetCDF file
     */
    static String strXiDim, strEtaDim, strZDim, strTimeDim;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strU, strV, strTp, strSal, strTime, strZeta;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strLon, strLat, strMask, strBathy;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strKv;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strLargePhyto, strLargeZoo, strSmallZoo;
    /**
     * Determines whether or not the temperature field should be read in the
     * NetCDF file, function of the user's options.
     */
    private static boolean FLAG_TP;
    /**
     * Determines whether or not the salinity field should be read in the
     * NetCDF file, function of the user's options.
     */
    private static boolean FLAG_SAL;
    /**
     * Determines whether or not the turbulent diffusivity should be read in the
     * NetCDF file, function of the user's options.
     */
    private static boolean FLAG_VDISP;
    /**
     * Determines whether or not the plankton concentration fields should be
     * read in the NetCDF file, function of the user's options.
     */
    private static boolean FLAG_PLANKTON;
///////////////////////////////
// Declaration of the constants
///////////////////////////////
    /**
     * Turbulent dissipation rate used in the parametrization of Lagrangian
     * horizontal diffusion.
     * @see Monin and Ozmidov, 1981
     */
    private final static double EPSILON = 1e-9;
    private final static double EPSILON16 = Math.pow(EPSILON, 1.d / 6.d);
//////////////////////////////
// New variables added for OPA
//////////////////////////////
    static float[][][] e3t;
    static double[][] e1t, e2t, e1v, e2u;
    static String stre1t, stre2t, stre3t, stre1v, stre2u;
    static String strueiv, strveiv, strweiv;
    static String str_gdepT, str_gdepW;
    private ArrayList<String> listUFiles, listVFiles, listWFiles, listTFiles;
    //modif sp : lecture du kv
    private static NetcdfFile ncU, ncV, ncW, ncT;
    //modif sp : lecture du kv
    private static String file_hgr, file_zgr, file_mask;
    private static boolean isGridInfoInOneFile;

////////////////////////////
// Definition of the methods
////////////////////////////
    /**
     * Reads time non-dependant fields in NetCDF dataset
     */
    private void readConstantField() throws IOException {

        int[] origin = new int[]{jpo, ipo};
        int[] size = new int[]{ny, nx};
        NetcdfFile nc;
        try {
            nc = NetcdfDataset.openFile(listTFiles.get(0), null);
            System.out.println("read lon lat mask " + nc.getLocation());
            //fichier *byte*mask*
            lonRho = (float[][]) nc.findVariable(strLon).read(origin, size).
                    copyToNDJavaArray();
            latRho = (float[][]) nc.findVariable(strLat).read(origin, size).
                    copyToNDJavaArray();
            nc = NetcdfDataset.openFile(file_mask, null);
            maskRho = (byte[][][]) nc.findVariable(strMask).read(new int[]{0,
                        0, jpo, ipo}, new int[]{1, nz, ny, nx}).flip(1).reduce().
                    copyToNDJavaArray();
            if (!isGridInfoInOneFile) {
                nc.close();
                nc = NetcdfDataset.openFile(file_zgr, null);
            }
            System.out.println("read bathy gdept gdepw e3t " + nc.getLocation());
            //fichier *mesh*z*
            hRho = (float[][]) nc.findVariable(strBathy).read(new int[]{0, 0,
                        jpo, ipo}, new int[]{1, 1, ny, nx}).reduce().
                    copyToNDJavaArray();
            z_rho_cst = (float[]) nc.findVariable(str_gdepT).read(new int[]{0,
                        0, 0, 0}, new int[]{1, nz, 1, 1}).flip(1).reduce().
                    copyTo1DJavaArray();
            z_w_cst = (float[]) nc.findVariable(str_gdepW).read(new int[]{0, 0,
                        0, 0}, new int[]{1, nz + 1, 1, 1}).flip(1).reduce().
                    copyTo1DJavaArray();
            e3t = (float[][][]) nc.findVariable(stre3t).read(new int[]{0, 0, 0,
                        0}, new int[]{1, nz, ny, nx}).flip(1).reduce().
                    copyToNDJavaArray();
            if (!isGridInfoInOneFile) {
                nc.close();
                nc = NetcdfDataset.openFile(file_hgr, null);
            }
            System.out.println("read e1t e2t " + nc.getLocation());
            // fichier *mesh*h*
            e1t = (double[][]) nc.findVariable(stre1t).read(new int[]{0, 0,
                        jpo, ipo}, new int[]{1, 1, ny, nx}).reduce().
                    copyToNDJavaArray();
            e2t = (double[][]) nc.findVariable(stre2t).read(new int[]{0, 0,
                        jpo, ipo}, new int[]{1, 1, ny, nx}).reduce().
                    copyToNDJavaArray();
            e1v = (double[][]) nc.findVariable(stre1v).read(new int[]{0, 0,
                        jpo, ipo}, new int[]{1, 1, ny, nx}).reduce().
                    copyToNDJavaArray();
            e2u = (double[][]) nc.findVariable(stre2u).read(new int[]{0, 0,
                        jpo, ipo}, new int[]{1, 1, ny, nx}).reduce().
                    copyToNDJavaArray();
            nc.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            //throw new IOException(ex);
        }
    }

    /**
     * Advects the particle with the model velocity vector, using a Forward
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
     * pverley pour chourdin: ici il faudra revoir les adimensionalisations
     * de u et v par e2u et e1v
     */
    public double get_dUx(double[] pGrid, double time) {

        double du = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) Math.round(ix);
        int j = (int) jy;
        int k = (int) kz;
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double dz = kz - (double) k;
        double CO = 0.d;
        double co = 0.d;
        double x = 0.d;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < n; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    co = Math.abs((.5d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    x = 0.d;
                    x = (1.d - x_euler) * u_tp0[k + kk][j + jj][i + ii - 1]
                            + x_euler * u_tp1[k + kk][j + jj][i + ii - 1];
                    du += x * co / e2u[j + jj][i + ii - 1];
                }
            }
        }
        if (CO != 0) {
            du /= CO;
        }
        return du;
    }

    public double get_dWz(double[] pGrid, double time) {

        double dw = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) ix;
        int j = (int) jy;
        int k = (int) Math.round(kz);
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double dz = kz - (double) k;
        double CO = 0.d;
        double co = 0.d;
        double x = 0.d;
        for (int ii = 0; ii < n; ii++) {
            for (int jj = 0; jj < n; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    co = Math.abs((1.d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (.5d - (double) kk - dz));
                    CO += co;
                    x = 0.d;
                    x = (1.d - x_euler) * w_tp0[k + kk][j + jj][i + ii]
                            + x_euler * w_tp1[k + kk][j + jj][i + ii];
                    dw += 2.d * x * co
                            / (z_w_cst[Math.min(k + kk + 1, nz)]
                            - z_w_cst[Math.max(k + kk - 1, 0)]);
                }
            }
        }
        if (CO != 0) {
            dw /= CO;
        }
        return dw;
    }

    public double get_dVy(double[] pGrid, double time) {
        double dv = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) ix;
        int j = (int) Math.round(jy);
        int k = (int) kz;
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double dz = kz - (double) k;
        double CO = 0.d;
        double co = 0.d;
        double x = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < 2; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    co = Math.abs((1.d - (double) ii - dx)
                            * (.5d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    x = 0.d;
                    x = (1.d - x_euler) * v_tp0[k + kk][j + jj - 1][i
                            + ii]
                            + x_euler * v_tp1[k + kk][j + jj - 1][i + ii];
                    dv += x * co / e1v[j + jj - 1][i + ii];
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
    private float[][][] computeW() {

        double[][][] Huon = new double[nz][ny][nx];
        double[][][] Hvom = new double[nz][ny][nx];

        //---------------------------------------------------
        // Calculation Coeff Huon & Hvom
        for (int k = nz; k-- > 0;) {
            for (int i = 0; i < nx - 1; i++) {
                for (int j = 0; j < ny; j++) {
                    Huon[k][j][i] = u_tp1[k][j][i] * e2u[j][i]
                            * Math.min(e3t[k][j][i], e3t[k][j][i + 1]);
                }
            }
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny - 1; j++) {
                    Hvom[k][j][i] = v_tp1[k][j][i] * e1v[j][i]
                            * Math.min(e3t[k][j][i], e3t[k][j + 1][i]);
                }
            }
        }

        //---------------------------------------------------
        // Calcultaion of w(i, j, k)
        double[][][] w_double = new double[nz + 1][ny][nx];

        for (int j = 1; j < ny - 1; j++) {
            for (int i = 1; i < nx - 1; i++) {
                w_double[0][j][i] = 0.d;
                for (int k = 1; k < nz; k++) {
                    w_double[k][j][i] = w_double[k - 1][j][i]
                            - (Huon[k - 1][j][i] - Huon[k - 1][j][i
                            - 1] + Hvom[k - 1][j][i] - Hvom[k
                            - 1][j - 1][i]);
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
        float[][][] w = new float[nz + 1][ny][nx];
        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                for (int k = nz + 1; k-- > 0;) {
                    w[k][j][i] = (float) (w_double[k][j][i]
                            / (e1t[j][i] * e2t[j][i]));
                }
            }
        }

        //---------------------------------------------------
        // Return w
        return w;
    }

    /**
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
     * pverley pour chourdin: même remarque que chaque fois. Les infos dans
     * OPA se trouvent dans différents fichiers, donc selon la méthode appelée,
     * je dois recharger le fichier NetCDF correspondant. Au lieu d'utiliser
     * la variable ncIn globale.
     */
    private void readLonLat() throws IOException {

        NetcdfFile nc;
        Array arrLon, arrLat;
        try {
            nc = NetcdfDataset.openFile(listTFiles.get(0), null);
            arrLon = nc.findVariable(strLon).read();
            arrLat = nc.findVariable(strLat).read();
            if (arrLon.getElementType() == float.class) {
                lonRho = (float[][]) arrLon.copyToNDJavaArray();
                latRho = (float[][]) arrLat.copyToNDJavaArray();
            } else {
                lonRho = new float[ny][nx];
                latRho = new float[ny][nx];
                Index index = arrLon.getIndex();
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        index.set(j, i);
                        lonRho[j][i] = arrLon.getFloat(index);
                        latRho[j][i] = arrLat.getFloat(index);
                    }
                }
            }
            nc.close();
            arrLon = null;
            arrLat = null;
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    /**
     * Gets cell dimension [meter] in the XI-direction.
     *
     * pverley pour chourdin: vérifier avec Steph que je ne me trompe pas
     * dans la définition de e1t et e2t
     */
    public double getdxi(int j, int i) {

        return e1t[j][i];
    }

    /**
     * Gets cell dimension [meter] in the ETA-direction.
     */
    public double getdeta(int j, int i) {

        return e2t[j][i];
    }

    /**
     * Sets up the {@code Dataset}. The method first sets the appropriate
     * variable names, loads the first NetCDF dataset and extract the time
     * non-dependant information, such as grid dimensions, geographical
     * boundaries, depth at sigma levels.
     * @throws an IOException if an error occurs while setting up the
     * {@code Dataset}
     */
    public void setUp() {

        loadParameters();

        try {
            sortInputFiles();
            getDimNC();
            if (Boolean.valueOf(getParameter("is_ranged"))) {
                float[] p1 = new float[]{Float.valueOf(getParameter("range_P1_lon")), Float.valueOf(getParameter("range_P1_lat"))};
                float[] p2 = new float[]{Float.valueOf(getParameter("range_P2_lon")), Float.valueOf(getParameter("range_P2_lat"))};
                range(p1, p2);
            }
            readConstantField();
            getDimGeogArea();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Gets the names of the NetCDF variables from the configuration file.
     *
     * pverley pour chourdin : "Configuration" c'est la classe qui lit le
     * fichier de configuration. Tu vois que normallement, le nom des variables
     * est lu dans le fichier cfg. Temporairement je courcircuite
     * cette opération et je renseigne à la main le nom des variables
     * des sorties OPA.
     */
    public void loadParameters() {

        strXiDim = getParameter("field_dim_x");
        strEtaDim = getParameter("field_dim_y");
        strZDim = getParameter("field_dim_z");
        strTimeDim = getParameter("field_dim_time");
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strBathy = getParameter("field_var_bathy");
        strMask = getParameter("field_var_mask");
        strU = getParameter("field_var_u");
        strV = getParameter("field_var_v");
        //strZeta = Configuration.getStrZeta();
        strTp = getParameter("field_var_temp");
        strSal = getParameter("field_var_salt");
        strTime = getParameter("field_var_time");
        //strKv = Configuration.getStrKv();
        strKv = getParameter("field_var_kv");
        // modif sp : lecture du kv
        stre3t = getParameter("field_var_e3t");
        str_gdepT = getParameter("field_var_gdept"); // z_rho
        str_gdepW = getParameter("field_var_gdepw"); // z_w
        stre1t = getParameter("field_var_e1t");
        stre2t = getParameter("field_var_e2t");
        stre1v = getParameter("field_var_e1v");
        stre2u = getParameter("field_var_e2u");

        // Ces trois variables concernent des champs BIO existant dans
        // certaines config de ROMS couplé avec un modèle de biogéochimie.
        strLargePhyto = getParameter("");
        strLargeZoo = getParameter("");
        strSmallZoo = getParameter("");

        //fichier *mask*
        /*strXiDim = "x";
        strEtaDim = "y";
        strZDim = "z";
        strTimeDim = "time_counter";
        strLon = "nav_lon";
        strLat = "nav_lat";
        strMask = "fmask";
        strTime = "time_counter";
        // fichier *zgr*
        stre3t = "e3t_ps";
        strBathy = "hdepw";
        str_gdepT = "gdept"; // z_rho
        str_gdepW = "gdepw"; // z_w
        // fichier *gridu*
        strU = "vozocrtx";
        strueiv = "vozoeivu";
        //fichier *gridv*
        strV = "vomecrty";
        strveiv = "vomeeivv";
        //fichier *gridt*
        strTp = "votemper";
        strSal = "vosaline";
        //fichier *hgr*
        stre1t = "e1t";
        stre2t = "e2t";
        stre1v = "e1v";
        stre2u = "e2u";
        //fichier *gridw*
        strKv = "votkeavt";
        // modif sp : lecture du kv
        //strweiv = "voveeivx";*/
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     * @throws an IOException if an error occurs while reading the dimensions.
     *
     * pverley pour chourdin: Pour ROMS ou MARS je lisais les dimensions à
     * partir de la variable ncIn qui est le premier fichier sortie qui
     * me tombe sous la main. Avec OPA, les dimensions se lisent dans
     * un fichier particulier *byte*mask*. A déterminer si toujours vrai ?
     */
    private void getDimNC() throws IOException {

        NetcdfFile nc = new NetcdfDataset();
        try {
            nc = NetcdfDataset.openFile(file_mask, null);
            nx = nc.findDimension(strXiDim).getLength();
            ny = nc.findDimension(strEtaDim).getLength();
            nz = nc.findDimension(strZDim).getLength() - 1;
        } catch (NullPointerException e) {
            e.printStackTrace();
            /*throw new IOException("Problem reading dimensions from dataset "
            + nc.getLocation() + " : " + e.getMessage());*/
        }

        ipo = jpo = 0;
    }

    /**
     * Resizes the domain and determines the range of the grid indexes
     * taht will be used in the simulation.
     * The new domain is limited by the Northwest and the Southeast corners.
     * @param pGeog1 a float[], the geodesic coordinates of the domain
     * Northwest corner
     * @param pGeog2  a float[], the geodesic coordinates of the domain
     * Southeast corner
     * @throws an IOException if the new domain is not strictly nested
     * within the NetCDF dataset domain.
     */
    private void range(float[] pGeog1, float[] pGeog2) throws IOException {

        double[] pGrid1, pGrid2;
        int ipn, jpn;

        readLonLat();

        pGrid1 = lonlat2xy(pGeog1[0], pGeog1[1]);
        pGrid2 = lonlat2xy(pGeog2[0], pGeog2[1]);
        if (pGrid1[0] < 0 || pGrid2[0] < 0) {
            throw new IOException(
                    "Impossible to proportion the simulation area : points out of domain");
        }
        lonRho = null;
        latRho = null;

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
     * Computes the depth at w points, taking account of the free surface
     * elevation.
     * @return a double[][][], the depth at w point.
     *
     * pverley pour chourdin: je pense que c'est méthode n'est pas à faire
     * non plus pour OPA, puisqu'elle concerne la variation des niveaux
     * sigmas en fonction de la variation de la surface libre.
     */
    static double[][][] getSigLevels() {

        //-----------------------------------------------------
        // Daily recalculation of z_w and z_r with zeta

        return null;
    }

    /**
     * Determines the geographical boundaries of the domain in longitude,
     * latitude and depth.
     */
    private void getDimGeogArea() {

        //--------------------------------------
        // Calculate the Physical Space extrema

        lonMin = Double.MAX_VALUE;
        lonMax = -lonMin;
        latMin = Double.MAX_VALUE;
        latMax = -latMin;
        depthMax = 0.d;
        int i = nx;
        int j = 0;

        while (i-- > 0) {
            j = ny;
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

                if (hRho[j][i] >= depthMax) {
                    depthMax = hRho[j][i];
                }
            }
        }
        System.out.println("lonmin " + lonMin + " lonmax " + lonMax
                + " latmin " + latMin + " latmax " + latMax);
        System.out.println("depth max " + depthMax);

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
    }

    /**
     * Initializes the {@code Dataset}. Opens the file holding the first time
     * of the simulation. Checks out the existence of the fields required
     * by the current simulation. Sets all fields at time for the first time
     * step.
     * @throws an IOException if a required field cannot be found in the NetCDF
     * dataset.
     */
    public void init() {
        try {
            time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());
            long t0 = getSimulationManager().getTimeManager().get_tO();
            open(indexFile = getIndexFile(t0));
            FLAG_TP = FLAG_SAL = true;
            FLAG_VDISP = false;
            setAllFieldsTp1AtTime(rank = findCurrentRank(t0));
            time_tp1 = t0;
        } catch (IOException ex) {
            Logger.getLogger(NemoDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Reads time dependant variables in NetCDF dataset at specified rank.
     * @param rank an int, the rank of the time dimension in the NetCDF dataset.
     * @throws an IOException if an error occurs while reading the variables.
     *
     * pverley pour chourdin: la aussi je fais du provisoire en attendant
     * de voir si on peut dégager une structure systématique des input.
     */
    void setAllFieldsTp1AtTime(int rank) throws IOException {

        int[] origin = new int[]{rank, 0, jpo, ipo};
        double time_tp0 = time_tp1;

        try {
            u_tp1 = (float[][][]) ncU.findVariable(strU).read(origin, new int[]{1, nz, ny, nx}).
                    flip(1).reduce().copyToNDJavaArray();

            v_tp1 = (float[][][]) ncV.findVariable(strV).read(origin, new int[]{1, nz, ny, nx}).
                    flip(1).reduce().copyToNDJavaArray();

            Array xTimeTp1 = ncU.findVariable(strTime).read();
            time_tp1 = xTimeTp1.getFloat(xTimeTp1.getIndex().set(rank));
            xTimeTp1 = null;

            // Est-ce que le zeta existe dans OPA (variation surface libre) ?
            /*zeta_tp1 = (float[][]) ncIn.findVariable(strZeta).read(new int[] {
            rank, 0, 0}, new int[] {1, ny, nx}).reduce().
            copyToNDJavaArray();*/

            if (FLAG_TP) {
                temp_tp1 = (float[][][]) ncT.findVariable(strTp).read(origin,
                        new int[]{1, nz, ny, nx}).reduce().copyToNDJavaArray();
            }

            if (FLAG_SAL) {
                salt_tp1 = (float[][][]) ncT.findVariable(strSal).read(origin,
                        new int[]{1, nz, ny, nx}).reduce().copyToNDJavaArray();
            }

            if (FLAG_VDISP) {
                /*kv_tp1 = (float[][][]) ncIn.findVariable(strKv).read(origin,
                new int[] {1, nz, ny, nx}).reduce().copyToNDJavaArray();*/
                kv_tp1 = (float[][][]) ncW.findVariable(strKv).read(origin,
                        new int[]{1, nz, ny, nx}).reduce().copyToNDJavaArray();
                // modif sp : lecture du kv
            }

            if (FLAG_PLANKTON) {
                /*largePhyto_tp1 = (float[][][]) ncIn.findVariable(strLargePhyto).
                read(origin, new int[] {1, nz, ny, nx}).reduce().
                copyToNDJavaArray();
                largeZoo_tp1 = (float[][][]) ncIn.findVariable(strLargeZoo).
                read(origin, new int[] {1, nz, ny, nx}).reduce().
                copyToNDJavaArray();
                smallZoo_tp1 = (float[][][]) ncIn.findVariable(strSmallZoo).
                read(origin, new int[] {1, nz, ny, nx}).reduce().
                copyToNDJavaArray();*/
            }
        } catch (Exception e) {
            throw new IOException(e);
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
        //z_w_tp1 = getSigLevels(); // inutile puisque pas de niveau sigma
        w_tp1 = computeW();
    }

    /**
     * Computes the depth  of the specified sigma level at the x-y particle
     * location.
     * @param xRho a double, x-coordinate of the grid point
     * @param yRho a double, y-coordinate of the grid point
     * @param k an int, the index of the sigma level
     * @return a double, the depth [meter] at (x, y, k)
     */
    public double getDepth(int k) {

        return -1.d * z_rho_cst[k];
    }

    /**
     * Determines whether or not the specified grid cell(i, j) is in water.
     * @param i an int, i-coordinate of the cell
     * @param j an intn the j-coordinate of the cell
     * @return <code>true</code> if cell(i, j) is in water,
     *         <code>false</code> otherwise.
     */
    private boolean isInWater(int i, int j, int k) {
        //System.out.println(i + " " + j + " " + k + " - "  + (maskRho[k][j][i] > 0));
        try {
            return (maskRho[k][j][i] > 0);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    public boolean isInWater(int i, int j) {
        return isInWater(i, j, nz - 1);
    }

    /**
     * Determines whether the specified {@code RohPoint} is in water.
     * @param ptRho the RhoPoint
     * @return <code>true</code> if the {@code RohPoint} is in water,
     *         <code>false</code> otherwise.
     * @see #isInWater(int i, int j)
     */
    public boolean isInWater(double[] pGrid) {
        return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]), (int) Math.round(pGrid[2]));
    }

    /**
     * Determines whether or not the specified grid point is close to cost line.
     * The method first determines in which quater of the cell the grid point is
     * located, and then checks wether or not its cell and the three adjacent
     * cells to the quater are in water.
     *
     * @param pGrid a double[] the coordinates of the grid point
     * @return <code>true</code> if the grid point is close to cost,
     *         <code>false</code> otherwise.
     */
    boolean isCloseToCost(double[] pGrid) {

        int i, j, k, ii, jj;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        k = (int) (Math.round(pGrid[2]));
        ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
        jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
        return !(isInWater(i + ii, j, k) && isInWater(i + ii, j + jj, k)
                && isInWater(i, j + jj, k));
    }

    /**
     * Transforms the depth at specified x-y particle location into z coordinate
     *
     * @param xRho a double, the x-coordinate
     * @param yRho a double, the y-coordinate
     * @param depth a double, the depth of the particle
     * @return a double, the z-coordinate corresponding to the depth
     *
     * pverley pour chourdin: méthode à tester.
     */
    public double depth2z(double x, double y, double depth) {

        depth = Math.abs(depth);
        //-----------------------------------------------
        // Return z[grid] corresponding to depth[meters]
        double zRho = 0.d;
        try {
            for (int k = nz - 1; k > 0; k--) {
                //System.out.println("t1 " + z_w[k] + " " + (float)depth + " " + z_rho[k]);
                if (depth <= z_w_cst[k] && depth > z_rho_cst[k]) {
                    zRho = k + 0.d
                            - 0.5d
                            * Math.abs((z_rho_cst[k] - depth)
                            / (z_rho_cst[k] - z_w_cst[k]));
                    //System.out.println("t1 zGeo2Grid z = " + zRho);
                    return zRho;
                }
                //System.out.println("t2 " + z_rho[k] + " " + (float)depth + " " + z_w[k + 1]);
                if (depth <= z_rho_cst[k] && depth > z_w_cst[k + 1]) {
                    zRho = k + 0.d
                            + 0.5d
                            * Math.abs((z_rho_cst[k] - depth)
                            / (z_w_cst[k + 1] - z_rho_cst[k]));
                    //System.out.println("t2 zGeo2Grid z = " + zRho);
                    return zRho;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //System.out.println("zGeo2Grid z = " + zRho);
        return zRho;
    }

    /**
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
    public double z2depth(double x, double y, double z) {

        double depth = Double.NaN;
        double dz;

        int k = (int) Math.round(z);
        dz = z - k;

        try {
            if (dz < 0) { // >= ?
                depth = z_rho_cst[k]
                        + 2 * Math.abs(dz * (z_rho_cst[k] - z_w_cst[k]));
            } else {
                depth = z_rho_cst[k]
                        - 2 * Math.abs(dz * (z_rho_cst[k] - z_w_cst[k + 1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -depth;
    }

    /**
     * Transforms the specified 3D grid coordinates into geographical
     * coordinates.
     * It merely does a trilinear spatial interpolation of the surrounding grid
     * nods geographical coordinates.
     * @param xRho a double, the x-coordinate
     * @param yRho a double, the y-coordinate
     * @param zRho a double, the z-coordinate
     * @return a double[], the corresponding geographical coordinates
     * (latitude, longitude, depth)
     */
    public double[] grid2Geo(double xRho, double yRho, double zRho) {

        //--------------------------------------------------------------------
        // Computational space (x, y , z) => Physical space (lat, lon, depth)

        double[] pgeog = xy2lonlat(xRho, yRho);
        return new double[]{pgeog[0], pgeog[1], z2depth(xRho, yRho, zRho)};
    }

    /**
     * * Transforms the specified 2D grid coordinates into geographical
     * coordinates.
     * It merely does a bilinear spatial interpolation of the surrounding grid
     * nods geographical coordinates.
     * @param xRho a double, the x-coordinate
     * @param yRho a double, the y-coordinate
     * @return a double[], the corresponding geographical coordinates
     * (latitude, longitude)

     * @param xRho double
     * @param yRho double
     * @return double[]
     */
    public double[] xy2lonlat(double xRho, double yRho) {

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
        double co = 0.d;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                latitude += co * latRho[j + jj][i + ii];
                longitude += co * lonRho[j + jj][i + ii];
            }
        }
        return (new double[]{latitude, longitude});
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

     * @param lon a double, the longitude of the geographical point
     * @param lat a double, the latitude of the geographical point
     * @return a double[], the corresponding grid coordinates (x, y)
     * @see #isInsidePolygone
     */
    public double[] lonlat2xy(double lon, double lat) {

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
    }

    /**
     * Determines whether the specified geographical point (lon, lat) belongs
     * to the is inside the polygon defined by (imin, jmin) & (imin, jmax) &
     * (imax, jmax) & (imax, jmin).
     *
    <p>
     * The algorithm has been adapted from a function in ROMS/UCLA code,
     * originally written by Alexander F. Shchepetkin and Hernan G. Arango.
     * Please find below an extract of the ROMS/UCLA documention.
     * </p>
     * <pre>
     * Given the vectors Xb and Yb of size Nb, defining the coordinates
     * of a closed polygon,  this function find if the point (Xo,Yo) is
     * inside the polygon.  If the point  (Xo,Yo)  falls exactly on the
     * boundary of the polygon, it still considered inside.
     * This algorithm does not rely on the setting of  Xb(Nb)=Xb(1) and
     * Yb(Nb)=Yb(1).  Instead, it assumes that the last closing segment
     * is (Xb(Nb),Yb(Nb)) --> (Xb(1),Yb(1)).
     *
     * Reference:
     * Reid, C., 1969: A long way from Euclid. Oceanography EMR,
     * page 174.
     *
     * Algorithm:
     *
     * The decision whether the point is  inside or outside the polygon
     * is done by counting the number of crossings from the ray (Xo,Yo)
     * to (Xo,-infinity), hereafter called meridian, by the boundary of
     * the polygon.  In this counting procedure,  a crossing is counted
     * as +2 if the crossing happens from "left to right" or -2 if from
     * "right to left". If the counting adds up to zero, then the point
     * is outside.  Otherwise,  it is either inside or on the boundary.
     *
     * This routine is a modified version of the Reid (1969) algorithm,
     * where all crossings were counted as positive and the decision is
     * made  based on  whether the  number of crossings is even or odd.
     * This new algorithm may produce different results  in cases where
     * Xo accidentally coinsides with one of the (Xb(k),k=1:Nb) points.
     * In this case, the crossing is counted here as +1 or -1 depending
     * of the sign of (Xb(k+1)-Xb(k)).  Crossings  are  not  counted if
     * Xo=Xb(k)=Xb(k+1).  Therefore, if Xo=Xb(k0) and Yo>Yb(k0), and if
     * Xb(k0-1) < Xb(k0) < Xb(k0+1),  the crossing is counted twice but
     * with weight +1 (for segments with k=k0-1 and k=k0). Similarly if
     * Xb(k0-1) > Xb(k0) > Xb(k0+1), the crossing is counted twice with
     * weight -1 each time.  If,  on the other hand,  the meridian only
     * touches the boundary, that is, for example, Xb(k0-1) < Xb(k0)=Xo
     * and Xb(k0+1) < Xb(k0)=Xo, then the crossing is counted as +1 for
     * segment k=k0-1 and -1 for segment k=k0, resulting in no crossing.
     *
     * Note 1: (Explanation of the logical condition)
     *
     * Suppose  that there exist two points  (x1,y1)=(Xb(k),Yb(k))  and
     * (x2,y2)=(Xb(k+1),Yb(k+1)),  such that,  either (x1 < Xo < x2) or
     * (x1 > Xo > x2).  Therefore, meridian x=Xo intersects the segment
     * (x1,y1) -> (x2,x2) and the ordinate of the point of intersection
     * is:
     *                y1*(x2-Xo) + y2*(Xo-x1)
     *            y = -----------------------
     *                         x2-x1
     * The mathematical statement that point  (Xo,Yo)  either coinsides
     * with the point of intersection or lies to the north (Yo>=y) from
     * it is, therefore, equivalent to the statement:
     *
     *      Yo*(x2-x1) >= y1*(x2-Xo) + y2*(Xo-x1),   if   x2-x1 > 0
     * or
     *      Yo*(x2-x1) <= y1*(x2-Xo) + y2*(Xo-x1),   if   x2-x1 < 0
     *
     * which, after noting that  Yo*(x2-x1) = Yo*(x2-Xo + Xo-x1) may be
     * rewritten as:
     *
     *      (Yo-y1)*(x2-Xo) + (Yo-y2)*(Xo-x1) >= 0,   if   x2-x1 > 0
     * or
     *      (Yo-y1)*(x2-Xo) + (Yo-y2)*(Xo-x1) <= 0,   if   x2-x1 < 0
     *
     * and both versions can be merged into  essentially  the condition
     * that (Yo-y1)*(x2-Xo)+(Yo-y2)*(Xo-x1) has the same sign as x2-x1.
     * That is, the product of these two must be positive or zero.
     * </pre>
     *
     * @param imin an int, i-coordinate of the area left corners
     * @param imax an int, i-coordinate of the area right corners
     * @param jmin an int, j-coordinate of the area left corners
     * @param jmax an int, j-coordinate of the area right corners
     * @param lon a double, the longitude of the geographical point
     * @param lat a double, the latitude of the geographical point
     * @return <code>true</code> if (lon, lat) belongs to the polygon,
     *         <code>false</code>otherwise.
     */
    public static boolean isInsidePolygone(int imin, int imax, int jmin,
            int jmax, double lon, double lat) {

        //--------------------------------------------------------------
        // Return true if (lon, lat) is insidide the polygon defined by
        // (imin, jmin) & (imin, jmax) & (imax, jmax) & (imax, jmin)

        //-----------------------------------------
        // Build the polygone
        int nb, shft;
        double[] xb, yb;
        boolean isInPolygone = true;

        nb = 2 * (jmax - jmin + imax - imin);
        xb = new double[nb + 1];
        yb = new double[nb + 1];
        shft = 0 - imin;
        for (int i = imin; i <= (imax - 1); i++) {
            xb[i + shft] = lonRho[jmin][i];
            yb[i + shft] = latRho[jmin][i];
        }
        shft = 0 - jmin + imax - imin;
        for (int j = jmin; j <= (jmax - 1); j++) {
            xb[j + shft] = lonRho[j][imax];
            yb[j + shft] = latRho[j][imax];
        }
        shft = jmax - jmin + 2 * imax - imin;
        for (int i = imax; i >= (imin + 1); i--) {
            xb[shft - i] = lonRho[jmax][i];
            yb[shft - i] = latRho[jmax][i];
        }
        shft = 2 * jmax - jmin + 2 * (imax - imin);
        for (int j = jmax; j >= (jmin + 1); j--) {
            xb[shft - j] = lonRho[j][imin];
            yb[shft - j] = latRho[j][imin];
        }
        xb[nb] = xb[0];
        yb[nb] = yb[0];

        //---------------------------------------------
        //Check if {lon, lat} is inside polygone
        int inc, crossings;
        double dx1, dx2, dxy;
        crossings = 0;

        for (int k = 0; k < nb; k++) {
            if (xb[k] != xb[k + 1]) {
                dx1 = lon - xb[k];
                dx2 = xb[k + 1] - lon;
                dxy = dx2 * (lat - yb[k]) - dx1 * (yb[k + 1] - lat);
                inc = 0;
                if ((xb[k] == lon) & (yb[k] == lat)) {
                    crossings = 1;
                } else if (((dx1 == 0.) & (lat >= yb[k]))
                        | ((dx2 == 0.) & (lat >= yb[k + 1]))) {
                    inc = 1;
                } else if ((dx1 * dx2 > 0.) & ((xb[k + 1] - xb[k]) * dxy >= 0.)) {
                    inc = 2;
                }
                if (xb[k + 1] > xb[k]) {
                    crossings += inc;
                } else {
                    crossings -= inc;
                }
            }
        }
        if (crossings == 0) {
            isInPolygone = false;
        }
        return (isInPolygone);
    }

    /**
     * Interpolates the temperature field at particle location and specified
     * time.
     *
     * @param pGrid a double[], the particle grid coordinates.
     * @param time a double, the current time [second] of the simulation
     * @return a double, the sea water temperature [celsius] at particle
     * location. Returns <code>NaN</code> if the temperature field could not
     * be found in the NetCDF dataset.
     * @throws an ArrayIndexOutOfBoundsException if the particle is out of
     * the domain.
     */
    public double getTemperature(double[] pGrid, double time) throws
            ArrayIndexOutOfBoundsException {

        if (!FLAG_TP) {
            return Double.NaN;
        }

        double co, CO, x, frac, tp;
        int n = isCloseToCost(pGrid) ? 1 : 2;

        frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

        //-----------------------------------------------------------
        // Interpolate the temperature fields
        // in the computational grid.
        int i = (int) pGrid[0];
        int j = (int) pGrid[1];
        double kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
        int k = (int) kz;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double dz = kz - (double) k;
        tp = 0.d;
        CO = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < n; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    {
                        co = Math.abs((1.d - (double) ii - dx)
                                * (1.d - (double) jj - dy)
                                * (1.d - (double) kk - dz));
                        CO += co;
                        x = 0.d;
                        try {
                            x = (1.d - frac) * temp_tp0[k + kk][j + jj][i + ii]
                                    + frac * temp_tp1[k + kk][j + jj][i + ii];
                            tp += x * co;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            throw new ArrayIndexOutOfBoundsException(
                                    "Problem interpolating temperature field : "
                                    + e.getMessage());
                        }
                    }
                }
            }
        }
        if (CO != 0) {
            tp /= CO;
        }

        return tp;

    }

    /**
     * Interpolates the salinity field at particle location and specified
     * time.
     *
     * @param pGrid a double[], the particle grid coordinates.
     * @param time a double, the current time [second] of the simulation
     * @return a double, the sea water salinity [psu] at particle
     * location. Returns <code>NaN</code> if the salinity field could not
     * be found in the NetCDF dataset.
     * @throws an ArrayIndexOutOfBoundsException if the particle is out of
     * the domain.
     */
    public double getSalinity(double[] pGrid, double time) throws
            ArrayIndexOutOfBoundsException {

        if (!FLAG_SAL) {
            return Double.NaN;
        }

        double co, CO, x, frac, sal;
        int n = isCloseToCost(pGrid) ? 1 : 2;

        frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

        //-----------------------------------------------------------
        // Interpolate the temperature fields
        // in the computational grid.
        int i = (int) pGrid[0];
        int j = (int) pGrid[1];
        double kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
        int k = (int) kz;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double dz = kz - (double) k;
        sal = 0.d;
        CO = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < n; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    {
                        co = Math.abs((1.d - (double) ii - dx)
                                * (1.d - (double) jj - dy)
                                * (1.d - (double) kk - dz));
                        CO += co;
                        x = 0.d;
                        try {
                            x = (1.d - frac) * salt_tp0[k + kk][j + jj][i + ii]
                                    + frac * salt_tp1[k + kk][j + jj][i + ii];
                            sal += x * co;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            throw new ArrayIndexOutOfBoundsException(
                                    "Problem interpolating salinity field : "
                                    + e.getMessage());
                        }
                    }
                }
            }
        }
        if (CO != 0) {
            sal /= CO;
        }

        return sal;
    }

    /**
     * Interpolates the prey concentration fields at particle location and
     * specified time: large phytoplankton, small zooplankton and large
     * zooplankton.
     *
     * @param pGrid a double[], the particle grid coordinates.
     * @param time a double, the current time [second] of the simulation
     * @return a double, the concentration [mMol/m3] of arge phytoplankton,
     * small zooplankton and large zooplankton at particle
     * location. Returns <code>NaN</code> if the prey concentration fields
     * could not be found in the NetCDF dataset.
     * @throws an ArrayIndexOutOfBoundsException if the particle is out of
     * the domain.
     */
    public double[] getPlankton(double[] pGrid, double time) {

        if (!FLAG_PLANKTON) {
            return new double[]{Double.NaN, Double.NaN, Double.NaN};
        }

        double co, CO, x, frac, largePhyto, smallZoo, largeZoo;

        frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

        //-----------------------------------------------------------
        // Interpolate the plankton concentration fields
        // in the computational grid.
        int i = (int) pGrid[0];
        int j = (int) pGrid[1];
        final double kz = Math.max(0.d,
                Math.min(pGrid[2], (double) nz - 1.00001f));
        int k = (int) kz;
        //System.out.println("i " + i + " j " + j + " k " + k);
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double dz = kz - (double) k;
        largePhyto = 0.d;
        smallZoo = 0.d;
        largeZoo = 0.d;
        CO = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < 2; jj++) {
                for (int ii = 0; ii < 2; ii++) {
                    if (isInWater(i + ii, j + jj, k + kk)) {
                        co = Math.abs((1.d - (double) ii - dx)
                                * (1.d - (double) jj - dy)
                                * (1.d - (double) kk - dz));
                        CO += co;
                        x = 0.d;
                        x = (1.d - frac) * largePhyto_tp0[k + kk][j + jj][i
                                + ii] + frac * largePhyto_tp1[k + kk][j + jj][i
                                + ii];
                        largePhyto += x * co;
                        x = (1.d - frac) * smallZoo_tp0[k + kk][j + jj][i + ii]
                                + frac * smallZoo_tp1[k + kk][j + jj][i + ii];
                        smallZoo += x * co;
                        x = (1.d - frac) * largeZoo_tp0[k + kk][j + jj][i + ii]
                                + frac * largeZoo_tp1[k + kk][j + jj][i + ii];
                        largeZoo += x * co;
                    }
                }
            }
        }
        if (CO != 0) {
            largePhyto /= CO;
            smallZoo /= CO;
            largeZoo /= CO;
        }

        return new double[]{largePhyto, smallZoo, largeZoo};
    }

    /**
     * Gets the list of NetCDF input files that satisfy the file filter and
     * sorts them according to the chronological order induced by the
     * {@code NCComparator}.
     *
     * @param path a String, the path of the folder that contains the model
     * input files.
     * @return an ArrayList, the list of the input files sorted in time.
     * @throws an IOException if an exception occurs while scanning the input
     * files.
     */
    private ArrayList<String> getInputList(String path, String fileMask) throws IOException {

        ArrayList<String> list = null;

        File inputPath = new File(path);
        //String fileMask = Configuration.getFileMask();
        File[] listFile = inputPath.listFiles(new MetaFilenameFilter(fileMask));
        if (listFile.length == 0) {
            throw new IOException(path + " contains no file matching mask "
                    + fileMask);
        }
        list = new ArrayList<String>(listFile.length);
        for (File file : listFile) {
            list.add(file.toString());
        }
        if (list.size() > 1) {
            Collections.sort(list, new NCComparator(strTime));
        }
        return list;
    }

    /**
     * Sort OPA input files. First make sure that there is at least
     * and only one file matching the hgr, zgr and byte mask patterns.
     * Then list the gridU, gridV and gridT files.
     *
     * @param path
     * @throws java.io.IOException
     */
    private void sortInputFiles() throws IOException {

        URI uriCurrent = new File("").toURI();
        String path = uriCurrent.resolve(
                URI.create(getParameter("input_path"))).getPath();
        File file = new File(path);

        file_mask = checkExistenceAndUnicity(file, getParameter("byte_mask_pattern"));
        file_hgr = checkExistenceAndUnicity(file, getParameter("hgr_pattern"));
        file_zgr = checkExistenceAndUnicity(file, getParameter("zgr_pattern"));

        isGridInfoInOneFile = file_mask.matches(file_hgr)
                && file_mask.matches(file_zgr);

        listUFiles = getInputList(path, getParameter("gridu_pattern"));
        listVFiles = getInputList(path, getParameter("gridv_pattern"));
        listTFiles = getInputList(path, getParameter("gridt_pattern"));
        listWFiles = getInputList(path, getParameter("gridw_pattern"));
        // modif sp : lecture du kv
    }

    private String checkExistenceAndUnicity(File file, String pattern) throws IOException {

        File[] listFiles = file.listFiles(new MetaFilenameFilter(pattern));
        int nbFiles = listFiles.length;

        if (nbFiles == 0) {
            throw new IOException("No file matching pattern " + pattern);
        } else if (nbFiles > 1) {
            throw new IOException("More than one file matching pattern "
                    + pattern);
        }

        return listFiles[0].toString();
    }

    /**
     * Loads the NetCDF dataset from the specified filename.
     * @param filename a String that can be a local pathname or an OPeNDAP URL.
     * @throws IOException
     */
    private void open(int index) throws IOException {

        getLogger().info("Opening dataset");
        ncU = NetcdfDataset.openFile(listUFiles.get(index), null);
        ncV = NetcdfDataset.openFile(listVFiles.get(index), null);
        ncW = NetcdfDataset.openFile(listWFiles.get(index), null);
        // modif sp : lecture du kv
        ncT = NetcdfDataset.openFile(listTFiles.get(index), null);

        nbTimeRecords = ncU.findDimension(strTimeDim).getLength();
    }

    private int getIndexNextFile(long time, int indexCurrent) throws IOException {

        int index = indexCurrent - (1 - time_arrow) / 2;
        boolean noNext = (listUFiles.size() == 1) || (index < 0)
                || (index >= listUFiles.size() - 1);
        if (noNext) {
            throw new IOException("Unable to find any file following "
                    + listUFiles.get(indexCurrent));
        }
        if (isTimeBetweenFile(time, index)) {
            return indexCurrent + time_arrow;
        }
        throw new IOException("Unable to find any file following "
                + listUFiles.get(indexCurrent));
    }

    private int getIndexFile(long time) throws IOException {

        int indexLast = listUFiles.size() - 1;

        for (int i = 0; i < indexLast; i++) {
            if (isTimeIntoFile(time, i)) {
                return i;
            } else if (isTimeBetweenFile(time, i)) {
                return (i - (time_arrow - 1) / 2);
            }
        }

        if (isTimeIntoFile(time, indexLast)) {
            return indexLast;
        }

        throw new IOException("Time value " + (long) time + " not contained among NetCDF files");
    }

    /**
     * Determines whether or not the specified time is contained within the ith
     * input file.
     * @param time a long, the current time [second] of the simulation
     * @param index an int, the index of the file in the {@code listInputFiles}
     * @return <code>true</code> if time is contained within the file
     *         <code>false</code>
     * @throws an IOException if an error occurs while reading the input file
     */
    private boolean isTimeIntoFile(long time, int index) throws IOException {

        String filename = "";
        NetcdfFile nc;
        Array timeArr;
        long time_r0, time_rf;

        try {
            filename = listUFiles.get(index);
            nc = NetcdfDataset.openFile(filename, null);
            timeArr = nc.findVariable(strTime).read();
            time_r0 = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(0)));
            time_rf = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(
                    timeArr.getShape()[0] - 1)));
            nc.close();

            return (time >= time_r0 && time < time_rf);
            /*switch (time_arrow) {
            case 1:
            return (time >= time_r0 && time < time_rf);
            case -1:
            return (time > time_r0 && time <= time_rf);
            }*/
        } catch (IOException e) {
            throw new IOException("Problem reading file " + filename + " : "
                    + e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("Unable to read " + strTime
                    + " variable in file " + filename + " : "
                    + e.getCause());
        }
        //return false;

    }

    /**
     * Determines whether or not the specified time is contained between the ith
     * and the (i+1)th input files.
     * @param time a long, the current time [second] of the simulation
     * @param index an int, the index of the file in the {@code listInputFiles}
     * @return <code>true</code> if time is contained between the two files
     *         <code>false</code> otherwise.
     * @throws an IOException if an error occurs while reading the input files
     */
    private boolean isTimeBetweenFile(long time, int index) throws IOException {

        NetcdfFile nc;
        String filename = "";
        Array timeArr;
        long[] time_nc = new long[2];

        try {
            for (int i = 0; i < 2; i++) {
                filename = listUFiles.get(index + i);
                nc = NetcdfDataset.openFile(filename, null);
                timeArr = nc.findVariable(strTime).read();
                time_nc[i] = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(
                        0)));
                nc.close();
            }
            if (time >= time_nc[0] && time < time_nc[1]) {
                return true;
            }
        } catch (IOException e) {
            throw new IOException("Problem reading file " + filename + " : "
                    + e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("Unable to read " + strTime
                    + " variable in file " + filename + " : "
                    + e.getCause());
        }
        return false;
    }

    /**
     * Finds the index of the dataset time variable such as
     * <code>time(rank) <= time < time(rank + 1)
     *
     * @param time a long, the current time [second] of the simulation
     * @return an int, the current rank of the NetCDF dataset for time dimension
     * @throws an IOException if an error occurs while reading the input file
     *
     * pverley pour chourdin: remplacer ncIn par le fichier OPA concerné.
     */
    private int findCurrentRank(long time) throws IOException {

        int lrank = 0;
        long time_rank;
        Array timeArr;
        try {
            timeArr = ncU.findVariable(strTime).read();
            time_rank = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(lrank)));
            while (time >= time_rank) {
                if (time_arrow < 0 && time == time_rank) {
                    break;
                }
                lrank++;
                time_rank = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(
                        lrank)));
            }
        } catch (IOException e) {
            throw new IOException("Problem reading file " + ncU.getLocation() + " : "
                    + e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("Unable to read " + strTime
                    + " variable in file " + ncU.getLocation() + " : "
                    + e.getCause());
        } catch (ArrayIndexOutOfBoundsException e) {
            lrank = nbTimeRecords;
        }
        lrank = lrank - (time_arrow + 1) / 2;

        return lrank;
    }

    /**
     * Determines whether or not the x-y particle location is on edge of the
     * domain.
     * @param x a double, the x-coordinate
     * @param y a double, the y-coordinate
     * @return <code>true</code> if the particle is on edge of the domain
     *         <code>false</code> otherwise.
     */
    public boolean isOnEdge(double[] pGrid) {
        return ((pGrid[0] > (nx - 2.0f))
                || (pGrid[0] < 1.0f)
                || (pGrid[1] > (ny - 2.0f))
                || (pGrid[1] < 1.0f));
    }

    /**
     * Computes the diffusivity at specified grid point (i, j, depth), using a
     * cubic spline interpolation. The idea behind cubic spline is to draw
     * smooth curves through a number of points, here the values of the
     * vertical turbulent diffusion in water column (i, j).
     *
     * <p>
     * The algorithme used to smooth the profile of diffusivity has been adapted
     * from an article written by Sky McKinley and Megan Levine
     * "Cubic spline interpolation : An introduction into the theory and
     * application of cubic splines." It can be download from
     * {@link http://online.redwoods.cc.ca.us/instruct/darnold/laproj/Fall98/SkyMeg/Proj.PDF}
     * </p>
     *
     * <pre>
     * Let's briefly sum up the drill.
     * 1. We first interpolate in time the values of Kv(t0) and Kv(t1) read in
     * the NetCDF dataset, where t0 and t1 are the values of the time NetCDF
     * variable bounding the specified time.
     * 2. We transform the depth into the corresponding z level and we determine
     * the kth piecewise of the spline corresponding to the given z level such
     * as k <= z < k + 1
     * 3. We compute the polynomial coefficients of the piecewise of the spline
     * contained between [k; k + 1]. Let's take M = Kv''
     * a = (M(k + 1) - M(k)) / 6
     * b = M(k) / 2
     * c = Kv(k + 1) - Kv(k) - (M(k + 1) - M(k)) / 6
     * d = Kv(k);
     * 4. We compute Kv'(z). Let's take dz = z - truncate(z)
     * Kv'(z) = 3.d * a * dz2 + 2.d * b * dz + c;
     * 5. zz = depthToz(depth + 0.5 * Kv'(depth) * dt). dz = zz - truncate(z)
     * Kv(zz) = a * dz3 + b * dz2 + c * dz + d
     * </pre>
     *
     * @param i an int, the i-coordinate
     * @param j an int, the j-coordinate
     * @param z a double, the z-coordinate
     * @param dt a double, the simulation time step [second]
     * @return a double[], the diffusivity and its first derivate at
     * (i, j, depth) {diffKv(depth), Kv(depth)}
     */
    private double[] getKv(int i, int j, double depth, double time, double dt) {

        double diffzKv, Kvzz, ddepth, dz, zz;
        double[] Kv = new double[nz];
        double a, b, c, d;
        double xTime;
        int k;
        double z;
        xTime = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        for (k = nz; k-- > 0;) {
            Kv[k] = (1.d - xTime) * kv_tp0[k][j][i] + xTime * kv_tp1[k][j][i];
        }

        z = depth2z(i, j, depth);
        k = (int) z;
        //dz = z - Math.floor(z);
        ddepth = Math.abs(depth - z_rho_cst[k]);
        /** Compute the polynomial coefficients of the piecewise of the spline
         * contained between [k; k + 1]. Let's take M = Kv''
         * a = (M(k + 1) - M(k)) / 6
         * b = M(k) / 2
         * c = Kv(k + 1) - Kv(k) - (M(k + 1) - M(k)) / 6
         * d = Kv(k);
         */
        a = (DatasetUtil.diff2(Kv, k + 1) - DatasetUtil.diff2(Kv, k)) / 6.d;
        b = DatasetUtil.diff2(Kv, k) / 2.d;
        c = (Kv[k + 1] - Kv[k]) - (DatasetUtil.diff2(Kv, k + 1) + 2.d * DatasetUtil.diff2(Kv, k)) / 6.d;
        d = Kv[k];

        /** Compute Kv'(z)
         * Kv'(z) = 3.d * a * dz2 + 2.d * b * dz + c; */
        diffzKv = c + ddepth * (2.d * b + 3.d * a * ddepth);

        zz = depth2z(i, j, depth + 0.5d * diffzKv * dt);
        dz = zz - Math.floor(z);
        ddepth = Math.abs(depth + 0.5d * diffzKv * dt - z_rho_cst[k]);
        if (dz >= 1.f || dz < 0) {
            k = (int) zz;
            a = (DatasetUtil.diff2(Kv, k + 1) - DatasetUtil.diff2(Kv, k)) / 6.d;
            b = DatasetUtil.diff2(Kv, k) / 2.d;
            c = (Kv[k + 1] - Kv[k])
                    - (DatasetUtil.diff2(Kv, k + 1) + 2.d * DatasetUtil.diff2(Kv, k)) / 6.d;
            d = Kv[k];
        }
        /** Compute Kv(z)
         * Kv(z) = a * dz3 + b * dz2 + c * dz + d;*/
        Kvzz = d + ddepth * (c + ddepth * (b + ddepth * a));
        Kvzz = Math.max(0.d, Kvzz);

        return new double[]{diffzKv, Kvzz};
    }

    /**
     * Computes the vertical turbulent diffusivity , the
     * first derivative and the coefficient to adimensionalize the move due to
     * vertical dispersion at the specified location.
     * <pre>
     * Drill:
     * Particle current location: X(x, y, depth)
     * Let's take i = truncate(x), j = truncate(y)
     * The method interpolates the values of the diffusivity at the surounding
     * points K(i, j, depth) K(i + 1, j, depth) K(i, j + 1, depth)
     * and K(i + 1,  j + 1, depth). Be aware that we are working at a specified
     * depth and not a specified z level for computing the diffusivity (and the
     * first derivative).
     * </pre>
     *
     * @param pGrid a double[], the grid coordinates
     * @param dt a double, the simulation time step [second]
     * @return a double[], the diffusivity and its first derivative at (x, y, z)
     * {diffKv, Kv}
     */
    public double[] getKv(double[] pGrid, double time, double dt) {

        double co, CO = 0.d, Kv = 0.d, diffKv = 0.d, Hz = 0.d;
        double x, y, z, dx, dy;
        int i, j, k;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        double[] kvSpline;
        double depth;

        x = pGrid[0];
        y = pGrid[1];
        z = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));
        depth = z2depth(x, y, z);

        i = (int) x;
        j = (int) y;
        k = (int) Math.round(z);
        dx = x - Math.floor(x);
        dy = y - Math.floor(y);

        if (DEBUG_VDISP) {
            for (int ii = 0; ii < n; ii++) {
                for (int jj = 0; jj < n; jj++) {
                    co = Math.abs((1.d - (double) ii - dx)
                            * (1.d - (double) jj - dy));
                    CO += co;
                    Hz += co * (z_w_cst[k + 1] - z_w_cst[k]);
                }
            }
            if (CO != 0) {
                Hz /= CO;
            }
            return new double[]{0, 1e-2, Hz};
        }

        for (int ii = 0; ii < n; ii++) {
            for (int jj = 0; jj < n; jj++) {
                co = Math.abs((1.d - (double) ii - dx)
                        * (1.d - (double) jj - dy));
                CO += co;
                kvSpline = getKv(i + ii, j + jj, depth, time, dt);
                diffKv += kvSpline[0] * co;
                Kv += kvSpline[1] * co;
                Hz += co * (z_w_cst[k + 1] - z_w_cst[Math.max(k - 1, 0)]);
            }
        }
        if (CO != 0) {
            diffKv /= CO;
            Kv /= CO;
            Hz /= CO;
        }

        return new double[]{diffKv, Kv, Hz};
    }

//////////
// Getters
//////////
    /**
     * Gets the value of the NetCDF time variable just superior (or inferior for
     * backward simulation) to the current time of the simulation.
     * @return a double, the time [second] of the NetCDF time variable strictly
     * superior (or inferior for backward simulation) to the current time of
     * the simulation.
     */
    public static double getTimeTp1() {
        return time_tp1;
    }

    /**
     * Gets the grid dimension in the XI-direction
     * @return an int, the grid dimension in the XI-direction (Zonal)
     */
    public int get_nx() {
        return nx;
    }

    /**
     * Gets the grid dimension in the ETA-direction
     * @return an int, the grid dimension in the ETA-direction (Meridional)
     */
    public int get_ny() {
        return ny;
    }

    /**
     * Gets the grid dimension in the vertical direction
     * @return an int, the grid dimension in the vertical direction
     */
    public int get_nz() {
        return nz;
    }

    /**
     * Gets domain minimum latitude.
     * @return a double, the domain minimum latitude [north degree]
     */
    public double getLatMin() {
        return latMin;
    }

    /**
     * Gets domain maximum latitude.
     * @return a double, the domain maximum latitude [north degree]
     */
    public double getLatMax() {
        return latMax;
    }

    /**
     * Gets domain minimum longitude.
     * @return a double, the domain minimum longitude [east degree]
     */
    public double getLonMin() {
        return lonMin;
    }

    /**
     * Gets domain maximum longitude.
     * @return a double, the domain maximum longitude [east degree]
     */
    public double getLonMax() {
        return lonMax;
    }

    /**
     * Gets domain maximum depth.
     * @return a float, the domain maximum depth [meter]
     */
    public double getDepthMax() {
        return depthMax;
    }

    /**
     * Gets the latitude at (i, j) grid point.
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the latitude [north degree] at (i, j) grid point.
     */
    public double getLat(int i, int j) {
        return latRho[j][i];
    }

    /**
     * Gets the longitude at (i, j) grid point.
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the longitude [east degree] at (i, j) grid point.
     */
    public double getLon(int i, int j) {
        return lonRho[j][i];
    }

    /**
     * Gets the bathymetry at (i, j) grid point.
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the bathymetry [meter] at (i, j) grid point if is in
     * water, return NaN otherwise.
     */
    public double getBathy(int i, int j) {

        double bathy = 0.d;
        if (isInWater(i, j, nz - 1)) {
            for (int k = 0; k < nz; k++) {
                bathy += maskRho[k][j][i] * e3t[k][j][i];
            }
            return bathy;
        }
        return Double.NaN;
    }

    //---------- End of class
    private void test() {

        double z = 10.d + Math.random();
        double depth = 2400.d + 100.d * Math.random();

        System.out.println("  ++++ " + z + " " + depth2z(0, 0, z2depth(0, 0, z)));
        System.out.println("  ++++ " + depth + " " + z2depth(0, 0, depth2z(0, 0, depth)));
    }

    public Number get(String variableName, double[] pGrid, double time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void nextStepTriggered(NextStepEvent e) {

        long time = e.getSource().getTime();

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        zeta_tp0 = zeta_tp1;
        temp_tp0 = temp_tp1;
        salt_tp0 = salt_tp1;
        kv_tp0 = kv_tp1;
        largePhyto_tp0 = largePhyto_tp1;
        largeZoo_tp0 = largeZoo_tp1;
        smallZoo_tp0 = smallZoo_tp1;
        if (z_w_tp1 != null) {
            z_w_tp0 = z_w_tp1;
        }
        rank += time_arrow;

        try {
            if (rank > (nbTimeRecords - 1) || rank < 0) {
                open(indexFile = getIndexNextFile(time, indexFile));
                rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
            }

            setAllFieldsTp1AtTime(rank);
        } catch (IOException ex) {
            Logger.getLogger(NemoDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
