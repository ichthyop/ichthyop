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
import org.ichthyop.grid.IGrid;
import org.ichthyop.grid.RectilinearGrid;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public abstract class Hycom3dCommon extends AbstractDataset {

    DatasetVariable u;
    DatasetVariable v;
    DatasetVariable w;
    boolean xTore = true;
    final int tilingh = 100, tilingv = 3, tilinghw = 10;

    abstract NetcdfFile getNC();

    @Override
    public void setUp() throws Exception {

        loadParameters();

        grid = new RectilinearGrid(getKey() + ".grid");
        grid.init();

        u = new DatasetVariable(grid);
        v = new DatasetVariable(grid);
        w = new DatasetVariable(grid);
    }

    @Override
    public double get_dUx(double[] pGrid, double time) {
        return u.getDouble(pGrid, time) / getGrid().get_dx((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {
        return v.getDouble(pGrid, time) / getGrid().get_dy((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]));
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {
        return w.getDouble(pGrid, time) / getGrid().get_dz((int) Math.round(pGrid[0]), (int) Math.round(pGrid[1]), (int) Math.round(pGrid[2]));
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

    public class WTiledVariable extends NetcdfTiledVariable {

        private final NetcdfTiledVariable uw;
        private final NetcdfTiledVariable vw;

        WTiledVariable(NetcdfFile nc, IGrid grid, int rank, double timestamp, int nh) throws IOException {
            super(nc, null, grid, 1, timestamp, 1, grid.get_nz());
            uw = new NetcdfTiledVariable(nc, "eastward_sea_water_velocity", grid, rank, timestamp, nh, nz);
            vw = new NetcdfTiledVariable(nc, "northward_sea_water_velocity", grid, rank, timestamp, nh, nz);
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
