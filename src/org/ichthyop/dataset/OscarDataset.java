/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package org.ichthyop.dataset;

import java.io.IOException;
import java.util.List;
import org.ichthyop.event.NextStepEvent;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public class OscarDataset extends NetcdfDataset {

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

    public OscarDataset(String prefix) {
        super(prefix);
    }

    @Override
    void loadParameters() {

        strLonDim = getConfiguration().getString("dataset.oscar.field_dim_lon");
        strLatDim = getConfiguration().getString("dataset.oscar.field_dim_lat");
        strTimeDim = getConfiguration().getString("dataset.oscar.field_dim_time");
        strLon = getConfiguration().getString("dataset.oscar.field_var_lon");
        strLat = getConfiguration().getString("dataset.oscar.field_var_lat");
        strU = getConfiguration().getString("dataset.oscar.field_var_u");
        strV = getConfiguration().getString("dataset.oscar.field_var_v");
        strTime = getConfiguration().getString("dataset.oscar.field_var_time");
    }

    @Override
    public void setUp() throws Exception {

        loadParameters();
        clearRequiredVariables();
        if (getConfiguration().getString("dataset.oscar.source").toLowerCase().contains("dataset.oscar.opendap")) {
            opendap = true;
            ncIn = DatasetUtil.openURL(getConfiguration().getString("dataset.oscar.opendap_url"), true);
        } else {
            opendap = false;
            listInputFiles = DatasetUtil.list(
                    getConfiguration().getString("dataset.oscar.input_path"),
                    getConfiguration().getString("dataset.oscar.file_filter"),
                    false);
            ncIn = DatasetUtil.open(listInputFiles.get(0), true);
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
        // Find the grid longitude index closest to lon
        double dlonMin = Double.MAX_VALUE;
        int iclosest = -1;
        for (int i = 0; i < nlon; i++) {
            if (Math.abs(lon % 360 - longitude[i] % 360) < dlonMin) {
                dlonMin = Math.abs(lon % 360 - longitude[i] % 360);
                iclosest = i;
            }
        }
        // Handle special case iclosest == nlon - 1
        if (lon >= longitude[iclosest]) {
            if (iclosest == nlon - 1) {
                iclosest -= 1080;
            }

        }
        // Handle special case iclosest = 0
        if (lon < longitude[iclosest]) {
            iclosest -= 1;
            if (iclosest < 0) {
                iclosest += 1080;
            }
        }
        double x = iclosest + (lon - longitude[iclosest]) / (longitude[iclosest + 1] - longitude[iclosest]);

        return new double[]{x, y};
    }

    private void loadMask() throws IOException, InvalidRangeException {

        // There is no mask variable in OSCAR.
        // Must be extracted from both U and V velocity (indeed Umask and Vmask
        // may differ slightly)
        int tn = ncIn.findDimension(strTimeDim).getLength();
        // Flip(0) for flipping latitude axis
        // Read U variable
        Array arr = ncIn.findVariable(strU).read(new int[]{tn - 1, 0, 0, 0}, new int[]{1, 1, nlat, nlon}).reduce().flip(0);
        Index index = arr.getIndex();
        u_tp1 = new double[nlat][nlon];
        for (int j = 0; j < nlat; j++) {
            for (int i = 0; i < nlon; i++) {
                u_tp1[j][i] = arr.getDouble(index.set(j, i));
            }
        }
        u_tp0 = u_tp1;
        // Read V variable
        arr = ncIn.findVariable(strV).read(new int[]{tn - 1, 0, 0, 0}, new int[]{1, 1, nlat, nlon}).reduce().flip(0);
        index = arr.getIndex();
        v_tp1 = new double[nlat][nlon];
        for (int j = 0; j < nlat; j++) {
            for (int i = 0; i < nlon; i++) {
                v_tp1[j][i] = arr.getDouble(index.set(j, i));
            }
        }
        v_tp0 = v_tp1;
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

    public double get_dUx(double[] pGrid, double time) {

        double du = 0.d;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double CO = 0.d;
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
                double x = (1.d - x_euler) * u_tp0[j + jj][ci] + x_euler * u_tp1[j + jj][ci];
                if (!Double.isNaN(x)) {
                    du += x * co / dlon[ci];
                }
            }
        }

        if (CO != 0) {
            du /= CO;
        }
        return du;
    }

    public double get_dVy(double[] pGrid, double time) {

        double dv = 0.d;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double CO = 0.d;
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
                double x = (1.d - x_euler) * v_tp0[j + jj][ci] + x_euler * v_tp1[j + jj][ci];
                if (!Double.isNaN(x)) {
                    dv += x * co / dlat[j + jj];
                }
            }
        }

        if (CO != 0) {
            dv /= CO;
        }
        return dv;
    }

    public boolean isInWater(double[] pGrid) {
        return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    public boolean isInWater(int i, int j) {
        int ci = i;
        if (ci < 0) {
            ci += 1080;
        }
        if (ci > nlon - 1) {
            ci -= 1080;
        }
        return !Double.isNaN(u_tp1[j][ci]) && !Double.isNaN(v_tp1[j][ci]);
    }

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
    public boolean isOnEdge(double[] pGrid) {
        return ((pGrid[1] > (nlat - 2.0f))
                || (pGrid[1] < 1.0f));
    }

    public int get_nx() {
        return nlon;
    }

    public int get_ny() {
        return nlat;
    }

    public double get_dx(int j, int i) {
        return dlon[i];
    }

    public double get_dy(int j, int i) {
        return dlat[j];
    }

    @Override
    public void init() throws Exception {
        setOnFirstTime();
        setAllFieldsTp1AtTime(rank);
    }

    private void setAllFieldsTp1AtTime(int rank) throws Exception {

        info("Reading NetCDF variables from {0} at rank {1}", new Object[]{ncIn.getLocation(), rank});

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

        time_tp1 = DatasetUtil.timeAtRank(ncIn, strTime, rank);

        // Time step of the Oscar dataset
        dt_HyMo = Math.abs(time_tp1 - time_tp0);
    }

    void setOnFirstTime() throws Exception {
        
        // Time is expressed as number of days since origin in Oscar
        if (!opendap) {
            indexFile = DatasetUtil.index(prefix, listInputFiles, t0, time_arrow, strTime);
            ncIn = DatasetUtil.open(listInputFiles.get(indexFile), true);
        }
        nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
        rank = DatasetUtil.rank(t0, ncIn, strTime, time_arrow);
        time_tp1 = t0;
    }

    /**
     * Gets domain minimum latitude.
     *
     * @return a double, the domain minimum latitude [north degree]
     */
    public double getLatMin() {
        return latitude[0];
    }

    /**
     * Gets domain maximum latitude.
     *
     * @return a double, the domain maximum latitude [north degree]
     */
    public double getLatMax() {
        return latitude[nlat - 1];
    }

    /**
     * Gets domain minimum longitude.
     *
     * @return a double, the domain minimum longitude [east degree]
     */
    public double getLonMin() {
        return longitude[0];
    }

    /**
     * Gets domain maximum longitude.
     *
     * @return a double, the domain maximum longitude [east degree]
     */
    public double getLonMax() {
        return longitude[nlon - 1];
    }

    public double getLon(int igrid, int jgrid) {
        return longitude[igrid];
    }

    public double getLat(int igrid, int jgrid) {
        return latitude[jgrid];
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) {

        double time = t0 + e.getSource().getTime();
        int timeArrow = time_arrow;

        if (timeArrow * time < timeArrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        rank += timeArrow;
        try {
            if (rank > (nbTimeRecords - 1) || rank < 0) {
                if (opendap) {
                    throw new IndexOutOfBoundsException("Time out of dataset range");
                } else {
                    indexFile = DatasetUtil.next(listInputFiles, indexFile, timeArrow);
                    ncIn = DatasetUtil.open(listInputFiles.get(indexFile), true);
                    rank = (1 - timeArrow) / 2 * (nbTimeRecords - 1);
                }
            }
            setAllFieldsTp1AtTime(rank);
        } catch (Exception ex) {
            error("Error while updating Oscar dataset", ex);
        }
    }

    public double xTore(double x) {
        if (x < 0) {
            return x + 1080.d;
        }
        if (x > nlon - 1) {
            return x - 1080.d;
        }
        return x;
    }

    public double yTore(double y) {
        return y;
    }

}
