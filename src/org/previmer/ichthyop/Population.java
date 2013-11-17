package org.previmer.ichthyop;

import java.util.ArrayList;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.event.SetupListener;
import org.previmer.ichthyop.manager.SimulationManager;
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

    /**
     * Applies a step on the {@code Population} at current time step. It
     * implements a Fork/Join algorithm for splitting the {@code Population} in
     * subsets and run the current step in multi thread environment.
     */
    public void step() {

        ForkStep step = new ForkStep(0, size());
        ForkJoinPool pool = new ForkJoinPool();
        pool.invoke(step);
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
                ((Particle) get(iParticle)).step();
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
