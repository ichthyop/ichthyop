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
 * @author pverley
 */
public class DepthTracker extends AbstractTracker {

    public DepthTracker() {
        super(DataType.FLOAT);
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
            getArray().set(0, particle.getIndex(), (float) particle.getDepth());
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
