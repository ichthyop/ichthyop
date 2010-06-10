/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.arch.ITracker;
import org.previmer.ichthyop.manager.OutputManager.NCDimFactory;
import org.previmer.ichthyop.manager.PropertyManager;
import org.previmer.ichthyop.SimulationManagerAccessor;
import java.util.ArrayList;
import java.util.List;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * @author pverley
 */
public abstract class AbstractTracker extends SimulationManagerAccessor implements ITracker {

    private String trackerKey;
    private ArrayList<Dimension> dimensions = new ArrayList();
    final private DataType type;
    private PropertyManager propertyManager = PropertyManager.getInstance(getClass());
    private Array array;
    private NCDimFactory dimFactory = getSimulationManager().getOutputManager().getDimensionFactory();

    abstract void setDimensions();

    abstract Array createArray();

    public AbstractTracker(DataType type) {
        trackerKey = getSimulationManager().getPropertyManager(getClass()).getProperty("block.key");
        this.type = type;
        setDimensions();
        array = createArray();
    }

    public Array getArray() {
        return array;
    }

    public void addDimension(Dimension dim) {

        dimensions.add(dimFactory.addDimension(dim));
    }

    public void addTimeDimension() {
        dimensions.add(dimFactory.getTimeDimension());
    }

    public void addDrifterDimension() {
        dimensions.add(dimFactory.getDrifterDimension());
    }

    public void addZoneDimension(TypeZone type) {
        dimensions.add(dimFactory.getZoneDimension(type));
    }

    public List<Dimension> dimensions() {
        return dimensions;
    }

    public int[] origin(int index_record) {
        int[] origin = new int[dimensions().size()];
        origin[0] = index_record;
        return origin;
    }

    public String short_name() {
        return propertyManager.getProperty("tracker.shortname");
    }

    public String long_name() {
        return propertyManager.getProperty("tracker.longname");
    }

    public String unit() {
        return propertyManager.getProperty("tracker.unit");
    }

    public Attribute[] attributes() {
        List<Attribute> listAttributes = new ArrayList();
        int i = 0;
        String name;
        try {
            while ((name = propertyManager.getProperty("tracker.attribute[" + i + "].name")) != null) {
                String value = propertyManager.getProperty("tracker.attribute[" + i + "].value");
                listAttributes.add(new Attribute(name, value));
                i++;
            }
        } catch (java.util.MissingResourceException ex) {
        }
        return listAttributes.toArray(new Attribute[listAttributes.size()]);
    }

    public DataType type() {
        return type;
    }
}
