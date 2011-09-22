/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import java.io.IOException;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
abstract class Mars3dCommon extends MarsCommon {

    /**
     * Vertical grid dimension
     */
    int nz;
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
     * Depth at rho point
     */
    static double[][][] z_rho_cst;
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
    static double[][][] z_w_cst;
    /**
     * Name of the Dimension in NetCDF file
     */
    static String strZDim, strZeta, strSigma;
    /**
     * 
     */
    double[] s_rho, s_w;
    /*
     *
     */
    private String strHC, strA, strB;

    @Override
    public void setUp() throws Exception {

        /* Call the common method */
        super.setUp();

        /* Copute sigma levels */
        getCstSigLevels();
        z_w_tp0 = getSigLevels();
    }

    public boolean is3D() {
        return true;
    }

    public int get_nz() {
        return nz;
    }

    @Override
    void loadParameters() {

        /* Call the common method */
        super.loadParameters();

        /* Read specific 3D variable names */
        strZDim = getParameter("field_dim_z");
        strZeta = getParameter("field_var_zeta");
        strSigma = getParameter("field_var_sigma");

        /* Read specifi generalized sigma parameters for MARS V8 */
        try {
            strHC = getParameter("field_var_hc");
            strA = getParameter("field_var_a");
            strB = getParameter("field_var_b");
        } catch (Exception ex) {
            strHC = strA = strB = null;
            getLogger().warning("{Dataset} Could not find generalized sigma level parameters in the configuration file. Simple sigma levels will be used then.");
        }

    }

