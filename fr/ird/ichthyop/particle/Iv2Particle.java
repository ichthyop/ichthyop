/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.particle;

import fr.ird.ichthyop.util.Constant;
import fr.ird.ichthyop.action.RecruitmentAction;
import fr.ird.ichthyop.arch.IActionManager;

/**
 *
 * @author pverley
 */
public class Iv2Particle extends GrowingParticle {

    public void step() {

        IActionManager actionManager = getSimulation().getActionManager();

        if (getAge() <= getSimulation().getStep().getTransportDuration()) {

            if (actionManager.get("action.recruitment") != null) {
                if (((RecruitmentAction) actionManager.get("action.recruitment")).isStopMoving() && isRecruited()) {
                    return;
                }
            }

            actionManager.execute("action.advection", this);
            actionManager.execute("action.hdisp", this);
            actionManager.execute("action.vdisp", this);

            if (isOnEdge()) {
                kill(Constant.DEAD_OUT);
            } else if (!isInWater()) {
                kill(Constant.DEAD_BEACH);
            }

            actionManager.execute("action.buoyancy", this);
            actionManager.execute("action.migration", this);

            /** Transform (x, y, z) into (lon, lat, depth) */
            if (isLiving()) {
                grid2Geo();
            }

            if (isLiving()) {
                actionManager.execute("action.growth", this);
                actionManager.execute("action.lethal_temperature", this);
            }

            if (isLiving()) {
                actionManager.execute("action.recruitment", this);
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
