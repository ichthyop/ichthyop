/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import org.previmer.ichthyop.util.MetaFilenameFilter;
import org.previmer.ichthyop.util.NCComparator;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.util.Constant;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;
import ucar.nc2.Attribute;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public abstract class Roms3dDataset extends AbstractDataset {

    /**
     * Grid dimension
     */
    static int nx, ny, nz;
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
     * Ocean free surface elevetation at current time
     */
    static float[][] zeta_tp0;
    /**
     * /**
     * Ocean free surface elevetation at time t + dt
     */
    static float[][] zeta_tp1;
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
     *  Meridional component of the velocity field at time t + dt
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
     * Water salinity at time t + dt
     */
    private static float[][][] salt_tp1;
    /**
     * Water salinity at current time
     */
    private static float[][][] salt_tp0;
    /**
     * Water temperature at current time
     */
    private static float[][][] temp_tp0;
    /**
     * Water temperature at time t + dt
     */
    private static float[][][] temp_tp1;
    /**
     *
     */
    private double[][] pm, pn;
    /**
     * Vertical diffusion coefficient at time t + dt
     */
    private float[][][] kv_tp1;
    /**
     * Vertical diffusion coefficient at current time
     */
    private float[][][] kv_tp0;
    /**
     * Depth at rho point
     */
    static double[][][] z_rho_cst;
    /**
     * Depth at w point at current time.
     * Takes account of free surface elevation.
     */
    static double[][][] z_w_tp0;
    /**
     * Depth at w point at time t + dt
     * Takes account of free surface elevation.
     */
    static double[][][] z_w_tp1;
    /**
     * Depth at w point. The free surface elevation is disregarded.
     */
    static double[][][] z_w_cst;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    static double dt_HyMo;
    /**
     * List on NetCDF input files in which dataset is read.
     */
    private ArrayList<String> listInputFiles;
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
    static NetcdfFile ncIn;
    /**
     * Name of the Dimension in NetCDF file
     */
    static String strXiDim, strEtaDim, strZDim, strTimeDim;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strU, strV, strTp, strSal, strTime, strZeta;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strLon, strLat, strMask, strBathy;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strKv;
    String strCs_r, strCs_w, strSc_r, strSc_w, strPn, strPm;
    /**
     * Determines whether or not the temperature field should be read in the
     * NetCDF file, function of the user's options.
     */
    private static boolean FLAG_TP;
    /**
     * Determines whether or not the salinity field should be read in the
     * NetCDF file, function of the user's options.
     */
    private static boolean FLAG_SAL;
    /**
     * Determines whether or not the turbulent diffusivity should be read in the
     * NetCDF file, function of the user's options.
     */
    private static boolean FLAG_VDISP;
    private String gridFile;
    /**
     * Geographical boundary of the domain
     */
    private double latMin, lonMin, latMax, lonMax, depthMax;

    abstract float getHc();

    void loadParameters() {

        strXiDim = getParameter("Dimension in the XI-direction");
        strEtaDim = getParameter("Dimension in the ETA-direction");
        strZDim = getParameter("Dimension in the Z-direction");
        strTimeDim = getParameter("Dimension in time");
        strLon = getParameter("Longitude of RHO-points");
        strLat = getParameter("Latitude of RHO-points");
        strBathy = getParameter("Bathymetry at RHO-points");
        strMask = getParameter("Mask on RHO-points");
        strU = getParameter("U-momentum component");
        strV = getParameter("V-momentum component");
        strZeta = getParameter("Free-surface elevation");
        strTp = getParameter("Averaged potential temperature");
        strSal = getParameter("Averaged salinity");
        strTime = getParameter("Averaged time since initialization");
        strKv = getParameter("Vertical turbulent diffusion");
        strPn = getParameter("Curvilinear coordinate metric in ETA");
        strPm = getParameter("Curvilinear coordinate metric in XI");
        strCs_r = getParameter("S-coordinate stretching curves at RHO-points");
        strCs_w = getParameter("S-coordinate stretching curves at W-points");
        strSc_r = getParameter("S-coordinate at RHO-points");
        strSc_w = getParameter("S-coordinate at W-points");
    }

    private void openLocation(String rawPath) throws IOException {

        URI uriCurrent = new File("").toURI();
        String path = uriCurrent.resolve(URI.create(rawPath)).getPath();

        if (isDirectory(path)) {
            listInputFiles = getInputList(path);
            if (!getParameter("Grid file").isEmpty()) {
                gridFile = getGridFile(getParameter("Grid file"));
            } else {
                gridFile = listInputFiles.get(0);
            }
        }
        open(listInputFiles.get(0));
    }

    private String getGridFile(String rawFile) throws IOException {

        File filename = new File(rawFile);
        if (!filename.exists()) {
            throw new IOException("Grid file " + rawFile + " does not exist");
        }

        return filename.toString();
    }

    private ArrayList<String> getInputList(String path) throws IOException {

        ArrayList<String> list = null;

        File inputPath = new File(path);
        String fileMask = getParameter("File filter");
        File[] listFile = inputPath.listFiles(new MetaFilenameFilter(fileMask));
        if (listFile.length == 0) {
            throw new IOException(path + " contains no file matching mask " + fileMask);
        }
        list = new ArrayList<String>(listFile.length);
        for (File file : listFile) {
            list.add(file.toString());
        }
        if (list.size() > 1) {
            Collections.sort(list, new NCComparator(strTime));
        }
        return list;
    }

    private boolean isDirectory(String location) throws IOException {

        File f = new File(location);
        if (!f.isDirectory()) {
            throw new IOException(location + " is not a valid directory.");
        }
        return f.isDirectory();
    }

    public void setUp() {

        loadParameters();

        try {
            openLocation(getParameter("Input path"));
            getDimNC();
            if (Boolean.valueOf(getParameter("Is ranged"))) {
                float[] p1 = new float[]{Float.valueOf(getParameter("range_P1_lon")), Float.valueOf(getParameter("range_P1_lat"))};
                float[] p2 = new float[]{Float.valueOf(getParameter("range_P2_lon")), Float.valueOf(getParameter("range_P2_lat"))};
                range(p1, p2);
            }
            readConstantField(gridFile);
            getDimGeogArea();
            getCstSigLevels();
            z_w_tp0 = getSigLevels();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public void init() {
        try {
            long t0 = getSimulationManager().getTimeManager().get_tO();
            open(getFile(t0));
            FLAG_TP = FLAG_SAL = FLAG_VDISP = true;
            setAllFieldsTp1AtTime(rank = findCurrentRank(t0));
            time_tp1 = t0;
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private int findCurrentRank(long time) throws IOException {

        int lrank = 0;
        int time_arrow = (int) Math.signum(getSimulationManager().getTimeManager().get_dt());
        long time_rank;
        Array timeArr;
        try {
            timeArr = ncIn.findVariable(strTime).read();
            time_rank = skipSeconds(
                    timeArr.getLong(timeArr.getIndex().set(lrank)));
            while (time >= time_rank) {
                if (time_arrow < 0 && time == time_rank) {
                    break;
                }
                lrank++;
                time_rank = skipSeconds(
                        timeArr.getLong(timeArr.getIndex().set(lrank)));
            }
        } catch (IOException e) {
            throw new IOException("Problem reading file " + ncIn.getLocation().toString() + " : " +
                    e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("Unable to read " + strTime +
                    " variable in file " + ncIn.getLocation().toString() + " : " +
                    e.getCause());
        } catch (ArrayIndexOutOfBoundsException e) {
            lrank = nbTimeRecords;
        }
        lrank = lrank - (time_arrow + 1) / 2;

        return lrank;
    }

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

        throw new IOException("Time value " + (long) time + " not contained among NetCDF files " + getParameter("File filter") + " of folder " + getParameter("Input path"));
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
            time_r0 = skipSeconds(timeArr.getLong(timeArr.getIndex().set(0)));
            time_rf = skipSeconds(timeArr.getLong(timeArr.getIndex().set(
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
            throw new IOException("Problem reading file " + filename + " : " + e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("Unable to read " + strTime +
                    " variable in file " + filename + " : " + e.getCause());
        }
        //return false;

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
                time_nc[i] = skipSeconds(
                        timeArr.getLong(timeArr.getIndex().set(0)));
                nc.close();
            }
            if (time >= time_nc[0] && time < time_nc[1]) {
                return true;
            }
        } catch (IOException e) {
            throw new IOException("Problem reading file " + filename + " : " + e.getCause());
        } catch (NullPointerException e) {
            throw new IOException("Unable to read " + strTime +
                    " variable in file " + filename + " : " + e.getCause());
        }
        return false;
    }

    /**
     * Computes the depth at sigma levels disregarding the free
     * surface elevation.
     */
    void getCstSigLevels() throws IOException {

        double hc;
        double sc_r;
        double[] Cs_r = new double[nz];
        double[] cff_r = new double[nz];
        double sc_w;
        double[] Cs_w = new double[nz + 1];
        double[] cff_w = new double[nz + 1];

        //-----------------------------------------------------------
        // Read the Param in ncIn
        hc = getHc();
        Attribute attrib_sc_w = ncIn.findGlobalAttribute(strSc_w);
        Attribute attrib_sc_r = ncIn.findGlobalAttribute(strSc_r);
        Attribute attrib_cs_w = ncIn.findGlobalAttribute(strCs_w);
        Attribute attrib_cs_r = ncIn.findGlobalAttribute(strCs_r);


        //-----------------------------------------------------------
        // Calculation of the Coeff;
        for (int k = nz; k-- > 0;) {
            if (null != attrib_sc_r) {
                sc_r = attrib_sc_r.getNumericValue(k).floatValue();
            } else {
                sc_r = ((double) (k - nz) + .5d) / (double) nz;
            }
            Cs_r[k] = attrib_cs_r.getNumericValue(k).floatValue();
            cff_r[k] = hc * (sc_r - Cs_r[k]);
        }

        for (int k = nz + 1; k-- > 0;) {
            if (null != attrib_sc_w) {
                sc_w = attrib_sc_w.getNumericValue(k).floatValue();
            } else {
                sc_w = (double) (k - nz) / (double) nz;
            }
            Cs_w[k] = attrib_cs_w.getNumericValue(k).floatValue();
            cff_w[k] = hc * (sc_w - Cs_w[k]);
        }
        Cs_w[0] = -1.d;

        //------------------------------------------------------------
        // Calculation of z_w , z_r
        double[][][] z_r_tmp = new double[nz][ny][nx];
        double[][][] z_w_tmp = new double[nz + 1][ny][nx];

        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                z_w_tmp[0][j][i] = -hRho[j][i];
                for (int k = nz; k-- > 0;) {
                    z_r_tmp[k][j][i] = cff_r[k] + Cs_r[k] * hRho[j][i];
                    z_w_tmp[k +
                            1][j][i] = cff_w[k + 1] + Cs_w[k + 1] * hRho[j][i];

                }
                z_w_tmp[nz][j][i] = 0.d;
            }
        }
        //z_rho_cst = new double[nz][ny][nx];
        //z_w_cst = new double[nz + 1][ny][nx];

        z_rho_cst = z_r_tmp;
        z_w_cst = z_w_tmp;

        z_w_tp0 = new double[nz + 1][ny][nx];
        z_w_tp1 = new double[nz + 1][ny][nx];

        //System.out.println("cst sig ok");

    }

    void readConstantField(String gridFile) throws IOException {

        int[] origin = new int[]{jpo, ipo};
        int[] size = new int[]{ny, nx};
        Array arrLon, arrLat, arrMask, arrH, arrZeta, arrPm, arrPn;
        Index index;
        StringBuffer list = new StringBuffer(strLon);
        list.append(", ");
        list.append(strLat);
        list.append(", ");
        list.append(strMask);
        list.append(", ");
        list.append(strBathy);
        list.append(", ");
        list.append(strZeta);
        list.append(", ");
        list.append(strPm);
        list.append(", ");
        list.append(strPn);
        try {

            NetcdfFile ncGrid = NetcdfDataset.openFile(gridFile, null);
            arrLon = ncGrid.findVariable(strLon).read(origin, size);
            arrLat = ncGrid.findVariable(strLat).read(origin, size);
            arrMask = ncGrid.findVariable(strMask).read(origin, size);
            arrH = ncGrid.findVariable(strBathy).read(origin, size);
            arrPm = ncGrid.findVariable(strPm).read(origin, size);
            arrPn = ncGrid.findVariable(strPn).read(origin, size);
            ncGrid.close();

            arrZeta = ncIn.findVariable(strZeta).read(new int[]{0, jpo, ipo}, new int[]{1, ny, nx}).reduce();

            if (arrLon.getElementType() == double.class) {
                lonRho = (double[][]) arrLon.copyToNDJavaArray();
                latRho = (double[][]) arrLat.copyToNDJavaArray();
            } else {
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
            }

            if (arrMask.getElementType() != byte.class) {
                maskRho = new byte[ny][nx];
                index = arrMask.getIndex();
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        maskRho[j][i] = arrMask.getByte(index.set(j, i));
                    }
                }
            } else {
                maskRho = (byte[][]) arrMask.copyToNDJavaArray();
            }

            if (arrPm.getElementType() == double.class) {
                pm = (double[][]) arrPm.copyToNDJavaArray();
                pn = (double[][]) arrPn.copyToNDJavaArray();
            } else {
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
            }

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

            if (arrZeta.getElementType() == float.class) {
                zeta_tp0 = (float[][]) arrZeta.copyToNDJavaArray();
            } else {
                zeta_tp0 = new float[ny][nx];
                index = arrZeta.getIndex();
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        zeta_tp0[j][i] = arrZeta.getFloat(index.set(j, i));
                    }
                }
            }
            zeta_tp1 = zeta_tp0;
        } catch (IOException e) {
            throw new IOException("Problem reading one of the fields " + list.toString() + " at location: " + e.getMessage());
        } catch (InvalidRangeException e) {
            throw new IOException("Problem reading one of the fields " + list.toString() + " at location: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem reading one of the fields " + list.toString() + " at location: " + e.getMessage());
        }
    }

    /**
     * Reads the dimensions of the NetCDF dataset
     * @throws an IOException if an error occurs while reading the dimensions.
     */
    private void getDimNC() throws IOException {

        try {
            nx = ncIn.findDimension(strXiDim).getLength();
            ny = ncIn.findDimension(strEtaDim).getLength();
            nz = ncIn.findDimension(strZDim).getLength();
        } catch (NullPointerException e) {
            e.printStackTrace();
            //throw new IOException("Problem reading dimensions from dataset " + ncIn.getLocation() + " : " + e.getMessage());
        }
        //Logger.getLogger(getClass().getName()).info("nx " + nx + " - ny " + ny + " - nz " + nz);
        ipo = jpo = 0;
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

    public double[] xy2lonlat(double xRho, double yRho) {

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
                    (double) lk +
                    (depth - pr) / (getDepth(x, y, lk + 1) - pr));
        }
        return (z);
    }

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
                    co = Math.abs((1.d - (double) ii - dx) *
                            (1.d - (double) jj - dy) *
                            (1.d - (double) kk - dz));
                    if (isInWater(i + ii, j + jj)) {
                        z_r = z_rho_cst[k + kk][j + jj][i + ii] + (double) zeta_tp0[j + jj][i + ii] *
                                (1.d + z_rho_cst[k + kk][j + jj][i + ii] / hRho[j +
                                jj][i + ii]);
                        depth += co * z_r;
                    }
                }
            }
        }
        return depth;
    }

    public double get_dWz(double[] pGrid, double time) {

        double dw = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) ix;
        int j = (int) jy;
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
                    co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy) * (.5d - (double) kk - dz));
                    CO += co;
                    x = (1.d - x_euler) * w_tp0[k + kk][j + jj][i + ii] + x_euler * w_tp1[k + kk][j + jj][i + ii];
                    dw += 2.d * x * co / (z_w_tp0[Math.min(k + kk + 1, nz)][j + jj][i + ii] - z_w_tp0[Math.max(k + kk - 1, 0)][j + jj][i + ii]);
                }
            }
        }
        if (CO != 0) {
            dw /= CO;
        }
        return dw;
    }

    public double get_dVy(double[] pGrid, double time) {
        double dv = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) ix;
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
                    co = Math.abs((1.d - (double) ii - dx) *
                            (.5d - (double) jj - dy) *
                            (1.d - (double) kk - dz));
                    CO += co;
                    x = (1.d - x_euler) * v_tp0[k + kk][j + jj - 1][i + ii] + x_euler * v_tp1[k + kk][j + jj - 1][i + ii];
                    dv += .5d * x * co * (pn[Math.max(j + jj - 1, 0)][i + ii] + pn[j + jj][i + ii]);
                }
            }
        }
        if (CO != 0) {
            dv /= CO;
        }
        return dv;
    }

    public double get_dUx(double[] pGrid, double time) {

        double du = 0.d;
        double ix, jy, kz;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) Math.round(ix);
        int j = (int) jy;
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
                    co = Math.abs((.5d - (double) ii - dx) *
                            (1.d - (double) jj - dy) *
                            (1.d - (double) kk - dz));
                    CO += co;
                    x = (1.d - x_euler) * u_tp0[k + kk][j + jj][i + ii - 1] + x_euler * u_tp1[k + kk][j + jj][i + ii - 1];
                    du += .5d * x * co * (pm[j + jj][Math.max(i + ii - 1, 0)] + pm[j + jj][i + ii]);
                }
            }
        }
        if (CO != 0) {
            du /= CO;
        }
        return du;
    }

    public double adimensionalize(double number, double xRho, double yRho) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isInWater(double[] pGrid) {
        try {
            return (maskRho[(int) Math.round(pGrid[1])][(int) Math.round(pGrid[0])] > 0);
        } catch (ArrayIndexOutOfBoundsException e) {
            return false;
        }
    }

    public boolean isOnEdge(double[] pGrid) {
        return ((pGrid[0] > (nx - 2.0f)) ||
                (pGrid[0] < 1.0f) ||
                (pGrid[1] > (ny - 2.0f)) ||
                (pGrid[1] < 1.0f));
    }

    public double getBathy(int i, int j) {
        if (isInWater(i, j)) {
            return hRho[j][i];
        }
        return Double.NaN;
    }

    private double getDepth(double xRho, double yRho, int k) {

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
                    double z_r = 0.d;
                    z_r = z_rho_cst[k][j + jj][i + ii] + (double) zeta_tp0[j +
                            jj][i + ii] *
                            (1.d + z_rho_cst[k][j + jj][i + ii] / hRho[j + jj][i +
                            ii]);
                    hh += co * z_r;
                }
            }
        }
        return (hh);
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
        double kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
        int k = (int) kz;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double dz = kz - (double) k;
        tp = 0.d;
        CO = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < n; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    {
                        co = Math.abs((1.d - (double) ii - dx) *
                                (1.d - (double) jj - dy) *
                                (1.d - (double) kk - dz));
                        CO += co;
                        x = 0.d;
                        try {
                            x = (1.d - frac) * temp_tp0[k + kk][j + jj][i + ii] +
                                    frac * temp_tp1[k + kk][j + jj][i + ii];
                            tp += x * co;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            throw new ArrayIndexOutOfBoundsException(
                                    "Problem interpolating temperature field : " +
                                    e.getMessage());
                        }
                    }
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
        double kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
        int k = (int) kz;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double dz = kz - (double) k;
        sal = 0.d;
        CO = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < n; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    {
                        co = Math.abs((1.d - (double) ii - dx) *
                                (1.d - (double) jj - dy) *
                                (1.d - (double) kk - dz));
                        CO += co;
                        x = 0.d;
                        try {
                            x = (1.d - frac) * salt_tp0[k + kk][j + jj][i + ii] +
                                    frac * salt_tp1[k + kk][j + jj][i + ii];
                            sal += x * co;
                        } catch (ArrayIndexOutOfBoundsException e) {
                            throw new ArrayIndexOutOfBoundsException(
                                    "Problem interpolating salinity field : " +
                                    e.getMessage());
                        }
                    }
                }
            }
        }
        if (CO != 0) {
            sal /= CO;
        }

        return sal;
    }

    public int get_nx() {
        return nx;
    }

    public int get_ny() {
        return ny;
    }

    public int get_nz() {
        return nz;
    }

    public double getdxi(int j, int i) {
        return (pm[j][i] != 0) ? (1 / pm[j][i]) : 0.d;
    }

    public double getdeta(int j, int i) {
        return (pn[j][i] != 0) ? (1 / pn[j][i]) : 0.d;
    }

    public double[] getKv(double[] pGrid, double time, double dt) {

        double co, CO = 0.d, Kv = 0.d, diffKv = 0.d, Hz = 0.d;
        double x, y, z, dx, dy;
        int i, j, k;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        double[] kvSpline;
        double depth;

        x = pGrid[0];
        y = pGrid[1];
        z = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));
        depth = z2depth(x, y, z);

        i = (int) x;
        j = (int) y;
        k = (int) Math.round(z);
        dx = x - Math.floor(x);
        dy = y - Math.floor(y);

        for (int ii = 0; ii < n; ii++) {
            for (int jj = 0; jj < n; jj++) {
                co = Math.abs((1.d - (double) ii - dx) * (1.d - (double) jj - dy));
                CO += co;
                kvSpline = getKv(i + ii, j + jj, depth, time, dt);
                diffKv += kvSpline[0] * co;
                Kv += kvSpline[1] * co;
                Hz += co * (z_w_tp0[k + 1][j + jj][i + ii] - z_w_tp0[Math.max(k - 1, 0)][j + jj][i + ii]);
            }
        }
        if (CO != 0) {
            diffKv /= CO;
            Kv /= CO;
            Hz /= CO;
        }

        return new double[]{diffKv, Kv, Hz};
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
                } else if (((dx1 == 0.) & (lat >= yb[k])) |
                        ((dx2 == 0.) & (lat >= yb[k + 1]))) {
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
        return (maskRho[j][i] > 0);
    }

    private boolean isCloseToCost(double[] pGrid) {

        int i, j, ii, jj;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
        jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
        return !(isInWater(i + ii, j) && isInWater(i + ii, j + jj) && isInWater(i, j + jj));
    }

    private double[] getKv(int i, int j, double depth, double time, double dt) {

        double diffzKv, Kvzz, ddepth, dz, zz;
        double[] Kv = new double[nz];
        double a, b, c, d;
        double xTime;
        int k;
        double z;
        xTime = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        for (k = nz; k-- > 0;) {
            Kv[k] = (1.d - xTime) * kv_tp0[k][j][i] + xTime * kv_tp1[k][j][i];
        }

        z = Math.min(depth2z(i, j, depth), nz - 1.00001f);
        k = (int) z;
        //dz = z - Math.floor(z);
        ddepth = depth - z_rho_cst[k][j][i];
        /** Compute the polynomial coefficients of the piecewise of the spline
         * contained between [k; k + 1]. Let's take M = Kv''
         * a = (M(k + 1) - M(k)) / 6
         * b = M(k) / 2
         * c = Kv(k + 1) - Kv(k) - (M(k + 1) - M(k)) / 6
         * d = Kv(k);
         */
        a = (diff2(Kv, k + 1) - diff2(Kv, k)) / 6.d;
        b = diff2(Kv, k) / 2.d;
        c = (Kv[k + 1] - Kv[k]) - (diff2(Kv, k + 1) + 2.d * diff2(Kv, k)) / 6.d;
        d = Kv[k];

        /** Compute Kv'(z)
         * Kv'(z) = 3.d * a * dz2 + 2.d * b * dz + c; */
        diffzKv = c + ddepth * (2.d * b + 3.d * a * ddepth);

        zz = Math.min(depth2z(i, j, depth + 0.5d * diffzKv * dt), nz - 1.00001f);
        dz = zz - Math.floor(z);
        if (dz >= 1.f || dz < 0) {
            k = (int) zz;
            a = (diff2(Kv, k + 1) - diff2(Kv, k)) / 6.d;
            b = diff2(Kv, k) / 2.d;
            c = (Kv[k + 1] - Kv[k]) -
                    (diff2(Kv, k + 1) + 2.d * diff2(Kv, k)) / 6.d;
            d = Kv[k];
        }
        ddepth = depth + 0.5d * diffzKv * dt - z_rho_cst[k][j][i];
        /** Compute Kv(z)
         * Kv(z) = a * dz3 + b * dz2 + c * dz + d;*/
        Kvzz = d + ddepth * (c + ddepth * (b + ddepth * a));
        Kvzz = Math.max(0.d, Kvzz);

        return new double[]{diffzKv, Kvzz};
    }

    private double diff2(double[] X, int k) {

        int length = X.length;
        /** Returns NaN if size <= 2 */
        if (length < 3) {
            return Double.NaN;
        }

        /** This return statement traduces the natural spline hypothesis
         * M(0) = M(nz - 1) = 0 */
        if ((k <= 0) || (k >= (length - 1))) {
            return 0.d;
        }

        return (X[k + 1] - 2.d * X[k] + X[k - 1]);
    }

    public void nextStepTriggered(NextStepEvent e) {

        long time = e.getSource().getTime();
        //Logger.getAnonymousLogger().info("set fields at time " + time);
        int time_arrow = (int) Math.signum(e.getSource().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        zeta_tp0 = zeta_tp1;
        temp_tp0 = temp_tp1;
        salt_tp0 = salt_tp1;
        kv_tp0 = kv_tp1;
        if (z_w_tp1 != null) {
            z_w_tp0 = z_w_tp1;
        }
        rank += time_arrow;
        try {
            if (rank > (nbTimeRecords - 1) || rank < 0) {
                open(getNextFile(time_arrow));
                rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
            }
            setAllFieldsTp1AtTime(rank);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private String getNextFile(int time_arrow) throws IOException {

        int index = indexFile - (1 - time_arrow) / 2;
        boolean noNext = (listInputFiles.size() == 1) || (index < 0) || (index >= listInputFiles.size() - 1);
        if (noNext) {
            throw new IOException("Unable to find any file following " + listInputFiles.get(indexFile));
        }
        indexFile += time_arrow;
        return listInputFiles.get(indexFile);
    }

    private void open(String filename) throws IOException {
        try {
            if (ncIn == null || (new File(ncIn.getLocation()).compareTo(new File(filename)) != 0)) {
                ncIn = NetcdfDataset.openFile(filename, null);
                nbTimeRecords = ncIn.findDimension(strTimeDim).getLength();
            }
            getLogger().info("Open dataset " + filename);
        } catch (IOException e) {
            throw new IOException("Problem opening dataset " + filename + " - " + e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem reading " + strTimeDim + " dimension at location " + filename +
                    " : " + e.getMessage());
        }
    }

    void setAllFieldsTp1AtTime(int rank) throws IOException {

        int[] origin = new int[]{rank, 0, jpo, ipo};
        double time_tp0 = time_tp1;

        try {
            u_tp1 = (float[][][]) ncIn.findVariable(strU).read(origin, new int[]{1, nz, ny, (nx - 1)}).reduce().copyToNDJavaArray();

            v_tp1 = (float[][][]) ncIn.findVariable(strV).read(origin,
                    new int[]{1, nz, (ny - 1), nx}).reduce().copyToNDJavaArray();

            Array xTimeTp1 = ncIn.findVariable(strTime).read();
            time_tp1 = xTimeTp1.getFloat(xTimeTp1.getIndex().set(rank));
            time_tp1 -= time_tp1 % 60;
            xTimeTp1 = null;

            zeta_tp1 = (float[][]) ncIn.findVariable(strZeta).read(
                    new int[]{rank, 0, 0},
                    new int[]{1, ny, nx}).reduce().copyToNDJavaArray();

            if (FLAG_TP) {
                temp_tp1 = (float[][][]) ncIn.findVariable(strTp).read(origin,
                        new int[]{1, nz, ny, nx}).reduce().copyToNDJavaArray();
            }

            if (FLAG_SAL) {
                salt_tp1 = (float[][][]) ncIn.findVariable(strSal).read(origin,
                        new int[]{1, nz, ny, nx}).reduce().copyToNDJavaArray();
            }

            if (FLAG_VDISP) {
                kv_tp1 = (float[][][]) ncIn.findVariable(strKv).read(origin,
                        new int[]{1, nz, ny, nx}).reduce().copyToNDJavaArray();
            }


        } catch (IOException e) {
            throw new IOException("Problem extracting fields at location " + ncIn.getLocation().toString() + " : " +
                    e.getMessage());
        } catch (InvalidRangeException e) {
            throw new IOException("Problem extracting fields at location " + ncIn.getLocation().toString() + " : " +
                    e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem extracting fields at location " + ncIn.getLocation().toString() + " : " +
                    e.getMessage());
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
        z_w_tp1 = getSigLevels();
        w_tp1 = computeW();
    }

    float[][][] computeW() {

        //System.out.println("Compute vertical velocity");
        double[][][] Huon = new double[nz][ny][nx];
        double[][][] Hvom = new double[nz][ny][nx];
        double[][][] z_w_tmp = z_w_tp1;

        //---------------------------------------------------
        // Calculation Coeff Huon & Hvom
        for (int k = nz; k-- > 0;) {
            for (int i = 0; i++ < nx - 1;) {
                for (int j = ny; j-- > 0;) {
                    Huon[k][j][i] = (((z_w_tmp[k + 1][j][i] -
                            z_w_tmp[k][j][i]) +
                            (z_w_tmp[k + 1][j][i - 1] -
                            z_w_tmp[k][j][i - 1])) /
                            (pn[j][i] + pn[j][i - 1])) *
                            u_tp1[k][j][i - 1];
                }
            }
            for (int i = nx; i-- > 0;) {
                for (int j = 0; j++ < ny - 1;) {
                    Hvom[k][j][i] = (((z_w_tmp[k + 1][j][i] -
                            z_w_tmp[k][j][i]) +
                            (z_w_tmp[k + 1][j - 1][i] -
                            z_w_tmp[k][j - 1][i])) /
                            (pm[j][i] + pm[j - 1][i])) *
                            v_tp1[k][j - 1][i];
                }
            }
        }

        //---------------------------------------------------
        // Calcultaion of w(i, j, k)
        double[] wrk = new double[nx];
        double[][][] w_double = new double[nz + 1][ny][nx];

        for (int j = ny - 1; j-- > 0;) {
            for (int i = nx; i-- > 0;) {
                w_double[0][j][i] = 0.f;
            }
            for (int k = 0; k++ < nz;) {
                for (int i = nx - 1; i-- > 0;) {
                    w_double[k][j][i] = w_double[k - 1][j][i] +
                            (float) (Huon[k - 1][j][i] - Huon[k - 1][j][i + 1] +
                            Hvom[k - 1][j][i] - Hvom[k -
                            1][j + 1][i]);
                }
            }
            for (int i = nx; i-- > 0;) {
                wrk[i] = w_double[nz][j][i] /
                        (z_w_tmp[nz][j][i] - z_w_tmp[0][j][i]);
            }
            for (int k = nz; k-- >= 2;) {
                for (int i = nx; i-- > 0;) {
                    w_double[k][j][i] += -wrk[i] *
                            (z_w_tmp[k][j][i] - z_w_tmp[0][j][i]);
                }
            }
            for (int i = nx; i-- > 0;) {
                w_double[nz][j][i] = 0.f;
            }
        }

        //---------------------------------------------------
        // Boundary Conditions
        for (int k = nz + 1; k-- > 0;) {
            for (int j = ny; j-- > 0;) {
                w_double[k][j][0] = w_double[k][j][1];
                w_double[k][j][nx - 1] = w_double[k][j][nx - 2];
            }
        }
        for (int k = nz + 1; k-- > 0;) {
            for (int i = nx; i-- > 0;) {
                w_double[k][0][i] = w_double[k][1][i];
                w_double[k][ny - 1][i] = w_double[k][ny - 2][i];
            }
        }

        //---------------------------------------------------
        // w * pm * pn
        float[][][] w = new float[nz + 1][ny][nx];
        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                for (int k = nz + 1; k-- > 0;) {
                    w[k][j][i] = (float) (w_double[k][j][i] * pm[j][i] *
                            pn[j][i]);
                }
            }
        }
        //---------------------------------------------------
        // Return w
        return w;

    }

    static double[][][] getSigLevels() {

        //-----------------------------------------------------
        // Daily recalculation of z_w and z_r with zeta

        double[][][] z_w_tmp = new double[nz + 1][ny][nx];
        double[][][] z_w_cst_tmp = z_w_cst;

        //System.out.print("Calculation of the s-levels\n");

        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                if (zeta_tp1[j][i] == 999.f) {
                    zeta_tp1[j][i] = 0.f;
                }
                for (int k = 0; k < nz + 1; k++) {
                    z_w_tmp[k][j][i] = z_w_cst_tmp[k][j][i] + zeta_tp1[j][i] *
                            (1.f + z_w_cst_tmp[k][j][i] / hRho[j][i]);
                }
            }
        }
        z_w_cst_tmp = null;
        return z_w_tmp;
    }

    /**
     * Reads longitude and latitude fields in NetCDF dataset
     */
    void readLonLat(String gridFile) throws IOException {
        Array arrLon, arrLat;
        try {

            NetcdfFile ncGrid = NetcdfDataset.openFile(gridFile, null);
            arrLon = ncIn.findVariable(strLon).read();
            arrLat = ncIn.findVariable(strLat).read();
            ncGrid.close();

            if (arrLon.getElementType() == double.class) {
                lonRho = (double[][]) arrLon.copyToNDJavaArray();
                latRho = (double[][]) arrLat.copyToNDJavaArray();
            } else {
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
            arrLon = null;
            arrLat = null;
        } catch (IOException e) {
            throw new IOException("Problem reading lon/lat fields at location: " + e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem reading lon/lat at location: " + e.getMessage());
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
    private void range(float[] pGeog1, float[] pGeog2) throws IOException {

        double[] pGrid1, pGrid2;
        int ipn, jpn;

        readLonLat(gridFile);

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
     * Computes the Hyperbolic Sinus of x
     */
    private static double sinh(double x) {
        return ((Math.exp(x) - Math.exp(-x)) / 2.d);
    }

    /**
     * Computes the Hyperbolic Cosinus of x
     */
    private static double cosh(double x) {
        return ((Math.exp(x) + Math.exp(-x)) / 2.d);
    }

    /**
     * Computes the Hyperbolic Tangent of x
     */
    private static double tanh(double x) {
        return (sinh(x) / cosh(x));
    }

    private long skipSeconds(long time) {
        return time - time % 60L;
    }

    public Number get(String variableName, double[] pGrid, double time) {
        Variable variable = ncIn.findVariable(variableName);
        if (!variable.getDataType().isNumeric()) {
            throw new NumberFormatException(variableName + " is not a numeric variable");
        }
        int[] origin = new int[variable.getShape().length];
        int[] shape = variable.getShape();
        int i = (int) pGrid[0];
        int j = (int) pGrid[1];
        int n = isCloseToCost(pGrid) ? 1 : 2;
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double kz, dz, value_t0, value_t1;
        int k;
        Array array;
        try {
            switch (variable.getShape().length) {
                case 4:
                    kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
                    k = (int) kz;
                    dz = kz - (double) k;
                    shape = new int[]{2, 2, 2, 2};
                    origin = new int[]{rank - 1, k, j, i};
                    array = variable.read(origin, shape);
                    value_t0 = interp3D((ArrayFloat.D3) array.section(new int[]{0, 0, 0, 0}, new int[]{1, 2, 2, 2}).reduce(), dx, dy, dz, n);
                    value_t1 = interp3D((ArrayFloat.D3) array.section(new int[]{1, 0, 0, 0}, new int[]{1, 2, 2, 2}).reduce(), dx, dy, dz, n);
                    return interpTime(value_t0, value_t1, time);
                case 3:
                    if (variable.isUnlimited()) {
                        shape = new int[]{2, 2, 2};
                        origin = new int[]{rank - 1, j, i};
                        array = variable.read(origin, shape);
                        value_t0 = interp2D((ArrayFloat.D2) array.section(new int[]{0, 0, 0}, new int[]{1, 2, 2}).reduce(), dx, dy, n);
                        value_t1 = interp2D((ArrayFloat.D2) array.section(new int[]{1, 0, 0}, new int[]{1, 2, 2}).reduce(), dx, dy, n);
                        return interpTime(value_t0, value_t1, time);
                    } else {
                        kz = Math.max(0.d, Math.min(pGrid[2], (double) nz - 1.00001f));
                        k = (int) kz;
                        dz = kz - (double) k;
                        shape = new int[]{2, 2, 2};
                        origin = new int[]{k, j, i};
                        array = variable.read(origin, shape);
                        return interp3D((ArrayFloat.D3) array, dx, dy, dz, n);
                    }
                case 2:
                    shape = new int[]{2, 2};
                    origin = new int[]{j, i};
                    array = variable.read(origin, shape);
                    return interp2D((ArrayFloat.D2) array, dx, dy, n);
            }
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private double interpTime(double value_t0, double value_t1, double time) {
        double frac = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        return (1.d - frac) * value_t0 + frac * value_t1;
    }

    private double interp2D(ArrayFloat.D2 array, double dx, double dy, int n) {
        double value = 0.d;
        double CO = 0.d;

        for (int jj = 0; jj < n; jj++) {
            for (int ii = 0; ii < n; ii++) {
                double co = Math.abs((1.d - (double) ii - dx) *
                        (1.d - (double) jj - dy));
                CO += co;
                value += array.get(jj, ii) * co;
            }
        }

        if (CO != 0) {
            value /= CO;
        }
        return value;
    }

    private double interp3D(ArrayFloat.D3 array, double dx, double dy, double dz, int n) {
        double value = 0.d;
        double CO = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < n; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    double co = Math.abs((1.d - (double) ii - dx) *
                            (1.d - (double) jj - dy) *
                            (1.d - (double) kk - dz));
                    CO += co;
                    value += array.get(kk, jj, ii) * co;
                }
            }
        }
        if (CO != 0) {
            value /= CO;
        }
        return value;
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
}
