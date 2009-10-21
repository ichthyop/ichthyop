/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class RunBatch extends SimulationManagerAccessor {

    public RunBatch(String path) {

        path = System.getProperty("user.dir") + File.separator + "cfg2.xic";
        try {
            File file = new File(path);
            if (file.exists()) {
                getSimulationManager().setConfigurationFile(file);
                new Thread(getSimulationManager()).start();
            } else {
                throw new IOException("Configuration file not found");
            }
        } catch (Exception ex) {
            Logger.getLogger(RunBatch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static void main(String... args) {
        String filename = System.getProperty("user.dir") + File.separator + "cfg2.xic";
        new RunBatch(filename);
    }
}
