/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import org.previmer.ichthyop.arch.IBasicParticle;
import java.util.Iterator;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;

/**
 *
 * @author mariem
 */
public class SalinityTracker extends AbstractTracker {

    public SalinityTracker() {
        super(DataType.FLOAT);
    }

    @Override
    void setDimensions() {
        addTimeDimension();
        addDrifterDimension();
    }

    public void track() {
        IBasicParticle particle;
        Iterator<IBasicParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            getArray().set(0, particle.getIndex(), particle.getSalinity());
        }
    }

    @Override
    public ArrayFloat.D2 getArray() {
        return (ArrayFloat.D2) super.getArray();
    }

    @Override
    Array createArray() {
        ArrayFloat.D2 array = new ArrayFloat.D2(1, dimensions().get(1).getLength());
        for (int i = 0; i < dimensions().get(1).getLength(); i++) {
            array.set(0, i, Float.NaN);
        }
        return array;
    }
}

