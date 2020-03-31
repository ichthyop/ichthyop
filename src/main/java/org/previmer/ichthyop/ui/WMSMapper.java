/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import de.micromata.opengis.kml.v_2_2_0.Document;
import de.micromata.opengis.kml.v_2_2_0.Folder;
import de.micromata.opengis.kml.v_2_2_0.IconStyle;
import de.micromata.opengis.kml.v_2_2_0.Kml;
import de.micromata.opengis.kml.v_2_2_0.KmlFactory;
import de.micromata.opengis.kml.v_2_2_0.Placemark;
import de.micromata.opengis.kml.v_2_2_0.Style;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.Tile;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.mapviewer.empty.EmptyTileFactory;
import org.jdesktop.swingx.mapviewer.wms.WMSService;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;
import org.previmer.ichthyop.calendar.Day360Calendar;
import org.previmer.ichthyop.dataset.IDataset;
import org.previmer.ichthyop.calendar.InterannualCalendar;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.manager.SimulationManager;
import org.previmer.ichthyop.manager.TimeManager;
import ucar.ma2.Array;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D0;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayFloat.D2;
import ucar.ma2.ArrayInt;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.Attribute;
import ucar.nc2.Dimension;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class WMSMapper extends JXMapKit {

    private List<GeoPosition> region;
    private HashMap<String, WMSMapper.DrawableZone> zones;
    private static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    private NetcdfFile nc;
    private Variable vlon, vlat, pcolorVariable, vtime, vmortality;
    boolean canRepaint = false;
    private Painter bgPainter;
    boolean loadFromHeap = false;
    private double defaultLat = 48.38, defaultLon = -4.62;
    private int defaultZoom = 10;
    private Calendar calendar;
    private Kml kml;
    private Document kmlDocument;
    private Folder kmlMainFolder;
    private double[] time;
    private int nbSteps;
    private List<String> colors;
    private Color defaultColor = Color.WHITE;
    private int particlePixel = 1;
    /**
     * Lightest color of the color range.
     */
    private Color colormin = Color.BLUE;
    /**
     * Intermediate color of the color range.
     */
    private Color colormed = Color.YELLOW;
    /**
     * Darkest color of the color range.
     */
    private Color colormax = Color.RED;
    private float valmin = 0;
    private float valmed = 50;
    private float valmax = 100;
    private Painter colorbarPainter;
    final private Color bottom = new Color(0, 0, 150);
    final private Color surface = Color.CYAN;
    private boolean gridVisible = false;

    public WMSMapper() {

        setTileFactory(new OfflineTileFactory());
        setMiniMapVisible(false);
        setZoomButtonsVisible(true);
        setZoomSliderVisible(true);
        //getMainMap().addMouseMotionListener(new LonLatTracker());
    }

    public void setWMS(String wmsURL) {

        String url = wmsURL.toLowerCase();
        if (url.contains("offline")) {
            setTileFactory(new OfflineTileFactory());
        } else if (url.contains("marine")) {
            setTileFactory(new MGDSTileFactory());
        } else if (url.contains("demis")) {
            setTileFactory(new DemisTileFactory());
        } else if (url.contains("street")) {
            setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
        } else {
            setTileFactory(new EmptyTileFactory());
        }

        setAddressLocation(new GeoPosition(defaultLat, defaultLon));
        setZoom(defaultZoom);
    }

    public int getNbSteps() {
        return nbSteps;
    }

    public void init() {

        nbSteps = nc.getUnlimitedDimension().getLength();

        double lonmin = Double.MAX_VALUE;
        double lonmax = -lonmin;
        double latmin = Double.MAX_VALUE;
        double latmax = -latmin;

        for (GeoPosition gp : getRegion()) {
            if (gp.getLongitude() >= lonmax) {
                lonmax = gp.getLongitude();
            }
            if (gp.getLongitude() <= lonmin) {
                lonmin = gp.getLongitude();
            }
            if (gp.getLatitude() >= latmax) {
                latmax = gp.getLatitude();
            }
            if (gp.getLatitude() <= latmin) {
                latmin = gp.getLatitude();
            }
        }

        double double_tmp;
        if (lonmin > lonmax) {
            double_tmp = lonmin;
            lonmin = lonmax;
            lonmax = double_tmp;
        }

        if (latmin > latmax) {
            double_tmp = latmin;
            latmin = latmax;
            latmax = double_tmp;
        }

        defaultLat = 0.5d * (latmin + latmax);
        defaultLon = 0.5d * (lonmin + lonmax);
        setAddressLocation(new GeoPosition(defaultLat, defaultLon));

        //double dlon_meter = Math.abs(lonmax - lonmin) * ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * avgLat / 180.d);
        double dlat_meter = Math.abs(latmax - latmin) * ONE_DEG_LATITUDE_IN_METER;

        defaultZoom = (int) Math.round(1.17 * Math.log(dlat_meter * 1.25) - 4.8);
        setZoom(defaultZoom);

        vlon = nc.findVariable("lon");
        vlat = nc.findVariable("lat");
        vmortality = nc.findVariable("mortality");
        vtime = nc.findVariable("time");
        // Set origin of time
        Calendar calendar_o = Calendar.getInstance();
        int year_o = 1;
        int month_o = Calendar.JANUARY;
        int day_o = 1;
        int hour_o = 0;
        int minute_o = 0;
        if (null != vtime.findAttribute("origin")) {
            try {
                SimpleDateFormat dFormat = TimeManager.INPUT_DATE_FORMAT;
                dFormat.setCalendar(calendar_o);
                calendar_o.setTime(dFormat.parse(vtime.findAttribute("origin").getStringValue()));
                year_o = calendar_o.get(Calendar.YEAR);
                month_o = calendar_o.get(Calendar.MONTH);
                day_o = calendar_o.get(Calendar.DAY_OF_MONTH);
                hour_o = calendar_o.get(Calendar.HOUR_OF_DAY);
                minute_o = calendar_o.get(Calendar.MINUTE);
            } catch (ParseException ex) {
                // Something went wrong, default origin of time
                // set to 0001/01/01 00:00
            }
        }
        if (vtime.findAttribute("calendar").getStringValue().equals("climato")) {
            calendar = new Day360Calendar(year_o, month_o, day_o, hour_o, minute_o);
        } else {
            calendar = new InterannualCalendar(year_o, month_o, day_o, hour_o, minute_o);
        }

        time = new double[nbSteps];
        for (int i = 0; i < nbSteps; i++) {
            time[i] = readTime(i);
        }

        bgPainter = getBgPainter();
    }

    public File getFolder() {
        return new File(getFile().getAbsolutePath().substring(0, getFile().getAbsolutePath().lastIndexOf(".nc")));
    }

    public File getFile() {
        if (null != nc) {
            return new File(nc.getLocation());
        } else {
            return null;
        }
    }

    public void drawBackground() {
        CompoundPainter cp = new CompoundPainter();
        if (null != colorbarPainter) {
            cp.setPainters(getBgPainter(), colorbarPainter);
        } else {
            cp.setPainters(getBgPainter());
        }
        cp.setCacheable(false);
        getMainMap().setOverlayPainter(cp);
    }

    public void setFile(File ncfile) {

        region = null;
        zones = null;

        if (ncfile != null && ncfile.isFile()) {
            try {
                nc = NetcdfDataset.openFile(ncfile.getAbsolutePath(), null);
                init();
                CompoundPainter cp = new CompoundPainter();
                cp.setPainters(getBgPainter());
                cp.setCacheable(false);
                getMainMap().setOverlayPainter(cp);
                /*
                 * Check whether the mask can be drawn
                 */
                gridVisible = false;
                if (null != getSimulationManager().getConfigurationFile() && getSimulationManager().isSetup()) {
                    if (null != nc.findGlobalAttribute("xml_file")) {
                        String xml = nc.findGlobalAttribute("xml_file").getStringValue();
                        if (getSimulationManager().getConfigurationFile().getPath().equals(xml)) {
                            gridVisible = true;
                        }
                    }
                }
            } catch (IOException ex) {
                SimulationManager.getLogger().log(Level.SEVERE, null, ex);
            }
        } else {
            nc = null;
            defaultLat = 48.38;
            defaultLon = -4.62;
            defaultZoom = 10;
            setAddressLocation(new GeoPosition(defaultLat, defaultLon));
            setZoom(defaultZoom);
            getMainMap().setOverlayPainter(null);
        }
    }

    public String[] getVariableList() {
        List<String> list = new ArrayList();
        list.add("None");
        list.add("time");
        for (Variable variable : nc.getVariables()) {
            List<Dimension> dimensions = variable.getDimensions();
            boolean excluded = (dimensions.size() != 2);
            if (!excluded) {
                excluded = !(dimensions.get(0).getName().equals("time") && dimensions.get(1).getName().equals("drifter"));
            }
            if (!excluded) {
                list.add(variable.getName());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public float[] getRange(String variable) throws IOException {

        Array array = nc.findVariable(variable).read();

        float[] dataset = (float[]) array.get1DJavaArray(Float.class);
        if (variable.equals("time")) {
            if (dataset[0] > dataset[dataset.length - 1]) {
                return new float[]{dataset[dataset.length - 1], dataset[0]};
            } else {
                return new float[]{dataset[0], dataset[dataset.length - 1]};
            }
        } else {
            double mean = getMean(dataset);
            double stdDeviation = getStandardDeviation(dataset, mean);
            float lower = (float) Math.max((float) (mean - 2 * stdDeviation), getMin(dataset));
            float upper = (float) Math.min((float) (mean + 2 * stdDeviation), getMax(dataset));
            System.out.println("min: " + getMin(dataset) + " max: " + getMax(dataset));
            return new float[]{lower, upper};
        }
    }

    private double getMin(float[] dataset) {
        float min = Float.MAX_VALUE;
        for (float num : dataset) {
            if (num < min) {
                min = num;
            }
        }
        return min;
    }

    private double getMax(float[] dataset) {
        float max = -1 * Float.MAX_VALUE;
        for (float num : dataset) {
            if (num > max) {
                max = num;
            }
        }
        return max;
    }

    private double getMean(float[] dataset) {
        double sum = 0;
        for (double num : dataset) {
            if (!Double.isNaN(num)) {
                sum += num;
            }
        }
        return sum / dataset.length;
    }

    private double getSquareSum(float[] dataset) {
        double sum = 0;
        for (double num : dataset) {
            if (!Double.isNaN(num)) {
                sum += num * num;
            }
        }
        return sum;
    }

    private double getStandardDeviation(float[] dataset, double mean) {
        // Return standard deviation of all the items that have been entered.
        // Value will be Double.NaN if count == 0.
        double squareSum = getSquareSum(dataset);
        return Math.sqrt(squareSum / dataset.length - mean * mean);
    }

    Painter getPainterForStep(int index) {

        final List<WMSMapper.DrawableParticle> listParticles = getParticles(index);

        Painter particleLayer = new Painter<JXMapViewer>() {
            @Override
            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                g = (Graphics2D) g.create();
                //convert from viewport to world bitmap
                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x, -rect.y);

                //drawRegion(g, map);
                for (WMSMapper.DrawableParticle particle : listParticles) {
                    drawParticle(g, map, particle);
                }
                g.dispose();
            }
        };
        return particleLayer;
    }

    void map(Painter particleLayer, Painter timeLayer) {
        CompoundPainter cp = new CompoundPainter();
        if (colorbarPainter != null) {
            cp.setPainters(bgPainter, particleLayer, timeLayer, colorbarPainter);
        } else {
            cp.setPainters(bgPainter, particleLayer, timeLayer);
        }
        cp.setCacheable(false);
        getMainMap().setOverlayPainter(cp);
    }

    public List<GeoPosition> getRegion() {
        if (null == region) {
            region = readRegion();
        }
        return region;
    }

    HashMap<String, WMSMapper.DrawableZone> getZones() {
        if (null == zones) {
            zones = readZones();
        }
        return zones;
    }

    private Painter<JXMapViewer> getBgPainter() {
        Painter<JXMapViewer> bgOverlay = new Painter<JXMapViewer>() {
            @Override
            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                g = (Graphics2D) g.create();
                //convert from viewport to world bitmap
                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x, -rect.y);

//                drawRegion(g, map);
                drawZones(g, map);
//                if (gridVisible) {
//                    drawGrid(g, map);
//                }
                g.dispose();
            }
        };
        return bgOverlay;
    }

    private List<GeoPosition> readRegion() {
        final List<GeoPosition> lregion = new ArrayList<GeoPosition>();
        try {
            ArrayFloat.D2 regionEdge = (D2) nc.findVariable("region_edge").read();
            for (int i = 0; i < regionEdge.getShape()[0]; i++) {
                lregion.add(new GeoPosition(regionEdge.get(i, 0), regionEdge.get(i, 1)));

            }
        } catch (IOException ex) {
            Logger.getLogger(WMSMapper.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return lregion;
    }

    private HashMap<String, WMSMapper.DrawableZone> readZones() {
        HashMap<String, WMSMapper.DrawableZone> lzones = new HashMap();
        if (null != nc.findGlobalAttribute("nb_zones")) {
            int nbZones = nc.findGlobalAttribute("nb_zones").getNumericValue().intValue();
            for (int iZone = 0; iZone < nbZones; iZone++) {
                List<Point2D.Float> points = new ArrayList<>();
                try {
                    Variable varZone = nc.findVariable("zone" + iZone);
                    ArrayFloat.D2 zoneEdge = (D2) varZone.read();
                    String type = varZone.findAttribute("type").getStringValue();
                    String color = varZone.findAttribute("color").getStringValue();
                    for (int i = 0; i < zoneEdge.getShape()[0]; i++) {
                        points.add(new Point2D.Float(zoneEdge.get(i, 0), zoneEdge.get(i, 1)));
                    }
                    WMSMapper.DrawableZone zone = new WMSMapper.DrawableZone(points, color);
                    lzones.put(type + "_zone" + iZone, zone);

                } catch (IOException ex) {
                    Logger.getLogger(WMSMapper.class
                            .getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return lzones;
    }

    private void drawZone(WMSMapper.DrawableZone zone, Graphics2D g, JXMapViewer map) {

        Color color = zone.getColor();
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 30);
        Color edgeColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 70);
        for (Point2D pt : zone.getPoints()) {
            Polygon polygon = new Polygon();
            addCellPoint(map, polygon, pt.getX() - 0.5, pt.getY() - 0.5);
            addCellPoint(map, polygon, pt.getX() + 0.5, pt.getY() - 0.5);
            addCellPoint(map, polygon, pt.getX() + 0.5, pt.getY() + 0.5);
            addCellPoint(map, polygon, pt.getX() - 0.5, pt.getY() + 0.5);
            g.setColor(fillColor);
            g.fill(polygon);
            g.setColor(edgeColor);
            g.draw(polygon);
        }
    }

    private void drawZones(Graphics2D g, JXMapViewer map) {

        if (null != getSimulationManager().getDataset()) {
            for (WMSMapper.DrawableZone zone : getZones().values()) {
                drawZone(zone, g, map);
            }
        }
    }

    private void addGeoPoint(JXMapViewer map, Polygon polygon, GeoPosition gp) {
        Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
        polygon.addPoint((int) pt.getX(), (int) pt.getY());
    }

    private void addCellPoint(JXMapViewer map, Polygon polygon, double x, double y) {
        double[] pos = getSimulationManager().getDataset().xy2latlon(x, y);
        addGeoPoint(map, polygon, new GeoPosition(pos[0], pos[1]));
    }

    private Point2D getPoint(JXMapViewer map, double x, double y) {
        double[] pos = getSimulationManager().getDataset().xy2latlon(x, y);
        GeoPosition gp = new GeoPosition(pos[0], pos[1]);
        return map.getTileFactory().geoToPixel(gp, map.getZoom());
    }

    private void drawMask(Graphics2D g, JXMapViewer map, int i, int j) {

        Line2D line = new Line2D.Float(getPoint(map, i - 0.5, j + 0.5), getPoint(map, i + 0.5, j + 0.5));
        g.setColor(Color.DARK_GRAY);
        g.draw(line);
        line.setLine(getPoint(map, i + 0.5, j + 0.5), getPoint(map, i + 0.5, j - 0.5));
        g.draw(line);
    }

    private void drawCell(Graphics2D g, JXMapViewer map, int i, int j) {

        Polygon polygon = new Polygon();
        addCellPoint(map, polygon, i - 0.5, j - 0.5);
        addCellPoint(map, polygon, i + 0.5, j - 0.5);
        addCellPoint(map, polygon, i + 0.5, j + 0.5);
        addCellPoint(map, polygon, i - 0.5, j + 0.5);

        Color color = getBathyColor(getSimulationManager().getDataset().getBathy(i, j));
        /*double value = getSimulationManager().getDataset().get("temp", new double[] {i, j, getSimulationManager().getDataset().get_nz() - 1}, getSimulationManager().getTimeManager().get_tO() + getSimulationManager().getTimeManager().getSimulationDuration()).doubleValue();
         Color color = getSimulationManager().getDataset().isInWater(i, j)
         ? getVarColor(value)
         : Color.DARK_GRAY;*/
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 100);
        g.setColor(fillColor);
        g.fill(polygon);
        g.draw(polygon);
    }

    SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    /*private Color getMaskColor(int i, int j) {
    
     NemoDataset dataset = (NemoDataset) getSimulationManager().getDataset();
     if (!dataset.getUMask(i, j) && !dataset.getVMask(i, j)) {
     return Color.ORANGE;
     }
     if (!dataset.getUMask(i, j)) {
     return Color.GREEN;
     }
     if (!dataset.getVMask(i, j)) {
     return Color.MAGENTA;
     }
     if (!dataset.isInWater(i, j)) {
     return Color.DARK_GRAY;
     }
     return Color.CYAN;
    
     }*/
    private Color getBathyColor(double depth) {

        float xdepth;
        if (Double.isNaN(depth)) {
            return (Color.darkGray);
        } else {
            xdepth = (float) Math.abs((getSimulationManager().getDataset().getDepthMax() - depth)
                    / getSimulationManager().getDataset().getDepthMax());
            xdepth = Math.max(0, Math.min(xdepth, 1));

        }
        return (new Color((int) (xdepth * surface.getRed()
                + (1 - xdepth) * bottom.getRed()),
                (int) (xdepth * surface.getGreen()
                + (1 - xdepth) * bottom.getGreen()),
                (int) (xdepth * surface.getBlue()
                + (1 - xdepth) * bottom.getBlue())));

    }

    private Color getVarColor(double value) {

        Color low = Color.CYAN;
        Color high = Color.ORANGE;

        float xdepth;
        if (Double.isNaN(value)) {
            return (Color.darkGray);
        } else {
            xdepth = (float) Math.abs(value - 10) / 15.f;
            xdepth = Math.max(0, Math.min(xdepth, 1));

        }
        return (new Color((int) (xdepth * high.getRed()
                + (1 - xdepth) * low.getRed()),
                (int) (xdepth * high.getGreen()
                + (1 - xdepth) * low.getGreen()),
                (int) (xdepth * high.getBlue()
                + (1 - xdepth) * low.getBlue())));
    }

    private void drawGrid(Graphics2D g, JXMapViewer map) {

        IDataset dataset = getSimulationManager().getDataset();
        for (int i = 1; i < dataset.get_nx() - 1; i++) {
            for (int j = 1; j < dataset.get_ny() - 1; j++) {
                //drawCell(g, map, i, j);
                if (!dataset.isInWater(i, j)) {
                    drawCell(g, map, i, j);
                    //drawMask(g, map, i, j);
                }
            }
        }
    }

    private void drawRegion(Graphics2D g, JXMapViewer map) {
        Polygon poly = new Polygon();
        for (GeoPosition gp : getRegion()) {
            //convert geo to world bitmap pixel
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
            poly.addPoint((int) pt.getX(), (int) pt.getY());
        }

        //do the drawing
        g.setColor(new Color(255, 255, 205, 50));
        //g.fill(poly);
        g.setColor(Color.WHITE);
        g.draw(poly);
    }

    private void drawParticle(Graphics2D g, JXMapViewer map, WMSMapper.DrawableParticle particle) {

        //create a polygon
        Point2D pt = map.getTileFactory().geoToPixel(particle, map.getZoom());
        if (particle.isLiving()) {
            Ellipse2D ellipse = new Ellipse2D.Double(pt.getX(), pt.getY(), particlePixel, particlePixel);
            g.setColor(getColor(particle.getColorValue()));
            g.fill(ellipse);
        } else {
            g.setColor(Color.BLACK);
            Rectangle2D rectangle = new Rectangle2D.Double(pt.getX() + 0.5d * particlePixel, pt.getY() - 0.5d * particlePixel, particlePixel, particlePixel);
            g.fill(rectangle);
        }
    }

    /**
     * Determines the color of the particle as a function of its depth or the
     * sea water temperature (depending on the display option).
     *
     * @param value a float, the depth or the water temperature of the particle
     * @return the Color of the particle.
     */
    private Color getColor(float value) {

        if (Float.isNaN(value)) {
            return defaultColor;
        }

        if (value <= valmed) {
            float xval = Math.abs(bound((valmed - value) / (valmed - valmin)));
            return (new Color(((int) (xval * colormin.getRed()
                    + (1 - xval) * colormed.getRed())),
                    ((int) (xval * colormin.getGreen()
                    + (1 - xval) * colormed.getGreen())),
                    ((int) (xval * colormin.getBlue()
                    + (1 - xval) * colormed.getBlue()))));
        } else {
            float xval = Math.abs(bound((valmax - value) / (valmax - valmed)));
            return (new Color(((int) (xval * colormed.getRed()
                    + (1 - xval) * colormax.getRed())),
                    ((int) (xval * colormed.getGreen()
                    + (1 - xval) * colormax.getGreen())),
                    ((int) (xval * colormed.getBlue()
                    + (1 - xval) * colormax.getBlue()))));
        }
    }

    public void setParticlePixel(int pixel) {
        this.particlePixel = pixel;
    }

    public void setDefaultColor(Color color) {
        defaultColor = color;
    }

    public void setColorbar(String variable, float valmin, float valmed, float valmax, Color colormin, Color colormed, Color colormax) {

        CompoundPainter cp = new CompoundPainter();
        if (null != variable && !variable.toLowerCase().contains("none")) {
            pcolorVariable = nc.findVariable(variable);
            if (null != pcolorVariable) {
                this.valmin = valmin;
                this.valmed = valmed;
                this.valmax = valmax;
                this.colormin = colormin;
                this.colormed = colormed;
                this.colormax = colormax;
                colorbarPainter = getColorbarPainter();
                cp.setPainters(bgPainter, colorbarPainter);
            } else {
                colorbarPainter = null;
                cp.setPainters(bgPainter);
            }
        } else {
            pcolorVariable = null;
            colorbarPainter = null;
            cp.setPainters(bgPainter);
        }
        cp.setCacheable(false);
        getMainMap().setOverlayPainter(cp);
    }

    Painter getTimePainter(final int index) {

        Painter<JXMapViewer> timePainter = new Painter<JXMapViewer>() {
            @Override
            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {

                g = (Graphics2D) g.create();
                Paint paint = g.getPaint();

                int wbar = 300;
                int hbar = 20;
                int xbar = hbar / 2;
                int ybar = h - 3 * hbar / 2;

                RoundRectangle2D bar = new RoundRectangle2D.Double(0.0, 0.0, wbar, hbar, hbar, hbar);
                g.translate(xbar, ybar);
                g.setColor(Color.BLACK);
                g.draw(bar);

                g.setColor(Color.WHITE);
                g.fill(bar);

                SimpleDateFormat dtFormat = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");
                dtFormat.setCalendar(calendar);
                String time = "Time: " + dtFormat.format(getTime(index));
                FontRenderContext context = g.getFontRenderContext();
                Font font = new Font("Dialog", Font.PLAIN, 11);
                TextLayout layout = new TextLayout(time, font, context);

                Rectangle2D bounds = layout.getBounds();
                float text_x = (float) ((wbar - bounds.getWidth()) / 2.0);
                float text_y = (float) ((hbar - layout.getAscent() - layout.getDescent()) / 2.0) + layout.getAscent() - layout.getLeading();
                g.setColor(Color.BLACK);
                layout.draw(g, text_x, text_y);

                g.setPaint(paint);
                g.translate(-xbar, -ybar);
                g.dispose();
            }
        };
        return timePainter;
    }

    private Painter<JXMapViewer> getColorbarPainter() {

        Painter<JXMapViewer> clrbarPainter;
        clrbarPainter = new Painter<JXMapViewer>() {
            @Override
            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                g = (Graphics2D) g.create();

                int wbar = 300;
                int hbar = 20;
                int xbar = (w - wbar) - hbar;
                int ybar = h - 3 * hbar / 2;
                float x = Math.abs((valmed - valmin) / (valmax - valmin));
                float offset = 0.f;

                Rectangle2D bar = new Rectangle2D.Double(0.0, 0.0, x * wbar, hbar);
                g.translate(xbar, ybar);
                g.setColor(Color.BLACK);
                g.draw(bar);

                Ellipse2D corner = new Ellipse2D.Double(-0.5f * hbar, 0.f, hbar, hbar);
                g.draw(corner);
                g.setColor(colormin);
                g.fill(corner);

                Paint paint = g.getPaint();
                GradientPaint painter = new GradientPaint(0, 0, colormin, (x + offset) * wbar, hbar, colormed);
                g.setPaint(painter);
                g.fill(bar);

                bar = new Rectangle2D.Double(x * wbar, 0.0, (1 - x) * wbar, hbar);
                g.setColor(Color.BLACK);
                g.draw(bar);

                corner = new Ellipse2D.Double(wbar - 0.5 * hbar, 0.0, hbar, hbar);
                g.draw(corner);
                g.setColor(colormax);
                g.fill(corner);

                painter = new GradientPaint((x - offset) * wbar, 0, colormed, wbar, hbar, colormax);
                g.setPaint(painter);
                g.fill(bar);

                FontRenderContext context = g.getFontRenderContext();
                Font font = new Font("Dialog", Font.PLAIN, 11);
                TextLayout layout = new TextLayout(String.valueOf(valmin), font, context);

                float text_x = 10;
                float text_y = (float) ((hbar - layout.getAscent() - layout.getDescent()) / 2.0) + layout.getAscent() - layout.getLeading();
                g.setColor(Color.BLACK);
                layout.draw(g, text_x, text_y);

                String vname = pcolorVariable.getName();
                try {
                    List<Attribute> attributes = pcolorVariable.getAttributes();

                    for (Attribute attribute : attributes) {
                        if (attribute.getName().equals("unit")) {
                            vname += " (" + attribute.getStringValue() + ")";
                            break;
                        }
                    }
                } catch (Exception e) {
// do nothing, unit will not be displayed
                }

                layout = new TextLayout(vname, font, context);
                Rectangle2D bounds = layout.getBounds();
                text_x = (float) ((wbar - bounds.getWidth()) / 2.0);
                text_y = (float) ((hbar - layout.getAscent() - layout.getDescent()) / 2.0) + layout.getAscent() - layout.getLeading();
                g.setColor(Color.BLACK);
                layout.draw(g, text_x, text_y);

                layout = new TextLayout(String.valueOf(valmax), font, context);
                bounds = layout.getBounds();
                text_x = (float) (wbar - bounds.getWidth() - 10);
                text_y = (float) ((hbar - layout.getAscent() - layout.getDescent()) / 2.0) + layout.getAscent() - layout.getLeading();
                g.setColor(Color.BLACK);
                layout.draw(g, text_x, text_y);

                g.setPaint(paint);
                g.translate(-xbar, -ybar);
                g.dispose();
            }
        };

        return clrbarPainter;
    }

    void drawText(Graphics2D g2, float x, float y, String text) {

        if (text != null && text.length() > 0) {
            FontRenderContext context = g2.getFontRenderContext();
            Font font = new Font("Dialog", Font.PLAIN, 10);
            TextLayout layout = new TextLayout(text, font, context);
            g2.setColor(Color.BLACK);
            layout.draw(g2, x, y);
        }
    }

    /**
     * Ensures that float {@code x} belongs to [0, 1]
     *
     * @param x any float
     * @return <code>x</code> if between 0 and 1, the closest boundary
     * otherwise.
     */
    private float bound(float x) {

        return Math.max(Math.min(1.f, x), 0.f);
    }

    private double readTime(int index) {
        double timeD = 0.d;
        try {
            ArrayDouble.D0 arrTime = (D0) vtime.read(new int[]{index}, new int[]{1}).reduce();
            timeD = arrTime.get();

        } catch (IOException ex) {
            Logger.getLogger(WMSMapper.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(WMSMapper.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return timeD;
    }

    private List<WMSMapper.DrawableParticle> getParticles(int index) {
        List<WMSMapper.DrawableParticle> list = new ArrayList();
        try {
            ArrayFloat.D1 arrLon = (ArrayFloat.D1) vlon.read(new int[]{index, 0}, new int[]{1, vlon.getShape(1)}).reduce(0);
            ArrayFloat.D1 arrLat = (ArrayFloat.D1) vlat.read(new int[]{index, 0}, new int[]{1, vlat.getShape(1)}).reduce(0);
            ArrayInt.D1 arrMortality = (ArrayInt.D1) vmortality.read(new int[]{index, 0}, new int[]{1, vmortality.getShape(1)}).reduce(0);
            Array arrColorVariable = null;
            if (null != pcolorVariable) {
                if (pcolorVariable.getName().equals("time")) {
                    arrColorVariable = pcolorVariable.read(new int[]{index}, new int[]{1}).reduce();
                } else {
                    arrColorVariable = pcolorVariable.read(new int[]{index, 0}, new int[]{1, pcolorVariable.getShape(1)}).reduce();
                }
            }
            int length = arrLon.getShape()[0];
            for (int i = 0; i < length; i++) {
                float lon = arrLon.get(i);
                if (arrMortality.get(i) == 0) {
                    if (null != arrColorVariable) {
                        if (arrColorVariable.getSize() < 2) {
                            list.add(new WMSMapper.DrawableParticle(lon, arrLat.get(i), arrColorVariable.getFloat(0)));
                        } else {
                            list.add(new WMSMapper.DrawableParticle(lon, arrLat.get(i), arrColorVariable.getFloat(i)));
                        }
                    } else {
                        list.add(new WMSMapper.DrawableParticle(lon, arrLat.get(i), Float.NaN));
                    }
                } else {
                    list.add(new WMSMapper.DrawableParticle(lon, arrLat.get(i)));

                }
            }
        } catch (IOException ex) {
            Logger.getLogger(WMSMapper.class
                    .getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(WMSMapper.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    public void createKML() {
        kml = KmlFactory.createKml();
        kmlDocument = kml.createAndSetDocument().withName(getFile().getName()).withOpen(true);
        final Style style = kmlDocument.createAndAddStyle().withId("randomColorIcon");
        final IconStyle iconstyle = style.createAndSetIconStyle().withColor("ffff3df0").withScale(particlePixel / 10.d);
        iconstyle.createAndSetIcon().withHref("http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png");
        kmlMainFolder = kmlDocument.createAndAddFolder();
        colors = new ArrayList();
    }

    public boolean marshalAndKMZ() throws IOException {
        File kmzFile = new File(getKMZPath());
        if (kmzFile.exists()) {
            kmzFile.delete();
        }
        return kml.marshalAsKmz(getKMZPath());
    }

    public String getKMZPath() {
        return getFile().getPath().replace(".nc", ".kmz");
    }

    public void writeKMLStep(int i) {

        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat dtFormat2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Folder stepFolder = new Folder();
        dtFormat.setCalendar(calendar);
        dtFormat2.setCalendar(calendar);
        stepFolder.withName(dtFormat2.format(getTime(i)));//.createAndSetTimeStamp().setWhen(dtFormat.format(cld.getTime()));
        if (getTime(i).before(getTime(i + 1))) {
            stepFolder.createAndSetTimeSpan().withBegin(dtFormat.format(getTime(i))).withEnd(dtFormat.format(getTime(i + 1)));
        } else {
            stepFolder.createAndSetTimeSpan().withBegin(dtFormat.format(getTime(i + 1))).withEnd(dtFormat.format(getTime(i)));
        }
        for (WMSMapper.DrawableParticle particle : getParticles(i)) {
            String coord = Double.toString(particle.getLongitude()) + "," + Double.toString(particle.getLatitude());
            Placemark placeMark = stepFolder.createAndAddPlacemark();
            String styleURL = "#" + createStyle(particle);
            placeMark.withStyleUrl(styleURL).createAndSetPoint().addToCoordinates(coord);
        }
        kmlMainFolder.addToFeature(stepFolder);
    }

    private String createStyle(WMSMapper.DrawableParticle particle) {
        String color = colorForKML(getColor(particle.getColorValue()));
        if (!colors.contains(color)) {
            colors.add(color);
            Style style = kmlDocument.createAndAddStyle().withId("IconStyle" + color);
            IconStyle iconstyle = style.createAndSetIconStyle().withColor(color).withScale(particlePixel / 10.d);
            iconstyle.createAndSetIcon().withHref("http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png");
        }
        return "IconStyle" + color;
    }

    private String colorForKML(Color color) {
        int red = color.getRed();
        int green = color.getGreen();
        int blue = color.getBlue();
        StringBuilder hexColor = new StringBuilder(8);
        hexColor.append("ff");
        String hR = Integer.toHexString(red);
        if (hR.length() < 2) {
            hR = "0" + hR;
        }
        String hB = Integer.toHexString(blue);
        if (hB.length() < 2) {
            hB = "0" + hB;
        }
        String hG = Integer.toHexString(green);
        if (hG.length() < 2) {
            hG = "0" + hG;
        }
        hexColor.append(hB);
        hexColor.append(hG);
        hexColor.append(hR);
        return hexColor.toString();
    }

    /**
     * Saves the snapshot of the specified component as a PNG picture. The name
     * of the picture includes the current time of the simulation.
     *
     * @param cpnt the Component to save as a PNG picture.
     * @param cld the Calendar of the current {@code Step} object.
     * @throws an IOException if an ouput exception occurs when saving the
     * picture.
     */
    public void screen2File(final int index) {

        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        paintAll(g);
        new Thread(new WMSMapper.ImageWriter(index, bi)).start();
    }

    Date getTime(int index) {
        if (index > nbSteps - 1) {
            double dt = time[1] - time[0];
            long ltime = (long) (time[0] + index * dt) * 1000L;
            calendar.setTimeInMillis(ltime);
        } else {
            calendar.setTimeInMillis((long) (time[index] * 1000L));
        }
        return calendar.getTime();

    }

    private class ImageWriter implements Runnable {

        private int index;
        private BufferedImage bi;
        SimpleDateFormat dtFormat;

        ImageWriter(int index, BufferedImage bi) {
            this.index = index;
            this.bi = bi;
            dtFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        }

        @Override
        public void run() {
            dtFormat.setCalendar(calendar);
            StringBuilder filename = new StringBuilder(getFile().getParent());
            filename.append(File.separator);
            String id = getFile().getName().substring(0, getFile().getName().indexOf(".nc"));
            filename.append(id);
            filename.append(File.separator);
            filename.append(id);
            filename.append("_img");
            filename.append(dtFormat.format(getTime(index)));
            filename.append(".png");
            try {
                IOTools.makeDirectories(filename.toString());
                ImageIO.write(bi, "PNG", new File(filename.toString()));
            } catch (IOException ex) {
                SimulationManager.getLogger().log(Level.SEVERE, null, ex);
            }
        }
    }

    class NasaTileFactory extends DefaultTileFactory {

        /**
         * Creates a new instance of IchthyopTileFactory
         */
        public NasaTileFactory() {
            super(new TileFactoryInfo(4, 15, 17, 300, true, true, "", "x", "y", "zoom") {
                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    int zz = 17 - zoom;
                    int z = (int) Math.pow(2, (double) zz - 1);
                    return new WMSService().toWMSURL(x - z, z - 1 - y, zz, getTileSize(zoom));
                }
            });
        }
    }

    class LocalTileFactory extends DefaultTileFactory {

        final String base = "file:/home/pverley/downloads/world.topo.bathy.";

        /**
         * Creates a new instance of IchthyopTileFactory
         */
        LocalTileFactory() {
            super(new TileFactoryInfo(
                    0, //min level
                    8, //max allowed level
                    10, // max level
                    256, //tile size
                    true, true, // x/y orientation is normal
                    "file:/home/pverley/downloads/world.topo.bathy.", // base url
                    "x", "y", "z" // url args for x, y & z
            ) {
                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    return baseURL + x + "x" + y + "x" + "z" + ".jpg";
                }
            });
        }
    }

    class MGDSTileFactory extends DefaultTileFactory {

        /**
         * Creates a new instance of IchthyopTileFactory
         */
        public MGDSTileFactory() {
            super(new TileFactoryInfo(4, 15, 17, 300, true, true, "", "x", "y", "zoom") {
                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    int zz = 17 - zoom;
                    int z = 4;
                    z = (int) Math.pow(2, (double) zz - 1);
                    WMSService wmsServ = new WMSService("https://www.gmrt.org/services/mapserver/wms_merc?request=GetCapabilities&service=WMS&CRS=EPSG:3395&", "topo");
                    return wmsServ.toWMSURL(x - z, z - 1 - y, zz, getTileSize(zoom));
                }
            });
        }
    }

    class DemisTileFactory extends DefaultTileFactory {

        /**
         * Creates a new instance of IchthyopTileFactory
         */
        public DemisTileFactory() {
            super(new TileFactoryInfo(4, 15, 17, 300, true, true, "", "x", "y", "zoom") {
                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    int zz = 17 - zoom;
                    int z = (int) Math.pow(2, (double) zz - 1);
                    return new WMSService("http://www2.demis.nl/wms/wms.asp?wms=WorldMap&", "Bathymetry,Topography,Borders,Coastlines").toWMSURL(x - z, z - 1 - y, zz, getTileSize(zoom));
                }
            });
        }
    }

    public class OfflineTileFactory extends TileFactory {

        /**
         * The empty tile image.
         */
        private BufferedImage emptyTile;
        private Color ocean = new Color(181, 208, 208);

        /**
         * Creates a new instance of EmptyTileFactory
         */
        public OfflineTileFactory() {
            this(new TileFactoryInfo("EmptyTileFactory 300x300", 1, 15, 17, 300,
                    true, true, "", "x", "y", "z"));
        }

        /**
         * Creates a new instance of EmptyTileFactory using the specified info.
         */
        public OfflineTileFactory(TileFactoryInfo info) {

            super(info);
            int tileSize = info.getTileSize(info.getMinimumZoomLevel());
            emptyTile = new BufferedImage(tileSize, tileSize,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = emptyTile.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(ocean);
            g.fillRect(0, 0, tileSize, tileSize);
            g.setColor(Color.WHITE);
            g.dispose();
//            File file = new File("/home/pverley/downloads/seamless_blue_water.jpg");
//            try {
//                emptyTile = ImageIO.read(file);
//            } catch (IOException ex) {
//                Logger.getLogger(WMSMapper.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            Graphics2D g = emptyTile.createGraphics();
//            g.dispose();
        }

        /**
         * Gets an instance of an empty tile for the given tile position and
         * zoom on the world map.
         *
         * @param x The tile's x position on the world map.
         * @param y The tile's y position on the world map.
         * @param zoom The current zoom level.
         */
        @Override
        public Tile getTile(int x, int y, int zoom) {
            return new Tile(x, y, zoom) {
                @Override
                public boolean isLoaded() {
                    return true;
                }

                @Override
                public BufferedImage getImage() {
                    return emptyTile;
                }
            };
        }

        /**
         * Override this method to load the tile using, for example, an
         * <code>ExecutorService</code>.
         *
         * @param tile The tile to load.
         */
        @Override
        protected void startLoading(Tile tile) {
            // noop
        }
    }

    class DrawableParticle extends GeoPosition {

        private float colorValue;
        private boolean isLiving;

        DrawableParticle(float lon, float lat, float colorValue) {
            super(lat, lon);
            this.colorValue = colorValue;
            isLiving = true;
        }

        private DrawableParticle(float lon, float lat) {
            super(lat, lon);
            isLiving = false;
            colorValue = Float.NaN;
        }

        public float getColorValue() {
            return colorValue;
        }

        public boolean isLiving() {
            return isLiving;
        }
    }

    class DrawableZone {

        private List<Point2D.Float> points;
        private Color color;

        DrawableZone(List<Point2D.Float> points, String color) {
            this.points = points;
            this.color = getColor(color);
        }

        public Color getColor() {
            return color;
        }

        public List<Point2D.Float> getPoints() {
            return points;
        }

        private Color getColor(String strColor) {

            if (null == strColor) {
                return Color.WHITE;
            }
            strColor = strColor.substring(1, strColor.length() - 1);
            String[] rgb = strColor.split(",");
            if (rgb.length != 3) {
                return Color.WHITE;
            }
            int red = Integer.valueOf(rgb[0].substring(rgb[0].indexOf("=") + 1));
            int green = Integer.valueOf(rgb[1].substring(rgb[1].indexOf("=") + 1));
            int blue = Integer.valueOf(rgb[2].substring(rgb[2].indexOf("=") + 1));
            return new Color(red, green, blue);

        }
    }

    private class LonLatTracker extends MouseMotionAdapter {

        @Override
        public void mouseMoved(MouseEvent e) {
            java.awt.Dimension dim = getMainMap().getTileFactory().getMapSize(getMainMap().getZoom());
            int tileSize = getMainMap().getTileFactory().getTileSize(getMainMap().getZoom());
            float wMap = dim.width;
            float hMap = dim.height;
            Rectangle view = getMainMap().getViewportBounds();
            float x0Map = view.x;
            float y0Map = view.y;
            float hPnl = view.height;
            float wPnl = view.width;
            float xPnl = e.getX();
            float yPnl = e.getY();

            float xMap = x0Map + (xPnl / wPnl) * wMap;
            float yMap = y0Map + (yPnl / hPnl) * hMap;
            Point2D pt = new Point2D.Float(xMap, yMap);

            System.out.println(x0Map + " " + y0Map + " - " + pt);

            GeoPosition gp = getMainMap().getTileFactory().pixelToGeo(pt, getMainMap().getZoom());
            System.out.println("MouseEvent " + e.getPoint() + " " + gp);
        }
    }
//    //checks for connection to the internet through dummy request
//    public static boolean isInternetReachable() {
//        try {
//            //make a URL to a known source
//            URL url = new URL("http://www.google.com");
//
//            //open a connection to that source
//            HttpURLConnection urlConnect = (HttpURLConnection) url.openConnection();
//
//            //trying to retrieve data from the source. If there
//            //is no connection, this line will fail
//            Object objData = urlConnect.getContent();
//
//        } catch (UnknownHostException e) {
//            // TODO Auto-generated catch block
//            return false;
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            return false;
//        }
//        return true;
//    }
}
