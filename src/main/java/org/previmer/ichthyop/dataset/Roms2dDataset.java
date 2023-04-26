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

import java.io.IOException;
import java.util.List;
import org.previmer.ichthyop.dataset.MarsCommon.ErrorMessage;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.io.IOTools;
import static org.previmer.ichthyop.io.IOTools.isFile;
import ucar.ma2.Array;
import ucar.ma2.Index;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public class Roms2dDataset extends RomsCommon {

    /**
     * Zonal component of the velocity field at current time
     */
    static float[][] u_tp0;
    /**
     * Zonal component of the velocity field at time t + dt
     */
    static float[][] u_tp1;
    /**
     * Meridional component of the velocity field at current time
     */
    static float[][] v_tp0;
    /**
     * Meridional component of the velocity field at time t + dt
     */
    static float[][] v_tp1;
    private List<String> ncfiles;
    private int ncindex;

    @Override
    public boolean is3D() {
        return false;
    }

    @Override
    public double depth2z(double x, double y, double depth) {
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public double z2depth(double x, double y, double z) {
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public double get_dWz(double[] pGrid, double time) {
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {
        double dv = 0.d;
        double ix, jy;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (n == 1) ? (int) Math.round(ix) : (int) ix;
        int j = (int) Math.round(jy);
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double CO = 0.d;
        double co;
        double x;

        for (int jj = 0; jj < 2; jj++) {
            for (int ii = 0; ii < n; ii++) {
                co = Math.abs((1.d - (double) ii - dx)
                        * (.5d - (double) jj - dy));
                CO += co;
                x = (1.d - x_euler) * v_tp0[j + jj - 1][i + ii] + x_euler * v_tp1[j + jj - 1][i + ii];
                if (!Double.isNaN(x)) {
                    dv += .5d * x * co * (pn[Math.max(j + jj - 1, 0)][i + ii] + pn[j + jj][i + ii]);
                }

            }
        }
        if (CO != 0) {
            dv /= CO;
        }
        return dv;
    }

    @Override
    public double get_dUx(double[] pGrid, double time) {

        double du = 0.d;
        double ix, jy;
        int n = isCloseToCost(pGrid) ? 1 : 2;
        ix = pGrid[0];
        jy = pGrid[1];

        double x_euler = (dt_HyMo - Math.abs(time_tp1 - time)) / dt_HyMo;
        int i = (int) Math.round(ix);
        int j = (n == 1) ? (int) Math.round(jy) : (int) jy;
        double dx = ix - (double) i;
        double dy = jy - (double) j;
        double CO = 0.d;
        double co;
        double x;
        for (int ii = 0; ii < 2; ii++) {
            for (int jj = 0; jj < n; jj++) {

                co = Math.abs((.5d - (double) ii - dx)
                        * (1.d - (double) jj - dy));
                CO += co;
                x = (1.d - x_euler) * u_tp0[j + jj][i + ii - 1] + x_euler * u_tp1[j + jj][i + ii - 1];
                if (!Double.isNaN(x)) {
                    du += .5d * x * co * (pm[j + jj][Math.max(i + ii - 1, 0)] + pm[j + jj][i + ii]);
                }
            }
        }
        if (CO != 0) {
            du /= CO;
        }
        return du;
    }

    @Override
    public int get_nz() {
        throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();
        //Logger.getAnonymousLogger().info("set fields at time " + time);
        int time_arrow = (int) Math.signum(e.getSource().get_dt());

        if (time_arrow * time < time_arrow * time_tp1) {
            return;
        }

        u_tp0 = u_tp1;
        v_tp0 = v_tp1;
        rank += time_arrow;
        if (rank > (nbTimeRecords - 1) || rank < 0) {
            ncindex = DatasetUtil.next(ncfiles, ncindex, time_arrow);
            ncIn = DatasetUtil.openFile(ncfiles.get(ncindex), true);
            readTimeLength();
            rank = (1 - time_arrow) / 2 * (nbTimeRecords - 1);
        }
        setAllFieldsTp1AtTime(rank);
    }

    @Override
    void setAllFieldsTp1AtTime(int rank) throws Exception {

        getLogger().info("Reading NetCDF variables...");

        int[] origin = new int[]{rank, jpo, ipo};
        double time_tp0 = time_tp1;
        Array arr;
        Index index;

        try {
            arr = ncIn.findVariable(strU).read(origin, new int[]{1, ny, (nx - 1)}).reduce();
            u_tp1 = new float[ny][nx - 1];
            index = arr.getIndex();
            for (int j = 0; j < ny; j++) {
                for (int i = 0; i < nx - 1; i++) {
                    index.set(j, i);
                    u_tp1[j][i] = arr.getFloat(index);
                }
            }
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading dataset U velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        try {
            arr = ncIn.findVariable(strV).read(origin, new int[]{1, (ny - 1), nx}).reduce();
            v_tp1 = new float[ny - 1][nx];
            index = arr.getIndex();
            for (int j = 0; j < ny - 1; j++) {
                for (int i = 0; i < nx; i++) {
                    index.set(j, i);
                    v_tp1[j][i] = arr.getFloat(index);
                }
            }
        } catch (IOException | InvalidRangeException ex) {
            IOException ioex = new IOException("Error reading dataset V velocity variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        try {
            time_tp1 = DatasetUtil.timeAtRank(ncIn, strTime, rank);
        } catch (IOException ex) {
            IOException ioex = new IOException("Error reading dataset time variable. " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        dt_HyMo = Math.abs(time_tp1 - time_tp0);
        for (RequiredVariable variable : requiredVariables.values()) {
            variable.nextStep(readVariable(ncIn, variable.getName(), rank), time_tp1, dt_HyMo);
        }
    }

    @Override
    public Array readVariable(NetcdfFile nc, String name, int rank) throws Exception {
        Variable variable = nc.findVariable(name);
        int[] origin = null, shape = null;
        switch (variable.getShape().length) {
            case 2:
                origin = new int[]{jpo, ipo};
                shape = new int[]{ny, nx};
                break;
            case 3:
                origin = new int[]{rank, jpo, ipo};
                shape = new int[]{1, ny, nx};
                break;
            default:
                throw new UnsupportedOperationException(ErrorMessage.NOT_IN_2D.message());

        }

        return variable.read(origin, shape).reduce();
    }

    @Override
    void openDataset() throws Exception {

        ncfiles = DatasetUtil.list(getParameter("input_path"), getParameter("file_filter"));
        if (!skipSorting()) {
            DatasetUtil.sort(ncfiles, strTime, timeArrow());
        }
        ncIn = DatasetUtil.openFile(ncfiles.get(0), true);
        readTimeLength();

        try {
            if (!getParameter("grid_file").isEmpty()) {
                String path = IOTools.resolveFile(getParameter("grid_file"));
                if (!isFile(path)) {
                    throw new IOException("{Dataset} " + getParameter("grid_file") + " is not a valid file.");
                }
                gridFile = path;
            } else {
                gridFile = ncIn.getLocation();
            }
        } catch (NullPointerException ex) {
            gridFile = ncIn.getLocation();
        }
    }

    @Override
    void setOnFirstTime() throws Exception {
        double t0 = getSimulationManager().getTimeManager().get_tO();
        ncindex = DatasetUtil.index(ncfiles, t0, timeArrow(), strTime);
        ncIn = DatasetUtil.openFile(ncfiles.get(ncindex), true);
        readTimeLength();
        rank = DatasetUtil.rank(t0, ncIn, strTime, timeArrow());
        time_tp1 = t0;
    }
}
