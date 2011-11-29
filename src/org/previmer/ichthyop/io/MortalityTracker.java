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

import org.previmer.ichthyop.arch.IBasicParticle;
import java.util.Iterator;
import org.previmer.ichthyop.particle.ParticleMortality;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class MortalityTracker extends AbstractTracker {

    public MortalityTracker() {
        super(DataType.INT);
    }

    @Override
    void setDimensions() {
        addTimeDimension();
        addDrifterDimension();
        addAliveDimension();
    }

    public void track() {
        IBasicParticle particle;
        Iterator<IBasicParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            getArray().set(0, particle.getIndex(), particle.getDeathCause().getCode());
        }
    }

    @Override
    public ArrayInt.D2 getArray() {
        return (ArrayInt.D2) super.getArray();
    }

    @Override
    Array createArray() {
        ArrayInt.D2 array = new ArrayInt.D2(1, dimensions().get(1).getLength());
        for (int i = 0; i < dimensions().get(1).getLength(); i++) {
            array.set(0, i, -99);
        }
        return array;
    }

    @Override
    public Attribute[] attributes() {
        Attribute[] attributes = new Attribute[ParticleMortality.values().length];
        int i = 0;
        for (ParticleMortality cause : ParticleMortality.values()) {
            attributes[i++] = new Attribute(cause.toString(), cause.getCode());
        }
        return attributes;
    }
}
