
package org.previmer.ichthyop.arch;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public interface IAndresBuoyantParticle {

    public int getStage();

    public double computeSpecificGravity(double temperature, double salinity, double waterDensity);

    public double getSpecificGravity();

    public double getTemperature();

    public double getSalinity();

    public double getWaterDensity();

}
