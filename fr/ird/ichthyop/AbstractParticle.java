/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

/**
 *
 * @author pverley
 */
public abstract class AbstractParticle  implements IBasicParticle {

    private int index;
    
    private long age;

    private boolean living;

    public int index() {
        return index;
    }

    public boolean isLiving() {
        return living;
    }

    public void setLiving(boolean living) {
        this.living = living;
    }

    public long getAge() {
        return age;
    }
}
