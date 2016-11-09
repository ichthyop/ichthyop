/*
 *  Copyright (C) 2011 mcuif
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
import org.previmer.ichthyop.particle.RecruitableParticleLayer;

/**
 *
 * @author mcuif
 */
public class RecruitmentStainTracker extends IntegerTracker {

    @Override
    int getValue(IParticle particle) {
        RecruitableParticleLayer rparticle = (RecruitableParticleLayer) particle.getLayer(RecruitableParticleLayer.class);
        return rparticle.isRecruited() ? 1 : 0;
    }
}
