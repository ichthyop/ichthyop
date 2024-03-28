package org.previmer.ichthyop.io;

import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.SargassumParticleLayer;

public class SargassumCarbonTracker extends FloatTracker {

    @Override
    float getValue(IParticle particle) {
        SargassumParticleLayer gParticle = (SargassumParticleLayer) particle.getLayer(SargassumParticleLayer.class);
        return (float) gParticle.getC();
    }
    
}