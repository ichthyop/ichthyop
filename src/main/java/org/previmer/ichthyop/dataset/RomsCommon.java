
package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.logging.Level;
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDatasets;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
abstract class RomsCommon extends AbstractDataset {

    /**
     * Grid dimension
     */
    static int nx, ny;
    /**
     * Origin for grid index
     */
    static int ipo, jpo;
    /**
     * Number of time records in current NetCDF file
     */
    static int nbTimeRecords;
    /**
     * Longitude at rho point.
     */
    static double[][] lonRho;
    /**
     * Latitude at rho point.
     */
    static double[][] latRho;
    /**
     * Bathymetry
     */
    static double[][] hRho;
    /**
     * Mask: water = 1, cost = 0
     */
    static byte[][] maskRho;
    /**
     *
     */
    double[][] pm, pn;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    static double dt_HyMo;
    /**
     * Time t + dt expressed in seconds
     */
    static double time_tp1;
    /**
     * Current rank in NetCDF dataset
     */
    static int rank;
    /**
     *
     */
    static NetcdfFile ncIn;
    /**
     * Name of the Dimension in NetCDF file
     */
    static String strXiDim, strEtaDim, strTimeDim;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strU, strV, strTime;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strLon, strLat, strMask, strBathy;
    /**
     * Name of the Variable in NetCDF file
     */
    private String strPn, strPm;
    /**
     * Determines whether or not the turbulent diffusivity should be read in the
     * NetCDF file, function of the user's options.
     */
    String gridFile;
    /**
     * Geographical boundary of the domain
     */
    private double latMin, lonMin, latMax, lonMax, depthMax;

    abstract void setAllFieldsTp1AtTime(int rank) throws Exception;

    abstract void openDataset() throws Exception;

    abstract void setOnFirstTime() throws Exception;

    @Override
    void loadParameters() {

        strXiDim = getParameter("field_dim_xi");
        strEtaDim = getParameter("field_dim_eta");
        strTimeDim = getParameter("field_dim_time");
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strBathy = getParameter("field_var_bathy");
        strMask = getParameter("field_var_mask");
        strU = getParameter("field_var_u");
        strV = getParameter("field_var_v");
        strTime = getParameter("field_var_time");
        strPn = getParameter("field_var_pn");
        strPm = getParameter("field_var_pm");
    }

