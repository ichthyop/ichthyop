/*
 * Copyright (C) 2012 mtravers
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
import java.util.logging.Level;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author mtravers fevrier 2012 pour pouvoir lire des fichiers netcdf issu du
 * modele hydro GETM (fourni par CEFAS)
 */
public class GetmDataset extends AbstractDataset {

    /**
     * Number of time records in NetCDF dataset
     */
    int nbTimeRecords;
    /**
     * Ocean free surface elevetation at current time
     */
    static float[][] elev_tp0;
    /**
     * /**
     * Ocean free surface elevetation at time t + dt
     */
    static float[][] elev_tp1;
    /**
     * Grid dimension
     */
    int nx, ny, nz;
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
    private static double latMin, lonMin, latMax, lonMax;
    /**
     * Maximum depth [meter] of the domain
     */
    private static double depthMax;
    /**
     * Name of the Dimension in NetCDF file
     */
    static String strLonDim, strLatDim, strTimeDim, strZDim;
    /**
     * __________________________________SPECIFICITE GETM 
     */
    static String strh, strW, strElev;
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
     * hauteur des cellules verticales, correspond au h du fichier
     * netcdf________________________________input Morgane
     */
    float[][][] h_tp0, h_tp1;
    double dzw;
    /**
     *
     */
    private String gridFile;
    /**
     * Zonal component of the velocity field at current time
     */
    static float[][][] u_tp0;
    /**
     * Zonal component of the velocity field at time t + dt
     */
    static float[][][] u_tp1;
    /**
     * Meridional component of the velocity field at current time
     */
    static float[][][] v_tp0;
    /**
     * Meridional component of the velocity field at time t + dt
     */
    static float[][][] v_tp1;
    /**
     * Vertical component of the velocity field at current time
     */
    static float[][][] w_tp0;
    /**
     * Vertical component of the velocity field at time t + dt
     */
    static float[][][] w_tp1;
    /**
     *
     */
    NetcdfFile ncIn;
    /**
     * List on NetCDF input files in which dataset is read.
     */
    ArrayList<String> listInputFiles;
    /**
     * Time arrow: forward = +1, backward = -1
     */
    private static int time_arrow;
    /**
     * Index of the current file read in the {@code listInputFiles}
     */
    private int indexFile;
    /**
     * Time t + dt expressed in seconds
     */
    static double time_tp1;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    static double dt_HyMo;
    /**
     * Current rank in NetCDF dataset
     */
    private static int rank;

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

