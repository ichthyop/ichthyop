/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public class Particle extends RhoPoint implements IBasicParticle {

    private int index;
    private long age;
    private String deathCause;
    private boolean living;
    private static Advection advection;
    private static HorizontalDispersion hdispersion;
    private static VerticalDispersion vdispersion;
    private static Buoyancy buoyancy;
    private static Recruitment recruitment;

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

    }

    public void kill(String cause) {

        this.deathCause = cause;
        living = false;
        setLon(Double.NaN);
        setLat(Double.NaN);
        setDepth(Double.NaN);
    }

    public void step() {
        if (getAge() <= getSimulation().getStep().getTransportDuration()) {
            
            

            move();
        }
    }

    public boolean isLiving() {
        return living;
    }

    public int index() {
        return index;
    }

    public long getAge() {
        return age;
    }

    public String getDeathCause() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
