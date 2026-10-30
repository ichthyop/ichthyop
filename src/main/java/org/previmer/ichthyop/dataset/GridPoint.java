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

/**
 * Location of a variable on a staggered (Arakawa C) grid. Each grid point
 * holds its offset, in grid index units, relative to the T point with the
 * same indexes: U(i) is at x = i + 0.5, V(j) at y = j + 0.5, W(k) at
 * z = k - 0.5 (k = 0 at the bottom) and F(i, j) at (i + 0.5, j + 0.5).
 *
 * A variable can be bound to a grid point by suffixing its name with
 * "@T", "@U", "@V", "@W" or "@F" (e.g. "vozocrtx@U").
 */
public enum GridPoint {

    T(0.d, 0.d, 0.d),
    U(0.5d, 0.d, 0.d),
    V(0.d, 0.5d, 0.d),
    W(0.d, 0.d, -0.5d),
    F(0.5d, 0.5d, 0.d);

    private static final char SEPARATOR = '@';

    private final double dx, dy, dz;

    GridPoint(double dx, double dy, double dz) {
        this.dx = dx;
        this.dy = dy;
        this.dz = dz;
    }

    public double dx() {
        return dx;
    }

    public double dy() {
        return dy;
    }

    public double dz() {
        return dz;
    }

    /**
     * Returns the variable name without the grid point suffix,
     * "vozocrtx@U" gives "vozocrtx".
     */
    public static String parseName(String spec) {
        int index = spec.lastIndexOf(SEPARATOR);
        return (index < 0) ? spec.trim() : spec.substring(0, index).trim();
    }

    /**
     * Returns the grid point given by the suffix of the variable name,
     * "vozocrtx@U" gives U, or null if the name has no suffix.
     *
     * @throws IllegalArgumentException if the suffix is not a grid point.
     */
    public static GridPoint parseGridPoint(String spec) {
        int index = spec.lastIndexOf(SEPARATOR);
        if (index < 0) {
            return null;
        }
        String suffix = spec.substring(index + 1).trim().toUpperCase();
        try {
            return GridPoint.valueOf(suffix);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Unknown grid point \"" + suffix + "\" in variable " + spec
                    + ". Expected one of T, U, V, W, F.");
        }
    }
}
