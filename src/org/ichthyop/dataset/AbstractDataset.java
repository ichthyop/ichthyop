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
package org.ichthyop.dataset;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import org.ichthyop.grid.AbstractRegularGrid;
import java.util.HashMap;
import java.util.List;
import org.ichthyop.event.NextStepListener;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.dataset.variable.AbstractDatasetVariable;
import org.ichthyop.dataset.variable.IVariable;
import org.ichthyop.dataset.variable.NetcdfDatasetVariable;
import org.ichthyop.event.NextStepEvent;
import org.ichthyop.grid.IGrid;
import org.ichthyop.manager.TimeManager;
import org.ichthyop.util.NCComparator;
import ucar.nc2.Attribute;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public abstract class AbstractDataset extends IchthyopLinker implements IDataset, NextStepListener {

    // constants
    final int NLAYER = 3;
    final int TILING_H = 100;
    final int TILING_V = 3;
    // variables
    final HashMap<String, AbstractDatasetVariable> variables = new HashMap();
    // names of the variables
    final HashMap<String, List<String>> requiredBy = new HashMap();
    // dataset grid
    AbstractRegularGrid grid;
    // prefix in the configuration file
    final String prefix;
    private String location;
    // 
    final HashMap<String, List<String>> variableMap = new HashMap();
    // 
    int time_arrow;
    //
    boolean enhanced;
    private boolean alphabetically_sorted;

    // constructor
    public AbstractDataset(String prefix) {
        this.prefix = prefix;

    }

    abstract void loadParameters();

    private AbstractRegularGrid createGrid() {

        String classname = getConfiguration().getString(prefix + ".grid.class_name");

        try {
            return (AbstractRegularGrid) Class.forName(classname).getConstructor(String.class).newInstance(prefix);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            error("[dataset] Failed to instantiate dataset grid " + prefix + ".grid", ex);
        }
        return null;
    }

    @Override
    public String getKey() {
        return prefix;
    }

    @Override
    public void setUp() throws Exception {

        time_arrow = getConfiguration().getString("app.time.time_arrow").equals(TimeManager.TimeDirection.FORWARD.toString()) ? 1 : -1;

        enhanced = !getConfiguration().isNull(prefix + ".enhanced_mode")
                ? getConfiguration().getBoolean(prefix + ".enhanced_mode")
                : true;

        alphabetically_sorted = getConfiguration().isNull(prefix + ".alphabetically_sorted")
                ? true
                : getConfiguration().getBoolean(prefix + ".alphabetically_sorted", false);

        this.location = getConfiguration().getString(prefix + ".location");
        mapDatasetVariables(location);
        if (variableMap.isEmpty()) {
            error("Failed to list any variable in dataset " + prefix, new IOException("Invalid dataset location " + location));
        }
        // sort locations
        for (String name : variableMap.keySet()) {
            if (alphabetically_sorted) {
                Collections.sort(variableMap.get(name));
            } else {
                String variable_time;
                try (NetcdfFile nc = DatasetUtil.open(variableMap.get(name).get(0), enhanced)) {
                    variable_time = DatasetUtil.findTimeVariable(nc);
                }
                Collections.sort(variableMap.get(name), new NCComparator(variable_time, time_arrow));
            }
        }

        grid = createGrid();
        grid.init();

        loadParameters();
    }

    AbstractDatasetVariable createVariable(String name, int nlayer, int tilingh, int tilingv) {

        return new NetcdfDatasetVariable(variableMap.get(name), name,
                nlayer, grid, tilingh, Math.min(tilingv, grid.get_nz()),
                enhanced);
    }

    private void mapDatasetVariables(String source) {

        if (DatasetUtil.isValidDataset(source)) {
            try (NetcdfFile nc = NetcdfDataset.openDataset(source, true, null)) {
                for (Variable variable : nc.getVariables()) {
                    if (!variable.isCoordinateVariable()) {
                        List<String> names = new ArrayList();
                        names.add(variable.getFullName());
                        Attribute sname = variable.findAttributeIgnoreCase("standard_name");
                        if (null != sname) {
                            names.add(sname.getStringValue());
                        }
                        Attribute lname = variable.findAttributeIgnoreCase("long_name");
                        if (null != lname) {
                            names.add(lname.getStringValue());
                        }
                        for (String name : names) {
                            if (variableMap.containsKey(name)) {
                                variableMap.get(name).add(nc.getLocation());
                            } else {
                                List<String> list = new ArrayList();
                                list.add(nc.getLocation());
                                variableMap.put(name, list);
                            }
                        }
                    }
                }
            } catch (IOException ex) {
                warning("Error listing non-coordinate variables from dataset " + source, ex);
            }
        } else if (new File(source).isDirectory()) {
            for (File file : new File(source).listFiles()) {
                mapDatasetVariables(file.getAbsolutePath());
            }
        }
    }

    @Override
    public void init() throws Exception {

        // instantiate dataset variables
        for (String name : requiredBy.keySet()) {
            variables.put(name, createVariable(name, NLAYER, TILING_H, TILING_V));
        }

        // initialise them
        double t0 = getSimulationManager().getTimeManager().get_tO();
        for (AbstractDatasetVariable variable : variables.values()) {
            if (null != variable) {
                variable.init(t0, time_arrow);
            }
        }
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();

        for (AbstractDatasetVariable variable : variables.values()) {
            if (null != variable) {
                variable.update(time, time_arrow);
            }
        }
    }

    @Override
    public IGrid getGrid() {
        return grid;
    }

    @Override
    public IVariable getVariable(String name) {
        return variables.get(name);
    }

    @Override
    public void requireVariable(String name, Class requiredBy) {
        if (!this.requiredBy.containsKey(name)) {
            this.requiredBy.put(name, new ArrayList());
        }
        this.requiredBy.get(name).add(requiredBy.getCanonicalName());
    }

    public void clearRequiredVariables() {
        requiredBy.clear();
        variables.clear();
        variableMap.clear();
    }

    @Override
    public void removeRequiredVariable(String name, Class requiredBy) {

        if (this.requiredBy.containsKey(name)) {
            this.requiredBy.get(name).remove(requiredBy.getCanonicalName());
        }
        if (this.requiredBy.get(name).isEmpty()) {
            this.requiredBy.remove(name);
            variables.remove(name);
            variableMap.remove(name);
        }
    }
}
