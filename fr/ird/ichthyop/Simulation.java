/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public class Simulation implements ISimulation {

    private static Simulation simulation = new Simulation();
    private static Population population;
    private static ParameterManager parameterManager = new ParameterManager(fr.ird.ichthyop.ICFile.class);

    public void setUp() {}

    public void init() {}

    public Population getPopulation() {
        return population;
    }

    public static Simulation getInstance() {
        return simulation;
    }

    public IDataset getDataset() {
        return null;
    }

    public Step getStep() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void step() {
        population.step();
    }

    public IActionManager getActionManager() {
        return ActionManager.getInstance();
    }

    public IParameterManager getParameterManager() {
        return parameterManager;
    }

    public IParameterManager getParameterManager(Class aClass) {
        return new ParameterManager(aClass);
    }
}
