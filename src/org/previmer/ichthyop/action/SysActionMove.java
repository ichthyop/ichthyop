package org.previmer.ichthyop.action;

import org.previmer.ichthyop.arch.IMasterParticle;
import org.previmer.ichthyop.particle.ParticleMortality;

/**
 *
 * @author pverley
 */
public class SysActionMove extends AbstractSysAction {

    private boolean bouncyCostline;

    @Override
    public void loadParameters() throws Exception {
        try {
            bouncyCostline = Boolean.valueOf(getParameter("app.transport", "bouncy-costline"));
        } catch (Exception ex) {
            getLogger().warning("Bouncy costline disabled since the parameter could not be found in the configuration file.");
        }
    }

    @Override
    public void execute(IMasterParticle particle) {
        if (!particle.isLocked()) {
            particle.applyMove(bouncyCostline);
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
