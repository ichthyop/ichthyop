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

import org.ichthyop.event.NextStepEvent;
import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public abstract class Hycom3dCommon extends AbstractDataset {

    double[] longitude;
    double[] latitude;
    double[] depth;
    double[][] ssh_tp0, ssh_tp1;
    double[][] u_tp0, u_tp1;
    double[][] v_tp0, v_tp1;
    double[][] w_tp0, w_tp1;
    int nx, ny, nz;

    @Override
    public void setUp() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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

    @Override
    public double depth2z(double x, double y, double depth) {
        double z;
        int lk = nz - 1;
        while ((lk > 0) && (getDepth(x, y, lk) > depth)) {
            lk--;
        }
        if (lk == (nz - 1)) {
            z = (double) lk;
        } else {
            double pr = getDepth(x, y, lk);
            z = Math.max(0.d, (double) lk + (depth - pr) / (getDepth(x, y, lk + 1) - pr));
        }
        return (z);
    }

    @Override
    public double z2depth(double x, double y, double z) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
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
        return depth[k];
    }

    @Override
    public double get_dUx(double[] pGrid, double time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isInWater(double[] pGrid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isInWater(int i, int j) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isCloseToCost(double[] pGrid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isOnEdge(double[] pGrid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getBathy(int i, int j) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_nx() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_ny() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int get_nz() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getdxi(int j, int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getdeta(int j, int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void init() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getLatMin() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getLatMax() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getLonMin() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getLonMax() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getLon(int igrid, int jgrid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getLat(int igrid, int jgrid) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getDepthMax() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean is3D() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double xTore(double x) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double yTore(double y) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
