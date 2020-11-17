/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.particle;

/**
 * Public interface that lists the methods a Particle of the model will have to
 * provide, as a minimum. Other classes that only need to get some Particle
 * information will actually manipulate IParticle and not the Particle itself.
 * It is a way to avoid the particles to be modified by these classes. When the
 * particles are accessed through the IParticle interface, they are kind of
 * "read only" objects.
 *
 * <p>
 * Copyright: Copyright (c) 2007 - Free software under GNU GPL
 * </p>
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
     * 
     * @return a double, the x grid coordinate of the particle
     */
    public double getX();

    /**
     * Gets the y grid coordinate
     * 
     * @return a double, the y grid coordinate of the particle
     */
    public double getY();

    /**
     * Gets the z grid coordinate
     * 
     * @return a double, the z grid coordinate of the particle
     */
    public double getZ();

    /**
     * Gets the longitude
     * 
     * @return a double, the longitude of the particle location [East degree]
     */
    public double getLon();

    /**
     * Gets the latitude
     * 
     * @return a double, the latitude of the particle location [North degree
     */
    public double getLat();

    /**
     * Gets the depth
     * 
     * @return a double, the depth of the particle [meter]
     */
    public double getDepth();

    /**
     * Checks whether the particle is living or not
     * 
     * @return <code>true</code> if the particle is living; <code>false</code>
     *         otherwise.
     */
    public boolean isLiving();

    /**
     * Gets the index of the particle.
     * 
     * @return the particle index.
     */
    public int getIndex();

    /**
     * Gets the age of the particle.
     * 
     * @return the age in seconds.
     */
    public long getAge();

    /**
     * Kills the particle and specify the cause of the death. Sets
     * <code>living</code> status to <code>false</code>. And sets longitude,
     * latitude and depth to NaN.
     * 
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

	public void step();

}
