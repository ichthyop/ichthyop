/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import java.util.ArrayList;
import java.util.List;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;

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
    public Attribute[] getAttributes() {
        List<Attribute> listAttributes = new ArrayList();
        String calendarName = getSimulationManager().getTimeManager().getCalendar().getClass().getSimpleName();
        if (calendarName.toLowerCase().contains("interannual")) {
            listAttributes.add(new Attribute("calendar", "gregorian"));
        } else {
            listAttributes.add(new Attribute("calendar", "climato"));
        }
        listAttributes.add(new Attribute("origin", getSimulationManager().getParameterManager().getParameter("app.time", "time_origin")));
        return listAttributes.toArray(new Attribute[listAttributes.size()]);
    }

    @Override
    Array createArray() {
        return new ArrayDouble.D1(1);
    }

    @Override
    public void track() {
        getArray().setDouble(0, getSimulationManager().getTimeManager().getTime());
    }
    
    @Override
    void addRuntimeAttributes() {
        // no runtime attribute
    }
}
