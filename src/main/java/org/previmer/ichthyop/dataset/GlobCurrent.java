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
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public class GlobCurrent extends AbstractDataset {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Grid dimension
     */
    private int nx, ny;
    /**
     * Number of time records in current NetCDF file
     */
    private int nbTimeRecords;
    /**
     * Longitude at rho point.
     */
    private float[] longitude;
    /**
     * Latitude at rho point.
     */
    private float[] latitude;
    /**
     * Zonal component of the velocity field at current time
     */
    private float[][] u_tp0;
    /**
     * Zonal component of the velocity field at time t + dt
     */
    private float[][] u_tp1;
    /**
     * Meridional component of the velocity field at current time
     */
    private float[][] v_tp0;
    /**
     * Meridional component of the velocity field at time t + dt
     */
    private float[][] v_tp1;
    /**
     * Geographical boundary of the domain
     */
    private double latMin, lonMin, latMax, lonMax;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    private double dt_HyMo;
    /**
     * Index of the current file read in the {@code listInputFiles}
     */
    private int indexFile;
    /**
     * Time t + dt expressed in seconds
     */
    private double time_tp1;
    /**
     * Current rank in NetCDF dataset
     */
    private int rank;
    /**
     * Time arrow: forward = +1, backward = -1
     */
    private int time_arrow;
    /**
     * Name of the Variable in NetCDF file
     */
    private String strU, strV, strTime;
    /**
     * Name of the Variable in NetCDF file
     */
    private String strLon, strLat;
    /**
     *
     */
    private double[] dxu;
    private double dyv;
    private List<String> listUFiles, listVFiles;
    private NetcdfFile ncU, ncV;

    private boolean use_constant_mask;
    private String mask_var;
    private String mask_file;
    private double [][] mask_array;

    /**
     * Whether horizontal periodicity should be applied
     */
    boolean xTore = true;

////////////////////////////
// Definition of the methods
////////////////////////////
    @Override
    public boolean is3D() {
        return false;
    }

    /**
     * Reads time non-dependant fields in NetCDF dataset
     */
    private void readConstantField() throws Exception {

        getLogger().log(Level.INFO, "Read longitude and latitude from {0}", ncU.getLocation());
        longitude = (float[]) ncU.findVariable(strLon).read().copyToNDJavaArray();
        latitude = (float[]) ncU.findVariable(strLat).read().copyToNDJavaArray();
        nx = longitude.length;
        ny = latitude.length;

        // scale factors
        dyv = 111138.d * (latitude[1] - latitude[0]);
        dxu = new double[ny];
        for (int j = 0; j < ny; j++) {
            dxu[j] = dyv * Math.cos(Math.PI * latitude[j] / 180.d);
        }

        if(use_constant_mask) {
            NetcdfFile ncMask = DatasetUtil.openFile(this.mask_file, true);
            mask_array = (double[][]) ncMask.findVariable(mask_var).read().copyToNDJavaArray();
            ncMask.close();
        }
    }

    /*
     * Advects the particle with the model velocity vector, using a Forward
     * Euler scheme. Let's see how it works with the example of the Zonal
     * component.
     * <pre>
     * ROMS and MARS uses an Arakawa C grid.
     * Here is the scheme (2D) of cells (i, j) and (i + 1, j):
     *
     *     +-----V(i, j)-----+---V(i + 1, j)---+
     *     |                 |                 |
     *     |            *X   |                 |
     * U(i - 1, j)  +      U(i, j)    +     U(i + 1, j)
     *     |                 |                 |
     *     |                 |                 |
     *     +---V(i, j - 1)---+-V(i + 1, j - 1)-+
     *
     * Particle current location: X(x, y, z)
     * Let's take i = round(x), j = truncate(y) and k = truncate(z)
     * dx = x - i, dy = y - j, dz = z - k
     * Let's call t, the current time of the simulation, and t0 and t1 the
     * values of the time NetCDF variable bounding t: t0 <= t < t1
     * We first interpolate the model velocity field at t0:
     * U(t0) = U(t0, i, j, k) * |(0.5 - dx) * (1 - dy) * (1 - dz)|
     *       + U(t0, i, j, k + 1) * |(0.5 - dx) * (1 - dy) * dz|
     *       + U(t0, i, j + 1, k) * |(0.5 - dx) * dy * (1 - dz)|
     *       + U(t0, i, j + 1, k + 1) * |(0.5 - dx) * dy * dz|
     *       + U(t0, i + 1, j, k) * |(0.5 + dx) * (1 - dy) * (1 - dz)|
     *       + U(t0, i + 1, j, k + 1) * |(0.5 + dx) * (1 - dy) * dz|
     *       + U(t0, i + 1, j + 1, k) * |(0.5 + dx) * dy * (1 - dz)|
     *       + U(t0, i + 1, j + 1, k + 1) * |(0.5 + dx) * dy * dz|
     *
     * This large expression can be written:
     *
     * U(t0) = U(t0, i + ii, j + jj, k + kk)
     *         * |(0.5 - ii - dx) * (1 - jj - dy) * (1 - kk - dz)|
     *
     * with ii, jj and kk integers varying from zero to one.
     * U is expressed in meter per second and we would like to express the move
     * in grid unit. Therefore it has to be adimensionalized.
     * Let's call Ua the velocity in grid unit per second
     * Let's take dXI(i, j) the length of the cell in the zonal direction.
     *
     * Ua(t0) = U(t0, i + ii, j + jj, k + kk)
     *          / [dXI(i + ii , j + jj) + dXI(i + ii + 1, j + jj)]
     *          * |(0.5 - ii - dx) * (1 - jj - dy) * (1 - kk - dz)|
     *
     * Same with U(t1).
     * Let's take frac = (t - t0) / (t1 - t0)
     * Then Ua(t) = (1 - frac) * Ua(t0) + frac * Ua(t1)
     *
     * x(t + dt) = x(t) + Ua(t) * dt
     * </pre>
     *
     */
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
                {
                    int ci = i + ii - 1;
                    if (ci < 0) {
                        ci = nx - 1;
                    }
                    if (ci > nx - 1) {
                        ci = 0;
                    }

                    double co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
                    CO += co;
                    x = (1.d - x_euler) * u_tp0[j + jj][ci] + x_euler * u_tp1[j + jj][ci];
                    if (!Double.isNaN(x)) {
                        du += x * co / dxu[j + jj];
                    }
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
        double x;
        for (int jj = 0; jj < n; jj++) {
            for (int ii = 0; ii < n; ii++) {
                int ci = i + ii;
                if (ci < 0) {
                    ci = nx - 1;
                }
                if (ci > nx - 1) {
                    ci = 0;
                }
                double co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
                CO += co;
                x = (1.d - x_euler) * v_tp0[j + jj][ci] + x_euler * v_tp1[j + jj][ci];
                if (!Double.isNaN(x)) {
                    dv += x * co / dyv;
                }
            }
        }

        if (CO != 0) {
            dv /= CO;
        }
        return dv;
    }

    /*
     * Adimensionalizes the given magnitude at the specified grid location.
     */
    public double adimensionalize(double number, double xRho, double yRho) {

        int j = (int) Math.round(yRho);
        return 2.d * number / (dxu[j] + dyv);
    }

    /**
     * Gets cell dimension [meter] in the XI-direction.
     *
     * @param j
     * @param i
     * @return
     */
    @Override
    public double getdxi(int j, int i) {

        return dxu[j];
    }

    /**
     * Gets cell dimension [meter] in the ETA-direction.
     *
     * @param j
     * @param i
     * @return
     */
    @Override
    public double getdeta(int j, int i) {

        return dyv;
    }

    /**
     * Sets up the {@code Dataset}. The method first sets the appropriate
     * variable names, loads the first NetCDF dataset and extract the time
     * non-dependant information, such as grid dimensions, geographical
     * boundaries, depth at sigma levels.
     *
     * @throws java.lang.Exception
     */
    @Override
    public void setUp() throws Exception {

        loadParameters();
        clearRequiredVariables();
        // List U and V files
        listUFiles = DatasetUtil.list(getParameter("input_path"), getParameter("gridu_pattern"));
        if (!skipSorting()) {
            DatasetUtil.sort(listUFiles, strTime, timeArrow());
        }
        listVFiles = DatasetUtil.list(getParameter("input_path"), getParameter("gridv_pattern"));
        if (!skipSorting()) {
            DatasetUtil.sort(listVFiles, strTime, timeArrow());
        }
        // Open first file
        open(0);
        readConstantField();
        getDimGeogArea();
        setAllFieldsTp1AtTime(0);
    }

    /**
     * Gets the names of the NetCDF variables from the configuration file.
     */
    @Override
    public void loadParameters() {

        // Variable names
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strU = getParameter("field_var_u");
        strV = getParameter("field_var_v");
        strTime = getParameter("field_var_time");
        // Time arrow
        time_arrow = timeArrow();

        // Longitudinal toricity (barrier.n)
        // phv 2018/01/26 true by default
          try {
             xTore = Boolean.valueOf(getParameter("longitude_tore"));
        } catch (NullPointerException ex ) {
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

    /**
     * Determines the geographical boundaries of the domain in longitude,
     * latitude and depth.
     */
    void getDimGeogArea() {

        //--------------------------------------
        // Calculate the Physical Space extrema
        lonMin = Double.MAX_VALUE;
        lonMax = -lonMin;
        latMin = Double.MAX_VALUE;
        latMax = -latMin;

        int i = nx;
        while (i-- > 0) {
            if (longitude[i] >= lonMax) {
                lonMax = longitude[i];
            }
            if (longitude[i] <= lonMin) {
                lonMin = longitude[i];
            }
        }
        int j = ny;
        while (j-- > 0) {
            if (latitude[j] >= latMax) {
                latMax = latitude[j];
            }
            if (latitude[j] <= latMin) {
                latMin = latitude[j];
            }
        }

        //System.out.println("lonmin " + lonMin + " lonmax " + lonMax + " latmin " + latMin + " latmax " + latMax);
        double double_tmp;
        if (lonMin > lonMax) {
            double_tmp = lonMin;
            lonMin = lonMax;
            lonMax = double_tmp;
        }

        if (latMin > latMax) {
            double_tmp = latMin;
            latMin = latMax;
            latMax = double_tmp;
        }
    }

    /**
     * Initialises the {@code Dataset}. Opens the file holding the first time of
     * the simulation. Checks out the existence of the fields required by the
     * current simulation. Sets all fields at time for the first time step.
     *
     * @throws Exception if a required field cannot be found in the NetCDF
     * dataset.
     */
    @Override
    public void init() throws Exception {

        double t0 = getSimulationManager().getTimeManager().get_tO();
        open(indexFile = DatasetUtil.index(listUFiles, t0, time_arrow, strTime));
        setAllFieldsTp1AtTime(rank = DatasetUtil.rank(t0, ncU, strTime, time_arrow));
        time_tp1 = t0;

        for(RequiredVariable var : this.requiredVariables.values()) {
            var.setUnlimited(true);
        }
    }

    /**
     * Reads time dependant variables in NetCDF dataset at specified rank.
     *
     * @param rank an int, the rank of the time dimension in the NetCDF dataset.
     * @throws an IOException if an error occurs while reading the variables.
     *
     */
    void setAllFieldsTp1AtTime(int rank) throws Exception {

        getLogger().info("Reading NetCDF variables...");

        int[] origin = new int[]{rank, 0, 0, 0};
        double time_tp0 = time_tp1;

        try {
            if (ncU.findVariable(strU).getShape().length > 3) {
                u_tp1 = (float[][]) ncU.findVariable(strU).read(origin, new int[]{1, 1, ny, nx}).reduce().copyToNDJavaArray();
            } else {
                u_tp1 = (float[][]) ncU.findVariable(strU).read(new int[]{rank, 0, 0}, new int[]{1, ny, nx}).reduce().copyToNDJavaArray();
            }
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading U velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            if (ncV.findVariable(strV).getShape().length > 3) {
                v_tp1 = (float[][]) ncV.findVariable(strV).read(origin, new int[]{1, 1, ny, nx}).reduce().copyToNDJavaArray();
            } else {
                v_tp1 = (float[][]) ncV.findVariable(strV).read(new int[]{rank, 0, 0}, new int[]{1, ny, nx}).reduce().copyToNDJavaArray();
            }
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading V velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            time_tp1 = DatasetUtil.timeAtRank(ncU, strTime, rank);
        } catch (IOException ex) {
            IOException ioex = new IOException("Error reading time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        for (RequiredVariable variable : requiredVariables.values()) {
            variable.nextStep(readVariable(ncU, variable.getName(), rank), time_tp1, dt_HyMo);
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
    }

    @Override
    public boolean isInWater(int i, int j) {
        int ci = i;
        if (xTore) {
            if (ci < 0) {
                ci = nx - 1;
            }
            if (ci > nx - 1) {
                ci = 0;
            }
        }

        if (use_constant_mask) {
            try {
                return (mask_array[j][ci] == 1);
            } catch (ArrayIndexOutOfBoundsException ex) {
                return false;
            }
        }

        //System.out.println(i + " " + j + " " + k + " - "  + (maskRho[k][j][i] > 0));
        try {
            return (!Double.isNaN(u_tp1[j][ci]) && !Double.isNaN(v_tp1[j][ci]));
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }

    }

    /**
     * Determines whether the specified {@code RohPoint} is in water.
     *
     * @param pGrid the RhoPoint
     * @return <code>true</code> if the {@code RohPoint} is in water,
     * <code>false</code> otherwise.
     * @see #isInWater(int i, int j)
     */
    @Override
    public boolean isInWater(double[] pGrid) {
        return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    /**
     * Determines whether or not the specified grid point is close to cost line.
     * The method first determines in which quater of the cell the grid point is
     * located, and then checks wether or not its cell and the three adjacent
     * cells to the quater are in water.
     *
     * @param pGrid a double[] the coordinates of the grid point
     * @return <code>true</code> if the grid point is close to cost,
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isCloseToCost(double[] pGrid) {

        int i, j, ii, jj;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
        jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
        int ci = i + ii;
        if (ci < 0) {
            ci = nx - 1;
        }
        if (ci > nx - 1) {
            ci = 0;
        }
        return !(isInWater(ci, j) && isInWater(ci, j + jj) && isInWater(i, j + jj));
    }

    @Override
    public double depth2z(double x, double y, double depth) {
        throw new UnsupportedOperationException("Method not supported in 2D");
    }

    @Override
    public double z2depth(double x, double y, double z) {
        throw new UnsupportedOperationException("Method not supported in 2D");
    }

    /**
     * * Transforms the specified 2D grid coordinates into geographical
     * coordinates. It merely does a bilinear spatial interpolation of the
     * surrounding grid nods geographical coordinates.
     *
     * @param xRho a double, the x-coordinate
     * @param yRho a double, the y-coordinate
     * @return a double[], the corresponding geographical coordinates (latitude,
     * longitude)
     */
    @Override
    public double[] xy2latlon(double xRho, double yRho) {

        //--------------------------------------------------------------------
        // Computational space (x, y , z) => Physical space (lat, lon, depth)
        final double jy = Math.max(0.00001f,
                Math.min(yRho, (double) ny - 1.00001f));

        final int i = (int) Math.floor(xRho);
        final int j = (int) Math.floor(jy);
        double lat = 0.d;
        double lon = 0.d;
        final double dx = xRho - (double) i;
        final double dy = jy - (double) j;
        double co;
        for (int ii = 0; ii < 2; ii++) {
            int ci = i;
            if (i < 0) {
                ci = nx - 1;
            }
            int cii = i + ii;
            if (cii > nx - 1) {
                cii = 0;
            }
            if (cii < 0) {
                cii = nx - 1;
            }
            for (int jj = 0; jj < 2; jj++) {
                co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                lat += co * latitude[j + jj];
                if (Math.abs(longitude[cii] - longitude[ci]) < 180) {
                    lon += co * longitude[cii];
                } else {
                    double dlon = Math.abs(360.d - Math.abs(longitude[cii] - longitude[ci]));
                    if (longitude[ci] < 0) {
                        lon += co * (longitude[ci] - dlon);
                    } else {
                        lon += co * (longitude[ci] + dlon);
                    }
                }
            }
        }

        return (new double[]{lat, lon});
    }

    /**
     * Transforms the specified 2D geographical coordinates into a grid
     * coordinates.
     *
     * The algorithme has been adapted from a function in ROMS/UCLA code,
     * originally written by Alexander F. Shchepetkin and Hernan G. Arango.
     * Please find below an extract of the ROMS/UCLA documention.
     *
     * <pre>
     *  Checks the position to find if it falls inside the whole domain.
     *  Once it is established that it is inside, find the exact cell to which
     *  it belongs by successively dividing the domain by a half (binary
     *  search).
     * </pre>
     *
     * @param lon a double, the longitude of the geographical point
     * @param lat a double, the latitude of the geographical point
     * @return a double[], the corresponding grid coordinates (x, y)
     * @see #isInsidePolygone
     */
    @Override
    public double[] latlon2xy(double lat, double lon) {

        //--------------------------------------------------------------------
        // Physical space (lat, lon) => Computational space (x, y)
        boolean found1 = false;
        boolean found2 = false;

        int ci = nx / 2;
        int cj = ny / 2;
        int di = ci / 2;
        int dj = cj / 2;

        // Find the closet grid point to {lat, lon}
        while (!(found1 && found2)) {
            int i = ci;
            int j = cj;
            double dmin = DatasetUtil.geodesicDistance(lat, lon, latitude[j], longitude[i]);
            for (int ii = -di; ii <= di; ii += di) {
                if ((i + ii >= 0) && (i + ii < nx)) {
                    double d = DatasetUtil.geodesicDistance(lat, lon, latitude[j], longitude[i + ii]);
                    if (d < dmin) {
                        dmin = d;
                        ci = i + ii;
                        cj = j;
                    }
                }
            }
            for (int jj = -dj; jj <= dj; jj += dj) {
                if ((j + jj >= 0) && (j + jj < ny)) {
                    double d = DatasetUtil.geodesicDistance(lat, lon, latitude[j + jj], longitude[i]);
                    if (d < dmin) {
                        dmin = d;
                        ci = i;
                        cj = j + jj;
                    }
                }
            }
            if (i == ci && j == cj) {
                found1 = true;
                if (dj == 1 && di == 1) {
                    found2 = true;
                } else {
                    di = (int) Math.max(1, di / 2);
                    dj = (int) Math.max(1, di / 2);
                    found1 = false;
                }
            }
        }

        // Refine within cell (ci, cj) by linear interpolation
        int cip1 = ci + 1 > nx - 1 ? 0 : ci + 1;
        int cim1 = ci - 1 < 0 ? nx - 1 : ci - 1;
        int cjp1 = cj + 1 > ny - 1 ? ny - 1 : cj + 1;
        int cjm1 = cj - 1 < 0 ? 0 : cj - 1;
        // xgrid
        double xgrid;
        if (lon >= longitude[ci]) {
            double dx = (Math.abs(longitude[cip1] - longitude[ci]) > 180.d)
                    ? 360.d + (longitude[cip1] - longitude[ci])
                    : longitude[cip1] - longitude[ci];
            double deltax = (lon - longitude[ci]) / dx;
            xgrid = xTore(ci + deltax);
        } else {
            double dx = (Math.abs(longitude[ci] - longitude[cim1]) > 180.d)
                    ? 360.d + (longitude[ci] - longitude[cim1])
                    : longitude[ci] - longitude[cim1];
            double deltax = (lon - longitude[cim1]) / dx;
            xgrid = xTore(cim1 + deltax);
        }
        // ygrid
        double ygrid;
        if (lat >= latitude[cj]) {
            double dy = latitude[cjp1] - latitude[cj];
            double deltay = (lat - latitude[cj]) / dy;
            ygrid = (double) cj + deltay;
        } else {
            double dy = latitude[cj] - latitude[cjm1];
            double deltay = (lat - latitude[cjm1]) / dy;
            ygrid = (double) cjm1 + deltay;
        }

        return (new double[]{xgrid, ygrid});
    }

    /**
     * Loads the NetCDF dataset from the specified filename.
     *
     * @param filename a String that can be a local pathname or an OPeNDAP URL.
     * @throws IOException
     */
    private void open(int index) throws IOException {

        if (ncU != null) {
            ncU.close();
        }
        ncU = DatasetUtil.openFile(listUFiles.get(index), true);
        if (ncV != null) {
            ncV.close();
        }
        ncV = DatasetUtil.openFile(listVFiles.get(index), true);

        nbTimeRecords = ncU.findVariable(strTime).getShape(0);
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
        /* barrier.n, 2017-08-02> adding the last two lines for zonal checking */
        return (!xTore && (pGrid[0] > (nx - 2.d)) || (!xTore && (pGrid[0] < 1.d))
                || (pGrid[1] > (ny - 2.d)) || (pGrid[1] < 1.d));
    }

//////////
// Getters
//////////
    /**
     * Gets the grid dimension in the XI-direction
     *
     * @return an int, the grid dimension in the XI-direction (Zonal)
     */
    @Override
    public int get_nx() {
        return nx;
    }

    /**
     * Gets the grid dimension in the ETA-direction
     *
     * @return an int, the grid dimension in the ETA-direction (Meridional)
     */
    @Override
    public int get_ny() {
        return ny;
    }

    /**
     * Gets the grid dimension in the vertical direction
     *
     * @return an int, the grid dimension in the vertical direction
     */
    @Override
    public int get_nz() {
        throw new UnsupportedOperationException("Method not supported in 2D");
    }

    /**
     * Gets domain minimum latitude.
     *
     * @return a double, the domain minimum latitude [north degree]
     */
    @Override
    public double getLatMin() {
        return latMin;
    }

    /**
     * Gets domain maximum latitude.
     *
     * @return a double, the domain maximum latitude [north degree]
     */
    @Override
    public double getLatMax() {
        return latMax;
    }

    /**
     * Gets domain minimum longitude.
     *
     * @return a double, the domain minimum longitude [east degree]
     */
    @Override
    public double getLonMin() {
        return lonMin;
    }

    /**
     * Gets domain maximum longitude.
     *
     * @return a double, the domain maximum longitude [east degree]
     */
    @Override
    public double getLonMax() {
        return lonMax;
    }

    /**
     * Gets domain maximum depth.
     *
     * @return a float, the domain maximum depth [meter]
     */
    @Override
    public double getDepthMax() {
        return -1;
    }

    /**
     * Gets the latitude at (i, j) grid point.
     *
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the latitude [north degree] at (i, j) grid point.
     */
    @Override
    public double getLat(int i, int j) {
        return latitude[j];
    }

    /**
     * Gets the longitude at (i, j) grid point.
     *
     * @param i an int, the i-ccordinatex
     * @param j an int, the j-coordinate
     * @return a double, the longitude [east degree] at (i, j) grid point.
     */
    @Override
    public double getLon(int i, int j) {
        return longitude[i];
    }

    /**
     * Gets the bathymetry at (i, j) grid point.
     *
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the bathymetry [meter] at (i, j) grid point if is in
     * water, return NaN otherwise.
     */
    @Override
    public double getBathy(int i, int j) {
        return -1.d;
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        rank += time_arrow;

        if (rank > (nbTimeRecords - 1) || rank < 0) {
            open(indexFile = DatasetUtil.next(listUFiles, indexFile, time_arrow));
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }

        setAllFieldsTp1AtTime(rank);

    }

    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        for(Variable var : nc.getVariables()) {
            System.out.println(var.getNameAndDimensions());
        }
        Variable variable = nc.findVariable(name);
        int[] origin = null, shape = null;
        boolean hasVerticalDim = false;
        switch (variable.getShape().length) {
            case 4:
                origin = new int[]{rank, 0, 0, 0};
                shape = new int[]{1, 1, ny, nx};
                hasVerticalDim = true;
                break;
            case 2:
                origin = new int[]{0, 0};
                shape = new int[]{ny, nx};
                break;
            case 3:
                if (!variable.isUnlimited()) {
                    origin = new int[]{0, 0, 0};
                    shape = new int[]{1, ny, nx};
                    hasVerticalDim = true;
                } else {
                    origin = new int[]{rank, 0, 0};
                    shape = new int[]{1, ny, nx};
                }
                break;
        }

        Array array = variable.read(origin, shape).reduce();
        if (hasVerticalDim) {
            array = array.flip(0);
        }
        return array;
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {
        throw new UnsupportedOperationException("Method not supported in 2D");
    }

    @Override
    public double xTore(double x) {
        if (xTore) {
            if (x < -0.5d) {
                return nx + x;
            }
            if (x > nx - 0.5d) {
                return x - nx;
            }
        }
        return x;
    }

    @Override
    public double yTore(double y) {
        return y;
    }
}
