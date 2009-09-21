/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.particle.GrowingParticle;
import fr.ird.ichthyop.util.Constant;
import fr.ird.ichthyop.action.Recruitment;
import fr.ird.ichthyop.*;

/**
 *
 * @author pverley
 */
public class Iv2Particle extends GrowingParticle {

    public void step(ActionPool actionPool) {

        if (getAge() <= getSimulation().getStep().getTransportDuration()) {

            if (actionPool.get("Recruitment").isActivated()) {
                if (((Recruitment) actionPool.get("Recruitment")).isStopMoving() && isRecruited()) {
                    return;
                }
            }

            actionPool.get("Advection").execute(this);
            actionPool.get("HorizontalDispersion").execute(this);
            actionPool.get("VerticalDispersion").execute(this);

            if (getSimulation().getDataset().isOnEdge(getGridPoint())) {
                kill(Constant.DEAD_OUT);
            } else if (!getSimulation().getDataset().isInWater(getGridPoint())) {
                kill(Constant.DEAD_BEACH);
            }

            actionPool.get("Buoyancy").execute(this);
            actionPool.get("Migration").execute(this);

            /** Transform (x, y, z) into (lon, lat, depth) */
            if (isLiving()) {
                grid2Geo();
            }

            if (isLiving()) {
                actionPool.get("LinearGrowth").execute(this);
                actionPool.get("LethalTemperature").execute(this);
            }

            if (isLiving()) {
                actionPool.get("Recruitment").execute(this);
            }

            incrementAge();

        } else {
            kill(Constant.DEAD_OLD);
        }
    }
}
