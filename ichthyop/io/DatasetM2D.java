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
 * Specialization of class {@code Dataset} for 2D MARS simulations.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 *
 * @author P.Verley
 * @see ichthyop.io.Dataset
 */
public class DatasetM2D extends Dataset {

    /**
     * Array storing the cell sizes in the XI-direction
     */
    private double[] dxu;
    /**
     * Cell size in the ETA-direction
     */
    private double dyv;

//////////////////////////////////////////////
// Definition of the inherited abstact methods
//////////////////////////////////////////////

    /**
     * Reads longitude and latitude fields in NetCDF dataset
     */
    void readLonLat(String gridFile) {

        Array arrLon, arrLat;
        lonRho = new double[ny][nx];
        latRho = new double[ny][nx];
        try {
            NetcdfFile ncGrid = NetcdfDataset.openFile(gridFile, null);
            arrLon = ncGrid.findVariable(strLon).read();
            arrLat = ncGrid.findVariable(strLat).read();
            ncGrid.close();
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
    void readConstantField(String gridFile) {

        Array arrLon, arrLat, arrH;
        Index index;
        lonRho = new double[ny][nx];
        latRho = new double[ny][nx];
        maskRho = new byte[ny][nx];
        dxu = new double[ny];

        try {
            NetcdfFile ncGrid = NetcdfDataset.openFile(gridFile, null);
            arrLon = ncGrid.findVariable(strLon).read(new int[] {ipo},
                    new int[] {nx});
            arrLat = ncGrid.findVariable(strLat).read(new int[] {jpo},
                    new int[] {ny});
            arrH = ncGrid.findVariable(strBathy).read(new int[] {jpo, ipo},
                    new int[] {ny, nx});
            ncGrid.close();

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
     * Does nothing. Vertical dimension disregarded for 2D simulation.
     */
    void getCstSigLevels() {}

    /**
     * Does nothing. Vertical dimension disregarded for 2D simulation.
     */
    float[][][] computeW() {
        return null;
    }

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

                    du += x * co / dxu[j + jj];

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
                    dv += x * co / dyv;

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

        return (new double[] {du, dv});

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

    //---------- End of class
}
