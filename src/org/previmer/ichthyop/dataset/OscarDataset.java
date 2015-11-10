/*
 * Copyright (C) 2015 pverley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.logging.Level;
import org.previmer.ichthyop.event.NextStepEvent;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public class OscarDataset extends AbstractDataset {

    ///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Grid dimension nx corresponds to lon dimension in Oscar NetCDF file ny
     * corresponds to lat dimension in Oscar NetCDF file
     */
    private int nlon, nlat;
    /**
     * Number of time records in current Oscar NetCDF file
     */
    private int nbTimeRecords;
    /**
     * Longitude at centre of the cell.
     */
    private double[] longitude;
    /**
     * Latitude at centre of the cell.
     */
    private double[] latitude;
    /**
     * Mask at centre of the cell.
     */
    private boolean[][] water;
    /**
     * Zonal component of the velocity field at current time
     */
    private double[][] u_tp0;
    /**
     * Zonal component of the velocity field at time t + dt
     */
    private double[][] u_tp1;
    /**
     * Meridional component of the velocity field at current time
     */
    private double[][] v_tp0;
    /**
     * Meridional component of the velocity field at time t + dt
     */
    private double[][] v_tp1;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    private double dt_HyMo;
    /**
     * Time t + dt expressed in seconds
     */
    private double time_tp1;
    /**
     * Current rank in NetCDF dataset
     */
    private int rank;
    /**
     * Name of the Dimension in NetCDF file
     */
    private String strLonDim, strLatDim, strTimeDim;
    /**
     * Name of the Variable in NetCDF file
     */
    private String strU, strV, strTime;
    /**
     * Name of the Variable in NetCDF file
     */
    private String strLon, strLat;

    private NetcdfFile ncIn;

    private boolean opendap;

    @Override
    void loadParameters() {

        strLonDim = getParameter("field_dim_lon");
        strLatDim = getParameter("field_dim_lat");
        strTimeDim = getParameter("field_dim_time");
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strU = getParameter("field_var_u");
        strV = getParameter("field_var_v");
        strTime = getParameter("field_var_time");
    }

    @Override
    public void setUp() throws Exception {

        loadParameters();
        clearRequiredVariables();
        DatasetIO.setTimeField(strTime);
        if (getParameter("source").toLowerCase().contains("opendap")) {
            opendap = true;
            ncIn = DatasetIO.openURL(getParameter("opendap_url"));
        } else {
            opendap = false;
            ncIn = DatasetIO.openLocation(getParameter("input_path"), getParameter("file_filter"), false);
        }
        getDimNC();
        readLonLat();
        loadMask();
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     *
     * @throws an IOException if an error occurs while reading the dimensions.
     */
    private void getDimNC() {

        nlon = ncIn.findDimension(strLonDim).getLength();
        nlat = ncIn.findDimension(strLatDim).getLength();
    }

    @Override
    public double[] latlon2xy(double lat, double lon) {

        // latitude to y
        double y = 0.d;
        for (int j = 0; j < nlat - 1; j++) {
            if (lat <= latitude[j] && lat > latitude[j + 1]) {
                y = j + (lat - latitude[j]) / (latitude[j + 1] - latitude[j]);
                break;
            }
        }

        // longitude to x
        double x = 0.d;
        for (int i = 0; i < nlon - 1; i++) {
            if (lon >= longitude[i] && lon < longitude[i + 1]) {
                x = i + (lon - longitude[i]) / (longitude[i + 1] - longitude[i]);
                break;
            }
        }

        return new double[]{x, y};
    }

    private void loadMask() {

        water = new boolean[nlat][nlon];
        try {
            // There is no mask variable in OSCAR.
            // Must be extracted from U or V velocity
            Array arr = ncIn.findVariable(strU).read(new int[]{0, 0, 0, 0}, new int[]{1, 1, nlat, nlon}).reduce();
            Index index = arr.getIndex();
            for (int i = 0; i < nlon; i++) {
                for (int j = 0; j < nlat; j++) {
                    water[j][i] = !Float.isNaN(arr.getFloat(index.set(j, i)));
                }
            }
        } catch (IOException | InvalidRangeException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private void readLonLat() {

        Array arr;
        Index index;
        try {
            // longitude
            arr = ncIn.findVariable(strLon).read();
            index = arr.getIndex();
            longitude = new double[nlon];
            for (int i = 0; i < nlon; i++) {
                longitude[i] = arr.getDouble(index.set(i));
            }
            // latitude
            arr = ncIn.findVariable(strLat).read();
            index = arr.getIndex();
            latitude = new double[nlat];
            for (int j = 0; j < nlat; j++) {
                latitude[j] = arr.getDouble(index.set(j));
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public double[] xy2latlon(double x, double y) {

        // y to latitude
        final int j = (int) Math.floor(y);
        final double dy = y - j;
        double lat = (1 - dy) * latitude[j] + dy * latitude[j + 1];

        // x to longitude
        final int i = (int) Math.floor(x);
        final double dx = x - i;
        double lon = (1 - dx) * longitude[i] + dx * longitude[i + 1];

        return new double[]{lat, lon};
    }

    @Override
    public double depth2z(double x, double y, double depth) {
        throw new UnsupportedOperationException(MarsCommon.ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public double z2depth(double x, double y, double z) {
        throw new UnsupportedOperationException(MarsCommon.ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public double get_dUx(double[] pGrid, double time) {
        return 0.d;
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {
        return 0.d;
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {
        throw new UnsupportedOperationException(MarsCommon.ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public boolean isInWater(double[] pGrid) {
        return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    @Override
    public boolean isInWater(int i, int j) {
        return water[j][i];
    }

    @Override
    public boolean isCloseToCost(double[] pGrid) {
        int i, j, ii, jj;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
        jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
        return !(isInWater(i + ii, j) && isInWater(i + ii, j + jj) && isInWater(i, j + jj));
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
        return ((pGrid[0] > (nlon - 2.0f))
                || (pGrid[0] < 1.0f)
                || (pGrid[1] > (nlat - 2.0f))
                || (pGrid[1] < 1.0f));
    }

    @Override
    public double getBathy(int i, int j) {
        return water[j][i] ? 0.d : Double.NaN;
    }

    @Override
    public int get_nx() {
        return nlon;
    }

    @Override
    public int get_ny() {
        return nlat;
    }

    @Override
    public int get_nz() {
        throw new UnsupportedOperationException(MarsCommon.ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public double getdxi(int j, int i) {
        double lat1 = getLat(Math.max(i - 1, 0), j);
        double lat2 = getLat(Math.min(i + 1, nlat - 1), j);
        double lon1 = getLat(Math.max(i - 1, 0), j);
        double lon2 = getLat(Math.min(i + 1, nlat - 1), j);
        return 0.5d * DatasetUtil.geodesicDistance(lat1, lon1, lat2, lon2);
    }

    @Override
    public double getdeta(int j, int i) {
        double lat1 = getLat(i, Math.max(j - 1, 0));
        double lat2 = getLat(i, Math.min(j + 1, nlat - 1));
        double lon1 = getLat(i, Math.max(j - 1, 0));
        double lon2 = getLat(i, Math.min(j + 1, nlat - 1));
        return 0.5d * DatasetUtil.geodesicDistance(lat1, lon1, lat2, lon2);
    }

    @Override
    public void init() throws Exception {
        setOnFirstTime();
        checkRequiredVariable(ncIn);
        setAllFieldsTp1AtTime(rank);
    }

    private void setAllFieldsTp1AtTime(int rank) {

        getLogger().info("Reading NetCDF variables...");

        int[] origin = new int[]{rank, 0, 0, 0};
        int[] shape = new int[]{1, 1, nlat, nlon};
        double time_tp0 = time_tp1;
        Array arr;
        Index index;

        try {
            arr = ncIn.findVariable(strU).read(origin, shape).reduce();
            index = arr.getIndex();
            u_tp1 = new double[nlat][nlon];
            for (int j = 0; j < nlat; j++) {
                for (int i = 0; i < nlon; i++) {
                    u_tp1[j][i] = arr.getDouble(index.set(j, i));
                }
            }
        } catch (IOException | InvalidRangeException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }

        try {
            arr = ncIn.findVariable(strV).read(origin, shape).reduce();
            index = arr.getIndex();
            v_tp1 = new double[nlat][nlon];
            for (int j = 0; j < nlat; j++) {
                for (int i = 0; i < nlon; i++) {
                    v_tp1[j][i] = arr.getDouble(index.set(j, i));
                }
            }
        } catch (IOException | InvalidRangeException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }

        Array xTimeTp1;
        try {
            xTimeTp1 = ncIn.findVariable(strTime).read();
            time_tp1 = xTimeTp1.getDouble(xTimeTp1.getIndex().set(rank));
            // Time is expressed in days in Oscar and seconds in Ichthyop
            time_tp1 *= 24 * 3600;
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }

        // Time step of the Oscar dataset
        dt_HyMo = Math.abs(time_tp1 - time_tp0);

        for (RequiredVariable variable : requiredVariables.values()) {
            try {
                variable.nextStep(readVariable(ncIn, variable.getName(), rank), time_tp1, dt_HyMo);
            } catch (Exception ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        }
    }

    void setOnFirstTime() throws Exception {
        long t0 = getSimulationManager().getTimeManager().get_tO();
        // Time is expressed as number of days since origin in Oscar
        t0 /= (3600 * 24);
        if (!opendap) {
            ncIn = DatasetIO.openFile(DatasetIO.getFile(t0));
        } else {
            Array timeArr = ncIn.findVariable(strTime).read();
            int ntime = timeArr.getShape()[0];
            long time0 = timeArr.getLong(timeArr.getIndex().set(0));
            long timeN = timeArr.getLong(timeArr.getIndex().set(ntime - 1));
            if (t0 < time0 || t0 > timeN) {
                throw new IndexOutOfBoundsException("Time value " + t0 + " not contained among dataset.");
            }
        }
        nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
        rank = findCurrentRank(t0);
        time_tp1 = getSimulationManager().getTimeManager().get_tO();
    }

    private int findCurrentRank(float time) {

        int lrank = 0;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());
        float time_rank;
        Array timeArr;
        try {
            timeArr = ncIn.findVariable(strTime).read();
            time_rank = timeArr.getFloat(timeArr.getIndex().set(lrank));
            while (time >= time_rank) {
                if (time_arrow < 0 && time == time_rank) {
                    break;
                }
                lrank++;
                time_rank = timeArr.getFloat(timeArr.getIndex().set(lrank));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            lrank = nbTimeRecords;
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
        lrank = lrank - (time_arrow + 1) / 2;

        return lrank;
    }

    /**
     * Gets domain minimum latitude.
     *
     * @return a double, the domain minimum latitude [north degree]
     */
    @Override
    public double getLatMin() {
        return latitude[nlat - 1];
    }

    /**
     * Gets domain maximum latitude.
     *
     * @return a double, the domain maximum latitude [north degree]
     */
    @Override
    public double getLatMax() {
        return latitude[0];
    }

    /**
     * Gets domain minimum longitude.
     *
     * @return a double, the domain minimum longitude [east degree]
     */
    @Override
    public double getLonMin() {
        return longitude[0];
    }

    /**
     * Gets domain maximum longitude.
     *
     * @return a double, the domain maximum longitude [east degree]
     */
    @Override
    public double getLonMax() {
        return longitude[nlon - 1];
    }

    @Override
    public double getLon(int igrid, int jgrid) {
        return longitude[igrid];
    }

    @Override
    public double getLat(int igrid, int jgrid) {
        return latitude[jgrid];
    }

    @Override
    public double getDepthMax() {
        return -1.d;
    }

    @Override
    public boolean is3D() {
        return false;
    }

    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) {

        long time = e.getSource().getTime();
        int time_arrow = (int) Math.signum(e.getSource().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            if (opendap) {
                throw new IndexOutOfBoundsException("Time out of dataset range");
            } else {
                try {
                    ncIn = DatasetIO.openFile(DatasetIO.getNextFile(time_arrow));
                } catch (IOException ex) {
                    getLogger().log(Level.SEVERE, null, ex);
                }
                rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
            }
        }
        setAllFieldsTp1AtTime(rank);
    }

}
