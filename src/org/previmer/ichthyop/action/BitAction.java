/*
 *  Copyright (C) 2011 pverley
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

package org.previmer.ichthyop.action;

import org.previmer.ichthyop.particle.IParticle;
import org.previmer.ichthyop.io.BitTracker;
import org.previmer.ichthyop.particle.BitParticleLayer;

/**
 *
 * @author pverley
 */
public class BitAction extends AbstractAction {

    @Override
    public void loadParameters() throws Exception {
        getSimulationManager().getOutputManager().addPredefinedTracker(BitTracker.class);
    }
    
    @Override
    public void init(IParticle particle) {
        // Nothing to do
    }

    @Override
    public void execute(IParticle particle) {
        ((BitParticleLayer) particle.getLayer(BitParticleLayer.class)).setBit((int) Math.round(Math.random()));
    }

}
