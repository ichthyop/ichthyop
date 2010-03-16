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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.wms.WMSService;
import org.previmer.ichthyop.arch.ISimulationManager;
import org.previmer.ichthyop.manager.SimulationManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;
import org.previmer.ichthyop.calendar.Calendar1900;
import org.previmer.ichthyop.calendar.ClimatoCalendar;
import org.previmer.ichthyop.io.IOTools;
import ucar.ma2.ArrayDouble;
import ucar.ma2.ArrayDouble.D0;
import ucar.ma2.ArrayFloat;
import ucar.ma2.ArrayFloat.D1;
import ucar.ma2.InvalidRangeException;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;
import ucar.nc2.dataset.NetcdfDataset;

/**
 *
 * @author pverley
 */
public class WMSMapper extends JXMapKit {

    private TileFactory tileFactory = new MGDSTileFactory();
    private List<GeoPosition> region;
    private HashMap<String, DrawableZone> zones;
    private static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    private NetcdfFile nc;
    private Variable vlon, vlat, vtime;
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

    public WMSMapper() {
        setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.Custom);
        setMiniMapVisible(false);
        setZoomButtonsVisible(true);
        setZoomSliderVisible(true);
        setTileFactory(tileFactory);
    }

    public void setWMS(String wmsURL) {
        if (wmsURL.contains("marine")) {
            setTileFactory(new MGDSTileFactory());
        } else if (wmsURL.contains("demis")) {
            setTileFactory(new DemisTileFactory());
        } else if (wmsURL.contains("nasa")) {
            setTileFactory(new NasaTileFactory());
        } else {
            setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.OpenStreetMaps);
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
        vtime = nc.findVariable("time");
        if (vtime.findAttribute("calendar").getStringValue().matches("climato")) {
            calendar = new ClimatoCalendar();
        } else {
            String time_origin = vtime.findAttribute("origin").getStringValue();
            calendar = new Calendar1900(Calendar1900.getTimeOrigin(time_origin, Calendar.YEAR),
                    Calendar1900.getTimeOrigin(time_origin, Calendar.MONTH),
                    Calendar1900.getTimeOrigin(time_origin, Calendar.DAY_OF_MONTH));
        }

        time = new double[nbSteps];
        for (int i = 0; i < nbSteps - 1; i++) {
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

    public void setFile(File ncfile) {
        if (ncfile != null && ncfile.isFile()) {
            try {
                nc = NetcdfDataset.openFile(ncfile.getAbsolutePath(), null);
                init();
                getMainMap().setOverlayPainter(getBgPainter());
            } catch (IOException ex) {
                Logger.getLogger(WMSMapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            defaultLat = 48.38;
            defaultLon = -4.62;
            defaultZoom = 10;
            setAddressLocation(new GeoPosition(defaultLat, defaultLon));
            setZoom(defaultZoom);
            getMainMap().setOverlayPainter(null);
            region = null;
        }

    }

    Painter getPainterForStep(int index) {

        final List<GeoPosition> listGP = getListGeoPosition(index);

        Painter particleLayer = new Painter<JXMapViewer>() {

            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                g = (Graphics2D) g.create();
                //convert from viewport to world bitmap
                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x, -rect.y);

                //drawRegion(g, map);
                for (GeoPosition gp : listGP) {
                    drawParticle(g, map, gp);
                }
                g.dispose();
            }
        };
        return particleLayer;
    }

    void map(Painter particleLayer) {
        CompoundPainter cp = new CompoundPainter();
        cp.setPainters(bgPainter, particleLayer);
        cp.setCacheable(false);
        getMainMap().setOverlayPainter(cp);
    }

    public List<GeoPosition> getRegion() {
        if (null == region) {
            region = readRegion();
        }
        return region;
    }

    HashMap<String, DrawableZone> getZones() {
        if (null == zones) {
            zones = readZones();
        }
        return zones;
    }

    private Painter<JXMapViewer> getBgPainter() {
        Painter<JXMapViewer> bgOverlay = new Painter<JXMapViewer>() {

            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                g = (Graphics2D) g.create();
                //convert from viewport to world bitmap
                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x, -rect.y);

                drawRegion(g, map);
                drawZones(g, map);

                g.dispose();
            }
        };

        return bgOverlay;
    }

    private List<GeoPosition> readRegion() {

        final List<GeoPosition> lregion = new ArrayList<GeoPosition>();
        ArrayFloat.D1 lonEdge = (D1) nc.findGlobalAttribute("edge_lon").getValues();
        ArrayFloat.D1 latEdge = (D1) nc.findGlobalAttribute("edge_lat").getValues();
        for (int i = 0; i < lonEdge.getShape()[0]; i++) {
            lregion.add(new GeoPosition(latEdge.get(i), lonEdge.get(i)));
        }
        return lregion;
    }

    private HashMap<String, DrawableZone> readZones() {
        HashMap<String, DrawableZone> lzones = new HashMap();
        if (null != nc.findGlobalAttribute("nb_zones")) {
            int nbZones = nc.findGlobalAttribute("nb_zones").getNumericValue().intValue();
            for (int iZone = 0; iZone < nbZones; iZone++) {
                List<GeoPosition> edge = new ArrayList<GeoPosition>();
                ArrayFloat.D1 lonEdge = (D1) nc.findGlobalAttribute("zone" + iZone + "_lon").getValues();
                ArrayFloat.D1 latEdge = (D1) nc.findGlobalAttribute("zone" + iZone + "_lat").getValues();
                String type = nc.findGlobalAttribute("zone" + iZone + "_type").getStringValue();
                String color = nc.findGlobalAttribute("zone" + iZone + "_color").getStringValue();
                for (int i = 0; i < lonEdge.getShape()[0]; i++) {
                    edge.add(new GeoPosition(latEdge.get(i), lonEdge.get(i)));
                }
                DrawableZone zone = new DrawableZone(edge, color);
                lzones.put(type + "_zone" + iZone, zone);
            }
        }
        return lzones;
    }

    private void drawZone(DrawableZone zone, Graphics2D g, JXMapViewer map) {

        Polygon polygon = new Polygon();
        for (GeoPosition gp : zone.getEdge()) {
            //convert geo to world bitmap pixel
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
            polygon.addPoint((int) pt.getX(), (int) pt.getY());
        }
        //do the drawing
        Color color = zone.getColor();
        Color fillColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 30);
        g.setColor(fillColor);
        g.fill(polygon);
        Color edgeColor = new Color(color.getRed(), color.getGreen(), color.getBlue(), 70);
        g.setColor(edgeColor);
        g.draw(polygon);
    }

    private void drawZones(Graphics2D g, JXMapViewer map) {
        for (DrawableZone zone : getZones().values()) {
            drawZone(zone, g, map);
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
        g.fill(poly);
        g.setColor(Color.WHITE);
        g.draw(poly);
    }

    /*public void drawParticles() {

    if (getSimulationManager().getSimulation().getPopulation() != null) {
    Painter<JXMapViewer> particleLayer = new Painter<JXMapViewer>() {

    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
    g = (Graphics2D) g.create();
    //convert from viewport to world bitmap
    Rectangle rect = map.getViewportBounds();
    g.translate(-rect.x, -rect.y);

    drawRegion(g, map);

    Iterator it = getSimulationManager().getSimulation().getPopulation().iterator();
    while (it.hasNext()) {
    IBasicParticle particle = (IBasicParticle) it.next();
    if (particle.isLiving()) {
    drawParticle(g, map, particle);
    }
    }
    g.dispose();
    }
    };

    CompoundPainter cp = new CompoundPainter();
    cp.setPainters(particleLayer);
    cp.setCacheable(false);
    getMainMap().setOverlayPainter(cp);
    }
    }*/
    private void drawParticle(Graphics2D g, JXMapViewer map, GeoPosition gp) {

        //create a polygon
        Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
        Ellipse2D ellipse = new Ellipse2D.Double(pt.getX(), pt.getY(), 1, 1);

        //do the drawing
        g.setColor(Color.WHITE);
        g.draw(ellipse);
    }

    public ISimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    private double readTime(int index) {
        double timeD = 0.d;
        try {
            ArrayDouble.D0 arrTime = (D0) vtime.read(new int[]{index}, new int[]{1}).reduce();
            timeD = arrTime.get();
        } catch (IOException ex) {
            Logger.getLogger(WMSMapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(WMSMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return timeD;
    }

    private List<GeoPosition> getListGeoPosition(int index) {
        
        List<GeoPosition> list = new ArrayList();
        try {
            ArrayFloat.D1 arrLon = (D1) vlon.read(new int[]{index, 0}, new int[]{1, vlon.getShape(1)}).reduce();
            ArrayFloat.D1 arrLat = (D1) vlat.read(new int[]{index, 0}, new int[]{1, vlat.getShape(1)}).reduce();
            //float[] lon = (float[]) vlon.read(new int[]{index, 0}, new int[]{1, vlon.getShape(1)}).reduce().copyTo1DJavaArray();
            //float[] lat = (float[]) vlat.read(new int[]{index, 0}, new int[]{1, vlat.getShape(1)}).reduce().copyTo1DJavaArray();
            int length = arrLon.getShape()[0];
            for (int i = 0; i < length - 1; i++) {
                float lon = arrLon.get(i);
                if (!Float.isNaN(lon)) {
                    list.add(new GeoPosition(arrLat.get(i), lon));
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(WMSMapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(WMSMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    public void createKML() {
        kml = KmlFactory.createKml();
        kmlDocument = kml.createAndSetDocument().withName(getFile().getName()).withOpen(true);
        final Style style = kmlDocument.createAndAddStyle().withId("randomColorIcon");
        final IconStyle iconstyle = style.createAndSetIconStyle().withColor("ffff3df0").withScale(0.3d);
        iconstyle.createAndSetIcon().withHref("http://maps.google.com/mapfiles/kml/shapes/shaded_dot.png");
        kmlMainFolder = kmlDocument.createAndAddFolder();
    }

    public boolean marshalAndKMZ() throws IOException {
        kmlDocument.getFeature().add(kmlMainFolder);
        File kmzFile = new File(getKMZPath());
        if (kmzFile.exists()) {
            kmzFile.delete();
        }
        return kml.marshalAsKmz(getKMZPath(), kml);
    }

    public String getKMZPath() {
        return getFile().getPath().replace(".nc", ".kmz");
    }

    public void writeKMLStep(int i) {

        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        SimpleDateFormat dtFormat2 = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        Folder stepFolder = new Folder();
        dtFormat.setCalendar(calendar);
        stepFolder.withName(dtFormat2.format(getTime(i)));//.createAndSetTimeStamp().setWhen(dtFormat.format(cld.getTime()));
        stepFolder.createAndSetTimeSpan().withBegin(dtFormat.format(getTime(i))).withEnd(dtFormat.format(getTime(i + 1)));
        for (GeoPosition gp : getListGeoPosition(i)) {
            String coord = Double.toString(gp.getLongitude()) + "," + Double.toString(gp.getLatitude());
            Placemark placeMark = stepFolder.createAndAddPlacemark();
            placeMark.withStyleUrl("#randomColorIcon").createAndSetPoint().addToCoordinates(coord);
        }
        kmlMainFolder.addToFeature(stepFolder);
    }

    /**
     * Saves the snapshot of the specified component as a PNG picture.
     * The name of the picture includes the current time of the simulation.
     * @param cpnt the Component to save as a PNG picture.
     * @param cld the Calendar of the current {@code Step} object.
     * @throws an IOException if an ouput exception occurs when saving the
     * picture.
     */
    public void screen2File(final Component component, final int index) {

        BufferedImage bi = new BufferedImage(component.getWidth(),
                component.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        component.paintAll(g);
        new Thread(new ImageWriter(index, bi)).start();
    }

    Date getTime(int index) {
        calendar.setTimeInMillis((long) (time[Math.min(index, nbSteps - 1)] * 1000L));
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

        public void run() {
            dtFormat.setCalendar(calendar);
            StringBuffer filename = new StringBuffer(getFile().getParent());
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
                Logger.getLogger(WMSMapper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    class NasaTileFactory extends DefaultTileFactory {

        /** Creates a new instance of IchthyopTileFactory */
        public NasaTileFactory() {
            super(new TileFactoryInfo(8, 14, 17, 300, true, true, "", "x", "y", "zoom") {

                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    int zz = 17 - zoom;
                    int z = 4;
                    z = (int) Math.pow(2, (double) zz - 1);
                    return new WMSService().toWMSURL(x - z, z - 1 - y, zz, getTileSize(zoom));
                }
            });
        }
    }

    class LocalTileFactory extends DefaultTileFactory {

        final String base = "file:/home/pverley/downloads/world.topo.bathy.";

        /** Creates a new instance of IchthyopTileFactory */
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

        /** Creates a new instance of IchthyopTileFactory */
        public MGDSTileFactory() {
            super(new TileFactoryInfo(8, 14, 17, 300, true, true, "", "x", "y", "zoom") {

                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    int zz = 17 - zoom;
                    int z = 4;
                    z = (int) Math.pow(2, (double) zz - 1);
                    return new WMSService("http://www.marine-geo.org/services/wms?", "GMRT,topo").toWMSURL(x - z, z - 1 - y, zz, getTileSize(zoom));
                }
            });
        }
    }

    class DemisTileFactory extends DefaultTileFactory {

        /** Creates a new instance of IchthyopTileFactory */
        public DemisTileFactory() {
            super(new TileFactoryInfo(6, 14, 17, 300, true, true, "", "x", "y", "zoom") {

                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    int zz = 17 - zoom;
                    int z = 4;
                    z = (int) Math.pow(2, (double) zz - 1);
                    return new WMSService("http://www2.demis.nl/wms/wms.asp?wms=WorldMap&", "Bathymetry,Topography,Borders,Coastlines").toWMSURL(x - z, z - 1 - y, zz, getTileSize(zoom));
                }
            });
        }
    }

    class DrawableZone {

        private List<GeoPosition> edge;
        private Color color;

        DrawableZone(List<GeoPosition> edge, String color) {
            this.edge = edge;
            this.color = getColor(color);
        }

        public Color getColor() {
            return color;
        }

        public List<GeoPosition> getEdge() {
            return edge;
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
}
