/*
 *  Copyright (C) 2011 pverley
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.logging.Level;
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public abstract class MarsCommon extends AbstractDataset {

    /**
     * Number of time records in NetCDF dataset
     */
    int nbTimeRecords;
    /**
     * Grid dimension
     */
    int nx, ny;
    /**
     * Origin for grid index
     */
    int ipo, jpo;
    /**
     * Longitude at rho point.
     */
    double[][] lonRho;
    /**
     * Latitude at rho point.
     */
    double[][] latRho;
    /**
     * Bathymetry
     */
    double[][] hRho;
    /**
     * Mask: water = 1, cost = 0
     */
    byte[][] maskRho;
    /**
     * Geographical boundary of the domain
     */
    double latMin, lonMin, latMax, lonMax, depthMax;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    static double dt_HyMo;
    /**
     * Time t + dt expressed in seconds
     */
    static double time_tp1;
    /**
     * Name of the Dimension in NetCDF file
     */
    static String strLonDim, strLatDim, strTimeDim;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strU, strV, strTime;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strLon, strLat, strBathy;
    /**
     *
     */
    double[][] dxu;
    /**
     *
     */
    double[][] dyv;
    /**
     *
     */
    NetcdfFile ncIn;
    /**
     * Current rank in NetCDF dataset
     */
    static int rank;

    abstract void openDataset() throws Exception;

    abstract void setOnFirstTime() throws Exception;

    abstract void setAllFieldsTp1AtTime(int rank) throws Exception;

    public void setUp() throws Exception {
        loadParameters();
        clearRequiredVariables();
        openDataset();
        getDimNC();
        shrinkGrid();
        readConstantField();
        getDimGeogArea();
    }

    public void init() throws Exception {
        setOnFirstTime();
        checkRequiredVariable(ncIn);
        setAllFieldsTp1AtTime(rank);
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
        strTime = getParameter("field_var_time");
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     * @throws an IOException if an error occurs while reading the dimensions.
     */
    void getDimNC() throws IOException {

        try {
            nx = ncIn.findDimension(strLonDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset longitude dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            ny = ncIn.findDimension(strLatDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset latitude dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        ipo = jpo = 0;
    }

    void readTimeLength() throws IOException {
        try {
            nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Failed to read dataset time dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
    }

    void readConstantField() throws Exception {

        Array arrH = null;
        Index index;
        lonRho = new double[ny][nx];
        latRho = new double[ny][nx];
        maskRho = new byte[ny][nx];
        dxu = new double[ny][nx];
        dyv = new double[ny][nx];

        /* Read longitude & latitude */
        readLonLat();

        /* Read bathymetry */
        try {
            arrH = ncIn.findVariable(strBathy).read(new int[]{jpo, ipo}, new int[]{ny, nx});
        } catch (Exception ex) {
            IOException ioex = new IOException("{Dataset} Error reading bathymetry variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        hRho = new double[ny][nx];
        index = arrH.getIndex();
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                hRho[j][i] = arrH.getDouble(index.set(j, i));
            }
        }

        /* Compute mask */
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                maskRho[j][i] = (hRho[j][i] < 0)
                        ? (byte) 0
                        : (byte) 1;
            }
        }

        /* Compute metrics dxu & dyv */
        double[] ptGeo1, ptGeo2;
        for (int j = 1; j < ny - 1; j++) {
            for (int i = 1; i < nx - 1; i++) {
                ptGeo1 = xy2latlon(i - 0.5d, (double) j);
                ptGeo2 = xy2latlon(i + 0.5d, (double) j);
                dxu[j][i] = DatasetUtil.geodesicDistance(ptGeo1[0], ptGeo1[1], ptGeo2[0], ptGeo2[1]);
                ptGeo1 = xy2latlon((double) i, j - 0.5d);
                ptGeo2 = xy2latlon((double) i, j + 0.5d);
                dyv[j][i] = DatasetUtil.geodesicDistance(ptGeo1[0], ptGeo1[1], ptGeo2[0], ptGeo2[1]);
            }
        }
        /* Boundary conditions */
        for (int j = ny; j-- > 0;) {
            dxu[j][0] = dxu[j][1];
            dxu[j][nx - 1] = dxu[j][nx - 2];
            dyv[j][0] = dyv[j][1];
            dyv[j][nx - 1] = dyv[j][nx - 2];
        }
        for (int i = nx; i-- > 0;) {
            dxu[0][i] = dxu[1][i];
            dxu[ny - 1][i] = dxu[ny - 2][i];
            dyv[0][i] = dyv[1][i];
            dyv[ny - 1][i] = dyv[ny - 2][i];
        }
    }

    int findCurrentRank(long time) throws Exception {

        int lrank = 0;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());
        long time_rank;
        Array timeArr = null;
        try {
            timeArr = ncIn.findVariable(strTime).read();
            time_rank = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(lrank)));
            while (time >= time_rank) {
                if (time_arrow < 0 && time == time_rank) {
                    break;
                }
                lrank++;
                time_rank = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(lrank)));
            }
        } catch (ArrayIndexOutOfBoundsException e) {
            lrank = nbTimeRecords;
        }
        lrank = lrank - (time_arrow + 1) / 2;

        return lrank;
    }

    /**
     * Adimensionalizes the given magnitude at the specified grid location.
     */
    public double adimensionalize(double number, double xRho, double yRho) {
        int i = (int) Math.round(xRho);
        int j = (int) Math.round(yRho);
        return 2.d * number / (dyv[j][i] + dxu[j][i]);
    }

    public boolean isInWater(double[] pGrid) {
        return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    public boolean isOnEdge(double[] pGrid) {
        return ((pGrid[0] > (nx - 2.0f))
                || (pGrid[0] < 1.0f)
                || (pGrid[1] > (ny - 2.0f))
                || (pGrid[1] < 1.0f));
    }

    public double getBathy(int i, int j) {
        if (isInWater(i, j)) {
            return hRho[j][i];
        }
        return Double.NaN;
    }

    public double[] lonlat2xy(double lon, double lat) {

        //--------------------------------------------------------------------
        // Physical space (lat, lon) => Computational space (x, y)

        boolean found;
        int imin, imax, jmin, jmax, i0, j0;
        double dx1, dy1, dx2, dy2, c1, c2, deltax, deltay, xgrid, ygrid;

        xgrid = -1.;
        ygrid = -1.;
        found = isInsidePolygone(0, nx - 1, 0, ny - 1, lon, lat);

        //-------------------------------------------
        // Research surrounding grid-points
        if (found) {
            imin = 0;
            imax = nx - 1;
            jmin = 0;
            jmax = ny - 1;
            while (((imax - imin) > 1) | ((jmax - jmin) > 1)) {
                if ((imax - imin) > 1) {
                    i0 = (imin + imax) / 2;
                    found = isInsidePolygone(imin, i0, jmin, jmax, lon, lat);
                    if (found) {
                        imax = i0;
                    } else {
                        imin = i0;
                    }
                }
                if ((jmax - jmin) > 1) {
                    j0 = (jmax + jmin) / 2;
                    found = isInsidePolygone(imin, imax, jmin, j0, lon, lat);
                    if (found) {
                        jmax = j0;
                    } else {
                        jmin = j0;
                    }
                }
            }

            //--------------------------------------------
            // Trilinear interpolation
            dy1 = latRho[jmin + 1][imin] - latRho[jmin][imin];
            dx1 = lonRho[jmin + 1][imin] - lonRho[jmin][imin];
            dy2 = latRho[jmin][imin + 1] - latRho[jmin][imin];
            dx2 = lonRho[jmin][imin + 1] - lonRho[jmin][imin];

            c1 = lon * dy1 - lat * dx1;
            c2 = lonRho[jmin][imin] * dy2 - latRho[jmin][imin] * dx2;
            deltax = (c1 * dx2 - c2 * dx1) / (dx2 * dy1 - dy2 * dx1);
            deltax = (deltax - lonRho[jmin][imin]) / dx2;
            xgrid = (double) imin + Math.min(Math.max(0.d, deltax), 1.d);

            c1 = lonRho[jmin][imin] * dy1 - latRho[jmin][imin] * dx1;
            c2 = lon * dy2 - lat * dx2;
            deltay = (c1 * dy2 - c2 * dy1) / (dx2 * dy1 - dy2 * dx1);
            deltay = (deltay - latRho[jmin][imin]) / dy1;
            ygrid = (double) jmin + Math.min(Math.max(0.d, deltay), 1.d);
        }
        return (new double[]{xgrid, ygrid});
    }

    public double[] xy2latlon(double xRho, double yRho) {

        //--------------------------------------------------------------------
        // Computational space (x, y , z) => Physical space (lat, lon, depth)

        final double ix = Math.max(0.00001f, Math.min(xRho, (double) nx - 1.00001f));
        final double jy = Math.max(0.00001f, Math.min(yRho, (double) ny - 1.00001f));

        final int i = (int) Math.floor(ix);
        final int j = (int) Math.floor(jy);
        double latitude = 0.d;
        double longitude = 0.d;
        final double dx = ix - (double) i;
        final double dy = jy - (double) j;
        double co = 0.d;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                latitude += co * latRho[j + jj][i + ii];
                longitude += co * lonRho[j + jj][i + ii];
            }
        }
        return (new double[]{latitude, longitude});
    }

    boolean isInsidePolygone(int imin, int imax, int jmin, int jmax, double lon, double lat) {

        //--------------------------------------------------------------
        // Return true if (lon, lat) is insidide the polygon defined by
        // (imin, jmin) & (imin, jmax) & (imax, jmax) & (imax, jmin)

        //-----------------------------------------
        // Build the polygone
        int nb, shft;
        double[] xb, yb;
        boolean isInPolygone = true;

        nb = 2 * (jmax - jmin + imax - imin);
        xb = new double[nb + 1];
        yb = new double[nb + 1];
        shft = 0 - imin;
        for (int i = imin; i <= (imax - 1); i++) {
            xb[i + shft] = lonRho[jmin][i];
            yb[i + shft] = latRho[jmin][i];
        }
        shft = 0 - jmin + imax - imin;
        for (int j = jmin; j <= (jmax - 1); j++) {
            xb[j + shft] = lonRho[j][imax];
            yb[j + shft] = latRho[j][imax];
        }
        shft = jmax - jmin + 2 * imax - imin;
        for (int i = imax; i >= (imin + 1); i--) {
            xb[shft - i] = lonRho[jmax][i];
            yb[shft - i] = latRho[jmax][i];
        }
        shft = 2 * jmax - jmin + 2 * (imax - imin);
        for (int j = jmax; j >= (jmin + 1); j--) {
            xb[shft - j] = lonRho[j][imin];
            yb[shft - j] = latRho[j][imin];
        }
        xb[nb] = xb[0];
        yb[nb] = yb[0];

        //---------------------------------------------
        //Check if {lon, lat} is inside polygone
        int inc, crossings;
        double dx1, dx2, dxy;
        crossings = 0;

        for (int k = 0; k < nb; k++) {
            if (xb[k] != xb[k + 1]) {
                dx1 = lon - xb[k];
                dx2 = xb[k + 1] - lon;
                dxy = dx2 * (lat - yb[k]) - dx1 * (yb[k + 1] - lat);
                inc = 0;
                if ((xb[k] == lon) & (yb[k] == lat)) {
                    crossings = 1;
                } else if (((dx1 == 0.) & (lat >= yb[k]))
                        | ((dx2 == 0.) & (lat >= yb[k + 1]))) {
                    inc = 1;
                } else if ((dx1 * dx2 > 0.) & ((xb[k + 1] - xb[k]) * dxy >= 0.)) {
                    inc = 2;
                }
                if (xb[k + 1] > xb[k]) {
                    crossings += inc;
                } else {
                    crossings -= inc;
                }
            }
        }
        if (crossings == 0) {
            isInPolygone = false;
        }
        return (isInPolygone);
    }

    public boolean isInWater(int i, int j) {
        try {
            return (maskRho[j][i] > 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    /**
     * Determines whether or not the specified grid point is close to cost line.
     * The method first determines in which quater of the cell the grid point is
     * located, and then checks wether or not its cell and the three adjacent
     * cells to the quater are in water.
     *
     * @param pGrid a double[] the coordinates of the grid point
     * @return <code>true</code> if the grid point is close to cost,
     *         <code>false</code> otherwise.
     */
    @Override
    public boolean isCloseToCost(double[] pGrid) {

        int i, j, ii, jj;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
        jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
        return !(isInWater(i + ii, j) && isInWater(i + ii, j + jj) && isInWater(i, j + jj));
    }

    public int get_nx() {
        return nx;
    }

    public int get_ny() {
        return ny;
    }

    public double getdxi(int j, int i) {
        return dxu[j][i];
    }

    public double getdeta(int j, int i) {
        return dyv[j][i];
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
        depthMax = 0.d;
        int i = nx;
        int j = 0;

        while (i-- > 0) {
            j = ny;
            while (j-- > 0) {
                if (lonRho[j][i] >= lonMax) {
                    lonMax = lonRho[j][i];
                }
                if (lonRho[j][i] <= lonMin) {
                    lonMin = lonRho[j][i];
                }
                if (latRho[j][i] >= latMax) {
                    latMax = latRho[j][i];
                }
                if (latRho[j][i] <= latMin) {
                    latMin = latRho[j][i];
                }
                if (hRho[j][i] >= depthMax) {
                    depthMax = hRho[j][i];
                }
            }
        }
        //System.out.println("lonmin " + lonMin + " lonmax " + lonMax + " latmin " + latMin + " latmax " + latMax);
        //System.out.println("depth max " + depthMax);

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

    public void shrinkGrid() {
        boolean isParamDefined = false;
        try {
            Boolean.valueOf(getParameter("shrink_domain"));
            isParamDefined = true;
        } catch (NullPointerException ex) {
            isParamDefined = false;
        }

        if (isParamDefined && Boolean.valueOf(getParameter("shrink_domain"))) {
            try {
                float lon1 = Float.valueOf(LonLatConverter.convert(getParameter("north-west-corner.lon"), LonLatFormat.DecimalDeg));
                float lat1 = Float.valueOf(LonLatConverter.convert(getParameter("north-west-corner.lat"), LonLatFormat.DecimalDeg));
                float lon2 = Float.valueOf(LonLatConverter.convert(getParameter("south-east-corner.lon"), LonLatFormat.DecimalDeg));
                float lat2 = Float.valueOf(LonLatConverter.convert(getParameter("south-east-corner.lat"), LonLatFormat.DecimalDeg));
                float[] p1 = new float[]{lon1, lat1};
                float[] p2 = new float[]{lon2, lat2};
                range(p1, p2);
            } catch (Exception ex) {
                getLogger().log(Level.WARNING, "Failed to resize domain. " + ex.toString(), ex);
            }
        }
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
            throw new IOException("Impossible to proportion the simulation area : points out of domain");
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
        Array arrLon = null, arrLat = null;
        try {
            if (ncIn.findVariable(strLon).getShape().length > 1) {
                arrLon = ncIn.findVariable(strLon).read(new int[]{jpo, ipo}, new int[]{ny, nx});
            } else {
                arrLon = ncIn.findVariable(strLon).read(new int[]{ipo}, new int[]{nx});
            }
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset longitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            if (ncIn.findVariable(strLat).getShape().length > 1) {
                arrLat = ncIn.findVariable(strLat).read(new int[]{jpo, ipo}, new int[]{ny, nx});
            } else {
                arrLat = ncIn.findVariable(strLat).read(new int[]{jpo}, new int[]{ny});
            }
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset latitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        lonRho = new double[ny][nx];
        latRho = new double[ny][nx];
        if (arrLon.getShape().length > 1) {
            Index index = arrLon.getIndex();
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    index.set(j, i);
                    lonRho[j][i] = arrLon.getDouble(index);
                    if (Math.abs(lonRho[j][i]) > 360) {
                        lonRho[j][i] = Double.NaN;
                    }
                    latRho[j][i] = arrLat.getDouble(index);
                    if (Math.abs(latRho[j][i]) > 90) {
                        latRho[j][i] = Double.NaN;
                    }
                }
            }
        } else {
            Index indexLon = arrLon.getIndex();
            Index indexLat = arrLat.getIndex();
            for (int j = 0; j < ny; j++) {
                indexLat.set(j);
                for (int i = 0; i < nx; i++) {
                    latRho[j][i] = arrLat.getDouble(indexLat);
                    indexLon.set(i);
                    lonRho[j][i] = arrLon.getDouble(indexLon);
                }
            }
        }
        arrLon = null;
        arrLat = null;
    }

    /**
     * Gets domain minimum latitude.
     * @return a double, the domain minimum latitude [north degree]
     */
    public double getLatMin() {
        return latMin;
    }

    /**
     * Gets domain maximum latitude.
     * @return a double, the domain maximum latitude [north degree]
     */
    public double getLatMax() {
        return latMax;
    }

    /**
     * Gets domain minimum longitude.
     * @return a double, the domain minimum longitude [east degree]
     */
    public double getLonMin() {
        return lonMin;
    }

    /**
     * Gets domain maximum longitude.
     * @return a double, the domain maximum longitude [east degree]
     */
    public double getLonMax() {
        return lonMax;
    }

    /**
     * Gets domain maximum depth.
     * @return a float, the domain maximum depth [meter]
     */
    public double getDepthMax() {
        return depthMax;
    }

    /**
     * Gets the latitude at (i, j) grid point.
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the latitude [north degree] at (i, j) grid point.
     */
    public double getLat(int i, int j) {
        return latRho[j][i];
    }

    /**
     * Gets the longitude at (i, j) grid point.
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the longitude [east degree] at (i, j) grid point.
     */
    public double getLon(int i, int j) {
        return lonRho[j][i];
    }

    public enum ErrorMessage {

        INIT("Error reading some dataset variables at initialization"),
        NEXT_STEP("Error reading dataset variables for next timestep"),
        NOT_IN_2D("Method not supported in 2D"),
        TIME_OUTOF_BOUND("Time out of dataset range");
        private String msg;

        ErrorMessage(String msg) {
            this.msg = msg;
        }

        String message() {
            return msg;
        }
    }
}
