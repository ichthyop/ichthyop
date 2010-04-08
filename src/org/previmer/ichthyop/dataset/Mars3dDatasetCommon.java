/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.logging.Level;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
abstract class Mars3dDatasetCommon extends MarsDatasetCommon {

    /**
     * Vertical grid dimension
     */
    int nz;
    /**
     * Number of time records in current NetCDF file
     */
    static int nbTimeRecords;
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
    static float[][][] salt_tp1;
    /**
     * Water salinity at current time
     */
    static float[][][] salt_tp0;
    /**
     * Water temperature at current time
     */
    static float[][][] temp_tp0;
    /**
     * Water temperature at time t + dt
     */
    static float[][][] temp_tp1;
    /**
     * Vertical diffusion coefficient at time t + dt
     */
    float[][][] kv_tp1;
    /**
     * Vertical diffusion coefficient at current time
     */
    float[][][] kv_tp0;
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
     * Time step [second] between two records in NetCDF dataset
     */
    static double dt_HyMo;
    /**
     * Time t + dt expressed in seconds
     */
    static double time_tp1;
    /**
     * 
     */
    static NetcdfFile ncIn;
    /**
     * Name of the Dimension in NetCDF file
     */
    static String strLonDim, strLatDim, strZDim, strTimeDim;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strU, strV, strTp, strSal, strTime, strZeta;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strLon, strLat, strBathy;
    /**
     * Name of the Variable in NetCDF file
     */
    String strSigma;
    /**
     * Current rank in NetCDF dataset
     */
    static int rank;
    /**
     * Determines whether or not the temperature field should be read in the
     * NetCDF file, function of the user's options.
     */
    static boolean FLAG_TP;
    /**
     * Determines whether or not the salinity field should be read in the
     * NetCDF file, function of the user's options.
     */
    static boolean FLAG_SAL;
    /**
     * Determines whether or not the turbulent diffusivity should be read in the
     * NetCDF file, function of the user's options.
     */
    static boolean FLAG_VDISP;
    float[] s_rho;

    public boolean is3D() {
        return true;
    }

    public int get_nz() {
        return nz;
    }

    void loadParameters() {

        strLonDim = getParameter("field_dim_lon");
        strLatDim = getParameter("field_dim_lat");
        strZDim = getParameter("field_dim_z");
        strTimeDim = getParameter("field_dim_time");
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strBathy = getParameter("field_var_bathy");
        strU = getParameter("field_var_u");
        strV = getParameter("field_var_v");
        strZeta = getParameter("field_var_zeta");
        strTp = getParameter("field_var_temp");
        //strSal = getParameter("Averaged salinity");
        strTime = getParameter("field_var_time");
        strSigma = getParameter("field_var_sigma");
    }

    int findCurrentRank(long time) throws IOException {

        int lrank = 0;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());
        long time_rank;
        Array timeArr;
        try {
            timeArr = ncIn.findVariable(strTime).read();
            time_rank = DatasetUtil.skipSeconds(
                    timeArr.getLong(timeArr.getIndex().set(lrank)));
            while (time >= time_rank) {
                if (time_arrow < 0 && time == time_rank) {
                    break;
                }
                lrank++;
                time_rank = DatasetUtil.skipSeconds(
                        timeArr.getLong(timeArr.getIndex().set(lrank)));
            }
        } catch (IOException e) {
            throw new IOException("Problem reading file " + ncIn.getLocation().toString() + " : "
                    + e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("Unable to read " + strTime
                    + " variable in file " + ncIn.getLocation().toString() + " : "
                    + e.getCause());
        } catch (ArrayIndexOutOfBoundsException e) {
            lrank = nbTimeRecords;
        }
        lrank = lrank - (time_arrow + 1) / 2;

        return lrank;
    }

