/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public abstract class AbstractAction implements IAction {

    private ParameterManager parameterManager = new ParameterManager(this.getClass());

    public String getParameter(String name) {
        return parameterManager.getValue(name);
    }

    public String getProperty(String name) {
        return parameterManager.getProperty(name);
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }

    public boolean isActivated() {
        return getSimulation().getActionManager().getAction(getClass().getSimpleName()).isEnabled();
    }
}
