package ichthyop.io;

/** import java.io */
import java.io.IOException;

/** import netcdf */
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;

/** local import */
import ichthyop.util.Constant;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/**
 * Specialization of class {@code Dataset} for 2D ROMS simulations.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 *
 * @author P.Verley
 * @see ichthyop.io.Dataset
 */
public class DatasetR2D extends Dataset {

    double[][] pm, pn;
    private String strPn, strPm;

//////////////////////////////////////////////
// Definition of the inherited abstact methods
//////////////////////////////////////////////


    void getFieldsName() {

        super.getFieldsName();
        strPn = Configuration.getStrPn();
        strPm = Configuration.getStrPm();
    }

    /**
     * Reads time non-dependant fields in NetCDF dataset
     */
    void readConstantField(String gridFile) {
        int[] origin = new int[] {jpo, ipo};
        int[] size = new int[] {ny, nx};
        Array arrLon, arrLat, arrMask, arrH, arrPm, arrPn;
        Index index;
        try {
            NetcdfFile ncGrid = NetcdfDataset.openFile(gridFile, null);
            arrLon = ncGrid.findVariable(strLon).read(origin, size);
            arrLat = ncGrid.findVariable(strLat).read(origin, size);
            arrMask = ncGrid.findVariable(strMask).read(origin, size);
            arrH = ncGrid.findVariable(strBathy).read(origin, size);
            arrPm = ncGrid.findVariable(strPm).read(origin, size);
            arrPn = ncGrid.findVariable(strPn).read(origin, size);
            ncGrid.close();

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

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (InvalidRangeException ex) {}
    }

    /**
     * Reads longitude and latitude fields in NetCDF dataset
     */
    void readLonLat(String gridFile) {
        Array arrLon, arrLat;
        try {
            NetcdfFile ncGrid = NetcdfDataset.openFile(gridFile, null);
            arrLon = ncGrid.findVariable(strLon).read();
            arrLat = ncGrid.findVariable(strLat).read();
            ncGrid.close();
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Does nothing. Vertical dimension disregarded for 2D simulation.
     */
    void getCstSigLevels() {}

    /**
     * Advects the particle with the model velocity vector, using a Forward
     * Euler scheme.
     *
     * @see ichthyop.io.Dataset#advectEuler for details
     */
    public double[] advectEuler(double[] pGrid, double time, double dt_sec) {
        double co, CO, x, du, dv, x_euler;

        //-----------------------------------------------------------
        // Interpolate the velocity, temperature and salinity fields
        // in the computational grid.

        double ix, jy;
        ix = pGrid[0];
        jy = pGrid[1];
        du = 0.d;
        dv = 0.d;
        x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

        try {
            //------------------------
            // Get du
            int i = (int) Math.round(ix);
            int j = (int) jy;
            double dx = ix - (double) i;
            double dy = jy - (double) j;
            CO = 0.d;
            for (int ii = 0; ii < 2; ii++) {
                for (int jj = 0; jj < 2; jj++) {
                    co = Math.abs((.5d - (double) ii - dx) *
                                  (1.d - (double) jj - dy));
                    CO += co;
                    x = 0.d;
                    x = (1.d - x_euler) * u_tp0[0][j + jj][i + ii - 1]
                        + x_euler * u_tp1[0][j + jj][i + ii - 1];

                    du += .5d * x * co *
                            (pm[j + jj][Math.max(i + ii - 1, 0)] + pm[j +
                             jj][i + ii]);
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

            for (int jj = 0; jj < 2; jj++) {
                for (int ii = 0; ii < 2; ii++) {
                    co = Math.abs((1.d - (double) ii - dx) *
                                  (.5d - (double) jj - dy));
                    CO += co;
                    x = 0.d;
                    x = (1.d - x_euler) * v_tp0[0][j + jj - 1][i + ii]
                        + x_euler * v_tp1[0][j + jj - 1][i + ii];
                    dv += .5d * x * co *
                            (pn[Math.max(j + jj - 1, 0)][i + ii] + pn[j +
                             jj][i + ii]);
                }
            }

            dv *= dt_sec;
            if (CO != 0) {
                dv /= CO;
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException(
                    "Problem interpolating velocity fields - " + e.getMessage());
        }
        if (du > Constant.THRESHOLD_CFL) {
            System.err.println("! WARNING : CFL broken for u " + (float) du);
        }
        if (dv > Constant.THRESHOLD_CFL) {
            System.err.println("! WARNING : CFL broken for v " + (float) dv);
        }

        return (new double[] {du, dv});

    }

    /**
     * Does nothing. Vertical dimension disregarded for 2D simulation.
     */
    float[][][] computeW() {
        return null;
    }

    /**
     * Adimensionalizes the given magnitude at the specified grid location.
     */
    public double adimensionalize(double number, double xRho, double yRho) {
        return .5d * number *
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

///////////////////////////////////
// Definition of overriding methods
///////////////////////////////////

    /**
     * Overrides {@code Dataset#setAllFieldsTp1AtTime}.
     * <br>
     * Reads 2D time dependant variables in NetCDF dataset at specified rank.
     *
     * @param rank an int, the rank of the time dimension in the NetCDF dataset.
     * @throws an IOException if an error occurs while reading the variables.
     */
    @Override
    void setAllFieldsTp1AtTime(int i_time) throws IOException {

        int[] origin = new int[] {i_time, jpo, ipo};
        u_tp1 = new float[1][ny][nx - 1];
        v_tp1 = new float[1][ny - 1][nx];
        double time_tp0 = time_tp1;

        try {
            u_tp1[0] = (float[][]) ncIn.findVariable(strU).read(origin,
                    new int[] {1, ny, (nx - 1)}).reduce().copyToNDJavaArray();

            v_tp1[0] = (float[][]) ncIn.findVariable(strV).read(origin,
                    new int[] {1, (ny - 1), nx}).reduce().copyToNDJavaArray();

            if (FLAG_TP) {
                temp_tp1 = new float[1][ny][nx];
                temp_tp1[0] = (float[][]) ncIn.findVariable(strTp).read(origin,
                    new int[] {1, ny, nx}).reduce().copyToNDJavaArray();
            }

            Array xTimeTp1 = ncIn.findVariable(strTime).read();
            time_tp1 = xTimeTp1.getFloat(xTimeTp1.getIndex().set(i_time));
            time_tp1 -= time_tp1 % 60;
            xTimeTp1 = null;

        } catch (IOException e) {
            throw new IOException("Problem extracting fields at location "
                                  + ncIn.getLocation().toString() + " : " +
                                  e.getMessage());
        } catch (InvalidRangeException e) {
            throw new IOException("Problem extracting fields at location "
                                  + ncIn.getLocation().toString() + " : " +
                                  e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem extracting fields at location "
                                  + ncIn.getLocation().toString() + " : " +
                                  e.getMessage());
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
    }

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
        int i = (int) pGrid[0];
        int j = (int) pGrid[1];
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        tp = 0.d;
        CO = 0.d;
        
            for (int jj = 0; jj < n; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    {
                        co = Math.abs((1.d - (double) ii - dx) *
                                      (1.d - (double) jj - dy));
                        CO += co;
                        x = 0.d;
                        try {
                            x = (1.d - frac) * temp_tp0[0][j + jj][i + ii] +
                                frac * temp_tp1[0][j + jj][i + ii];
                            tp += x * co;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            throw new ArrayIndexOutOfBoundsException(
                                    "Problem interpolating temperature field : " +
                                    e.getMessage());
                        }
                    }
                }
            }
        
        if (CO != 0) {
            tp /= CO;
        }

        return tp;

    }

    //---------- End of class
}
