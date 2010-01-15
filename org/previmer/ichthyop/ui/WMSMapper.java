/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

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
import java.util.List;
import javax.imageio.ImageIO;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
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
    private static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    private NetcdfFile nc;
    private Variable vlon, vlat, vtime;
    private int indexMax;
    boolean canRepaint = false;
    private Painter bgPainter;
    boolean loadFromHeap = false;
    private double defaultLat = 48.38, defaultLon = -4.62;
    private int defaultZoom = 10;
    private Calendar calendar;

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

    public int getIndexMax() {
        return indexMax;
    }

    public void init() {

        indexMax = nc.getUnlimitedDimension().getLength() - 1;

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
            calendar = new Calendar1900(getSimulationManager().getTimeManager().getTimeOrigin(time_origin, Calendar.YEAR),
                    getSimulationManager().getTimeManager().getTimeOrigin(time_origin, Calendar.MONTH),
                    getSimulationManager().getTimeManager().getTimeOrigin(time_origin, Calendar.DAY_OF_MONTH));
        }

        bgPainter = getBgPainter();
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

    void map(final MapStep mapStep) {
        Painter<JXMapViewer> particleLayer = new Painter<JXMapViewer>() {

            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                g = (Graphics2D) g.create();
                //convert from viewport to world bitmap
                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x, -rect.y);

                //drawRegion(g, map);
                for (GeoPosition gp : mapStep.getParticlesGP()) {
                    drawParticle(g, map, gp);
                }
                g.dispose();
            }
        };
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

    private Painter<JXMapViewer> getBgPainter() {
        Painter<JXMapViewer> bgOverlay = new Painter<JXMapViewer>() {

            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                g = (Graphics2D) g.create();
                //convert from viewport to world bitmap
                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x, -rect.y);

                drawRegion(g, map);

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

    public void drawRegion() {

        Painter<JXMapViewer> polygonOverlay = new Painter<JXMapViewer>() {

            public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                g = (Graphics2D) g.create();
                //convert from viewport to world bitmap
                Rectangle rect = map.getViewportBounds();
                g.translate(-rect.x, -rect.y);

                drawRegion(g, map);

                g.dispose();
            }
        };

        CompoundPainter cp = new CompoundPainter();
        cp.setPainters(polygonOverlay);
        cp.setCacheable(false);
        getMainMap().setOverlayPainter(cp);
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
        double time = 0.d;
        try {
            ArrayDouble.D0 arrTime = (D0) vtime.read(new int[]{index}, new int[]{1}).reduce();
            time = arrTime.get();
        } catch (IOException ex) {
            Logger.getLogger(WMSMapper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(WMSMapper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return time;
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

    /**
     * Saves the snapshot of the specified component as a PNG picture.
     * The name of the picture includes the current time of the simulation.
     * @param cpnt the Component to save as a PNG picture.
     * @param cld the Calendar of the current {@code Step} object.
     * @throws an IOException if an ouput exception occurs when saving the
     * picture.
     */
    public void screen2File(Component component, Calendar calendar) {

        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        dtFormat.setCalendar(calendar);
        StringBuffer fileName = new StringBuffer(getFile().getParent());
        fileName.append(File.separator);
        String id = getFile().getName().substring(0, getFile().getName().indexOf(".nc"));
        fileName.append(id);
        fileName.append(File.separator);
        fileName.append(id);
        fileName.append("_img");
        fileName.append(dtFormat.format(calendar.getTime()));
        fileName.append(".png");

        BufferedImage bi = new BufferedImage(component.getWidth(),
                component.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        component.paintAll(g);
        try {
            IOTools.makeDirectories(fileName.toString());
            ImageIO.write(bi, "PNG", new File(fileName.toString()));

        } catch (IOException ex) {
            Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public MapStep getMapStep(int index) {
        MapStep mapStep = new MapStep(index);
        mapStep.process();
        return mapStep;
    }

    public class MapStep {

        private int index;
        private List<GeoPosition> gp;
        private Long time;

        MapStep(int index) {
            this.index = index;
        }

        void process() {
            gp = getListGeoPosition(index);
            time = (long) (readTime(index) * 1000L);
        }

        Calendar getCalendar() {
            calendar.setTimeInMillis(time);
            return calendar;
        }

        List<GeoPosition> getParticlesGP() {
            return gp;
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
}
