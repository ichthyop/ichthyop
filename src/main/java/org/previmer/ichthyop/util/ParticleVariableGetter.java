package org.previmer.ichthyop.util;

import org.previmer.ichthyop.particle.IParticle;

@FunctionalInterface
public interface ParticleVariableGetter {

    public double getVariable(IParticle particle);

}
