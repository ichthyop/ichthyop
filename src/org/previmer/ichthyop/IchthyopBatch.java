/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop;

import java.io.File;
import java.util.logging.Level;
import org.previmer.ichthyop.io.IOTools;

/**
 *
 * @author pverley
 */
public class IchthyopBatch extends SimulationManagerAccessor implements Runnable {

    /**
     * The path of the configuration file.
     */
    private final String filename;

    /**
     * Creates a new run in batch mode with the specified configuration file.
     *
     * @param filename, the path of the configuration file
     */
    public IchthyopBatch(String filename) {
        this.filename = filename;
    }

    /**
     * Runs the simulation.
     */
    @Override
    public void run() {

        try {
            File file = new File(IOTools.resolveFile(filename));
            getSimulationManager().setConfigurationFile(file);
            getLogger().log(Level.INFO, "Opened configuration file {0}", file.getPath());
            getLogger().info("===== Simulation started =====");
            getSimulationManager().resetId();
            getSimulationManager().resetTimerGlobal();
            /* */
            do {
                getLogger().log(Level.INFO, "++++ Run {0}", getSimulationManager().indexSimulationToString());
                /* setup */
                getLogger().info("Setting up...");
                getSimulationManager().setup();
                /* initialization */
                getLogger().info("Initializing...");
                getSimulationManager().init();
                /* first time step */
                getSimulationManager().getTimeManager().firstStepTriggered();
                getSimulationManager().resetTimerCurrent();
                do {
                    /* check whether the simulation has been interrupted by user */
                    if (getSimulationManager().isStopped()) {
                        break;
                    }
                    /* step simulation */
                    getSimulationManager().getSimulation().step();
                    progress();
                } while (getSimulationManager().getTimeManager().hasNextStep());
            } while (getSimulationManager().hasNextSimulation());
            getLogger().info("===== Simulation completed =====");
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, "An error occured while running the simulation", ex);
        }
    }

    /**
     * Logs the progress of the simulation.
     */
    private void progress() {

        StringBuilder msg = new StringBuilder();
        msg.append(getSimulationManager().getTimeManager().stepToString());
        msg.append(" Time ");
        msg.append(getSimulationManager().getTimeManager().timeToString());
        msg.append(" ");
        int percent = (int) (getSimulationManager().progressCurrent() * 100);
        msg.append(percent);
        msg.append("% - Run ");
        msg.append(getSimulationManager().indexSimulationToString());
        msg.append(" ");
        msg.append(getSimulationManager().timeLeftCurrent());
        msg.append(" - ");
        msg.append("Simulation ");
        percent = (int) (getSimulationManager().progressGlobal() * 100);
        msg.append(percent);
        msg.append("% ");
        msg.append(getSimulationManager().timeLeftGlobal());
        getLogger().info(msg.toString());
    }
}
