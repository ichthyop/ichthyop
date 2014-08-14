/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.particle.IParticle;

/**
 *
 * @author pverley
 */
public abstract class AbstractAction extends SimulationManagerAccessor {

    private final String actionKey;

    abstract public void loadParameters() throws Exception;

    abstract public void execute(IParticle particle);

    abstract public void init(IParticle particle);

    public AbstractAction() {
        actionKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
    }

    public String getBlockKey() {
        return actionKey;
    }

    public String getParameter(String key) {
        return getSimulationManager().getParameterManager().getParameter(BlockType.ACTION, actionKey, key);
    }
    
    public String[] getListParameter(String key) {
        return getSimulationManager().getParameterManager().getListParameter(BlockType.ACTION, actionKey, key);
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
