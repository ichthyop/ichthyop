/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IAction;
import org.previmer.ichthyop.io.XParameter;
import org.previmer.ichthyop.SimulationManagerAccessor;
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
        return getSimulationManager().getActionManager().getParameter(actionKey, key);
    }

    public boolean isEnabled() {
        return getSimulationManager().getActionManager().isEnabled(actionKey);
    }

}
