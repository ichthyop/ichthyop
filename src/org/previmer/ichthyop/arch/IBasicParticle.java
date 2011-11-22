package org.previmer.ichthyop.arch;

import org.previmer.ichthyop.particle.ParticleLayer;
import org.previmer.ichthyop.particle.ParticleMortality;

/**
 * Public interface that lists the methods a Particle of the model will have
 * to provide, as a minimum.
 * Other classes that only need to get some Particle information will actually
 * manipulate IParticle and not the Particle itself. It is a way to avoid the
 * particles to be modified by these classes. When the particles are accessed
 * through the IParticle interface, they are kind of "read only" objects.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see ichthyop.core.Particle that implements IParticle
 */
public interface IBasicParticle {

/////////////////////////////
// Declaration of the methods
/////////////////////////////
    /**
     * Gets the x grid coordinate
     * @return a double, the x grid coordinate of the particle
     */
    public double getX();

    /**
     * Gets the y grid coordinate
     * @return a double, the y grid coordinate of the particle
     */
    public double getY();

    /**
     * Gets the z grid coordinate
     * @return a double, the z grid coordinate of the particle
     */
    public double getZ();

    /**
     * Gets the longitude
     * @return a double, the longitude of the particle location [East degree]
     */
    public double getLon();

    /**
     * Gets the latitude
     * @return a double, the latitude of the particle location [North degree
     */
    public double getLat();

    /**
     * Gets the depth
     * @return a double, the depth of the particle [meter]
     */
    public double getDepth();

    /**
     * Checks whether the particle is living or not
     * @return <code>true</code> if the particle is living; <code>false</code>
     * otherwise.
     */
    public boolean isLiving();

    /**
     * Gets the index of the particle.
     * @return the particle index.
     */
    public int getIndex();

    /**
     * Gets the age of the particle.
     * @return the age in seconds.
     */
    public long getAge();

    /**
     * Gets the temperature of the the location where the particle is.
     * @return the temperature en degree.
     */
    public float getTemperature();

    /**
     * Gets the salinity of the the location where the particle is.
     * @return the salinity.
     */
    public float getSalinity();
    /**
     * Kills the particle and specify the cause of the death.
     * Sets <code>living</code> status to <code>false</code>.
     * And sets longitude, latitude and depth to NaN.
     * @param the cause of death.
     */
    public void kill(ParticleMortality cause);

    public ParticleMortality getDeathCause();

    public boolean isLocked();

    public void lock();

    public void unlock();

    public ParticleLayer getLayer(Class layerClass);

    public double[] getGridCoordinates();

    public double[] getGeoCoordinates();

    public void increment(double[] move);

    public void increment(double[] move, boolean exclusivityH, boolean exclusivityV);
    
    //---------- End of interface
}
