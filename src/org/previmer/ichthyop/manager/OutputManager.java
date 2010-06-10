/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import java.awt.geom.Point2D;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.LastStepEvent;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.arch.IOutputManager;
import org.previmer.ichthyop.io.XBlock;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import ucar.nc2.NetcdfFileWriteable;
import org.previmer.ichthyop.arch.ITimeManager;
import org.previmer.ichthyop.arch.ITracker;
import org.previmer.ichthyop.event.LastStepListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.arch.IDataset;
import org.previmer.ichthyop.io.DepthTracker;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.io.LatTracker;
import org.previmer.ichthyop.io.LonTracker;
import org.previmer.ichthyop.io.MortalityTracker;
import org.previmer.ichthyop.io.TimeTracker;
import org.previmer.ichthyop.io.UserDefinedTracker;
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
public class OutputManager extends AbstractManager implements IOutputManager, LastStepListener {

    final private static OutputManager outputManager = new OutputManager();
    private final static String block_key = "app.output";
    private int dt_record;
    private NCDimFactory dimensionFactory;
    private int i_record;
    private int record_frequency;
    private List<GeoPosition> region;
    private List<List<GeoPosition>> zoneEdges;
    private Dimension latlonDim;
    /**
     * Object for creating/writing netCDF files.
     */
    private static NetcdfFileWriteable ncOut;
    /**
     *
     */
    private List<ITracker> trackers;
    private List<Class> requestedTrackers;

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
        return getParameter("output_path") + getParameter("file_prefix") + "_" + getSimulationManager().getId() + ".nc";
    }

    public boolean isTrackerEnabled(String trackerKey) {
        return getSimulationManager().getParameterManager().isBlockEnabled(BlockType.TRACKER, trackerKey);
    }

    /**
     * Closes the NetCDF file.
     */
    private void close() {
        try {
            ncOut.close();
            System.out.println("close ncOut");
        } catch (IOException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NullPointerException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
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
        getSimulationManager().getZoneManager().init();
        zoneEdges = new ArrayList();
        for (TypeZone type : TypeZone.values()) {
            if (null != getSimulationManager().getZoneManager().getZones(type)) {
                for (Zone zone : getSimulationManager().getZoneManager().getZones(type)) {
                    zoneEdges.add(iZone, makeZoneEdge(zone));
                    Dimension zoneDim = ncOut.addDimension("zone" + iZone, zoneEdges.get(iZone).size());
                    ncOut.addVariable("zone" + iZone, DataType.FLOAT, new Dimension[]{zoneDim, latlonDim});
                    ncOut.addVariableAttribute("zone" + iZone, "long_name", zone.getKey());
                    ncOut.addVariableAttribute("zone" + iZone, "unit", "lat degree north lon degree east");
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
        for (List<GeoPosition> zoneEdge : zoneEdges) {
            ArrayFloat.D2 zoneGp = new ArrayFloat.D2(zoneEdge.size(), 2);
            int i = 0;
            for (GeoPosition gp : zoneEdge) {
                zoneGp.set(i, 0, (float) gp.getLatitude());
                zoneGp.set(i, 1, (float) gp.getLongitude());
                i++;
            }
            ncOut.write("zone" + iZone, zoneGp);
            iZone++;
        }
    }

    private void addGlobalAttributes() {

        /* Add transport dimension */
        String dim = getSimulationManager().getDataset().is3D()
                ? "3d"
                : "2d";
        ncOut.addGlobalAttribute("transport_dimension", dim);
        for (BlockType type : BlockType.values()) {
            for (XBlock block : getSimulationManager().getParameterManager().getBlocks(type)) {
                if (!block.getType().equals(BlockType.OPTION)) {
                    ncOut.addGlobalAttribute(block.getKey() + ".enabled", String.valueOf(block.isEnabled()));
                }
                if (block.isEnabled()) {
                    for (XParameter param : block.getXParameters()) {
                        if (!param.isHidden()) {
                            String key = block.getKey() + "." + param.getKey();
                            ncOut.addGlobalAttribute(key, param.getValue());
                        }
                    }
                }
            }
        }
    }

    private List<GeoPosition> makeZoneEdge(Zone zone) {
        List<GeoPosition> list = new ArrayList();
        int xmin = (int) Math.floor(zone.getXmin());
        int xmax = (int) Math.ceil(zone.getXmax());
        int ymin = (int) Math.floor(zone.getYmin());
        int ymax = (int) Math.ceil(zone.getYmax());
        IDataset dataset = getSimulationManager().getDataset();
        int refinement = 5;
        float incr = 1 / (float) refinement;
        int nx = (xmax - xmin + 1) * refinement;
        int ny = (ymax - ymin + 1) * refinement;
        boolean[][] bzone = new boolean[nx][ny];
        boolean[][] ezone = new boolean[nx][ny];
        for (float i = xmin; i < xmax; i += incr) {
            for (float j = ymin; j < ymax; j += incr) {
                int ii = (int) Math.round((i - xmin) * refinement);
                int jj = (int) Math.round((j - ymin) * refinement);
                bzone[ii][jj] = zone.isGridPointInZone(i, j);
            }
        }
        List<Point2D.Float> listPt = new ArrayList();
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                int im1 = Math.max(i - 1, 0);
                int ip1 = Math.min(i + 1, nx - 1);
                int jm1 = Math.max(j - 1, 0);
                int jp1 = Math.min(j + 1, ny - 1);
                ezone[i][j] = bzone[i][j] && !(bzone[im1][j] && bzone[ip1][j] && bzone[i][jm1] && bzone[i][jp1]);
                if (ezone[i][j]) {
                    listPt.add(new Point2D.Float(xmin + i * incr, ymin + j * incr));
                }
            }
        }

        Point2D.Float pt1 = listPt.get(0);
        double[] lonlat = dataset.xy2lonlat(pt1.x, pt1.y);
        GeoPosition gp = new GeoPosition(lonlat[0], lonlat[1]);
        list.add(gp);
        listPt.remove(pt1);
        while (!listPt.isEmpty()) {
            Point2D.Float closestToP1 = new Point2D.Float(Integer.MAX_VALUE, Integer.MAX_VALUE);
            double distMin = getDistance(pt1, closestToP1);
            for (Point2D.Float pt2 : listPt) {
                double dist = Math.sqrt(Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2));
                if (dist < distMin) {
                    closestToP1 = pt2;
                    distMin = dist;
                }
            }
            lonlat = dataset.xy2lonlat(closestToP1.x, closestToP1.y);
            gp = new GeoPosition(lonlat[0], lonlat[1]);
            list.add(gp);
            listPt.remove(closestToP1);
            pt1 = closestToP1;
        }
        return list;
    }

    private double getDistance(Point2D.Float pt1, Point2D.Float pt2) {
        return Math.sqrt(Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2));
    }

    private List<GeoPosition> makeRegion() {

        final List<GeoPosition> lregion = new ArrayList<GeoPosition>();
        IDataset dataset = getSimulationManager().getDataset();
        for (int i = 1; i < dataset.get_nx(); i++) {
            lregion.add(new GeoPosition(dataset.getLat(i, 0), dataset.getLon(i, 0)));
        }
        for (int j = 1; j < dataset.get_ny(); j++) {
            lregion.add(new GeoPosition(dataset.getLat(dataset.get_nx() - 1, j), dataset.getLon(dataset.get_nx() - 1, j)));
        }
        for (int i = dataset.get_nx() - 1; i > 0; i--) {
            lregion.add(new GeoPosition(dataset.getLat(i, dataset.get_ny() - 1), dataset.getLon(i, dataset.get_ny() - 1)));
        }
        for (int j = dataset.get_ny() - 1; j > 0; j--) {
            lregion.add(new GeoPosition(dataset.getLat(0, j), dataset.getLon(0, j)));
        }
        return lregion;
    }

    public void addTracker(Class trackerClass) {
        requestedTrackers.add(trackerClass);
    }

    private void addAppTrackers() {
        trackers = new ArrayList();
        trackers.add(new TimeTracker());
        trackers.add(new LonTracker());
        trackers.add(new LatTracker());
        trackers.add(new MortalityTracker());
        if (getSimulationManager().getDataset().is3D()) {
            trackers.add(new DepthTracker());
        }
        /* Add trackers requested by external actions */
        for (Class trackerClass : requestedTrackers) {
            try {
                ITracker tracker = (ITracker) trackerClass.newInstance();
                trackers.add(tracker);
            } catch (Exception ex) {
                Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        for (ITracker tracker : trackers) {
            addVar2NcOut(tracker);
        }
    }

    private void addUserTrackers() {
        for (XBlock xtracker : getSimulationManager().getParameterManager().getBlocks(BlockType.TRACKER)) {
            if (xtracker.isEnabled()) {
                try {
                    ITracker tracker = new UserDefinedTracker(xtracker.getKey());
                    trackers.add(tracker);
                    addVar2NcOut(tracker);
                } catch (Exception ex) {
                    Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }

    public void nextStepTriggered(NextStepEvent e) {

        ITimeManager timeManager = e.getSource();
        //Logger.getAnonymousLogger().info(step.getTime() + " " + step.get_tO());
        if (((timeManager.getTime() - timeManager.get_tO()) % dt_record) == 0) {
            writeToNetCDF(i_record++);
        }
    }

    private void writeToNetCDF(int i_record) {
        System.out.println("  --> record " + i_record + " - time " + getSimulationManager().getTimeManager().timeToString());
        //Logger.getAnonymousLogger().info("  --> record " + i_record + " - time " + getSimulationManager().getTimeManager().timeToString());
        for (ITracker tracker : trackers) {
            tracker.track();
            writeTrackerToNetCDF(tracker, i_record);
        }
    }

    /**
     * Writes data to the specified variable.
     *
     * @param field a Field, the variable to be written
     * @param origin an int[], the offset within the variable to start writing.
     * @param array the Array that will be written; must be same type and
     * rank as Field
     */
    private void writeTrackerToNetCDF(ITracker tracker, int index) {
        try {

            ncOut.write(tracker.short_name(), tracker.origin(index), tracker.getArray());
        } catch (IOException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Adds the specified variable to the NetCDF file.
     *
     * @param field a Field, the variable to be added in the file.
     */
    private void addVar2NcOut(ITracker tracker) {

        ncOut.addVariable(tracker.short_name(), tracker.type(), tracker.dimensions());
        ncOut.addVariableAttribute(tracker.short_name(), "long_name",
                tracker.long_name());
        ncOut.addVariableAttribute(tracker.short_name(), "unit", tracker.unit());
        if (tracker.attributes() != null) {
            for (Attribute attribute : tracker.attributes()) {
                ncOut.addVariableAttribute(tracker.short_name(), attribute);
            }
        }
    }

    public void lastStepOccurred(LastStepEvent e) {
        writeToNetCDF(i_record);
        close();
    }

    public void setupPerformed(SetupEvent e) throws Exception {

        ncOut = NetcdfFileWriteable.createNew("");
        record_frequency = Integer.valueOf(getParameter("record_frequency"));
        ncOut.setLocation(getFileLocation());
        getDimensionFactory().resetDimensions();
        requestedTrackers = new ArrayList();
    }

    public void initializePerformed(InitializeEvent e) {
        getSimulationManager().getTimeManager().addNextStepListener(this);
        getSimulationManager().getTimeManager().addLastStepListener(this);
        try {
            i_record = 0;
            dt_record = record_frequency * getSimulationManager().getTimeManager().get_dt();
            addAppTrackers();
            addUserTrackers();
            addGlobalAttributes();
            addRegion();
            addZones();
            IOTools.makeDirectories(ncOut.getLocation());
            ncOut.create();
            writeRegion();
            writeZones();
        } catch (IOException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(OutputManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        Logger.getAnonymousLogger().info("Created output file : " + ncOut.getLocation());
    }

    public class NCDimFactory {

        private Dimension time, drifter;
        private Hashtable<TypeZone, Dimension> zoneDimension;
        private Hashtable<String, Dimension> dimensions;

        private void fillHashtable() {
            dimensions.put(time.getName(), time);
            dimensions.put(drifter.getName(), drifter);
            for (Dimension dim : zoneDimension.values()) {
                dimensions.put(dim.getName(), dim);
            }
        }

        public Dimension addDimension(Dimension dim) {
            if (dimensions.isEmpty()) {
                fillHashtable();
            }
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
            }
            return time;
        }

        public Dimension getDrifterDimension() {
            if (null == drifter) {
                drifter = ncOut.addDimension("drifter", getSimulationManager().getReleaseManager().getNbParticles());
            }
            return drifter;
        }

        public Dimension getZoneDimension(TypeZone type) {
            if (null == zoneDimension) {
                zoneDimension = new Hashtable();
            }
            if (null == zoneDimension.get(type)) {
                String name = type.toString() + "_zone";
                zoneDimension.put(type, ncOut.addDimension(name, getSimulationManager().getZoneManager().getZones(type).size()));
            }
            return zoneDimension.get(type);
        }

        public void resetDimensions() {
            time = null;
            drifter = null;
            zoneDimension = null;
        }
    }
}
