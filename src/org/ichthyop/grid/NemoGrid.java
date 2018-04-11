/*
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2017
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Nicolas BARRIER, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
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
import org.ichthyop.dataset.variable.NetcdfTiledArray;
import org.ichthyop.ui.LonLatConverter;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author Nicolas Barrier
 */
public class NemoGrid extends AbstractRegularGrid {

    private String varlon;
    private String varlat;
    private String vardepth;
    private String varmask;
    private String vare2t;
    private String vare1t;
            
    private double[][] longitude;
    private double[][] latitude;
    private double[] depthLevel;
    private int nx, ny, nz;
    private int i0, j0;
    private double[][] dx;
    private double[][] dy;
    private NetcdfTiledArray mask;

    private NetcdfTiledArray e3t;  // e3t variable is 3D in NEMO
    private double[] e3t_0;  // e3t_0 (1d scale factor)
    private double[][] e3t_ps; // 2D scale factor of the last point
    private int[][] mbathy; // Number of unmasked points on the vertical

    public NemoGrid(String prefix) {
        super(prefix);
    }

    @Override
    void makeGrid() {
        // grid file
        try {

            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ latitude (2D array)
            varlat = getConfiguration().isNull(grid_prefix + ".variable.latitude")
                    ? "gphit"
                    : getConfiguration().getString(grid_prefix + ".variable.latitude");

            if (!variables.containsKey(varlat)) {
                throw new IOException("Latitude variable not found in dataset " + grid_prefix);
            }

            // For a given variable of name varlat, the get function returns
            // a list containing the (file location, the standard_name and the long_name)
            String location = variables.get(varlat).get(0);

            NetcdfFile nc = DatasetUtil.open(location, enhanced);
            varlat = DatasetUtil.findVariable(nc, varlat);
            Array array = nc.findVariable(varlat).read().reduce();
            nc.close();
            int[] shape = array.getShape();
            ny = shape[0];
            nx = shape[1];
            latitude = new double[ny][nx];
            Index index = array.getIndex();
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    index.set(j, i);
                    latitude[j][i] = array.getDouble(index);
                }
            }
            j0 = 0;

            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ latitude (2D array)
            varlon = getConfiguration().isNull(grid_prefix + ".variable.longitude")
                    ? "glamt"
                    : getConfiguration().getString(grid_prefix + ".variable.longitude");

            if (!variables.containsKey(varlon)) {
                throw new IOException("Longitude variable not found in dataset " + grid_prefix);
            }

            // For a given variable of name varlat, the get function returns
            // a list containing the (file location, the standard_name and the long_name)
            location = variables.get(varlon).get(0);

