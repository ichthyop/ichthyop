package fr.ird.ichthyop;

/** import java.util */
import fr.ird.ichthyop.arch.IBasicParticle;
import fr.ird.ichthyop.arch.IPopulation;
import java.util.HashSet;
import java.util.Iterator;

/**
 * <p> The Population is the intermediate level of the hierarchy of the IBM:
 * Simulation > Population > Individual (Particle). In accordance
 * with the name, this class manages a collection of particles. It is indeed
 * one of the core classes of the IBM (Simulation, Population and Particle).
 * The access to the particles always goes through this class.</p>
 *
 * <p>The class extends a <code>HasSet</code> that contains Particle objects</p>
 *
 * <p>In terms of proprer methods, this class mainly controls the release of
 * the particles. It also relays the <code>step</code> function to every
 * particle. The <code>step</code> function appears in the 3 core classes
 * (see above) of the model. It handles the march of the model through time.</p>
 *
 * @see {@link java.util.HashSet} for more details about the HashSet class.
 *
 * @author P.Verley
 */
public class Population extends HashSet implements IPopulation {

////////////////
// Debug purpose
////////////////
///////////////////////////////
// Declaration of the variables
///////////////////////////////
    ActionPool actionPool = new ActionPool();
///////////////
// Constructors
///////////////
    public Population(int nbParticles) {
        super(nbParticles);
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    public void step() {

        Iterator<IBasicParticle> iter = iterator();
        IBasicParticle particle;
        while (iter.hasNext()) {
            particle = iter.next();
            if (particle.isLiving()) {
                particle.step(actionPool);
            }
        }
    }
    //------- End of class
}
