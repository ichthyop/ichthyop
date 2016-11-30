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
import java.util.HashMap;
import java.util.logging.Level;
import static org.ichthyop.manager.SimulationManager.getLogger;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public class TiledVariable {

    private final NetcdfFile nc;
    private final Variable variable;
    private final HashMap<Integer, Array> tiles;
    private final int nx, ny, nz;
    private final int i0, j0;
    private final int nh, nv;
    private final int ntilex, ntiley, ntilez;
    private final int rank;
    private final int length;


    /*
     * 2D Tile Variable
     */
    public TiledVariable(NetcdfFile nc, String standardName, int nx, int ny, int i0, int j0, int rank, int nh) {

        
        this.tiles = new HashMap();
        this.nc = nc;
        this.variable = this.nc.findVariable(standardName);
        this.nx = nx;
        this.ny = ny;
        this.nz = -1;
        this.i0 = i0;
        this.j0 = j0;
        this.nh = nh;
        this.nv = -1;
        this.ntilex = (int) Math.round((float) nx / nh);
        this.ntiley = (int) Math.round((float) ny / nh);
        this.ntilez = -1;
        this.rank = rank;
        this.length = variable.getShape().length;
    }

    /*
     * 3D Tile Variable
     */
    public TiledVariable(NetcdfFile nc, String standardName, int nx, int ny, int nz, int i0, int j0, int rank, int nh, int nv) {
        
        this.tiles = new HashMap();
        this.nc = nc;
        this.variable = this.nc.findVariableByAttribute(null, "standard_name", standardName);
        this.nx = nx;
        this.ny = ny;
        this.nz = nz;
        this.i0 = i0;
        this.j0 = j0;
        this.nh = nh;
        this.nv = nv;
        this.ntilex = (int) Math.round((float) nx / nh);
        this.ntiley = (int) Math.round((float) ny / nh);
        this.ntilez = (int) Math.round((float) nz / nv);
        this.rank = rank;
        this.length = variable.getShape().length;
    }
    
    public void clear() {
        tiles.clear();
        try {
            nc.close();
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public double getDouble(int i, int j) {

        int tag = ij2tag(i, j);
        int[] ijtile = tag2ij(tag);
        int i0tile = i0 + ijtile[0];
        int j0tile = j0 + ijtile[1];
        int nxtile = Math.min(nh, i0 + nx - i0tile);
        int nytile = Math.min(nh, j0 + ny - j0tile);

        if (!tiles.containsKey(tag)) {
            // load tile
            int[] origin = null, shape = null;
            switch (length) {
                case 2:
                    origin = new int[]{j0tile, i0tile};
                    shape = new int[]{nytile, nxtile};
                    break;
                case 3:
                    origin = new int[]{rank, j0tile, i0tile};
                    shape = new int[]{1, nytile, nxtile};
                    break;
            }
            try {
                tiles.put(tag, variable.read(origin, shape).reduce());
            } catch (IOException | InvalidRangeException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        }
        Array tile = tiles.get(tag);
        return tile.getDouble(tile.getIndex().set(j % nh, i % nh));
    }

    public double getDouble(int i, int j, int k) {

        int tag = ijk2tag(i, j, k);
//        if (log) {
//            System.out.println("i " + i + " j " + j + " k " + k + " tag=" + tag);
//        }
        int[] ijktile = tag2ijk(tag);
//        if (log) {
//            System.out.println("i0 " + ijktile[0] + " j0 " + ijktile[1] + " k0 " + ijktile[2]);
//        }
        int i0tile = i0 + ijktile[0];
        int j0tile = j0 + ijktile[1];
        int k0tile = ijktile[2];
        int nxtile = ((tag + 1) % ntilex == 0) ? i0 + nx - i0tile : nh;
        int nytile = ((tag / ntilex + 1) % ntiley == 0) ? j0 + ny - j0tile : nh;
        int nztile = ((tag / (ntilex * ntiley) + 1) % ntilez == 0) ? nz - k0tile : nv;

        if (!tiles.containsKey(tag)) {
            // load tile
            int[] origin = null, shape = null;
            switch (length) {
                case 3:
                    origin = new int[]{k0tile, j0tile, i0tile};
                    shape = new int[]{nztile, nytile, nxtile};
                    break;
                case 4:
                    origin = new int[]{rank, k0tile, j0tile, i0tile};
                    shape = new int[]{1, nztile, nytile, nxtile};
                    break;
            }
            try {
                tiles.put(tag, variable.read(origin, shape).reduce());
            } catch (IOException | InvalidRangeException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        }
        Array tile = tiles.get(tag);
//        int kt = Math.min(k, nv * ntilez - 1) % nv + Math.max(k - nv * ntilez + 1, 0);
//        int jt = Math.min(j, nh * ntiley - 1) % nh + Math.max(j - nh * ntiley + 1, 0);
//        int it = Math.min(i, nh * ntilex - 1) % nh + Math.max(i - nh * ntilex + 1, 0);
//        System.out.println(it + " " + jt + " "+ kt);
        return tile.getDouble(tile.getIndex().set(
                Math.min(k, nv * ntilez - 1) % nv + Math.max(k - nv * ntilez + 1, 0),
                Math.min(j, nh * ntiley - 1) % nh + Math.max(j - nh * ntiley + 1, 0),
                Math.min(i, nh * ntilex - 1) % nh + Math.max(i - nh * ntilex + 1, 0)));
    }

    private int ij2tag(int i, int j) {
        return Math.min(i / nh, ntilex - 1) + ntilex * Math.min(j / nh, ntiley - 1);
    }

    private int[] tag2ij(int tag) {
        return new int[]{nh * (tag % ntilex), nh * ((tag / ntilex) % ntiley)};
    }

    private int ijk2tag(int i, int j, int k) {
        return Math.min(i / nh, ntilex - 1) + ntilex * Math.min(j / nh, ntiley - 1) + ntilex * ntiley * Math.min(k / nv, ntilez - 1);
    }

    private int[] tag2ijk(int tag) {
        return new int[]{nh * (tag % ntilex), nh * ((tag / ntilex) % ntiley), nv * ((tag / (ntilex * ntiley)) % ntilez)};
    }
}
