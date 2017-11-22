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

import java.util.ArrayList;
import org.ichthyop.grid.AbstractRegularGrid;
import java.util.HashMap;
import java.util.List;
import org.ichthyop.event.NextStepListener;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.dataset.variable.AbstractDatasetVariable;
import org.ichthyop.dataset.variable.IVariable;
import org.ichthyop.event.NextStepEvent;
import org.ichthyop.grid.IGrid;
import org.ichthyop.manager.TimeManager;
import ucar.nc2.NetcdfFile;

/**
 *
 * @author pverley
 */
public abstract class AbstractDataset extends IchthyopLinker implements IDataset, NextStepListener {

    final HashMap<String, AbstractDatasetVariable> variables = new HashMap();
    final HashMap<String, List<String>> names = new HashMap();

    AbstractRegularGrid grid;

    abstract String getKey();

    abstract void loadParameters();

    abstract AbstractDatasetVariable createVariable(String name);
    
    abstract AbstractRegularGrid createGrid();
    
    @Override
    public void setUp() throws Exception {

        loadParameters();

        grid = createGrid();
        grid.init();
    }

    @Override
    public void init() throws Exception {

        // instantiate dataset variables
        for (String name : names.keySet()) {
            variables.put(name, createVariable(name));
        }

        // initialise them
        int time_arrow = timeArrow();
        double t0 = getSimulationManager().getTimeManager().get_tO();
        for (AbstractDatasetVariable variable : variables.values()) {
            variable.init(t0, time_arrow);
        }
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = e.getSource().getTime();
        int time_arrow = timeArrow();

        for (AbstractDatasetVariable variable : variables.values()) {
            variable.update(time, time_arrow);
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
        if (!names.containsKey(name)) {
            names.put(name, new ArrayList());
        }
        names.get(name).add(requiredBy.getCanonicalName());
    }

    public void clearRequiredVariables() {
        names.clear();
        variables.clear();
    }

    @Override
    public void removeRequiredVariable(String name, Class requiredBy) {

        if (names.containsKey(name)) {
            names.get(name).remove(requiredBy.getCanonicalName());
        }
        if (names.get(name).isEmpty()) {
            names.remove(name);
            variables.remove(name);
        }
    }

    public void checkRequiredVariable(NetcdfFile nc) {

    }

    boolean skipSorting() {
        return getConfiguration().getBoolean(getKey() + ".skip_sorting", false);
    }

    int timeArrow() {
        return getConfiguration().getString("app.time.time_arrow").equals(TimeManager.TimeDirection.FORWARD.toString()) ? 1 : -1;
    }

    boolean enhanced() {
        return getConfiguration().canFind(getKey() + ".enhanced_mode")
                ? getConfiguration().getBoolean(getKey() + ".enhanced_mode")
                : true;
    }
}
