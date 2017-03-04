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
package org.ichthyop.manager;

import java.awt.geom.Point2D;
import java.io.File;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.LastStepEvent;
import org.ichthyop.event.NextStepEvent;
import org.ichthyop.TypeZone;
import org.ichthyop.event.SetupEvent;
import org.ichthyop.io.BlockType;
import java.io.IOException;
import ucar.nc2.NetcdfFileWriteable;
import org.ichthyop.event.LastStepListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.ichthyop.Zone;
import org.ichthyop.dataset.IDataset;
import org.ichthyop.event.NextStepListener;
import org.ichthyop.io.AbstractTracker;
import org.ichthyop.io.DepthTracker;
import org.ichthyop.io.IOTools;
import org.ichthyop.io.LatTracker;
import org.ichthyop.io.LonTracker;
import org.ichthyop.io.MortalityTracker;
import org.ichthyop.io.TimeTracker;
import org.ichthyop.io.CustomTracker;
import org.ichthyop.io.ParameterSet;
import org.ichthyop.io.Parameter;
import ucar.ma2.ArrayFloat;
import ucar.ma2.DataType;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;

/**
 *
 * @author pverley
 */
public class OutputManager extends AbstractManager implements LastStepListener, NextStepListener {

    final private static OutputManager outputManager = new OutputManager();
    private final static String block_key = "app.output";
    private int dt_record;
    private NCDimFactory dimensionFactory;
    private int i_record;
    private int record_frequency;
    private List<GeoPosition> region;
    private List<List<Point2D>> zoneAreas;
    private Dimension latlonDim;
    private boolean clearPredefinedTrackerList = false;
    private boolean clearCustomTrackerList = false;
    /**
     * Object for creating/writing netCDF files.
     */
    private static NetcdfFileWriteable ncOut;
    /**
     *
     */
    private List<AbstractTracker> trackers;
    private List<Class> predefinedTrackers;
    private List<String> customTrackers;
    private String basename;

    public static OutputManager getInstance() {
        return outputManager;
    }

    public NCDimFactory getDimensionFactory() {
        if (null == dimensionFactory) {
            dimensionFactory = new NCDimFactory();
        }
        return dimensionFactory;
    }

    public String getParameter(String key) {
        return getSimulationManager().getParameterManager().getString(block_key + "." + key);
    }

    public String getFileLocation() {
        return basename;
    }

