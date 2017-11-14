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
import org.ichthyop.grid.RectilinearGrid;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public abstract class Hycom3dCommon extends AbstractDataset {

    final int nt = 3;
    NetcdfTiledVariable[] u;
    NetcdfTiledVariable[] v;
    WTiledVariable[] w;
    double dt_HyMo, time_tp1;
    int rank;
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

        grid = new RectilinearGrid(getKey() + ".grid");
        grid.init();

        u = new NetcdfTiledVariable[nt];
        v = new NetcdfTiledVariable[nt];
        w = new WTiledVariable[nt];
        // Initializes u[0] & v[0] for the mask
        u[0] = new NetcdfTiledVariable(getNC(), "eastward_sea_water_velocity", getGrid().get_nx(), getGrid().get_ny(), getGrid().get_nz(), getGrid().get_i0(), getGrid().get_j0(), 0, 0, tilingh, tilingv);
        v[0] = new NetcdfTiledVariable(getNC(), "northward_sea_water_velocity", getGrid().get_nx(), getGrid().get_ny(), getGrid().get_nz(), getGrid().get_i0(), getGrid().get_j0(), 0, 0, tilingh, tilingv);
    }

    private double weight(double[] xyz, int[] ijk, int p) {
        double distance = 0.d;
        for (int n = 0; n < xyz.length; n++) {
            distance += Math.abs(Math.pow(xyz[n] - ijk[n], p));
        }
        return 1.d / distance;
    }

    private boolean isOut(int i, int j, int k) {
        return i < 0 || j < 0 || k < 0 || i > getGrid().get_nx() - 1 || j > getGrid().get_ny() - 1 || k > getGrid().get_nz() - 1;
    }

    private double interpolateIDW(AbstractTiledVariable[] tv, double[] pGrid, double time) {

        double value = 0.d;
        boolean coast = getGrid().isCloseToCost(pGrid);
        int n[] = coast ? new int[]{0, 1} : new int[]{0, 2}; // 8 points
        //int n[] = coast ? new int[]{0, 1} : new int[] {-1, 3}; // 16 points
        //int n[] = coast ? new int[]{0, 1} : new int[] {-2, 4}; // 64 points
        int i = coast ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = coast ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        int k = coast ? (int) Math.round(pGrid[2]) : (int) pGrid[2];
        double dt = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        double CO = 0.d;

        if (Double.isInfinite(weight(pGrid, new int[]{i, j, k}, p))) {
            // pGrid falls on a grid point
            CO = 1.d;
            i = getGrid().xTore(i);
            if (!(Double.isNaN(tv[0].getDouble(i, j, k)) || Double.isNaN(tv[1].getDouble(i, j, k)))) {
                value = (1.d - dt) * tv[0].getDouble(i, j, k) + dt * tv[1].getDouble(i, j, k);
            }
        } else {
            for (int ii = n[0]; ii < n[1]; ii++) {
                for (int jj = n[0]; jj < n[1]; jj++) {
                    for (int kk = n[0]; kk < n[1]; kk++) {
                        int ci = getGrid().xTore(i + ii);
                        if (isOut(ci, j + jj, k + kk)) {
                            continue;
                        }
                        double co = weight(pGrid, new int[]{i + ii, j + jj, k + kk}, p);
                        CO += co;
                        if (!(Double.isNaN(tv[0].getDouble(ci, j + jj, k + kk)) || Double.isNaN(tv[1].getDouble(ci, j + jj, k + kk)))) {
                            double x = (1.d - dt) * tv[0].getDouble(ci, j + jj, k + kk) + dt * tv[1].getDouble(ci, j + jj, k + kk);
                            value += x * co;
                        }
                    }
                }
            }
        }
        if (CO != 0) {
            value /= CO;
        }

        return value;
    }

    @Override
    public double get_dUx(double[] pGrid, double time) {
        return interpolateIDW(u, pGrid, time) / getGrid().get_dx((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {
        return interpolateIDW(v, pGrid, time) / getGrid().get_dy((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {
        return interpolateIDW(w, pGrid, time) / getGrid().get_dz((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]), (int) Math.round(pGrid[2]));
    }

    @Override
    public double getBathy(int i, int j) {
        return getGrid().getDepthMax(i, j);
    }

    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
//        Variable variable = nc.findVariable(name);
//        int[] origin = null, shape = null;
//        switch (variable.getShape().length) {
//            case 4:
//                origin = new int[]{rank, 0, j0, i0};
//                shape = new int[]{1, getGrid().get_nz(), getGrid().get_ny(), getGrid().get_nx()};
//                break;
//            case 2:
//                origin = new int[]{j0, i0};
//                shape = new int[]{getGrid().get_ny(), getGrid().get_nx()};
//                break;
//            case 3:
//                if (!variable.isUnlimited()) {
//                    origin = new int[]{0, j0, i0};
//                    shape = new int[]{getGrid().get_nz(), getGrid().get_ny(), getGrid().get_nx()};
//
//                } else {
//                    origin = new int[]{rank, j0, i0};
//                    shape = new int[]{1, getGrid().get_ny(), getGrid().get_nx()};
//                }
//                break;
//        }
//
//        return variable.read(origin, shape).reduce();
        return null;
    }

    public class WTiledVariable extends AbstractTiledVariable {

        private final NetcdfTiledVariable uw;
        private final NetcdfTiledVariable vw;

        WTiledVariable(NetcdfFile nc, int nx, int ny, int nz, int i0, int j0, int nh, int rank, double timestamp) throws IOException {
            super(nx, ny, nz, 1, nz, timestamp);
            uw = new NetcdfTiledVariable(nc, "eastward_sea_water_velocity", nx, ny, nz, i0, j0, rank, timestamp, nh, nz);
            vw = new NetcdfTiledVariable(nc, "northward_sea_water_velocity", nx, ny, nz, i0, j0, rank, timestamp, nh, nz);
        }

        @Override
        Array loadTile(int tag) {

            int j = tag / getGrid().get_nx();
            int i = tag % getGrid().get_nx();

            double[][] Huon = new double[getGrid().get_nz()][2];
            double[][] Hvom = new double[getGrid().get_nz()][2];

            int ci = i, cim1 = i - 1;
            if (i == 0) {
                ci = xTore ? i : i + 1;
                cim1 = xTore ? getGrid().get_nx() - 1 : i;
            }
            int cj = (j == 0) ? j + 1 : j;
            int cjm1 = (j == 0) ? j : j - 1;

            for (int k = 0; k < getGrid().get_nz(); k++) {
                double dz = getGrid().get_dz(0, 0, k);
                Huon[k][1] = Double.isNaN(uw.getDouble(ci, cj, k))
                        ? 0.d
                        : uw.getDouble(ci, cj, k) * getGrid().get_dy(ci, cj) * dz;
                Huon[k][0] = Double.isNaN(uw.getDouble(cim1, cj, k))
                        ? 0.d
                        : uw.getDouble(cim1, cj, k) * getGrid().get_dy(cj, cim1) * dz;

                Hvom[k][1] = Double.isNaN(vw.getDouble(ci, cj, k))
                        ? 0.d
                        : vw.getDouble(ci, cj, k) * getGrid().get_dx(ci, cj) * dz;
                Hvom[k][0] = Double.isNaN(vw.getDouble(ci, cjm1, k))
                        ? 0.d
                        : vw.getDouble(ci, cjm1, k) * getGrid().get_dx(ci, cjm1) * dz;
            }

            // Find k0, index of the deepest cell in water
            int k0 = getGrid().get_nz() - 1;
            for (int k = getGrid().get_nz() - 1; k > 0; k--) {
                if (!Double.isNaN(uw.getDouble(ci, cj, k))) {
                    k0 = k;
                    break;
                }
            }

            Array w = new ArrayDouble.D1(getGrid().get_nz());
            for (int k = getGrid().get_nz() - 1; k > k0; k--) {
                w.setDouble(k, 0.d);
            }
            double dy = getGrid().get_dy(ci, cj);
            double dx = getGrid().get_dx(ci, cj);
            for (int k = k0; k > 0; k--) {
                double wtmp = ((k > getGrid().get_nz() - 2) ? 0. : w.getDouble(k + 1)) - (Huon[k][1] - Huon[k][0] + Hvom[k][1] - Hvom[k][0]);
                wtmp /= (dx * dy);
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
