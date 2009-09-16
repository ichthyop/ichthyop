package fr.ird.ichthyop;

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

    public void setZ(double z);

    public double[] getGridPoint();

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
    public int index();

    public void step(double time);

    public void increment(double[] move);

    //public int getNumZone(int typeZone);

    public long getAge();

    public void grid2Geog();

    public double depth2z(double depth);

    //---------- End of interface
}
