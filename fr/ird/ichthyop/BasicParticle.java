/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public abstract class BasicParticle extends RhoPoint implements IBasicParticle {

    private int index;
    private long age;
    private String deathCause;
    private boolean living;

    public boolean isLiving() {
        return living;
    }

    public int index() {
        return index;
    }

    public long getAge() {
        return age;
    }

    public void kill(String cause) {

        this.deathCause = cause;
        living = false;
        setLon(Double.NaN);
        setLat(Double.NaN);
        setDepth(Double.NaN);
    }

    public String getDeathCause() {
        if (deathCause != null && !deathCause.isEmpty()) {
            return deathCause;
        } else {
            return "not-dead-yet";
        }
    }
}
