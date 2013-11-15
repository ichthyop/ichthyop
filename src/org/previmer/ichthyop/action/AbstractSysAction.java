package org.previmer.ichthyop.action;

import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.particle.MasterParticle;

/**
 *
 * @author pverley
 */
public abstract class AbstractSysAction extends SimulationManagerAccessor {
    
    abstract public void loadParameters() throws Exception;

    abstract public void execute(MasterParticle particle);

    public String getParameter(String block, String key) {
        return getSimulationManager().getParameterManager().getParameter(block, key);
    }

}
