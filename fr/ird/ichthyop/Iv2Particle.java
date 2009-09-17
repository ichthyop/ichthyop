/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public class Iv2Particle extends GrowingParticle {

    private static Advection advection;
    private static HorizontalDispersion hdispersion;
    private static VerticalDispersion vdispersion;
    private static Buoyancy buoyancy;
    private static Recruitment recruitment;
    private static LinearGrowth growth;
    private static LethalTemperature lethalTp;
    private static Migration migration;

    private void move() {

        if (advection.isActivated()) {
            advection.execute(this);
        }

        if (hdispersion.isActivated()) {
            hdispersion.execute(this);
        }

        if (vdispersion.isActivated()) {
            vdispersion.execute(this);
        }

        if (getSimulation().getDataset().isOnEdge(getGridPoint())) {
            kill(Constant.DEAD_OUT);
        } else if (!getSimulation().getDataset().isInWater(getGridPoint())) {
            kill(Constant.DEAD_BEACH);
        }

        if (buoyancy.isActivated() && isLiving()) {
            buoyancy.execute(this);
        }

        /** vertical migration */
        if (migration.isActivated() && isLiving()) {
            migration.execute(this);
        }

        /** Transform (x, y, z) into (lon, lat, depth) */
        if (isLiving()) {
            grid2Geo();
        }

    }

    public void step() {

        if (getAge() <= getSimulation().getStep().getTransportDuration()) {

            if (recruitment.isActivated() && recruitment.isStopMoving() && isRecruited()) {
                return;
            }

            move();

            if (growth.isActivated() && isLiving()) {
                growth.execute(this);
            } else if (lethalTp.isActivated() && isLiving()) {
                lethalTp.execute(this);
            }

            if (recruitment.isActivated() && isLiving()) {
                recruitment.execute(this);
            }

            incrementAge();

        } else {
            kill(Constant.DEAD_OLD);
        }
    }
}
