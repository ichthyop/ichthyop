
package org.previmer.ichthyop.action;

import org.previmer.ichthyop.particle.MasterParticle;
import org.previmer.ichthyop.particle.ParticleMortality;

/**
 *
 * @author pverley
 */
public class SysActionAgeMonitoring extends AbstractSysAction {

    boolean keepDrifting;

    @Override
    public void execute(MasterParticle particle) {
        if (!keepDrifting && particle.getAge() > getSimulationManager().getTimeManager().getTransportDuration()) {
            particle.kill(ParticleMortality.OLD);
            return;
        }
        particle.incrementAge();
    }

    @Override
    public void loadParameters() throws Exception {
        keepDrifting = Boolean.valueOf(getParameter("app.time", "keep_drifting"));
    }

}
