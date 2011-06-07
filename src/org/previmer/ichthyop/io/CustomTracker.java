/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import java.util.ArrayList;
import java.util.List;
import org.previmer.ichthyop.arch.IBasicParticle;
import java.util.Iterator;
import org.previmer.ichthyop.SimulationManagerAccessor;
import org.previmer.ichthyop.arch.ITracker;
import org.previmer.ichthyop.manager.OutputManager.NCDimFactory;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * @author pverley
 */
public class CustomTracker extends SimulationManagerAccessor implements ITracker {

    private String variableName;
    private ArrayFloat.D2 array;

    public CustomTracker(String variableName) {
        this.variableName = variableName;
        array = new ArrayFloat.D2(1, getSimulationManager().getOutputManager().getDimensionFactory().getDrifterDimension().getLength());
        getSimulationManager().getDataset().requireVariable(variableName, getClass());
    }

    public void track() {
        IBasicParticle particle;
        Iterator<IBasicParticle> iter = getSimulationManager().getSimulation().getPopulation().iterator();
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

    public int[] origin(int index_record) {
        return new int[]{index_record, 0};
    }

    public String short_name() {
        return variableName;
    }

    public String long_name() {
        return null;
    }

    public String unit() {
        return null;
    }

    public Attribute[] attributes() {
        return null;
    }

    public DataType type() {
        return DataType.FLOAT;
    }

    public List<Dimension> dimensions() {
        List<Dimension> list = new ArrayList(2);
        NCDimFactory dimFactory = getSimulationManager().getOutputManager().getDimensionFactory();
        list.add(dimFactory.getTimeDimension());
        list.add(dimFactory.getDrifterDimension());
        return list;
    }
}