            nc = DatasetUtil.open(location, enhanced);
            varlon = DatasetUtil.findVariable(nc, varlon);
            array = nc.findVariable(varlon).read().reduce();
            nc.close();
            longitude = new double[ny][nx];
            index = array.getIndex();
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    index.set(j, i);
                    longitude[j][i] = array.getDouble(index);
                }
            }
            i0 = 0;

            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++  depth (1D array)
            // to do: reconstruct "real" 3D bathymetry using e3t, and mbathy?
            vardepth = getConfiguration().isNull(grid_prefix + ".variable.depth")
                    ? "gdept"
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
                warning("[grid] Could not find depth variable in dataset " + grid_prefix + ". Ichthyop assumes the grid is 2D.");
                nz = 1;
                depthLevel = new double[]{0};
            }
            
            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ e2t (2D array)
            vare2t = getConfiguration().isNull(grid_prefix + ".variable.e2t")
                    ? "e2t"
                    : getConfiguration().getString(grid_prefix + ".variable.e2t");

            if (!variables.containsKey(vare2t)) {
                throw new IOException("e2t variable not found in dataset " + grid_prefix);
            }

            // For a given variable of name varlat, the get function returns
            // a list containing the (file location, the standard_name and the long_name)
            location = variables.get(vare2t).get(0);

            nc = DatasetUtil.open(location, enhanced);
            vare2t = DatasetUtil.findVariable(nc, vare2t);
            array = nc.findVariable(vare2t).read().reduce();
            nc.close();
            dy = new double[ny][nx];
            index = array.getIndex();
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    index.set(j, i);
                    dy[j][i] = array.getDouble(index);
                }
            }
            
            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ e2t (2D array)
            vare1t = getConfiguration().isNull(grid_prefix + ".variable.e1t")
                    ? "e1t"
                    : getConfiguration().getString(grid_prefix + ".variable.e1t");

            if (!variables.containsKey(vare1t)) {
                throw new IOException("e1t variable not found in dataset " + grid_prefix);
            }

            // For a given variable of name varlat, the get function returns
            // a list containing the (file location, the standard_name and the long_name)
            location = variables.get(vare1t).get(0);

            nc = DatasetUtil.open(location, enhanced);
            vare1t = DatasetUtil.findVariable(nc, vare1t);
            array = nc.findVariable(vare2t).read().reduce();
            nc.close();
            dx = new double[ny][nx];
            index = array.getIndex();
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    index.set(j, i);
                    dx[j][i] = array.getDouble(index);
                }
            }
            

            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++  mask
            varmask = getConfiguration().isNull(grid_prefix + ".variable.mask")
                    ? "tmask"
                    : getConfiguration().getString(grid_prefix + ".variable.mask");
            // If the tmask is not found from the grid file, raise an error.
            if (!variables.containsKey(varmask)) {
                error("[grid] Failed to load " + grid_prefix + " in the grid file", new IOException("Data not found!!!"));
            }

            // gets the mask variable on tile
            location = variables.get(varmask).get(0);
            mask = new NetcdfTiledArray(DatasetUtil.open(location, true), varmask, this, 0, 0, 10, Math.min(3, nz));

            //++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++ e3t
            this.read_e3t();
            
        } catch (IOException ex) {
            error("[grid] Failed to make grid " + grid_prefix, ex);
        }
    }

    @Override
    public boolean isInWater(int i, int j, int k) {

        if (null != mask) {
            int ci = xTore(i);
            return !Double.isNaN(mask.getDouble(ci, j, k));
        } else {
            return true;
        }

    }

    @Override
    public double getLon(int i, int j) {
        return longitude[j][i];
    }

    @Override
    public double getLat(int i, int j) {
        return latitude[j][i];
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
        return dx[j][i];
    }

    @Override
    public double get_dy(int i, int j) {
        return dy[j][i];
    }

    /** Recovers the dz variable on T grid points. It 
     * either uses the 3D e3t variable (most common) or
     * the 2D variables e3t_ps, mbathy and e3t_0
     * 
     * @param i
     * @param j
     * @param k
     * @return 
     */
    @Override
    public double get_dz(int i, int j, int k) {

        double output;
        int ci = xTore(i);

        // If e3t is not null, the 3D e3t variable is used
        if (null != e3t) {
            output = e3t.getDouble(ci, j, k);

        } else {

            if ((mbathy != null) && (e3t_ps != null)) {
                // if e3t should be reconstructed by using the 2D scale factors
                if (k == get_mbathy(i, j)) {
                    // if k is the last masked point, recovers 
                    // the partial step scale factor
                    output = get_e3tps(i, j);
                } else {
                    // else, recovers the full step scale factor
                    output = get_e3t0(k);
                }
            } else {
                // if full step is used.
                output = get_e3t0(k);
            }
        }   

        return output;
    }
    
    private double get_e3tps(int i, int j) {
        return e3t_ps[j][i];
    }

    private double get_e3t0(int k) {
        return e3t_0[k];
    }

    private int get_mbathy(int i, int j) {
        return mbathy[j][i];
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

    /** Reads the variables that allow the computation
     * of 3D vertical scale factors on T points (e3t).
     * 
     * @throws IOException 
     */
    private void read_e3t() throws IOException {

        String vare3t;
        String vare3t_0;
        String vare3t_ps;
        String vare3t_mbathy;
        String location;
        NetcdfFile nc;
        Array array;
        Index index;

        // Reads the e3t variable if it exists in the file. Since it is a 
        // 3D variable, reads it as TiledVariable
        if (!getConfiguration().isNull(grid_prefix + ".variable.e3t")) {
            // Assumes partial step defined in the e3t variable scale factors (most common case)
            vare3t = getConfiguration().getString(grid_prefix + ".variable.e3t");
            // If the tmask is not found from the grid file, raise an error.
            if (!variables.containsKey(vare3t)) {
                error("[grid] Failed to load " + grid_prefix + " in the grid file", new IOException("Data not found!!!"));
            }
            // gets the mask variable on tile
            location = variables.get(vare3t).get(0);
            e3t = new NetcdfTiledArray(DatasetUtil.open(location, true), vare3t, this, 0, 0, 10, Math.min(3, nz));
        }

        // Reads the e3t_ps variable: this is the vertical scale factor of the last ocean point
        if (!getConfiguration().isNull(grid_prefix + ".variable.e3t_ps")) {

            vare3t_ps = getConfiguration().getString(grid_prefix + ".variable.e3t_ps");

            // For a given variable of name varlat, the get function returns
            // a list containing the (file location, the standard_name and the long_name)
            location = variables.get(vare3t_ps).get(0);

            nc = DatasetUtil.open(location, enhanced);
            vare3t_ps = DatasetUtil.findVariable(nc, vare3t_ps);
            array = nc.findVariable(vare3t_ps).read().reduce();
            nc.close();
            e3t_ps = new double[ny][nx];
            index = array.getIndex();
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    index.set(j, i);
                    e3t_ps[j][i] = array.getDouble(index);
                }
            }
        }

        // Reads the mbathy variable: this is the number of unmasked points on the vertical
        if (!getConfiguration().isNull(grid_prefix + ".variable.e3t_mbathy")) {

            vare3t_mbathy = getConfiguration().getString(grid_prefix + ".variable.e3t_mbathy");

            // For a given variable of name varlat, the get function returns
            // a list containing the (file location, the standard_name and the long_name)
            location = variables.get(vare3t_mbathy).get(0);

            nc = DatasetUtil.open(location, enhanced);
            vare3t_mbathy = DatasetUtil.findVariable(nc, vare3t_mbathy);
            array = nc.findVariable(vare3t_mbathy).read().reduce();
            nc.close();
            mbathy = new int[ny][nx];
            index = array.getIndex();
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx; i++) {
                    index.set(j, i);
                    mbathy[j][i] = array.getInt(index) - 1;   // remove one so that mbathy = k index of the last ocean point
                }
            }
        }

        // Reads the e3t_0 variable: this is the full step vertical scale factor
        if (!getConfiguration().isNull(grid_prefix + ".variable.e3t_0")) {

            vare3t_0 = getConfiguration().getString(grid_prefix + ".variable.e3t_0");

            // For a given variable of name varlat, the get function returns
            // a list containing the (file location, the standard_name and the long_name)
            location = variables.get(vare3t_0).get(0);

            nc = DatasetUtil.open(location, enhanced);
            vare3t_0 = DatasetUtil.findVariable(nc, vare3t_0);
            array = nc.findVariable(vare3t_0).read().reduce();
            nc.close();
            e3t_0 = new double[nz];
            index = array.getIndex();
            for (int k = 0; k < nz; k++) {
                index.set(k);
                e3t_0[k] = array.getDouble(index);
            }

        }

    }

}
