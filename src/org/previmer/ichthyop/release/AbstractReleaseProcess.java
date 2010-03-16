/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.release;

import org.previmer.ichthyop.event.ReleaseEvent;
import org.previmer.ichthyop.arch.IReleaseProcess;
import org.previmer.ichthyop.SimulationManagerAccessor;
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
        releaseKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
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
        return getSimulationManager().getReleaseManager().getParameter(releaseKey, key);
    }
}