        //  LE VERTICAL : CF NEMO en partie-----------------------------------------------------> les noms ne sont pas déclarés
        strZDim = getParameter("field_dim_level");  // dimension verticale
        strh = getParameter("field_var_h");     // hauteur de chaque couche d'eau
        strW = getParameter("field_var_w");     // vitesse verticale
        strElev = getParameter("field_var_elev");   // elevation
    }

    @Override
    public void setUp() throws Exception {
        loadParameters();
        clearRequiredVariables();
        openLocation(getParameter("input_path"));
        getDimNC();
        shrinkGrid();
        readConstantField();
        getDimGeogArea();
    }

    private void openLocation(String rawPath) throws IOException {

        String path = IOTools.resolvePath(rawPath);

        if (isDirectory(path)) {
            listInputFiles = getInputList(path);
            try {
                if (!getParameter("grid_file").isEmpty()) {
                    gridFile = getGridFile(getParameter("grid_file"));  //______________________qu'est-ce qu'il y a dans grid_file ?
                } else {
                    gridFile = listInputFiles.get(0);
                }
            } catch (NullPointerException ex) {
                gridFile = listInputFiles.get(0);
            }
        }
        open(listInputFiles.get(0));
    }

    // vient de RomsCommon
    private String getFile(long time) throws IOException {

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

        throw new IOException("Time value " + (long) time + " not contained among NetCDF files " + getParameter("file_filter") + " of folder " + getParameter("input_path"));
    }

    private boolean isTimeIntoFile(long time, int index) throws IOException {

        String filename = "";
        NetcdfFile nc;
        Array timeArr;
        long time_r0, time_rf;

        try {
            filename = listInputFiles.get(index);
            nc = NetcdfDataset.openFile(filename, null);
            timeArr = nc.findVariable(strTime).read();
            time_r0 = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(0)));
            time_rf = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(timeArr.getShape()[0] - 1)));
            nc.close();

            return (time >= time_r0 && time < time_rf);
            /*
             * switch (time_arrow) { case 1: return (time >= time_r0 && time <
             * time_rf); case -1: return (time > time_r0 && time <= time_rf);
            }
             */
        } catch (IOException e) {
            IOException ioex = new IOException("Problem reading file " + filename + " : " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        } catch (NullPointerException e) {
            IOException ioex = new IOException("Unable to read " + strTime + " variable in file " + filename + " : " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }

    }

    private boolean isTimeBetweenFile(long time, int index) throws IOException {

        NetcdfFile nc;
        String filename = "";
        Array timeArr;
        long[] time_nc = new long[2];

        try {
            for (int i = 0; i < 2; i++) {
                filename = listInputFiles.get(index + i);
                nc = NetcdfDataset.openFile(filename, null);
                timeArr = nc.findVariable(strTime).read();
                time_nc[i] = DatasetUtil.skipSeconds(timeArr.getLong(timeArr.getIndex().set(0)));
                nc.close();
            }
            if (time >= time_nc[0] && time < time_nc[1]) {
                return true;
            }
        } catch (IOException e) {
            IOException ioex = new IOException("Problem reading file " + filename + " : " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        } catch (NullPointerException e) {
            IOException ioex = new IOException("Unable to read " + strTime + " variable in file " + filename + " : " + e.toString());
            ioex.setStackTrace(e.getStackTrace());
            throw ioex;
        }
        return false;
    }

    // vient de RomsCommon
    String getNextFile(int time_arrow) throws IOException {

        int index = indexFile - (1 - time_arrow) / 2;
        boolean noNext = (listInputFiles.size() == 1) || (index < 0) || (index >= listInputFiles.size() - 1);
        if (noNext) {
            throw new IOException("Unable to find any file following " + listInputFiles.get(indexFile));
        }
        indexFile += time_arrow;
        return listInputFiles.get(indexFile);
    }
    // vient de RomsCommon

    private ArrayList<String> getInputList(String path) throws IOException {

        ArrayList<String> list = null;

        File inputPath = new File(path);
        String fileMask = getParameter("file_filter");
        File[] listFile = inputPath.listFiles(new MetaFilenameFilter(fileMask));
        if (listFile.length == 0) {
            throw new IOException(path + " contains no file matching mask " + fileMask);
        }
        list = new ArrayList<String>(listFile.length);
        for (File file : listFile) {
            list.add(file.toString());
        }
        if (list.size() > 1) {
            boolean skipSorting = false;
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

    void open(String filename) throws IOException {
        if (ncIn == null || (new File(ncIn.getLocation()).compareTo(new File(filename)) != 0)) {
            if (ncIn != null) {
                ncIn.close();
            }
            try {
                ncIn = NetcdfDataset.openFile(filename, null);
            } catch (Exception ex) {
                IOException ioex = new IOException("Error opening dataset " + filename + " ==> " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
            try {
                nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
            } catch (Exception ex) {
                IOException ioex = new IOException("Error dataset time dimension ==> " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
        }
        getLogger().log(Level.INFO, "Opened dataset {0}", filename);
    }

    private boolean isDirectory(String location) throws IOException {

        File f = new File(location);
        if (!f.isDirectory()) {
            throw new IOException(location + " is not a valid directory.");
        }
        return f.isDirectory();
    }

    private String getGridFile(String rawFile) throws IOException {

        File filename = new File(IOTools.resolveFile(rawFile));
        if (!filename.exists()) {
            throw new IOException("Grid file " + filename + " does not exist");
        }
        return filename.toString();
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     *
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
        /*
         * read the vertical dimension dans Getm, la dimension verticale est 26,
         * mais il n'y a que 25 couches (la premiere couche est remplie de NaN
         * pour les variables u, v, h...) ca sert surtout à la variable w qui
         * elle est fournies pour les points W (donc 26 points) avec la premiere
         * couche égale à 0 car vitesse nulle au fond
         */
        try {
            //  nz = ncIn.findDimension(strZDim).getLength();
            nz = ncIn.findDimension(strZDim).getLength() - 1;                       // modif Morgane pour ramener nz au nb reel de couches d'eau
        } catch (Exception ex) {
            IOException ioex = new IOException("Failed to read dataset vertical dimension. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
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
                range(lat1, lon1, lat2, lon2);
            } catch (Exception ex) {
                getLogger().log(Level.WARNING, "Failed to resize domain. " + ex.toString(), ex);
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
    void range(double lat1, double lon1, double lat2, double lon2) throws IOException {

        double[] pGrid1, pGrid2;
        int ipn, jpn;

        readLonLat();

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

        nx = Math.min(nx, ipn - ipo + 1);
        ny = Math.min(ny, jpn - jpo + 1);
        //System.out.println("ipo " + ipo + " nx " + nx + " jpo " + jpo + " ny " + ny);
    }

    void readConstantField() throws Exception {

        Array arrH = null;
        Index index;
        lonRho = new double[ny][nx];
        latRho = new double[ny][nx];
        maskRho = new byte[ny][nx];
        dxu = new double[ny][nx];
        dyv = new double[ny][nx];

        /*
         * Read longitude & latitude
         */
        readLonLat();

        /*
         * Read bathymetry
         */
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

        /*
         * Compute mask
         */
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                maskRho[j][i] = (hRho[j][i] <0) 
                        ? (byte) 0
                        : (byte) 1;
                
            }
        }
  /*      
        int tempX = 0;
        int tempY = 0;
        System.out.print(tempY);System.out.print(" : ");System.out.print(hRho[tempY][tempX]);System.out.print("  et mask : ");System.out.println(maskRho[tempY][tempX]);
        tempX = 0;
        tempY = 40;
        System.out.print(tempY);System.out.print(" : ");System.out.print(hRho[tempY][tempX]);System.out.print("  et mask : ");System.out.println(maskRho[tempY][tempX]);
        tempX = 40;
        tempY = 0;
        System.out.print(tempY);System.out.print(" : ");System.out.print(hRho[tempY][tempX]);System.out.print("  et mask : ");System.out.println(maskRho[tempY][tempX]);
        tempX = 100;
        tempY = 100;
        System.out.print(tempY);System.out.print(" : ");System.out.print(hRho[tempY][tempX]);System.out.print("  et mask : ");System.out.println(maskRho[tempY][tempX]);
*/
        /*
         * Compute metrics dxu & dyv
         */
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
        /*
         * Boundary conditions
         */
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

        /**
         * read vertical information
         *
         * vertical velocity W and thickness of each layer h vary with time,
         * they are read in setAllFieldsTp1AtTime() h is used directly to
         * compute dzw
         */
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
     * Determines the geographical boundaries of the domain in longitude,
     * latitude and depth.--------------> vient de NEMO
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

    // vient de mars 3d, mais getDepth est different
    @Override
    public double depth2z(double x, double y, double depth) {
        //-----------------------------------------------
        // Return z[grid] corresponding to depth[meters]
        double z = 0.d;
        int lk = nz - 1;

        while ((lk > 0) && (getDepth(x, y, lk) > depth)) {
            lk--;
        }
        if (lk == (nz - 1)) {
            z = (double) lk;
        } else {
            double pr = getDepth(x, y, lk);
            z = Math.max(0.d,
                    (double) lk
                    + (depth - pr) / (getDepth(x, y, lk + 1) - pr));
        }
        return (z);
    }

    /**
     * Computes the depth of the specified sigma level at the x-y particle
     * location.
     *
     * @param xRho a double, x-coordinate of the grid point
     * @param yRho a double, y-coordinate of the grid point
     * @param k an int, the index of the sigma level
     * @return a double, the depth [meter] at (x, y, k)
     */
    double getDepth(double xRho, double yRho, int k) {

        final int i = (int) xRho;
        final int j = (int) yRho;
        double hh = 0.d;
        final double dx = (xRho - i);
        final double dy = (yRho - j);
        double co = 0.d;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                if (isInWater(i + ii, j + jj)) {
                    co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                    // il faut réduire la taille du tableau (enlever les dimensions i et j)
                    float[] h_temp = new float[nz];
                    for (int ktemp = 0; ktemp < nz; ktemp++) {
                        h_temp[ktemp] = h_tp0[ktemp][j + jj][i + ii];
                    }
                    double z_r = computeLocalDepth(i + ii, j + jj, h_temp, k);        // ____________________________________ici Modif Morgane
                    hh += co * z_r;
                }
            }
        }
        return (hh);
    }

    // vient de mars 3d, mais un peu different
    @Override
    public double z2depth(double x, double y, double z) {
        final double kz = Math.max(0.d, Math.min(z, (double) nz - 1.00001f));
        final int i = (int) Math.floor(x);
        final int j = (int) Math.floor(y);
        final int k = (int) Math.floor(kz);
        double depth = 0.d;
        final double dx = x - (double) i;
        final double dy = y - (double) j;
        final double dz = kz - (double) k;
        double co = 0.d;
        double z_r;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    co = Math.abs((1.d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    if (isInWater(i + ii, j + jj)) {
                        // il faut réduire la taille du tableau (enlever les dimensions i et j)
                        float[] h_temp = new float[nz];
                        for (int ktemp = 0; ktemp < nz; ktemp++) {
                            h_temp[ktemp] = h_tp0[ktemp][j + jj][i + ii];
                        }
                        z_r = computeLocalDepth(i + ii, j + jj, h_temp, k + kk);
                        depth += co * z_r;
                    }
                }
            }
        }
        return depth;

    }

    @Override
    public double get_dUx(double[] pGrid, double time) {

        double du = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) Math.round(ix);
        int j = (n == 1) ? (int) Math.round(jy) : (int) jy;
        int k = (int) kz;
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double dz = kz - (double) k;
        double CO = 0.d;
        double co = 0.d;
        double x = 0.d;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < n; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    if(isInWater(i + ii, j + jj)){                       
                    co = Math.abs((.5d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    x = (1.d - x_euler) * u_tp0[k + kk][j + jj][i + ii - 1] + x_euler * u_tp1[k + kk][j + jj][i + ii - 1];
                    du += 2.d * x * co / (dxu[j + jj][i + ii - 1] + dxu[j + jj][i + ii]);
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
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (n == 1) ? (int) Math.round(ix) : (int) ix;
        int j = (int) Math.round(jy);
        int k = (int) kz;
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double dz = kz - (double) k;
        double CO = 0.d;
        double co = 0.d;
        double x = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < 2; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    if(isInWater(i + ii, j + jj)){
                    co = Math.abs((1.d - (double) ii - dx)
                            * (.5d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    x = (1.d - x_euler) * v_tp0[k + kk][j + jj - 1][i + ii] + x_euler * v_tp1[k + kk][j + jj - 1][i + ii];
                    dv += 2.d * x * co / (dyv[j + jj - 1][i + ii] + dyv[j + jj][i + ii]);
                }}
            }
        }
        if (CO != 0) {
            dv /= CO;
        }
        return dv;
    }

//_____________________________________________________________________________________________ MODIFIE PAR MORGANE -> meme structure que les autres mais appelle w_tp0 et dzw
    @Override
    public double get_dWz(double[] pGrid, double time) {

        double dw = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (n == 1) ? (int) Math.round(ix) : (int) ix;
        int j = (n == 1) ? (int) Math.round(jy) : (int) jy;
        int k = (int) Math.round(kz);
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double dz = kz - (double) k;
        double CO = 0.d;
        double co = 0.d;
        double x = 0.d;


        for (int ii = 0; ii < n; ii++) {
            for (int jj = 0; jj < n; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    if (isInWater(i + ii, j + jj)){
                    co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (.5d - (double) kk - dz));
                    CO += co;
                    x = (1.d - x_euler) * w_tp0[k + kk][j + jj][i + ii] + x_euler * w_tp1[k + kk][j + jj][i + ii];
                    dzw = (1.d - x_euler) * 0.5 * (h_tp0[Math.min(k + kk, nz - 1)][j + jj][i + ii] 
                            + h_tp0[Math.min(k + kk + 1, nz - 1)][j + jj][i + ii])
                            + x_euler * 0.5 * (h_tp1[Math.min(k + kk, nz - 1)][j + jj][i + ii] 
                            + h_tp1[Math.min(k + kk + 1, nz - 1)][j + jj][i + ii]);

                    dw += x * co / dzw;
                    }
                }
            }
        }
        if (CO != 0) {
            dw /= CO;
        }
        return dw;
    }

    /**
     * Compute the depth at the center of the vertical layer k, for the cell
     * (i,j)
     */
    double computeLocalDepth2(int i, int j, float[] h, int k) {
        // double cDepth = 0;
        double cDepth = elev_tp0[j][i]; // peut etre faire une interpolation temporelle ? mais il faudrait le faire aussi pour h

        for (int kk = nz - 1; kk > k; kk--) {
            cDepth -= h[kk];
        }
        cDepth -= h[k] / 2;   // on enleve la moitié de la hauteur de la derniere cellule (qui n'était pas ds la boucle) pour avoir la profondeur
        // au centre de la cellule et non pas à l'entre cellule

        return cDepth; 
    }
    
    double computeLocalDepth(int i, int j, float[] h, int k) {
        // double cDepth = 0;
        double cDepth = -1.0 * hRho[j][i]; 

        for (int kk = 0; kk < k; kk++) {
            cDepth += h[kk];
        }
        cDepth += h[k] / 2;   // on enleve la moitié de la hauteur de la derniere cellule (qui n'était pas ds la boucle) pour avoir la profondeur
        // au centre de la cellule et non pas à l'entre cellule

        return cDepth; 
    }

    @Override
    public boolean isInWater(double[] pGrid) {
        return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
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
     * @param pGrid a double[] the coordinates of the grid point -> HERE AS MARS
     * IN 2D... BUT IN 3D IN NEMO - OK faire comme mars car toutes les cellules
     * sont de l'eau
     * @return
     * <code>true</code> if the grid point is close to cost,
     * <code>false</code> otherwise.
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

    @Override
     public boolean isOnEdge(double[] pGrid) {
        return ((pGrid[0] > (nx - 2.0f))
                || (pGrid[0] < 1.0f)
                || (pGrid[1] > (ny - 2.0f))
                || (pGrid[1] < 1.0f));
    }
   /*
   public boolean isOnEdge(double[] pGrid) {    // version de NEMO
        return ((pGrid[0] > (nx - 3.0f))
                || (pGrid[0] < 2.0f)
                || (pGrid[1] > (ny - 3.0f))
                || (pGrid[1] < 2.0f));
    }
   */ 
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
    public int get_nz() {
        return nz;
    }

    @Override
    public double getdxi(int j, int i) {
        return dxu[j][i];
    }

    @Override
    public double getdeta(int j, int i) {
        return dyv[j][i];
    }

    /**
     * Initializes the {@code Dataset}. Opens the file holding the first time of
     * the simulation. Checks out the existence of the fields required by the
     * current simulation. Sets all fields at time for the first time step.
     *
     * @throws an IOException if a required field cannot be found in the NetCDF
     * dataset.
     */
    @Override
    public void init() throws Exception {
        time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());
        long t0 = getSimulationManager().getTimeManager().get_tO();
        open(getFile(t0));
        checkRequiredVariable(ncIn);
        setAllFieldsTp1AtTime(rank = findCurrentRank(t0));
        time_tp1 = t0;
    }

    private int findCurrentRank(long time) throws Exception {

        int lrank = 0;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());
        long time_rank;
        Array timeArr;
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
     * Reads time dependant variables in NetCDF dataset at specified rank.
     *
     * @param rank an int, the rank of the time dimension in the NetCDF dataset.
     * @throws an IOException if an error occurs while reading the variables.
     *
     * pverley pour chourdin: la aussi je fais du provisoire en attendant de
     * voir si on peut dégager une structure systématique des input.
     */
    void setAllFieldsTp1AtTime(int rank) throws Exception {

        int[] originW = new int[]{rank, 0, jpo, ipo};    //  ----------------------------------> modif Morgane : on ne veut pas la premiere couche verticale qui est remplie de NaN
        int[] origin = new int[]{rank, 1, jpo, ipo};
        double time_tp0 = time_tp1;

        try {
            u_tp1 = (float[][][]) ncIn.findVariable(strU).read(origin, new int[]{1, nz, ny, nx - 1}).
                    flip(1).reduce().copyToNDJavaArray();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading U velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            v_tp1 = (float[][][]) ncIn.findVariable(strV).read(origin, new int[]{1, nz, ny - 1, nx}).
                    flip(1).reduce().copyToNDJavaArray();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading V velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            Array xTimeTp1 = ncIn.findVariable(strTime).read();
            time_tp1 = xTimeTp1.getDouble(xTimeTp1.getIndex().set(rank));
            time_tp1 -= time_tp1 % 100;
            xTimeTp1 = null;
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
        for (RequiredVariable variable : requiredVariables.values()) {
            variable.nextStep(readVariable(ncIn, variable.getName(), rank), time_tp1, dt_HyMo);
        }


        try {
            w_tp1 = (float[][][]) ncIn.findVariable(strW).read(originW, new int[]{1, nz + 1, ny, nx}).
                    flip(1).reduce().copyToNDJavaArray();               // ---------------> different origin
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading W velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            h_tp1 = (float[][][]) ncIn.findVariable(strh).read(origin, new int[]{1, nz, ny, nx}).
                    flip(1).reduce().copyToNDJavaArray();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading h variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        /*
         * read zeta ocean free surface
         */
        int[] origin3d = new int[]{rank, jpo, ipo};

        try {
            elev_tp1 = (float[][]) ncIn.findVariable(strElev).read(origin3d, new int[]{1, ny, nx}).
                    flip(1).reduce().copyToNDJavaArray();
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading elev variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

    }

    @Override
    public double getLatMin() {
        return latMin;
    }

    @Override
    public double getLatMax() {
        return latMax;
    }

    @Override
    public double getLonMin() {
        return lonMin;
    }

    @Override
    public double getLonMax() {
        return lonMax;
    }

    @Override
    public double getLon(int igrid, int jgrid) {
        return lonRho[jgrid][igrid];
    }

    @Override
    public double getLat(int igrid, int jgrid) {
        return latRho[jgrid][igrid];
    }

    @Override
    public double getDepthMax() {
        return depthMax;
    }

    @Override
    public boolean is3D() {
        return true;
    }

    // PROVIENT DE MARS3D - Difference avec NEMO : pas de bouleen hasVerticalDim....
    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        Variable variable = nc.findVariable(name);
        int[] origin = null, shape = null;
        switch (variable.getShape().length) {
            case 4:
                //              origin = new int[]{rank, 0, jpo, ipo};        ---------------------------------------------> modif Morgane car on ne veut pas la premiere couche verticale qui est pleine de NaN
                origin = new int[]{rank, 1, jpo, ipo};
                shape = new int[]{1, nz, ny, nx};
                break;
            case 2:
                origin = new int[]{jpo, ipo};
                shape = new int[]{ny, nx};
                break;
            case 3:
                if (!variable.isUnlimited()) {
                    //                origin = new int[]{0, jpo, ipo};              ---------------------------------------------> modif Morgane car on ne veut pas la premiere couche verticale qui est pleine de NaN
                    origin = new int[]{1, jpo, ipo};
                    shape = new int[]{nz, ny, nx};
                } else {
                    origin = new int[]{rank, jpo, ipo};
                    shape = new int[]{1, ny, nx};
                }
                break;
        }

        return variable.read(origin, shape).reduce();
    }

    
    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {
        long time = e.getSource().getTime();

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        h_tp0 = h_tp1;                  // Ajout Morgane : hauteur d'eau de chaque cellule
        elev_tp0 = elev_tp1;

        rank += time_arrow;

        if (rank > (nbTimeRecords - 1) || rank < 0) {
            open(getNextFile(time_arrow));
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }

        setAllFieldsTp1AtTime(rank);
    }
}
