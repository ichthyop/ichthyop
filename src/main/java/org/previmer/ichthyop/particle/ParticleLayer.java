/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.particle;

import org.previmer.ichthyop.SimulationManagerAccessor;

/**
 *
 * @author pverley
 */
public abstract class ParticleLayer extends SimulationManagerAccessor {

    final private IParticle linkedParticle;

    public ParticleLayer(IParticle particle) {
        this.linkedParticle = particle;
    }

    abstract public void init();

    public IParticle particle() {
        return linkedParticle;
    }

}
