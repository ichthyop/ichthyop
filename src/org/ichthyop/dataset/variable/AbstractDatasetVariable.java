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
import java.util.Calendar;
import java.util.HashMap;
import org.ichthyop.grid.IGrid;

/**
 *
 * @author pverley
 */
public abstract class AbstractDatasetVariable implements IVariable {

    protected final String dataset_prefix;
    protected final IGrid grid;
    protected final NetcdfTiledArray[] stack;
    protected final int nlayer;
    protected final Calendar calendar;
    protected final double t0;
    private final HashMap<Double, Double> times = new HashMap();
    // constants
    private final int IDW_POWER = 2;
    private final int IDW_RADIUS = 1;

    public abstract void init(double t0, int time_arrow) throws IOException;

    public abstract void update(double currenttime, int time_arrow) throws IOException;

    public AbstractDatasetVariable(String dataset_prefix, int nlayer, IGrid grid, Calendar calendar, double t0) {
        this.dataset_prefix = dataset_prefix;
        this.nlayer = nlayer;
        this.grid = grid;
        this.calendar = calendar;
        this.t0 = t0;
        stack = new NetcdfTiledArray[nlayer];
    }

    /**
     * Returns the stack array.
     *
     * @return TiledVariable[] stack
     */
    public NetcdfTiledArray[] getStack() {
        return this.stack;
    }

    /**
     * Returns the grid object.
     *
     * @return Grid object
     */
    public IGrid getGrid() {
        return this.grid;
    }

    protected boolean updateNeeded(double time, int time_arrow) {
        return (time_arrow * time >= time_arrow * stack[1].getTimeStamp());
    }

    protected void update(NetcdfTiledArray variable) {

        times.clear();
        // clear first variable of the stack
        if (null != stack[0]) {
            stack[0].clear();
        }
        // cascade down the variables in the stack
        for (int istack = 0; istack < nlayer - 1; istack++) {
            stack[istack] = stack[istack + 1];
        }
        // update the last variable of the stack
        stack[nlayer - 1] = variable;
        if (null != stack[0] && null != variable) {
            stack[nlayer - 1].loadTiles(stack[0].getTilesIndex());
        }
    }

    @Override
    public double getDouble(double[] pGrid, double seconds) {

        if (null != calendar && !times.containsKey(seconds)) {
            long millis = (long) (t0 + seconds) * 1000L;
            calendar.setTimeInMillis(millis);
            times.putIfAbsent(seconds, calendar.getTimeInMillis() / 1000.d);
            //System.out.println(seconds + " " + times.get(seconds));
        }
        return interpolateIDW(pGrid, times.containsKey(seconds) ? times.get(seconds) : Double.NaN, IDW_RADIUS, IDW_POWER);
    }

    @Override
    public double getDouble(int i, int j, int k) {
        return stack[0].getDouble(i, j, k);
    }

    private double interpolateTime(int i, int j, int k, double xt) {

        double value = 0;
        if (xt > 0 && stack.length > 1) {
            value = (1.d - xt) * stack[0].getDouble(i, j, k) + xt * stack[1].getDouble(i, j, k);
        } else {
            value = stack[0].getDouble(i, j, k);
        }
        return value;
    }

    // interpolate Inverse Distance Weight
    private double interpolateIDW(double[] pGrid, double time, int r, int p) {

        if (!grid.isInWater(pGrid)) {
            return Double.NaN;
        }
        
        double value = 0.d;
        boolean coast = grid.isCloseToCost(pGrid);

        int n[] = coast ? new int[]{0, 1} : new int[]{1 - r, 1 + r};
        int i = coast ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = coast ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        int k = coast ? (int) Math.round(pGrid[2]) : (int) pGrid[2];
        double dt = stack.length > 1
                ? Math.abs((time - stack[0].getTimeStamp()) / (stack[1].getTimeStamp() - stack[0].getTimeStamp()))
                : 0.d;
        double CO = 0.d;

        if (Double.isInfinite(weight(pGrid, new int[]{i, j, k}, p))) {
            // pGrid falls on a grid point
            return interpolateTime(grid.continuity(i), j, k, dt);
        } else {
            for (int ii = n[0]; ii < n[1]; ii++) {
                for (int jj = n[0]; jj < n[1]; jj++) {
                    for (int kk = n[0]; kk < n[1]; kk++) {
                        int ci = grid.continuity(i + ii);
                        if (isOut(ci, j + jj, k + kk)) {
                            continue;
                        }
                        double co = weight(pGrid, new int[]{ci, j + jj, k + kk}, p);
                        double v = interpolateTime(ci, j + jj, k + kk, dt);
                        if (!Double.isNaN(v)) {
                            CO += co;
                            value += v * co;
                        }
                    }
                }
            }
            if (CO != 0) {
                return value / CO;
            } else {
                // NaN values only in the interpolation
                return Double.NaN;
            }
        }
    }

    public boolean isOut(int i, int j, int k) {
        return i < 0 || j < 0 || k < 0
                || i > (grid.get_nx() - 1) || j > (grid.get_ny() - 1) || k > (grid.get_nz() - 1);
    }

    private double weight(double[] xyz, int[] ijk, int power) {
        double distance = 0.d;
        for (int n = 0; n < xyz.length; n++) {
            distance += Math.abs(Math.pow(xyz[n] - ijk[n], power));
        }
        return 1.d / distance;
    }
}
