
package org.previmer.ichthyop.arch;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public interface IAndresBuoyantParticle {

    public int getStage();

    public double getSpecificGravity(double temperature);

}
