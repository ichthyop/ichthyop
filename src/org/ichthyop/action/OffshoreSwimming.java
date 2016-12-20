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
package org.ichthyop.action;

import java.util.ArrayList;
import org.ichthyop.dataset.DatasetUtil;
import org.ichthyop.particle.IParticle;

/**
 *
 * @author pverley
 */
public class OffshoreSwimming extends AbstractAction {

    // max radius (meter)
    private final double radius = 50 * 1e3;

    @Override
    public void loadParameters() throws Exception {
        // Nothing to do
    }

    @Override
    public void execute(IParticle particle) {

        double lat0 = particle.getLat();
        double lon0 = particle.getLon();
        int i0 = (int) Math.round(particle.getX());
        int j0 = (int) Math.round(particle.getY());
        int n = 1;
        // Find coastal cells withing the defined radius
        ArrayList<int[]> coord = new ArrayList();
        int nx = getSimulationManager().getDataset().get_nx();
        int ny = getSimulationManager().getDataset().get_ny();
        while (true) {
            int c = 0;
            for (int i = -n; i < n; i++) {
                if (j0 + n < ny && i0 + i >= 0 && i0 + i < nx) {
                    if (inside(i0 + i, j0 + n, lat0, lon0)) {
                        c++;
                        if (coastal(i0 + i, j0 + n)) {
                            coord.add(new int[]{i0 + i, j0 + n});
                        }
                    }
                }
                if (j0 - n >= 0 && i0 + i >= 0 && i0 + i < nx) {
                    if (inside(i0 + i, j0 - n, lat0, lon0)) {
                        c++;
                        if (coastal(i0 + i, j0 - n)) {
                            coord.add(new int[]{i0 + i, j0 - n});
                        }
                    }
                }
            }
            for (int j = -n + 1; j < n - 1; j++) {
                if (i0 + n < nx && j0 + j >= 0 && j0 + j < ny) {
                    if (inside(i0 + n, j0 + j, lat0, lon0)) {
                        c++;
                        if (coastal(i0 + n, j0 + j)) {
                            coord.add(new int[]{i0 + n, j0 + j});
                        }
                    }
                }
                if (i0 - n >= 0 && j0 + j >= 0 && j0 + j < ny) {
                    if (inside(i0 - n, j0 + j, lat0, lon0)) {
                        c++;
                        if (coastal(i0 - n, j0 + j)) {
                            coord.add(new int[]{i0 - n, j0 + j});
                        }
                    }
                }
            }
            if (c == 0) {
                break;
            }
            n++;
        }

//        for (int[] ij : coord) {
//            System.out.println("particle " + particle.getIndex() + " i" + ij[0] + " j" + ij[1]);
//        }

    }

    private int ibound(int i) {
        return Math.max(Math.min(i, getSimulationManager().getDataset().get_nx() - 1), 0);
    }

    private int jbound(int j) {
        return Math.max(Math.min(j, getSimulationManager().getDataset().get_ny() - 1), 0);
    }

    private boolean inside(int i, int j, double lat0, double lon0) {
        double lat = getSimulationManager().getDataset().getLat(i, j);
        double lon = getSimulationManager().getDataset().getLon(i, j);
        return DatasetUtil.geodesicDistance(lat0, lon0, lat, lon) < radius;
    }

    private boolean coastal(int i, int j) {

        if (!getSimulationManager().getDataset().isInWater(i, j)) {
            int ip1 = Math.min(i + 1, getSimulationManager().getDataset().get_nx()-1);
            int jp1 = Math.min(j + 1, getSimulationManager().getDataset().get_ny()-1);
            int im1 = Math.max(i - 1, 0);
            int jm1 = Math.max(j - 1, 0);
            return getSimulationManager().getDataset().isInWater(ip1, j)
                    || getSimulationManager().getDataset().isInWater(im1, j)
                    || getSimulationManager().getDataset().isInWater(i, jp1)
                    || getSimulationManager().getDataset().isInWater(i, jm1);
        }
        return false;
    }

    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

}
