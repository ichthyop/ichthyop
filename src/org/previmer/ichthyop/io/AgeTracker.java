/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import org.previmer.ichthyop.arch.IBasicParticle;
import java.util.Iterator;
import ucar.ma2.Array;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;

/**
 *
 * @author mariem
 */
public class AgeTracker extends AbstractTracker {

    public AgeTracker() {
        super(DataType.INT);
    }
    // A revoir car les dimensions ne sont pas les mêmes.
    @Override
    void setDimensions() {
        addTimeDimension();
        addAliveDimension();
    }

    public void track() {
        IBasicParticle particle;
        Iterator<IBasicParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            getArray().set(0, particle.getIndex(), (int) particle.getAge());
            // le getAge défini dans IBasicParticle est un long, et il est en jours.
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
}
