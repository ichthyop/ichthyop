/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.dataset;

import fr.ird.ichthyop.MetaFilenameFilter;
import fr.ird.ichthyop.NCComparator;
import fr.ird.ichthyop.NextStepEvent;
import fr.ird.ichthyop.util.Constant;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class Roms3dUclaDataset extends AbstractDataset {

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
    private String strThetaS, strThetaB, strHc, strPn, strPm;
    /**
     * Name of the Variable in NetCDF file
     */
    static String strLargePhyto, strLargeZoo, strSmallZoo;
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
    private static boolean FLAG_3D;

    @Override
    void loadParameters() {

        FLAG_3D = Boolean.valueOf(getSimulation().getParameterManager().getValue("app.transport", "three-dimension"));

    }

    private void openLocation(String rawPath) throws IOException {

        URI uriCurrent = new File("").toURI();
        //String path = URI.create(rawPath).getPath();
        String path = uriCurrent.resolve(URI.create(rawPath)).getPath();

        if (isDirectory(path)) {
            listInputFiles = getInputList(path);
        }
        open(listInputFiles.get(0));
    }

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

    private void loadFieldNames() {

        strXiDim = getParameter("field_dim_xi");
        strEtaDim = getParameter("field_dim_eta");
        strZDim = getParameter("field_dim_z");
        strTimeDim = getParameter("field_dim_time");
        strLon = getParameter("field_var_lon");
        strLat = getParameter("field_var_lat");
        strBathy = getParameter("field_var_bathy");
        strMask = getParameter("field_var_mask");
        strU = getParameter("field_var_u");
        strV = getParameter("field_var_v");
        strZeta = getParameter("field_var_zeta");
        strTp = getParameter("field_var_tp");
        strSal = getParameter("field_var_sal");
        strTime = getParameter("field_var_time");
        strKv = getParameter("field_var_kv");
        strPn = getParameter("field_var_pn");
        strPm = getParameter("field_var_pm");
        strThetaS = getParameter("field_attrib_thetas");
        strThetaB = getParameter("field_attrib_thetab");
        strHc = getParameter("field_attrib_hc");
    }

    public void setUp() {

        loadParameters();
        loadFieldNames();
        try {
            openLocation(getParameter("input_path"));
            getDimNC();
            /*if (Boolean.valueOf(getParameter("is_ranged"))) {
            //range(Configuration.getP1(), Configuration.getP2());
            }*/
            readConstantField();
            //getDimGeogArea();
            getCstSigLevels();
            if (FLAG_3D) {
                z_w_tp0 = getSigLevels();
            }

            long t0 = getSimulation().getStep().get_tO();
            open(getFile(t0));
            FLAG_TP = FLAG_SAL = false;
            setAllFieldsTp1AtTime(rank = findCurrentRank(t0));
            time_tp1 = t0;
        } catch (IOException ex) {
            Logger.getLogger(Roms3dUclaDataset.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private int findCurrentRank(long time) throws IOException {

        int lrank = 0;
        int time_arrow = (int) Math.signum(getSimulation().getStep().get_dt());
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
        int time_arrow = (int) Math.signum(getSimulation().getStep().get_dt());

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

        double thetas = 0, thetab = 0, hc = 0;
        double cff1, cff2;
        double[] sc_r = new double[nz];
        double[] Cs_r = new double[nz];
        double[] cff_r = new double[nz];
        double[] sc_w = new double[nz + 1];
        double[] Cs_w = new double[nz + 1];
        double[] cff_w = new double[nz + 1];

        //-----------------------------------------------------------
        // Read the Param in ncIn
        try {
            if (ncIn.findGlobalAttribute(strThetaS) == null) {
                System.out.println("ROMS Rutgers");
                thetas = ncIn.findVariable(strThetaS).readScalarDouble();
                thetab = ncIn.findVariable(strThetaB).readScalarDouble();
                hc = ncIn.findVariable(strHc).readScalarDouble();
            } else {
                System.out.println("ROMS UCLA");
                thetas = (ncIn.findGlobalAttribute(strThetaS).getNumericValue()).doubleValue();
                thetab = (ncIn.findGlobalAttribute(strThetaB).getNumericValue()).doubleValue();
                hc = (ncIn.findGlobalAttribute(strHc).getNumericValue()).doubleValue();
            }
        } catch (IOException e) {
            throw new IOException(
                    "Problem reading thetaS/thetaB/hc at location " + ncIn.getLocation().toString() + " : " + e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException(
                    "Problem reading thetaS/thetaB/hc at location " + ncIn.getLocation().toString() + " : " + e.getMessage());
        }

        //-----------------------------------------------------------
        // Calculation of the Coeff
        cff1 = 1.d / sinh(thetas);
        cff2 = .5d / tanh(.5d * thetas);
        for (int k = nz; k-- > 0;) {
            sc_r[k] = ((double) (k - nz) + .5d) / (double) nz;
            Cs_r[k] = (1.d - thetab) * cff1 * sinh(thetas * sc_r[k]) + thetab * (cff2 * tanh((thetas * (sc_r[k] + .5d))) - .5d);
            cff_r[k] = hc * (sc_r[k] - Cs_r[k]);
        }

        for (int k = nz + 1; k-- > 0;) {
            sc_w[k] = (double) (k - nz) / (double) nz;
            Cs_w[k] = (1.d - thetab) * cff1 * sinh(thetas * sc_w[k]) + thetab * (cff2 * tanh((thetas * (sc_w[k] + .5d))) - .5d);
            cff_w[k] = hc * (sc_w[k] - Cs_w[k]);
        }
        sc_w[0] = -1.d;
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

    void readConstantField() throws IOException {

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
            arrLon = ncIn.findVariable(strLon).read(origin, size);
            arrLat = ncIn.findVariable(strLat).read(origin, size);
            arrMask = ncIn.findVariable(strMask).read(origin, size);
            arrH = ncIn.findVariable(strBathy).read(origin, size);
            arrZeta = ncIn.findVariable(strZeta).read(new int[]{0, jpo, ipo},
                    new int[]{1, ny, nx}).reduce();
            arrPm = ncIn.findVariable(strPm).read(origin, size);
            arrPn = ncIn.findVariable(strPn).read(origin, size);

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
            throw new IOException("Problem reading one of the fields " + list.toString() + " at location " + ncIn.getLocation().toString() + " : " +
                    e.getMessage());
        } catch (InvalidRangeException e) {
            throw new IOException("Problem reading one of the fields " + list.toString() + " at location " + ncIn.getLocation().toString() + " : " +
                    e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem reading one of the fields " + list.toString() + " at location " + ncIn.getLocation().toString() + " : " +
                    e.getMessage());
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
            nz = (FLAG_3D)
                    ? ncIn.findDimension(strZDim).getLength()
                    : 1;
        } catch (NullPointerException e) {
            throw new IOException("Problem reading dimensions from dataset " + ncIn.getLocation() + " : " + e.getMessage());
        }

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

    public double[] advectEuler(double[] pGrid, double time, double dt) {

        double co, CO, x, dw, du, dv, x_euler;
        int n = isCloseToCost(pGrid) ? 1 : 2;

        //-----------------------------------------------------------
        // Interpolate the velocity, temperature and salinity fields
        // in the computational grid.

        double ix, jy, kz;
        ix = pGrid[0];
        jy = pGrid[1];
        kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        du = 0.d;
        dv = 0.d;
        dw = 0.d;
        x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

        try {
            //-----------------------
            //Get dw
            int i = (int) ix;
            int j = (int) jy;
            int k = (int) Math.round(kz);
            double dx = ix - (double) i;
            double dy = jy - (double) j;
            double dz = kz - (double) k;
            CO = 0.d;
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
            dw *= dt;
            if (CO != 0) {
                dw /= CO;
            }

            //------------------------
            // Get du
            //kz = Math.min(kz, nz - 1.00001f);
            i = (int) Math.round(ix);
            k = (int) kz;
            dx = ix - (double) i;
            dz = kz - (double) k;
            CO = 0.d;
            for (int ii = 0; ii < 2; ii++) {
                for (int jj = 0; jj < n; jj++) {
                    for (int kk = 0; kk < 2; kk++) {
                        //if (isInWater(i + ii, j + jj)) {
                        {
                            co = Math.abs((.5d - (double) ii - dx) *
                                    (1.d - (double) jj - dy) *
                                    (1.d - (double) kk - dz));
                            CO += co;
                            x = (1.d - x_euler) * u_tp0[k + kk][j + jj][i + ii -
                                    1] + x_euler * u_tp1[k + kk][j + jj][i + ii - 1];
                            du += .5d * x * co *
                                    (pm[j + jj][Math.max(i + ii - 1, 0)] + pm[j +
                                    jj][i + ii]);
                        }
                    }
                }
            }
            du *= dt;
            if (CO != 0) {
                du /= CO;
            }

            //-------------------------
            // Get dv
            i = (int) ix;
            j = (int) Math.round(jy);
            dx = ix - (double) i;
            dy = jy - (double) j;
            CO = 0.d;
            for (int kk = 0; kk < 2; kk++) {
                for (int jj = 0; jj < 2; jj++) {
                    for (int ii = 0; ii < n; ii++) {
                        //if (isInWater(i + ii, j + jj)) {
                        {
                            co = Math.abs((1.d - (double) ii - dx) *
                                    (.5d - (double) jj - dy) *
                                    (1.d - (double) kk - dz));
                            CO += co;
                            x = (1.d - x_euler) * v_tp0[k + kk][j + jj - 1][i +
                                    ii] + x_euler * v_tp1[k + kk][j + jj - 1][i + ii];
                            dv += .5d * x * co * (pn[Math.max(j + jj - 1, 0)][i + ii] + pn[j + jj][i + ii]);
                        }
                    }
                }
            }
            dv *= dt;
            if (CO != 0) {
                dv /= CO;
            }
        } catch (java.lang.ArrayIndexOutOfBoundsException e) {
            throw new ArrayIndexOutOfBoundsException(
                    "Problem interpolating velocity fields : " + e.getMessage());
        }
        if (du > Constant.THRESHOLD_CFL) {
            System.err.println("! WARNING : CFL broken for u " + (float) du);
        }
        if (dv > Constant.THRESHOLD_CFL) {
            System.err.println("! WARNING : CFL broken for v " + (float) dv);
        }

        return (new double[]{du, dv, dw});
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

    public double getDepth(double xRho, double yRho, int k) {

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
            ;
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

    private boolean isInWater(int i, int j) {
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
            Logger.getLogger(Roms3dUclaDataset.class.getName()).log(Level.SEVERE, null, ex);
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
            System.out.print("Open dataset " + filename + "\n");
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

            /*if (FLAG_VDISP) {
            kv_tp1 = (float[][][]) ncIn.findVariable(strKv).read(origin,
            new int[]{1, nz, ny, nx}).reduce().copyToNDJavaArray();
            }*/


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
    void readLonLat() throws IOException {
        Array arrLon, arrLat;
        try {
            arrLon = ncIn.findVariable(strLon).read();
            arrLat = ncIn.findVariable(strLat).read();
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
            throw new IOException("Problem reading lon/lat fields at location " + ncIn.getLocation().toString() + " : " +
                    e.getMessage());
        } catch (NullPointerException e) {
            throw new IOException("Problem reading lon/lat at location " + ncIn.getLocation().toString() + " : " +
                    e.getMessage());
        }

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
}
