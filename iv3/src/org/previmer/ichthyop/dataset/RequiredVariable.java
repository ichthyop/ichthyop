/*
 *  Copyright (C) 2010 Philippe Verley <philippe dot verley at ird dot fr>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.dataset;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.arch.IDataset;
import org.previmer.ichthyop.arch.ISimulationManager;
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
    private List<Class> requiredByList;

    public RequiredVariable(String name, Class requiredBy) {
        this.name = name;
        this.dataset = SimulationManager.getInstance().getDataset();
        requiredByList = new ArrayList();
        requiredByList.add(requiredBy);
    }

    public RequiredVariable(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public void addRequiredBy(Class requiredBy) {
        requiredByList.add(requiredBy);
    }

    public List<Class> getRequiredBy() {
        return requiredByList;
    }

    private ISimulationManager getSimulationManager() {
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
                    if (!is3D) {
                        throw new UnsupportedOperationException("2D simulation cannot deal with 3D variable " + name);
                    }
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

    public void nextStep(NetcdfFile nc, int rank, int ip0, int jp0, double time_tp1, double dt_dataset) {

        this.time_tp1 = time_tp1;
        this.dt_dataset = dt_dataset;

        Variable variable = nc.findVariable(name);
        int[] origin = null, shape = null;
        array_tp0 = array_tp1;
        switch (variable.getShape().length) {
            case 4:
                origin = new int[]{rank, 0, jp0, ip0};
                shape = new int[]{1, dataset.get_nz(), dataset.get_ny(), dataset.get_nx()};
                break;
            case 2:
                origin = new int[]{jp0, ip0};
                shape = new int[]{dataset.get_ny(), dataset.get_nx()};
                break;
            case 3:
                if (!isUnlimited) {
                    origin = new int[]{0, jp0, ip0};
                    shape = new int[]{dataset.get_nz(), dataset.get_ny(), dataset.get_nx()};
                } else {
                    origin = new int[]{rank, jp0, ip0};
                    shape = new int[]{1, dataset.get_ny(), dataset.get_nx()};
                }
                break;
        }
        try {
            array_tp1 = variable.read(origin, shape).reduce();
        } catch (IOException ex) {
            Logger.getLogger(RequiredVariable.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(RequiredVariable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public Number get(double[] pGrid, double time) {

        int[] origin = null;
        int[] shape = null;
        int i = (int) pGrid[0];
        int j = (int) pGrid[1];
        int n = dataset.isCloseToCost(pGrid) ? 1 : 2;
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
