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
import org.previmer.ichthyop.particle.IParticle;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;

/**
 *
 * @author pverley
 */
public abstract class IntegerTracker extends AbstractTracker {
    
    abstract int getValue(IParticle particle);
    
    IntegerTracker() {
        super(DataType.INT);
    }

    @Override
    void setDimensions() {
        addTimeDimension();
        addDrifterDimension();
    }
    
    @Override
    public Array createArray() {
        ArrayInt.D2 array = new ArrayInt.D2(1, getNParticle());
        for (int i = 0; i < getNParticle(); i++) {
            array.set(0, i, -99);
        }
        return array;
    }
    
    @Override
    public void track() {
        IParticle particle;
        Iterator<IParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            getArray().setInt(getIndex().set(0, particle.getIndex()), getValue(particle));
        }
    }
    
    @Override
    public void addRuntimeAttributes() {
        // no runtime atributes
    }
}
