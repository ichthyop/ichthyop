/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.previmer.ichthyop.io.IOTools;

/**
 *
 * @author pverley
 */
public class IchthyopBatch extends SimulationManagerAccessor implements Runnable {

    private String pathname;

    public IchthyopBatch(String pathname) {

        this.pathname = pathname;
    }

    public void run() {

        try {
            File file = new File(IOTools.resolveFile(pathname));
            getSimulationManager().setConfigurationFile(file);
            getLogger().info("Opened configuration file " + file.getPath());
            /* */
            getLogger().info("===== Simulation started =====");
            getSimulationManager().resetId();
            getSimulationManager().resetTimerGlobal();
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
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }
}
