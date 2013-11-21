/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import org.previmer.ichthyop.dataset.MarsCommon.ErrorMessage;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class Mercator2dDataset extends AbstractDataset {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
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
    private static int nbTimeRecords;
    /**
     * Longitude at rho point.
     */
    static float[][] lonRho;
    /**
     * Latitude at rho point.
     */
    static float[][] latRho;
    /**
     * Mask: water = 1, cost = 0
     */
    static byte[][] maskRho;
    /**
     * Zonal component of the velocity field at current time
     */
    static float[][] u_tp0;
    /**
     * Zonal component of the velocity field at time t + dt
     */
    static float[][] u_tp1;
    /**
     * Meridional component of the velocity field at current time
     */
    static float[][] v_tp0;
    /**
     * Meridional component of the velocity field at time t + dt
     */
    static float[][] v_tp1;
    /**
     * Geographical boundary of the domain
     */
    private static double latMin, lonMin, latMax, lonMax;
    /**
     * Maximum depth [meter] of the domain
     */
    private static double depthMax;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    static double dt_HyMo;
    /**
     * List on NetCDF input files in which dataset is read.
     */
    //private ArrayList<String> listInputFiles;
    /**
     * Index of the current file read in the {@code listInputFiles}
     */
    private int indexFile;
    /**
     * Time t + dt expressed in seconds
     */
    static double time_tp1;
    /**
     * Current rank in NetCDF dataset
     */
    private static int rank;
    /**
     * Time arrow: forward = +1, backward = -1
     */
    private static int time_arrow;
    /**
     * Name of the Dimension in NetCDF file
     */
    static String strXDim, strYDim, strZDim, strTimeDim;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strU, strV, strTime;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strLon, strLat, strMask;
    /**
     *
     */
    static double[][] e1t, e2t, e1v, e2u;
    static String stre1t, stre2t, stre1v, stre2u;
    private ArrayList<String> listUFiles, listVFiles;
    private static NetcdfFile ncU, ncV;
    private static String file_mask;

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

        int[] origin = new int[]{jpo, ipo};
        int[] size = new int[]{ny, nx};
        NetcdfFile nc;
        nc = NetcdfDataset.openDataset(file_mask);
        getLogger().log(Level.INFO, "read lon, lat & mask from {0}", nc.getLocation());
        //fichier *byte*mask*
        lonRho = (float[][]) nc.findVariable(strLon).read(origin, size).
                copyToNDJavaArray();
        latRho = (float[][]) nc.findVariable(strLat).read(origin, size).
                copyToNDJavaArray();

        maskRho = (byte[][]) nc.findVariable(strMask).
                read(new int[]{0, jpo, ipo}, new int[]{1, ny, nx}).reduce().copyToNDJavaArray();

        //System.out.println("read e1t e2t " + nc.getLocation());
        // fichier *mesh*h*
        e1t = read_e1_e2_field(nc, stre1t);
        e2t = read_e1_e2_field(nc, stre2t);
        e1v = read_e1_e2_field(nc, stre1v);
        e2u = read_e1_e2_field(nc, stre2u);
        nc.close();
    }

    private double[][] read_e1_e2_field(NetcdfFile nc, String varname) throws InvalidRangeException, IOException {

        Variable ncvar = nc.findVariable(varname);
        Array array;
        if (ncvar.getShape().length > 2) {
            array = ncvar.read(new int[]{0, jpo, ipo}, new int[]{1, ny, nx}).reduce();
        } else {
            array = ncvar.read(new int[]{jpo, ipo}, new int[]{ny, nx}).reduce();
        }
        double[][] field = new double[ny][nx];
        Index index = array.getIndex();
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                index.set(j, i);
                field[j][i] = array.getDouble(index);
            }
        }
        return field;
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
     * pverley pour chourdin: ici il faudra revoir les adimensionalisations de u
     * et v par e2u et e1v
     */
    @Override
    public double get_dUx(double[] pGrid, double time) {

        double du = 0.d;
        double ix, jy;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) Math.round(ix);
        int j = (n == 1) ? (int) Math.round(jy) : (int) jy;
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double CO = 0.d;
        double co, x;
        for (int ii = 0; ii < 2; ii++) {
            int ci = i + ii - 1;
            if (ci < 0) {
                ci = nx - 2;
            }
            if (ci > nx - 2) {
                ci = 0;
            }
            for (int jj = 0; jj < n; jj++) {
                co = Math.abs((.5d - (double) ii - dx)
                        * (1.d - (double) jj - dy));
                CO += co;
                if (!(Float.isNaN(u_tp0[j + jj][ci]))) {
                    x = (1.d - x_euler) * u_tp0[j + jj][ci]
                            + x_euler * u_tp1[j + jj][ci];
                    du += x * co / e2u[j + jj][ci];
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
        double ix, jy;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (n == 1) ? (int) Math.round(ix) : (int) ix;
        int j = (int) Math.round(jy);
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double CO = 0.d;
        double co, x;
        for (int jj = 0; jj < 2; jj++) {
            for (int ii = 0; ii < n; ii++) {
                int ci = i + ii;
                if (ci > nx - 2) {
                    ci = 0;
                }
                co = Math.abs((1.d - (double) ii - dx)
                        * (.5d - (double) jj - dy));
                CO += co;
                if (!Float.isNaN(v_tp0[j + jj - 1][ci])) {
                    x = (1.d - x_euler) * v_tp0[j + jj - 1][ci]
                            + x_euler * v_tp1[j + jj - 1][ci];
                    dv += x * co / e1v[j + jj - 1][ci];
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

        int i = (int) Math.round(xRho);
        int j = (int) Math.round(yRho);
        return 2.d * number / (e1t[j][i] + e2t[j][i]);
    }

    /**
     * Reads longitude and latitude fields in NetCDF dataset
     *
     * pverley pour chourdin: même remarque que chaque fois. Les infos dans OPA
     * se trouvent dans différents fichiers, donc selon la méthode appelée, je
     * dois recharger le fichier NetCDF correspondant. Au lieu d'utiliser la
     * variable ncIn globale.
     */
    private void readLonLat() throws IOException {

        NetcdfFile nc;
        Array arrLon, arrLat;

        nc = NetcdfDataset.openDataset(file_mask);
        arrLon = nc.findVariable(strLon).read();
        arrLat = nc.findVariable(strLat).read();
        if (arrLon.getElementType() == float.class) {
            lonRho = (float[][]) arrLon.copyToNDJavaArray();
            latRho = (float[][]) arrLat.copyToNDJavaArray();
        } else {
            lonRho = new float[ny][nx];
            latRho = new float[ny][nx];
            Index index = arrLon.getIndex();
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    index.set(j, i);
                    lonRho[j][i] = arrLon.getFloat(index);
                    latRho[j][i] = arrLat.getFloat(index);
                }
            }
        }
        nc.close();
    }

    /**
     * Gets cell dimension [meter] in the XI-direction.
     *
     * pverley pour chourdin: vérifier avec Steph que je ne me trompe pas dans
     * la définition de e1t et e2t
     * @param j
     * @param i
     * @return 
     */
    @Override
    public double getdxi(int j, int i) {

        return e1t[j][i];
    }

    /**
     * Gets cell dimension [meter] in the ETA-direction.
     * @param j
     * @param i
     * @return 
     */
    @Override
    public double getdeta(int j, int i) {

        return e2t[j][i];
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
        sortInputFiles();
        getDimNC();
        shrinkGrid();
        readConstantField();
        getDimGeogArea();
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
                getLogger().log(Level.WARNING, "Failed to resize domain. " + ex.toString(), ex);
            }
        }
    }

    /**
     * Gets the names of the NetCDF variables from the configuration file.
     *
     * pverley pour chourdin : "Configuration" c'est la classe qui lit le
     * fichier de configuration. Tu vois que normallement, le nom des variables
     * est lu dans le fichier cfg. Temporairement je courcircuite cette
     * opération et je renseigne à la main le nom des variables des sorties OPA.
     */
    @Override
    public void loadParameters() {

        strXDim = getParameter("field_dim_x");
        strYDim = getParameter("field_dim_y");
        strZDim = getParameter("field_dim_z");
        strTimeDim = getParameter("field_dim_time");
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strMask = getParameter("field_var_mask");
        strU = getParameter("field_var_u");
        strV = getParameter("field_var_v");
        strTime = getParameter("field_var_time");
        stre1t = getParameter("field_var_e1t");
        stre2t = getParameter("field_var_e2t");
        stre1v = getParameter("field_var_e1v");
        stre2u = getParameter("field_var_e2u");
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     *
     * @throws an IOException if an error occurs while reading the dimensions.
     *
     * pverley pour chourdin: Pour ROMS ou MARS je lisais les dimensions à
     * partir de la variable ncIn qui est le premier fichier sortie qui me tombe
     * sous la main. Avec OPA, les dimensions se lisent dans un fichier
     * particulier *byte*mask*. A déterminer si toujours vrai ?
     */
    private void getDimNC() throws IOException {

        NetcdfFile nc = new NetcdfDataset();
        nc = NetcdfDataset.openDataset(file_mask);
        try {
            nx = nc.findDimension(strXDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset X dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            ny = nc.findDimension(strYDim).getLength();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset Y dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        ipo = jpo = 0;
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

        readLonLat();

        pGrid1 = latlon2xy(lat1, lon1);
        pGrid2 = latlon2xy(lat2, lon2);
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

        while (i-- > 0) {
            int j = ny;
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
                double depth = getBathy(i, j);
                if (depth > depthMax) {
                    depthMax = depth;
                }
            }
        }

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
     * Initializes the {@code Dataset}. Opens the file holding the first time of
     * the simulation. Checks out the existence of the fields required by the
     * current simulation. Sets all fields at time for the first time step.
     *
     * @throws Exception if a required field cannot be found in the NetCDF
     * dataset.
     */
    @Override
    public void init() throws Exception {

        time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());
        long t0 = getSimulationManager().getTimeManager().get_tO();
        open(indexFile = getIndexFile(t0));
        setAllFieldsTp1AtTime(rank = findCurrentRank(t0));
        time_tp1 = t0;
    }

    /**
     * Reads time dependant variables in NetCDF dataset at specified rank.
     *
     * @param rank an int, the rank of the time dimension in the NetCDF dataset.
     * @throws an IOException if an error occurs while reading the variables.
     *
     * pverley pour chourdin: la aussi je fais du provisoire en attendant de
     * voir si on peut dégager une structure systématique des input.
     */
    void setAllFieldsTp1AtTime(int rank) throws Exception {
        
        getLogger().info("Reading NetCDF variables...");
    
        int[] origin = new int[]{rank, 0, jpo, ipo};
        double time_tp0 = time_tp1;

        try {
            u_tp1 = (float[][]) ncU.findVariable(strU).read(origin, new int[]{1, 1, ny, nx - 1}).reduce().copyToNDJavaArray();
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading U velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            v_tp1 = (float[][]) ncV.findVariable(strV).read(origin, new int[]{1, 1, ny - 1, nx}).reduce().copyToNDJavaArray();
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading V velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            Array xTimeTp1 = ncU.findVariable(strTime).read();
            time_tp1 = xTimeTp1.getDouble(xTimeTp1.getIndex().set(rank));
            time_tp1 -= time_tp1 % 100;
        } catch (IOException ex) {
            IOException ioex = new IOException("Error reading time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
    }

    @Override
    public boolean isInWater(int i, int j) {
        int ci = i;
        if (ci < 0) {
            ci = nx - 1;
        }
        if (ci > nx - 1) {
            ci = 0;
        }
        return (maskRho[j][ci] > 0);
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

        int i, j, k, ii, jj;
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

    /**
     * Transforms the depth at specified x-y particle location into z coordinate
     *
     * @param x a double, the x-coordinate
     * @param y a double, the y-coordinate
     * @param depth a double, the depth of the particle
     * @return a double, the z-coordinate corresponding to the depth
     *
     * pverley pour chourdin: méthode à tester.
     */
    @Override
    public double depth2z(double x, double y, double depth) {
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
    }

    /**
     * Transdorms the specified z-location into depth
     *
     * pverley pour chourdin: j'ai testé z2depth et depth2z, ça à l'air de
     * marcher mais il faudra faire une validation plus sérieuse.
     *
     * @param x double
     * @param y double
     * @param z double
     * @return double
     */
    @Override
    public double z2depth(double x, double y, double z) {
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
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
        double latitude = 0.d;
        double longitude = 0.d;
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
                latitude += co * latRho[j + jj][cii];
                if (Math.abs(lonRho[j + jj][cii] - lonRho[j][ci]) < 180) {
                    longitude += co * lonRho[j + jj][cii];
                } else {
                    double dlon = Math.abs(360.d - Math.abs(lonRho[j + jj][cii] - lonRho[j][ci]));
                    if (lonRho[j][ci] < 0) {
                        longitude += co * (lonRho[j][ci] - dlon);
                    } else {
                        longitude += co * (lonRho[j][ci] + dlon);
                    }
                }
            }
        }

        return (new double[]{latitude, longitude});
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
            double dmin = DatasetUtil.geodesicDistance(lat, lon, latRho[j][i], lonRho[j][i]);
            for (int ii = -di; ii <= di; ii += di) {
                if ((i + ii >= 0) && (i + ii < nx)) {
                    double d = DatasetUtil.geodesicDistance(lat, lon, latRho[j][i + ii], lonRho[j][i + ii]);
                    if (d < dmin) {
                        dmin = d;
                        ci = i + ii;
                        cj = j;
                    }
                }
            }
            for (int jj = -dj; jj <= dj; jj += dj) {
                if ((j + jj >= 0) && (j + jj < ny)) {
                    double d = DatasetUtil.geodesicDistance(lat, lon, latRho[j + jj][i], lonRho[j + jj][i]);
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

        // Try to refine within cell (ci, cj)
        int cip1 = ci + 1 > nx - 1 ? 0 : ci + 1;
        //--------------------------------------------
        // Trilinear interpolation
        double dy1 = latRho[cj + 1][ci] - latRho[cj][ci];
        double dx1 = lonRho[cj + 1][ci] - lonRho[cj][ci];
        double dy2 = latRho[cj][cip1] - latRho[cj][ci];
        double dx2 = (Math.abs(lonRho[cj][cip1] - lonRho[cj][ci]) > 180.d)
                ? 360.d + (lonRho[cj][cip1] - lonRho[cj][ci])
                : lonRho[cj][cip1] - lonRho[cj][ci];

        double c1 = lon * dy1 - lat * dx1;
        double c2 = lonRho[cj][ci] * dy2 - latRho[cj][ci] * dx2;
        double deltax = (c1 * dx2 - c2 * dx1) / (dx2 * dy1 - dy2 * dx1);
        deltax = (deltax - lonRho[cj][ci]) / dx2;
        double xgrid = (double) ci + Math.min(Math.max(0.d, deltax), 1.d);

        c1 = lonRho[cj][ci] * dy1 - latRho[cj][ci] * dx1;
        c2 = lon * dy2 - lat * dx2;
        double deltay = (c1 * dy2 - c2 * dy1) / (dx2 * dy1 - dy2 * dx1);
        deltay = (deltay - latRho[cj][ci]) / dy1;
        double ygrid = (double) cj + Math.min(Math.max(0.d, deltay), 1.d);

        return (new double[]{xgrid, ygrid});
    }

    /**
     * Gets the list of NetCDF input files that satisfy the file filter and
     * sorts them according to the chronological order induced by the
     * {@code NCComparator}.
     *
     * @param path a String, the path of the folder that contains the model
     * input files.
     * @return an ArrayList, the list of the input files sorted in time.
     * @throws an IOException if an exception occurs while scanning the input
     * files.
     */
    private ArrayList<String> getInputList(String path, String fileMask) throws IOException {

        File inputPath = new File(path);
        //String fileMask = Configuration.getFileMask();
        File[] listFile = inputPath.listFiles(new MetaFilenameFilter(fileMask));
        if (listFile.length == 0) {
            throw new IOException(path + " contains no file matching mask " + fileMask);
        }
        ArrayList<String> list = new ArrayList(listFile.length);
        for (File file : listFile) {
            list.add(file.toString());
        }
        if (list.size() > 1) {
            boolean skipSorting;
            try {
                skipSorting = Boolean.valueOf(getParameter("skip_sorting"));
            } catch (Exception ex) {
                skipSorting = false;
            }
            if (skipSorting) {
                Collections.sort(list);
            } else {
                Collections.sort(list, new NCComparator(strTime));
            }
        }
        return list;
    }

    /**
     * Sort OPA input files. First make sure that there is at least and only one
     * file matching the hgr, zgr and byte mask patterns. Then list the gridU,
     * gridV and gridT files.
     *
     * @param path
     * @throws java.io.IOException
     */
    private void sortInputFiles() throws IOException {

        String path = IOTools.resolvePath(getParameter("input_path"));
        File file = new File(path);

        file_mask = checkExistenceAndUnicity(file, getParameter("byte_mask_pattern"));
        listUFiles = getInputList(path, getParameter("gridu_pattern"));
        listVFiles = getInputList(path, getParameter("gridv_pattern"));
    }

    private String checkExistenceAndUnicity(File file, String pattern) throws IOException {

        File[] listFiles = file.listFiles(new MetaFilenameFilter(pattern));
        int nbFiles = listFiles.length;

        if (nbFiles == 0) {
            throw new IOException("No file matching pattern " + pattern);
        } else if (nbFiles > 1) {
            throw new IOException("More than one file matching pattern " + pattern);
        }

        return listFiles[0].toString();
    }

    /**
     * Loads the NetCDF dataset from the specified filename.
     *
     * @param filename a String that can be a local pathname or an OPeNDAP URL.
     * @throws IOException
     */
    private void open(int index) throws IOException {

        getLogger().info("Opening NEMO dataset");
        if (ncU != null) {
            ncU.close();
        }
        ncU = NetcdfDataset.openDataset(listUFiles.get(index));
        if (ncV != null) {
            ncV.close();
        }
        ncV = NetcdfDataset.openDataset(listVFiles.get(index));

        nbTimeRecords = ncU.findDimension(strTimeDim).getLength();
    }

    private int getIndexNextFile(long time, int indexCurrent) throws IOException {

        int index = indexCurrent - (1 - time_arrow) / 2;
        boolean noNext = (listUFiles.size() == 1) || (index < 0)
                || (index >= listUFiles.size() - 1);
        if (noNext) {
            throw new IOException("Unable to find any file following "
                    + listUFiles.get(indexCurrent));
        }
        if (isTimeBetweenFile(time, index)) {
            return indexCurrent + time_arrow;
        }
        throw new IOException("Unable to find any file following "
                + listUFiles.get(indexCurrent));
    }

    private int getIndexFile(long time) throws IOException {

        int indexLast = listUFiles.size() - 1;

        for (int i = 0; i < indexLast; i++) {
            if (isTimeIntoFile(time, i)) {
                return i;
            } else if (isTimeBetweenFile(time, i)) {
                return (i - (time_arrow - 1) / 2);
            }
        }

        if (isTimeIntoFile(time, indexLast)) {
            return indexLast;
        }

        throw new IOException("Time value " + (long) time + " not contained among NetCDF files");
    }

    /**
     * Determines whether or not the specified time is contained within the ith
     * input file.
     *
     * @param time a long, the current time [second] of the simulation
     * @param index an int, the index of the file in the {@code listInputFiles}
     * @return <code>true</code> if time is contained within the file
     * <code>false</code>
     * @throws an IOException if an error occurs while reading the input file
     */
    private boolean isTimeIntoFile(long time, int index) throws IOException {

        String filename = "";
        NetcdfFile nc;
        Array timeArr;
        long time_r0, time_rf;

        try {
            filename = listUFiles.get(index);
            nc = NetcdfDataset.openDataset(filename);
            timeArr = nc.findVariable(strTime).read();
            time_r0 = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(0)));
            time_rf = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(
                    timeArr.getShape()[0] - 1)));
            nc.close();

            return (time >= time_r0 && time < time_rf);
            /*switch (time_arrow) {
             case 1:
             return (time >= time_r0 && time < time_rf);
             case -1:
             return (time > time_r0 && time <= time_rf);
             }*/
        } catch (IOException e) {
            throw new IOException("Problem reading file " + filename + " : "
                    + e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("Unable to read " + strTime
                    + " variable in file " + filename + " : "
                    + e.getCause());
        }
        //return false;

    }

    /**
     * Determines whether or not the specified time is contained between the ith
     * and the (i+1)th input files.
     *
     * @param time a long, the current time [second] of the simulation
     * @param index an int, the index of the file in the {@code listInputFiles}
     * @return <code>true</code> if time is contained between the two files
     * <code>false</code> otherwise.
     * @throws an IOException if an error occurs while reading the input files
     */
    private boolean isTimeBetweenFile(long time, int index) throws IOException {

        NetcdfFile nc;
        String filename = "";
        Array timeArr;
        long[] time_nc = new long[2];

        try {
            for (int i = 0; i < 2; i++) {
                filename = listUFiles.get(index + i);
                nc = NetcdfDataset.openDataset(filename);
                timeArr = nc.findVariable(strTime).read();
                time_nc[i] = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(
                        0)));
                nc.close();
            }
            if (time >= time_nc[0] && time < time_nc[1]) {
                return true;
            }
        } catch (IOException e) {
            throw new IOException("Problem reading file " + filename + " : "
                    + e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("Unable to read " + strTime
                    + " variable in file " + filename + " : "
                    + e.getCause());
        }
        return false;
    }

    /**
     * Finds the index of the dataset time variable such as      <code>time(rank) <= time < time(rank + 1)
     *
     * @param time a long, the current time [second] of the simulation
     * @return an int, the current rank of the NetCDF dataset for time dimension
     * @throws an IOException if an error occurs while reading the input file
     *
     * pverley pour chourdin: remplacer ncIn par le fichier OPA concerné.
     */
    int findCurrentRank(long time) throws Exception {

        int lrank = 0;
        long time_rank;
        Array timeArr;
        try {
            timeArr = ncU.findVariable(strTime).read();
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
        return ((pGrid[1] > (ny - 2.0f))
                || (pGrid[1] < 1.0f));
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
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
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

        long time = e.getSource().getTime();

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        rank += time_arrow;

        if (rank > (nbTimeRecords - 1) || rank < 0) {
            open(indexFile = getIndexNextFile(time, indexFile));
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }

        setAllFieldsTp1AtTime(rank);

    }

    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        Variable variable = nc.findVariable(name);
        int[] origin = null, shape = null;
        boolean hasVerticalDim = false;
        switch (variable.getShape().length) {
            case 4:
                origin = new int[]{rank, 0, jpo, ipo};
                shape = new int[]{1, 1, ny, nx};
                hasVerticalDim = true;
                break;
            case 2:
                origin = new int[]{jpo, ipo};
                shape = new int[]{ny, nx};
                break;
            case 3:
                if (!variable.isUnlimited()) {
                    origin = new int[]{0, jpo, ipo};
                    shape = new int[]{1, ny, nx};
                    hasVerticalDim = true;
                } else {
                    origin = new int[]{rank, jpo, ipo};
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
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
    }
}
