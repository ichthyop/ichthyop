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
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import org.ichthyop.event.LastStepListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.ichthyop.Zone;
import org.ichthyop.dataset.IDataset;
import org.ichthyop.event.NextStepListener;
import org.ichthyop.grid.IGrid;
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
import ucar.nc2.NetcdfFileWriter;

/**
 *
 * @author pverley
 */
public class OutputManager extends AbstractManager implements LastStepListener, NextStepListener {

    final private static OutputManager OUTPUT_MANAGER = new OutputManager();
    final private static String OUTPUT_KEY = "app.output";
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
    private NetcdfFileWriter ncOut;
    private List<AbstractTracker> trackers;
    private List<Class> predefinedTrackers;
    private List<String> customTrackers;
    private String basename;

    public static OutputManager getInstance() {
        return OUTPUT_MANAGER;
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

    private String makeFileLocation() {

        String filename = IOTools.resolvePath(getParameter("output_path"));

        if (!getConfiguration().isNull(OUTPUT_KEY + ".file_prefix")) {
            filename += getParameter("file_prefix") + "_";
        }
        filename += getSimulationManager().getId() + ".nc";
        File file = new File(filename);
        try {
            IOTools.makeDirectories(file.getAbsolutePath());
        } catch (SecurityException ex) {
            error("[output] Failed to create output directory " + file.getParent(), ex);
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
            String strFilePart = ncOut.getNetcdfFile().getLocation();
            String strFileBase = strFilePart.substring(0, strFilePart.indexOf(".part"));
            Path filePart = new File(strFilePart).toPath();
            Path fileBase = new File(strFileBase).toPath();
            Files.move(filePart, fileBase, REPLACE_EXISTING);
            info("[output] Closed NetCDF output file.");
        } catch (IOException ex) {
            warning("[output] Error closing NetCDF output file.", ex);
        }
    }

    private void addMask() {

        mask = maskToPointCloud();
        Dimension maskDim = ncOut.addDimension(null, "mask", mask.size());
        ncOut.addVariable(null, "mask", DataType.FLOAT, Arrays.asList(new Dimension[]{maskDim, latlonDim}));
        ncOut.addVariableAttribute(ncOut.findVariable("mask"), new Attribute("long_name", "geoposition of masked cells"));
        ncOut.addVariableAttribute(ncOut.findVariable("mask"), new Attribute("unit", "lat degree north lon degree east"));
    }

    private void writeMask() throws IOException, InvalidRangeException {

        ArrayFloat.D2 arrMask = new ArrayFloat.D2(mask.size(), 2);
        int i = 0;
        for (GeoPosition gp : mask) {
            arrMask.set(i, 0, (float) gp.getLatitude());
            arrMask.set(i, 1, (float) gp.getLongitude());
            i++;
        }
        ncOut.write(ncOut.findVariable("mask"), arrMask);
    }

    private void addEdge() {

        /* Add the region edges */
        edge = edgeToPointCloud();
        Dimension edgeDim = ncOut.addDimension(null, "edge", edge.size());
        ncOut.addVariable(null, "edge", DataType.FLOAT, Arrays.asList(new Dimension[]{edgeDim, latlonDim}));
        ncOut.addVariableAttribute(ncOut.findVariable("edge"), new Attribute("long_name", "geopositions of edge"));
        ncOut.addVariableAttribute(ncOut.findVariable("edge"), new Attribute("unit", "lat degree north lon degree east"));
    }

    private void writeEdge() throws IOException, InvalidRangeException {

        ArrayFloat.D2 arrEdge = new ArrayFloat.D2(edge.size(), 2);
        int i = 0;
        for (GeoPosition gp : edge) {
            arrEdge.set(i, 0, (float) gp.getLatitude());
            arrEdge.set(i, 1, (float) gp.getLongitude());
            i++;
        }
        ncOut.write(ncOut.findVariable("edge"), arrEdge);
    }

    private void addZones() {

        int iZone = 0;
        zoneAreas = new ArrayList();
        for (Zone zone : getSimulationManager().getZoneManager().getZones()) {
            zoneAreas.add(iZone, zoneToPointCloud(zone));
            Dimension zoneDim = ncOut.addDimension(null, "zone" + iZone, zoneAreas.get(iZone).size());
            ncOut.addVariable(null, "zone" + iZone, DataType.FLOAT, Arrays.asList(new Dimension[]{zoneDim, latlonDim}));
            ncOut.addVariableAttribute(ncOut.findVariable("zone" + iZone), new Attribute("long_name", zone.getName()));
            ncOut.addVariableAttribute(ncOut.findVariable("zone" + iZone), new Attribute("unit", "latitude longitude coordinates of the center of the cells in the zone"));
            ncOut.addVariableAttribute(ncOut.findVariable("zone" + iZone), new Attribute("key", zone.getKey()));
            ncOut.addVariableAttribute(ncOut.findVariable("zone" + iZone), new Attribute("color", zone.getColor().getRGB()));
            iZone++;
        }
        ncOut.addGroupAttribute(null, new Attribute("number_of_zones", iZone));
    }

    private void writeZone(int index) throws IOException, InvalidRangeException {

        List<GeoPosition> zoneArea = zoneAreas.get(index);
        ArrayFloat.D2 arrZoneArea = new ArrayFloat.D2(zoneArea.size(), 2);
        int i = 0;
        for (GeoPosition gp : zoneArea) {
            arrZoneArea.set(i, 0, (float) gp.getLatitude());
            arrZoneArea.set(i, 1, (float) gp.getLongitude());
            i++;
        }
        ncOut.write(ncOut.findVariable("zone" + index), arrZoneArea);
    }

    private void addGlobalAttributes() {

        // Write parameters from enabled subsets
        for (String subsetKey : getConfiguration().getParameterSubsets()) {
            if (!getConfiguration().canFind(subsetKey + ".enabled") || getConfiguration().getBoolean(subsetKey + ".enabled", false)) {
                if (!getConfiguration().isNull(subsetKey + ".parameters")) {
                    for (String paramKey : getConfiguration().getArrayString(subsetKey + ".parameters")) {
                        String key = subsetKey + "." + paramKey;
                        if (getConfiguration().canFind(key)) {
                            ncOut.addGroupAttribute(null, new Attribute(key, getConfiguration().getString(key)));
                        }
                    }
                }
            }
        }

        // Add the corresponding configuration file 
        ncOut.addGroupAttribute(null, new Attribute("cfgfile", getConfiguration().getMainFile()));
    }

    private List<GeoPosition> zoneToPointCloud(Zone zone) {
        List<GeoPosition> list = new ArrayList();

        for (int i = 0; i < getSimulationManager().getDataset().getGrid().get_nx(); i += 2) {
            for (int j = 0; j < getSimulationManager().getDataset().getGrid().get_ny(); j += 2) {
                if (getSimulationManager().getZoneManager().isInside(i, j, zone.getKey())) {
                    double[] latlon = getSimulationManager().getDataset().getGrid().xy2latlon(i, j);
                    list.add(new GeoPosition(latlon[0], latlon[1] > 180 ? latlon[1] - 360.d : latlon[1]));
                }
            }
        }

        return list;
    }

    private List<GeoPosition> edgeToPointCloud() {

        List<GeoPosition> lregion = new ArrayList();
        IGrid grid = getSimulationManager().getDataset().getGrid();
        for (int i = 1; i < grid.get_nx(); i += 2) {
            if (!Double.isNaN(grid.getLat(i, 0)) && !Double.isNaN(grid.getLon(i, 0))) {
                double lon = grid.getLon(i, 0);
                if (lon > 180) {
                    lon = lon - 360.d;
                }
                lregion.add(new GeoPosition(grid.getLat(i, 0), lon));
            }
        }
        for (int j = 1; j < grid.get_ny(); j += 2) {
            if (!Double.isNaN(grid.getLat(grid.get_nx() - 1, j)) && !Double.isNaN(grid.getLon(grid.get_nx() - 1, j))) {
                double lon = grid.getLon(grid.get_nx() - 1, j);
                if (lon > 180) {
                    lon = lon - 360.d;
                }
                lregion.add(new GeoPosition(grid.getLat(grid.get_nx() - 1, j), lon));
            }
        }
        for (int i = grid.get_nx() - 1; i > 0; i -= 2) {
            if (!Double.isNaN(grid.getLat(i, grid.get_ny() - 1)) && !Double.isNaN(grid.getLon(i, grid.get_ny() - 1))) {
                double lon = grid.getLon(i, grid.get_ny() - 1);
                if (lon > 180) {
                    lon = lon - 360.d;
                }
                lregion.add(new GeoPosition(grid.getLat(i, grid.get_ny() - 1), lon));
            }
        }
        for (int j = grid.get_ny() - 1; j > 0; j -= 2) {
            if (!Double.isNaN(grid.getLat(0, j)) && !Double.isNaN(grid.getLon(0, j))) {
                double lon = grid.getLon(0, j);
                if (lon > 180) {
                    lon = lon - 360.d;
                }
                lregion.add(new GeoPosition(grid.getLat(0, j), lon));
            }
        }
        return lregion;
    }

    public List<GeoPosition> maskToPointCloud() {

        List<GeoPosition> lmask = new ArrayList();
        for (int i = 0; i < getSimulationManager().getDataset().getGrid().get_nx(); i += 2) {
            for (int j = 0; j < getSimulationManager().getDataset().getGrid().get_ny(); j += 2) {
                if (!getSimulationManager().getDataset().getGrid().isInWater(i, j)) {
                    double[] latlon = getSimulationManager().getDataset().getGrid().xy2latlon(i, j);
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

    private void setupPredefinedTrackers() throws Exception {
        trackers = new ArrayList();
        trackers.add(new TimeTracker());
        trackers.add(new LonTracker());
        trackers.add(new LatTracker());
        trackers.add(new MortalityTracker());
        if (getSimulationManager().getDataset().getGrid().is3D()) {
            trackers.add(new DepthTracker());
        }
        /* Add trackers requested by external actions */
        if (null != predefinedTrackers) {
            for (Class trackerClass : predefinedTrackers) {
                try {
                    AbstractTracker tracker = (AbstractTracker) trackerClass.newInstance();
                    trackers.add(tracker);
                } catch (InstantiationException | IllegalAccessException ex) {
                    warning("[output] Error adding NetCDF variable \"" + trackerClass.getSimpleName() + "\". The variable will not be recorded.", ex);
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

    private void setupCustomTrackers(List<String> variables) {

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

        // Create NetCDF on first time step
        if (timeManager.getTime() - timeManager.get_tO() == 0) {
            createNetCDF();
        }

        // write current step
        if (((long) (timeManager.getTime() - timeManager.get_tO()) % dt_record) == 0) {
            writeToNetCDF(i_record++);
        }
    }

    private void writeToNetCDF(int i_record) {
        info("[output] Saving variables...");
        List<AbstractTracker> errTrackers = new ArrayList();
        for (AbstractTracker tracker : trackers) {
            if (tracker.isEnabled()) {
                /* Retrieve the values of the variable */
                try {
                    tracker.track();
                } catch (Exception ex) {
                    errTrackers.add(tracker);
                    getSimulationManager().getDataset().removeRequiredVariable(tracker.getName(), tracker.getClass());
                    warning("Error tracking variable \"" + tracker.getName() + "\". The variable will no longer be recorded in the NetCDF output file.", ex);
                    continue;
                }
                /* Write the current time step in the NetCDF file */
                try {
                    ncOut.write(ncOut.findVariable(tracker.getName()), tracker.origin(i_record), tracker.getArray());
                } catch (IOException | InvalidRangeException ex) {
                    errTrackers.add(tracker);
                    getSimulationManager().getDataset().removeRequiredVariable(tracker.getName(), tracker.getClass());
                    warning("Error writing variable \"" + tracker.getName() + "\". The variable will no longer be recorded in the NetCDF output file.", ex);
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
        writeUIVariables();
        close();
        if (null != predefinedTrackers) {
            predefinedTrackers.clear();
        }
        if (null != customTrackers) {
            customTrackers.clear();
        }
    }

    private void createNetCDF() throws IOException {

        // create the NetCDF writeable object
        ncOut = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf3, makeFileLocation());

        // initialize all trackers
        List<AbstractTracker> errTrackers = new ArrayList();
        for (AbstractTracker tracker : trackers) {
            try {
                tracker.init();
                ncOut.addVariable(null, tracker.getName(), tracker.getDataType(), tracker.getDimensions());
            } catch (Exception ex) {
                errTrackers.add(tracker);
                warning("[output] Error adding variable \"" + tracker.getName() + "\" in NetCDF output file. The variable will not be recorded.", ex);
            }
        }

        // remove faulty trackers 
        trackers.removeAll(errTrackers);

        // add output variables runtime attributes
        for (AbstractTracker tracker : trackers) {
            tracker.addRuntimeAttributes();
            try {
                if (tracker.getAttributes() != null) {
                    for (Attribute attribute : tracker.getAttributes()) {
                        ncOut.addVariableAttribute(ncOut.findVariable(tracker.getName()), attribute);
                    }
                }
            } catch (Exception ex) {
                // do nothing, attribute wont show
            }
        }

        // add UI variables 
        latlonDim = ncOut.addDimension(null, "latlon", 2);
        // add edge of the simulated area
        addEdge();
        // add masked cells
        addMask();
        // add zone areas
        addZones();

        // add gloabal attributes
        addGlobalAttributes();

        // create the NetCDF file
        try {
            ncOut.create();
        } catch (SecurityException | IOException ex) {
            error("[output] Failed to create NetCDF output file " + basename, ex);
        }

        info("[output] Created NetCDF output file " + basename);
    }

    private void writeUIVariables() {

        // write the definition of the simulated area
        try {
            writeEdge();
        } catch (IOException | InvalidRangeException ex) {
            warning("[output] Error writing NetCDF \"edge\" variable. It may affect the mapping process.", ex);
        }

        // write masked cells
        try {
            writeMask();
        } catch (IOException | InvalidRangeException ex) {
            warning("[output] Error writing NetCDF \"mask\" variable. It may affect the mapping process.", ex);
        }

        // write zone areas
        for (int index = 0; index < zoneAreas.size(); index++) {
            try {
                writeZone(index);
            } catch (IOException | InvalidRangeException ex) {
                warning("[output] Error writing NetCDF \"zone " + index + "\" variable. It may affect the mapping process.", ex);
            }
        }
    }

    /**
     * @param e
     * @throws Exception
     */
    @Override
    public void setupPerformed(SetupEvent e) throws Exception {

        // Get record frequency
        record_frequency = Integer.valueOf(getParameter("record_frequency"));
        dt_record = record_frequency * Math.abs(getSimulationManager().getTimeManager().get_dt());

        // Reset NetCDF dimensions
        getDimensionFactory().resetDimensions();

        // setup application trackers lon lat depth time
        setupPredefinedTrackers();

        // setup custom trackers
        setupCustomTrackers(customTrackers);

        // setup user defined trackers
        setupCustomTrackers(getUserTrackers());

        clearPredefinedTrackerList = true;
        clearCustomTrackerList = true;

        info("[output] Setup [OK]");
    }

    @Override
    public void initializePerformed(InitializeEvent e) throws Exception {

        // add listeners
        getSimulationManager().getTimeManager().addNextStepListener(this);
        getSimulationManager().getTimeManager().addLastStepListener(this);

        /* reset counter */
        i_record = 0;

        info("[output] Initialization [OK]");
    }

    public class NCDimFactory {

        private Dimension time, drifter;
        private HashMap<String, Dimension> dimensions;

        public Dimension createDimension(Dimension dim) {
            if (dimensions.containsKey(dim.getFullName())) {
                if (dim.getLength() != dimensions.get(dim.getFullName()).getLength()) {
                    throw new IllegalArgumentException("Dimension (" + dim.getFullName() + ") has already been defined with a different length.");
                } else {
                    return dimensions.get(dim.getFullName());
                }
            } else {
                Dimension newDim = ncOut.addDimension(null, dim.getFullName(), dim.getLength());
                dimensions.put(newDim.getFullName(), newDim);
                return newDim;
            }
        }

        public Dimension getTimeDimension() {
            if (null == time) {
                time = ncOut.addUnlimitedDimension("time");
                dimensions.put(time.getFullName(), time);
            }
            return time;
        }

        public Dimension getDrifterDimension() {
            if (null == drifter) {
                drifter = ncOut.addDimension(null, "drifter", getSimulationManager().getReleaseManager().getNbParticles());
                dimensions.put(drifter.getName(), drifter);
            }
            return drifter;
        }

        public void resetDimensions() {
            time = null;
            drifter = null;
            dimensions = new HashMap();
        }
    }
}
