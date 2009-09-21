/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

/**
 *
 * @author pverley
 */
public interface IGrowingParticle extends IBasicParticle {

    public double getLength();

    public void setLength(double length);

    public int getStage();

}
