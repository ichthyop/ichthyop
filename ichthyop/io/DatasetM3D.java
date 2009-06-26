package ichthyop.io;

/** import java.io */
import java.io.IOException;

/** import netcdf */
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

/** local import */
import ichthyop.util.Constant;

/**
 * Specialization of class {@code Dataset} for 3D MARS simulations.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 *
 * @author P.Verley
 * @see ichthyop.io.Dataset
 */
public class DatasetM3D extends Dataset {

    double[] dxu;
    double dyv;
    float[] s_rho;

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
        dxu = new double[ny];

        try {
            arrLon = ncIn.findVariable(strLon).read(new int[] {ipo},
                    new int[] {nx});
            arrLat = ncIn.findVariable(strLat).read(new int[] {jpo},
                    new int[] {ny});
            arrH = ncIn.findVariable(strBathy).read(new int[] {jpo, ipo},
                    new int[] {ny, nx});
            arrZeta = ncIn.findVariable(strZeta).read(new int[] {0, jpo, ipo},
                    new int[] {1, ny, nx}).reduce();

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
                for (int i = 0; i < nx; i++) {
                    indexLon.set(i);
                    lonRho[j][i] = arrLon.getDouble(indexLon);
                    latRho[j][i] = arrLat.getDouble(indexLat);
                    maskRho[j][i] = (hRho[j][i] == -999.0) ? (byte) 0 :
                                    (byte) 1;
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

            s_rho = (float[]) ncIn.findVariable(strSigma).read().
                    copyToNDJavaArray();

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InvalidRangeException ex) {}

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
        for (int k = nz; k-- > 0; ) {
            for (int i = 0; i++ < nx - 1; ) {
                for (int j = ny; j-- > 0; ) {
                    Huon[k][j][i] = .5d * ((z_w_tmp[k + 1][j][i] -
                                            z_w_tmp[k][j][i]) +
                                           (z_w_tmp[k + 1][j][i - 1] -
                                            z_w_tmp[k][j][i - 1])) * dyv *
                                    u_tp1[k][j][i - 1];
                }
            }
            for (int i = nx; i-- > 0; ) {
                for (int j = 0; j++ < ny - 1; ) {
                    Hvom[k][j][i] = .25d * (((z_w_tmp[k + 1][j][i] -
                                              z_w_tmp[k][j][i]) +
                                             (z_w_tmp[k + 1][j - 1][i] -
                                              z_w_tmp[k][j - 1][i])) *
                                            (dxu[j] +
                                             dxu[j - 1]))
                                    *
                                    v_tp1[k][j - 1][i];
                }
            }
        }

        //---------------------------------------------------
        // Calcultaion of w(i, j, k)
        double[] wrk = new double[nx];
        double[][][] w_double = new double[nz + 1][ny][nx];

        for (int j = ny - 1; j-- > 0; ) {
            for (int i = nx; i-- > 0; ) {
                w_double[0][j][i] = 0.f;
            }
            for (int k = 0; k++ < nz; ) {
                for (int i = nx - 1; i-- > 0; ) {
                    w_double[k][j][i] = w_double[k - 1][j][i] +
                                        (float) (Huon[k - 1][j][i]
                                                 - Huon[k
                                                 - 1][j][i + 1] + Hvom[k -
                                                 1][j][i] - Hvom[k - 1][j +
                                                 1][i]);
                }
            }
            for (int i = nx; i-- > 0; ) {
                wrk[i] = w_double[nz][j][i] /
                         (z_w_tmp[nz][j][i] - z_w_tmp[0][j][i]);
            }
            for (int k = nz; k-- >= 2; ) {
                for (int i = nx; i-- > 0; ) {
                    w_double[k][j][i] += -wrk[i] *
                            (z_w_tmp[k][j][i] - z_w_tmp[0][j][i]);
                }
            }
            for (int i = nx; i-- > 0; ) {
                w_double[nz][j][i] = 0.f;
            }
        }

        //---------------------------------------------------
        // Boundary Conditions
        for (int k = nz + 1; k-- > 0; ) {
            for (int j = ny; j-- > 0; ) {
                w_double[k][j][0] = w_double[k][j][1];
                w_double[k][j][nx - 1] = w_double[k][j][nx - 2];
            }
        }
        for (int k = nz + 1; k-- > 0; ) {
            for (int i = nx; i-- > 0; ) {
                w_double[k][0][i] = w_double[k][1][i];
                w_double[k][ny - 1][i] = w_double[k][ny - 2][i];
            }
        }

        //---------------------------------------------------
        // w * dxu * dyv
        float[][][] w = new float[nz + 1][ny][nx];
        for (int i = nx; i-- > 0; ) {
            for (int j = ny; j-- > 0; ) {
                for (int k = nz + 1; k-- > 0; ) {
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

        double[] s_w = new double[nz + 1];

        s_w[nz] = 1.d;
        s_w[0] = 0.d;
        for (int k = 1; k < nz; k++) {
            s_w[k] = .5d * (s_rho[k - 1] + s_rho[k]);
        }

        for (int i = nx; i-- > 0; ) {
            for (int j = ny; j-- > 0; ) {
                z_w_tmp[0][j][i] = -hRho[j][i];
                for (int k = nz; k-- > 0; ) {
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
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

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
                            co = Math.abs((1.d - (double) ii - dx) *
                                          (1.d - (double) jj - dy) *
                                          (.5d - (double) kk - dz));
                            CO += co;
                            x = 0.d;
                            x = (1.d - x_euler) * w_tp0[k + kk][j + jj][i + ii]
                                + x_euler * w_tp1[k + kk][j + jj][i + ii];
                            dw += 2.d * x * co /
                                    (z_w_tp0[Math.min(k + kk + 1, nz)][j +
                                     jj][i + ii]
                                     - z_w_tp0[Math.max(k + kk - 1, 0)][j +
                                     jj][i + ii]);
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
                            co = Math.abs((.5d - (double) ii - dx) *
                                          (1.d - (double) jj - dy) *
                                          (1.d - (double) kk - dz));
                            CO += co;
                            x = 0.d;
                            x = (1.d - x_euler) * u_tp0[k + kk][j + jj][i + ii -
                                1]
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
                            co = Math.abs((1.d - (double) ii - dx) *
                                          (.5d - (double) jj - dy) *
                                          (1.d - (double) kk - dz));
                            CO += co;
                            x = 0.d;
                            x = (1.d - x_euler) * v_tp0[k + kk][j + jj - 1][i +
                                ii]
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

        return (new double[] {du, dv, dw});
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

    //---------- End of class
}
