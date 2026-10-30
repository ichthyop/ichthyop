/*
 *
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full
 * description, see the LICENSE file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package org.previmer.ichthyop.dataset;

import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 * A 2D or 3D field located on a given {@link GridPoint} of a
 * {@link StaggeredGrid}, stored at two consecutive time records, and
 * interpolated linearly in time and space at particle positions (T grid
 * coordinates).
 *
 * The values are copied into flat float arrays, in [k][j][i] order, once per
 * time record, so that interpolation does not create any object.
 *
 * Two treatments of the corners that are on land (or NaN, or out of the
 * grid) are available:
 * <ul>
 * <li>maskLand = true (tracers): they are discarded and the weights of the
 * remaining corners are normalized. Returns NaN if all corners are
 * discarded.</li>
 * <li>maskLand = false (velocities): they are counted as zero, so that the
 * velocity vanishes toward the coast.</li>
 * </ul>
 */
public class StaggeredField {

    /**
     * Factor applied to the value of a corner (i, j, k), for instance
     * 1 / e1u to convert a velocity in grid units. A NaN factor discards
     * the corner.
     */
    public interface CornerScale {

        double get(int i, int j, int k);
    }

    private final GridPoint gridPoint;
    private final StaggeredGrid grid;
    private final boolean maskLand;
    private float[] data_tp0, data_tp1;
    private int ni, nj, nk;
    private boolean is3D;

    public StaggeredField(GridPoint gridPoint, StaggeredGrid grid, boolean maskLand) {
        this.gridPoint = gridPoint;
        this.grid = grid;
        this.maskLand = maskLand;
    }

    public GridPoint getGridPoint() {
        return gridPoint;
    }

    /**
     * Stores a new time record, the current one becoming the previous one.
     *
     * @param array a [j][i] or a [k][j][i] array, k = 0 at the bottom.
     */
    public void push(Array array) {
        int[] shape = array.getShape();
        is3D = (shape.length == 3);
        nk = is3D ? shape[0] : 1;
        nj = shape[shape.length - 2];
        ni = shape[shape.length - 1];
        data_tp0 = data_tp1;
        data_tp1 = (float[]) array.get1DJavaArray(DataType.FLOAT);
    }

    public double interpolate(double[] pGrid, double frac) {
        return interpolate(pGrid, frac, null);
    }

    /**
     * Interpolates the field at the given position.
     *
     * @param pGrid the position in T grid coordinates (x, y[, z])
     * @param frac the time fraction between the previous (0) and the current
     * (1) time records
     * @param scale factor applied to the corner values, may be null
     */
    public double interpolate(double[] pGrid, double frac, CornerScale scale) {

        float[] tp1 = data_tp1;
        float[] tp0 = (null == data_tp0) ? tp1 : data_tp0;

        // Position in the grid of the variable
        double x = grid.xTore(pGrid[0]) - gridPoint.dx();
        double y = pGrid[1] - gridPoint.dy();
        int i = (int) Math.floor(x);
        int j = (int) Math.floor(y);
        double fx = x - i;
        double fy = y - j;

        int k, nkk;
        double fz;
        if (!is3D) {
            k = 0;
            nkk = 1;
            fz = 0.d;
        } else if (pGrid.length < 3) {
            // 2D position, takes the surface level
            k = nk - 1;
            nkk = 1;
            fz = 0.d;
        } else {
            // Clamped to the T levels, then moved to the levels of the variable
            int nkT = (gridPoint == GridPoint.W) ? nk - 1 : nk;
            double z = Math.max(0.d, Math.min(pGrid[2], nkT - 1.00001d)) - gridPoint.dz();
            k = (int) Math.floor(z);
            nkk = 2;
            fz = z - k;
        }

        double value = 0.d;
        double weight = 0.d;
        for (int kk = 0; kk < nkk; kk++) {
            int kc = k + kk;
            double wz = (nkk == 1) ? 1.d : (kk == 0 ? 1.d - fz : fz);
            for (int jj = 0; jj < 2; jj++) {
                int jc = j + jj;
                double wzy = wz * (jj == 0 ? 1.d - fy : fy);
                for (int ii = 0; ii < 2; ii++) {
                    double w = wzy * (ii == 0 ? 1.d - fx : fx);
                    int ic = grid.xIndex(i + ii);
                    boolean valid = (jc >= 0) && (jc < nj) && (ic >= 0) && (ic < ni);
                    double v = Double.NaN;
                    if (valid && maskLand) {
                        valid = is3D
                                ? grid.isInWater(gridPoint, ic, jc, kc)
                                : grid.isInWater(gridPoint, ic, jc);
                    }
                    if (valid) {
                        int index = (kc * nj + jc) * ni + ic;
                        v = (1.d - frac) * tp0[index] + frac * tp1[index];
                        if (null != scale) {
                            v *= scale.get(ic, jc, kc);
                        }
                        valid = !Double.isNaN(v);
                    }
                    if (valid) {
                        value += w * v;
                        weight += w;
                    } else if (!maskLand) {
                        weight += w;
                    }
                }
            }
        }

        if (weight > 0) {
            return value / weight;
        }
        return maskLand ? Double.NaN : 0.d;
    }
}
