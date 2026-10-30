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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import ucar.ma2.Array;
import ucar.ma2.DataType;

/**
 * Tests of the interpolation of variables on staggered grids.
 */
public class TestStaggeredField {

    private static final double EPS = 1e-5;
    private static final int NI = 6, NJ = 5, NK = 4;

    /** Simple grid, all in water except the given T point, with optional zonal cyclicity. */
    private static class Grid implements StaggeredGrid {

        boolean cyclic;
        int iLand = -1, jLand = -1, kLand = -1;

        @Override
        public double xTore(double x) {
            if (cyclic) {
                if (x < -0.5) {
                    return x + NI;
                } else if (x >= NI - 0.5) {
                    return x - NI;
                }
            }
            return x;
        }

        @Override
        public int xIndex(int i) {
            if (cyclic) {
                if (i < 0) {
                    return i + NI;
                } else if (i >= NI) {
                    return i - NI;
                }
            }
            return i;
        }

        @Override
        public boolean isInWater(GridPoint gridPoint, int i, int j, int k) {
            return !(i == iLand && j == jLand && k == kLand);
        }

        @Override
        public boolean isInWater(GridPoint gridPoint, int i, int j) {
            return isInWater(gridPoint, i, j, NK - 1);
        }
    }

    private static double linear(double x, double y, double z) {
        return 2 * x + 3 * y + 5 * z;
    }

    /** Field f = linear(x, y, z) + shift sampled at the positions of the grid point. */
    private static Array sample(GridPoint gridPoint, double shift) {
        int nk = (gridPoint == GridPoint.W) ? NK + 1 : NK;
        float[] data = new float[nk * NJ * NI];
        for (int k = 0; k < nk; k++) {
            for (int j = 0; j < NJ; j++) {
                for (int i = 0; i < NI; i++) {
                    data[(k * NJ + j) * NI + i] = (float) (shift
                            + linear(i + gridPoint.dx(), j + gridPoint.dy(), k + gridPoint.dz()));
                }
            }
        }
        return Array.factory(DataType.FLOAT, new int[]{nk, NJ, NI}, data);
    }

    @Test
    public void linearFieldIsExactOnAllGridPoints() {
        double[] pGrid = new double[]{2.3, 1.7, 1.4};
        for (GridPoint gridPoint : GridPoint.values()) {
            StaggeredField field = new StaggeredField(gridPoint, new Grid(), true);
            field.push(sample(gridPoint, 0));
            assertEquals(linear(2.3, 1.7, 1.4), field.interpolate(pGrid, 1.d), EPS, gridPoint.toString());
        }
    }

    @Test
    public void timeInterpolation() {
        StaggeredField field = new StaggeredField(GridPoint.U, new Grid(), true);
        field.push(sample(GridPoint.U, 0));
        field.push(sample(GridPoint.U, 10));
        double[] pGrid = new double[]{2.3, 1.7, 1.4};
        assertEquals(linear(2.3, 1.7, 1.4) + 2.5, field.interpolate(pGrid, 0.25), EPS);
    }

    @Test
    public void landCornersAreDiscardedForTracers() {
        Grid grid = new Grid();
        grid.iLand = 2;
        grid.jLand = 1;
        grid.kLand = 1;
        float[] data = new float[NK * NJ * NI];
        java.util.Arrays.fill(data, 7.f);
        data[(1 * NJ + 1) * NI + 2] = 1000.f;
        StaggeredField field = new StaggeredField(GridPoint.T, grid, true);
        field.push(Array.factory(DataType.FLOAT, new int[]{NK, NJ, NI}, data));
        assertEquals(7.d, field.interpolate(new double[]{2.3, 1.7, 1.4}, 1.d), EPS);

        // All corners on land
        grid.iLand = 0;
        StaggeredField field2D = new StaggeredField(GridPoint.T, new Grid() {
            @Override
            public boolean isInWater(GridPoint gridPoint, int i, int j) {
                return false;
            }
        }, true);
        field2D.push(Array.factory(DataType.FLOAT, new int[]{NJ, NI}, new float[NJ * NI]));
        assertTrue(Double.isNaN(field2D.interpolate(new double[]{2.3, 1.7}, 1.d)));
    }

    @Test
    public void nanCornersCountAsZeroForVelocities() {
        float[] data = new float[NK * NJ * NI];
        java.util.Arrays.fill(data, 1.f);
        // U corner (i = 2, j = 2, k = 1) of the particle at (2.7, 2.0, 1.0), weight 0.8
        data[(1 * NJ + 2) * NI + 2] = Float.NaN;
        StaggeredField field = new StaggeredField(GridPoint.U, new Grid(), false);
        field.push(Array.factory(DataType.FLOAT, new int[]{NK, NJ, NI}, data));
        assertEquals(0.2d, field.interpolate(new double[]{2.7, 2.0, 1.0}, 1.d), EPS);
    }

    @Test
    public void zonalCyclicity() {
        Grid grid = new Grid();
        grid.cyclic = true;
        float[] data = new float[NJ * NI];
        for (int j = 0; j < NJ; j++) {
            for (int i = 0; i < NI; i++) {
                data[j * NI + i] = i;
            }
        }
        // T point between the last column and the first one
        StaggeredField fieldT = new StaggeredField(GridPoint.T, grid, true);
        fieldT.push(Array.factory(DataType.FLOAT, new int[]{NJ, NI}, data));
        assertEquals(2.5d, fieldT.interpolate(new double[]{NI - 0.5, 2.0}, 1.d), EPS);

        // U point west of the first U column: between U(NI - 1) and U(0)
        StaggeredField fieldU = new StaggeredField(GridPoint.U, grid, true);
        fieldU.push(Array.factory(DataType.FLOAT, new int[]{NJ, NI}, data));
        // x = 0.3 is at -0.2 in U layout: 0.2 * U(NI - 1) + 0.8 * U(0)
        assertEquals(0.2d * (NI - 1), fieldU.interpolate(new double[]{0.3, 2.0}, 1.d), EPS);
    }

    @Test
    public void parseVariableName() {
        assertEquals("vozocrtx", GridPoint.parseName("vozocrtx@U"));
        assertEquals(GridPoint.U, GridPoint.parseGridPoint("vozocrtx@u"));
        assertEquals("temp", GridPoint.parseName("temp"));
        assertNull(GridPoint.parseGridPoint("temp"));
        assertThrows(IllegalArgumentException.class, () -> GridPoint.parseGridPoint("temp@X"));
    }
}
