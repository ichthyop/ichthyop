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

import java.io.IOException;
import java.util.Arrays;
import org.ichthyop.dataset.DatasetUtil;
import org.ichthyop.dataset.variable.TiledVariable;
import org.ichthyop.ui.LonLatConverter;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public class RectilinearGrid extends AbstractRegularGrid {

    private String varlon;
    private String varlat;
    private String vardepth;
    private String varmask;
    private double[] longitude;
    private double[] latitude;
    private double[] depthLevel;
    private double[] ddepth;
    private int nx, ny, nz;
    private int i0, j0;
    private double[] dx;
    private double dy;
    private TiledVariable mask;

    public RectilinearGrid(String prefix) {
        super(prefix);
    }

    @Override
    void makeGrid() {

        // grid file
        try {
            // latitude
            varlat = getConfiguration().isNull(grid_prefix + ".variable.latitude")
                    ? "latitude"
                    : getConfiguration().getString(grid_prefix + ".variable.latitude");

            if (!variables.containsKey(varlat)) {
                throw new IOException("Latitude variable not found in dataset " + grid_prefix);
            }
            String location = variables.get(varlat).get(0);
            NetcdfFile nc = DatasetUtil.open(location, enhanced);
            varlat = DatasetUtil.findVariable(nc, varlat);
            Array array = nc.findVariable(varlat).read().reduce();
            nc.close();
            int[] shape = array.getShape();
            ny = shape[0];
            latitude = new double[ny];
            Index index = array.getIndex();
            for (int j = 0; j < ny; j++) {
                if (shape.length > 1) {
                    index.set(j, 0);
                } else {
                    index.set(j);
                }
                latitude[j] = array.getDouble(index);
            }
            j0 = 0;

            // longitude
            varlon = getConfiguration().isNull(grid_prefix + ".variable.longitude")
                    ? "longitude"
                    : getConfiguration().getString(grid_prefix + ".variable.longitude");
            if (!variables.containsKey(varlon)) {
                throw new IOException("Longitude variable not found in dataset " + grid_prefix);
            }
            location = variables.get(varlon).get(0);
            nc = DatasetUtil.open(location, enhanced);
            varlon = DatasetUtil.findVariable(nc, varlon);
            array = nc.findVariable(varlon).read().reduce();
            nc.close();
            shape = array.getShape();
            nx = shape.length > 1 ? shape[1] : shape[0];
            longitude = new double[nx];
            for (int i = 0; i < nx; i++) {
                longitude[i] = validLon(array.getDouble(i));
            }
            i0 = 0;

            if (getConfiguration().getBoolean(grid_prefix + ".shrink")) {
                crop();
                longitude = Arrays.copyOfRange(longitude, i0, i0 + nx);
                latitude = Arrays.copyOfRange(latitude, j0, j0 + ny);
            }

            // depth
            vardepth = getConfiguration().isNull(grid_prefix + ".variable.depth")
                    ? "depth"
                    : getConfiguration().getString(grid_prefix + ".variable.depth");
            if (variables.containsKey(vardepth)) {
                location = variables.get(vardepth).get(0);
                nc = DatasetUtil.open(location, enhanced);
                vardepth = DatasetUtil.findVariable(nc, vardepth);
                array = nc.findVariable(vardepth).read().reduce();
                nc.close();
                nz = array.getShape().length > 0 ? array.getShape()[0] : 1;
                depthLevel = new double[nz];
                for (int k = 0; k < nz; k++) {
                    depthLevel[k] = array.getDouble(k);
                }
            } else {
                warning("[grid] Did not find depth variable in dataset " + grid_prefix + ". Ichthyop assumes the grid is 2D.");
                nz = 1;
                depthLevel = new double[]{0};
            }

            // scale factors
            dy = 111138.d * (latitude[1] - latitude[0]);
            dx = new double[ny];
            for (int j = 0; j < ny; j++) {
                dx[j] = dy * Math.cos(Math.PI * latitude[j] / 180.d);
            }
            ddepth = new double[nz];
            if (nz > 1) {
                ddepth[0] = 0.5 * Math.abs(depthLevel[0] - depthLevel[1]);
                ddepth[nz - 1] = 0.5 * Math.abs(depthLevel[nz - 2] - depthLevel[nz - 1]);
                for (int k = 1; k < nz - 1; k++) {
                    ddepth[k] = 0.5 * Math.abs(depthLevel[k - 1] - depthLevel[k + 1]);
                }
            } else {
                ddepth[0] = 1;
            }

            // mask
            varmask = getConfiguration().isNull(grid_prefix + ".variable.mask")
                    ? "mask"
                    : getConfiguration().getString(grid_prefix + ".variable.mask");
            // assume that the mask can be extracted from any 3D variable
            if (!variables.containsKey(varmask)) {
                for (String name : variables.keySet()) {
                    location = variables.get(name).get(0);
                    nc = DatasetUtil.open(location, enhanced);
                    name = DatasetUtil.findVariable(nc, name);
                    Variable variable = nc.findVariable(name);
                    nc.close();
                    if ((variable.isUnlimited() && variable.getShape().length == 4)
                            || (!variable.isUnlimited() && variable.getShape().length == 3)
                            || (!variable.isUnlimited() && variable.getShape().length == 2) && nz == 1) {
                        varmask = variable.getFullName().toLowerCase();
                        break;
                    }
                }
            }
            if (null == varmask) {
                throw new IOException("Did not find suitable mask variable in grid file. Please specify parameter " + grid_prefix + ".variable.mask");
            }
            location = variables.get(varmask).get(0);
            mask = new TiledVariable(DatasetUtil.open(location, true), varmask, this, 0, 0, 10, Math.min(3, nz));

        } catch (IOException ex) {
            error("[grid] Failed to make grid " + grid_prefix, ex);
        }

    }

    private void crop() {

        double lon1 = validLon(Float.valueOf(LonLatConverter.convert(getConfiguration().getString(grid_prefix + ".north-west-corner.lon"), LonLatConverter.LonLatFormat.DecimalDeg)));
        double lat1 = Float.valueOf(LonLatConverter.convert(getConfiguration().getString(grid_prefix + ".north-west-corner.lat"), LonLatConverter.LonLatFormat.DecimalDeg));
        double lon2 = validLon(Float.valueOf(LonLatConverter.convert(getConfiguration().getString(grid_prefix + ".south-east-corner.lon"), LonLatConverter.LonLatFormat.DecimalDeg)));
        double lat2 = Float.valueOf(LonLatConverter.convert(getConfiguration().getString(grid_prefix + ".south-east-corner.lat"), LonLatConverter.LonLatFormat.DecimalDeg));

        double[] pGrid1, pGrid2;
        int ipn, jpn;

        pGrid1 = latlon2xy(lat1, lon1);
        pGrid2 = latlon2xy(lat2, lon2);
        if (pGrid1[0] < 0 || pGrid2[0] < 0) {
            error("[dataset] Crop grid error.", new IOException("Impossible to proportion the simulation area : points out of domain"));
        }

        //System.out.println((float)pGrid1[0] + " " + (float)pGrid1[1] + " " + (float)pGrid2[0] + " " + (float)pGrid2[1]);
        i0 = (int) Math.min(Math.floor(pGrid1[0]), Math.floor(pGrid2[0]));
        ipn = (int) Math.max(Math.ceil(pGrid1[0]), Math.ceil(pGrid2[0]));
        j0 = (int) Math.min(Math.floor(pGrid1[1]), Math.floor(pGrid2[1]));
        jpn = (int) Math.max(Math.ceil(pGrid1[1]), Math.ceil(pGrid2[1]));

        nx = Math.min(nx, ipn - i0 + 1);
        ny = Math.min(ny, jpn - j0 + 1);
        debug("[dataset] Crop i0 " + i0 + " nx " + nx + " j0 " + j0 + " ny " + ny);
    }

    @Override
    public boolean isInWater(int i, int j, int k) {
        int ci = xTore(i);
        return !Double.isNaN(mask.getDouble(ci, j, k));
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
    public double getDepth(double x, double y, int k) {
        return -depthLevel[k];
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
    public int get_i0() {
        return i0;
    }

    @Override
    public int get_j0() {
        return j0;
    }

    @Override
    public int get_nz() {
        return nz;
    }

    @Override
    public double get_dx(int i, int j) {
        return dx[j];
    }

    @Override
    public double get_dy(int i, int j) {
        return dy;
    }

    @Override
    public double get_dz(int i, int j, int k) {
        return ddepth[k];
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Grid ");
        sb.append(grid_prefix);
        sb.append("\n  latmin: ");
        sb.append((float) getLatMin());
        sb.append(", latmax: ");
        sb.append((float) getLatMax());
        sb.append(", lonmin: ");
        sb.append((float) getLonMin());
        sb.append(", lonmax: ");
        sb.append((float) getLonMax());
        sb.append("\n  i0: ");
        sb.append(get_i0());
        sb.append(", nx: ");
        sb.append(get_nx());
        sb.append(", j0: ");
        sb.append(get_j0());
        sb.append(", ny: ");
        sb.append(get_ny());
        sb.append("\n  nz: ");
        sb.append(get_nz());
        return sb.toString();
    }

}
