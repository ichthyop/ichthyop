/*
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2017
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
package org.ichthyop.grid;

import java.util.HashMap;
import java.util.List;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.dataset.DatasetUtil;

/**
 *
 * @author pverley
 */
public abstract class AbstractRegularGrid extends IchthyopLinker implements IGrid {

    final String grid_prefix;
    private final String dataset_prefix;
    private double latmin, latmax, lonmin, lonmax;
    private boolean continuity;
    final HashMap<String, List<String>> variables = new HashMap();
    boolean enhanced;
    private boolean z0AtSurface;

    abstract void makeGrid();

    public AbstractRegularGrid(String dataset_prefix) {

        this.dataset_prefix = dataset_prefix;
        this.grid_prefix = dataset_prefix + ".grid";
    }

    public void init() {

        // list variables
        String location = getConfiguration().isNull(grid_prefix + ".location")
                ? getConfiguration().getString(dataset_prefix + ".location")
                : getConfiguration().getString(grid_prefix + ".location");
        variables.putAll(DatasetUtil.mapVariables(grid_prefix, location, false));
        variables.putAll(DatasetUtil.mapVariables(grid_prefix, location, true));

        // x and y periodicity
        continuity = getConfiguration().getBoolean(grid_prefix + ".continuity");

        // enhanced dataset
        enhanced = !getConfiguration().isNull(dataset_prefix + ".enhanced_mode")
                ? getConfiguration().getBoolean(dataset_prefix + ".enhanced_mode")
                : true;

        // make grid
        makeGrid();

        // check whether z=0 is at surface or ocean floor
        z0AtSurface = false;
        if (get_nz() < 2) {
            z0AtSurface = true;
        } else {
            int i = 0, j = 0;
            while (!isInWater(i, j, get_nz() / 2)) {
                i = (int) (Math.random() * get_nx());
                j = (int) (Math.random() * get_ny());
            }
            if (getDepth(i, j, 0) > getDepth(i, j, 1)) {
                z0AtSurface = true;
            }
        }
        
        // compute grid extent
        extent();
    }

    public void extent() {

        lonmin = Double.MAX_VALUE;
        lonmax = -Double.MAX_VALUE;
        latmin = Double.MAX_VALUE;
        latmax = -Double.MAX_VALUE;

        for (int i = 0; i < get_nx(); i++) {
            for (int j = 0; j < get_ny(); j++) {
                if (getLon(i, j) >= lonmax) {
                    lonmax = getLon(i, j);
                }
                if (getLon(i, j) <= lonmin) {
                    lonmin = getLon(i, j);
                }
                if (getLat(i, j) >= latmax) {
                    latmax = getLat(i, j);
                }
                if (getLat(i, j) <= latmin) {
                    latmin = getLat(i, j);
                }
            }
        }

        double double_tmp;
        if (lonmin > lonmax) {
            double_tmp = lonmin;
            lonmin = lonmax;
            lonmax = double_tmp;
        }

        if (latmin > latmax) {
            double_tmp = latmin;
            latmin = latmax;
            latmax = double_tmp;
        }
    }

