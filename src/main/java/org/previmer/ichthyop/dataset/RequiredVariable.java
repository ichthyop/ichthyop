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

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.manager.SimulationManager;
import ucar.ma2.Array;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class RequiredVariable {

    private IDataset dataset;
    private String name;
    private Array array_tp0, array_tp1;
    private double time_tp1, dt_dataset;
    private boolean isUnlimited;
    private List<Class<?>> requiredByList;

    private FvcomDataset fvcom;

    private interface Getter {
        public Number get(double[] pGrid, double time);
    }

    Getter getter;

    public RequiredVariable(String name, Class<?> requiredBy) {
        this.name = name;
        this.dataset = SimulationManager.getInstance().getDataset();
        if(this.dataset instanceof FvcomDataset) {
            fvcom = (FvcomDataset) this.dataset;
            getter = (pGrid, time) -> getFVCOM(pGrid, time);
        } else {
            getter = (pGrid, time) -> getStandard(pGrid, time);
        }

        requiredByList = new ArrayList<>();
        requiredByList.add(requiredBy);
    }

    public RequiredVariable(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public void setUnlimited(boolean unlim) {
        this.isUnlimited = unlim;
    }

    public void addRequiredBy(Class<?> requiredBy) {
        requiredByList.add(requiredBy);
    }

    public List<Class<?>> getRequiredBy() {
        return requiredByList;
    }

    private SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    public boolean checked(NetcdfFile nc) throws NumberFormatException, NullPointerException {
        Variable variable = nc.findVariable(name);
        if (variable != null) {
            if (!variable.getDataType().isNumeric()) {
                throw new NumberFormatException("Variable " + name + " is not a numeric variable");
            }
            isUnlimited = variable.isUnlimited();
            boolean is3D = getSimulationManager().getDataset().is3D();
            switch (variable.getShape().length) {
                case 4:
                    if ((variable.getShape()[1]!= 1) & !is3D) {
                        throw new UnsupportedOperationException("2D simulation cannot deal with 3D variable " + name);
                    }
                    break;
                case 3:
                    if (!isUnlimited && !is3D) {
                        throw new UnsupportedOperationException("2D simulation cannot deal with 3D variable " + name);
                    }
                    break;
            }
            return true;
        } else {
            throw new NullPointerException("Variable " + name + " not found.");
        }
    }

    public void nextStep(Array array_tp1, double time_tp1, double dt_dataset) {

        this.time_tp1 = time_tp1;
        this.dt_dataset = dt_dataset;
        array_tp0 = this.array_tp1;
        this.array_tp1 = array_tp1;
    }

    public Number getFVCOM(double[] pGrid, double time) {

         // getting the value at the T-cell to which the particle belongs
         double z = pGrid[2];
         int kz = (int) Math.floor(z);
         double dist = 1;

        double[][] tracer_0 = fvcom.getTracer0(name);
        double[][] dT_dX = fvcom.getDtDx(name);
        double[][] dT_dY = fvcom.getDtDy(name);

        int iTriangle = fvcom.findTriangle(pGrid);
        double xB = fvcom.getXBarycenter(iTriangle);
        double yB = fvcom.getYBarycenter(iTriangle);
        double dX = pGrid[0] - xB;
        double dY = pGrid[1] - yB;

        double output_kz = tracer_0[kz][iTriangle] + dT_dX[kz][iTriangle] * dX + dT_dY[kz][iTriangle] * dY;
        double output_kzp1 = 0;

        if (z >= 0.5 || z <= fvcom.getNLayer() + 0.5) {
            // if the depth of the particle is between two T layers, we recover the value
            // at the T layer which is below
            output_kzp1 = tracer_0[kz + 1][iTriangle] + dT_dX[kz + 1][iTriangle] * dX + dT_dY[kz + 1][iTriangle] * dY;
            dist = kz + 0.5 - z;
        }

        double output = dist * output_kz + ( 1 - dist) *output_kzp1;

        return output;

    }

    public  Number get(double[] pGrid, double time) {
        return getter.get(pGrid, time);
    }


    public Number getStandard(double[] pGrid, double time) {

        int[] origin;
        int[] shape;
        int n = dataset.isCloseToCost(pGrid) ? 1 : 2;
        int i = (n == 1) ? (int) Math.round(pGrid[0]) : (int) pGrid[0];
        int j = (n == 1) ? (int) Math.round(pGrid[1]) : (int) pGrid[1];
        double dx = pGrid[0] - (double) i;
        double dy = pGrid[1] - (double) j;
        double kz, dz;
        int k;
        if (isUnlimited) {
            switch (array_tp0.getShape().length) {
                case 3:
                    kz = Math.max(0.d, Math.min(pGrid[2], (double) dataset.get_nz() - 1.00001f));
                    k = (int) kz;
                    dz = kz - (double) k;
                    shape = new int[]{2, 2, 2};
                    origin = new int[]{k, j, i};
                    try {
                        double value_t0 = interp3D(array_tp0.section(origin, shape), dx, dy, dz, n);
                        double value_t1 = interp3D(array_tp1.section(origin, shape), dx, dy, dz, n);
                        return interpTime(value_t0, value_t1, time);
                    } catch (InvalidRangeException ex) {
                        Logger.getLogger(RequiredVariable.class.getName()).log(Level.SEVERE, null, ex);
                        return Float.NaN;
                    }
                case 2:
                    shape = new int[]{2, 2};
                    origin = new int[]{j, i};
                    try {
                        double value_t0 = interp2D(array_tp0.section(origin, shape), dx, dy, n);
                        double value_t1 = interp2D(array_tp1.section(origin, shape), dx, dy, n);
                        return interpTime(value_t0, value_t1, time);
                    } catch (InvalidRangeException ex) {
                        Logger.getLogger(RequiredVariable.class.getName()).log(Level.SEVERE, null, ex);
                        return Float.NaN;
                    }
            }
        } else {
            switch (array_tp0.getShape().length) {
                case 3:
                    kz = Math.max(0.d, Math.min(pGrid[2], (double) dataset.get_nz() - 1.00001f));
                    k = (int) kz;
                    dz = kz - (double) k;
                    shape = new int[]{2, 2, 2};
                    origin = new int[]{k, j, i};
                    try {
                        return interp3D(array_tp0.section(origin, shape), dx, dy, dz, n);
                    } catch (InvalidRangeException ex) {
                        Logger.getLogger(RequiredVariable.class.getName()).log(Level.SEVERE, null, ex);
                        return Float.NaN;
                    }
                case 2:
                    shape = new int[]{2, 2};
                    origin = new int[]{j, i};
                    try {
                        return interp2D(array_tp0.section(origin, shape), dx, dy, n);
                    } catch (InvalidRangeException ex) {
                        Logger.getLogger(RequiredVariable.class.getName()).log(Level.SEVERE, null, ex);
                        return Float.NaN;
                    }

            }
        }
        return Float.NaN;
    }

    private double interpTime(double value_t0, double value_t1, double time) {
        double frac = (dt_dataset - Math.abs(time_tp1 - time)) / dt_dataset;
        return (1.d - frac) * value_t0 + frac * value_t1;
    }

    private double interp2D(Array array, double dx, double dy, int n) {
        double value = 0.d;
        double CO = 0.d;

        for (int jj = 0; jj < n; jj++) {
            for (int ii = 0; ii < n; ii++) {
                double co = Math.abs((1.d - (double) ii - dx)
                        * (1.d - (double) jj - dy));
                CO += co;
                value += array.getFloat(array.getIndex().set(jj, ii)) * co;
            }
        }

        if (CO != 0) {
            value /= CO;
        }
        return value;
    }

    private double interp3D(Array array, double dx, double dy, double dz, int n) {
        double value = 0.d;
        double CO = 0.d;
        for (int kk = 0; kk < 2; kk++) {
            for (int jj = 0; jj < n; jj++) {
                for (int ii = 0; ii < n; ii++) {
                    double co = Math.abs((1.d - (double) ii - dx)
                            * (1.d - (double) jj - dy)
                            * (1.d - (double) kk - dz));
                    CO += co;
                    value += array.getFloat(array.getIndex().set(kk, jj, ii)) * co;
                }
            }
        }
        if (CO != 0) {
            value /= CO;
        }
        return value;
    }
}
