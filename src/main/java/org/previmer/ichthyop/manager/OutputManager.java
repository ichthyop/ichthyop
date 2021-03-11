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

package org.previmer.ichthyop.manager;

import java.awt.geom.Point2D;
import java.io.File;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.LastStepEvent;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.XBlock;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import java.util.logging.Level;
import ucar.nc2.NetcdfFileWriter;
import ucar.nc2.Variable;

import org.previmer.ichthyop.event.LastStepListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.dataset.IDataset;
import org.previmer.ichthyop.event.NextStepListener;
import org.previmer.ichthyop.io.AbstractTracker;
import org.previmer.ichthyop.io.DepthTracker;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.io.LatTracker;
import org.previmer.ichthyop.io.LonTracker;
import org.previmer.ichthyop.io.MortalityTracker;
import org.previmer.ichthyop.io.TimeTracker;
import org.previmer.ichthyop.io.CustomTracker;
import org.previmer.ichthyop.io.XParameter;
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
    private static NetcdfFileWriter ncOut;
    /**
     *
     */
    private List<AbstractTracker> trackers;
    private List<Class<?>> predefinedTrackers;
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
        return getSimulationManager().getParameterManager().getParameter(block_key, key);
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
            String strFilePart = ncOut.getNetcdfFile().getLocation();
            String strFileBase = strFilePart.substring(0, strFilePart.indexOf(".part"));
            Path filePart = new File(strFilePart).toPath();
            Path fileBase = new File(strFileBase).toPath();
            Files.move(filePart, fileBase, REPLACE_EXISTING);
            getLogger().info("Closed NetCDF output file.");
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Problem closing the NetCDF output file ==> {0}", ex.toString());
        }
    }

    private void addRegion() {

        /* Add the region edges */
        region = makeRegion();
        Dimension edge = ncOut.addDimension(null, "edge", region.size());
        latlonDim = ncOut.addDimension(null, "latlon", 2);
        ncOut.addVariable(null, "region_edge", DataType.FLOAT, new ArrayList<Dimension>(Arrays.asList(edge, latlonDim)));
        ncOut.findVariable("region_edge").addAttribute(new Attribute("long_name", "geoposition of region edge"));
        ncOut.findVariable("region_edge").addAttribute(new Attribute("unit", "lat degree north lon degree east"));
    }

    private void writeRegion() throws IOException, InvalidRangeException {

        ArrayFloat.D2 edge = new ArrayFloat.D2(region.size(), 2);
        int i = 0;
        for (GeoPosition gp : region) {
            edge.set(i, 0, (float) gp.getLatitude());
            edge.set(i, 1, (float) gp.getLongitude());
            i++;
        }
        ncOut.write(ncOut.findVariable("region_edge"), edge);
    }

    private void addZones() {

        int iZone = 0;
        zoneAreas = new ArrayList<>();
        for (TypeZone type : TypeZone.values()) {
            if (null != getSimulationManager().getZoneManager().getZones(type)) {
                for (Zone zone : getSimulationManager().getZoneManager().getZones(type)) {
                    zoneAreas.add(iZone, makeZoneArea(zone));
                    Dimension zoneDim = ncOut.addDimension(null, "zone" + iZone, zoneAreas.get(iZone).size());
                    Variable varZone = ncOut.addVariable(null, "coord_zone" + iZone, DataType.FLOAT, new ArrayList<Dimension>(Arrays.asList(zoneDim, latlonDim)));
                    varZone.addAttribute(new Attribute("long_name", zone.getKey()));
                    varZone.addAttribute(new Attribute("unit", "x and y coordinates of the center of the cells in the zone"));
                    varZone.addAttribute(new Attribute("type", zone.getType().toString()));
                    String color = zone.getColor().toString();
                    color = color.substring(color.lastIndexOf("["));
                    varZone.addAttribute(new Attribute("color", color));
                    
                    Dimension geoDim = ncOut.addDimension(null, "geozone" + iZone, zone.getLat().size());
                    Variable lonlatZone = ncOut.addVariable(null, "coord_geo_zone" + iZone, DataType.FLOAT, new ArrayList<Dimension>(Arrays.asList(geoDim, latlonDim)));
                    
                    iZone++;
                    
                    
                }
            }
        }        
        ncOut.addGroupAttribute(null, new Attribute("nb_zones", iZone));
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
            ncOut.write(ncOut.findVariable("coord_zone" + iZone), arrZoneArea);
            iZone++;
        }
        
        iZone = 0 ;
        for (TypeZone type : TypeZone.values()) {
            if (null != getSimulationManager().getZoneManager().getZones(type)) {
                for (Zone zone : getSimulationManager().getZoneManager().getZones(type)) {
                    int nPoints = zone.getLon().size();
                    ArrayFloat.D2 arrZoneArea = new ArrayFloat.D2(nPoints, 2);
                    for(int k = 0; k < nPoints; k++) { 
                        arrZoneArea.set(k, 0,  ((float) zone.getLat().get(k)));
                        arrZoneArea.set(k, 1, (float) (zone.getLon().get(k)));
                    }      
                    ncOut.write(ncOut.findVariable("coord_geo_zone" + iZone), arrZoneArea);
                    iZone++;
                }
            }
        }    
    }

    private void addGlobalAttributes() {

        /* Add transport dimension */
        String dim = getSimulationManager().getDataset().is3D()
                ? "3d"
                : "2d";
        ncOut.addGroupAttribute(null, new Attribute("transport_dimension", dim));

        /* Write all parameters */
        for (BlockType type : BlockType.values()) {
            for (XBlock block : getSimulationManager().getParameterManager().getBlocks(type)) {
                if (!block.getType().equals(BlockType.OPTION)) {
                    ncOut.addGroupAttribute(null, new Attribute(block.getKey() + ".enabled", String.valueOf(block.isEnabled())));
                }
                if (block.isEnabled()) {
                    for (XParameter param : block.getXParameters()) {
                        if (!param.isHidden()) {
                            String key = block.getKey() + "." + param.getKey();
                            ncOut.addGroupAttribute(null, new Attribute(key, param.getValue()));
                        }
                    }
                }
            }
        }

        /* Add the corresponding xml file */
        ncOut.addGroupAttribute(null, new Attribute("xml_file", getSimulationManager().getConfigurationFile().getAbsolutePath()));
    }

    private List<Point2D> makeZoneArea(Zone zone) {
        List<Point2D> list = new ArrayList<>();

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
        
        // for tiny zones include at least one point, the barycenter of the zone
        // not satisfactory solution, this fix should be improved
        if (list.isEmpty()) {
            Point2D xy = new Point2D.Float(
                    (float) (0.5 * (zone.getXmin() + zone.getXmax())),
                    (float) (0.5 * (zone.getYmin() + zone.getYmax())));
            list.add(xy);
        }

        return list;
    }

    /*
    private double getDistance(Point2D.Float pt1, Point2D.Float pt2) {
        return Math.sqrt(Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2));
    }
    */

    private List<GeoPosition> makeRegion() {

        final List<GeoPosition> lregion = new ArrayList<>();
        IDataset dataset = getSimulationManager().getDataset();
        for (int i = 1; i < dataset.get_nx(); i++) {
            if (!Double.isNaN(dataset.getLat(i, 0)) && !Double.isNaN(dataset.getLon(i, 0))) {
                lregion.add(new GeoPosition(dataset.getLat(i, 0), dataset.getLon(i, 0)));
            }
        }
        for (int j = 1; j < dataset.get_ny(); j++) {
            if (!Double.isNaN(dataset.getLat(dataset.get_nx() - 1, j)) && !Double.isNaN(dataset.getLon(dataset.get_nx() - 1, j))) {
                lregion.add(new GeoPosition(dataset.getLat(dataset.get_nx() - 1, j), dataset.getLon(dataset.get_nx() - 1, j)));
            }
        }
        for (int i = dataset.get_nx() - 1; i > 0; i--) {
            if (!Double.isNaN(dataset.getLat(i, dataset.get_ny() - 1)) && !Double.isNaN(dataset.getLon(i, dataset.get_ny() - 1))) {
                lregion.add(new GeoPosition(dataset.getLat(i, dataset.get_ny() - 1), dataset.getLon(i, dataset.get_ny() - 1)));
            }
        }
        for (int j = dataset.get_ny() - 1; j > 0; j--) {
            if (!Double.isNaN(dataset.getLat(0, j)) && !Double.isNaN(dataset.getLon(0, j))) {
                lregion.add(new GeoPosition(dataset.getLat(0, j), dataset.getLon(0, j)));
            }
        }
        return lregion;
    }

    public void addPredefinedTracker(Class<?> trackerClass) {
        if (null == predefinedTrackers) {
            predefinedTrackers = new ArrayList<>();
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
            customTrackers = new ArrayList<>();
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
        trackers = new ArrayList<>();
        trackers.add(new TimeTracker());
        trackers.add(new LonTracker());
        trackers.add(new LatTracker());
        trackers.add(new MortalityTracker());
        if (getSimulationManager().getDataset().is3D()) {
            trackers.add(new DepthTracker());
        }
        /* Add trackers requested by external actions */
        if (null != predefinedTrackers) {
            for (Class<?> trackerClass : predefinedTrackers) {
                try {
                    AbstractTracker tracker = (AbstractTracker) trackerClass.getDeclaredConstructor().newInstance();
                    trackers.add(tracker);
                } catch (Exception ex) {
                    getLogger().log(Level.SEVERE, "Error adding tracker " + trackerClass.getSimpleName() + " in NetCDF output file. The variable will not be recorded.", ex);
                }
            }
        }
    }

    private List<String> getUserTrackers() throws Exception {
        String userTrackers = getParameter("trackers");
        try {
            String[] tokens = userTrackers.split("\"");
            List<String> variables = new ArrayList<>();
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
        if (((long)(timeManager.getTime() - timeManager.get_tO()) % dt_record) == 0) {
            writeToNetCDF(i_record++);
        }
    }

    private void writeToNetCDF(int i_record) {
        getLogger().log(Level.INFO, "Saving variables...");
        List<AbstractTracker> errTrackers = new ArrayList<>();
        for (AbstractTracker tracker : trackers) {
            if (tracker.isEnabled()) {
                /* Retrieve the values of the variable */
                try {
                    tracker.track();
                } catch (Exception ex) {
                    errTrackers.add(tracker);
                    getSimulationManager().getDataset().removeRequiredVariable(tracker.getName(), tracker.getClass());
                    getLogger().log(Level.WARNING, "Error tracking variable " + tracker.getName() + ". The variable will no longer be recorded in the NetCDF output file.", ex);
                    continue;
                }
                /* Write the current time step in the NetCDF file */
                try {
                    ncOut.write(ncOut.findVariable(tracker.getName()), tracker.origin(i_record), tracker.getArray());
                } catch (Exception ex) {
                    errTrackers.add(tracker);
                    getSimulationManager().getDataset().removeRequiredVariable(tracker.getName(), tracker.getClass());
                    getLogger().log(Level.WARNING, "Error writing variable " + tracker.getName() + ". The variable will no longer be recorded in the NetCDF output file.", ex);
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
        
        if((ncOut != null) && (!ncOut.isDefineMode())) {
            // if ncOut is not null, i.e. if file is defined,
            // and if not in defined mode, nothing is done here
            return;
        }
        /* Create the NetCDF writeable object */
        ncOut = NetcdfFileWriter.createNew(NetcdfFileWriter.Version.netcdf4, makeFileLocation());

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
        List<AbstractTracker> errTrackers = new ArrayList<>();
        for (AbstractTracker tracker : trackers) {
            try {
                tracker.init();
                ncOut.addVariable(null, tracker.getName(), tracker.getDataType(), tracker.getDimensions());
            } catch (Exception ex) {
                errTrackers.add(tracker);
                getLogger().log(Level.WARNING, "Error adding tracker " + tracker.getName() + " in NetCDF output file. The variable will not be recorded.", ex);
            }
        }
        trackers.removeAll(errTrackers);

        /* add gloabal attributes */
        addGlobalAttributes();

        /* add definition of the simulated area */
        addRegion();

        clearPredefinedTrackerList = true;
        clearCustomTrackerList = true;

        getLogger().info("Output manager setup [OK]");
    }

    @Override
    public void initializePerformed(InitializeEvent e) throws Exception {

        if (!ncOut.isDefineMode()) {
            /* add listeners */
            getSimulationManager().getTimeManager().addNextStepListener(this);
            getSimulationManager().getTimeManager().addLastStepListener(this);
            getLogger().info("Output manager initialization [OK]");
            // If the file is not in defined mode, assumes that everything has been already
            // created
            return;
        }
        
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
                        ncOut.findVariable(tracker.getName()).addAttribute(attribute);
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
            getLogger().log(Level.WARNING, sb.toString());
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
            getLogger().log(Level.WARNING, sb.toString());
        }

        /* initialization completed */
        getLogger().log(Level.INFO, "Created output file {0}", ncOut.getNetcdfFile().getLocation());
        getLogger().info("Output manager initialization [OK]");
    }

    public class NCDimFactory {

        private Dimension time, drifter;
        private HashMap<TypeZone, Dimension> zoneDimension;
        private HashMap<String, Dimension> dimensions;

        public Dimension createDimension(Dimension dim) {
            if (dimensions.containsKey(dim.getShortName())) {
                if (dim.getLength() != dimensions.get(dim.getShortName()).getLength()) {
                    throw new IllegalArgumentException("Dimension (" + dim.getShortName() + ") has already been defined with a different length.");
                } else {
                    return dimensions.get(dim.getShortName());
                }
            } else {
                Dimension newDim = ncOut.addDimension(null, dim.getShortName(), dim.getLength());
                dimensions.put(newDim.getShortName(), newDim);
                return newDim;
            }
        }

        public Dimension getTimeDimension() {
            if (null == time) {
                time = ncOut.addUnlimitedDimension("time");
                dimensions.put(time.getShortName(), time);
            }
            return time;
        }

        public Dimension getDrifterDimension() {
            if (null == drifter) {
                drifter = ncOut.addDimension(null, "drifter", getSimulationManager().getReleaseManager().getNbParticles());
                dimensions.put(drifter.getShortName(), drifter);
            }
            return drifter;
        }

        public Dimension getZoneDimension(TypeZone type) {
            if (null == zoneDimension) {
                zoneDimension = new HashMap<>();
            }
            if (null == zoneDimension.get(type)) {
                String name = type.toString() + "_zone";
                Dimension zoneDim = ncOut.addDimension(null, name, getSimulationManager().getZoneManager().getZones(type).size());
                zoneDimension.put(type, zoneDim);
                dimensions.put(zoneDim.getShortName(), zoneDim);
            }
            return zoneDimension.get(type);
        }

        public void resetDimensions() {
            time = null;
            drifter = null;
            zoneDimension = null;
            dimensions = new HashMap<>();
        }
    }
}
