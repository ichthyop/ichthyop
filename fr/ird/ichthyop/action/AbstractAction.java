/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.action;

import fr.ird.ichthyop.*;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.IAction;
import fr.ird.ichthyop.io.XParameter;
import java.util.ArrayList;

/**
 *
 * @author pverley
 */
public abstract class AbstractAction implements IAction {

    private String actionKey;

    public AbstractAction() {
        actionKey = getSimulation().getPropertyManager(getClass()).getProperty("action.key");
    }

    public String getParameter(String key) {
        return getSimulation().getActionManager().getXAction(actionKey).getParameter(key).getValue();
    }

    public ArrayList<XParameter> getParameters() {
        return getSimulation().getActionManager().getXAction(actionKey).getParameters();
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }

    public boolean isEnabled() {
        return getSimulation().getActionManager().getXAction(actionKey).isEnabled();
    }
}
