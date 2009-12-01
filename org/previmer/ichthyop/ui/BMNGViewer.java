/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.JPanel;
import org.jdesktop.swingx.JXMapKit;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactory;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.mapviewer.wms.WMSService;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;
import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.Zone;
import org.previmer.ichthyop.arch.IBasicParticle;
import org.previmer.ichthyop.arch.IDataset;
import org.previmer.ichthyop.arch.ISimulationManager;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 *
 * @author pverley
 */
public class BMNGViewer extends JXMapKit {

    private TileFactory tileFactory = new NasaTileFactory();
    private static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    List<GeoPosition> region, roughRegion;
    List<List<GeoPosition>> zones;
    boolean firstCall = true;

    public BMNGViewer() {
        setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.Custom);
        setMiniMapVisible(false);
        setTileFactory(tileFactory);
    }

    public void init() {

        double latmin = getSimulationManager().getDataset().getLatMin();
        double latmax = getSimulationManager().getDataset().getLatMax();
        double lonmin = getSimulationManager().getDataset().getLonMin();
        double lonmax = getSimulationManager().getDataset().getLonMax();

        roughRegion = new ArrayList();
        roughRegion.add(new GeoPosition(latmin, lonmin));
        roughRegion.add(new GeoPosition(latmin, lonmax));
        roughRegion.add(new GeoPosition(latmax, lonmax));
        roughRegion.add(new GeoPosition(latmax, lonmin));

        double avgLat = 0.5d * (latmin + latmax);
        double avgLon = 0.5d * (lonmin + lonmax);
        setAddressLocation(new GeoPosition(avgLat, avgLon));

        //double dlon_meter = Math.abs(lonmax - lonmin) * ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * avgLat / 180.d);
        double dlat_meter = Math.abs(latmax - latmin) * ONE_DEG_LATITUDE_IN_METER;

        int zoom = (int) Math.round(1.17 * Math.log(dlat_meter * 1.25) - 4.8);
        setZoom(zoom);

        zones = new ArrayList(getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE).size());
    }

    private List<GeoPosition> getRegion() {
        if (null == region) {
            region = makeRegion();
        }
        return region;
    }

    private List<GeoPosition> getRoughRegion() {
        return roughRegion;
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

    private void drawGrid(Graphics2D g, JXMapViewer map) {

        IDataset dataset = getSimulationManager().getDataset();
        for (int i = 10; i < dataset.get_nx() - 1; i += 10) {
            for (int j = 10; j < dataset.get_ny() - 1; j += 10) {
                Point2D pt = map.getTileFactory().geoToPixel(new GeoPosition(dataset.getLat(i, j), dataset.getLon(i, j)), map.getZoom());
                Rectangle2D rectangle = new Rectangle2D.Double(pt.getX(), pt.getY(), 2, 2);
                //do the drawing
                g.setColor(Color.WHITE);
                g.draw(rectangle);
            }
        }
    }

    private void drawZone(List<GeoPosition> zoneEdge, Graphics2D g, JXMapViewer map) {

        Polygon polygon = new Polygon();
        for (GeoPosition gp : zoneEdge) {
            //convert geo to world bitmap pixel
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
            polygon.addPoint((int) pt.getX(), (int) pt.getY());
        }
        //do the drawing
        g.setColor(new Color(255, 0, 0, 50));
        g.fill(polygon);
        g.setColor(Color.RED);
        g.draw(polygon);

    }

    private List<GeoPosition> getZoneEdge(Zone zone) {
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

    public void drawZones(Graphics2D g, JXMapViewer map) {
        if (firstCall) {
            for (Zone zone : getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE)) {
                System.out.println(zone);
                zone.init();
                zones.add(getZoneEdge(zone));
            }
        }
        firstCall = false;
        for (List<GeoPosition> zoneEdge : zones) {
            drawZone(zoneEdge, g, map);
        }

    }

    public void drawParticles() {

        if (getSimulationManager().getSimulation().getPopulation() != null) {
            Painter<JXMapViewer> particleLayer = new Painter<JXMapViewer>() {

                public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                    g = (Graphics2D) g.create();
                    //convert from viewport to world bitmap
                    Rectangle rect = map.getViewportBounds();
                    g.translate(-rect.x, -rect.y);

                    drawRegion(g, map);
                    drawZones(g, map);
                    //drawGrid(g, map);

                    if (getSimulationManager().getSimulation().getPopulation() != null) {
                        Iterator it = getSimulationManager().getSimulation().getPopulation().iterator();
                        while (it.hasNext()) {
                            IBasicParticle particle = (IBasicParticle) it.next();
                            if (particle.isLiving()) {
                                drawParticle(g, map, particle);
                            }
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
    }

    private void drawParticle(Graphics2D g, JXMapViewer map, final IBasicParticle particle) {

        //create a polygon
        Point2D pt = map.getTileFactory().geoToPixel(new GeoPosition(particle.getLat(), particle.getLon()), map.getZoom());
        Ellipse2D ellipse = new Ellipse2D.Double(pt.getX(), pt.getY(), 1, 1);

        //do the drawing
        g.setColor(Color.WHITE);
        g.draw(ellipse);
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.setZoomButtonsVisible(enabled);
        this.setZoomSliderVisible(enabled);
        getMainMap().setZoomEnabled(enabled);
        getMainMap().setPanEnabled(enabled);
    }

    public ISimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
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
}
