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
    
    /**
     * Check whether parameter 'key' has 'null' value. The function returns
     * {@code true} in several cases: the parameter does not exist, the value of
     * the parameter is empty or the value of the parameter is set to "null".
     *
     * @param key, the key of the parameter
     * @return {@code true} if the parameter is either null, empty or does not
     * exist
     */
    public boolean isNull(String key) {
        String value;
        try {
            value = getParameter(key).trim();
        } catch (Exception ex) {
            return true;
        }
        return (null == value) || value.isEmpty() ||  value.equalsIgnoreCase("null");
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