    /**
     * Computes the depth at sigma levels disregarding the free
     * surface elevation.
     */
    void getCstSigLevels() throws IOException {

        s_rho = new double[nz];
        s_w = new double[nz + 1];

        /* read sigma levels */
        try {
            Array arrSrho = ncIn.findVariable(strSigma).read();
            Index index = arrSrho.getIndex();
            for (int k = 0; k < nz; k++) {
                s_rho[k] = arrSrho.getDouble(index.set(k));
            }
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading sigma levels. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        if (s_rho[nz - 1] > 0) {
            for (int k = 0; k < s_rho.length; k++) {
                s_rho[k] -= 1.d;
            }
        }

        for (int k = 1; k < nz; k++) {
            s_w[k] = .5d * (s_rho[k - 1] + s_rho[k]);
        }
        s_w[nz] = 0.d;
        s_w[0] = -1.d;

        if ((strHC != null) && (null != ncIn.findVariable(strHC))) {
            getLogger().info("{Dataset} Generalized sigma levels detected.");
            getSigLevelsV8();
        } else {
            getSigLevelsV6();
        }
    }

    private void getSigLevelsV6() {

        double[][][] z_r_tmp = new double[nz][ny][nx];
        double[][][] z_w_tmp = new double[nz + 1][ny][nx];

        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                z_w_tmp[0][j][i] = -hRho[j][i];
                for (int k = nz; k-- > 0;) {
                    z_r_tmp[k][j][i] = s_rho[k] * hRho[j][i];
                    z_w_tmp[k + 1][j][i] = s_w[k + 1] * hRho[j][i];
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

    private void getSigLevelsV8() throws IOException {

        float hc[][];
        double a, b;
        double[] Cs_r = new double[nz];
        double[] Cs_w = new double[nz + 1];
        try {
            //-----------------------------------------------------------
            // Read hc, Cs_r and Cs_w in the NetCDF file.
            Array arrHc = ncIn.findVariable(strHC).read(new int[]{jpo, ipo}, new int[]{ny, nx});
            hc = (float[][]) arrHc.copyToNDJavaArray();
        } catch (Exception ex) {
            IOException ioex = new IOException("{Dataset} Error reading hc variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            a = ncIn.findVariable(strA).readScalarDouble();
        } catch (IOException ex) {
            IOException ioex = new IOException("{Dataset} Error reading theta, surface control parameter. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            b = ncIn.findVariable(strB).readScalarDouble();
        } catch (IOException ex) {
            IOException ioex = new IOException("{Dataset} Error reading b, bottom control parameter. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        //-----------------------------------------------------------
        // Calculation of Cs_r and Cs_w, the streching functions
        for (int k = nz; k-- > 0;) {
            Cs_r[k] = (1.d - b) * Math.sinh(a * s_rho[k]) / Math.sinh(a)
                    + b * (Math.tanh(a * (s_rho[k] + 0.5d)) / (2 * Math.tanh(0.5d * a)) - 0.5d);
            Cs_w[k + 1] = (1.d - b) * Math.sinh(a * s_w[k + 1]) / Math.sinh(a)
                    + b * (Math.tanh(a * (s_w[k + 1] + 0.5d)) / (2 * Math.tanh(0.5d * a)) - 0.5d);
        }
        Cs_w[0] = -1.d;

        //------------------------------------------------------------
        // Calculation of z_w , z_r
        double[][][] z_r_tmp = new double[nz][ny][nx];
        double[][][] z_w_tmp = new double[nz + 1][ny][nx];


        // OLD: z_unperturbated = hc * (sc - Cs) + Cs * h

        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                z_w_tmp[0][j][i] = -hRho[j][i];
                for (int k = nz; k-- > 0;) {
                    z_r_tmp[k][j][i] = hc[j][i] * (s_rho[k] - Cs_r[k]) + Cs_r[k] * hRho[j][i];
                    z_w_tmp[k + 1][j][i] = hc[j][i] * (s_w[k + 1] - Cs_w[k + 1]) + Cs_w[k + 1] * hRho[j][i];
                }
                z_w_tmp[nz][j][i] = 0.d;
            }
        }

        z_rho_cst = z_r_tmp;
        z_w_cst = z_w_tmp;

        z_w_tp0 = new double[nz + 1][ny][nx];
        z_w_tp1 = new double[nz + 1][ny][nx];
    }

    @Override
    void readConstantField() throws Exception {

        /* Call the common method */
        super.readConstantField();

        /* read zeta ocean free surface */
        Array arrZeta = null;
        try {
            arrZeta = ncIn.findVariable(strZeta).read(new int[]{0, jpo, ipo}, new int[]{1, ny, nx}).reduce();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading ocean free surface elevation. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }


        if (arrZeta.getElementType() == float.class) {
            zeta_tp0 = (float[][]) arrZeta.copyToNDJavaArray();
        } else {
            zeta_tp0 = new float[ny][nx];
            Index index = arrZeta.getIndex();
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    zeta_tp0[j][i] = arrZeta.getFloat(index.set(j, i));
                }
            }
        }
        zeta_tp1 = zeta_tp0;
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     * @throws an IOException if an error occurs while reading the dimensions.
     */
    @Override
    void getDimNC() throws IOException {

        super.getDimNC();
        /* read the vertical dimension */
        try {
            nz = ncIn.findDimension(strZDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Failed to read dataset vertical dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    public double depth2z(double x, double y, double depth) {

        //-----------------------------------------------
        // Return z[grid] corresponding to depth[meters]
        double z = 0.d;
        int lk = nz - 1;
        while ((lk > 0) && (getDepth(x, y, lk) > depth)) {
            lk--;
        }
        if (lk == (nz - 1)) {
            z = (double) lk;
        } else {
            double pr = getDepth(x, y, lk);
            z = Math.max(0.d,
                    (double) lk
                    + (depth - pr) / (getDepth(x, y, lk + 1) - pr));
        }
        return (z);
    }

    public double z2depth(double x, double y, double z) {

        final double kz = Math.max(0.d, Math.min(z, (double) nz - 1.00001f));
        final int i = (int) Math.floor(x);
        final int j = (int) Math.floor(y);
        final int k = (int) Math.floor(kz);
        double depth = 0.d;
        final double dx = x - (double) i;
        final double dy = y - (double) j;
        final double dz = kz - (double) k;
        double co = 0.d;
        double z_r;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    co = Math.abs((1.d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    if (isInWater(i + ii, j + jj)) {
                        z_r = z_rho_cst[k + kk][j + jj][i + ii]
                                + (double) zeta_tp0[j + jj][i + ii] * (1.d + s_rho[k + kk]);
                        depth += co * z_r;
                    }
                }
            }
        }
        return depth;
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
                    co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (.5d - (double) kk - dz));
                    CO += co;
                    x = (1.d - x_euler) * w_tp0[k + kk][j + jj][i + ii] + x_euler * w_tp1[k + kk][j + jj][i + ii];
                    dw += 2.d * x * co / (z_w_tp0[Math.min(k + kk + 1, nz)][j + jj][i + ii] - z_w_tp0[Math.max(k + kk - 1, 0)][j + jj][i + ii]);
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
                    x = (1.d - x_euler) * v_tp0[k + kk][j + jj - 1][i + ii] + x_euler * v_tp1[k + kk][j + jj - 1][i + ii];
                    dv += 2.d * x * co / (dyv[j + jj - 1][i + ii] + dyv[j + jj][i + ii]);
                }
            }
        }
        if (CO != 0) {
            dv /= CO;
        }
        return dv;
    }

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
                    x = (1.d - x_euler) * u_tp0[k + kk][j + jj][i + ii - 1] + x_euler * u_tp1[k + kk][j + jj][i + ii - 1];
                    du += 2.d * x * co / (dxu[j + jj][i + ii - 1] + dxu[j + jj][i + ii]);
                }
            }
        }
        if (CO != 0) {
            du /= CO;
        }
        return du;
    }

    double getDepth(double xRho, double yRho, int k) {

        final int i = (int) xRho;
        final int j = (int) yRho;
        double hh = 0.d;
        final double dx = (xRho - i);
        final double dy = (yRho - j);
        double co = 0.d;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                if (isInWater(i + ii, j + jj)) {
                    co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                    double z_r = 0.d;
                    z_r = z_rho_cst[k][j + jj][i + ii]
                            + (double) zeta_tp0[j + jj][i + ii] * (1.d + s_rho[k]);
                    hh += co * z_r;
                }
            }
        }
        return (hh);
    }

    void setAllFieldsTp1AtTime(int rank) throws Exception {

        int[] origin = new int[]{rank, 0, jpo, ipo};
        double time_tp0 = time_tp1;


        try {
            u_tp1 = (float[][][]) ncIn.findVariable(strU).read(origin, new int[]{1, nz, ny, (nx - 1)}).reduce().copyToNDJavaArray();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading U velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }


        try {
            v_tp1 = (float[][][]) ncIn.findVariable(strV).read(origin, new int[]{1, nz, (ny - 1), nx}).reduce().copyToNDJavaArray();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading V velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            Array xTimeTp1 = ncIn.findVariable(strTime).read();
            time_tp1 = xTimeTp1.getDouble(xTimeTp1.getIndex().set(rank));
            time_tp1 -= time_tp1 % 100;
            xTimeTp1 = null;
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            zeta_tp1 = (float[][]) ncIn.findVariable(strZeta).read(new int[]{rank, 0, 0}, new int[]{1, ny, nx}).reduce().copyToNDJavaArray();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading ocean free surface elevation. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
        for (RequiredVariable variable : requiredVariables.values()) {
            variable.nextStep(readVariable(ncIn, variable.getName(), rank), time_tp1, dt_HyMo);
        }
        z_w_tp1 = getSigLevels();
        w_tp1 = computeW();
    }

    float[][][] computeW() {

        double[][][] Huon = new double[nz][ny][nx];
        double[][][] Hvom = new double[nz][ny][nx];
        double[][][] z_w_tmp = z_w_tp1;

        //---------------------------------------------------
        // Calculation Coeff Huon & Hvom
        for (int k = nz; k-- > 0;) {
            for (int i = 0; i++ < nx - 1;) {
                for (int j = ny; j-- > 0;) {
                    Huon[k][j][i] = .25d * ((z_w_tmp[k + 1][j][i] - z_w_tmp[k][j][i])
                            + (z_w_tmp[k + 1][j][i - 1] - z_w_tmp[k][j][i - 1]))
                            * (dyv[j][i] + dyv[j][i - 1])
                            * u_tp1[k][j][i - 1];
                }
            }
            for (int i = nx; i-- > 0;) {
                for (int j = 0; j++ < ny - 1;) {
                    Hvom[k][j][i] = .25d * (((z_w_tmp[k + 1][j][i] - z_w_tmp[k][j][i])
                            + (z_w_tmp[k + 1][j - 1][i] - z_w_tmp[k][j - 1][i]))
                            * (dxu[j][i] + dxu[j - 1][i]))
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
                    w[k][j][i] = (float) (w_double[k][j][i] / (dxu[j][i] * dyv[j][i]));
                }
            }
        }

        //---------------------------------------------------
        // Return w
        return w;

    }

    double[][][] getSigLevels() {

        //-----------------------------------------------------
        // Daily recalculation of z_w and z_r with zeta

        double[][][] z_w_tmp = new double[nz + 1][ny][nx];
        double[][][] z_w_cst_tmp = z_w_cst;

        //System.out.print("Calculation of the s-levels\n");

        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                if (Math.abs(zeta_tp1[j][i]) > 10.f) {
                    zeta_tp1[j][i] = 0.f;
                }
                for (int k = 0; k < nz + 1; k++) {
                    z_w_tmp[k][j][i] = z_w_cst_tmp[k][j][i] + zeta_tp1[j][i] * (1.d + s_w[k]);
                }
            }
        }
        z_w_cst_tmp = null;
        return z_w_tmp;
    }

    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        Variable variable = nc.findVariable(name);
        int[] origin = null, shape = null;
        switch (variable.getShape().length) {
            case 4:
                origin = new int[]{rank, 0, jpo, ipo};
                shape = new int[]{1, nz, ny, nx};
                break;
            case 2:
                origin = new int[]{jpo, ipo};
                shape = new int[]{ny, nx};
                break;
            case 3:
                if (!variable.isUnlimited()) {
                    origin = new int[]{0, jpo, ipo};
                    shape = new int[]{nz, ny, nx};
                } else {
                    origin = new int[]{rank, jpo, ipo};
                    shape = new int[]{1, ny, nx};
                }
                break;
        }

        return variable.read(origin, shape).reduce();
    }
}