    private String makeFileLocation() throws IOException {

        String filename = IOTools.resolvePath(getParameter("output_path"));
        if (!getParameter("file_prefix").isEmpty()) {
            filename += getParameter("file_prefix") + "_";
        }
        filename += getSimulationManager().getId() + ".nc";
        File file = new File(filename);
        try {
            IOTools.makeDirectories(file.getAbsolutePath());
            file.createNewFile();
            file.delete();
        } catch (Exception ex) {
            IOException ioex = new IOException("{Ouput} Failed to create NetCDF file " + filename + " ==> " + ex.getMessage());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        basename = filename;
        return filename + ".part";
    }

    /**
     * Closes the NetCDF file.
     */
    private void close() {
        try {
            ncOut.close();
            String strFilePart = ncOut.getLocation();
            String strFileBase = strFilePart.substring(0, strFilePart.indexOf(".part"));
            File filePart = new File(strFilePart);
            File fileBase = new File(strFileBase);
            filePart.renameTo(fileBase);
            info("Closed NetCDF output file.");
        } catch (Exception ex) {
            warning("Problem closing the NetCDF output file ==> {0}", ex.toString());
        }
    }

    private void addRegion() {

        /* Add the region edges */
        region = makeRegion();
        Dimension edge = ncOut.addDimension("edge", region.size());
        latlonDim = ncOut.addDimension("latlon", 2);
        ncOut.addVariable("region_edge", DataType.FLOAT, new Dimension[]{edge, latlonDim});
        ncOut.addVariableAttribute("region_edge", "long_name", "geoposition of region edge");
        ncOut.addVariableAttribute("region_edge", "unit", "lat degree north lon degree east");
    }

    private void writeRegion() throws IOException, InvalidRangeException {

        ArrayFloat.D2 edge = new ArrayFloat.D2(region.size(), 2);
        int i = 0;
        for (GeoPosition gp : region) {
            edge.set(i, 0, (float) gp.getLatitude());
            edge.set(i, 1, (float) gp.getLongitude());
            i++;
        }
        ncOut.write("region_edge", edge);
    }

    private void addZones() {

        int iZone = 0;
        zoneAreas = new ArrayList();
        for (TypeZone type : TypeZone.values()) {
            if (null != getSimulationManager().getZoneManager().getZones(type)) {
                for (Zone zone : getSimulationManager().getZoneManager().getZones(type)) {
                    zoneAreas.add(iZone, makeZoneArea(zone));
                    Dimension zoneDim = ncOut.addDimension("zone" + iZone, zoneAreas.get(iZone).size());
                    ncOut.addVariable("zone" + iZone, DataType.FLOAT, new Dimension[]{zoneDim, latlonDim});
                    ncOut.addVariableAttribute("zone" + iZone, "long_name", zone.getKey());
                    ncOut.addVariableAttribute("zone" + iZone, "unit", "x and y coordinates of the center of the cells in the zone");
                    ncOut.addVariableAttribute("zone" + iZone, "type", zone.getType().toString());
                    String color = zone.getColor().toString();
                    color = color.substring(color.lastIndexOf("["));
                    ncOut.addVariableAttribute("zone" + iZone, "color", color);
                    iZone++;
                }
            }
        }
        ncOut.addGlobalAttribute("nb_zones", iZone);
    }

    private void writeZones() throws IOException, InvalidRangeException {

        int iZone = 0;
        for (List<Point2D> zoneArea : zoneAreas) {
            ArrayFloat.D2 arrZoneArea = new ArrayFloat.D2(zoneArea.size(), 2);
            int i = 0;
            for (Point2D xy : zoneArea) {
                arrZoneArea.set(i, 0, (float) xy.getX());
                arrZoneArea.set(i, 1, (float) xy.getY());
                i++;
            }
            ncOut.write("zone" + iZone, arrZoneArea);
            iZone++;
        }
    }

    private void addGlobalAttributes() {

        /* Add transport dimension */
        String dim = getSimulationManager().getDataset().is3D()
                ? "3d"
                : "2d";
        ncOut.addGlobalAttribute("transport_dimension", dim);

        /* Write all parameters */
        for (String key : getConfiguration().getParameterSets()) {
            ParameterSet parameterSet = new ParameterSet(key);
            if (!parameterSet.getType().equals(BlockType.OPTION)) {
                ncOut.addGlobalAttribute(parameterSet.getKey() + ".enabed", String.valueOf(parameterSet.isEnabled()));
            }
            if (parameterSet.isEnabled()) {
                for (Parameter param : parameterSet.getParameters()) {
                    ncOut.addGlobalAttribute(param.getKey(), param.getValue());
                }
            }
        }

        /* Add the corresponding xml file */
        ncOut.addGlobalAttribute("xml_file", getSimulationManager().getConfigurationFile().getAbsolutePath());
    }

    private List<Point2D> makeZoneArea(Zone zone) {
        List<Point2D> list = new ArrayList();

        int xmin = (int) Math.floor(zone.getXmin());
        int xmax = (int) Math.ceil(zone.getXmax());
        int ymin = (int) Math.floor(zone.getYmin());
        int ymax = (int) Math.ceil(zone.getYmax());

        for (float i = xmin; i < xmax; i++) {
            for (float j = ymin; j < ymax; j++) {
                if (zone.isGridPointInZone(i, j)) {
                    Point2D xy = new Point2D.Float(i, j);
                    list.add(xy);
                }
            }
        }

        return list;
    }

    private double getDistance(Point2D.Float pt1, Point2D.Float pt2) {
        return Math.sqrt(Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2));
    }

    private List<GeoPosition> makeRegion() {

        final List<GeoPosition> lregion = new ArrayList<>();
        IDataset dataset = getSimulationManager().getDataset();
        for (int i = 1; i < dataset.get_nx(); i++) {
            if (!Double.isNaN(dataset.getLat(i, 0)) && !Double.isNaN(dataset.getLon(i, 0))) {
                double lon = dataset.getLon(i, 0);
                if (lon > 180) {
                    lon = lon - 360.d;
                }
                lregion.add(new GeoPosition(dataset.getLat(i, 0), lon));
            }
        }
        for (int j = 1; j < dataset.get_ny(); j++) {
            if (!Double.isNaN(dataset.getLat(dataset.get_nx() - 1, j)) && !Double.isNaN(dataset.getLon(dataset.get_nx() - 1, j))) {
                double lon = dataset.getLon(dataset.get_nx() - 1, j);
                if (lon > 180) {
                    lon = lon - 360.d;
                }
                lregion.add(new GeoPosition(dataset.getLat(dataset.get_nx() - 1, j), lon));
            }
        }
        for (int i = dataset.get_nx() - 1; i > 0; i--) {
            if (!Double.isNaN(dataset.getLat(i, dataset.get_ny() - 1)) && !Double.isNaN(dataset.getLon(i, dataset.get_ny() - 1))) {
                double lon = dataset.getLon(i, dataset.get_ny() - 1);
                if (lon > 180) {
                    lon = lon - 360.d;
                }
                lregion.add(new GeoPosition(dataset.getLat(i, dataset.get_ny() - 1), lon));
            }
        }
        for (int j = dataset.get_ny() - 1; j > 0; j--) {
            if (!Double.isNaN(dataset.getLat(0, j)) && !Double.isNaN(dataset.getLon(0, j))) {
                double lon = dataset.getLon(0, j);
                if (lon > 180) {
                    lon = lon - 360.d;
                }
                lregion.add(new GeoPosition(dataset.getLat(0, j), lon));
            }
        }
        return lregion;
    }

