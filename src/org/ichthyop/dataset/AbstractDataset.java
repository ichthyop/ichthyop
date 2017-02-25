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

import java.util.HashMap;
import java.util.logging.Level;
import org.ichthyop.event.NextStepListener;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.manager.TimeManager;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public abstract class AbstractDataset extends IchthyopLinker implements IDataset, NextStepListener {
    
    private final String datasetKey;
    /*
     *
     */
    HashMap<String, RequiredVariable> requiredVariables;
    
    abstract void loadParameters();
    
    public AbstractDataset() {
        datasetKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
    }
    
    public String getParameter(String key) {
        return getSimulationManager().getDatasetManager().getParameter(datasetKey, key);
    }
    
    public boolean isNull(String key) {
        return getSimulationManager().getParameterManager().isNull(datasetKey + "." + key);
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
    public void requireVariable(String name, Class requiredBy) {
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
            requiredVariables = new HashMap();
        }
    }
    
    @Override
    public void removeRequiredVariable(String name, Class requiredBy) {
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
                for (Class aClass : variable.getRequiredBy()) {
                    msg.append(aClass.getCanonicalName());
                    msg.append(", ");
                }
                msg.append("\n");
                msg.append("Watch out, these classes might not work correctly.");
                warning(msg.toString(), ex);
            }
        }
    }
    
    boolean skipSorting() {
        try {
            return Boolean.valueOf(getParameter("skip_sorting"));
        } catch (NullPointerException ex) {
            return false;
        }
    }
    
    int timeArrow() {
        return getSimulationManager().getParameterManager().getString("app.time.time_arrow").equals(TimeManager.TimeDirection.FORWARD.toString()) ? 1 : -1;
    }
    
    boolean enhanced() {
        try {
            return Boolean.valueOf(getParameter("enhanced_mode"));
        } catch (NullPointerException ex) {
            return true;
        }
    }
}
