package org.previmer.ichthyop.io;

import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.particle.SargassumParticleLayer;

public class SargassumDensityTracker extends FloatTracker {

    @Override
    float getValue(IParticle particle) {
        SargassumParticleLayer gParticle = (SargassumParticleLayer) particle.getLayer(SargassumParticleLayer.class);
        return (float) gParticle.getDensity();
    }
    
}
