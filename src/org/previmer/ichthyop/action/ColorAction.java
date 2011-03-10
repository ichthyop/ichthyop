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

import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.io.ColorTracker;
import org.previmer.ichthyop.particle.ColorParticleLayer;

/**
 *
 * @author pverley
 */
public class ColorAction extends AbstractAction {

    public void loadParameters() throws Exception {
        getSimulationManager().getOutputManager().addPredefinedTracker(ColorTracker.class);
    }

    public void execute(IBasicParticle particle) {
        int color = (int) (Math.random() * 3 + 1);
        ((ColorParticleLayer) particle.getLayer(ColorParticleLayer.class)).setColor(color);
    }
}
