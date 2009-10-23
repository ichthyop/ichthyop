/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.io;

import fr.ird.ichthyop.arch.IBasicParticle;
import java.util.Iterator;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;

/**
 *
 * @author pverley
 */
public class GenericTracker extends AbstractTracker {

    private String variableName;

    GenericTracker() {
        super(DataType.FLOAT);
    }

    @Override
    void setDimensions() {
        addTimeDimension();
        addDrifterDimension();
    }

    @Override
    public ArrayFloat.D2 getArray() {
        return (ArrayFloat.D2) super.getArray();
    }

    @Override
    Array createArray() {
        return new ArrayFloat.D2(1, dimensions().get(1).getLength());
    }

    public void track() {
        IBasicParticle particle;
        Iterator<IBasicParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            float valueTracked = getSimulationManager().getDataset().get(variableName, particle.getGridPoint(), getSimulationManager().getTimeManager().getTime()).floatValue();
            getArray().set(0, particle.getIndex(), valueTracked);
        }
    }

}
