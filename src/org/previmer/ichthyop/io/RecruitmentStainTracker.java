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

import java.util.Iterator;
import org.previmer.ichthyop.arch.IParticle;
import org.previmer.ichthyop.particle.RecruitableParticleLayer;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;

/**
 *
 * @author mcuif
 */
public class RecruitmentStainTracker extends AbstractTracker {

    public RecruitmentStainTracker() {
        super(DataType.INT);
    }

    @Override
    void setDimensions() {
        addTimeDimension();
        addDrifterDimension();
    }

    @Override
    Array createArray() {
        return new ArrayInt.D2(1, dimensions().get(1).getLength());
    }

    @Override
    public ArrayInt.D2 getArray() {
        return (ArrayInt.D2) super.getArray();
    }

    public void track() {
        IParticle particle;
        RecruitableParticleLayer rparticle;
        Iterator<IParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            rparticle = (RecruitableParticleLayer) particle.getLayer(RecruitableParticleLayer.class);
            getArray().set(0, particle.getIndex(), rparticle.isRecruited() ? 1 : 0);
        }
    }

}
