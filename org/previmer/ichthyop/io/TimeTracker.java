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
    public Attribute[] attributes() {
        List<Attribute> listAttributes = new ArrayList();
        String calendarName = getSimulationManager().getTimeManager().getCalendar().getClass().getSimpleName();
        if (calendarName.toLowerCase().contains("calendar1900")) {
            listAttributes.add(new Attribute("calendar", "gregorian"));
            listAttributes.add(new Attribute("origin", getSimulationManager().getParameterManager().getParameter("app.time", "time_origin")));
        } else {
            listAttributes.add(new Attribute("calendar", "climato"));
        }
        return listAttributes.toArray(new Attribute[listAttributes.size()]);
    }

    @Override
    Array createArray() {
        return new ArrayDouble.D1(1);
    }

    public void track() {
        getArray().setDouble(0, getSimulationManager().getTimeManager().getTime());
    }
}
