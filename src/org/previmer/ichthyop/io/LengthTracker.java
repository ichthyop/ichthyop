/*
 *  Copyright (C) 2010 Philippe Verley <philippe dot verley at ird dot fr>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.io;

import org.previmer.ichthyop.particle.IParticle;
import static org.previmer.ichthyop.SimulationManagerAccessor.getSimulationManager;
import org.previmer.ichthyop.particle.DebParticleLayer;
import org.previmer.ichthyop.particle.GrowingParticleLayer;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class LengthTracker extends FloatTracker {

    private final boolean isLinearGrowth;

    public LengthTracker() {
        isLinearGrowth = getSimulationManager().getActionManager().isEnabled("action.growth");
    }

    @Override
    float getValue(IParticle particle) {
        if (isLinearGrowth) {
            GrowingParticleLayer gParticle = (GrowingParticleLayer) particle.getLayer(GrowingParticleLayer.class);
            return (float) gParticle.getLength();
        } else {
            DebParticleLayer gParticle = (DebParticleLayer) particle.getLayer(DebParticleLayer.class);
            return (float) gParticle.getLength();
        }
    }
}
