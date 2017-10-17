/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
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
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */

package org.ichthyop.output;

import org.ichthyop.Zone;
import org.ichthyop.manager.OutputManager.NCDimFactory;
import org.ichthyop.manager.PropertyManager;
import org.ichthyop.IchthyopLinker;
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
public abstract class AbstractTracker extends IchthyopLinker {

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

    public void addZoneDimension(String classname) {
        addDimension(dimFactory.getZoneDimension(classname));
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
