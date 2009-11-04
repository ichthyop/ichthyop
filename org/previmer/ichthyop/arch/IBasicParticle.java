package org.previmer.ichthyop.arch;

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

public interface IBasicParticle extends IRhoPoint {

/////////////////////////////
// Declaration of the methods
/////////////////////////////

    public void step();
    
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

    public void setIndex(int index);

    //public int getNumZone(int typeZone);

    public long getAge();

    public void incrementAge();

    public void kill(String cause);

    public String getDeathCause();

    public boolean isLocked();

    public void lock();

    public void unlock();

    //public void init();

    //---------- End of interface
}
