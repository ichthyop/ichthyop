/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class RunBatch implements Runnable {

    private Simulation simulation;
    /**
     * The configuration file
     */
    private ICFile icfile;
    /**
     * Date and time of the current step
     */
    String strTime;
    /**
     * Refresh time step [second]
     */
    private int dt;
    /**
     * Index of the current step
     */
    private int i_step;
    /**
     * Total number of steps of the run
     */
    private int nb_steps;
    /**
     * The {@code Step} object holding information about the current
     * step of the run.
     */
    private Step step;

    public RunBatch(String path) {

        try {
            File file = new File(path);
            if (file.exists()) {
                //icfile = new ICFile(file);
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
     * Sets up the main frame. It loads a configuration file and calls for the
     * setup SwingWorker.
     * @see inner class SetupSwingWorker
     */
    public void setUp() throws Exception {

        try {

            //new Configuration(cfgFile);
        } catch (Exception e) {
            throw new IOException("Problem loading configuration file");
        }
        
        simulation = new Simulation();
        simulation.setUp();
    }

    public void run() {
        do {
            simulation.step();
        } while (step.next());
    }
}
