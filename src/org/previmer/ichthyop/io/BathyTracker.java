/*
 * Copyright (C) 2012 gandres
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
import org.previmer.ichthyop.arch.IParticle;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;

/**
 *
 * @author gandres
 */
public class BathyTracker extends AbstractTracker {

    public BathyTracker() {
        super(DataType.FLOAT);
    }
    
    @Override
    void setDimensions() {
        addTimeDimension();
        addDrifterDimension();
    }

    @Override
    Array createArray() {
        ArrayFloat.D2 array = new ArrayFloat.D2(1, dimensions().get(1).getLength());
        for (int i = 0; i < dimensions().get(1).getLength(); i++) {
            array.set(0, i, -1);
        }
        return array;
    }

    @Override
    public void track() {
        IParticle particle;
        Iterator<IParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            double[] pGrid={particle.getX(),particle.getY(),particle.getZ() };
            double bathy= getSimulationManager().getDataset().getBathy((int) Math.round(pGrid[0]), (int)  Math.round(pGrid[1]));
            getArray().set(0, particle.getIndex(), (float) bathy);
        }
        
    }
    
    @Override
    public ArrayFloat.D2 getArray() {
        return (ArrayFloat.D2) super.getArray();
    }
    
}