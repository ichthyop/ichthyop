/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.ui.LonLatConverter;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;
import org.previmer.ichthyop.util.MetaFilenameFilter;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class NemoDataset extends AbstractDataset {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Grid dimension
     */
    private int nx, ny, nz;
    /**
     * Origin for grid index
     */
    private int ipo, jpo;
    /**
     * Number of time records in current NetCDF file
     */
    private int nbTimeRecords;
    /**
     * Longitude at rho point.
     */
    private float[][] lonRho;
    /**
     * Latitude at rho point.
     */
    private float[][] latRho;
    /**
     * Mask: water = 1, cost = 0
     */
    private int[][][] maskRho;//, masku, maskv;
    /**
     * Zonal component of the velocity field at current time
     */
    private Array u_tp0;
    /**
     * Zonal component of the velocity field at time t + dt
     */
    private Array u_tp1;
    /**
     * Meridional component of the velocity field at current time
     */
    private Array v_tp0;
    /**
     * Meridional component of the velocity field at time t + dt
     */
    private Array v_tp1;
    /**
     * Vertical component of the velocity field at current time
     */
    private Array w_tp0;
    /**
     * Vertical component of the velocity field at time t + dt
     */
    private Array w_tp1;
    /**
     * Depth at rho point
     */
    private double[] gdepT;
    /**
     * Depth at w point. The free surface elevation is disregarded.
     */
    private double[] gdepW;
    /**
     * Geographical boundary of the domain
     */
    private double latMin, lonMin, latMax, lonMax;
    /**
     * Maximum depth [meter] of the domain
     */
    private double depthMax;
    /**
     * Time step [second] between two records in NetCDF dataset
     */
    private double dt_HyMo;
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
     * Name of the Dimension in NetCDF file
     */
    private String strXDim, strYDim, strZDim, strTimeDim;
    /**
     * Name of the Variable in NetCDF file
     */
    private String strU, strV, strW, strTime;
    /**
     * Name of the Variable in NetCDF file
     */
    private String strLon, strLat, strMask;
    /**
     *
     */
    private double[][][] e3t, e3u, e3v;
    private double[][] e1t, e2t, e1v, e2u;
    private String stre1t, stre2t, stre3t, stre1v, stre2u, stre3u, stre3v;
    private String str_gdepT, str_gdepW;
    private List<String> listUFiles, listVFiles, listWFiles, listTFiles;
    private NetcdfFile ncU, ncV, ncW, ncT;
    private String file_hgr, file_zgr, file_mask;
    private boolean isGridInfoInOneFile;
    // Whether vertical velocity should be read from NetCDF or calculated from U & V
    private boolean readW;

////////////////////////////
// Definition of the methods
////////////////////////////
    @Override
    public boolean is3D() {
        return true;
    }

    /**
     * Reads time non-dependant fields in NetCDF dataset
     */
    private void readConstantField() throws Exception {

        NetcdfFile nc;
        nc = NetcdfDataset.openDataset(file_hgr, enhanced(), null);
        getLogger().log(Level.INFO, "read lon, lat & mask from {0}", nc.getLocation());
        lonRho = new float[ny][nx];
        latRho = new float[ny][nx];
        Array arrLon, arrLat;
        Index index;
        switch (nc.findVariable(strLon).getShape().length) {
            case 2:
                int[] origin = new int[]{jpo, ipo};
                int[] size = new int[]{ny, nx};
                arrLon = nc.findVariable(strLon).read(origin, size);
                arrLat = nc.findVariable(strLat).read(origin, size);
                index = arrLon.getIndex();
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        index.set(j, i);
                        lonRho[j][i] = arrLon.getFloat(index);
                        latRho[j][i] = arrLat.getFloat(index);
                        System.out.println(latRho[j][i] + " " + lonRho[j][i]);
                    }
                }
                break;
            case 1:
                arrLon = nc.findVariable(strLon).read(new int[]{ipo}, new int[]{nx});
                arrLat = nc.findVariable(strLon).read(new int[]{jpo}, new int[]{ny});
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        lonRho[j][i] = arrLon.getFloat(arrLon.getIndex().set(i));
                        latRho[j][i] = arrLat.getFloat(arrLat.getIndex().set(j));
                    }
                }
                break;
            default:
                throw new IOException("Latitude and longitude variables are expected to have either one or two dimensions.");
        }
        
        if (!isGridInfoInOneFile) {
            nc.close();
            nc = NetcdfDataset.openDataset(file_mask, enhanced(), null);
        }
        maskRho = new int[nz][ny][nx];
        Array arrMask;
        switch (nc.findVariable(strMask).getShape().length) {
            case 4:
                arrMask = nc.findVariable(strMask).read(new int[]{0,
                    0, jpo, ipo}, new int[]{1, nz, ny, nx}).flip(1).reduce();
                break;
            case 3:
                arrMask = nc.findVariable(strMask).read(new int[]{0, jpo, ipo}, new int[]{nz, ny, nx}).flip(1).reduce();
                break;
            default:
                throw new IOException("Mask variable is expected to have either three or four dimensions.");
        }
        index = arrMask.getIndex();
        for (int k = 0; k < nz; k++) {
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    maskRho[k][j][i] = arrMask.getInt(index.set(k, j, i));
                }
            }
        }
        
        if (!isGridInfoInOneFile) {
            nc.close();
            nc = NetcdfDataset.openDataset(file_zgr, enhanced(), null);
        }
        //System.out.println("read bathy gdept gdepw e3t " + nc.getLocation());
        //fichier *mesh*z*
        get_gdep_fields(nc);

        // phv 20150319 - patch for e3t that can be found in NEMO output spread
        // into three variables e3t_0, e3t_ps and mbathy
        if (findParameter("field_var_e3t0") && findParameter("field_var_e3tps") && findParameter("field_var_mbathy")) {
            compute_e3t();
        } else {
            e3t = read_e3_field(nc, stre3t);
        }
        if (stre3u.equals(stre3t) || (null == nc.findVariable(stre3u))) {
            e3u = compute_e3u(e3t);
        } else {
            e3u = read_e3_field(nc, stre3u);
        }
        if (stre3v.equals(stre3t) || (null == nc.findVariable(stre3v))) {
            e3v = compute_e3v(e3t);
        } else {
            e3v = read_e3_field(nc, stre3v);
        }

        if (!isGridInfoInOneFile) {
            nc.close();
            nc = NetcdfDataset.openDataset(file_hgr, enhanced(), null);
        }
        //System.out.println("read e1t e2t " + nc.getLocation());
        // fichier *mesh*h*
        e1t = read_e1_e2_field(nc, stre1t);
        e2t = read_e1_e2_field(nc, stre2t);
        if (stre1v.equals(stre1t) || (null == nc.findVariable(stre1v))) {
            e1v = e1t;
        } else {
            e1v = read_e1_e2_field(nc, stre1v);
        }
        if (stre2u.equals(stre2t) || (null == nc.findVariable(stre2u))) {
            e2u = e2t;
        } else {
            e2u = read_e1_e2_field(nc, stre2u);
        }
        nc.close();
    }

    private void compute_e3t() {

        // double[nz]
        double[] e3t_0 = null;
        // double[ny][nx]
        double[][] e3t_ps = null;
        // int[ny][nx]
        short[][] mbathy = null;

        String str_e3t0 = getParameter("field_var_e3t0");
        String str_e3tps = getParameter("field_var_e3tps");
        String str_mbathy = getParameter("field_var_mbathy");

        getLogger().log(Level.INFO, "Ichthyop now reconstructs the e3t variable from {0}, {1} and {2}", new String[]{str_e3t0, str_e3tps, str_mbathy});

        try {
            // Open NetCDF file
            NetcdfFile nc = NetcdfDataset.openDataset(file_zgr, enhanced(), null);

            // Read e3t_0
            e3t_0 = (double[]) nc.findVariable(str_e3t0).read(new int[]{0, 0}, new int[]{1, nz}).flip(1).reduce().copyToNDJavaArray();

            // Read e3t_ps
            e3t_ps = (double[][]) nc.findVariable(str_e3tps).read(new int[]{0, jpo, ipo}, new int[]{1, ny, nx}).reduce().copyToNDJavaArray();

            // Read mbathy
            mbathy = (short[][]) nc.findVariable(str_mbathy).read(new int[]{0, jpo, ipo}, new int[]{1, ny, nx}).reduce().copyToNDJavaArray();

        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "Error while reconstructing e3t[][][] from e3t_0, e3t_ps and mbathy...", ex);
            System.exit(1);
        }

        // Reconstruct e3t_ps
        e3t = new double[nz][ny][nx];
        for (int j = 0; j < ny; j++) {
            for (int i = 0; i < nx; i++) {
                // First we initialize e3t with e3t_0
                for (int k = 0; k < nz; k++) {
                    e3t[k][j][i] = e3t_0[k];
                }
                // From NEMO to Ichthyop grid, we remove the deepest z level
                // as it is always ocean bottom in NEMO and we flip z-axis
                // So the index of mbathy must be converted into Ichthyop grid
                int km = nz - (int) mbathy[j][i] - 1;
                // Next we correct the depth of the layer adjacent to the ocean
                // bottom with e3t_ps
                if (km > 0) {
                    e3t[km][j][i] = e3t_ps[j][i];
                }
            }
        }

    }

    private double[][][] compute_e3u(double[][][] e3t) {

        double[][][] e3u_calc = new double[nz][ny][nx];

        for (int k = 0; k < nz; k++) {
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx - 1; i++) {
                    /*
                     * In NEMO domzgr.F90
                     * e3u (ji,jj,jk) = MIN( e3t(ji,jj,jk), e3t(ji+1,jj,jk))
                     */
                    //e3u_calc[k][j][i] = 0.5d * (e3t[k][j][i] + e3t[k][j][i + 1]);
                    e3u_calc[k][j][i] = Math.min(e3t[k][j][i], e3t[k][j][i + 1]);
                }
                e3u_calc[k][j][nx - 1] = e3t[k][j][nx - 1];
            }
        }
        return e3u_calc;
    }

    private double[][][] compute_e3v(double[][][] e3t) {

        double[][][] e3v_calc = new double[nz][ny][nx];

        for (int k = 0; k < nz; k++) {
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny - 1; j++) {
                    /*
                     * In Nemo domzgr.F90
                     * e3v (ji,jj,jk) = MIN( e3t(ji,jj,jk), e3t(ji,jj+1,jk))
                     */
                    //e3v_calc[k][j][i] = 0.5d * (e3t[k][j][i] + e3t[k][j + 1][i]);
                    e3v_calc[k][j][i] = Math.min(e3t[k][j][i], e3t[k][j + 1][i]);
                }
                e3v_calc[k][ny - 1][i] = e3t[k][ny - 1][i];
            }
        }
        return e3v_calc;
    }

    private double[][][] read_e3_field(NetcdfFile nc, String varname) throws InvalidRangeException, IOException {

        Variable ncvar;
        Index index;
        Array array;

        ncvar = nc.findVariable(stre3t);
        switch (ncvar.getShape().length) {
            case 4:
                array = ncvar.read(new int[]{0, 0, jpo, ipo}, new int[]{1, nz, ny, nx}).flip(1).reduce();
                break;
            case 3:
                array = ncvar.read(new int[]{0, jpo, ipo}, new int[]{nz, ny, nx}).flip(0).reduce();
            default:
                throw new UnsupportedOperationException("Field " + varname + " cannot be read because of undexpected dimensions.");
        }
        index = array.getIndex();
        double[][][] field = new double[nz][ny][nx];
        for (int k = 0; k < nz; k++) {
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    index.set(k, j, i);
                    field[k][j][i] = array.getDouble(index);
                }
            }
        }
        return field;
    }

    private void get_gdep_fields(NetcdfFile nc) throws InvalidRangeException, IOException {

        Variable ncvar;
        Index index;
        Array array;
        /*
         * Read gdept
         */
        ncvar = nc.findVariable(str_gdepT);
        if (ncvar.getShape().length > 2) {
            array = ncvar.read(new int[]{0, 0, 0, 0}, new int[]{1, nz, 1, 1}).flip(1).reduce();
        } else {
            array = ncvar.read(new int[]{0, 0}, new int[]{1, nz}).flip(1).reduce();
        }
        index = array.getIndex();
        gdepT = new double[nz];
        for (int k = 0; k < nz; k++) {
            index.set(k);
            gdepT[k] = array.getDouble(index);
        }
        /*
         * Read or compute gdepw
         */
        gdepW = new double[nz + 1];
        ncvar = nc.findVariable(str_gdepW);
        // Read gdepw
        if (null != ncvar) {
            if (ncvar.getShape().length > 2) {
                array = ncvar.read(new int[]{0, 0, 0, 0}, new int[]{1, nz + 1, 1, 1}).flip(1).reduce();
            } else {
                array = ncvar.read(new int[]{0, 0}, new int[]{1, nz + 1}).flip(1).reduce();
            }
            index = array.getIndex();

            for (int k = 0; k < nz + 1; k++) {
                index.set(k);
                gdepW[k] = array.getDouble(index);
            }
        } else {
            // Compute gdepw (approximation)
            for (int k = 1; k < nz; k++) {
                gdepW[k] = 0.5 * (gdepT[k - 1] + gdepT[k]);
            }
            gdepW[0] = gdepT[0];
            gdepW[nz] = 0.;
        }

    }

    private double[][] read_e1_e2_field(NetcdfFile nc, String varname) throws InvalidRangeException, IOException {

        Variable ncvar = nc.findVariable(varname);
        Array array;
        if (ncvar.getShape().length > 3) {
            array = ncvar.read(new int[]{0, 0, jpo, ipo}, new int[]{1, 1, ny, nx}).reduce();
        } else {
            array = ncvar.read(new int[]{0, jpo, ipo}, new int[]{1, ny, nx}).reduce();
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

    /**
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
     *
     * @param pGrid
     * @param time
     * @return
     */
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
        double co, x;
        Index index = u_tp0.getIndex();
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < n; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    co = Math.abs((.5d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    index.set(k + kk, j + jj, i + ii - 1);
                    if (!(Double.isNaN(u_tp0.getDouble(index)))) {
                        x = (1.d - x_euler) * u_tp0.getDouble(index)
                                + x_euler * u_tp1.getDouble(index);
                        du += x * co / e2u[j + jj][i + ii - 1];
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
        double co, x;
        Index index = w_tp0.getIndex();
        for (int ii = 0; ii < n; ii++) {
            for (int jj = 0; jj < n; jj++) {
                for (int kk = 0; kk < 2; kk++) {
                    co = Math.abs((1.d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (.5d - (double) kk - dz));
                    CO += co;
                    index.set(k + kk, j + jj, i + ii);
                    x = (1.d - x_euler) * w_tp0.getDouble(index)
                            + x_euler * w_tp1.getDouble(index);
                    dw += 2.d * x * co
                            / (gdepW[Math.max(k + kk - 1, 0)]
                            - gdepW[Math.min(k + kk + 1, nz)]);
                }
            }
        }
        if (CO != 0) {
            dw /= CO;
        }

        /*double dwr = get_dWrz(pGrid, time);
         float err = (float) Math.abs((dwr - dw) / dwr);
         System.out.println("dw: " + dw + " - dwr: " + dwr + " - err: " + err);*/
        return dw;
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
        double co, x;
        Index index = v_tp0.getIndex();
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < 2; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    co = Math.abs((1.d - (double) ii - dx)
                            * (.5d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    index.set(k + kk, j + jj - 1, i + ii);
                    if (!Double.isNaN(v_tp0.getDouble(index))) {
                        x = (1.d - x_euler) * v_tp0.getDouble(index)
                                + x_euler * v_tp1.getDouble(index);
                        dv += x * co / e1v[j + jj - 1][i + ii];
                    }
                }
            }
        }
        if (CO != 0) {
            dv /= CO;
        }
        return dv;
    }

    /**
     * Computes the vertical velocity vector.
     * <pre>
     * Drill:
     *
     *     +-----V(i, j, k)-----+
     *     |                    |
     *     |                    |
     * U(i - 1, j, k)  +      U(i, j, k)
     *     |                    |
     *     |                    |
     *     +---V(i, j - 1, k)---+
     *
     * Lets call Su(i, j, k) the grid face for which U(i, j, k) is the normal.
     * Same definition for Sv(i, j, k) and Sw(i, j, k)
     * Mass conservation means that:
     *   U(i, j, k) * Su(i, j, k) - U(i - 1, j, k) * Su(i - 1, j, k)
     * + V(i, j, k) * Sv(i, j, k) - V(i, j - 1, k) * Sv(i, j - 1, k)
     * + W(i, j, k) * Sw(i, j, k) - W(i, j, k - 1) * Sw(i, j, k - 1)
     * = 0
     * Bottom conditions: W(i, j, 0) = 0
     * Reading U and V in the NetCDF dataset, we can therefore compute the
     * vertical flux W(i, j, k) * Sw(i, j, k) from bottom to surface and
     * then the vertical velocity.
     * This last result is the abolute vertical velocity. Nonetheless, ROMS and
     * MARS use a sigma level vertical grid, that dilates and contracts function
     * of the free surface elevation. So we must substract to the vertical
     * velocity the grid vertical velocity.
     *
     * Let's take Zw(i, j, k) the depth at w-point and let's call Wgrid the
     * vertical velocity of the grid at the sigma levels.
     * Wgrid(i, j, k) = W(i, j, N)
     *                  * [Zw(i, j, k) - Zw(i, j, 0)]
     *                  / [Zw(i, j, N) - Zw(i, j, 0)];
     *
     * So the water vertical velocity at sigma levels is
     * Ws(i, j, k) = W(i, j, k) - Wgrid(i, j, k)
     * </pre>
     */
    private Array computeW() {

        double[][][] Huon = new double[nz][ny][nx];
        double[][][] Hvom = new double[nz][ny][nx];

        //---------------------------------------------------
        // Calculation Coeff Huon & Hvom
        Index index;
        for (int k = nz; k-- > 0;) {
            index = u_tp1.getIndex();
            for (int i = 0; i < nx - 1; i++) {
                for (int j = 0; j < ny; j++) {
//                    Huon[k][j][i] = u_tp1[k][j][i] * e2u[j][i] * Math.min(e3t[k][j][i], e3t[k][j][i + 1]);
                    index.set(k, j, i);
                    Huon[k][j][i] = Double.isNaN(u_tp1.getDouble(index))
                            ? 0.f
                            : u_tp1.getDouble(index) * e2u[j][i] * e3u[k][j][i];
                }
            }
            index = v_tp1.getIndex();
            for (int i = 0; i < nx; i++) {
                for (int j = 0; j < ny - 1; j++) {
//                    Hvom[k][j][i] = v_tp1[k][j][i] * e1v[j][i] * Math.min(e3t[k][j][i], e3t[k][j + 1][i]);
                    index.set(k, j, i);
                    Hvom[k][j][i] = Double.isNaN(v_tp1.getDouble(index))
                            ? 0.f
                            : v_tp1.getDouble(index) * e1v[j][i] * e3v[k][j][i];
                }
            }
        }

        //---------------------------------------------------
        // Calcultaion of w(i, j, k)
        double[][][] w_double = new double[nz + 1][ny][nx];

        for (int j = 1; j < ny - 1; j++) {
            for (int i = 1; i < nx - 1; i++) {
                /*
                 * pverley 15/02/2011
                 * I must start integrating the vertical velocity at the bottom
                 * of the water column wich is not necessarily k = 0;
                 * So first I look for k0 such as (k0 - 1) is bottom, k0 is first
                 * cell of the column in water.
                 */
                int k0 = 0;
                for (int k = 0; k < nz; k++) {
                    if (isInWater(k, j, i)) {
                        k0 = k;
                        break;
                    }
                }
                /*
                 * pverley 15/02/2011
                 * Ensured that w(0:k0, :, :) = 0;
                 */
                for (int k = 0; k < k0 + 1; k++) {
                    w_double[k][j][i] = 0.d;
                    //System.out.println("k: " + k + " k0: " + k0 + " wr: " + wr_tp1[k][j][i] + " " + isInWater(k, j, i));
                }
                for (int k = k0 + 1; k < nz; k++) {
                    w_double[k][j][i] = w_double[k - 1][j][i]
                            - (Huon[k - 1][j][i] - Huon[k - 1][j][i - 1] + Hvom[k - 1][j][i] - Hvom[k - 1][j - 1][i]);
                }
                w_double[nz][j][i] = 0.d;
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
        // w * dxu * dyv
        //float[][][] w = new float[nz + 1][ny][nx];
        Array w = new ArrayDouble.D3(nz + 1, ny, nx);
        index = w.getIndex();
        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                for (int k = nz + 1; k-- > 0;) {
                    w.setDouble(index.set(k, j, i), isInWater(i, j, k)
                            ? (float) (w_double[k][j][i] / (e1t[j][i] * e2t[j][i]))
                            : 0.f);
                }
            }
        }

        //---------------------------------------------------
        // Return w
        return w;
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
        nc = NetcdfDataset.openDataset(file_hgr, enhanced(), null);
        arrLon = nc.findVariable(strLon).read().reduce();
        arrLat = nc.findVariable(strLat).read().reduce();
        lonRho = new float[ny][nx];
        latRho = new float[ny][nx];
        switch (arrLon.getShape().length) {
            case 2:
                Index index = arrLon.getIndex();
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        index.set(j, i);
                        lonRho[j][i] = arrLon.getFloat(index);
                        latRho[j][i] = arrLat.getFloat(index);
                    }
                }
                break;
            case 1:
                for (int j = 0; j < ny; j++) {
                    for (int i = 0; i < nx; i++) {
                        lonRho[j][i] = arrLon.getFloat(arrLon.getIndex().set(i));
                        latRho[j][i] = arrLat.getFloat(arrLat.getIndex().set(j));
                    }
                }
                break;
            default:
                throw new IOException("Latitude and longitude variables are expected to have either one or two dimensions.");
        }
        nc.close();

    }

    /*
     * Gets cell dimension [meter] in the XI-direction.
     *
     * pverley pour chourdin: vérifier avec Steph que je ne me trompe pas dans
     * la définition de e1t et e2t
     */
    @Override
    public double getdxi(int j, int i) {
        return e1t[j][i];
    }

    /*
     * Gets cell dimension [meter] in the ETA-direction.
     */
    @Override
    public double getdeta(int j, int i) {
        return e2t[j][i];
    }

    /*
     * Sets up the {@code Dataset}. The method first sets the appropriate
     * variable names, loads the first NetCDF dataset and extract the time
     * non-dependant information, such as grid dimensions, geographical
     * boundaries, depth at sigma levels.
     *
     * @throws an IOException if an error occurs while setting up the
     * {@code Dataset}
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

        if (findParameter("shrink_domain") && Boolean.valueOf(getParameter("shrink_domain"))) {
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

    /*
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
        if (findParameter("read_var_w")) {
            readW = Boolean.valueOf(getParameter("read_var_w"));
        } else {
            readW = false;
            getLogger().warning("Ichthyop will recalculate W variable from U and V");
        }
        if (readW) {
            strW = getParameter("field_var_w");
        }
        strTime = getParameter("field_var_time");
        stre3t = getParameter("field_var_e3t");
        stre3u = getParameter("field_var_e3u");
        stre3v = getParameter("field_var_e3v");
        str_gdepT = getParameter("field_var_gdept"); // z_rho
        str_gdepW = getParameter("field_var_gdepw"); // z_w
        stre1t = getParameter("field_var_e1t");
        stre2t = getParameter("field_var_e2t");
        stre1v = getParameter("field_var_e1v");
        stre2u = getParameter("field_var_e2u");
        if (!findParameter("enhanced()_mode")) {
            getLogger().warning("Ichthyop assumes that the NEMO NetCDF files must be opened in enhanced() mode (scale,offset,missing).");
        }
        time_arrow = timeArrow();
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

        NetcdfFile nc = NetcdfDataset.openDataset(file_mask, enhanced(), null);
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
        try {
            nz = nc.findDimension(strZDim).getLength() - 1;
        } catch (Exception ex) {
            IOException ioex = new IOException("Error reading dataset Z dimension. " + ex.toString());
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

    /*
     * Initializes the {@code Dataset}. Opens the file holding the first time of
     * the simulation. Checks out the existence of the fields required by the
     * current simulation. Sets all fields at time for the first time step.
     *
     * @throws an IOException if a required field cannot be found in the NetCDF
     * dataset.
     */
    @Override
    public void init() throws Exception {

        double t0 = getSimulationManager().getTimeManager().get_tO();
        open(indexFile = DatasetUtil.index(listUFiles, t0, time_arrow, strTime));
        checkRequiredVariable(ncT);
        setAllFieldsTp1AtTime(rank = DatasetUtil.rank(t0, ncU, strTime, time_arrow));
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
            u_tp1 = ncU.findVariable(strU).read(origin, new int[]{1, nz, ny, nx - 1}).flip(1).reduce();
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading U velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            v_tp1 = ncV.findVariable(strV).read(origin, new int[]{1, nz, ny - 1, nx}).flip(1).reduce();
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

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
        for (RequiredVariable variable : requiredVariables.values()) {
            variable.nextStep(readVariable(ncT, variable.getName(), rank), time_tp1, dt_HyMo);
        }
        if (readW) {
            try {
                w_tp1 = ncW.findVariable(strW).read(origin, new int[]{1, nz + 1, ny, nx}).flip(1).reduce();
            } catch (IOException | InvalidRangeException ex) {
                IOException ioex = new IOException("Error reading W variable. " + ex.toString());
                ioex.setStackTrace(ex.getStackTrace());
                throw ioex;
            }
        } else {
            w_tp1 = computeW();
        }
    }

    /*
     * Computes the depth of the specified sigma level at the x-y particle
     * location.
     *
     * @param xRho a double, x-coordinate of the grid point
     * @param yRho a double, y-coordinate of the grid point
     * @param k an int, the index of the sigma level
     * @return a double, the depth [meter] at (x, y, k)
     */
    public double getDepth(int k) {

        return -1.d * gdepT[k];
    }

    /**
     * Determines whether or not the specified grid cell(i, j) is in water.
     *
     * @param i an int, i-coordinate of the cell
     * @param j an intn the j-coordinate of the cell
     * @return <code>true</code> if cell(i, j) is in water, <code>false</code>
     * otherwise.
     */
    private boolean isInWater(int i, int j, int k) {
        //System.out.println(i + " " + j + " " + k + " - "  + (maskRho[k][j][i] > 0));
        try {
            return (maskRho[k][j][i] > 0);
        } catch (ArrayIndexOutOfBoundsException ex) {
            return false;
        }
    }

    @Override
    public boolean isInWater(int i, int j) {
        return isInWater(i, j, nz - 1);
    }

    /*
     * Determines whether the specified {@code RohPoint} is in water.
     *
     * @param ptRho the RhoPoint
     * @return <code>true</code> if the {@code RohPoint} is in water,
     * <code>false</code> otherwise.
     * @see #isInWater(int i, int j)
     */
    @Override
    public boolean isInWater(double[] pGrid) {
        if (pGrid.length > 2) {
            return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]), (int) Math.round(pGrid[2]));
        } else {
            return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
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

        int i, j, k, ii, jj;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        k = (int) (Math.round(pGrid[2]));
        ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
        jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
        return !(isInWater(i + ii, j, k) && isInWater(i + ii, j + jj, k) && isInWater(i, j + jj, k));
    }

    /*
     * Transforms the depth at specified x-y particle location into z coordinate
     *
     * @param xRho a double, the x-coordinate
     * @param yRho a double, the y-coordinate
     * @param depth a double, the depth of the particle
     * @return a double, the z-coordinate corresponding to the depth
     *
     * pverley pour chourdin: méthode à tester.
     */
    @Override
    public double depth2z(double x, double y, double depth) {

        if (depth > 0) {
            depth = 0.d;
        }
        depth = Math.abs(depth);
        //-----------------------------------------------
        // Return z[grid] corresponding to depth[meters]
        double zRho = 0.d;

        /* case particle is going straight up to surface, due to strong
         * buoyancy for instance.
         */
        if (depth < gdepT[nz - 1]) {
            //System.out.println("depth: " + depth + " ==> z: " + (nz - 1) + " gdepT[nz - 1]: " + gdepT[nz - 1]);
            return (nz - 1);
        }
        for (int k = nz - 1; k > 0; k--) {
            //System.out.println("t1 " + z_w[k] + " " + (float)depth + " " + z_rho[k]);
            if (depth <= gdepW[k] && depth > gdepT[k]) {
                zRho = k + 0.d - 0.5d * Math.abs((gdepT[k] - depth) / (gdepT[k] - gdepW[k]));
                //System.out.println("depth: " + depth + " ==> z: " + zRho + " - k: " + k + " gdepW[k]: " + gdepW[k] + " gdepT[k]: " + gdepT[k]);
                return zRho;
            }
            //System.out.println("t2 " + z_rho[k] + " " + (float)depth + " " + z_w[k + 1]);
            if (depth <= gdepT[k] && depth > gdepW[k + 1]) {
                zRho = k + 0.d
                        + 0.5d
                        * Math.abs((gdepT[k] - depth)
                                / (gdepW[k + 1] - gdepT[k]));
                //System.out.println("depth: " + depth + " ==> z: " + zRho + " - k: " + k + " gdepW[k + 1]: " + gdepW[k + 1] + " gdepT[k]: " + gdepT[k]);
                return zRho;
            }
        }
        //System.out.println("depth: " + depth + " ==> z: " + zRho);
        return zRho;
    }

    /*
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

        double depth;
        double dz;

        double kz = Math.max(0.d, Math.min(z, (double) nz - 1.00001f));
        int k = (int) Math.round(kz);
        dz = z - k;

        if (dz < 0) { // >= ?
            depth = gdepT[k]
                    + 2 * Math.abs(dz * (gdepT[k] - gdepW[k]));
            //System.out.println("z: " + z + " => depth: " + depth + " k: " + k + " gdepT[k]: " + gdepT[k] + " gdepW[k]: " + gdepW[k]) ;
        } else {
            depth = gdepT[k]
                    - 2 * Math.abs(dz * (gdepT[k] - gdepW[k + 1]));
            //System.out.println("z: " + z + " => depth: " + depth + " k: " + k + " gdepT[k]: " + gdepT[k] + " gdepW[k + 1]: " + gdepW[k + 1]);
        }
        return -depth;
    }

    /*
     * * Transforms the specified 2D grid coordinates into geographical
     * coordinates. It merely does a bilinear spatial interpolation of the
     * surrounding grid nods geographical coordinates.
     *
     * @param xRho a double, the x-coordinate
     * @param yRho a double, the y-coordinate
     * @return a double[], the corresponding geographical coordinates (latitude,
     * longitude)
     *
     * @param xRho double
     * @param yRho double
     * @return double[]
     */
    @Override
    public double[] xy2latlon(double xRho, double yRho) {

        //--------------------------------------------------------------------
        // Computational space (x, y , z) => Physical space (lat, lon, depth)
        final double ix = Math.max(0.00001f,
                Math.min(xRho, (double) nx - 1.00001f));
        final double jy = Math.max(0.00001f,
                Math.min(yRho, (double) ny - 1.00001f));

        final int i = (int) Math.floor(ix);
        final int j = (int) Math.floor(jy);
        double latitude = 0.d;
        double longitude = 0.d;
        final double dx = ix - (double) i;
        final double dy = jy - (double) j;
        double co;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                latitude += co * latRho[j + jj][i + ii];
                longitude += co * lonRho[j + jj][i + ii];
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

    /**
     * Determines whether the specified geographical point (lon, lat) belongs to
     * the is inside the polygon defined by (imin, jmin) & (imin, jmax) & (imax,
     * jmax) & (imax, jmin).
     *
     * <p>
     * The algorithm has been adapted from a function in ROMS/UCLA code,
     * originally written by Alexander F. Shchepetkin and Hernan G. Arango.
     * Please find below an extract of the ROMS/UCLA documention.
     * </p>
     * <pre>
     * Given the vectors Xb and Yb of size Nb, defining the coordinates
     * of a closed polygon,  this function find if the point (Xo,Yo) is
     * inside the polygon.  If the point  (Xo,Yo)  falls exactly on the
     * boundary of the polygon, it still considered inside.
     * This algorithm does not rely on the setting of  Xb(Nb)=Xb(1) and
     * Yb(Nb)=Yb(1).  Instead, it assumes that the last closing segment
     * is (Xb(Nb),Yb(Nb)) --> (Xb(1),Yb(1)).
     *
     * Reference:
     * Reid, C., 1969: A long way from Euclid. Oceanography EMR,
     * page 174.
     *
     * Algorithm:
     *
     * The decision whether the point is  inside or outside the polygon
     * is done by counting the number of crossings from the ray (Xo,Yo)
     * to (Xo,-infinity), hereafter called meridian, by the boundary of
     * the polygon.  In this counting procedure,  a crossing is counted
     * as +2 if the crossing happens from "left to right" or -2 if from
     * "right to left". If the counting adds up to zero, then the point
     * is outside.  Otherwise,  it is either inside or on the boundary.
     *
     * This routine is a modified version of the Reid (1969) algorithm,
     * where all crossings were counted as positive and the decision is
     * made  based on  whether the  number of crossings is even or odd.
     * This new algorithm may produce different results  in cases where
     * Xo accidentally coinsides with one of the (Xb(k),k=1:Nb) points.
     * In this case, the crossing is counted here as +1 or -1 depending
     * of the sign of (Xb(k+1)-Xb(k)).  Crossings  are  not  counted if
     * Xo=Xb(k)=Xb(k+1).  Therefore, if Xo=Xb(k0) and Yo>Yb(k0), and if
     * Xb(k0-1) < Xb(k0) < Xb(k0+1),  the crossing is counted twice but
     * with weight +1 (for segments with k=k0-1 and k=k0). Similarly if
     * Xb(k0-1) > Xb(k0) > Xb(k0+1), the crossing is counted twice with
     * weight -1 each time.  If,  on the other hand,  the meridian only
     * touches the boundary, that is, for example, Xb(k0-1) < Xb(k0)=Xo
     * and Xb(k0+1) < Xb(k0)=Xo, then the crossing is counted as +1 for
     * segment k=k0-1 and -1 for segment k=k0, resulting in no crossing.
     *
     * Note 1: (Explanation of the logical condition)
     *
     * Suppose  that there exist two points  (x1,y1)=(Xb(k),Yb(k))  and
     * (x2,y2)=(Xb(k+1),Yb(k+1)),  such that,  either (x1 < Xo < x2) or
     * (x1 > Xo > x2).  Therefore, meridian x=Xo intersects the segment
     * (x1,y1) -> (x2,x2) and the ordinate of the point of intersection
     * is:
     *                y1*(x2-Xo) + y2*(Xo-x1)
     *            y = -----------------------
     *                         x2-x1
     * The mathematical statement that point  (Xo,Yo)  either coinsides
     * with the point of intersection or lies to the north (Yo>=y) from
     * it is, therefore, equivalent to the statement:
     *
     *      Yo*(x2-x1) >= y1*(x2-Xo) + y2*(Xo-x1),   if   x2-x1 > 0
     * or
     *      Yo*(x2-x1) <= y1*(x2-Xo) + y2*(Xo-x1),   if   x2-x1 < 0
     *
     * which, after noting that  Yo*(x2-x1) = Yo*(x2-Xo + Xo-x1) may be
     * rewritten as:
     *
     *      (Yo-y1)*(x2-Xo) + (Yo-y2)*(Xo-x1) >= 0,   if   x2-x1 > 0
     * or
     *      (Yo-y1)*(x2-Xo) + (Yo-y2)*(Xo-x1) <= 0,   if   x2-x1 < 0
     *
     * and both versions can be merged into  essentially  the condition
     * that (Yo-y1)*(x2-Xo)+(Yo-y2)*(Xo-x1) has the same sign as x2-x1.
     * That is, the product of these two must be positive or zero.
     * </pre>
     *
     * @param imin an int, i-coordinate of the area left corners
     * @param imax an int, i-coordinate of the area right corners
     * @param jmin an int, j-coordinate of the area left corners
     * @param jmax an int, j-coordinate of the area right corners
     * @param lon a double, the longitude of the geographical point
     * @param lat a double, the latitude of the geographical point
     * @return <code>true</code> if (lon, lat) belongs to the polygon,
     * <code>false</code>otherwise.
     */
    private boolean isInsidePolygone(int imin, int imax, int jmin,
            int jmax, double lon, double lat) {

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
        file_hgr = checkExistenceAndUnicity(file, getParameter("hgr_pattern"));
        file_zgr = checkExistenceAndUnicity(file, getParameter("zgr_pattern"));

        isGridInfoInOneFile = (new File(file_mask).equals(new File(file_hgr)))
                && (new File(file_mask).equals(new File(file_zgr)));

        listUFiles = DatasetUtil.list(path, getParameter("gridu_pattern"));
        if (listUFiles.isEmpty()) {
            throw new IOException("{Dataset} " + path + " contains no file matching pattern " + getParameter("gridu_pattern"));
        }
        listVFiles = DatasetUtil.list(getParameter("input_path"), getParameter("gridv_pattern"));
        if (listVFiles.isEmpty()) {
            throw new IOException("{Dataset} " + path + " contains no file matching pattern " + getParameter("gridv_pattern"));
        }
        listTFiles = DatasetUtil.list(getParameter("input_path"), getParameter("gridt_pattern"));
        if (readW) {
            listWFiles = DatasetUtil.list(getParameter("input_path"), getParameter("gridw_pattern"));
            if (listWFiles.isEmpty()) {
                throw new IOException("{Dataset} " + path + " contains no file matching pattern " + getParameter("gridw_pattern"));
            }
        }
        if (!skipSorting()) {
            DatasetUtil.sort(listUFiles, strTime, time_arrow);
            DatasetUtil.sort(listVFiles, strTime, time_arrow);
            DatasetUtil.sort(listTFiles, strTime, time_arrow);
            if (readW) {
                DatasetUtil.sort(listWFiles, strTime, time_arrow);
            }
        }
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
        ncU = DatasetUtil.openFile(listUFiles.get(index), enhanced());
        if (ncV != null) {
            ncV.close();
        }
        if (listUFiles.get(index).equals(listVFiles.get(index))) {
            ncV = ncU;
        } else {
            ncV = DatasetUtil.openFile(listVFiles.get(index), enhanced());
        }
        if (readW) {
            if (listUFiles.get(index).equals(listWFiles.get(index))) {
                ncW = ncU;
            } else {
                ncW = DatasetUtil.openFile(listWFiles.get(index), enhanced());
            }
        }
        if (ncT != null) {
            ncT.close();
        }
        if (!listTFiles.isEmpty()) {
            if (listUFiles.get(index).equals(listTFiles.get(index))) {
                ncT = ncU;
            } else {
                ncT = DatasetUtil.openFile(listTFiles.get(index), enhanced());
            }
        }
        nbTimeRecords = ncU.findDimension(strTimeDim).getLength();
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
        return ((pGrid[0] > (nx - 3.0f))
                || (pGrid[0] < 2.0f)
                || (pGrid[1] > (ny - 3.0f))
                || (pGrid[1] < 2.0f));
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
        return nz;
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

        double bathy = 0.d;
        if (isInWater(i, j, nz - 1)) {
            for (int k = 0; k < nz; k++) {
                bathy += maskRho[k][j][i] * e3t[k][j][i];
                //System.out.println("k: " + k + " " + maskRho[k][j][i] + " " + bathy);
            }
            return bathy;
        }
        return Double.NaN;
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        w_tp0 = w_tp1;
        //wr_tp0 = wr_tp1;
        rank += time_arrow;

        if (rank > (nbTimeRecords - 1) || rank < 0) {
            open(indexFile = DatasetUtil.next(listUFiles, indexFile, time_arrow));
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
                shape = new int[]{1, nz, ny, nx};
                hasVerticalDim = true;
                break;
            case 2:
                origin = new int[]{jpo, ipo};
                shape = new int[]{ny, nx};
                break;
            case 3:
                if (!variable.isUnlimited()) {
                    origin = new int[]{0, jpo, ipo};
                    shape = new int[]{nz, ny, nx};
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
    public double xTore(double x) {
        return x;
    }

    @Override
    public double yTore(double y) {
        return y;
    }
}
