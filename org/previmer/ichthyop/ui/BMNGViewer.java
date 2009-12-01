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
        for (GeoPosition gp : getRoughRegion()) {
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
        boolean[][] bzone = new boolean[xmax - xmin + 1][ymax - ymin + 1];
        boolean[][] ezone = new boolean[xmax - xmin + 1][ymax - ymin + 1];
        for (int i = xmin; i < xmax; i++) {
            for (int j = ymin; j < ymax; j++) {
                bzone[i - xmin][j - ymin] = zone.isGridPointInZone(i, j);
            }
        }
        List<Point> listPt = new ArrayList();
        for (int i = xmin; i < xmax; i++) {
            for (int j = ymin; j < ymax; j++) {
                int ii = i - xmin;
                int jj = j - ymin;
                int im1 = Math.max(ii - 1, 0);
                int ip1 = Math.min(ii + 1, xmax - xmin);
                int jm1 = Math.max(jj - 1, 0);
                int jp1 = Math.min(jj + 1, ymax - ymin);
                ezone[ii][jj] = bzone[ii][jj] && !(bzone[im1][jj] && bzone[ip1][jj] && bzone[ii][jm1] && bzone[ii][jp1]);
                if (ezone[ii][jj]) {
                    listPt.add(new Point(i, j));
                }
            }
        }

        Point pt1 = listPt.get(0);
        GeoPosition gp = new GeoPosition(dataset.getLat(pt1.x, pt1.y), dataset.getLon(pt1.x, pt1.y));
        list.add(gp);
        listPt.remove(pt1);
        while (!listPt.isEmpty()) {
            Point closestToP1 = new Point(Integer.MAX_VALUE, Integer.MAX_VALUE);
            double distMin = getDistance(pt1, closestToP1);
            for (Point pt2 : listPt) {
                double dist = Math.sqrt(Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2));
                if (dist < distMin) {
                    closestToP1 = pt2;
                    distMin = dist;
                }
            }
            gp = new GeoPosition(dataset.getLat(closestToP1.x, closestToP1.y), dataset.getLon(closestToP1.x, closestToP1.y));
            list.add(gp);
            listPt.remove(closestToP1);
            pt1 = closestToP1;
        }
        return list;
    }

    private double getDistance(Point pt1, Point pt2) {
        return Math.sqrt(Math.pow(pt2.x - pt1.x, 2) + Math.pow(pt2.y - pt1.y, 2));
    }

    public void drawZones(Graphics2D g, JXMapViewer map) {
        if (firstCall) {
            for (Zone zone : getSimulationManager().getZoneManager().getZones(TypeZone.RELEASE)) {
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
                    //drawZones(g, map);

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
