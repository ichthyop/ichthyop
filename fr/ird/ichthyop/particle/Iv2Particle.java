/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.particle;

import fr.ird.ichthyop.util.Constant;
import fr.ird.ichthyop.action.Recruitment;
import fr.ird.ichthyop.arch.IActionPool;

/**
 *
 * @author pverley
 */
public class Iv2Particle extends GrowingParticle {

    public void step(IActionPool actionPool) {

        if (getAge() <= getSimulation().getStep().getTransportDuration()) {

            if (actionPool.get("action.recruitment").isEnabled()) {
                if (((Recruitment) actionPool.get("action.recruitment")).isStopMoving() && isRecruited()) {
                    return;
                }
            }

            actionPool.get("action.advection").execute(this);
            actionPool.get("action.hdisp").execute(this);
            actionPool.get("action.vdisp").execute(this);

            if (getSimulation().getDataset().isOnEdge(getGridPoint())) {
                kill(Constant.DEAD_OUT);
            } else if (!getSimulation().getDataset().isInWater(getGridPoint())) {
                kill(Constant.DEAD_BEACH);
            }

            actionPool.get("action.buoyancy").execute(this);
            actionPool.get("action.migration").execute(this);

            /** Transform (x, y, z) into (lon, lat, depth) */
            if (isLiving()) {
                grid2Geo();
            }

            if (isLiving()) {
                actionPool.get("action.growth").execute(this);
                actionPool.get("action.lethal_temperature").execute(this);
            }

            if (isLiving()) {
                actionPool.get("action.recruitment").execute(this);
            }

            incrementAge();

        } else {
            kill(Constant.DEAD_OLD);
        }
    }
}
