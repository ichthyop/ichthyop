package ichthyop.util;

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

public interface IParticle {

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
     * Gets the temperature
     * @return a double, the sea water temperature [celsius] at particle
     * location
     */
    public double getTemperature();

    /**
     * Gets the length
     * @return a double, the particle length [millimeter]
     */
    public double getLength();

    /**
     * Interpolates the temperature at specified time at particle location
     * @param time a double, the current time of the simulation [second]
     * @return a double, the sea water temperature at particle location
     */
    public double getTemperature(double time);

    /**
     * Interpolates the salinity at specified time at particle location
     * @param time a double, the current time of the simulation [second]
     * @return a double, the sea water salinity [psu] at particle location
     */
    public double getSalinity(double time);

    /**
     * Gets the averaged concentration of small zooplankton at particle
     * location [mMol N m-3]
     * @return a double, small zooplankton concentration [mMol N m-3]
     */
    public double getSmallZoo();

    /**
     * Gets the averaged concentration of large zooplankton at particle
     * location [mMol N m-3]
     * @return a double, large zooplankton concentration [mMol N m-3]

     */
    public double getLargeZoo();

    /**
     * Gets the averaged concentration of large phytoplankton at particle
     * location [mMol N m-3]
     * @return a double, large phytoplankton concentration [mMol N m-3]

     */
    public double getLargePhyto();

    /**
     * Gets the number of the release zone
     * @return an int, the number of the zone where the particle has been
     * released
     */
    public int getNumZoneInit();

    /**
     * Gets the recruitment zone
     * @return an int, the number of the zone where the particle has been
     * recruited
     */
    public int getNumRecruitZone();

    /**
     * Gets the zone of the particle at current time and location.
     * @return the number of the zone where the particle is currently located;
     */
    public int getNumZoneNC();

    /**
     * Gets death information
     * @return an int that contains information about the status of the particle
     * @see ichthyop.util.Resources for details about the DEAD_* variables
     */
    public int getDeath();

    /**
     * Checks whether the particle is living or not
     * @return <code>true</code> if the particle is living; <code>false</code>
     * otherwise.
     */
    public boolean isLiving();

    /**
     * Check whether the particle has been recruited.
     * @param typeRecruit an int, the recruitment criterion: age or length
     * @return <code>true</code> if the particle has been recruited;
     * <code>false</code> otherwise.
     */
    public boolean checkRecruitment(int typeRecruit);
    
    /**
     * Determines whether a particle is newly recruited.
     * 
     * @return {@code true} if the particle is newly recruited,
     *         {@code false} otherwise.
     */
    public boolean isNewRecruited();
    
    /**
      *  Sets the newly recruited status of the particle to false.
      */
     public void resetNewRecruited();

    /**
     * Checks whether the particle is dead cold.
     * @return <code>true</code> if the particle is dead cold;
     * <code>false</code> otherwise.
     */
    public boolean isDeadCold();

    /**
     * Checks if the particle has been recruited within the specified zone.
     * @param numZone the number of the recruitment zone
     * @return <code>true</code> if the particle has been recruited in this
     * specified zone; <code>false</code> otherwise.
     */
    public boolean isRecruited(int numZone);

    /**
     * Gets the index of the particle.
     * @return the particle index.
     */
    public int index();

    //---------- End of interface
}