    public void addPredefinedTracker(Class trackerClass) {
        if (null == predefinedTrackers) {
            predefinedTrackers = new ArrayList();
        }
        if (clearPredefinedTrackerList) {
            predefinedTrackers.clear();
            clearPredefinedTrackerList = false;
        }
        if (!predefinedTrackers.contains(trackerClass)) {
            predefinedTrackers.add(trackerClass);
        }
    }

    public void addCustomTracker(String variableName) {
        if (null == customTrackers) {
            customTrackers = new ArrayList();
        }
        if (clearCustomTrackerList) {
            customTrackers.clear();
            clearCustomTrackerList = false;
        }
        if (!customTrackers.contains(variableName)) {
            customTrackers.add(variableName);
        }
    }

    private void addPredefinedTrackers() throws Exception {
        trackers = new ArrayList();
        trackers.add(new TimeTracker());
        trackers.add(new LonTracker());
        trackers.add(new LatTracker());
        trackers.add(new MortalityTracker());
        if (getSimulationManager().getDataset().is3D()) {
            trackers.add(new DepthTracker());
        }
        /* Add trackers requested by external actions */
        if (null != predefinedTrackers) {
            for (Class trackerClass : predefinedTrackers) {
                try {
                    AbstractTracker tracker = (AbstractTracker) trackerClass.newInstance();
                    trackers.add(tracker);
                } catch (Exception ex) {
                    warning("Error adding tracker " + trackerClass.getSimpleName() + " in NetCDF output file. The variable will not be recorded.", ex);
                }
            }
        }
    }

    private List<String> getUserTrackers() throws Exception {
        String userTrackers = getParameter("trackers");
        try {
            String[] tokens = userTrackers.split("\"");
            List<String> variables = new ArrayList();
            for (String token : tokens) {
                if (!token.trim().isEmpty()) {
                    variables.add(token.trim());
                }
            }
            return variables;
        } catch (Exception ex) {
            return null;
        }
    }

    private void addCustomTrackers(List<String> variables) {

        if (null != variables) {
            for (String variable : variables) {
                trackers.add(new CustomTracker(variable));
            }
        }
    }

    @Override
    public void nextStepTriggered(NextStepEvent e) throws Exception {

        if (e.isInterrupted()) {
            return;
        }
        TimeManager timeManager = e.getSource();
        if (((long) (timeManager.getTime() - timeManager.get_tO()) % dt_record) == 0) {
            writeToNetCDF(i_record++);
        }
    }

    private void writeToNetCDF(int i_record) {
        info("Saving variables...");
        List<AbstractTracker> errTrackers = new ArrayList();
        for (AbstractTracker tracker : trackers) {
            if (tracker.isEnabled()) {
                /* Retrieve the values of the variable */
                try {
                    tracker.track();
                } catch (Exception ex) {
                    errTrackers.add(tracker);
                    getSimulationManager().getDataset().removeRequiredVariable(tracker.getName(), tracker.getClass());
                    warning("Error tracking variable " + tracker.getName() + ". The variable will no longer be recorded in the NetCDF output file.", ex);
                    continue;
                }
                /* Write the current time step in the NetCDF file */
                try {
                    ncOut.write(tracker.getName(), tracker.origin(i_record), tracker.getArray());
                } catch (Exception ex) {
                    errTrackers.add(tracker);
                    getSimulationManager().getDataset().removeRequiredVariable(tracker.getName(), tracker.getClass());
                    warning("Error writing variable " + tracker.getName() + ". The variable will no longer be recorded in the NetCDF output file.", ex);
                }
            }
        }

        /* Remove trackers that caused error */
        trackers.removeAll(errTrackers);
    }

    @Override
    public void lastStepOccurred(LastStepEvent e) {
        if (!e.isInterrupted()) {
            writeToNetCDF(i_record);
        }
        close();
        if (null != predefinedTrackers) {
            predefinedTrackers.clear();
        }
        if (null != customTrackers) {
            customTrackers.clear();
        }
    }

