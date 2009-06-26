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
 * Specialization of class {@code Dataset} for 3D ROMS simulations.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 *
 * @author P.Verley
 * @see ichthyop.io.Dataset
 */
public class DatasetR3D extends Dataset {

    private double[][] pm, pn;
    private String strThetaS, strThetaB, strHc, strPn, strPm;


///////////////////////////////////
// Definition of overriding methods
///////////////////////////////////
    /**
     * Overrides {@code Dataset#getFieldsName}
     * <br>
     * Gets the general names of the NetCDF variables from the configuration
     * file, plus some specific names for ROMS 3D simulation.
     */
    @Override
    void getFieldsName() {

        super.getFieldsName();
        strPn = Configuration.getStrPn();
        strPm = Configuration.getStrPm();
        strThetaS = Configuration.getStrThetaS();
        strThetaB = Configuration.getStrThetaB();
        strHc = Configuration.getStrHc();
    }

//////////////////////////////////////////////
// Definition of the inherited abstact methods
//////////////////////////////////////////////

    /**
     * Reads longitude and latitude fields in NetCDF dataset
     */
    void readLonLat() throws IOException {
        Array arrLon, arrLat;
        try {
            arrLon = ncIn.findVariable(strLon).read();
            arrLat = ncIn.findVariable(strLat).read();
            if (arrLon.getElementType() == double.class) {
                lonRho = (double[][]) arrLon.copyToNDJavaArray();
                latRho = (double[][]) arrLat.copyToNDJavaArray();
            } else {
                lonRho = new double[ny][nx];
                latRho = new double[ny][nx];
                Index index = arrLon.getIndex();
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        index.set(j, i);
                        lonRho[j][i] = arrLon.getDouble(index);
                        latRho[j][i] = arrLat.getDouble(index);
                    }
                }
            }
            arrLon = null;
            arrLat = null;
        } catch (IOException e) {
            throw new IOException("Problem reading lon/lat fields at location "
                                  + ncIn.getLocation().toString() + " : " +
                                  e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem reading lon/lat at location "
                                  + ncIn.getLocation().toString() + " : " +
                                  e.getMessage());
        }

    }

    /**
     * Reads time non-dependant fields in NetCDF dataset
     */
    void readConstantField() throws IOException {

        int[] origin = new int[] {jpo, ipo};
        int[] size = new int[] {ny, nx};
        Array arrLon, arrLat, arrMask, arrH, arrZeta, arrPm, arrPn;
        Index index;
        StringBuffer list = new StringBuffer(strLon);
        list.append(", ");
        list.append(strLat);
        list.append(", ");
        list.append(strMask);
        list.append(", ");
        list.append(strBathy);
        list.append(", ");
        list.append(strZeta);
        list.append(", ");
        list.append(strPm);
        list.append(", ");
        list.append(strPn);
        try {
            arrLon = ncIn.findVariable(strLon).read(origin, size);
            arrLat = ncIn.findVariable(strLat).read(origin, size);
            arrMask = ncIn.findVariable(strMask).read(origin, size);
            arrH = ncIn.findVariable(strBathy).read(origin, size);
            arrZeta = ncIn.findVariable(strZeta).read(new int[] {0, jpo, ipo},
                    new int[] {1, ny, nx}).reduce();
            arrPm = ncIn.findVariable(strPm).read(origin, size);
            arrPn = ncIn.findVariable(strPn).read(origin, size);

            if (arrLon.getElementType() == double.class) {
                lonRho = (double[][]) arrLon.copyToNDJavaArray();
                latRho = (double[][]) arrLat.copyToNDJavaArray();
            } else {
                lonRho = new double[ny][nx];
                latRho = new double[ny][nx];
                index = arrLon.getIndex();
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        index.set(j, i);
                        lonRho[j][i] = arrLon.getDouble(index);
                        latRho[j][i] = arrLat.getDouble(index);
                    }
                }
            }

            if (arrMask.getElementType() != byte.class) {
                maskRho = new byte[ny][nx];
                index = arrMask.getIndex();
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        maskRho[j][i] = arrMask.getByte(index.set(j, i));
                    }
                }
            } else {
                maskRho = (byte[][]) arrMask.copyToNDJavaArray();
            }

            if (arrPm.getElementType() == double.class) {
                pm = (double[][]) arrPm.copyToNDJavaArray();
                pn = (double[][]) arrPn.copyToNDJavaArray();
            } else {
                pm = new double[ny][nx];
                pn = new double[ny][nx];
                index = arrPm.getIndex();
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        index.set(j, i);
                        pm[j][i] = arrPm.getDouble(index);
                        pn[j][i] = arrPn.getDouble(index);
                    }
                }
            }

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
        } catch (IOException e) {
            throw new IOException("Problem reading one of the fields "
                                  + list.toString() + " at location "
                                  + ncIn.getLocation().toString() + " : " +
                                  e.getMessage());
        } catch (InvalidRangeException e) {
            throw new IOException("Problem reading one of the fields "
                                  + list.toString() + " at location "
                                  + ncIn.getLocation().toString() + " : " +
                                  e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem reading one of the fields "
                                  + list.toString() + " at location "
                                  + ncIn.getLocation().toString() + " : " +
                                  e.getMessage());
        }
    }

    /**
     * Computes the depth at sigma levels disregarding the free
     * surface elevation.
     */
    void getCstSigLevels() throws IOException {

        double thetas = 0, thetab = 0, hc = 0;
        double cff1, cff2;
        double[] sc_r = new double[nz];
        double[] Cs_r = new double[nz];
        double[] cff_r = new double[nz];
        double[] sc_w = new double[nz + 1];
        double[] Cs_w = new double[nz + 1];
        double[] cff_w = new double[nz + 1];

        //-----------------------------------------------------------
        // Read the Param in ncIn
        try {
            if (ncIn.findGlobalAttribute(strThetaS) == null) {
                System.out.println("ROMS Rutgers");
                thetas = ncIn.findVariable(strThetaS).readScalarDouble();
                thetab = ncIn.findVariable(strThetaB).readScalarDouble();
                hc = ncIn.findVariable(strHc).readScalarDouble();
            } else {
                System.out.println("ROMS UCLA");
                thetas = (ncIn.findGlobalAttribute(strThetaS).getNumericValue()).
                         doubleValue();
                thetab = (ncIn.findGlobalAttribute(strThetaB).getNumericValue()).
                         doubleValue();
                hc = (ncIn.findGlobalAttribute(strHc).getNumericValue()).
                     doubleValue();
            }
        } catch (IOException e) {
            throw new IOException(
                    "Problem reading thetaS/thetaB/hc at location "
                    + ncIn.getLocation().toString() + " : " + e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException(
                    "Problem reading thetaS/thetaB/hc at location "
                    + ncIn.getLocation().toString() + " : " + e.getMessage());
        }

        //-----------------------------------------------------------
        // Calculation of the Coeff
        cff1 = 1.d / sinh(thetas);
        cff2 = .5d / tanh(.5d * thetas);
        for (int k = nz; k-- > 0; ) {
            sc_r[k] = ((double) (k - nz) + .5d) / (double) nz;
            Cs_r[k] = (1.d - thetab) * cff1 * sinh(thetas * sc_r[k])
                      + thetab
                      * (cff2 * tanh((thetas * (sc_r[k] + .5d))) - .5d);
            cff_r[k] = hc * (sc_r[k] - Cs_r[k]);
        }

        for (int k = nz + 1; k-- > 0; ) {
            sc_w[k] = (double) (k - nz) / (double) nz;
            Cs_w[k] = (1.d - thetab) * cff1 * sinh(thetas * sc_w[k])
                      + thetab
                      * (cff2 * tanh((thetas * (sc_w[k] + .5d))) - .5d);
            cff_w[k] = hc * (sc_w[k] - Cs_w[k]);
        }
        sc_w[0] = -1.d;
        Cs_w[0] = -1.d;

        //------------------------------------------------------------
        // Calculation of z_w , z_r
        double[][][] z_r_tmp = new double[nz][ny][nx];
        double[][][] z_w_tmp = new double[nz + 1][ny][nx];

        for (int i = nx; i-- > 0; ) {
            for (int j = ny; j-- > 0; ) {
                z_w_tmp[0][j][i] = -hRho[j][i];
                for (int k = nz; k-- > 0; ) {
                    z_r_tmp[k][j][i] = cff_r[k] + Cs_r[k] * hRho[j][i];
                    z_w_tmp[k +
                            1][j][i] = cff_w[k + 1] + Cs_w[k + 1] * hRho[j][i];

                }
                z_w_tmp[nz][j][i] = 0.d;
            }
        }
        //z_rho_cst = new double[nz][ny][nx];
        //z_w_cst = new double[nz + 1][ny][nx];

        z_rho_cst = z_r_tmp;
        z_w_cst = z_w_tmp;

        z_w_tp0 = new double[nz + 1][ny][nx];
        z_w_tp1 = new double[nz + 1][ny][nx];

        //System.out.println("cst sig ok");

    }

    /**
     * Advects the particle with the model velocity vector, using a Forward
     * Euler scheme.
     *
     * @see ichthyop.io.Dataset#advectEuler for details
     */
    public double[] advectEuler(double[] pGrid, double time,
                                double dt_sec) throws
            ArrayIndexOutOfBoundsException {

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
                            x = (1.d - x_euler) * w_tp0[k + kk][j + jj][i + ii]
                                + x_euler * w_tp1[k + kk][j + jj][i + ii];
                            dw += 2.d * x * co /
                                    (z_w_tp0[Math.min(k + kk + 1, nz)][j +
                                     jj][i +
                                     ii]
                                     - z_w_tp0[Math.max(k + kk - 1, 0)][j +
                                     jj][i + ii]);

                        }
                    }
                }
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
                            x = (1.d - x_euler) * u_tp0[k + kk][j + jj][i + ii -
                                1]
                                + x_euler * u_tp1[k + kk][j + jj][i + ii - 1];
                            du += .5d * x * co *
                                    (pm[j + jj][Math.max(i + ii - 1, 0)] + pm[j +
                                     jj][i + ii]);
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
                            x = (1.d - x_euler) * v_tp0[k + kk][j + jj - 1][i +
                                ii]
                                + x_euler * v_tp1[k + kk][j + jj - 1][i + ii];
                            dv += .5d * x * co *
                                    (pn[Math.max(j + jj - 1, 0)][i + ii] + pn[j +
                                     jj][i + ii]);
                        }
                    }
                }
            }
            dv *= dt_sec;
            if (CO != 0) {
                dv /= CO;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
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
     * Computes the vertical velocity vector.
     *
     * @see ichthyop.io.Dataset#computeW for details about the method
     */
    float[][][] computeW() {

        //System.out.println("Compute vertical velocity");
        double[][][] Huon = new double[nz][ny][nx];
        double[][][] Hvom = new double[nz][ny][nx];
        double[][][] z_w_tmp = z_w_tp1;

        //---------------------------------------------------
        // Calculation Coeff Huon & Hvom
        for (int k = nz; k-- > 0; ) {
            for (int i = 0; i++ < nx - 1; ) {
                for (int j = ny; j-- > 0; ) {
                    Huon[k][j][i] = (((z_w_tmp[k + 1][j][i] -
                                       z_w_tmp[k][j][i]) +
                                      (z_w_tmp[k + 1][j][i - 1] -
                                       z_w_tmp[k][j][i - 1])) /
                                     (pn[j][i] + pn[j][i - 1])) *
                                    u_tp1[k][j][i - 1];
                }
            }
            for (int i = nx; i-- > 0; ) {
                for (int j = 0; j++ < ny - 1; ) {
                    Hvom[k][j][i] = (((z_w_tmp[k + 1][j][i] -
                                       z_w_tmp[k][j][i]) +
                                      (z_w_tmp[k + 1][j - 1][i] -
                                       z_w_tmp[k][j - 1][i])) /
                                     (pm[j][i] + pm[j - 1][i])) *
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
                                                 - Huon[k - 1][j][i + 1] +
                                                 Hvom[k - 1][j][i] - Hvom[k -
                                                 1][j + 1][i]);
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
        // w * pm * pn
        float[][][] w = new float[nz + 1][ny][nx];
        for (int i = nx; i-- > 0; ) {
            for (int j = ny; j-- > 0; ) {
                for (int k = nz + 1; k-- > 0; ) {
                    w[k][j][i] = (float) (w_double[k][j][i] * pm[j][i] *
                                          pn[j][i]);
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
    public double adimensionalize(double length, double xRho, double yRho) {

        return .5d * length *
                (pm[(int) Math.round(yRho)][(int) Math.round(xRho)]
                 + pn[(int) Math.round(yRho)][(int) Math.round(xRho)]);
    }

    /**
     * Gets cell dimension [meter] in the XI-direction.
     */
    double getdxi(int j, int i) {
        return (pm[j][i] != 0) ? (1 / pm[j][i]) : 0.d;
    }

    /**
     * Gets cell dimension [meter] in the ETA-direction.
     */
    double getdeta(int j, int i) {
        return (pn[j][i] != 0) ? (1 / pn[j][i]) : 0.d;
    }


////////////////////////////////
//  Definition of proper methods
////////////////////////////////

    /**
     * Computes the Hyperbolic Sinus of x
     */
    private static double sinh(double x) {
        return ((Math.exp(x) - Math.exp( -x)) / 2.d);
    }

    /**
     * Computes the Hyperbolic Cosinus of x
     */
    private static double cosh(double x) {
        return ((Math.exp(x) + Math.exp( -x)) / 2.d);
    }

    /**
     * Computes the Hyperbolic Tangent of x
     */
    private static double tanh(double x) {
        return (sinh(x) / cosh(x));
    }

    //---------- End of class
}
