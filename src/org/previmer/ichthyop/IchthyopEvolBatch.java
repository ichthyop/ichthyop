package org.previmer.ichthyop;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.io.IOTools;

/**
 *
 * @author mariem
 */
public class IchthyopEvolBatch extends SimulationManagerAccessor implements Runnable {

    private String pathname;
    private int startIndexGeneration;
    private boolean restart = false;
    private String firstId;

    public IchthyopEvolBatch(String pathname, String indexGeneration, String firstId) {

        this.pathname = pathname;
        try {
            this.startIndexGeneration = Integer.valueOf(indexGeneration);
        } catch (Exception ex) {
            this.startIndexGeneration = 0;
        }
        if (startIndexGeneration > 0) {
            restart = true;
        }
        this.firstId = firstId;
    }

    public void run() {

        try {
            File file = new File(IOTools.resolveFile(pathname));
            getSimulationManager().setConfigurationFile(file);
            getSimulationManager().resetTimerGlobal();
            do {
                getLogger().info("Opened configuration file " + file.getPath());
                /* */
                getLogger().info("===== Simulation started =====");
                getSimulationManager().resetId();
                restart = false;
                if (restart) {
                    getSimulationManager().setFirstId(this.firstId);
                    getSimulationManager().getEvolManager().setIndexGeneration(this.startIndexGeneration);
                    getSimulationManager().setup();
                    restart = false;
                }
                getSimulationManager().getEvolManager().prepareNextGeneration();
                /* */
                do {
                    getLogger().info("++++ Run " + getSimulationManager().indexSimulationToString());
                    /* setup */
                    getLogger().info("Setting up...");
                    getSimulationManager().setup();
                    getLogger().info("Setup [OK]");
                    /* initialization */
                    getLogger().info("Initializing...");
                    getSimulationManager().init();
                    getLogger().info("Initialization [OK]");
                    /* */
                    getSimulationManager().getTimeManager().firstStepTriggered();
                    getSimulationManager().resetTimerCurrent();
                    StringBuffer msg = new StringBuffer();
                    int lengthMsg = 0;
                    do {
                        /* check whether the simulation has been interrupted by user */
                        if (getSimulationManager().isStopped()) {
                            break;
                        }
                        /* step simulation */
                        getSimulationManager().getSimulation().step();
                        /* Print message progress */
                        for (int i = 0; i < lengthMsg + 10; i++) {
                            msg.append('.');
                        }
                        //System.out.print("\r" + msg.toString());
                        msg = new StringBuffer();
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
                        //System.out.print("\r" + msg.toString());
                        getLogger().info(msg.toString());
                        lengthMsg = msg.length();
                    } while (getSimulationManager().getTimeManager().hasNextStep());
                } while (getSimulationManager().hasNextSimulation());
                getLogger().info("===== Simulation completed =====");
                getSimulationManager().getEvolManager().incrementGeneration();
            } while (getSimulationManager().getEvolManager().hasNextGeneration());

        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }
}
