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

import java.io.File;
import org.ichthyop.event.InitializeEvent;
import org.ichthyop.event.LastStepEvent;
import org.ichthyop.event.NextStepEvent;
import org.ichthyop.event.SetupEvent;
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
import org.ichthyop.output.AbstractTracker;
import org.ichthyop.output.DepthTracker;
import org.ichthyop.util.IOTools;
import org.ichthyop.output.LatTracker;
import org.ichthyop.output.LonTracker;
import org.ichthyop.output.MortalityTracker;
import org.ichthyop.output.TimeTracker;
import org.ichthyop.output.CustomTracker;
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
    private final static String OUTPUT_KEY = "app.output";
    private int dt_record;
    private NCDimFactory dimensionFactory;
    private int i_record;
    private int record_frequency;
    private List<GeoPosition> edge;
    private List<GeoPosition> mask;
    private List<List<GeoPosition>> zoneAreas;
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
        return getConfiguration().getString(OUTPUT_KEY + "." + key);
    }

    public String getFileLocation() {
        return basename;
    }

    private String makeFileLocation() throws IOException {

        String filename = IOTools.resolvePath(getParameter("output_path"));

        if (!getConfiguration().isNull(OUTPUT_KEY + ".file_prefix")) {
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

    private void addMask() {

        mask = maskToPointCloud();
        Dimension maskDim = ncOut.addDimension("mask", mask.size());
        ncOut.addVariable("mask", DataType.FLOAT, new Dimension[]{maskDim, latlonDim});
        ncOut.addVariableAttribute("mask", "long_name", "geoposition of masked cells");
        ncOut.addVariableAttribute("mask", "unit", "lat degree north lon degree east");
    }

    private void writeMask() throws IOException, InvalidRangeException {

        ArrayFloat.D2 arrMask = new ArrayFloat.D2(mask.size(), 2);
        int i = 0;
        for (GeoPosition gp : mask) {
            arrMask.set(i, 0, (float) gp.getLatitude());
            arrMask.set(i, 1, (float) gp.getLongitude());
            i++;
        }
        ncOut.write("mask", arrMask);
    }

    private void addEdge() {

        /* Add the region edges */
        edge = edgeToPointCloud();
        Dimension edgeDim = ncOut.addDimension("edge", edge.size());
        ncOut.addVariable("edge", DataType.FLOAT, new Dimension[]{edgeDim, latlonDim});
        ncOut.addVariableAttribute("edge", "long_name", "geoposition of region edge");
        ncOut.addVariableAttribute("edge", "unit", "lat degree north lon degree east");
    }

    private void writeEdge() throws IOException, InvalidRangeException {

        ArrayFloat.D2 arrEdge = new ArrayFloat.D2(edge.size(), 2);
        int i = 0;
        for (GeoPosition gp : edge) {
            arrEdge.set(i, 0, (float) gp.getLatitude());
            arrEdge.set(i, 1, (float) gp.getLongitude());
            i++;
        }
        ncOut.write("edge", arrEdge);
    }

    private void addZones() {

        int iZone = 0;
        zoneAreas = new ArrayList();
        for (String prefix : getSimulationManager().getZoneManager().getPrefixes()) {
            for (Zone zone : getSimulationManager().getZoneManager().getZones(prefix)) {
                zoneAreas.add(iZone, zoneToPointCloud(zone));
                Dimension zoneDim = ncOut.addDimension("zone" + iZone, zoneAreas.get(iZone).size());
                ncOut.addVariable("zone" + iZone, DataType.FLOAT, new Dimension[]{zoneDim, latlonDim});
                ncOut.addVariableAttribute("zone" + iZone, "long_name", zone.getName());
                ncOut.addVariableAttribute("zone" + iZone, "unit", "latitude longitude coordinates of the center of the cells in the zone");
                ncOut.addVariableAttribute("zone" + iZone, "key", zone.getKey());
                ncOut.addVariableAttribute("zone" + iZone, "color", zone.getColor().getRGB());
                iZone++;
            }
        }
        ncOut.addGlobalAttribute("number_of_zones", iZone);
    }

    private void writeZones() throws IOException, InvalidRangeException {

        int iZone = 0;
        for (List<GeoPosition> zoneArea : zoneAreas) {
            ArrayFloat.D2 arrZoneArea = new ArrayFloat.D2(zoneArea.size(), 2);
            int i = 0;
            for (GeoPosition gp : zoneArea) {
                arrZoneArea.set(i, 0, (float) gp.getLatitude());
                arrZoneArea.set(i, 1, (float) gp.getLongitude());
                i++;
            }
            ncOut.write("zone" + iZone, arrZoneArea);
            iZone++;
        }
    }

    private void addGlobalAttributes() {

        // Write parameters from enabled subsets
        for (String subsetKey : getConfiguration().getParameterSubsets()) {
            if (!getConfiguration().canFind(subsetKey + ".enabled") || getConfiguration().getBoolean(subsetKey + ".enabled", false)) {
                for (String paramKey : getConfiguration().getArrayString(subsetKey + ".parameters")) {
                    String key = subsetKey + "." + paramKey;
                    if (getConfiguration().canFind(key)) {
                        ncOut.addGlobalAttribute(key, getConfiguration().getString(key));
                    }
                }
            }
        }

        // Add the corresponding configuration file 
        ncOut.addGlobalAttribute("cfgfile", getConfiguration().getMainFile());
    }

    private List<GeoPosition> zoneToPointCloud(Zone zone) {
        List<GeoPosition> list = new ArrayList();

        int imin = (int) Math.floor(zone.getXmin());
        int imax = (int) Math.ceil(zone.getXmax());
        int jmin = (int) Math.floor(zone.getYmin());
        int jmax = (int) Math.ceil(zone.getYmax());

        for (int i = imin; i < imax; i += 2) {
            for (int j = jmin; j < jmax; j += 2) {
                if (zone.isGridPointInZone(i, j)) {
                    double[] latlon = getSimulationManager().getDataset().xy2latlon(i, j);
                    list.add(new GeoPosition(latlon[0], latlon[1] > 180 ? latlon[1] - 360.d : latlon[1]));
                }
            }
        }

        return list;
    }

    private List<GeoPosition> edgeToPointCloud() {

        List<GeoPosition> lregion = new ArrayList();
        IDataset dataset = getSimulationManager().getDataset();
        for (int i = 1; i < dataset.get_nx(); i += 2) {
            if (!Double.isNaN(dataset.getLat(i, 0)) && !Double.isNaN(dataset.getLon(i, 0))) {
                double lon = dataset.getLon(i, 0);
                if (lon > 180) {
                    lon = lon - 360.d;
                }
                lregion.add(new GeoPosition(dataset.getLat(i, 0), lon));
            }
        }
        for (int j = 1; j < dataset.get_ny(); j += 2) {
            if (!Double.isNaN(dataset.getLat(dataset.get_nx() - 1, j)) && !Double.isNaN(dataset.getLon(dataset.get_nx() - 1, j))) {
                double lon = dataset.getLon(dataset.get_nx() - 1, j);
                if (lon > 180) {
                    lon = lon - 360.d;
                }
                lregion.add(new GeoPosition(dataset.getLat(dataset.get_nx() - 1, j), lon));
            }
        }
        for (int i = dataset.get_nx() - 1; i > 0; i -= 2) {
            if (!Double.isNaN(dataset.getLat(i, dataset.get_ny() - 1)) && !Double.isNaN(dataset.getLon(i, dataset.get_ny() - 1))) {
                double lon = dataset.getLon(i, dataset.get_ny() - 1);
                if (lon > 180) {
                    lon = lon - 360.d;
                }
                lregion.add(new GeoPosition(dataset.getLat(i, dataset.get_ny() - 1), lon));
            }
        }
        for (int j = dataset.get_ny() - 1; j > 0; j -= 2) {
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

    public List<GeoPosition> maskToPointCloud() {

        List<GeoPosition> lmask = new ArrayList();
        for (int i = 0; i < getSimulationManager().getDataset().get_nx(); i += 2) {
            for (int j = 0; j < getSimulationManager().getDataset().get_ny(); j += 2) {
                if (!getSimulationManager().getDataset().isInWater(i, j)) {
                    double[] latlon = getSimulationManager().getDataset().xy2latlon(i, j);
                    lmask.add(new GeoPosition(latlon[0], latlon[1] > 180 ? latlon[1] - 360.d : latlon[1]));
                }
            }
        }
        return lmask;
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

        if (!getConfiguration().isNull(OUTPUT_KEY + ".trackers")) {
            try {
                String[] tokens = getParameter("trackers").split("\"");
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
        return null;
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

        // Create the NetCDF writeable object
        ncOut = NetcdfFileWriteable.createNew("");
        ncOut.setLocation(makeFileLocation());

        // Get record frequency
        record_frequency = Integer.valueOf(getParameter("record_frequency"));
        dt_record = record_frequency * Math.abs(getSimulationManager().getTimeManager().get_dt());

        // Reset NetCDF dimensions
        getDimensionFactory().resetDimensions();

        // add application trackers lon lat depth time
        addPredefinedTrackers();

        // add custom trackers
        addCustomTrackers(customTrackers);

        // add user defined trackers
        addCustomTrackers(getUserTrackers());

        // Initialize all trackers
        List<AbstractTracker> errTrackers = new ArrayList();
        for (AbstractTracker tracker : trackers) {
            try {
                tracker.init();
                ncOut.addVariable(tracker.getName(), tracker.getDataType(), tracker.getDimensions());
            } catch (Exception ex) {
                errTrackers.add(tracker);
                warning("Error adding variable \"" + tracker.getName() + "\" in NetCDF output file. The variable will not be recorded.", ex);
            }
        }
        trackers.removeAll(errTrackers);

        // add gloabal attributes
        addGlobalAttributes();

        // add edge of the simulated area
        latlonDim = ncOut.addDimension("latlon", 2);
        addEdge();

        clearPredefinedTrackerList = true;
        clearCustomTrackerList = true;

        info("Output manager setup [OK]");
    }

    @Override
    public void initializePerformed(InitializeEvent e) throws Exception {

        /* add the zones and the mask
         * It cannnot be done in the setup because the definition of the zones
         * requires that dataset has been initialized first.
         */
        addMask();
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
            writeEdge();
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("Problem occured writing the simulation area in the NetCDF output file == >");
            sb.append(ex.toString());
            sb.append("\n");
            sb.append("Map creation might not work correctly later on.");
            warning(sb.toString());
        }

        /* write the definition of the simulated area */
        try {
            writeMask();
        } catch (Exception ex) {
            StringBuilder sb = new StringBuilder();
            sb.append("Problem occured writing the mask in the NetCDF output file == >");
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
        private HashMap<String, Dimension> zoneDimension;
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

        public Dimension getZoneDimension(String classname) {
            if (null == zoneDimension) {
                zoneDimension = new HashMap();
            }
            if (null == zoneDimension.get(classname)) {
                String name = "zone_" + classname.substring(classname.lastIndexOf(".") + 1).toLowerCase();
                Dimension zoneDim = ncOut.addDimension(name, getSimulationManager().getZoneManager().getZones(classname).size());
                zoneDimension.put(classname, zoneDim);
                dimensions.put(zoneDim.getName(), zoneDim);
            }
            return zoneDimension.get(classname);
        }

        public void resetDimensions() {
            time = null;
            drifter = null;
            zoneDimension = null;
            dimensions = new HashMap();
        }
    }
}
