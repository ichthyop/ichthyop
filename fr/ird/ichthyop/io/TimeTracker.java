/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.io;

import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;

/**
 *
 * @author pverley
 */
public class TimeTracker extends AbstractTracker {

    public TimeTracker() {
        super(DataType.DOUBLE);
    }

    @Override
    void setDimensions() {
        addTimeDimension();
    }

    @Override
    Array createArray() {
        return new ArrayDouble.D1(1);
    }

    public void track() {
        getArray().setDouble(0, getSimulation().getStep().getTime());
    }

}
