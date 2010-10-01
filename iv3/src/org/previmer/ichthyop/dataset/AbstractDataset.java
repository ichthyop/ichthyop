/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.dataset;

import java.util.HashMap;
import java.util.logging.Level;
import org.previmer.ichthyop.event.NextStepListener;
import org.previmer.ichthyop.arch.IDataset;
import org.previmer.ichthyop.SimulationManagerAccessor;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public abstract class AbstractDataset extends SimulationManagerAccessor implements IDataset, NextStepListener {

    private String datasetKey;
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

    public Number get(String variableName, double[] pGrid, double time) {
        if (null != requiredVariables.get(variableName)) {
            return requiredVariables.get(variableName).get(pGrid, time);
        }
        return Float.NaN;
    }

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
            } catch (Exception ex) {
                requiredVariables.remove(variable.getName());
                StringBuffer msg = new StringBuffer();
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
                getLogger().log(Level.WARNING, msg.toString(), ex);
            }
        }
    }
}
