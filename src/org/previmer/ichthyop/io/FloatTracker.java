/*
 * Copyright (C) 2013 pverley
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.io;

import java.util.Iterator;
import static org.previmer.ichthyop.SimulationManagerAccessor.getSimulationManager;
import org.previmer.ichthyop.arch.IParticle;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;

/**
 *
 * @author pverley
 */
public abstract class FloatTracker extends AbstractTracker {
    
    abstract float getValue(IParticle particle);
    
    FloatTracker() {
        super(DataType.FLOAT);
    }

    @Override
    void setDimensions() {
        addTimeDimension();
        addDrifterDimension();
    }
    
    @Override
    Array createArray() {
        ArrayFloat.D2 array = new ArrayFloat.D2(1, getNParticle());
        for (int i = 0; i < getNParticle(); i++) {
            array.set(0, i, Float.NaN);
        }
        return array;
    }
    
    @Override
    void addRuntimeAttributes() {
        // no runtime attribute
    }
    
    @Override
    public void track() {
        IParticle particle;
        Iterator<IParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            getArray().setFloat(getIndex().set(0, particle.getIndex()), getValue(particle));
        }
    }
    
}
