/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IAction;
import org.previmer.ichthyop.SimulationManagerAccessor;

/**
 *
 * @author pverley
 */
public abstract class AbstractAction extends SimulationManagerAccessor implements IAction {

    private String actionKey;

    public AbstractAction() {
        actionKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
    }

    public String getParameter(String key) {
        return getSimulationManager().getActionManager().getParameter(actionKey, key);
    }

    public ActionPriority getPriority() {
        String priority = getParameter("priority");
        for (ActionPriority actionPriority : ActionPriority.values()) {
            if (priority.equals(actionPriority.toString())) {
                return actionPriority;
            }
        }
        return ActionPriority.NORMAL;
    }

    public boolean isEnabled() {
        return getSimulationManager().getActionManager().isEnabled(actionKey);
    }
}
