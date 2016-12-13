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
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.ichthyop.manager.SimulationManager.getLogger;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public class NetcdfTiledVariable extends AbstractTiledVariable {

    private final NetcdfFile nc;
    private final Variable variable;
    private final int i0, j0;
    private final int rank;
    private final int length;

    /*
     * 2D Tile Variable
     */
    public NetcdfTiledVariable(NetcdfFile nc, String standardName, int nx, int ny, int i0, int j0, int rank, int nh) {
        this(nc, standardName, nx, ny, 0, i0, j0, rank, nh, 1);
    }

    /*
     * 3D Tile Variable
     */
    public NetcdfTiledVariable(NetcdfFile nc, String standardName, int nx, int ny, int nz, int i0, int j0, int rank, int nh, int nv) {

        super(nx, ny, nz, nh, nv);
        this.nc = nc;
        this.variable = this.nc.findVariableByAttribute(null, "standard_name", standardName);
        this.i0 = i0;
        this.j0 = j0;
        this.rank = rank;
        this.length = variable.getShape().length;
    }

    @Override
    Array loadTile(int tag) {
        synchronized (nc) {
            int[] ijktile = tag2ijk(tag);
            int i0tile = i0 + ijktile[0];
            int j0tile = j0 + ijktile[1];
            int k0tile = ijktile[2];
            int nxtile = ((tag + 1) % ntilex == 0) ? nx - ijktile[0] : nh;
            int nytile = ((tag / ntilex + 1) % ntiley == 0) ? ny - ijktile[1] : nh;
            int nztile = ((tag / (ntilex * ntiley) + 1) % ntilez == 0) ? nz - ijktile[2] : nv;

            // load tile
            int[] origin = null, shape = null;
            switch (length) {
                case 2:
                    origin = new int[]{j0tile, i0tile};
                    shape = new int[]{nytile, nxtile};
                    break;
                case 3:
                    if (variable.isUnlimited()) {
                        origin = new int[]{rank, j0tile, i0tile};
                        shape = new int[]{1, nytile, nxtile};
                    } else {
                        origin = new int[]{k0tile, j0tile, i0tile};
                        shape = new int[]{nztile, nytile, nxtile};
                    }
                    break;
                case 4:
                    origin = new int[]{rank, k0tile, j0tile, i0tile};
                    shape = new int[]{1, nztile, nytile, nxtile};
                    break;
            }

            getLogger().log(Level.FINE, "Reading NetCDF variable {0} from file {1} at rank {2} tile {3} ({4} : {5})", new Object[]{variable.getFullName(), nc.getLocation(), rank, tag, Arrays.toString(origin), Arrays.toString(shape)});
            try {
                return variable.read(origin, shape).reduce();
            } catch (IOException | InvalidRangeException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
            return null;
        }
    }

    @Override
    void closeSource() {
        try {
            nc.close();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

}
