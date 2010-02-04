/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class IchthyopBatch extends SimulationManagerAccessor {

    public IchthyopBatch(String path) {

        try {
            File file = new File(path);
            if (file.exists()) {
                getSimulationManager().setConfigurationFile(file);
                new Thread(getSimulationManager()).start();
            } else {
                throw new IOException("Configuration file not found");
            }
        } catch (Exception ex) {
            Logger.getLogger(IchthyopBatch.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