    public void shrinkGrid() {
        boolean isParamDefined;
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
                range(lat1, lon1, lat2, lon2);
            } catch (IOException | NumberFormatException ex) {
                getLogger().log(Level.WARNING, "Failed to resize domain", ex);
            }
        }
    }

    @Override
    public void setUp() throws Exception {

        loadParameters();
        clearRequiredVariables();
        openDataset();
        //openLocation(getParameter("input_path"));
        getDimNC();
        shrinkGrid();
        readConstantField(gridFile);
        getDimGeogArea();
    }

    @Override
    public void init() throws Exception {

        setOnFirstTime();
        checkRequiredVariable(ncIn);
        setAllFieldsTp1AtTime(rank);
    }

    void readConstantField(String gridFile) throws IOException {

        // Read grid variables on the outer domain.
        // T points are read from 0 to nx - 1. nx is the nunber
        // of points including outer domain
        int[] origin = new int[]{jpo, ipo};
        int[] size = new int[]{ny, nx};
        Array arrLon, arrLat, arrMask, arrH, arrPm, arrPn;
        Index index;

        NetcdfFile ncGrid = NetcdfDatasets.openDataset(gridFile);
        try {
            arrLon = ncGrid.findVariable(strLon).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset longitude. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            arrLat = ncGrid.findVariable(strLat).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset latitude. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            arrMask = ncGrid.findVariable(strMask).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset mask. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            arrH = ncGrid.findVariable(strBathy).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset bathymetry. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        try {
            // for pm and pn, we need to read on the extended T domain
            // i.e. include halo cells
            // since it is used in the interpolation of U and V.
            arrPm = ncGrid.findVariable(strPm).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset pm metrics. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }

        try {
            arrPn = ncGrid.findVariable(strPn).read(origin, size);
        } catch (IOException | InvalidRangeException e) {
            IOException ioex = new IOException("Problem reading dataset pn metrics. " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        ncGrid.close();

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

        maskRho = new byte[ny][nx];
        index = arrMask.getIndex();
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                maskRho[j][i] = arrMask.getByte(index.set(j, i));
            }
        }

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

        hRho = new double[ny][nx];
        index = arrH.getIndex();
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                hRho[j][i] = arrH.getDouble(index.set(j, i));
            }
        }

    }

    /**
     * Reads the dimensions of the NetCDF dataset
     *
     * @throws an IOException if an error occurs while reading the dimensions.
     */
    void getDimNC() throws Exception {

        NetcdfFile ncGrid = NetcdfDatasets.openDataset(gridFile);

        try {
            // nx is the total number of T grid points, including the outer domain
            nx = ncGrid.findDimension(strXiDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset grid dimensions XI. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            // ny is the total number of T grid points, including the outer domain
            ny = ncGrid.findDimension(strEtaDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset grid dimensions ETA. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        ipo = jpo = 0;
    }

    @Override
    public double[] latlon2xy(double lat, double lon) {

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

    @Override
    public double[] xy2latlon(double xRho, double yRho) {

        // Same interpolation as for T points

        //--------------------------------------------------------------------
        // Computational space (x, y , z) => Physical space (lat, lon, depth)
        final double ix = Math.max(0.00001f, Math.min(xRho, (double) nx - 1.00001f));
        final double jy = Math.max(0.00001f, Math.min(yRho, (double) ny - 1.00001f));

        final int i = (int) Math.floor(ix);
        final int j = (int) Math.floor(jy);
        double latitude = 0.d;
        double longitude = 0.d;
        // final double dx = ix - (double) i;
        // final double dy = jy - (double) j;
        double co = 0;
        for (int ii = 0; ii < 2; ii++) {
            double cox = 1 - Math.abs(ix - (i + ii));
            for (int jj = 0; jj < 2; jj++) {
                double coy = 1 - Math.abs(jy - (j + jj));
                co = cox * coy;
                latitude += co * latRho[j + jj][i + ii];
                longitude += co * lonRho[j + jj][i + ii];
            }
        }
        return (new double[]{latitude, longitude});
    }

    @Override
    public boolean isInWater(double[] pGrid) {
        return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    @Override
    /** This function determines whether we are out of the domain.    (non-Javadoc)
     *
     * This is the case when U, V, W or T points cannot be interpolated. For Roms/Croco,
     * this occurs when we are out of the U and V limits.
     *
     * @see org.previmer.ichthyop.dataset.IDataset#isOnEdge(double[])
     */
    public boolean isOnEdge(double[] pGrid) {
        return ((pGrid[0] > (nx - 1.5f))
                || (pGrid[0] < 0.5f)
                || (pGrid[1] > (ny - 1.5f))
                || (pGrid[1] < 0.5f));
    }

    @Override
    public double getBathy(int i, int j) {
        if (isInWater(i, j)) {
            return hRho[j][i];
        }
        return Double.NaN;
    }

    @Override
    public int get_nx() {
        return nx;
    }

    @Override
    public int get_ny() {
        return ny;
    }

    @Override
    public double getdxi(int j, int i) {
        return (pm[j][i] != 0) ? (1 / pm[j][i]) : 0.d;
    }

    @Override
    public double getdeta(int j, int i) {
        return (pn[j][i] != 0) ? (1 / pn[j][i]) : 0.d;
    }

    private boolean isInsidePolygone(int imin, int imax, int jmin, int jmax, double lon, double lat) {

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

    @Override
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
     * <code>false</code> otherwise.
     */
    @Override
    public boolean isCloseToCost(double[] pGrid) {

        int i, j, ii, jj;

        // Find the T cell index in which the particle is located
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        double ix = pGrid[0];
        double jy = pGrid[1];

        if (jy > j) {
            // if the particle is north of the center of T cell
            // take cell above
            jj = 1;
        } else {
            // else take cell below
            jj = -1;
        }

        if (ix > i) {
            ii = 1;
        } else {
            ii = -1;
        }

        return !(isInWater(i + ii, j) && isInWater(i + ii, j + jj) && isInWater(i, j + jj));
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

    /**
     * Reads longitude and latitude fields in NetCDF dataset
     */
    void readLonLat(String gridFile) throws IOException {

        int[] start = new int[] { rank, jpo, ipo };
        int[] count = new int[] { rank, ny, nx };
        Array arrLon, arrLat;
        NetcdfFile ncGrid = NetcdfDatasets.openDataset(gridFile);
        try {
            arrLon = ncIn.findVariable(strLon).read(start, count);
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading dataset longitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            arrLat = ncIn.findVariable(strLat).read(start, count);
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading dataset latitude. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        ncGrid.close();

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

    /**
     * Resizes the domain and determines the range of the grid indexes taht will
     * be used in the simulation. The new domain is limited by the Northwest and
     * the Southeast corners.
     *
     * @param pGeog1 a float[], the geodesic coordinates of the domain Northwest
     * corner
     * @param pGeog2 a float[], the geodesic coordinates of the domain Southeast
     * corner
     * @throws an IOException if the new domain is not strictly nested within
     * the NetCDF dataset domain.
     */
    private void range(double lat1, double lon1, double lat2, double lon2) throws IOException {

        double[] pGrid1, pGrid2;
        int ipn, jpn;

        // Read lon/lat in the inner domain
        readLonLat(gridFile);

        // Gives the coordinates of the shrinked domain in the inner domain coordinates
        pGrid1 = latlon2xy(lat1, lon1);
        pGrid2 = latlon2xy(lat2, lon2);
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


        // This is the total number of T points that will be read
        nx = Math.min(nx, ipn - ipo + 1);
        ny = Math.min(ny, jpn - jpo + 1);
        //System.out.println("ipo " + ipo + " nx " + nx + " jpo " + jpo + " ny " + ny);
    }

    /**
     * Determines the geographical boundaries of the domain in longitude,
     * latitude and depth.
     */
    private void getDimGeogArea() {

        //--------------------------------------
        // Calculate the Physical Space extrema
        lonMin = Double.MAX_VALUE;
        lonMax = -lonMin;
        latMin = Double.MAX_VALUE;
        latMax = -latMin;
        depthMax = 0.d;
        int i = nx;
        int j;

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
        return depthMax;
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
        return latRho[j][i];
    }

    /**
     * Gets the longitude at (i, j) grid point.
     *
     * @param i an int, the i-ccordinate
     * @param j an int, the j-coordinate
     * @return a double, the longitude [east degree] at (i, j) grid point.
     */
    @Override
    public double getLon(int i, int j) {
        return lonRho[j][i];
    }

    @Override
    public double xTore(double x) {
        return x;
    }


    @Override
    public double yTore(double y) {
        return y;
    }

    /** Interpolates the pn variable into U points
     * i = index of the U cell
     * j = index of the U cell
     *
     * the T cell of index i is on the left of the U cell
     *
     */
    public double get_pnu(int i, int j) {
        return 0.5 * (pn[j][i] + pn[j][i + 1]);
    }

    /** Interpolates the pm variable on U points */
    public double get_pmu(int i, int j) {
        return 0.5 * (pm[j][i] + pm[j][i + 1]);
    }

    /**
     * Interpolates the pn variable onto V points
     *
     * @param i i Index of the V point
     * @param j j index of the V point
     */
    public double get_pnv(int i, int j) {
        return 0.5 * (pn[j][i] + pn[j + 1][i]);
    }

    /**
     * Interpolates the pm variable onto V points
     *
     * @param i i Index of the V point
     * @param j j index of the V point
     */
    public double get_pmv(int i, int j) {
        return 0.5 * (pm[j][i] + pm[j + 1][i]);
    }

    /**
     * Returns the equivalent of the e1u function in Nemo
     *
     * It first interpolates the pm value onto U face, then
     * returns the inverse
     *
     * @param i i index of the U cell
     * @param j j index of the U cell
     * @return
     */
    public double get_e1u(int i, int j) {
        return 1 / get_pmu(i, j);
    }

    /**
     * Returns the equivalent of the e1v function in Nemo
     *
     * It first interpolates the pm value onto V face, then
     * returns the inverse
     *
     * @param i i index of the V cell
     * @param j j index of the V cell
     * @return
     */
    public double get_e1v(int i, int j) {
        return 1 / get_pmv(i, j);
    }

    /**
     * Returns the equivalent of the e2u function in Nemo
     *
     * It first interpolates the pn value onto U face, then
     * returns the inverse
     *
     * @param i i index of the U cell
     * @param j j index of the U cell
     * @return
     */
    public double get_e2u(int i, int j) {
        return 1 / get_pnu(i, j);
    }

    /**
     * Returns the equivalent of the e2v function in Nemo
     *
     * It first interpolates the pn value onto V face, then
     * returns the inverse
     *
     * @param i i index of the V cell
     * @param j j index of the V cell
     * @return
     */
    public double get_e2v(int i, int j) {
        return 1 / get_pnv(i, j);
    }

}