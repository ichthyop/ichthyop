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
import ucar.nc2.Variable;
import ucar.nc2.write.Nc4Chunking;
import ucar.nc2.write.Nc4ChunkingStrategy;
import ucar.nc2.write.NetcdfFileFormat;
import ucar.nc2.write.NetcdfFormatWriter;

import org.previmer.ichthyop.event.LastStepListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.dataset.FvcomDataset;
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
import org.previmer.ichthyop.io.DensityTracker;
import org.previmer.ichthyop.io.XParameter;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayInt;
import ucar.ma2.DataType;
import ucar.ma2.Index;
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
    private boolean isDensityEnabled = false;
    private boolean isTrajectoryEnabled = true;

    // barrier.n: adding control over the version format
    NetcdfFileFormat ncVersion;

    DensityTracker densTracker;

    /**
     * Object for creating/writing netCDF files.
     */
    private static NetcdfFormatWriter ncOut;
    private static NetcdfFormatWriter.Builder bNcOut;

    /** Object for creating/writting netCDF density files */
    private static NetcdfFormatWriter densNcOut;
    private static NetcdfFormatWriter.Builder bDensNcOut;

    private String trajectoryFileName;
    private String densityFileName;

    private Nc4Chunking chunker;

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

    public boolean isNull(String key) {
        return getSimulationManager().getParameterManager().isNull(block_key, key);
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
     * Closes the Trajectory output NetCDF file.
     */
    private void closeTraj() {
        try {
            ncOut.close();
            String strFileBase = this.trajectoryFileName.substring(0, this.trajectoryFileName.indexOf(".part"));
            Path filePart = new File(this.trajectoryFileName).toPath();
            Path fileBase = new File(strFileBase).toPath();
            Files.move(filePart, fileBase, REPLACE_EXISTING);
            getLogger().info("Closed NetCDF output file.");
            ncOut = null;
            bNcOut = null;
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Problem closing the NetCDF output file ==> {0}", ex.toString());
        }
    }

    /**
     * Closes the Distribution NetCDF file.
     */
    private void closeDist() {
        try {
            densNcOut.close();
            String strFileBase = this.densityFileName.substring(0, this.densityFileName.indexOf(".part"));
            Path filePart = new File(this.densityFileName).toPath();
            Path fileBase = new File(strFileBase).toPath();
            Files.move(filePart, fileBase, REPLACE_EXISTING);
            getLogger().info("Closed NetCDF output file.");
            densNcOut = null;
            bDensNcOut = null;
        } catch (Exception ex) {
            getLogger().log(Level.WARNING, "Problem closing the NetCDF output file ==> {0}", ex.toString());
        }
    }

    private void addRegion() {

        /* Add the region edges */
        region = makeRegion();

        // If no bounding box, nothing is done.
        if(region.size() == 0) {
            return;
        }

        Dimension edge = bNcOut.addDimension("edge", region.size());
        latlonDim = bNcOut.addDimension("latlon", 2);
        Variable.Builder<?> variable = bNcOut.addVariable("region_edge", DataType.FLOAT, new ArrayList<Dimension>(Arrays.asList(edge, latlonDim)));
        variable.addAttribute(new Attribute("long_name", "geoposition of region edge"));
        variable.addAttribute(new Attribute("unit", "lat degree north lon degree east"));
    }

    private void writeRegion() throws IOException, InvalidRangeException {

        // If no bounding box, nothing is done.
        if (region.size() == 0) {
            return;
        }

        ArrayFloat.D2 edge = new ArrayFloat.D2(region.size(), 2);
        int i = 0;
        for (GeoPosition gp : region) {
            edge.set(i, 0, (float) gp.getLatitude());
            edge.set(i, 1, (float) gp.getLongitude());
            i++;
        }
        ncOut.write(ncOut.findVariable("region_edge"), edge);
    }

    private void addDrifters() {
        Variable.Builder<?> variable = bNcOut.addVariable("drifter", DataType.INT, "drifter");
        variable.addAttribute(new Attribute("long_name", "drifter index"));
        variable.addAttribute(new Attribute("unit", ""));
    }

    /**
     * Add drifter coordinates in the Netcdf. Used mainly for visualisation purposes
     */
    private void writeDrifters() throws IOException, InvalidRangeException {
        int nDrifters = getSimulationManager().getReleaseManager().getNbParticles();
        ArrayInt drifterIndex = new ArrayInt(new int[] {nDrifters}, false);
        Index index = drifterIndex.getIndex();
        for (int i = 0; i < nDrifters; i++) {
            index.set(i);
            drifterIndex.set(index, i);
        }
        ncOut.write(ncOut.findVariable("drifter"), drifterIndex);
    }

    private void addZones() {

        int iZone = 0;
        zoneAreas = new ArrayList<>();
        for (TypeZone type : TypeZone.values()) {
            if (null != getSimulationManager().getZoneManager().getZones(type)) {
                for (Zone zone : getSimulationManager().getZoneManager().getZones(type)) {
                    zoneAreas.add(iZone, makeZoneArea(zone));
                    Dimension zoneDim = bNcOut.addDimension("zone" + iZone, zoneAreas.get(iZone).size());
                    Variable.Builder<?> varZone = bNcOut.addVariable("coord_zone" + iZone, DataType.FLOAT, new ArrayList<Dimension>(Arrays.asList(zoneDim, latlonDim)));
                    varZone.addAttribute(new Attribute("long_name", zone.getKey()));
                    varZone.addAttribute(new Attribute("unit", "x and y coordinates of the center of the cells in the zone"));
                    varZone.addAttribute(new Attribute("type", zone.getType().toString()));
                    String color = zone.getColor().toString();
                    color = color.substring(color.lastIndexOf("["));
                    varZone.addAttribute(new Attribute("color", color));

                    Dimension geoDim = bNcOut.addDimension("geozone" + iZone, zone.getLat().size());
                    bNcOut.addVariable("coord_geo_zone" + iZone, DataType.FLOAT,
                            new ArrayList<Dimension>(Arrays.asList(geoDim, latlonDim)));

                    iZone++;

                }
            }
        }
        bNcOut.addAttribute(new Attribute("nb_zones", iZone));
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
        bNcOut.addAttribute(new Attribute("transport_dimension", dim));

        /* Write all parameters */
        for (BlockType type : BlockType.values()) {
            for (XBlock block : getSimulationManager().getParameterManager().getBlocks(type)) {
                if (!block.getType().equals(BlockType.OPTION)) {
                    bNcOut.addAttribute(new Attribute(block.getKey() + ".enabled", String.valueOf(block.isEnabled())));
                }
                if (block.isEnabled()) {
                    for (XParameter param : block.getXParameters()) {
                        if (!param.isHidden()) {
                            String key = block.getKey() + "." + param.getKey();
                            bNcOut.addAttribute(new Attribute(key, param.getValue()));
                        }
                    }
                }
            }
        }

        /* Add the corresponding xml file */
        bNcOut.addAttribute(new Attribute("xml_file", getSimulationManager().getConfigurationFile().getAbsolutePath()));
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
                    String varname = token.trim();
                    if(this.customTrackers.contains(varname)) {
                        // If the variable is already included in customTracker, nothing is done
                        continue;
                    }
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

            if (this.isTrajectoryEnabled) {
                writeToNetCDF(i_record);
            }

            if (this.isDensityEnabled) {
                writeDensToNetCDF(i_record);
            }

            i_record++;

        }
    }

    private void writeDensToNetCDF(int i_record) {
        getLogger().log(Level.INFO, "Saving variables...");
        int[] origin = new int[] {i_record, 0, 0};
        try {
            densNcOut.write(densNcOut.findVariable("density"), origin, this.densTracker.getDensity());
        } catch (IOException | InvalidRangeException e) {
            getLogger().log(Level.SEVERE, "Error saving density value");
            e.printStackTrace();
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

        if (this.isTrajectoryEnabled) {
            if (!e.isInterrupted()) {
                writeToNetCDF(i_record);
            }
            this.closeTraj();
        }

        if (this.isDensityEnabled) {
            if (!e.isInterrupted()) {
                writeDensToNetCDF(i_record);
            }
            this.closeDist();
        }

        if (null != predefinedTrackers) {
            predefinedTrackers.clear();
        }
        if (null != customTrackers) {
            customTrackers.clear();
        }
    }

    /** Setup trajectory outputs */
    private void setupTrajectoryOutput() throws Exception {

        if(bNcOut != null) {
            // if ncOut is not null, i.e. if file is defined,
            // and if not in defined mode, nothing is done here
            return;
        }

        this.trajectoryFileName = makeFileLocation();
        bNcOut = NetcdfFormatWriter.createNewNetcdf4(ncVersion, this.trajectoryFileName, this.chunker);

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
                Variable.Builder<?> variable = bNcOut.addVariable(tracker.getName(), tracker.getDataType(), tracker.getDimensions());
                tracker.addRuntimeAttributes();
                if (tracker.getAttributes() != null) {
                    for (Attribute attribute : tracker.getAttributes()) {
                        variable.addAttribute(attribute);
                    }
                }

            } catch (Exception ex) {
                errTrackers.add(tracker);
                getLogger().log(Level.WARNING, "Error adding tracker " + tracker.getName() + " in NetCDF output file. The variable will not be recorded.", ex);
            }
        }

        trackers.removeAll(errTrackers);

        // add gloabal attributes
        addGlobalAttributes();

        // add definition of the simulated area
        IDataset dataset = getSimulationManager().getDataset();

        // if no fvcom output, write zone in file
        if(!(dataset instanceof FvcomDataset)) {
            addRegion();
        }

        // add drifter variables
        addDrifters();

        clearPredefinedTrackerList = true;
        clearCustomTrackerList = true;
    }

    /** Setup density outputs */
    private void setupDensityOutput() throws IOException {

        // If the file is defined and not in define mode, nothing is done.
        if ((bDensNcOut != null)) {
            return;
        }

        // Initialize the density tracker
        this.densTracker = new DensityTracker();
        this.densTracker.init();

        Dimension dimTime;
        Dimension dimLongitude;
        Dimension dimLatitude;
        this.densityFileName = makeFileLocation().replace(".nc", "_density.nc");

        bDensNcOut = NetcdfFormatWriter.createNewNetcdf4(ncVersion, this.densityFileName, this.chunker);

        dimTime = bDensNcOut.addUnlimitedDimension("time");
        dimLongitude = bDensNcOut.addDimension("longitude", densTracker.getNLon());
        dimLatitude = bDensNcOut.addDimension("latitude", densTracker.getNLat());

        List<Dimension> dimsDens = new ArrayList<>(Arrays.asList(dimTime, dimLatitude, dimLongitude));

        bDensNcOut.addVariable("longitude", ucar.ma2.DataType.FLOAT, "longitude");
        bDensNcOut.addVariable("latitude", ucar.ma2.DataType.FLOAT, "latitude");
        bDensNcOut.addVariable("density", ucar.ma2.DataType.INT, dimsDens);

    }


    @Override
    public void setupPerformed(SetupEvent e) throws Exception {

        String key = "netcdf_output_format";
        if (this.isNull(key)) {
            ncVersion = NetcdfFileFormat.NETCDF3;
        } else {
            String ncOutputFormat = getParameter(key);
            switch (ncOutputFormat) {
                case "ncstream":
                    ncVersion = NetcdfFileFormat.NCSTREAM;
                    break;
                case "netcdf3":
                    ncVersion = NetcdfFileFormat.NETCDF3;
                    break;
                case "netcdf3_64bit_data":
                    ncVersion = NetcdfFileFormat.NETCDF3_64BIT_DATA;
                    break;
                case "netcdf3_64bit_offset":
                    ncVersion = NetcdfFileFormat.NETCDF3_64BIT_OFFSET;
                    break;
                case "netcdf4":
                    ncVersion = NetcdfFileFormat.NETCDF4;
                    break;
                case "netcdf4_classic":
                    ncVersion = NetcdfFileFormat.NETCDF4_CLASSIC;
                    break;
                default:
                    ncVersion = NetcdfFileFormat.NETCDF3;
                    break;
            }
        }

        // adding options for NetCDF compression
        if(ncVersion == NetcdfFileFormat.NETCDF4) {

            int deflateLevel = 0;
            boolean shuffle = false;

            // if netcdf4 output, check if deflate level is set.
            key = "netcdf_output_compression";
            if (!this.isNull(key)) {
                deflateLevel = Integer.valueOf(getParameter(key));
            }

            // if deflate > 0, compression is on.
            if(deflateLevel > 0) {
                key = "netcdf_output_shuffle";
                // we read whether shuffle parameter is on.
                if(!this.isNull(key)) {
                    shuffle = Boolean.valueOf(getParameter(key));
                }

                Nc4Chunking.Strategy strategy = Nc4Chunking.Strategy.none;
                key = "netcdf_output_compression_stragegy";
                if (!this.isNull(key)) {
                    switch (getParameter(key)) {
                        case "standard":
                            strategy = Nc4Chunking.Strategy.standard;
                            break;
                        case "grib":
                            strategy = Nc4Chunking.Strategy.grib;
                            break;
                        case "none":
                            strategy = Nc4Chunking.Strategy.none;
                            break;
                        default:
                            strategy = Nc4Chunking.Strategy.none;
                    }
                }

                this.chunker = Nc4ChunkingStrategy.factory(strategy, deflateLevel, shuffle);
            }
        }

        /* Get record frequency */
        record_frequency = Integer.valueOf(getParameter("record_frequency"));
        dt_record = record_frequency * Math.abs(getSimulationManager().getTimeManager().get_dt());

        key = "output.trajectory.enabled";
        if(!this.isNull(key)) {
            this.isTrajectoryEnabled = Boolean.valueOf(getParameter(key));
        }

        key = "output.density.enabled";
        if(!this.isNull(key)) {
            this.isDensityEnabled = Boolean.valueOf(getParameter(key));
        }

        if(this.isDensityEnabled) {
            this.setupDensityOutput();
        }

        if(this.isTrajectoryEnabled) {
            this.setupTrajectoryOutput();
        }

        getLogger().info("Output manager setup [OK]");
    }


    /** Initialize trajectory outputs. */
    private void initializeTrajectoryOutputs() throws Exception {

        if (ncOut != null) {
            // If the file is not in defined mode, assumes that everything has been already
            // created
            return;
        }

        // add the zones It cannnot be done in the setup because the definition of the
        // zones requires that dataset has been initialized first.
        // if FvcomDataset, no writting of Zones in Netcdf
        IDataset dataset = getSimulationManager().getDataset();
        if (!(dataset instanceof FvcomDataset)) {
            addZones();
        }

        /* reset counter */
        i_record = 0;

        /* create the NetCDF file */
        try {
            ncOut = bNcOut.build();
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

        try {
            writeDrifters();
        } catch(Exception ex) {

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
        getLogger().log(Level.INFO, "Created output file {0}", ncOut.getOutputFile().getLocation());

    }

    /** Initialize trajectory outputs. */
    private void initializeDensityOutputs() throws Exception {

        // write the lon/lat coordinates
        if (densNcOut != null) {
            // If the file is not in defined mode, assumes that everything has been already
            // created
            return;
        }

        densNcOut = bDensNcOut.build();

        densNcOut.write(densNcOut.findVariable("longitude"), densTracker.getLonCells());
        densNcOut.write(densNcOut.findVariable("latitude"), densTracker.getLatCells());

    }

    @Override
    public void initializePerformed(InitializeEvent e) throws Exception {

        getLogger().info("Output manager initialization [OK]");
        if(this.isTrajectoryEnabled) {
            this.initializeTrajectoryOutputs();
        }

        if(this.isDensityEnabled) {
            this.initializeDensityOutputs();
        }

        /* add listeners */
        getSimulationManager().getTimeManager().addNextStepListener(this);
        getSimulationManager().getTimeManager().addLastStepListener(this);
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
                Dimension newDim = bNcOut.addDimension(dim.getShortName(), dim.getLength());
                dimensions.put(newDim.getShortName(), newDim);
                return newDim;
            }
        }

        public Dimension getTimeDimension() {
            if (null == time) {
                time = bNcOut.addUnlimitedDimension("time");
                dimensions.put(time.getShortName(), time);
            }
            return time;
        }

        public Dimension getDrifterDimension() {
            if (null == drifter) {
                drifter = bNcOut.addDimension("drifter", getSimulationManager().getReleaseManager().getNbParticles());
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
                Dimension zoneDim = bNcOut.addDimension(name, getSimulationManager().getZoneManager().getZones(type).size());
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
