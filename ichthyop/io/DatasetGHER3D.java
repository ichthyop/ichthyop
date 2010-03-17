package ichthyop.io;

/** import java.io */
import java.io.IOException;

/** import netcdf */
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

/** local import */
import ichthyop.util.Constant;
import ichthyop.core.RhoPoint;

/**
 * Specialization of class {@code Dataset} for 3D MARS simulations.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 *
 * @author P.Verley
 * @see ichthyop.io.Dataset
 */
public class DatasetGHER3D extends Dataset {

    double[] dxu;
    double dyv;
    float[] s_rho;
    /**
     * MaskDoublezone: Double zone Sigma = 1 (for deepest zone), Only one sigma level = 0
     * hlim : depth limit foor the definition of the double sigma zone
     * klim : index of the deepest layer of the surface sigma zone
     */
    byte[][] maskdoublesigma;
    double hlim;
    int klim;
    private String strSigma;

///////////////////////////////////
// Definition of overriding methods
///////////////////////////////////
    /**
     * Overrides {@code Dataset#getFieldsName}
     * <br>
     * Gets the general names of the NetCDF variables from the configuration
     * file, plus some specific names for MARS 3D simulation.
     */
    @Override
    void getFieldsName() {

        super.getFieldsName();
        strSigma = Configuration.getStrSigma();
    }

//////////////////////////////////////////////
// Definition of the inherited abstact methods
//////////////////////////////////////////////
    /**
     * Reads longitude and latitude fields in NetCDF dataset
     */
    void readLonLat() {

        Array arrLon, arrLat;
        lonRho = new double[ny][nx];
        latRho = new double[ny][nx];
        try {
            arrLon = ncIn.findVariable(strLon).read();
            arrLat = ncIn.findVariable(strLat).read();
            Index indexLon = arrLon.getIndex();
            Index indexLat = arrLat.getIndex();
            for (int j = 0; j < ny; j++) {
                indexLat.set(j);
                for (int i = 0; i < nx; i++) {
                    indexLon.set(i);
                    lonRho[j][i] = arrLon.getDouble(indexLon);
                    latRho[j][i] = arrLat.getDouble(indexLat);
                }
            }
            arrLon = null;
            arrLat = null;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Reads time non-dependant fields in NetCDF dataset
     */
    void readConstantField() {

        Array arrLon, arrLat, arrH, arrZeta;
        Index index;
        lonRho = new double[ny][nx];
        latRho = new double[ny][nx];
        maskRho = new byte[ny][nx];
        maskdoublesigma = new byte[ny][nx];

        dxu = new double[ny];
        hlim = 0;
        klim = 0;


        try {
            arrLon = ncIn.findVariable(strLon).read(new int[]{ipo},
                    new int[]{nx});
            arrLat = ncIn.findVariable(strLat).read(new int[]{jpo},
                    new int[]{ny});
            arrH = ncIn.findVariable(strBathy).read(new int[]{jpo, ipo},
                    new int[]{ny, nx});
            arrZeta = ncIn.findVariable(strZeta).read(new int[]{0, jpo, ipo},
                    new int[]{1, ny, nx}).reduce();

            if (arrH.getElementType() == double.class) {
                hRho = (double[][]) arrH.copyToNDJavaArray();
            } else {
                hRho = new double[ny][nx];
                index = arrH.getIndex();
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        hRho[j][i] = arrH.getDouble(index.set(j, i));
                    }
                }
            }

            s_rho = (float[]) ncIn.findVariable(strSigma).read().
                    copyToNDJavaArray();

            // Read the Param in ncIn for the double sigma
            hlim = (ncIn.findGlobalAttribute("hlim").getNumericValue()).doubleValue();

            //found the index klim of the limit between the two sigma zones

            for (int k = 1; k < nz; k++) {
                if (s_rho[k] == 0) {
                    klim = k;
                }
            }
            //-----------------------------------------------------------

            Index indexLon = arrLon.getIndex();
            Index indexLat = arrLat.getIndex();
            for (int j = 0; j < ny; j++) {
                indexLat.set(j);
                for (int i = 0; i < nx; i++) {
                    indexLon.set(i);
                    lonRho[j][i] = arrLon.getDouble(indexLon);
                    latRho[j][i] = arrLat.getDouble(indexLat);
                    maskRho[j][i] = (hRho[j][i] == -999.0) ? (byte) 0
                            : (byte) 1;
                    maskdoublesigma[j][i] = (hRho[j][i] > hlim) ? (byte) 1
                            : (byte) 0;
                }
            }

            if (arrZeta.getElementType() == float.class) {
                zeta_tp0 = (float[][]) arrZeta.copyToNDJavaArray();
            } else {
                zeta_tp0 = new float[ny][nx];
                index = arrZeta.getIndex();
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        zeta_tp0[j][i] = arrZeta.getFloat(index.set(j, i));
                    }
                }
            }
            zeta_tp1 = zeta_tp0;



        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InvalidRangeException ex) {
        }

        double[] ptGeo1, ptGeo2;
        for (int j = 0; j < ny; j++) {
            ptGeo1 = grid2Geo(1.5d, (double) j);
            ptGeo2 = grid2Geo(2.5d, (double) j);
            dxu[j] = geodesicDistance(ptGeo1[0], ptGeo1[1], ptGeo2[0], ptGeo2[1]);
        }
        ptGeo1 = grid2Geo(1.d, 1.5d);
        ptGeo2 = grid2Geo(1.d, 2.5d);
        dyv = geodesicDistance(ptGeo1[0], ptGeo1[1], ptGeo2[0], ptGeo2[1]);

    }

    /**
     * Computes the vertical velocity vector.
     *
     * @see ichthyop.io.Dataset#computeW for details about the method
     */
    float[][][] computeW() {

        double[][][] Huon = new double[nz][ny][nx];
        double[][][] Hvom = new double[nz][ny][nx];
        double[][][] z_w_tmp = z_w_tp1;

        //---------------------------------------------------
        // Calculation Coeff Huon & Hvom
        for (int k = nz; k-- > 0;) {
            for (int i = 0; i++ < nx - 1;) {
                for (int j = ny; j-- > 0;) {
                    Huon[k][j][i] = .5d * ((z_w_tmp[k + 1][j][i]
                            - z_w_tmp[k][j][i])
                            + (z_w_tmp[k + 1][j][i - 1]
                            - z_w_tmp[k][j][i - 1])) * dyv
                            * u_tp1[k][j][i - 1];
                }
            }
            for (int i = nx; i-- > 0;) {
                for (int j = 0; j++ < ny - 1;) {
                    Hvom[k][j][i] = .25d * (((z_w_tmp[k + 1][j][i]
                            - z_w_tmp[k][j][i])
                            + (z_w_tmp[k + 1][j - 1][i]
                            - z_w_tmp[k][j - 1][i]))
                            * (dxu[j]
                            + dxu[j - 1]))
                            * v_tp1[k][j - 1][i];
                }
            }
        }

        //---------------------------------------------------
        // Calcultaion of w(i, j, k)
        double[] wrk = new double[nx];
        double[][][] w_double = new double[nz + 1][ny][nx];

        for (int j = ny - 1; j-- > 0;) {
            for (int i = nx; i-- > 0;) {
                w_double[0][j][i] = 0.f;
            }
            for (int k = 0; k++ < nz;) {
                for (int i = nx - 1; i-- > 0;) {
                    w_double[k][j][i] = w_double[k - 1][j][i]
                            + (float) (Huon[k - 1][j][i]
                            - Huon[k
                            - 1][j][i + 1] + Hvom[k
                            - 1][j][i] - Hvom[k - 1][j
                            + 1][i]);
                }
            }
            for (int i = nx; i-- > 0;) {
                wrk[i] = w_double[nz][j][i]
                        / (z_w_tmp[nz][j][i] - z_w_tmp[0][j][i]);
            }
            for (int k = nz; k-- >= 2;) {
                for (int i = nx; i-- > 0;) {
                    w_double[k][j][i] += -wrk[i]
                            * (z_w_tmp[k][j][i] - z_w_tmp[0][j][i]);
                }
            }
            for (int i = nx; i-- > 0;) {
                w_double[nz][j][i] = 0.f;
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
                    w[k][j][i] = (float) (w_double[k][j][i] / (dxu[j] * dyv));
                }
            }
        }

        //---------------------------------------------------
        // Return w
        return w;

    }

    /**
     * Computes the depth at sigma levels disregarding the free
     * surface elevation.
     */
    void getCstSigLevels() {
        double[][][] z_r_tmp = new double[nz][ny][nx];
        double[][][] z_w_tmp = new double[nz + 1][ny][nx];

        double[] s_w = new double[nz + 1]; // coordonées sigma des interfaces
        double[] s_r = new double[nz]; // coordonées sigma du milieu de cellules


        //in this configuration the sigma read is the  of the bottom of the cells (_W)

        s_w[nz] = 1.d;
        s_w[0] = 0.d;
        for (int k = 0; k < nz; k++) {
            s_w[k] = (s_rho[k]);
        }

        for (int k = 0; k < nz; k++) {
            s_r[k] = .5d * (s_w[k + 1] + s_w[k]);
        }

        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                z_w_tmp[0][j][i] = -hRho[j][i];
                for (int k = nz; k-- > klim;) {
                    z_r_tmp[k][j][i] = (s_r[k] - 1.d) * Math.min(hRho[j][i], hlim);
                    z_w_tmp[k + 1][j][i] = (s_w[k + 1] - 1.d) * Math.min(hRho[j][i], hlim);
                }
                for (int k = klim - 1; k-- > 0;) {
                    z_r_tmp[k][j][i] = -hlim + (s_r[k] - 1.d) * (hRho[j][i] - hlim);
                    z_w_tmp[k + 1][j][i] = -hlim + (s_w[k + 1] - 1.d) * (hRho[j][i] - hlim);
                }


            }
        }
        z_rho_cst = new double[nz][ny][nx];
        z_w_cst = new double[nz + 1][ny][nx];

        z_rho_cst = z_r_tmp;
        z_w_cst = z_w_tmp;

        z_w_tp0 = new double[nz + 1][ny][nx];
        z_w_tp1 = new double[nz + 1][ny][nx];

    }

    /**
     * Advects the particle with the model velocity vector, using a Forward
     * Euler scheme.
     *
     * @see ichthyop.io.Dataset#advectEuler for details
     */
    public double[] advectEuler(double[] pGrid, double time, double dt_sec) {

        double co, CO, x, dw, du, dv, x_euler;
        int n = isCloseToCost(pGrid) ? 1 : 2;

        //-----------------------------------------------------------
        // Interpolate the velocity, temperature and salinity fields
        // in the computational grid.

        double ix, jy, kz;
        ix = pGrid[0];
        jy = pGrid[1];
        
        if (isInDoubleZone((int) Math.round(ix), (int) Math.round(jy))) {
            kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));
        } else {
            kz = Math.max(klim, Math.min(pGrid[2], nz - 1.00001f));
        }

        du = 0.d;
        dv = 0.d;
        dw = 0.d;
        x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

        try {
            //-----------------------
            //Get dw
            int i = (int) ix;
            int j = (int) jy;
            int k = (int) Math.round(kz);
            double dx = ix - (double) i;
            double dy = jy - (double) j;
            double dz = kz - (double) k;
            CO = 0.d;
            for (int ii = 0; ii < n; ii++) {
                for (int jj = 0; jj < n; jj++) {
                    for (int kk = 0; kk < 2; kk++) {
                        //if (isInWater(i + ii, j + jj)) {
                        {
                            co = Math.abs((1.d - (double) ii - dx)
                                    * (1.d - (double) jj - dy)
                                    * (.5d - (double) kk - dz));
                            CO += co;
                            x = 0.d;
                            x = (1.d - x_euler) * w_tp0[k + kk][j + jj][i + ii]
                                    + x_euler * w_tp1[k + kk][j + jj][i + ii];
                            dw += 2.d * x * co
                                    / (z_w_tp0[Math.min(k + kk + 1, nz)][j
                                    + jj][i + ii]
                                    - z_w_tp0[Math.max(k + kk - 1, 0)][j
                                    + jj][i + ii]);
                            /*if (Double.isNaN(dw)) {
                            System.out.println("co " + co + " " + (1.d - (double) ii - dx) + " "
                            + (1.d - (double) jj - dy) + " "
                            + (.5d - (double) kk - dz));
                            System.out.println("x_euler " + x_euler);
                            System.out.println("w " + w_tp0[k + kk][j + jj][i + ii] + " " + w_tp1[k + kk][j + jj][i + ii]);
                            System.out.println("zw " + z_w[0][Math.min(k + kk + 1, nz)][j + jj][i + ii] + " " + z_w[0][Math.max(k + kk - 1, 0)][j + jj][i + ii]);
                            System.out.println("kz " + kz + " " + Math.min(k + kk + 1, nz) + " " + Math.max(k + kk - 1, 0));
                            System.exit(0);
                            }*/
                        }
                    }
                }
            }
            if (Double.isNaN(dw)) {
                System.out.println(dw);
            }
            dw *= dt_sec;
            if (CO != 0) {
                dw /= CO;
            }

            //------------------------
            // Get du
            //kz = Math.min(kz, nz - 1.00001f);
            i = (int) Math.round(ix);
            k = (int) kz;
            dx = ix - (double) i;
            dz = kz - (double) k;
            CO = 0.d;
            for (int ii = 0; ii < 2; ii++) {
                for (int jj = 0; jj < n; jj++) {
                    for (int kk = 0; kk < 2; kk++) {
                        //if (isInWater(i + ii, j + jj)) {
                        {
                            co = Math.abs((.5d - (double) ii - dx)
                                    * (1.d - (double) jj - dy)
                                    * (1.d - (double) kk - dz));
                            CO += co;
                            x = 0.d;
                            x = (1.d - x_euler) * u_tp0[k + kk][j + jj][i + ii
                                    - 1]
                                    + x_euler * u_tp1[k + kk][j + jj][i + ii - 1];
                            du += x * co / dxu[j + jj];
                        }
                    }
                }
            }
            du *= dt_sec;
            if (CO != 0) {
                du /= CO;
            }

            //-------------------------
            // Get dv
            i = (int) ix;
            j = (int) Math.round(jy);
            dx = ix - (double) i;
            dy = jy - (double) j;
            CO = 0.d;
            for (int kk = 0; kk < 2; kk++) {
                for (int jj = 0; jj < 2; jj++) {
                    for (int ii = 0; ii < n; ii++) {
                        //if (isInWater(i + ii, j + jj)) {
                        {
                            co = Math.abs((1.d - (double) ii - dx)
                                    * (.5d - (double) jj - dy)
                                    * (1.d - (double) kk - dz));
                            CO += co;
                            x = 0.d;
                            x = (1.d - x_euler) * v_tp0[k + kk][j + jj - 1][i
                                    + ii]
                                    + x_euler * v_tp1[k + kk][j + jj - 1][i + ii];
                            dv += x * co / dyv;
                        }
                    }
                }
            }
            dv *= dt_sec;
            if (CO != 0) {
                dv /= CO;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException(
                    "Problem interpolating velocity fields : " + e.getMessage());
        }
        if (du > Constant.THRESHOLD_CFL) {
            System.err.println("! WARNING : CFL broken for u " + (float) du);
        }
        if (dv > Constant.THRESHOLD_CFL) {
            System.err.println("! WARNING : CFL broken for v " + (float) dv);
        }

        return (new double[]{du, dv, dw});
    }

    /**
     * Adimensionalizes the given magnitude at the specified grid location.
     */
    public double adimensionalize(double number, double xRho, double yRho) {
        return 2.d * number / (dyv + dxu[(int) Math.round(yRho)]);
    }

    /**
     * Gets cell dimension [meter] in the XI-direction.
     */
    double getdxi(int j, int i) {
        return dxu[j];
    }

    /**
     * Gets cell dimension [meter] in the ETA-direction.
     */
    double getdeta(int j, int i) {
        return dyv;
    }

    public boolean isInDoubleZone(int i, int j) {
        return (maskdoublesigma[j][i] > 0);
    }

    /**
     * Determines whether the specified {@code RohPoint} is in water.
     * @param ptRho the RhoPoint
     * @return <code>true</code> if the {@code RohPoint} is in water,
     *         <code>false</code> otherwise.
     * @see #isInWater(int i, int j)
     */
    public boolean isInDoublezone(RhoPoint ptRho) {
        try {
            return (maskdoublesigma[(int) Math.round(ptRho.getY())][(int) Math.round(
                    ptRho.getX())] > 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
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
     *         <code>false</code> otherwise.
     */
    @Override
    boolean isCloseToCost(double[] pGrid) {

        int i, j, k, ii, jj, kk;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        k = (int) pGrid[2];
        if (k < klim) {
            ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
            jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
            return !(isInDoubleZone(i + ii, j) && isInDoubleZone(i + ii, j + jj)
                    && isInDoubleZone(i, j + jj));
        } else {
            ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
            jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
            return !(isInWater(i + ii, j) && isInWater(i + ii, j + jj)
                    && isInWater(i, j + jj));
        }
    }

    /**
     * Computes the depth  of the specified sigma level and the x-y particle
     * location.
     * @param xRho a double, x-coordinate of the grid point
     * @param yRho a double, y-coordinate of the grid point
     * @param k an int, the index of the sigma level
     * @return a double, the depth [meter] at (x, y, k)
     */
    @Override
    public double getDepth(double xRho, double yRho, int k) {

        final int i = (int) xRho;
        final int j = (int) yRho;
        double hh = 0.d;
        final double dx = (xRho - i);
        final double dy = (yRho - j);
        double co = 0.d;
        //getdepth est souvent utilisé avec un 0 explicite pour avoir la profondeur du fond
        //ce qui suit permet de guarder cette utilisation
        if (k == 0) {
            k = isInDoubleZone(i, j) ? 0 : klim;
        }
        if (k < klim) {
            for (int ii = 0; ii < 2; ii++) {
                for (int jj = 0; jj < 2; jj++) {
                    if (isInDoubleZone(i + ii, j + jj)) {
                        co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                        double z_r = 0.d;
                        z_r = z_rho_cst[k][j + jj][i + ii];
                        hh += co * z_r;
                    }
                }
            }
        } else {
            for (int ii = 0; ii < 2; ii++) {
                for (int jj = 0; jj < 2; jj++) {
                    if (isInWater(i + ii, j + jj)) {
                        co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                        double z_r = 0.d;
                        z_r = z_rho_cst[k][j + jj][i + ii] + (double) zeta_tp0[j
                                + jj][i + ii]
                                * (1.d + z_rho_cst[k][j + jj][i + ii] / Math.min(hRho[j + jj][i
                                + ii], hlim));
                        hh += co * z_r;
                    }
                }
            }
        }
        return (hh);
    }

        /**
     * Computes the depth at w points, taking account of the free surface
     * elevation.
     * @return a double[][][], the depth at w point.
     */
    @Override
    double[][][] getSigLevels() {

        //-----------------------------------------------------
        // Daily recalculation of z_w and z_r with zeta

        double[][][] z_w_tmp = new double[nz + 1][ny][nx];
        double[][][] z_w_cst_tmp = z_w_cst;

        //System.out.print("Calculation of the s-levels\n");

        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                if (zeta_tp1[j][i] == 999.f) {
                    zeta_tp1[j][i] = 0.f;
                }
                for (int k = klim; k < nz + 1; k++) {
                    z_w_tmp[k][j][i] = z_w_cst_tmp[k][j][i] + zeta_tp1[j][i]
                            * (1.f + z_w_cst_tmp[k][j][i] / Math.min(hRho[j][i],hlim));
                }
                for (int k = 0; k < klim; k++) {
                    z_w_tmp[k][j][i] = z_w_cst_tmp[k][j][i] ;
                }
            }
        }
        z_w_cst_tmp = null;
        return z_w_tmp;
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
    @Override
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
        int i = (int) Math.round(pGrid[0]);
        int j = (int) Math.round(pGrid[1]);
        double kz = Math.max((double) (isInDoubleZone(i, j) ? 0 : klim), Math.min(pGrid[2], (double) nz - 1.00001f));
        int k = (int) kz;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double dz = kz - (double) k;
        tp = 0.d;
        CO = 0.d;
        int iiinit=0;
        int jjinit=0;
        int ni=n;
        int nj=n;
        if ((ni>1) & (dx<0)){
            iiinit=-1;
            ni=0;
        }
        if ((nj>1) & (dy<0)){
            jjinit=-1;
            nj=0;
        }
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = jjinit; jj < nj; jj++) {
                for (int ii = iiinit; ii < ni; ii++) {
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
        if (tp > 100) {
            System.out.println("tp");
            System.out.println(tp);
            System.out.println("pGrid");
            System.out.println(pGrid[0]);
            System.out.println(pGrid[1]);
            System.out.println(pGrid[2]);
            System.out.println(i);
          System.out.println(j);
            System.out.println("isclose to cost");
          System.out.println(isCloseToCost(pGrid));
            System.out.println("isin water i , j");
            System.out.println(isInWater(i,j));
            System.out.println(getDepth(pGrid[0], pGrid[1], k));
          // System.out.println(isInWater(pGrid));
         //   System.out.println("time");
         //   System.out.println(time);
            System.out.println("kz");
           System.out.println(kz);
           System.out.println("klim");
           System.out.println(klim);
          //  System.out.println(isInDoubleZone(i,j));
          //  System.out.println( temp_tp0[k ][j ][i ]);
                        
        //    System.out.println("gettem levels lu dans GHER");
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
    @Override
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
        int i = (int) Math.round(pGrid[0]);
        int j = (int) Math.round(pGrid[1]);
        double kz = Math.max((double) (isInDoubleZone(i, j) ? 0 : klim), Math.min(pGrid[2], (double) nz - 1.00001f));
        int k = (int) kz;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double dz = kz - (double) k;
        int iiinit=0;
        int jjinit=0;
        int ni=n;
        int nj=n;
        if ((ni>1) & (dx<0)){
            iiinit=-1;
            ni=0;
        }
        if ((nj>1) & (dy<0)){
            jjinit=-1;
            nj=0;
        }
        sal = 0.d;
        CO = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = jjinit; jj < nj; jj++) {
                for (int ii = iiinit; ii < ni; ii++) {
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
    @Override
    public double[] getPlankton(double[] pGrid, double time) {

        if (!FLAG_PLANKTON) {
            return new double[]{Double.NaN, Double.NaN, Double.NaN};
        }

        double co, CO, x, frac, largePhyto, smallZoo, largeZoo;
        int n = isCloseToCost(pGrid) ? 1 : 2;

        frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

        //-----------------------------------------------------------
        // Interpolate the plankton concentration fields
        // in the computational grid.
        int i = (int) Math.round(pGrid[0]);
        int j = (int) Math.round(pGrid[1]);
        final double kz = Math.max((double) (isInDoubleZone(i, j) ? 0 : klim),
                Math.min(pGrid[2],
                (double) nz - 1.00001f));
        int k = (int) kz;
        //System.out.println("i " + i + " j " + j + " k " + k);
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double dz = kz - (double) k;
        int iiinit=0;
        int jjinit=0;
        int ni=n;
        int nj=n;
        if ((ni>1) & (dx<0)){
            iiinit=-1;
            ni=0;
        }
        if ((nj>1) & (dy<0)){
            jjinit=-1;
            nj=0;
        }
        largePhyto = 0.d;
        smallZoo = 0.d;
        largeZoo = 0.d;
        CO = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = jjinit; jj < nj; jj++) {
                for (int ii = iiinit; ii < ni; ii++) {
                    if (isInWater(i + ii, j + jj)) {
                        co = Math.abs((1.d - (double) ii - dx)
                                * (1.d - (double) jj - dy)
                                * (1.d - (double) kk - dz));
                        CO += co;
                        x = 0.d;
                        x = (1.d - frac) * largePhyto_tp0[k + kk][j + jj][i
                                + ii] + frac * largePhyto_tp1[k + kk][j + jj][i + ii];
                        largePhyto += x * co;
                        x = (1.d - frac) * smallZoo_tp0[k + kk][j + jj][i + ii] + frac * smallZoo_tp1[k + kk][j + jj][i + ii];
                        smallZoo += x * co;
                        x = (1.d - frac) * largeZoo_tp0[k + kk][j + jj][i + ii] + frac * largeZoo_tp1[k + kk][j + jj][i + ii];
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
    //---------- End of class
}
