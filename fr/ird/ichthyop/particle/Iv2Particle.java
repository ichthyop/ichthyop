/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.particle;

import fr.ird.ichthyop.util.Constant;
import fr.ird.ichthyop.action.RecruitmentAction;
import fr.ird.ichthyop.arch.IActionPool;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class Iv2Particle extends GrowingParticle {

    public void step(IActionPool actionPool) {

        if (getAge() <= getSimulation().getStep().getTransportDuration()) {

            if (actionPool.get("action.recruitment") != null) {
                if (((RecruitmentAction) actionPool.get("action.recruitment")).isStopMoving() && isRecruited()) {
                    return;
                }
            }

            actionPool.execute("action.advection", this);
            actionPool.execute("action.hdisp", this);
            actionPool.execute("action.vdisp", this);

            if (isOnEdge()) {
                kill(Constant.DEAD_OUT);
            } else if (!isInWater()) {
                kill(Constant.DEAD_BEACH);
            }

            actionPool.execute("action.buoyancy", this);
            actionPool.execute("action.migration", this);

            /** Transform (x, y, z) into (lon, lat, depth) */
            if (isLiving()) {
                grid2Geo();
            }

            if (isLiving()) {
                actionPool.execute("action.growth", this);
                actionPool.execute("action.lethal_temperature", this);
            }

            if (isLiving()) {
                actionPool.execute("action.recruitment", this);
            }

            incrementAge();

        } else {
            kill(Constant.DEAD_OLD);
        }

        /*if (getIndex() == 0) {
            Logger.getAnonymousLogger().info(getLon() + " " + getLat() + " " + getDepth());
        }*/
    }
}
