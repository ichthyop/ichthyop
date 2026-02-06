/*
 *
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 *
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software.
 *
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full
 * description, see the LICENSE file.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
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

    private final ArrayList<Dimension> dimensions = new ArrayList<>();
    private final List<Attribute> attributes = new ArrayList<>();
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
