/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.manager.OutputManager.NCDimFactory;
import org.previmer.ichthyop.manager.PropertyManager;
import org.previmer.ichthyop.SimulationManagerAccessor;
import java.util.ArrayList;
import java.util.List;
import ucar.ma2.Array;
import ucar.ma2.DataType;
import ucar.ma2.Index;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * @author pverley
 */
public abstract class AbstractTracker extends SimulationManagerAccessor {

    private final ArrayList<Dimension> dimensions = new ArrayList();
    private final List<Attribute> attributes = new ArrayList();
    private final DataType dataType;
    private final PropertyManager propertyManager = PropertyManager.getInstance(getClass());
    private Array array;
    private final NCDimFactory dimFactory = getSimulationManager().getOutputManager().getDimensionFactory();
    private boolean enabled;

    abstract void setDimensions();

    abstract Array createArray();

    public abstract void addRuntimeAttributes();

    public abstract void track();

    public AbstractTracker(DataType type) {
        this.dataType = type;
        enabled = true;
    }

    public void init() {
        setDimensions();
        array = createArray();
        String value;
        // add compulsory attributes
        value = propertyManager.getProperty("tracker.longname");
        if (null != value) {
            addAttribute(new Attribute("long_name", value));
        }
        value = propertyManager.getProperty("tracker.unit");
        if (null != value) {
            addAttribute(new Attribute("unit", value));
        }
        // add pre-defined attributes
        String name;
        int i = 0;
        while ((name = propertyManager.getProperty("tracker.attribute[" + i + "].name")) != null) {
            value = propertyManager.getProperty("tracker.attribute[" + i + "].value");
            addAttribute(new Attribute(name, value));
            i++;
        }
    }

    public Index getIndex() {
        return array.getIndex();
    }

    public Array getArray() {
        return array;
    }

    public void disable() {
        enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void addCustomDimension(Dimension dim) {
        addDimension(dimFactory.createDimension(dim));
    }

    public void addTimeDimension() {
        addDimension(dimFactory.getTimeDimension());

    }

    public void addDrifterDimension() {
        addDimension(dimFactory.getDrifterDimension());
    }

    public void addZoneDimension(TypeZone type) {
        addDimension(dimFactory.getZoneDimension(type));
    }

    public List<Dimension> getDimensions() {
        return dimensions;
    }

    private void addDimension(Dimension dimension) {
        if (!dimensions.contains(dimension)) {
            dimensions.add(dimension);
        }
    }

    public int[] origin(int index_record) {
        int[] origin = new int[getDimensions().size()];
        origin[0] = index_record;
        return origin;
    }

    public String getName() {
        return propertyManager.getProperty("tracker.shortname");
    }

    void addAttribute(Attribute attribute) {
        if (!attributes.contains(attribute)) {
            attributes.add(attribute);
        }
    }

    public Attribute[] getAttributes() {
        return attributes.toArray(new Attribute[attributes.size()]);
    }

    public DataType getDataType() {
        return dataType;
    }

    int getNParticle() {
        return getSimulationManager().getReleaseManager().getNbParticles();
    }
}
