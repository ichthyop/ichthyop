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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import static org.previmer.ichthyop.SimulationManagerAccessor.getLogger;
import static org.previmer.ichthyop.SimulationManagerAccessor.getSimulationManager;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

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

    private double[] dlon, dlat;

    /**
     * List on NetCDF input files in which dataset is read.
     */
    private List<String> listInputFiles;
    /**
     * Index of the current file read in the {@code listInputFiles}
     */
    private int indexFile;

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
        if (getParameter("source").toLowerCase().contains("opendap")) {
            opendap = true;
            ncIn = openURL(getParameter("opendap_url"));
        } else {
            opendap = false;
            ncIn = openLocation(getParameter("input_path"), getParameter("file_filter"), false);
        }
        getDimNC();
        readLonLat();
        loadMask();

        // Cell longitudinal dimension (metre)
        dlon = new double[nlon];
        for (int i = 0; i < nlon; i++) {
            double lon1 = getLon(Math.max(i - 1, 0), 0);
            double lon2 = getLon(Math.min(i + 1, nlon - 1), 0);
            dlon[i] = 0.5d * DatasetUtil.geodesicDistance(0, lon1, 0, lon2);
        }
        // Cell latitudinal dimension (metre)
        dlat = new double[nlat];
        for (int j = 0; j < nlat; j++) {
            double lat1 = getLat(0, Math.max(j - 1, 0));
            double lat2 = getLat(0, Math.min(j + 1, nlat - 1));
            dlat[j] = 0.5d * DatasetUtil.geodesicDistance(lat1, 0, lat2, 0);
        }
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
            if (lat >= latitude[j] && lat < latitude[j + 1]) {
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

    private void loadMask() throws IOException, InvalidRangeException {

        // There is no mask variable in OSCAR.
        // Must be extracted from U or V velocity
        int tn = ncIn.findDimension(strTimeDim).getLength();
        // Flip(0) for flipping latitude axis
        Array arr = ncIn.findVariable(strU).read(new int[]{tn - 1, 0, 0, 0}, new int[]{1, 1, nlat, nlon}).reduce().flip(0);
        Index index = arr.getIndex();
        u_tp1 = new double[nlat][nlon];
        for (int j = 0; j < nlat; j++) {
            for (int i = 0; i < nlon; i++) {
                u_tp1[j][i] = arr.getDouble(index.set(j, i));
            }
        }
    }

    private void readLonLat() throws IOException {

        Array arr;
        Index index;
        // longitude
        arr = ncIn.findVariable(strLon).read();
        index = arr.getIndex();
        longitude = new double[nlon];
        for (int i = 0; i < nlon; i++) {
            longitude[i] = arr.getDouble(index.set(i));
        }
        // latitude (flip latitude in order to have cell(0, 0) bottom left
        arr = ncIn.findVariable(strLat).read().flip(0);
        index = arr.getIndex();
        latitude = new double[nlat];
        for (int j = 0; j < nlat; j++) {
            latitude[j] = arr.getDouble(index.set(j));
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
        int ci = i;
        if (i < 0) {
            ci = i + 1080;
        }
        if (i > nlon - 2) {
            ci = i - 1080;
        }
        double lon = (1 - dx) * longitude[ci] + dx * longitude[ci + 1];

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

        double du = 0.d;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double CO = 0.d;
        double x;
        for (int jj = 0; jj < n; jj++) {
            for (int ii = 0; ii < n; ii++) {
                int ci = i + ii;
                if (ci < 0) {
                    ci += 1080;
                }
                if (ci > nlon - 2) {
                    ci -= 1080;
                }
                double co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
                CO += co;
                x = (1.d - x_euler) * u_tp0[j + jj][ci] + x_euler * u_tp1[j + jj][ci];
                du += x * co / dlon[ci];
            }
        }

        if (CO != 0) {
            du /= CO;
        }
        return du;
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {

        double dv = 0.d;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double CO = 0.d;
        double x;
        for (int jj = 0; jj < n; jj++) {
            for (int ii = 0; ii < n; ii++) {
                int ci = i + ii;
                if (ci < 0) {
                    ci += 1080;
                }
                if (ci > nlon - 2) {
                    ci -= 1080;
                }
                double co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
                CO += co;
                x = (1.d - x_euler) * v_tp0[j + jj][ci] + x_euler * v_tp1[j + jj][ci];
                dv += x * co / dlat[j + jj];
            }
        }

        if (CO != 0) {
            dv /= CO;
        }
        return dv;
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
        int ci = i;
        if (ci < 0) {
            ci += 1080;
        }
        if (ci > nlon - 1) {
            ci -= 1080;
        }
        return !Double.isNaN(u_tp1[j][ci]);
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
        return ((pGrid[1] > (nlat - 2.0f))
                || (pGrid[1] < 1.0f));
    }

    @Override
    public double getBathy(int i, int j) {
        return isInWater(i, j) ? 0.d : Double.NaN;
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
        return dlon[i];
    }

    @Override
    public double getdeta(int j, int i) {
        return dlat[j];
    }

    @Override
    public void init() throws Exception {
        setOnFirstTime();
        checkRequiredVariable(ncIn);
        setAllFieldsTp1AtTime(rank);
    }

    private void setAllFieldsTp1AtTime(int rank) throws Exception {

        getLogger().log(Level.INFO, "Reading NetCDF variables from {0} at rank {1}", new Object[]{ncIn.getLocation(), rank});

        int[] origin = new int[]{rank, 0, 0, 0};
        int[] shape = new int[]{1, 1, nlat, nlon};
        double time_tp0 = time_tp1;
        Array arr;
        Index index;

        // Flip(0) for flipping latitude axis
        arr = ncIn.findVariable(strU).read(origin, shape).reduce().flip(0);
        index = arr.getIndex();
        u_tp1 = new double[nlat][nlon];
        for (int j = 0; j < nlat; j++) {
            for (int i = 0; i < nlon; i++) {
                u_tp1[j][i] = arr.getDouble(index.set(j, i));
            }
        }

        // Flip(0) for flipping latitude axis
        arr = ncIn.findVariable(strV).read(origin, shape).reduce().flip(0);
        index = arr.getIndex();
        v_tp1 = new double[nlat][nlon];
        for (int j = 0; j < nlat; j++) {
            for (int i = 0; i < nlon; i++) {
                v_tp1[j][i] = arr.getDouble(index.set(j, i));
            }
        }

        Array xTimeTp1;

        xTimeTp1 = ncIn.findVariable(strTime).read();
        time_tp1 = xTimeTp1.getDouble(xTimeTp1.getIndex().set(rank));
        // Time is expressed in days in Oscar and seconds in Ichthyop
        time_tp1 *= 24.d * 3600.d;

        // Time step of the Oscar dataset
        dt_HyMo = Math.abs(time_tp1 - time_tp0);

        for (RequiredVariable variable : requiredVariables.values()) {
            variable.nextStep(readVariable(ncIn, variable.getName(), rank), time_tp1, dt_HyMo);
        }
    }

    void setOnFirstTime() throws Exception {
        // Time is expressed as number of days since origin in Oscar
        double t0 = (double) getSimulationManager().getTimeManager().get_tO() / (3600.d * 24.d);
        if (!opendap) {
            ncIn = openFile(getFile(t0));
        } else {
            Array timeArr = ncIn.findVariable(strTime).read();
            int ntime = timeArr.getShape()[0];
            double time0 = timeArr.getDouble(timeArr.getIndex().set(0));
            double timeN = timeArr.getDouble(timeArr.getIndex().set(ntime - 1));
            if (t0 < time0 || t0 > timeN) {
                throw new IndexOutOfBoundsException("Time value " + t0 + " not contained among dataset.");
            }
        }
        nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
        rank = findCurrentRank(t0);
        time_tp1 = getSimulationManager().getTimeManager().get_tO();
    }

    private int findCurrentRank(double time) throws Exception {

        int lrank = 0;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());
        double time_rank;
        Array timeArr;
        try {
            timeArr = ncIn.findVariable(strTime).read();
            time_rank = timeArr.getDouble(timeArr.getIndex().set(lrank));
            while (time >= time_rank) {
                if (time_arrow < 0 && time == time_rank) {
                    break;
                }
                lrank++;
                time_rank = timeArr.getDouble(timeArr.getIndex().set(lrank));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            lrank = nbTimeRecords;
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
        return latitude[0];
    }

    /**
     * Gets domain maximum latitude.
     *
     * @return a double, the domain maximum latitude [north degree]
     */
    @Override
    public double getLatMax() {
        return latitude[nlat - 1];
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

        double time = e.getSource().getTime();
        int time_arrow = (int) Math.signum(e.getSource().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        rank += time_arrow;
        try {
            if (rank > (nbTimeRecords - 1) || rank < 0) {
                if (opendap) {
                    throw new IndexOutOfBoundsException("Time out of dataset range");
                } else {
                    ncIn = openFile(getNextFile(time_arrow));
                    rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
                }
            }

            setAllFieldsTp1AtTime(rank);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    private NetcdfFile openLocation(String rawPath, String fileMask, boolean skipSorting) throws IOException {

        String path = IOTools.resolvePath(rawPath);

        if (!isDirectory(path)) {
            throw new IOException("{Dataset} " + rawPath + " is not a valid directory.");
        }
        listInputFiles = getInputList(path, fileMask, skipSorting);
        return openFile(listInputFiles.get(0));
    }

    private List<String> getInputList(String path, String fileMask, boolean skipSorting) throws IOException {

        ArrayList<String> list;

        File inputPath = new File(path);
        File[] listFile = inputPath.listFiles(new MetaFilenameFilter(fileMask));
        if (listFile.length == 0) {
            throw new IOException("{Dataset} " + path + " contains no file matching mask " + fileMask);
        }
        list = new ArrayList(listFile.length);
        for (File file : listFile) {
            list.add(file.toString());
        }
        if (list.size() > 1) {
            if (skipSorting) {
                Collections.sort(list);
            } else {
                Collections.sort(list, new NCComparator(strTime));
            }
        }
        return list;
    }

    private boolean isDirectory(String location) throws IOException {

        File f = new File(location);
        if (!f.isDirectory()) {
            throw new IOException("{Dataset} " + location + " is not a valid directory.");
        }
        return f.isDirectory();
    }

    private String getFile(double time) throws IOException {

        int indexLast = listInputFiles.size() - 1;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());

        for (int i = 0; i < indexLast; i++) {
            if (isTimeIntoFile(time, i)) {
                indexFile = i;
                return listInputFiles.get(i);
            } else if (isTimeBetweenFile(time, i)) {
                indexFile = i - (time_arrow - 1) / 2;
                return listInputFiles.get(indexFile);
            }
        }

        if (isTimeIntoFile(time, indexLast)) {
            indexFile = indexLast;
            return listInputFiles.get(indexLast);
        }
        StringBuilder msg = new StringBuilder();
        msg.append("{Dataset} Time value ");
        msg.append(getSimulationManager().getTimeManager().timeToString());
        msg.append(" (");
        msg.append(time);
        msg.append(" seconds) not contained among NetCDF files.");
        throw new IndexOutOfBoundsException(msg.toString());
    }

    private boolean isTimeIntoFile(double time, int index) throws IOException {

        String filename;
        NetcdfFile nc;
        Array timeArr;
        double time_r0, time_rf;

        filename = listInputFiles.get(index);
        nc = NetcdfDataset.openDataset(filename);
        timeArr = nc.findVariable(strTime).read();
        time_r0 = timeArr.getDouble(timeArr.getIndex().set(0));
        time_rf = timeArr.getDouble(timeArr.getIndex().set(timeArr.getShape()[0] - 1));
        nc.close();

        return (time >= time_r0 && time < time_rf);
    }

    private boolean isTimeBetweenFile(double time, int index) throws IOException {

        NetcdfFile nc;
        String filename = "";
        Array timeArr;
        double[] time_nc = new double[2];

        try {
            for (int i = 0; i < 2; i++) {
                filename = listInputFiles.get(index + i);
                nc = NetcdfDataset.openFile(filename, null);
                timeArr = nc.findVariable(strTime).read();
                time_nc[i] = timeArr.getDouble(timeArr.getIndex().set(0));
                nc.close();
            }
            if (time >= time_nc[0] && time < time_nc[1]) {
                return true;
            }
            //} catch (IOException e) {
            //throw new IOException("{Dataset} Problem reading file " + filename + " : " + e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("{Dataset} Unable to read " + strTime
                    + " variable in file " + filename, e.getCause());
        }
        return false;
    }

    private String getNextFile(int time_arrow) throws IOException {

        int index = indexFile - (1 - time_arrow) / 2;
        boolean noNext = (listInputFiles.size() == 1) || (index < 0) || (index >= listInputFiles.size() - 1);
        if (noNext) {
            throw new IOException("{Dataset} Unable to find any file following " + listInputFiles.get(indexFile));
        }
        indexFile += time_arrow;
        return listInputFiles.get(indexFile);
    }

    private NetcdfFile openFile(String filename) throws IOException {
        NetcdfFile nc;
        try {
            nc = NetcdfDataset.openDataset(filename);
            getLogger().log(Level.INFO, "'{'Dataset'}' Open {0}", filename);
            return nc;
        } catch (IOException e) {
            IOException ioex = new IOException("{Dataset} Problem opening dataset " + filename + " - " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
    }

    /**
     * Loads the NetCDF dataset from the specified filename.
     *
     * @param opendapURL a String that can be a local pathname or an OPeNDAP
     * URL.
     * @throws IOException
     */
    private NetcdfFile openURL(String opendapURL) throws IOException {
        NetcdfFile ncf;
        try {
            getLogger().log(Level.INFO, "Opening remote {0} Please wait...", opendapURL);
            ncf = NetcdfDataset.openDataset(opendapURL);
            getLogger().log(Level.INFO, "'{'Dataset'}' Open remote {0}", opendapURL);
            return ncf;
        } catch (IOException e) {
            IOException ioex = new IOException("{Dataset} Problem opening " + opendapURL + " ==> " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
    }

    @Override
    public double xTore(double x) {
        if (x < 0) {
            return x + 1080.d;
        }
        if (x > nlon - 1) {
            return x - 1080.d;
        }
        return x;
    }

    @Override
    public double yTore(double y) {
        return y;
    }

}
