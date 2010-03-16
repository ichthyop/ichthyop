/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import java.io.IOException;
import org.previmer.ichthyop.event.NextStepEvent;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public abstract class Mars2dDatasetCommon extends MarsDatasetCommon {

    /**
     * Number of time records in current NetCDF file
     */
    int nbTimeRecords;
    /**
     * Zonal component of the velocity field at current time
     */
    float[][] u_tp0;
    /**
     * Zonal component of the velocity field at time t + dt
     */
    float[][] u_tp1;
    /**
     * Meridional component of the velocity field at current time
     */
    float[][] v_tp0;
    /**
     *  Meridional component of the velocity field at time t + dt
     */
    float[][] v_tp1;
    /**
     * Water salinity at time t + dt
     */
    float[][] salt_tp1;
    /**
     * Water salinity at current time
     */
    float[][] salt_tp0;
    /**
     * Water temperature at current time
     */
    float[][] temp_tp0;
    /**
     * Water temperature at time t + dt
     */
    float[][] temp_tp1;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    double dt_HyMo;
    /**
     * Time t + dt expressed in seconds
     */
    double time_tp1;
    /**
     *
     */
    NetcdfFile ncIn;
    /**
     * Name of the Dimension in NetCDF file
     */
    String strLonDim, strLatDim, strTimeDim;
    /**
     * Name of the Variable in NetCDF file
     */
    String strU, strV, strTp, strSal, strTime;
    /**
     * Name of the Variable in NetCDF file
     */
    String strLon, strLat, strBathy;
    /**
     * Current rank in NetCDF dataset
     */
    int rank;
    /**
     * Determines whether or not the temperature field should be read in the
     * NetCDF file, function of the user's options.
     */
    boolean FLAG_TP;
    /**
     * Determines whether or not the salinity field should be read in the
     * NetCDF file, function of the user's options.
     */
    boolean FLAG_SAL;

    /**
     * Reads the dimensions of the NetCDF dataset
     * @throws an IOException if an error occurs while reading the dimensions.
     */
    void getDimNC() throws IOException {

        try {
            nx = ncIn.findDimension(strLonDim).getLength();
            ny = ncIn.findDimension(strLatDim).getLength();
        } catch (NullPointerException e) {
            e.printStackTrace();
            //throw new IOException("Problem reading dimensions from dataset " + ncIn.getLocation() + " : " + e.getMessage());
        }
        //Logger.getLogger(getClass().getName()).info("nx " + nx + " - ny " + ny + " - nz " + nz);
        ipo = jpo = 0;
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

    @Override
    public double get_dVy(double[] pGrid, double time) {
        double dv = 0.d;
        double ix, jy;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) ix;
        int j = (int) Math.round(jy);
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double CO = 0.d;
        double co = 0.d;
        double x = 0.d;
        for (int jj = 0; jj < 2; jj++) {
            for (int ii = 0; ii < n; ii++) {
                co = Math.abs((1.d - (double) ii - dx)
                        * (.5d - (double) jj - dy));
                CO += co;
                x = (1.d - x_euler) * v_tp0[j + jj - 1][i + ii] + x_euler * v_tp1[j + jj - 1][i + ii];
                dv += x * co / dyv;
            }
        }

        if (CO != 0) {
            dv /= CO;
        }
        return dv;
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

    @Override
    public double get_dUx(double[] pGrid, double time) {

        double du = 0.d;
        double ix, jy;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) Math.round(ix);
        int j = (int) jy;
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double CO = 0.d;
        double co = 0.d;
        double x = 0.d;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < n; jj++) {
                co = Math.abs((.5d - (double) ii - dx)
                        * (1.d - (double) jj - dy));
                CO += co;
                x = (1.d - x_euler) * u_tp0[j + jj][i + ii - 1] + x_euler * u_tp1[j + jj][i + ii - 1];
                du += x * co / dxu[j + jj];
            }
        }
        if (CO != 0) {
            du /= CO;
        }
        return du;
    }

    /**
     * Reads 2D time dependant variables in NetCDF dataset at specified rank.
     *
     * @param rank an int, the rank of the time dimension in the NetCDF dataset.
     * @throws an IOException if an error occurs while reading the variables.
     */
    void setAllFieldsTp1AtTime(int i_time) throws IOException {

        int[] origin = new int[]{i_time, jpo, ipo};
        u_tp1 = new float[ny][nx - 1];
        v_tp1 = new float[ny - 1][nx];
        double time_tp0 = time_tp1;

        try {
            u_tp1 = (float[][]) ncIn.findVariable(strU).read(origin,
                    new int[]{1, ny, (nx - 1)}).reduce().copyToNDJavaArray();

            v_tp1 = (float[][]) ncIn.findVariable(strV).read(origin,
                    new int[]{1, (ny - 1), nx}).reduce().copyToNDJavaArray();

            Array xTimeTp1 = ncIn.findVariable(strTime).read();
            time_tp1 = xTimeTp1.getFloat(xTimeTp1.getIndex().set(i_time));
            time_tp1 -= time_tp1 % 60;
            xTimeTp1 = null;
        } catch (IOException e) {
            throw new IOException("Problem extracting fields at location "
                    + ncIn.getLocation().toString() + " : "
                    + e.getMessage());
        } catch (InvalidRangeException e) {
            throw new IOException("Problem extracting fields at location "
                    + ncIn.getLocation().toString() + " : "
                    + e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem extracting fields at location "
                    + ncIn.getLocation().toString() + " : "
                    + e.getMessage());
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
    }

    @Override
    void loadParameters() {
        strLonDim = getParameter("field_dim_lon");
        strLatDim = getParameter("field_dim_lat");
        strTimeDim = getParameter("field_dim_time");
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strBathy = getParameter("field_var_bathy");
        strU = getParameter("field_var_u");
        strV = getParameter("field_var_v");
        strTp = getParameter("field_var_temp");
        strSal = getParameter("field_var_salt");
        strTime = getParameter("field_var_time");
    }

    public double depth2z(double x, double y, double depth) {
        throw new UnsupportedOperationException("Not supported in 2D.");
    }

    public double z2depth(double x, double y, double z) {
        throw new UnsupportedOperationException("Not supported in 2D.");
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
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        tp = 0.d;
        CO = 0.d;

        for (int jj = 0; jj < n; jj++) {
            for (int ii = 0; ii < n; ii++) {
                co = Math.abs((1.d - (double) ii - dx)
                        * (1.d - (double) jj - dy));
                CO += co;
                x = 0.d;
                try {
                    x = (1.d - frac) * temp_tp0[j + jj][i + ii]
                            + frac * temp_tp1[j + jj][i + ii];
                    tp += x * co;
                } catch (ArrayIndexOutOfBoundsException e) {
                    throw new ArrayIndexOutOfBoundsException("Problem interpolating temperature field : " + e.getMessage());
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
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        sal = 0.d;
        CO = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < n; jj++) {
                for (int ii = 0; ii < n; ii++) {

                    co = Math.abs((1.d - (double) ii - dx)
                            * (1.d - (double) jj - dy));
                    CO += co;
                    x = 0.d;
                    try {
                        x = (1.d - frac) * salt_tp0[j + jj][i + ii]
                                + frac * salt_tp1[j + jj][i + ii];
                        sal += x * co;
                    } catch (ArrayIndexOutOfBoundsException e) {
                        throw new ArrayIndexOutOfBoundsException("Problem interpolating salinity field : " + e.getMessage());
                    }
                }
            }
        }
        if (CO != 0) {
            sal /= CO;
        }

        return sal;
    }

    public int get_nz() {
        throw new UnsupportedOperationException("Not supported in 2D.");
    }

    /**
     * Does nothing. Vertical dimension disregarded for 2D simulation.
     */
    @Override
    public double get_dWz(double[] pGrid, double time) {
        throw new UnsupportedOperationException("Not supported in 2D.");
    }

    public double[] getKv(double[] pGrid, double time, double dt) {
        throw new UnsupportedOperationException("Not supported in 2D.");
    }

    public Number get(String variableName, double[] pGrid, double time) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
