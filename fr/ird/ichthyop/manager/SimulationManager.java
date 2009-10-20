/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.Simulation;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.ISimulationAccessor;
import fr.ird.ichthyop.io.BlockType;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.io.ParamType;
import fr.ird.ichthyop.io.XBlock;
import fr.ird.ichthyop.io.XParameter;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author pverley
 */
public class SimulationManager implements Runnable, ISimulationAccessor {

    final private static SimulationManager simulationManager = new SimulationManager();
    /**
     * Index of the current simulation (always 0 for SINGLE mode)
     */
    private int i_simulation;
    /***
     * Number of simulations (always 1 for SERIAL mode)
     */
    private int nb_simulations = 1;
    /**
     *
     */
    private List<XParameter> serialParameters;

    private SimulationManager() {
        serialParameters = getSerialParameters();
        for (XParameter xparam : serialParameters) {
            nb_simulations *= xparam.getLength();
        }
    }

    public static SimulationManager getInstance() {
        return simulationManager;
    }

    /**
     * Checks for the existence of a new set of parameters. Systematically
     * returns false for SINGLE mode.
     * @return <code>true</code> if a new set of parameters is available;
     * <code>false</code> otherwise
     */
    public boolean hasNextSimulation() {

        for (XParameter xparam : serialParameters) {
            if (xparam.hasNext()) {
                xparam.increment();
                this.reset();
                return true;
            } else {
                xparam.reset();
            }
        }
        return false;
    }

    public List<XParameter> getSerialParameters() {

        List<XParameter> list = new ArrayList();
        for (BlockType type : BlockType.values()) {
            for (XBlock xblock : ICFile.getInstance().getBlocks(type)) {
                if (xblock.isEnabled()) {
                    list.addAll(xblock.getParameters(ParamType.SERIAL));
                }
            }
        }
        return list;
    }

    /**
     * Gets the number of simulations (which equals the number of sets of
     * parameters predefined by the user). Returns 1 for SINGLE mode.
     * @return the number of simulations.
     */
    public int getNumberOfSimulations() {
        return nb_simulations;
    }

    /**
     * Resets indexes and time values at the beginning of a new simulation.
     */
    private void reset() {
        i_simulation++;
    }

    public void run() {
        do {
            getSimulation().setUp();
            getSimulation().getStep().firstStepTriggered();
            do {
                getSimulation().step();
            } while (getSimulation().getStep().hasNext());
        } while (hasNextSimulation());
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }
}