    @Override
    public double[] xy2latlon(double xRho, double yRho) {

        //--------------------------------------------------------------------
        // Computational space (x, y , z) => Physical space (lat, lon, depth)
        double jy = Math.max(0.00001d, Math.min(yRho, (double) get_ny() - 1.00001d));
        double ix = continuity ? xRho : Math.max(0.00001d, Math.min(xRho, (double) get_nx() - 1.00001d));

        final int i = (int) continuity(Math.floor(ix));
        final int j = (int) Math.floor(jy);
        double latitude = 0.d;
        double longitude = 0.d;
        final double dx = ix - (double) i;
        final double dy = jy - (double) j;
        double co;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < 2; jj++) {
                co = Math.abs((1 - ii - dx) * (1 - jj - dy));
                int cii = continuity(i + ii);
                latitude += co * getLat(cii, j + jj);
                longitude += co * getLon(cii, j + jj);
            }
        }
        return (new double[]{latitude, longitude});
    }

    @Override
    public double[] latlon2xy(double lat, double lon) {
        boolean found1 = false;
        boolean found2 = false;

        int ci = (int) Math.round(0.5 * get_nx());
        int cj = (int) Math.round(0.5 * get_ny());

        int di = (int) Math.ceil(0.5 * ci);
        int dj = (int) Math.ceil(0.5 * cj);

        // Find the closet grid point to {lat, lon}
        while (!(found1 && found2)) {
            int i = ci;
            int j = cj;
            double dmin = DatasetUtil.geodesicDistance(lat, lon, getLat(i, j), getLon(i, j));
            for (int ii = -di; ii <= di; ii += di) {
                if ((i + ii >= 0) && (i + ii < get_nx())) {
                    double d = DatasetUtil.geodesicDistance(lat, lon, getLat(i + ii, j), getLon(i + ii, j));
                    if (d < dmin) {
                        dmin = d;
                        ci = i + ii;
                        cj = j;
                    }
                }
            }
            for (int jj = -dj; jj <= dj; jj += dj) {
                if ((j + jj >= 0) && (j + jj < get_ny())) {
                    double d = DatasetUtil.geodesicDistance(lat, lon, getLat(i, j + jj), getLon(i, j + jj));
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
                    di = (int) Math.max(1, Math.ceil(0.5 * di));
                    dj = (int) Math.max(1, Math.ceil(0.5 * dj));
                    found1 = false;
                }
            }
        }

        if (getLat(ci, cj) == lat && getLon(ci, cj) == lon) {
            return new double[]{ci, cj};
        }

        // Refine within cell (ci, cj) by linear interpolation
        int cip1 = continuity ? continuity(ci + 1) : (ci + 1 > get_nx() - 1 ? get_nx() - 1 : ci + 1);
        int cim1 = continuity ? continuity(ci - 1) : (ci - 1 < 0 ? 0 : ci - 1);
        int cjp1 = cj + 1 > get_ny() - 1 ? get_ny() - 1 : cj + 1;
        int cjm1 = cj - 1 < 0 ? 0 : cj - 1;
        int imin = 0, jmin = 0;
        if (isInside(lat, lon,
                new double[]{
                    getLat(cim1, cjm1), getLat(cim1, cj), getLat(ci, cj), getLat(ci, cjm1), getLat(cim1, cjm1)},
                new double[]{
                    getLon(cim1, cjm1), getLon(cim1, cj), getLon(ci, cj), getLon(ci, cjm1), getLon(cim1, cjm1)})) {
            imin = cim1;
            jmin = cjm1;
        } else if (isInside(lat, lon,
                new double[]{
                    getLat(cim1, cj), getLat(cim1, cjp1), getLat(ci, cjp1), getLat(ci, cj), getLat(cim1, cj)},
                new double[]{
                    getLon(cim1, cj), getLon(cim1, cjp1), getLon(ci, cjp1), getLon(ci, cj), getLon(cim1, cj)})) {
            imin = cim1;
            jmin = cj;
        } else if (isInside(lat, lon,
                new double[]{
                    getLat(ci, cj), getLat(ci, cjp1), getLat(cip1, cjp1), getLat(cip1, cj), getLat(ci, cj)},
                new double[]{
                    getLon(ci, cj), getLon(ci, cjp1), getLon(cip1, cjp1), getLon(cip1, cj), getLon(ci, cj)})) {
            imin = ci;
            jmin = cj;
        } else if (isInside(lat, lon,
                new double[]{
                    getLat(ci, cjm1), getLat(ci, cj), getLat(cip1, cj), getLat(cip1, cjm1), getLat(ci, cjm1)},
                new double[]{
                    getLon(ci, cjm1), getLon(ci, cj), getLon(cip1, cj), getLon(cip1, cjm1), getLon(ci, cjm1)})) {
            imin = ci;
            jmin = cjm1;
        } else {
            warning("[grid] " + grid_prefix + " Failed to convert lat " + (float) lat + " lon " + (float) lon + " into x y coordinates");
//            System.out.println("  lat " + (float) lat + " lon " + (float) lon);
//            System.out.println("  nx " + get_nx() + " cim1 " + cim1 + " ci " + ci + " cip1 " + cip1);
//            System.out.println("  lon cim1 " + getLon(cim1, cj) + " ci " + getLon(ci, cj) + " cip1 " + getLon(cip1, cj));
//            System.out.println("  ny " + get_ny() + " cjm1 " + cjm1 + " cj " + cj + " cjp1 " + cjp1);
//            System.out.println("  lat cjm1 " + getLat(ci, cjm1) + " lat cj " + getLat(ci, cj) + " lat cjp1 " + getLat(ci, cjp1));
            return null;
        }

        // trilinear interpolation
        double dy1 = getLat(imin, jmin + 1) - getLat(imin, jmin);
        double dx1 = getLon(imin, jmin + 1) - getLon(imin, jmin);
        double dy2 = getLat(continuity(imin + 1), jmin) - getLat(imin, jmin);
        double dx2 = getLon(continuity(imin + 1), jmin) - getLon(imin, jmin);

        // xgrid
        double c1 = lon * dy1 - lat * dx1;
        double c2 = getLon(imin, jmin) * dy2 - getLat(imin, jmin) * dx2;
        double deltax = (c1 * dx2 - c2 * dx1) / (dx2 * dy1 - dy2 * dx1);
        deltax = (deltax - getLon(imin, jmin)) / dx2;
        double xgrid = imin + bound(deltax);

        // ygrid
        c1 = getLon(imin, jmin) * dy1 - getLat(imin, jmin) * dx1;
        c2 = lon * dy2 - lat * dx2;
        double deltay = (c1 * dy2 - c2 * dy1) / (dx2 * dy1 - dy2 * dx1);
        deltay = (deltay - getLat(imin, jmin)) / dy1;
        double ygrid = jmin + bound(deltay);

        return (new double[]{xgrid, ygrid});
    }

    private double bound(double x) {
        return Math.max(Math.min(1.d, x), 0.d);
    }

    double validLon(double lon) {
        return lon > 180 ? lon - 360.d : lon;
    }

    /*
    https://stackoverflow.com/questions/12083093/how-to-define-if-a-determinate-point-is-inside-a-region-lat-long
     */
    private boolean isInside(double lat0, double lon0, double[] lat, double[] lon) {
        int i, j;
        boolean inside = false;
        int sides = lat.length;
        for (i = 0, j = sides - 1; i < sides; j = i++) {
            //verifying if your coordinate is inside your region
            double dxi0 = substract(lon0, lon[i]);
            double dxj0 = substract(lon0, lon[j]);
            double dxji = substract(lon[j], lon[i]);
            if ((((dxi0 >= 0) && (dxj0 < 0)) || ((dxj0 >= 0) && (dxi0 < 0)))
                    && (lat0 < ((lat[j] - lat[i]) * dxi0 / dxji + lat[i]))) {
                inside = !inside;
            }
        }
        return inside;
    }

    private double substract(double lon1, double lon2) {
        double dx;
        if ((lon1 - lon2) > 180.d) {
            dx = lon1 - lon2 - 360.d;
        } else if ((lon1 - lon2) < -180.d) {
            dx = 360.d + lon1 - lon2;
        } else {
            dx = lon1 - lon2;
        }
        return dx;
    }

    @Override
    public double depth2z(double x, double y, double depth) {

        //System.out.println((float) x+ " " + (float) y + " " + (float) depth);
        // no vertical dimension
        if (get_nz() < 2) {
            return 0.d;
        }

        // handle particular cases for depth outside water column
        if (z0AtSurface) {
            if (depth >= getDepth(x, y, 0)) {
                return 0.d;
            }
            if (depth <= getDepth(x, y, get_nz() - 1)) {
                return (double) (get_nz() - 1);
            }
        } else {
            if (depth >= getDepth(x, y, get_nz() - 1)) {
                return (double) (get_nz() - 1);
            }
            if (depth <= getDepth(x, y, 0)) {
                return 0.d;
            }
        }

        // general case
        int lk;
        for (lk = 0; lk < get_nz() - 1; lk++) {
            double depthk = getDepth(x, y, lk);
            double depthkp1 = getDepth(x, y, lk + 1);
            if (Math.abs(depthk - depth) + Math.abs(depthkp1 - depth) <= Math.abs(depthk - depthkp1)) {
                break;
            }
        }
        double depthlk = getDepth(x, y, lk);
        //System.out.println(depthlk + " " + lk + " " + get_nz() + " " + grid_prefix);
        return Math.max(0.d, (double) lk + (depth - depthlk) / (getDepth(x, y, lk + 1) - depthlk));
    }

    @Override
    public double z2depth(double x, double y, double z) {

        double kz = Math.max(0.d, Math.min(z, (double) get_nz() - 1.00001f));
        int k = (int) Math.floor(kz);
        double dz = kz - (double) k;

        double depth = (1.d - dz) * getDepth(x, y, k) + dz * getDepth(x, y, k + 1);
        return depth;
    }

    @Override
    public double getDepthMax(double x, double y) {
        return getDepth(x, y, getDeepestLevel(x, y));
    }

    private int getDeepestLevel(double x, double y) {
        if (isInWater(new double[]{x, y, 0})) {
            for (int k = 0; k < get_nz(); k++) {
                if (!isInWater(new double[]{x, y, k})) {
                    return Math.max(0, k - 1);
                }
            }
            return get_nz() - 1;
        }
        return 0;
    }

    @Override
    public boolean isInWater(double[] pGrid) {
        if (pGrid.length > 2) {
            return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]), (int) Math.round(pGrid[2]));
        } else {
            return isInWater((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
        }
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
        int ci = continuity(i + ii);
        return !(isInWater(ci, j) && isInWater(ci, j + jj) && isInWater(i, j + jj));
    }

    @Override
    public boolean isOnEdge(double[] pGrid) {

        return (!continuity && (pGrid[0] > (get_nx() - 2.d)))
                || (!continuity && (pGrid[0] < 1.d))
                || (pGrid[1] > (get_ny() - 2.d))
                || (pGrid[1] < 1.d);
    }

    @Override
    public double getLatMin() {
        return latmin;
    }

    @Override
    public double getLatMax() {
        return latmax;
    }

    @Override
    public double getLonMin() {
        return lonmin;
    }

    @Override
    public double getLonMax() {
        return lonmax;
    }

    @Override
    public int continuity(int i) {
        if (continuity) {
            if (i < 0) {
                return get_nx() + i;
            }
            if (i > get_nx() - 1) {
                return i - get_nx();
            }
        }
        return i;
    }

    @Override
    public double continuity(double x) {
        if (continuity) {
            if (x < -0.5d) {
                return get_nx() + x;
            }
            if (x > get_nx() - 0.5d) {
                return x - get_nx();
            }
        }
        return x;
    }

    @Override
    public boolean continuity() {
        return continuity;
    }
}
