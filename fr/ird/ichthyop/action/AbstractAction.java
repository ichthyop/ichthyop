/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.action;

import fr.ird.ichthyop.arch.IAction;
import fr.ird.ichthyop.io.XParameter;
import fr.ird.ichthyop.SimulationManagerAccessor;
import java.util.ArrayList;

/**
 *
 * @author pverley
 */
public abstract class AbstractAction extends SimulationManagerAccessor implements IAction {

    private String actionKey;

    public AbstractAction() {
        actionKey = getSimulationManager().getPropertyManager(getClass()).getProperty("action.key");
        loadParameters();
    }

    public String getParameter(String key) {
        return getSimulationManager().getActionManager().getXAction(actionKey).getParameter(key).getValue();
    }

    public ArrayList<XParameter> getParameters() {
        return getSimulationManager().getActionManager().getXAction(actionKey).getParameters();
    }

    public boolean isEnabled() {
        return getSimulationManager().getActionManager().getXAction(actionKey).isEnabled();
    }

}
