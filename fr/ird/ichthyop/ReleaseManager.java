/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.ISimulationAccessor;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.io.XAction;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class ReleaseManager implements ReleaseListener, ISimulationAccessor {

    IReleaseProcess releaseProcess;

    private IReleaseProcess getReleaseProcess() {
        if (releaseProcess == null) {
            
            try {
                releaseProcess = (IReleaseProcess) Class.forName("").newInstance();
            } catch (InstantiationException ex) {
                Logger.getLogger(ReleaseManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(ReleaseManager.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(ActionPool.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return releaseProcess;
    }

    public void releaseTriggered(ReleaseEvent event) {
        try {
            getReleaseProcess().release(event);
        } catch (IOException ex) {
            Logger.getLogger(ReleaseManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }
}
