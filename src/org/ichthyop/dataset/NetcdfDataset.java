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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import org.ichthyop.grid.AbstractRegularGrid;
import java.util.HashMap;
import java.util.List;
import org.ichthyop.event.NextStepListener;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.calendar.CalendarUtil;
import org.ichthyop.calendar.GregorianCalendar;
import org.ichthyop.dataset.variable.AbstractDatasetVariable;
import org.ichthyop.dataset.variable.IVariable;
import org.ichthyop.dataset.variable.NetcdfDatasetVariable;
import org.ichthyop.event.NextStepEvent;
import org.ichthyop.grid.IGrid;
import org.ichthyop.manager.TimeManager;
import org.ichthyop.util.NCComparator;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

/**
 *
 * @author pverley
 */
public class NetcdfDataset extends IchthyopLinker implements IDataset, NextStepListener {

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
    private String path;
    // 
    final HashMap<String, List<String>> variableMap = new HashMap();
    //
    private Calendar calendar;
    protected double t0;
    // 
    int time_arrow;
    //
    boolean enhanced;
    private boolean alphabetically_sorted;

    // constructor
    public NetcdfDataset(String prefix) {
        this.prefix = prefix;

    }

    void loadParameters() {
        // does nothing
        // to be overidden by inheriting class
    }

    private AbstractRegularGrid createGrid() {

        String classname = getConfiguration().getString(prefix + ".grid.class_name");

        try {
            return (AbstractRegularGrid) Class.forName(classname).getConstructor(String.class).newInstance(prefix);
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            error("[dataset] Failed to instantiate dataset grid " + prefix + ".grid", ex);
        }
        return null;
    }

    private Calendar createCalendar() {

        String units = null;
        String classname = null;

        if (!getConfiguration().isNull(prefix + ".calendar")) {
            String scal = getConfiguration().getString(prefix + ".calendar");
            classname = CalendarUtil.CFToJavaClass(scal);
            if (null == classname) {
                error("[dataset] Parameter " + prefix + ".calendar " + scal + " not recognized.", new IOException("Invalid calendar type"));
            }
        } else {
            // user did not specify the type of calendar to use, Ichthyop takes a guess
            for (String name : variableMap.keySet()) {
                try (NetcdfFile nc = DatasetUtil.open(variableMap.get(name).get(0), enhanced)) {
                    String stime = DatasetUtil.findTimeVariable(nc);
                    if (stime != null) {
                        // found time variable, check its attribute
                        Variable vtime = nc.findVariable(stime);
                        if (null != vtime.findAttribute("calendar")) {
                            classname = CalendarUtil.CFToJavaClass(vtime.findAttribute("calendar").getStringValue());
                        }
                        if (null != vtime.findAttribute("units")) {
                            units = vtime.findAttribute("units").getStringValue();
                        }
                        break;
                    }
                } catch (IOException ex) {
                    // no need to report error here
                }
            }
        }
        // class name not set or not automatically detected
        if (null == classname) {
            classname = GregorianCalendar.class.getCanonicalName();
        }
        info("[dataset] " + prefix + " calendar class " + classname);

        if (!getConfiguration().isNull(prefix + ".calendar.units")) {
            if (null != units) {
                warning("[dataset] " + prefix + " you are overwriting NetCDF calendar units " + units);
            }
            units = getConfiguration().getString(prefix + ".calendar.units");
        }

        // calendar units not set or not automatically detected
        if (null == units) {
            units = "seconds since 1900-01-01 0:0:0";
        }
        info("[dataset] " + prefix + " calendar units " + units);

        try {
            Calendar calendar_o = Calendar.getInstance();
            SimpleDateFormat idf = CalendarUtil.unitsAsSDF(units);
            idf.setCalendar(calendar_o);
            calendar_o.setTime(idf.parse(units));
            int year = calendar_o.get(Calendar.YEAR);
            int month = calendar_o.get(Calendar.MONTH);
            int day = calendar_o.get(Calendar.DAY_OF_MONTH);
            int hour = calendar_o.get(Calendar.HOUR_OF_DAY);
            int minute = calendar_o.get(Calendar.MINUTE);
            return (Calendar) Class.forName(classname).getConstructor(int.class, int.class, int.class, int.class, int.class).newInstance(year, month, day, hour, minute);
        } catch (ParseException | ClassNotFoundException | NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
            error("[dataset] " + prefix + " Failed to create calendar.", ex);
        }
        return null;
    }

    @Override
    public Calendar getCalendar() {
        return calendar;
    }

    @Override
    public String getKey() {
        return prefix;
    }

    @Override
    public void setUp() throws Exception {

        time_arrow = getConfiguration().getString("time.time_arrow").equals(TimeManager.TimeDirection.FORWARD.toString()) ? 1 : -1;

        enhanced = !getConfiguration().isNull(prefix + ".enhanced_mode")
                ? getConfiguration().getBoolean(prefix + ".enhanced_mode")
                : true;

        alphabetically_sorted = getConfiguration().isNull(prefix + ".alphabetically_sorted")
                ? true
                : getConfiguration().getBoolean(prefix + ".alphabetically_sorted", false);

        this.path = getConfiguration().getString(prefix + ".path");
        variableMap.putAll(DatasetUtil.mapVariables(prefix, path, false));
        if (variableMap.isEmpty()) {
            error("Failed to list any variable in dataset " + prefix, new IOException("Invalid dataset path " + path));
        }
        // sort files in targeted path
        for (String name : variableMap.keySet()) {
            if (alphabetically_sorted) {
                Collections.sort(variableMap.get(name));
            } else {
                String variable_time;
                try (NetcdfFile nc = DatasetUtil.open(variableMap.get(name).get(0), enhanced)) {
                    variable_time = DatasetUtil.findTimeVariable(nc);
                }
                if (null != variable_time) {
                    Collections.sort(variableMap.get(name), new NCComparator(variable_time, time_arrow));
                }
            }
        }

        // initialize calendar
        calendar = createCalendar();
        calendar.setTimeInMillis(0);
        t0 = getSimulationManager().getTimeManager().get_tO(calendar);

        // create grid
        grid = createGrid();
        grid.init();

        loadParameters();
    }

    AbstractDatasetVariable createVariable(String name, int nlayer, int tilingh, int tilingv) {

        return new NetcdfDatasetVariable(prefix, variableMap.get(name), name,
                nlayer, grid, tilingh, Math.min(tilingv, grid.get_nz()),
                calendar, t0,
                enhanced);
    }

    @Override
    public void init() throws Exception {

        // instantiate dataset variables
        for (String name : requiredBy.keySet()) {
            debug("[dataset] " + prefix + " request NetCDF variable " + name);
            variables.put(name, createVariable(name, NLAYER, TILING_H, TILING_V));
        }

        // initialise dataset variables
        for (AbstractDatasetVariable variable : variables.values()) {
            if (null != variable) {
                variable.init(t0, time_arrow);
            }
        }
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        double time = t0 + e.getSource().getTime();
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
        debug("[dataset] " + prefix + " variable " + name + " required by " + requiredBy.getCanonicalName());
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
