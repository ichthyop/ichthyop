/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.release;

import fr.ird.ichthyop.event.ReleaseEvent;
import fr.ird.ichthyop.*;
import fr.ird.ichthyop.arch.IReleaseProcess;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.SimulationManagerAccessor;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public abstract class AbstractReleaseProcess extends SimulationManagerAccessor implements IReleaseProcess {

    private String releaseKey;
    private boolean paramLoaded = false;

    public AbstractReleaseProcess() {
        releaseKey = getSimulationManager().getPropertyManager(getClass()).getProperty("release.key");
        loadParameters();
    }

    abstract void loadParameters();

    abstract void proceedToRelease(ReleaseEvent event) throws IOException;

    final public void release(ReleaseEvent event) {
        /*if (!paramLoaded) {
            loadParameters();
            paramLoaded = true;
        }*/
        try {
            proceedToRelease(event);
        } catch (IOException ex) {
            Logger.getLogger(AbstractReleaseProcess.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    String getParameter(String key) {
        return getSimulationManager().getReleaseManager().getXReleaseProcess(releaseKey).getParameter(key).getValue();
    }
}
