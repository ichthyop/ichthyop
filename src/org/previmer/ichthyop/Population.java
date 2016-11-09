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

package org.previmer.ichthyop;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.event.SetupListener;
import org.previmer.ichthyop.manager.SimulationManager;
import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.Particle;

/**
 * The Population is the intermediate level of the hierarchy of the IBM:
 * Simulation > Population > Individual (Particle). In accordance with the name,
 * this class manages a collection of particles. It is one of the core classes
 * of the IBM (Simulation, Population and Particle). The access to the particles
 * always goes through this class.
 *
 * @see java.util.ArrayList
 *
 * @author P.Verley (philippe.verley@ird.fr)
 */
public class Population extends ArrayList implements SetupListener {

///////////////////////////////
// Declaration of the constants
///////////////////////////////
    /**
     * Use multi-thread environment for running a step.
     */
    private final boolean MULTITHREAD  = false;
    /**
     * The minimal number of particles for splitting the step in concurrent
     * pools of particles. Less than {@code THRESHOLD} particles, the simulation
     * will run in sequential environment. More than {@code THRESHOLD}
     * particles, the simulation will make use of the multi thread environment
     * (if any).
     */
    private final int THRESHOLD = 1000;
///////////////////////////////
// Declaration of the variables
///////////////////////////////
    private final static Population population = new Population();

///////////////
// Constructors
///////////////
    public Population() {
        SimulationManager.getInstance().addSetupListener(Population.this);
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    public static Population getInstance() {
        return population;
    }

    public boolean add(IParticle particle) {
        particle.init();
        return super.add(particle);
    }

    /**
     * Applies a step on the {@code Population} at current time step. It
     * implements a Fork/Join algorithm for splitting the {@code Population} in
     * subsets and run the current step in multi thread environment.
     */
    public void step() {

        if (MULTITHREAD) {
            ForkStep step = new ForkStep(0, size());
            ForkJoinPool pool = new ForkJoinPool();
            pool.invoke(step);
        } else {
            Iterator<Particle> iter = iterator();
            Particle particle;
            while (iter.hasNext()) {
                particle = iter.next();
                if (particle.isLiving()) {
                    particle.step();
                }
            }
        }
    }

    /**
     * Removes all school from the {@code Population}.
     *
     * @param e, the setup event thrown by the {@code SimulationManager} at
     * setup.
     */
    @Override
    public void setupPerformed(SetupEvent e) {
        population.clear();
    }

    /**
     * Implementation of the Fork/Join algorithm for splitting the set of
     * particles in several subsets.
     */
    private class ForkStep extends RecursiveAction {

        private final int iStart, iEnd;

        /**
         * Creates a new {@code ForkStep} that will handle a subset of
         * particles.
         *
         * @param iStart, index of the first particle of the subset
         * @param iEnd , index of the last particle of the subset
         */
        ForkStep(int iStart, int iEnd) {
            this.iStart = iStart;
            this.iEnd = iEnd;
        }

        /**
         * Loop over the subset of particles and apply the
         * {@link org.previmer.ichthyop.particle.MasterParticle#step()}
         * function.
         */
        private void processDirectly() {
            for (int iParticle = iStart; iParticle < iEnd; iParticle++) {
                Particle particle = (Particle) Population.this.get(iParticle);
                if (particle.isLiving()) {
                    particle.step();
                }
            }
        }

        @Override
        protected void compute() {

            // Size of the subset
            int nParticle = iEnd - iStart;
            if (nParticle < THRESHOLD) {
                // If the size of the subset is smaller than the THRESHOLD,
                // process directly the whole subset
                processDirectly();
            } else {
                // If the size of the subset is greater than the THRESHOLD,
                // splits subset in two subsets
                int iSplit = iStart + nParticle / 2;
                invokeAll(new ForkStep(iStart, iSplit), new ForkStep(iSplit, iEnd));
            }
        }
    }
}
