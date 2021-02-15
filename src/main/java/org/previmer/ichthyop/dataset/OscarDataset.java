/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.previmer.ichthyop.event.NextStepEvent;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

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
    /**
     * Whether horizontal periodicity should be applied
     */
    boolean xTore = true;

    private boolean use_constant_mask;
    private String mask_var;
    private String mask_file;
    private double[][] mask_array;
    

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

        // phv 2018/01/26 Longitudinal toricity, true by default
        try {
            xTore = Boolean.valueOf(getParameter("longitude_tore"));
        } catch (NullPointerException ex) {
            xTore = true;
        }
        
        this.use_constant_mask = false;
        if (this.findParameter("use_constant_mask")) {
            this.use_constant_mask = Boolean.valueOf(getParameter("use_constant_mask"));
        }

        if (this.use_constant_mask) {
            this.mask_file = getParameter("mask_file");
            this.mask_var = getParameter("mask_var");
        }
        
    }

    @Override
    public void setUp() throws Exception {

        loadParameters();
        clearRequiredVariables();
        if (getParameter("source").toLowerCase().contains("opendap")) {
            opendap = true;
            ncIn = DatasetUtil.openURL(getParameter("opendap_url"), true);
        } else {
            opendap = false;
            listInputFiles = DatasetUtil.list(getParameter("input_path"), getParameter("file_filter"));
            DatasetUtil.sort(listInputFiles, strTime, timeArrow());
            ncIn = DatasetUtil.openFile(listInputFiles.get(0), true);
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

        if (use_constant_mask) {
            NetcdfFile ncMask = DatasetUtil.openFile(this.mask_file, true);
            mask_array = (double[][]) ncMask.findVariable(mask_var).read().flip(0).copyToNDJavaArray();
            ncMask.close();
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

    @Override
    public double[] xy2latlon(double x, double y) {

        // y to latitude
        final int j = (int) Math.floor(y);
        final double dy = y - j;
        double lat = (1 - dy) * latitude[j] + dy * latitude[j + 1];
        
        // the DAP values are 1081 long, so off was 1080
        // new files are 1201 long, so off must be 1200
        int off = this.longitude.length - 1;

        // x to longitude
        final int i = (int) Math.floor(x);
        final double dx = x - i;
        int ci = i;
        if (i < 0) {
            ci = i + off;
        }
        if (i > nlon - 2) {
            ci = i - off;
        }
        double lon = (1 - dx) * longitude[ci] + dx * longitude[ci + 1];

        return new double[]{lat, lon};
    }

    @Override
    public double depth2z(double x, double y, double depth) {
        throw new UnsupportedOperationException("Method not supported in 2D");
    }

    @Override
    public double z2depth(double x, double y, double z) {
        throw new UnsupportedOperationException("Method not supported in 2D");
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

    @Override
    public double get_dWz(double[] pGrid, double time) {
        throw new UnsupportedOperationException("Method not supported in 2D");
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
        
        if (use_constant_mask) {
            try {
                return (mask_array[j][ci] == 1);
            } catch (ArrayIndexOutOfBoundsException ex) {
                return false;
            }
        }
        
        try {
            return (!Double.isNaN(u_tp1[j][ci]) && !Double.isNaN(v_tp1[j][ci]));
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
        
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
        return (!xTore && (pGrid[0] > (nlon - 2.d)) || (!xTore && (pGrid[0] < 1.d)))
                || ((pGrid[1] > (nlat - 2.0f)) || (pGrid[1] < 1.0f));
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
        throw new UnsupportedOperationException("Method not supported in 2D");
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
        
        for (RequiredVariable variable : requiredVariables.values()) {
            variable.setUnlimited(true);
        }
        
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

        time_tp1 = DatasetUtil.timeAtRank(ncIn, strTime, rank);

        // Time step of the Oscar dataset
        dt_HyMo = Math.abs(time_tp1 - time_tp0);

        for (RequiredVariable variable : requiredVariables.values()) {
            variable.nextStep(readVariable(ncIn, variable.getName(), rank), time_tp1, dt_HyMo);
        }
    }

    void setOnFirstTime() throws Exception {
        // Time is expressed as number of days since origin in Oscar
        double t0 = getSimulationManager().getTimeManager().get_tO();
        if (!opendap) {
            indexFile = DatasetUtil.index(listInputFiles, t0, timeArrow(), strTime);
            ncIn = DatasetUtil.openFile(listInputFiles.get(indexFile), true);
        }
        nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow());
        time_tp1 = getSimulationManager().getTimeManager().get_tO();
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
        
        Variable variable = nc.findVariable(name);
        int[] origin = new int[]{rank, 0, 0, 0};
        int[] shape = new int[]{1, 1, nlat, nlon};
        return variable.read(origin, shape).reduce().flip(0);
    }

        
    @Override
    public void nextStepTriggered(NextStepEvent e) {

        double time = e.getSource().getTime();
        int timeArrow = timeArrow();

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
                    ncIn = DatasetUtil.openFile(listInputFiles.get(indexFile), true);
                    rank = (1 - timeArrow) / 2 * (nbTimeRecords - 1);
                }
            }
            setAllFieldsTp1AtTime(rank);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
            System.exit(1);
        }
    }

    @Override
    public double xTore(double x) {
        if (xTore) {
            if (x < 0) {
                return x + 1080.d;
            }
            if (x > nlon - 1) {
                return x - 1080.d;
            }
        }
        return x;
    }

    @Override
    public double yTore(double y) {
        return y;
    }

}
