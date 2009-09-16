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

    private ISimulation simulation;
    private ParameterManager parameterManager = new ParameterManager(this.getClass());

    public AbstractAction(ISimulation simulation) {
        this.simulation = simulation;
    }

    public AbstractAction() {
        this(Simulation.getInstance());
    }

    public String getParameter(String name) {
        return parameterManager.getValue(name);
    }

    public String getProperty(String name) {
        return parameterManager.getString(name);
    }

    public ISimulation getSimulation() {
        return simulation;
    }

    public boolean isActivated() {
        return ActionManager.getAction(getClass().getSimpleName()).isEnabled();
    }
}
