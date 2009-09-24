/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.action;

import fr.ird.ichthyop.*;
import fr.ird.ichthyop.manager.ParameterManager;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.IAction;
import fr.ird.ichthyop.io.XParameter;
import java.util.ArrayList;

/**
 *
 * @author pverley
 */
public abstract class AbstractAction implements IAction {

    private ParameterManager parameterManager = new ParameterManager(this.getClass());
    private String actionKey;

    public AbstractAction() {
        actionKey = this.getProperty("action.key");
    }

    public String getParameter(String name) {
        return parameterManager.getValue(name);
    }

    public String getProperty(String name) {
        return parameterManager.getProperty(name);
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }

    public boolean isEnabled() {
        return getSimulation().getActionManager().getAction(actionKey).isEnabled();
    }

    public ArrayList<XParameter> getParameters() {
        return parameterManager.getXParameters();
    }
}
