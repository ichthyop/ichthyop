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
package org.ichthyop.dataset.variable;

import java.io.IOException;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.dataset.DatasetUtil;
import org.ichthyop.grid.IGrid;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public class TiledVariable extends IchthyopLinker {

    private final ConcurrentMap<Integer, Array> tiles;
    final int nx, ny, nz;
    final int nh, nv;
    final int ntilex, ntiley, ntilez;
    private final double timestamp;
    private final NetcdfFile nc;
    private final int i0, j0;
    private final int rank;
    private final Variable variable;
    private final int length;

    /*
     * 3D Tile Variable
     */
    public TiledVariable(NetcdfFile nc, String name, IGrid grid, int rank, double timestamp, int nh, int nv) {

        this.tiles = new ConcurrentHashMap();
        this.nc = nc;
        this.nx = grid.get_nx();
        this.ny = grid.get_ny();
        this.nz = grid.get_nz();
        this.i0 = grid.get_i0();
        this.j0 = grid.get_j0();
        this.rank = rank;
        this.nh = nh;
        this.nv = nv;
        this.timestamp = timestamp;
        this.ntilex = (int) Math.round((float) nx / nh);
        this.ntiley = (int) Math.round((float) ny / nh);
        this.ntilez = Math.max((int) Math.round((float) nz / nv), 1);
        String vname = (null != name) ? DatasetUtil.findVariable(nc, name) : null;
        if (null != vname) {
            this.variable = this.nc.findVariable(vname);
            this.length = variable.getShape().length;
        } else {
            this.variable = null;
            this.length = 0;
        }

    }

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

            debug("Reading NetCDF variable {0} from file {1} at rank {2} tile {3} ({4} : {5})", new Object[]{variable.getFullName(), nc.getLocation(), rank, tag, Arrays.toString(origin), Arrays.toString(shape)});
            try {
                return variable.read(origin, shape).reduce();
            } catch (IOException | InvalidRangeException ex) {
                error("Error reading NetCDF variable " + variable.getFullName() + " from " + nc.getLocation(), ex);
            }
            return null;
        }
    }

    void closeSource() {
        if (null != nc) {
            try {
                nc.close();
            } catch (IOException ex) {
                warning("Error closing NetCDF " + nc.getLocation(), ex);
            }
        }
    }

    public void clear() {
        tiles.clear();
        closeSource();
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
        int nxtile = ((tag + 1) % ntilex == 0) ? nx - ijktile[0] : nh;
        int nytile = ((tag / ntilex + 1) % ntiley == 0) ? ny - ijktile[1] : nh;
        int nztile = ((tag / (ntilex * ntiley) + 1) % ntilez == 0) ? nz - ijktile[2] : nv;

        Array tile = getTile(tag);

        // Since we apply reduce() on the array, the dimension of the tile may vary        
        if (nztile <= 1) {
            if (nytile <= 1) {
                if (nxtile <= 1) {
                    // dim z = 1 y = 1 x = 1
                    return tile.getDouble(0);
                } else {
                    // dim z = 1 y = 1 x > 1
                    return tile.getDouble(Math.min(i, nh * ntilex - 1) % nh + Math.max(i - nh * ntilex + 1, 0));
                }
            } else if (nxtile <= 1) {
                // dim z = 1 y > 1 x = 1
                return tile.getDouble(Math.min(j, nh * ntiley - 1) % nh + Math.max(j - nh * ntiley + 1, 0));
            } else {
                // dim z = 1 y > 1 x > 1
                return tile.getDouble(tile.getIndex().set(
                        Math.min(j, nh * ntiley - 1) % nh + Math.max(j - nh * ntiley + 1, 0),
                        Math.min(i, nh * ntilex - 1) % nh + Math.max(i - nh * ntilex + 1, 0)));
            }
        } else if (nytile <= 1) {
            if (nxtile <= 1) {
                // dim z > 1 y = 1 x = 1
                return tile.getDouble(Math.min(k, nv * ntilez - 1) % nv + Math.max(k - nv * ntilez + 1, 0));
            } else {
                // dim z > 1 y = 1 x > 1
                return tile.getDouble(tile.getIndex().set(
                        Math.min(k, nv * ntilez - 1) % nv + Math.max(k - nv * ntilez + 1, 0),
                        Math.min(i, nh * ntilex - 1) % nh + Math.max(i - nh * ntilex + 1, 0)
                ));
            }
        } else if (nxtile <= 1) {
            // dim z > 1 y > 1 x = 1
            return tile.getDouble(tile.getIndex().set(
                    Math.min(k, nv * ntilez - 1) % nv + Math.max(k - nv * ntilez + 1, 0),
                    Math.min(j, nh * ntiley - 1) % nh + Math.max(j - nh * ntiley + 1, 0)
            ));
        } else {
            // dim z > 1 y > 1 x > 1
            return tile.getDouble(tile.getIndex().set(
                    Math.min(k, nv * ntilez - 1) % nv + Math.max(k - nv * ntilez + 1, 0),
                    Math.min(j, nh * ntiley - 1) % nh + Math.max(j - nh * ntiley + 1, 0),
                    Math.min(i, nh * ntilex - 1) % nh + Math.max(i - nh * ntilex + 1, 0)));
        }
    }

    private int ijk2tag(int i, int j, int k) {
        return Math.min(i / nh, ntilex - 1) + ntilex * Math.min(j / nh, ntiley - 1) + ntilex * ntiley * Math.min(k / nv, ntilez - 1);
    }

    int[] tag2ijk(int tag) {
        return new int[]{nh * (tag % ntilex), nh * ((tag / ntilex) % ntiley), nv * ((tag / (ntilex * ntiley)) % ntilez)};
    }

    private Array getTile(int tag) {

        synchronized (tiles) {
            if (!tiles.containsKey(tag)) {
                tiles.putIfAbsent(tag, loadTile(tag));
            }
            return tiles.get(tag);
        }
    }

    Set<Integer> getTilesIndex() {
        return tiles.keySet();
    }

    public double getTimeStamp() {
        return timestamp;
    }

    void loadTiles(Set<Integer> tags) {

        ExecutorService pool = Executors.newWorkStealingPool();
        tags.forEach(
                (tag) -> {
                    pool.submit(new LoadTileTask(tag));
                }
        );
        pool.shutdown();

    }

    private class LoadTileTask implements Callable<Array> {

        private final int tag;

        LoadTileTask(int tag) {
            this.tag = tag;
        }

        @Override
        public Array call() throws Exception {
            return getTile(tag);
        }
    }
}