    /**
     * Computes the depth at sigma levels disregarding the free
     * surface elevation.
     */
    void getCstSigLevels() throws IOException {

        double[][][] z_r_tmp = new double[nz][ny][nx];
        double[][][] z_w_tmp = new double[nz + 1][ny][nx];

        double[] s_w = new double[nz + 1];

        s_w[nz] = 1.d;
        s_w[0] = 0.d;
        for (int k = 1; k < nz; k++) {
            s_w[k] = .5d * (s_rho[k - 1] + s_rho[k]);
        }

        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                z_w_tmp[0][j][i] = -hRho[j][i];
                for (int k = nz; k-- > 0;) {
                    z_r_tmp[k][j][i] = ((double) s_rho[k] - 1.d) * hRho[j][i];
                    z_w_tmp[k + 1][j][i] = (s_w[k + 1] - 1.d) * hRho[j][i];
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

    void readConstantField() throws IOException {

        Array arrLon, arrLat, arrH, arrZeta;
        Index index;
        lonRho = new double[nx];
        latRho = new double[ny];
        maskRho = new byte[ny][nx];
        dxu = new double[ny];

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

            Index indexLon = arrLon.getIndex();
            Index indexLat = arrLat.getIndex();
            for (int j = 0; j < ny; j++) {
                indexLat.set(j);
                latRho[j] = arrLat.getDouble(indexLat);
            }
            for (int i = 0; i < nx; i++) {
                indexLon.set(i);
                lonRho[i] = arrLon.getDouble(indexLon);
            }
            for (int j = 0; j < ny; j++) {
                indexLat.set(j);
                for (int i = 0; i < nx; i++) {
                    indexLon.set(i);
                    maskRho[j][i] = (hRho[j][i] == -999.0)
                            ? (byte) 0
                            : (byte) 1;
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

            s_rho = (float[]) ncIn.findVariable(strSigma).read().copyToNDJavaArray();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InvalidRangeException ex) {
        }

        double[] ptGeo1, ptGeo2;
        for (int j = 0; j < ny; j++) {
            ptGeo1 = xy2lonlat(1.5d, (double) j);
            ptGeo2 = xy2lonlat(2.5d, (double) j);
            dxu[j] = DatasetUtil.geodesicDistance(ptGeo1[0], ptGeo1[1], ptGeo2[0], ptGeo2[1]);
        }
        ptGeo1 = xy2lonlat(1.d, 1.5d);
        ptGeo2 = xy2lonlat(1.d, 2.5d);
        dyv = DatasetUtil.geodesicDistance(ptGeo1[0], ptGeo1[1], ptGeo2[0], ptGeo2[1]);
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     * @throws an IOException if an error occurs while reading the dimensions.
     */
    void getDimNC() throws IOException {

        try {
            nx = ncIn.findDimension(strLonDim).getLength();
            ny = ncIn.findDimension(strLatDim).getLength();
            nz = ncIn.findDimension(strZDim).getLength();
        } catch (NullPointerException e) {
            e.printStackTrace();
            //throw new IOException("Problem reading dimensions from dataset " + ncIn.getLocation() + " : " + e.getMessage());
        }
        //Logger.getLogger(getClass().getName()).info("nx " + nx + " - ny " + ny + " - nz " + nz);
        ipo = jpo = 0;
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
                        z_r = z_rho_cst[k + kk][j + jj][i + ii] + (double) zeta_tp0[j + jj][i + ii]
                                * (1.d + z_rho_cst[k + kk][j + jj][i + ii] / hRho[j
                                + jj][i + ii]);
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
                    dv += x * co / dyv;
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
                    du += x * co / dxu[j + jj];
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
                    z_r = z_rho_cst[k][j + jj][i + ii] + (double) zeta_tp0[j
                            + jj][i + ii]
                            * (1.d + z_rho_cst[k][j + jj][i + ii] / hRho[j + jj][i
                            + ii]);
                    hh += co * z_r;
                }
            }
        }
        return (hh);
    }

    public double getTemperature(double[] pGrid, double time) {

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

    public double getSalinity(double[] pGrid, double time) {

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

        for (int ii = 0; ii < n; ii++) {
            for (int jj = 0; jj < n; jj++) {
                co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
                CO += co;
                kvSpline = getKv(i + ii, j + jj, depth, time, dt);
                diffKv += kvSpline[0] * co;
                Kv += kvSpline[1] * co;
                Hz += co * (z_w_tp0[k + 1][j + jj][i + ii] - z_w_tp0[Math.max(k - 1, 0)][j + jj][i + ii]);
            }
        }
        if (CO != 0) {
            diffKv /= CO;
            Kv /= CO;
            Hz /= CO;
        }

        return new double[]{diffKv, Kv, Hz};
    }

    double[] getKv(int i, int j, double depth, double time, double dt) {

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

        z = Math.min(depth2z(i, j, depth), nz - 1.00001f);
        k = (int) z;
        //dz = z - Math.floor(z);
        ddepth = depth - z_rho_cst[k][j][i];
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

        zz = Math.min(depth2z(i, j, depth + 0.5d * diffzKv * dt), nz - 1.00001f);
        dz = zz - Math.floor(z);
        if (dz >= 1.f || dz < 0) {
            k = (int) zz;
            a = (diff2(Kv, k + 1) - diff2(Kv, k)) / 6.d;
            b = diff2(Kv, k) / 2.d;
            c = (Kv[k + 1] - Kv[k])
                    - (diff2(Kv, k + 1) + 2.d * diff2(Kv, k)) / 6.d;
            d = Kv[k];
        }
        ddepth = depth + 0.5d * diffzKv * dt - z_rho_cst[k][j][i];
        /** Compute Kv(z)
         * Kv(z) = a * dz3 + b * dz2 + c * dz + d;*/
        Kvzz = d + ddepth * (c + ddepth * (b + ddepth * a));
        Kvzz = Math.max(0.d, Kvzz);

        return new double[]{diffzKv, Kvzz};
    }

    double diff2(double[] X, int k) {

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

    void setAllFieldsTp1AtTime(int rank) throws IOException {

        int[] origin = new int[]{rank, 0, jpo, ipo};
        double time_tp0 = time_tp1;

        try {
            u_tp1 = (float[][][]) ncIn.findVariable(strU).read(origin, new int[]{1, nz, ny, (nx - 1)}).reduce().copyToNDJavaArray();

            v_tp1 = (float[][][]) ncIn.findVariable(strV).read(origin,
                    new int[]{1, nz, (ny - 1), nx}).reduce().copyToNDJavaArray();

            Array xTimeTp1 = ncIn.findVariable(strTime).read();
            time_tp1 = xTimeTp1.getFloat(xTimeTp1.getIndex().set(rank));
            time_tp1 -= time_tp1 % 60;
            xTimeTp1 = null;

            zeta_tp1 = (float[][]) ncIn.findVariable(strZeta).read(
                    new int[]{rank, 0, 0},
                    new int[]{1, ny, nx}).reduce().copyToNDJavaArray();

            if (FLAG_TP) {
                temp_tp1 = (float[][][]) ncIn.findVariable(strTp).read(origin,
                        new int[]{1, nz, ny, nx}).reduce().copyToNDJavaArray();
            }

            if (FLAG_SAL) {
                salt_tp1 = (float[][][]) ncIn.findVariable(strSal).read(origin,
                        new int[]{1, nz, ny, nx}).reduce().copyToNDJavaArray();
            }


        } catch (IOException e) {
            throw new IOException("Problem extracting fields at location " + ncIn.getLocation().toString() + " : "
                    + e.getMessage());
        } catch (InvalidRangeException e) {
            throw new IOException("Problem extracting fields at location " + ncIn.getLocation().toString() + " : "
                    + e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem extracting fields at location " + ncIn.getLocation().toString() + " : "
                    + e.getMessage());
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
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
                for (int k = 0; k < nz + 1; k++) {
                    z_w_tmp[k][j][i] = z_w_cst_tmp[k][j][i] + zeta_tp1[j][i]
                            * (1.f + z_w_cst_tmp[k][j][i] / hRho[j][i]);
                }
            }
        }
        z_w_cst_tmp = null;
        return z_w_tmp;
    }

    /**
     * Reads longitude and latitude fields in NetCDF dataset
     */
    void readLonLat() throws IOException {
        Array arrLon, arrLat;
        lonRho = new double[nx];
        latRho = new double[ny];
        try {
            arrLon = ncIn.findVariable(strLon).read();
            arrLat = ncIn.findVariable(strLat).read();
            Index indexLon = arrLon.getIndex();
            Index indexLat = arrLat.getIndex();
            for (int j = 0; j < ny; j++) {
                indexLat.set(j);
                latRho[j] = arrLat.getDouble(indexLat);
            }
            for (int i = 0; i < nx; i++) {
                indexLon.set(i);
                lonRho[i] = arrLon.getDouble(indexLon);
            }
            arrLon = null;
            arrLat = null;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
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
    void range(float[] pGeog1, float[] pGeog2) throws IOException {

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

    public Number get(String variableName, double[] pGrid, double time) {
        Variable variable = ncIn.findVariable(variableName);
        if (!variable.getDataType().isNumeric()) {
            throw new NumberFormatException(variableName + " is not a numeric variable");
        }
        int[] origin = new int[variable.getShape().length];
        int[] shape = variable.getShape();
        int i = (int) pGrid[0];
        int j = (int) pGrid[1];
        int n = isCloseToCost(pGrid) ? 1 : 2;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double kz, dz, value_t0, value_t1;
        int k;
        Array array;
        try {
            switch (variable.getShape().length) {
                case 4:
                    kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
                    k = (int) kz;
                    dz = kz - (double) k;
                    shape = new int[]{2, 2, 2, 2};
                    origin = new int[]{rank - 1, k, j, i};
                    array = variable.read(origin, shape);
                    value_t0 = interp3D((ArrayFloat.D3) array.section(new int[]{0, 0, 0, 0}, new int[]{1, 2, 2, 2}).reduce(), dx, dy, dz, n);
                    value_t1 = interp3D((ArrayFloat.D3) array.section(new int[]{1, 0, 0, 0}, new int[]{1, 2, 2, 2}).reduce(), dx, dy, dz, n);
                    return interpTime(value_t0, value_t1, time);
                case 3:
                    if (variable.isUnlimited()) {
                        shape = new int[]{2, 2, 2};
                        origin = new int[]{rank - 1, j, i};
                        array = variable.read(origin, shape);
                        value_t0 = interp2D((ArrayFloat.D2) array.section(new int[]{0, 0, 0}, new int[]{1, 2, 2}).reduce(), dx, dy, n);
                        value_t1 = interp2D((ArrayFloat.D2) array.section(new int[]{1, 0, 0}, new int[]{1, 2, 2}).reduce(), dx, dy, n);
                        return interpTime(value_t0, value_t1, time);
                    } else {
                        kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
                        k = (int) kz;
                        dz = kz - (double) k;
                        shape = new int[]{2, 2, 2};
                        origin = new int[]{k, j, i};
                        array = variable.read(origin, shape);
                        return interp3D((ArrayFloat.D3) array, dx, dy, dz, n);
                    }
                case 2:
                    shape = new int[]{2, 2};
                    origin = new int[]{j, i};
                    array = variable.read(origin, shape);
                    return interp2D((ArrayFloat.D2) array, dx, dy, n);
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private double interpTime(double value_t0, double value_t1, double time) {
        double frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        return (1.d - frac) * value_t0 + frac * value_t1;
    }

    private double interp2D(ArrayFloat.D2 array, double dx, double dy, int n) {
        double value = 0.d;
        double CO = 0.d;

        for (int jj = 0; jj < n; jj++) {
            for (int ii = 0; ii < n; ii++) {
                double co = Math.abs((1.d - (double) ii - dx)
                        * (1.d - (double) jj - dy));
                CO += co;
                value += array.get(jj, ii) * co;
            }
        }

        if (CO != 0) {
            value /= CO;
        }
        return value;
    }

    private double interp3D(ArrayFloat.D3 array, double dx, double dy, double dz, int n) {
        double value = 0.d;
        double CO = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < n; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    double co = Math.abs((1.d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    value += array.get(kk, jj, ii) * co;
                }
            }
        }
        if (CO != 0) {
            value /= CO;
        }
        return value;
    }
}
