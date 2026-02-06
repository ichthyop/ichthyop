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

import java.util.HashMap;
import java.util.logging.Level;
import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.manager.TimeManager;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public abstract class AbstractDataset extends SimulationManagerAccessor implements IDataset {

    private DistanceGetter distGetter;

    private final String datasetKey;
    /*
     *
     */
    HashMap<String, RequiredVariable> requiredVariables;

    abstract void loadParameters();

    /** Set the method for distance calculation.
     * @param distGetter */
    public void setDistGetter(DistanceGetter distGetter) {
        this.distGetter = distGetter;
    }

    @Override
    public double get_dUx(double[] pGrid, double time) {
        return get_dUx(pGrid, time, true);
    }

    @Override
    public double get_dVy(double[] pGrid, double time) {
        return get_dVy(pGrid, time, true);
    }

    /** Get the method for distance calculation.
     * @return  */
    public DistanceGetter getDistGetter() {
        return this.distGetter;
    }

    public AbstractDataset() {
        datasetKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
        distGetter = (lat1, lon1, lat2, lon2) -> DatasetUtil.geodesicDistance(lat1, lon1, lat2, lon2);
    }

    public String getParameter(String key) {
        return getSimulationManager().getDatasetManager().getParameter(datasetKey, key);
    }

    public boolean findParameter(String key) {
        // Check whether the parameter can be retrieved
        try {
            getSimulationManager().getDatasetManager().getParameter(datasetKey, key);
        } catch (NullPointerException ex) {
            // Tue parameter does not exist
            return false;
        }
        // The parameter does exist
        return true;
    }

    @Override
    public Number get(String variableName, double[] pGrid, double time) {
        if (null != requiredVariables.get(variableName)) {
            return requiredVariables.get(variableName).get(pGrid, time);
        }
        return Float.NaN;
    }

    @Override
    public void requireVariable(String name, Class<?> requiredBy) {
        if (!requiredVariables.containsKey(name)) {
            requiredVariables.put(name, new RequiredVariable(name, requiredBy));
        } else {
            requiredVariables.get(name).addRequiredBy(requiredBy);
        }
    }

    public void clearRequiredVariables() {
        if (requiredVariables != null) {
            requiredVariables.clear();
        } else {
            requiredVariables = new HashMap<>();
        }
    }

    @Override
    public void removeRequiredVariable(String name, Class<?> requiredBy) {
        RequiredVariable var = requiredVariables.get(name);
        if (null != var) {
            if (var.getRequiredBy().size() > 1) {
                /* just remove the reference but dont remove the
                 variable because other classes might need it */
                var.getRequiredBy().remove(requiredBy);
            } else if (var.getRequiredBy().get(0).equals(requiredBy)) {
                requiredVariables.remove(name);
            }
        }
    }

    public void checkRequiredVariable(NetcdfFile nc) {
        for (RequiredVariable variable : requiredVariables.values()) {
            try {
                variable.checked(nc);
            } catch (NullPointerException | NumberFormatException ex) {
                requiredVariables.remove(variable.getName());
                StringBuilder msg = new StringBuilder();
                msg.append("Failed to read dataset variable ");
                msg.append(variable.getName());
                msg.append(" ==> ");
                msg.append(ex.toString());
                msg.append("\n");
                msg.append("Required by classes ");
                for (Class<?> aClass : variable.getRequiredBy()) {
                    msg.append(aClass.getCanonicalName());
                    msg.append(", ");
                }
                msg.append("\n");
                msg.append("Watch out, these classes might not work correctly.");
                getLogger().log(Level.WARNING, msg.toString(), ex);
            }
        }
    }

    boolean skipSorting() {
        try {
            return Boolean.valueOf(getParameter("skip_sorting"));
        } catch (NullPointerException ex ) {
            return false;
        }
    }

    int timeArrow() {
        return getSimulationManager().getParameterManager().getParameter("app.time", "time_arrow").equals(TimeManager.TimeDirection.FORWARD.toString()) ? 1 :-1;
    }

    boolean enhanced() {
        try {
            return Boolean.valueOf(getParameter("enhanced_mode"));
        } catch (NullPointerException ex ) {
            return true;
        }
    }

    public boolean isProjected() {
        return false;
    }

}
