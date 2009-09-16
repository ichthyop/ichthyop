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

    ISimulation simulation;
    private int dead;
    private static Advection advection;
    private static HorizontalDispersion hdispersion;
    private static VerticalDispersion vdispersion;
    private static Buoyancy buoyancy;

    public void step(double time) {
        if (getAge() <= simulation.getTransportDuration()) {
            move();
        }
    }

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

        if (isOnEdge(simulation.getDataset().get_nx(), simulation.getDataset().get_ny())) {
            die(Constant.DEAD_OUT);
        } else if (!simulation.getDataset().isInWater(this)) {
            die(Constant.DEAD_BEACH);
        }

        if (buoyancy.isActivated() && isLiving()) {
            buoyancy.execute(this);
        }

    }

    private void die(int dead) {

        this.dead = dead;
        //setLiving(false);
        setLLD(Double.NaN, Double.NaN, Double.NaN);
        //length = temperature = salinity = Double.NaN;
    }

    public double[] getGridPoint() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean isLiving() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int index() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getAge() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
