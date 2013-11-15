package org.previmer.ichthyop;

/**
 * import java.util
 */
import java.util.ArrayList;
import java.util.Iterator;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.event.SetupListener;
import org.previmer.ichthyop.manager.SimulationManager;
import org.previmer.ichthyop.particle.MasterParticle;

/**
 * <p>
 * The Population is the intermediate level of the hierarchy of the IBM:
 * Simulation > Population > Individual (Particle). In accordance with the name,
 * this class manages a collection of particles. It is one of the core classes
 * of the IBM (Simulation, Population and Particle). The access to the particles
 * always goes through this class.</p>
 *
 * <p>
 * The class extends a <code>ArrayList</code> that contains Particle objects</p>
 *
 * <p>
 * The class relays the <code>step</code> function to every particle. The
 * <code>step</code> function appears in the 3 core classes (see above) of the
 * model. It handles the march of the model through time.</p>
 *
 * @see {@link java.util.ArrayList} for more details about the ArrayList class.
 *
 * @author P.Verley
 */
public class Population extends ArrayList implements SetupListener {

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
     * This function is called every time step. It loops over the {@code 
     */
    public void step() {

        Iterator<MasterParticle> iter = iterator();
        MasterParticle particle;
        while (iter.hasNext()) {
            particle = iter.next();
            if (particle.isLiving()) {
                particle.step();
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
    //------- End of class
}
