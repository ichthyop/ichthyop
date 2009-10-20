/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.ISimulationAccessor;
import fr.ird.ichthyop.io.ICFile;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class RunBatch implements Runnable, ISimulationAccessor {

    public RunBatch(String path) {

        path = System.getProperty("user.dir") + File.separator + "cfg2.xic";
        try {
            File file = new File(path);
            if (file.exists()) {
                ICFile.setFile(file);
                setUp();
            } else {
                throw new IOException("Configuration file not found");
            }
            new Thread(this).start();
        } catch (Exception ex) {
            Logger.getLogger(RunBatch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Set up the simulation.
     * @see inner class SetupSwingWorker
     */
    public void setUp() throws Exception {
        getSimulation().setUp();
        getSimulation().init();
    }

    public void run() {
        getSimulation().getStep().firstStepTriggered();
        do {
            getSimulation().step();
            //Logger.getLogger(RunBatch.class.getName()).info("Step " + getSimulation().getStep().timeToString());
        } while (getSimulation().getStep().hasNext());
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }

    public static void main(String... args) {
        String filename = System.getProperty("user.dir") + File.separator + "cfg2.xic";
        new RunBatch(filename);
    }
}
