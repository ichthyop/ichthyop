/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.util.Constant;

/**
 *
 * @author pverley
 */
public class Iv2Particle extends GrowingParticle {

    public Iv2Particle() {
        init();
    }

    public void step() {

        if (getAge() > getSimulationManager().getTimeManager().getTransportDuration()) {
            kill(Constant.DEAD_OLD);
            return;
        }
        if (isOnEdge()) {
            kill(Constant.DEAD_OUT);
            return;
        } else if (!isInWater()) {
            kill(Constant.DEAD_BEACH);
            return;
        }

        getSimulationManager().getActionManager().executeActions(this);

        grid2Geo();
        incrementAge();

    }
}
