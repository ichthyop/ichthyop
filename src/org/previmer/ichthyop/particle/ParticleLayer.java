/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.arch.IBasicParticle;

/**
 *
 * @author pverley
 */
public abstract class ParticleLayer extends SimulationManagerAccessor {

    final private IBasicParticle linkedParticle;

    public ParticleLayer(IBasicParticle particle) {
        this.linkedParticle = particle;
        init();
    }

    abstract public void init();

    public IBasicParticle particle() {
        return linkedParticle;
    }

}