    @Override
    public void setupPerformed(SetupEvent e) throws Exception {

        /* Create the NetCDF writeable object */
        ncOut = NetcdfFileWriteable.createNew("");
        ncOut.setLocation(makeFileLocation());

        /* Get record frequency */
        record_frequency = Integer.valueOf(getParameter("record_frequency"));
        dt_record = record_frequency * Math.abs(getSimulationManager().getTimeManager().get_dt());

        /* Reset NetCDF dimensions */
        getDimensionFactory().resetDimensions();

        /* add application trackers lon lat depth time */
        addPredefinedTrackers();

        /* add custom trackers */
        addCustomTrackers(customTrackers);

        /* add user defined trackers */
        addCustomTrackers(getUserTrackers());

        /* Initialize all trackers */
        List<AbstractTracker> errTrackers = new ArrayList();
        for (AbstractTracker tracker : trackers) {
            try {
                tracker.init();
                ncOut.addVariable(tracker.getName(), tracker.getDataType(), tracker.getDimensions());
            } catch (Exception ex) {
                errTrackers.add(tracker);
                warning("Error adding tracker " + tracker.getName() + " in NetCDF output file. The variable will not be recorded.", ex);
            }
        }
        trackers.removeAll(errTrackers);

        /* add gloabal attributes */
        addGlobalAttributes();

        /* add definition of the simulated area */
        addRegion();

        clearPredefinedTrackerList = true;
        clearCustomTrackerList = true;

        info("Output manager setup [OK]");
    }

    @Override
    public void initializePerformed(InitializeEvent e) throws Exception {

        /* add the zones
         * It cannnot be done in the setup because the definition of the zones
         * requires that dataset has been initialized first.
         */
        addZones();

        // Add attributes
        for (AbstractTracker tracker : trackers) {
            tracker.addRuntimeAttributes();
            try {
                if (tracker.getAttributes() != null) {
                    for (Attribute attribute : tracker.getAttributes()) {
                        ncOut.addVariableAttribute(tracker.getName(), attribute);
                    }
                }
            } catch (Exception ex) {
                // do nothing, attributes have minor importance
            }
        }

        /* add listeners */
        getSimulationManager().getTimeManager().addNextStepListener(this);
        getSimulationManager().getTimeManager().addLastStepListener(this);

        /* reset counter */
        i_record = 0;

        /* create the NetCDF file */
        try {
            IOTools.makeDirectories(ncOut.getLocation());
            ncOut.create();
        } catch (Exception ex) {
            IOException ioex = new IOException("Failed to create NetCDF output file ==> " + ex.toString());
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }

        /* write the definition of the simulated area */
        try {
            writeRegion();
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("Problem occured writing the simulation area in the NetCDF output file == >");
            sb.append(ex.toString());
            sb.append("\n");
            sb.append("Map creation might not work correctly later on.");
            warning(sb.toString());
        }

        /* write the zones */
        try {
            writeZones();
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("Problem occured writing the zones in the NetCDF output file == >");
            sb.append(ex.toString());
            sb.append("\n");
            sb.append("Map creation might not work correctly later on.");
            warning(sb.toString());
        }

        /* initialization completed */
        info("Created output file {0}", ncOut.getLocation());
        info("Output manager initialization [OK]");
    }

    public class NCDimFactory {

        private Dimension time, drifter;
        private HashMap<TypeZone, Dimension> zoneDimension;
        private HashMap<String, Dimension> dimensions;

        public Dimension createDimension(Dimension dim) {
            if (dimensions.containsKey(dim.getName())) {
                if (dim.getLength() != dimensions.get(dim.getName()).getLength()) {
                    throw new IllegalArgumentException("Dimension (" + dim.getName() + ") has already been defined with a different length.");
                } else {
                    return dimensions.get(dim.getName());
                }
            } else {
                Dimension newDim = ncOut.addDimension(dim.getName(), dim.getLength());
                dimensions.put(newDim.getName(), newDim);
                return newDim;
            }
        }

        public Dimension getTimeDimension() {
            if (null == time) {
                time = ncOut.addUnlimitedDimension("time");
                dimensions.put(time.getName(), time);
            }
            return time;
        }

        public Dimension getDrifterDimension() {
            if (null == drifter) {
                drifter = ncOut.addDimension("drifter", getSimulationManager().getReleaseManager().getNbParticles());
                dimensions.put(drifter.getName(), drifter);
            }
            return drifter;
        }

        public Dimension getZoneDimension(TypeZone type) {
            if (null == zoneDimension) {
                zoneDimension = new HashMap();
            }
            if (null == zoneDimension.get(type)) {
                String name = type.toString() + "_zone";
                Dimension zoneDim = ncOut.addDimension(name, getSimulationManager().getZoneManager().getZones(type).size());
                zoneDimension.put(type, zoneDim);
                dimensions.put(zoneDim.getName(), zoneDim);
            }
            return zoneDimension.get(type);
        }

        public void resetDimensions() {
            time = null;
            drifter = null;
            zoneDimension = null;
            dimensions = new HashMap();
        }
    }
}
