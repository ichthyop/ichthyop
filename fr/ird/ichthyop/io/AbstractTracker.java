/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.io;

import fr.ird.ichthyop.Simulation;
import fr.ird.ichthyop.arch.ISimulation;
import fr.ird.ichthyop.arch.ITracker;
import fr.ird.ichthyop.manager.PropertyManager;
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
public abstract class AbstractTracker implements ITracker {

    private String trackerKey;
    private ArrayList<Dimension> dimensions = new ArrayList();
    final private DataType type;
    private PropertyManager propertyManager = PropertyManager.getInstance(getClass());

    abstract void setDimensions();

    public AbstractTracker(DataType type) {
        trackerKey = getSimulation().getPropertyManager(getClass()).getProperty("tracker.key");
        this.type = type;
        setDimensions();
    }

    public List<Dimension> dimensions() {
        return dimensions;
    }

    public boolean isEnabled() {
        return getSimulation().getOutputManager().getXTracker(trackerKey).isEnabled();
    }

    public ISimulation getSimulation() {
        return Simulation.getInstance();
    }

    public String short_name() {
        return propertyManager.getProperty("tracker.shortname");
    }

    public String long_name() {
        return propertyManager.getProperty("Tracker.longname");
    }

    public String unit() {
        return propertyManager.getProperty("tracker.unit");
    }

    public Attribute[] attributes() {
        List<Attribute> listAttributes = new ArrayList();
        int i = 0;
        String name;
        while ((name = propertyManager.getProperty("tracker.attribute[" + i + "].name")) != null) {
            String value = propertyManager.getProperty("tracker.attribute[" + i + "].value");
            listAttributes.add(new Attribute(name, value));
            i++;
        }
        return listAttributes.toArray(new Attribute[listAttributes.size()]);
    }

    public DataType type() {
        return type;
    }
}
