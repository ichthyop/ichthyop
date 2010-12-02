package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IMasterParticle;
import org.previmer.ichthyop.particle.ParticleMortality;

/**
 *
 * @author pverley
 */
public class SysActionMove extends AbstractSysAction {

    private boolean reflexiveCostline;

    @Override
    public void loadParameters() throws Exception {
        try {
            reflexiveCostline = Boolean.valueOf(getParameter("app.transport", "reflexive-costline"));
        } catch (Exception ex) {
            getLogger().warning("Reflexive costline disabled since the parameter could not be found in the configuration file.");
        }
    }

    @Override
    public void execute(IMasterParticle particle) {
        if (!particle.isLocked()) {
            particle.applyMove(reflexiveCostline);
            if (particle.isOnEdge()) {
                particle.kill(ParticleMortality.OUT_OF_DOMAIN);
                return;
            } else if (!particle.isInWater()) {
                particle.kill(ParticleMortality.BEACHED);
                return;
            }
            particle.grid2Geo();
        }
        particle.unlock();
    }
}
