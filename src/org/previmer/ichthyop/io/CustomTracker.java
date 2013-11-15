/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import org.previmer.ichthyop.arch.IParticle;
import java.util.Iterator;
import ucar.ma2.Array;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

/**
 *
 * @author pverley
 */
public class CustomTracker extends AbstractTracker {

    private final String variableName;
    private ArrayFloat.D2 array;

    public CustomTracker(String variableName) {
        super(DataType.FLOAT);
        this.variableName = variableName;
        getSimulationManager().getDataset().requireVariable(variableName, getClass());
    }

    @Override
    public void track() {
        IParticle particle;
        Iterator<IParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
        while (iter.hasNext()) {
            particle = iter.next();
            float valueTracked = getSimulationManager().getDataset().get(variableName, particle.getGridCoordinates(), getSimulationManager().getTimeManager().getTime()).floatValue();
            getArray().set(0, particle.getIndex(), valueTracked);
        }
    }

    @Override
    public ArrayFloat.D2 getArray() {
        return array;
    }

    @Override
    public String short_name() {
        return variableName;
    }

    @Override
    public String long_name() {
        return null;
    }

    @Override
    public String unit() {
        return null;
    }

    @Override
    public Attribute[] attributes() {
        return null;
    }
    

    @Override
    void setDimensions() {
        addTimeDimension();
        addDrifterDimension();
    }

    @Override
    Array createArray() {
        return new ArrayFloat.D2(1, getSimulationManager().getOutputManager().getDimensionFactory().getDrifterDimension().getLength());

    }
}
