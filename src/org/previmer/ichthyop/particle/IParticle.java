/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package org.previmer.ichthyop.particle;

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
     * Kills the particle and specify the cause of the death.
     * Sets <code>living</code> status to <code>false</code>.
     * And sets longitude, latitude and depth to NaN.
     * @param cause the cause of death.
     */
    public void kill(ParticleMortality cause);

    public ParticleMortality getDeathCause();

    public boolean isLocked();

    public void lock();

    public void unlock();

    public ParticleLayer getLayer(Class layerClass);

    public double[] getGridCoordinates();

    public void increment(double[] move);

    public void increment(double[] move, boolean exclusivityH, boolean exclusivityV);
    
    public void init();
    
    //---------- End of interface
}
