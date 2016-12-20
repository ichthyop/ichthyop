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
import java.util.Arrays;
import org.ichthyop.ui.LonLatConverter;
import org.ichthyop.ui.LonLatConverter.LonLatFormat;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public abstract class Hycom3dCommon extends AbstractDataset {

    double[] longitude;
    double[] latitude;
    double[] depthLevel;
    double[] ddepth, ddepthw;
    final int nt = 3;
    NetcdfTiledVariable[] u;
    NetcdfTiledVariable[] v;
    WTiledVariable[] w;
    int nx, ny, nz;
    int i0, j0;
    double[] dxu;
    double dyv;
    private double latMin, lonMin, latMax, lonMax;
    double dt_HyMo, time_tp1;
    int rank;
    float[][] bathymetry;
    //NetcdfFile nc;
    int nbTimeRecords;
    boolean xTore = true;
    final int tilingh = 100, tilingv = 3, tilinghw = 10;
    final private int p = 2;

    abstract void open() throws Exception;

    abstract NetcdfFile getNC();

    @Override
    void loadParameters() {
        // does nothing for now
    }

    @Override
    public void setUp() throws Exception {

        // Clear required variables
//        clearRequiredVariables();
        // Open NetCDF (abstract)
        open();

        // Read whole grid
        NetcdfFile nc = getNC();
        Array array;
        // Latitude
        String name = DatasetUtil.findVariable(nc, "latitude");
        if (null == name) {
            throw new IOException("Latitude variable not found in HYCOM dataset");
        }
        array = nc.findVariable(name).read().reduce();
        ny = array.getShape()[0];
        latitude = new double[ny];
        for (int j = 0; j < ny; j++) {
            latitude[j] = array.getDouble(j);
        }
        j0 = 0;
        // Longitude
        name = DatasetUtil.findVariable(nc, "longitude");
        if (null == name) {
            throw new IOException("Longitude variable not found in HYCOM dataset");
        }
        array = nc.findVariable(name).read().reduce();
        nx = array.getShape()[0];
        longitude = new double[nx];
        for (int i = 0; i < nx; i++) {
            longitude[i] = array.getDouble(i);
        }
        i0 = 0;
        // Depth
        name = DatasetUtil.findVariable(nc, "depth");
        if (null == name) {
            throw new IOException("Depth variable not found in HYCOM dataset");
        }
        array = nc.findVariable(name).read().reduce();
        nz = array.getShape()[0];
        depthLevel = new double[nz];
        for (int k = 0; k < nz; k++) {
            depthLevel[k] = array.getDouble(k);
        }
        nc.close();

        // Compute ddepth
        ddepth = new double[nz];
        ddepth[0] = 0.5 * Math.abs(depthLevel[0] - depthLevel[1]);
        ddepth[nz - 1] = 0.5 * Math.abs(depthLevel[nz - 2] - depthLevel[nz - 1]);
        for (int k = 1; k < nz - 1; k++) {
            ddepth[k] = 0.5 * Math.abs(depthLevel[k - 1] - depthLevel[k + 1]);
        }
        // Compute ddepthw
        ddepthw = new double[nz + 1];
        ddepthw[0] = 0.5 * Math.abs(depthLevel[0] - depthLevel[1]);
        ddepthw[nz] = 0.5 * Math.abs(depthLevel[nz - 2] - depthLevel[nz - 1]);
        for (int k = 1; k < nz; k++) {
            ddepthw[k] = Math.abs(depthLevel[k - 1] - depthLevel[k]);
        }

        // Crop the grid
        if (findParameter("shrink_domain") && Boolean.valueOf(getParameter("shrink_domain"))) {
            crop();
            longitude = Arrays.copyOfRange(longitude, i0, i0 + nx);
            latitude = Arrays.copyOfRange(latitude, j0, j0 + ny);
        }

        // Longitudinal toricity
        xTore = !findParameter("longitude_tore") || Boolean.valueOf(getParameter("longitude_tore"));

        // scale factors (assuming regular grid)
        dyv = 111138.d * (latitude[1] - latitude[0]);
        dxu = new double[ny];
        for (int j = 0; j < ny; j++) {
            dxu[j] = dyv * Math.cos(Math.PI * latitude[j] / 180.d);
        }

        // Domain geographical extension
        extent();

        u = new NetcdfTiledVariable[nt];
        v = new NetcdfTiledVariable[nt];
        w = new WTiledVariable[nt];
        // Initializes u[0] & v[0] for the mask
        u[0] = new NetcdfTiledVariable(getNC(), "eastward_sea_water_velocity", nx, ny, nz, i0, j0, 0, tilingh, tilingv);
        v[0] = new NetcdfTiledVariable(getNC(), "northward_sea_water_velocity", nx, ny, nz, i0, j0, 0, tilingh, tilingv);
    }

    @Override
    public double[] latlon2xy(double lat, double lon) {
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
                    dj = (int) Math.max(1, dj / 2);
                    found1 = false;
                }
            }
        }

        // Refine within cell (ci, cj) by linear interpolation
        int cip1 = xTore(ci + 1);
        int cim1 = xTore(ci - 1);
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

    @Override
    public double[] xy2latlon(double xRho, double yRho) {
        double jy = Math.max(0.00001f, Math.min(yRho, (double) ny - 1.00001f));

        int i = (int) Math.floor(xRho);
        int j = (int) Math.floor(jy);
        double lat = 0.d;
        double lon = 0.d;
        double dx = xRho - (double) i;
        double dy = jy - (double) j;
        double co;
        for (int ii = 0; ii < 2; ii++) {
            int ci = xTore(i);
            int cii = xTore(i + ii);
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

    @Override
    public double depth2z(double x, double y, double depth) {
        double z;
        int lk = 0;
        while ((lk < nz - 1) && (getDepth(x, y, lk) > depth)) {
            lk++;
        }
        if (lk == 0) {
            z = 0;
        } else {
            double pr = getDepth(x, y, lk);
            z = Math.max(0.d, (double) lk + (depth - pr) / (getDepth(x, y, lk + 1) - pr));
        }
        return (z);
    }

    @Override
    public double z2depth(double x, double y, double z) {
        double kz = Math.max(0.d, Math.min(z, (double) nz - 1.00001f));
        int k = (int) Math.floor(kz);
        double dz = kz - (double) k;

        double depth = (1.d - dz) * getDepth(x, y, k) + dz * getDepth(x, y, k + 1);
        return depth;
    }

    double getDepth(double xRho, double yRho, int k) {

//        final int i = (int) xRho;
//        final int j = (int) yRho;
//        double hh = 0.d;
//        final double dx = (xRho - i);
//        final double dy = (yRho - j);
//        for (int ii = 0; ii < 2; ii++) {
//            for (int jj = 0; jj < 2; jj++) {
//                if (isInWater(i + ii, j + jj)) {
//                    double co = Math.abs((1 - ii - dx) * (1 - jj - dy));
//                    double z_r = depth[k]
//                            + (double) ssh_tp0[j + jj][i + ii] * (1.d + depth[k]/bathy[nz-1]);
//                    hh += co * z_r;
//                }
//            }
//        }
//        return (hh);
        return -depthLevel[k];
    }

    @Override
    public double getDepthMax(double x, double y) {

        if (isInWater((int) Math.round(x), (int) Math.round(y), 0)) {
            for (int k = 0; k < nz; k++) {
                int ix = (int) Math.round(x);
                int jy = (int) Math.round(y);
                if (!isInWater(ix, jy, k)) {
                    return getDepth(x, y, Math.max(0, k - 1));
                }
            }
        }
        return 0.d;
    }

    private double weight(double[] xyz, int[] ijk, int p) {
        double distance = 0.d;
        for (int n = 0; n < xyz.length; n++) {
            distance += Math.abs(Math.pow(xyz[n] - ijk[n], p));
        }
        return 1.d / distance;
    }

    @Override
    public double get_dUx(double[] pGrid, double time) {
        double du = 0.d;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        double kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double dt = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        int k = (int) kz;
        double CO = 0.d;

        if (Double.isInfinite(weight(pGrid, new int[]{i, j, k}, p))) {
            // pGrid falls on a grid point
            CO = 1.d;
            i = xTore(i);
            if (!(Double.isNaN(u[0].getDouble(i, j, k)) || Double.isNaN(u[1].getDouble(i, j, k)))) {
                du = (1.d - dt) * u[0].getDouble(i, j, k) + dt * u[1].getDouble(i, j, k);
            }
        } else {
            for (int ii = 0; ii < n; ii++) {
                for (int jj = 0; jj < n; jj++) {
                    for (int kk = 0; kk < 2; kk++) {
                        int ci = Math.max(xTore(i + ii), 0);
                        double co = weight(pGrid, new int[]{i + ii, j + jj, k + kk}, p);
                        CO += co;
                        if (!(Double.isNaN(u[0].getDouble(ci, j + jj, k + kk)) || Double.isNaN(u[1].getDouble(ci, j + jj, k + kk)))) {
                            double x = (1.d - dt) * u[0].getDouble(ci, j + jj, k + kk) + dt * u[1].getDouble(ci, j + jj, k + kk);
                            du += x * co;
                        }
                    }
                }
            }
        }
        if (CO != 0) {
            du /= (CO * dxu[(int) Math.round(pGrid[1])]);
        }
        return du;
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {
        double dv = 0.d;

        int n = isCloseToCost(pGrid) ? 1 : 2;
        double kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        double dt = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        int k = (int) kz;
        double CO = 0.d;

        if (Double.isInfinite(weight(pGrid, new int[]{i, j, k}, p))) {
            // pGrid falls on a grid point
            CO = 1.d;
            i = xTore(i);
            if (!(Double.isNaN(v[0].getDouble(i, j, k)) || Double.isNaN(v[1].getDouble(i, j, k)))) {
                dv = (1.d - dt) * v[0].getDouble(i, j, k) + dt * v[1].getDouble(i, j, k);
            }
        } else {
            for (int jj = 0; jj < n; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    for (int kk = 0; kk < 2; kk++) {
                        int ci = xTore(i + ii);
                        double co = weight(pGrid, new int[]{i + ii, j + jj, k + kk}, p);
                        CO += co;
                        if (!(Double.isNaN(v[0].getDouble(ci, j + jj, k + kk)) || Double.isNaN(v[1].getDouble(ci, j + jj, k + kk)))) {
                            double x = (1.d - dt) * v[0].getDouble(ci, j + jj, k + kk) + dt * v[1].getDouble(ci, j + jj, k + kk);
                            dv += x * co;
                        }
                    }
                }
            }
        }
        if (CO != 0) {
            dv /= (CO * dyv);
        }
        return dv;
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {
        double dw = 0.d;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        double kz = Math.max(0.d, Math.min(pGrid[2], nz - 1.00001f));

        // Time fraction
        double dt = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;

        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        int k = (int) kz;
        double CO = 0.d;

        if (Double.isInfinite(weight(pGrid, new int[]{i, j, k}, p))) {
            // pGrid falls on a grid point
            CO = 1.d;
            i = xTore(i);
            if (!(Double.isNaN(w[0].getDouble(i, j, k)) || Double.isNaN(w[1].getDouble(i, j, k)))) {
                dw = (1.d - dt) * w[0].getDouble(i, j, k) + dt * w[1].getDouble(i, j, k);
            }
        } else {
            for (int ii = 0; ii < n; ii++) {
                for (int jj = 0; jj < n; jj++) {
                    for (int kk = 0; kk < 2; kk++) {
                        double co = weight(pGrid, new int[]{i + ii, j + jj, k + kk}, p);
                        CO += co;
                        if (isInWater(i + ii, j + jj)) {
                            double x = (1.d - dt) * w[0].getDouble(i + ii, j + jj, k + kk) + dt * w[1].getDouble(i + ii, j + jj, k + kk);
                            dw += 2.d * x * co;
                        }
                    }
                }
            }
        }
        if (CO != 0) {
            dw /= (CO * ddepthw[(int) Math.round(pGrid[2])]);
        }
        return dw;
    }

    @Override
    public boolean isInWater(double[] pGrid) {
        if (pGrid.length > 2) {
            return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]), (int) Math.round(pGrid[2]));
        } else {
            return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
        }
    }

    private boolean isInWater(int i, int j, int k) {
        int ci = xTore(i);
        return !Double.isNaN(u[0].getDouble(ci, j, k)) && !Double.isNaN(v[0].getDouble(ci, j, k));
    }

    @Override
    public boolean isInWater(int i, int j) {
        return isInWater(i, j, 0);
    }

    @Override
    public boolean isCloseToCost(double[] pGrid) {
        int i, j, ii, jj;
        i = (int) (Math.round(pGrid[0]));
        j = (int) (Math.round(pGrid[1]));
        ii = (i - (int) pGrid[0]) == 0 ? 1 : -1;
        jj = (j - (int) pGrid[1]) == 0 ? 1 : -1;
        int ci = xTore(i + ii);
        return !(isInWater(ci, j) && isInWater(ci, j + jj) && isInWater(i, j + jj));
    }

    @Override
    public boolean isOnEdge(double[] pGrid) {

        return (!xTore && (pGrid[0] > (nx - 2.d)) || (!xTore && (pGrid[0] < 1.d))
                || (pGrid[1] > (ny - 2.d)) || (pGrid[1] < 1.d));
    }

    void extent() {

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
        //getLogger().log(Level.INFO, "lonmin {0} lonmax {1} latmin {2} latmax {3}", new Object[]{lonMin, lonMax, latMin, latMax});
    }

    @Override
    public double getBathy(int i, int j) {
        return -1;//-1*bathymetry[j][i];
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
        return dxu[j];
    }

    @Override
    public double getdeta(int j, int i) {
        return dyv;
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
    public double getLon(int i, int j) {
        return longitude[i];
    }

    @Override
    public double getLat(int i, int j) {
        return latitude[j];
    }

    @Override
    public double getDepthMax() {
        return -6000.d;
    }

    @Override
    public boolean is3D() {
        return true;
    }

    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        Variable variable = nc.findVariable(name);
        int[] origin = null, shape = null;
        switch (variable.getShape().length) {
            case 4:
                origin = new int[]{rank, 0, j0, i0};
                shape = new int[]{1, nz, ny, nx};
                break;
            case 2:
                origin = new int[]{j0, i0};
                shape = new int[]{ny, nx};
                break;
            case 3:
                if (!variable.isUnlimited()) {
                    origin = new int[]{0, j0, i0};
                    shape = new int[]{nz, ny, nx};

                } else {
                    origin = new int[]{rank, j0, i0};
                    shape = new int[]{1, ny, nx};
                }
                break;
        }

        return variable.read(origin, shape).reduce();
    }

    private int xTore(int i) {
        if (xTore) {
            if (i < 0) {
                return nx + i;
            }
            if (i > nx - 1) {
                return i - nx;
            }
        }
        return i;
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

    void crop() throws IOException {

        float lon1 = Float.valueOf(LonLatConverter.convert(getParameter("north-west-corner.lon"), LonLatFormat.DecimalDeg));
        float lat1 = Float.valueOf(LonLatConverter.convert(getParameter("north-west-corner.lat"), LonLatFormat.DecimalDeg));
        float lon2 = Float.valueOf(LonLatConverter.convert(getParameter("south-east-corner.lon"), LonLatFormat.DecimalDeg));
        float lat2 = Float.valueOf(LonLatConverter.convert(getParameter("south-east-corner.lat"), LonLatFormat.DecimalDeg));

        double[] pGrid1, pGrid2;
        int ipn, jpn;

        pGrid1 = latlon2xy(lat1, lon1);
        pGrid2 = latlon2xy(lat2, lon2);
        if (pGrid1[0] < 0 || pGrid2[0] < 0) {
            throw new IOException("Impossible to proportion the simulation area : points out of domain");
        }

        //System.out.println((float)pGrid1[0] + " " + (float)pGrid1[1] + " " + (float)pGrid2[0] + " " + (float)pGrid2[1]);
        i0 = (int) Math.min(Math.floor(pGrid1[0]), Math.floor(pGrid2[0]));
        ipn = (int) Math.max(Math.ceil(pGrid1[0]), Math.ceil(pGrid2[0]));
        j0 = (int) Math.min(Math.floor(pGrid1[1]), Math.floor(pGrid2[1]));
        jpn = (int) Math.max(Math.ceil(pGrid1[1]), Math.ceil(pGrid2[1]));

        nx = Math.min(nx, ipn - i0 + 1);
        ny = Math.min(ny, jpn - j0 + 1);
        //System.out.println("i0 " + i0 + " nx " + nx + " j0 " + j0 + " ny " + ny);
    }

    public class WTiledVariable extends AbstractTiledVariable {

        private final NetcdfTiledVariable uw;
        private final NetcdfTiledVariable vw;

        WTiledVariable(NetcdfFile nc, int nx, int ny, int nz, int i0, int j0, int nh, int rank) throws IOException {
            super(nx, ny, nz, 1, nz);
            uw = new NetcdfTiledVariable(nc, "eastward_sea_water_velocity", nx, ny, nz, i0, j0, rank, nh, nz);
            vw = new NetcdfTiledVariable(nc, "northward_sea_water_velocity", nx, ny, nz, i0, j0, rank, nh, nz);
        }

        @Override
        Array loadTile(int tag) {

            int j = tag / nx;
            int i = tag % nx;

            double[][] Huon = new double[nz][2];
            double[][] Hvom = new double[nz][2];

            int ci = i, cim1 = i - 1;
            if (i == 0) {
                ci = xTore ? i : i + 1;
                cim1 = xTore ? nx - 1 : i;
            }
            int cj = (j == 0) ? j + 1 : j;
            int cjm1 = (j == 0) ? j : j - 1;

            for (int k = nz; k-- > 0;) {
                Huon[k][1] = Double.isNaN(uw.getDouble(ci, cj, k))
                        ? 0.d
                        : uw.getDouble(ci, cj, k) * dyv * ddepth[k];
                Huon[k][0] = Double.isNaN(uw.getDouble(cim1, cj, k))
                        ? 0.d
                        : uw.getDouble(cim1, cj, k) * dyv * ddepth[k];

                Hvom[k][1] = Double.isNaN(vw.getDouble(ci, cj, k))
                        ? 0.d
                        : vw.getDouble(ci, cj, k) * dxu[j] * ddepth[k];
                Hvom[k][0] = Double.isNaN(vw.getDouble(ci, cjm1, k))
                        ? 0.d
                        : vw.getDouble(ci, cjm1, k) * dxu[cjm1] * ddepth[k];
            }

            // Find k0, index of the deepest cell in water
            int k0 = nz - 1;
            for (int k = nz - 1; k > 0; k--) {
                if (!Double.isNaN(uw.getDouble(ci, cj, k)) && !Double.isNaN(vw.getDouble(ci, cj, k))) {
                    k0 = k;
                    break;
                }
            }

            Array w = new ArrayDouble.D1(nz + 1);
            for (int k = nz; k > k0; k--) {
                w.setDouble(k, 0.d);
            }
            for (int k = k0; k > 0; k--) {
                double wtmp = w.getDouble(k + 1) - (Huon[k][1] - Huon[k][0] + Hvom[k][1] - Hvom[k][0]);
                wtmp /= (dyv * dxu[cj]);
                w.setDouble(k, wtmp);
            }
            w.setDouble(0, 0.d);

            return w;
        }

        @Override
        void closeSource() {
            uw.clear();
            vw.clear();
        }
    }
}
