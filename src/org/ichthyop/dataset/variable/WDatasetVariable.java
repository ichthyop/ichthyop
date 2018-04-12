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
package org.ichthyop.dataset.variable;

import java.io.IOException;
import org.ichthyop.grid.IGrid;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;

/**
 *
 * @author pverley
 */
public class WDatasetVariable extends AbstractDatasetVariable {

    private final AbstractDatasetVariable u;
    private final AbstractDatasetVariable v;

    public WDatasetVariable(AbstractDatasetVariable u, AbstractDatasetVariable v, IGrid grid) {
        super(u.nlayer, grid, u.calendar, u.t0);
        this.u = u;
        this.v = v;
    }

    @Override
    public void init(double t0, int time_arrow) throws IOException {
        u.init(t0, time_arrow);
        v.init(t0, time_arrow);
        for (int ilayer = 0; ilayer < nlayer; ilayer++) {
            if (null != u.stack[ilayer] && null != v.stack[ilayer]) {
                stack[ilayer] = new WTiledArray(u.stack[ilayer], v.stack[ilayer]);
            }
        }
    }

    @Override
    public void update(double currenttime, int time_arrow) throws IOException {
        if (updateNeeded(currenttime, time_arrow)) {
            u.update(currenttime, time_arrow);
            v.update(currenttime, time_arrow);
            update(new WTiledArray(u.stack[nlayer - 1], v.stack[nlayer - 1]));
        }
    }

    /*
    WARNING PhV 2018.04.12 this class only works so far for HYCOM like grid
    And it is not fully rigorous since I compute U and V flux at cell center though
    they should be computed on U and V point (arakawa C grid configuration)
    To be improved
    */
    private class WTiledArray extends NetcdfTiledArray {

        private final NetcdfTiledArray uw;
        private final NetcdfTiledArray vw;

        WTiledArray(NetcdfTiledArray uw, NetcdfTiledArray vw) {
            super(null, null, grid, -1, uw.getTimeStamp(), 1, grid.get_nz());
            this.uw = uw;
            this.vw = vw;
        }

        @Override
        Array loadTile(int tag) {

            int j = tag / grid.get_nx();
            int i = tag % grid.get_nx();

            double[][] Huon = new double[grid.get_nz()][2];
            double[][] Hvom = new double[grid.get_nz()][2];

            int ci = i, cim1 = i - 1;
            if (i == 0) {
                ci = grid.xTore() ? i : i + 1;
                cim1 = grid.xTore() ? grid.get_nx() - 1 : i;
            }
            int cj = j, cjm1 = j - 1;
            if (j == 0) {
                cj = grid.yTore() ? j : j + 1;
                cjm1 = grid.yTore() ? grid.get_ny() - 1 : j;
            }

            for (int k = 0; k < grid.get_nz(); k++) {
                double dz = grid.get_dz(ci, cj, k);
                Huon[k][1] = Double.isNaN(uw.getDouble(ci, cj, k))
                        ? 0.d
                        : uw.getDouble(ci, cj, k) * grid.get_dy(ci, cj) * dz;
                Huon[k][0] = Double.isNaN(uw.getDouble(cim1, cj, k))
                        ? 0.d
                        : uw.getDouble(cim1, cj, k) * grid.get_dy(cj, cim1) * dz;

                Hvom[k][1] = Double.isNaN(vw.getDouble(ci, cj, k))
                        ? 0.d
                        : vw.getDouble(ci, cj, k) * grid.get_dx(ci, cj) * dz;
                Hvom[k][0] = Double.isNaN(vw.getDouble(ci, cjm1, k))
                        ? 0.d
                        : vw.getDouble(ci, cjm1, k) * grid.get_dx(ci, cjm1) * dz;
            }

            // Find k0, index of the deepest cell in water
            int k0 = grid.get_nz() - 1;
            for (int k = grid.get_nz() - 1; k > 0; k--) {
                if (!Double.isNaN(uw.getDouble(ci, cj, k))) {
                    k0 = k;
                    break;
                }
            }

            Array w = new ArrayDouble.D1(grid.get_nz());
            for (int k = grid.get_nz() - 1; k > k0; k--) {
                w.setDouble(k, 0.d);
            }
            double dy = grid.get_dy(ci, cj);
            double dx = grid.get_dx(ci, cj);
            for (int k = k0; k > 0; k--) {
                double wtmp = ((k > grid.get_nz() - 2) ? 0. : w.getDouble(k + 1)) - (Huon[k][1] - Huon[k][0] + Hvom[k][1] - Hvom[k][0]);
                wtmp /= (dx * dy);
                w.setDouble(k, wtmp);
                //System.out.println("tile " + tag + " i" + ci + " j" + cj + " k" + k + " w" + (float)wtmp);
            }
            w.setDouble(0, 0.d);

            return w;
        }
    }

}
