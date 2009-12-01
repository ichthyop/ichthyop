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
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import org.jdesktop.swingx.JXMapViewer;
import org.jdesktop.swingx.mapviewer.DefaultTileFactory;
import org.jdesktop.swingx.mapviewer.GeoPosition;
import org.jdesktop.swingx.mapviewer.TileFactoryInfo;
import org.jdesktop.swingx.painter.CompoundPainter;
import org.jdesktop.swingx.painter.Painter;
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
public class SimuMapViewer extends JXMapKit {

    private TileFactory tileFactory = new NasaTileFactory();
    //private TileFactory tileFactory = new LocalTileFactory();
    private List<GeoPosition> region, roughRegion;
    private static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    private String id;
    private NetcdfFile nc;
    private Variable vlon, vlat;
    private int index, indexMax;
    private Thread loader;
    private boolean loadingDone = false;
    boolean canRepaint = false;
    private List<GeoPosition> currentPainter, indexp1Painter, indexm1Painter;
    private Painter bgPainter;
    boolean loadFromHeap = false;
    private List<GeoPosition>[] painterMap;
    private boolean[] loadedIndices;
    CompoundPainter cp;

    public SimuMapViewer() {
        setDefaultProvider(org.jdesktop.swingx.JXMapKit.DefaultProviders.Custom);
        setMiniMapVisible(false);
        setTileFactory(tileFactory);
        //getMainMap().setOverlayPainter(getCompoundPainter());
    }

    public int index() {
        return index;
    }

    public int getIndexMax() {
        return indexMax;
    }

    public int indexNext() {
        /*if (index < indexMax) {
        index++;
        }*/
        return index + 1;
    }

    public int indexPrevious() {
        /*if (index > 0) {
        index--;
        }*/
        return index - 1;
    }

    public int indexLast() {
        return indexMax;
    }

    public int indexFirst() {
        return 0;
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

        vlon = nc.findVariable("lon");
        vlat = nc.findVariable("lat");

        bgPainter = getBgPainter();
    }

    public void setId(String id) {
        this.id = id;
        if (null != id) {
            try {
                StringBuffer filename = new StringBuffer(System.getProperty("user.dir"));
                filename.append(File.separator);
                filename.append("img");
                filename.append(File.separator);
                filename.append(id);
                filename.append("_steps.nc");
                nc = NetcdfDataset.openFile(filename.toString(), null);
                index = Integer.MIN_VALUE / 2;
                init();
                //loadFromHeap = true;
                if (loadFromHeap) {
                    loadedIndices = new boolean[indexMax + 1];
                    painterMap = new ArrayList[indexMax + 1];
                    new Thread(new LoaderThread(0)).start();
                } else {
                    loadedIndices = null;
                    painterMap = null;
                }
                show(indexFirst());
            } catch (IOException ex) {
                Logger.getLogger(SimuMapViewer.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            this.setAddressLocation(new GeoPosition(48.5, -2.2));
            setZoom(13);
            getMainMap().setOverlayPainter(null);
            region = null;
        }
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

    private List<GeoPosition> getRoughRegion() {
        return roughRegion;
    }

    private List<GeoPosition> readRegion() {

        final List<GeoPosition> lregion = new ArrayList<GeoPosition>();
        try {
            ArrayFloat.D1 lonEdge = (D1) nc.findVariable("lon-edge").read();
            ArrayFloat.D1 latEdge = (D1) nc.findVariable("lat-edge").read();
            for (int i = 0; i < lonEdge.getShape()[0]; i++) {
                lregion.add(new GeoPosition(latEdge.get(i), lonEdge.get(i)));
            }
        } catch (IOException ex) {
            Logger.getLogger(SimuMapViewer.class.getName()).log(Level.SEVERE, null, ex);
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

    public boolean showFast(int index) {
        return loadFromHeap
                ? showFastHeap(index)
                : showFastRead(index);
    }

    private boolean showFastRead(int index) {
        if (loader == null || loadingDone) {
            loadingDone = false;
            canRepaint = false;
            loader = new Thread(new FastLoaderThread(index));
            loader.start();
            return true;
        } else {
            return false;
        }
    }

    private boolean showFastHeap(int index) {
        if (index > indexMax) {
            index = 0;
        } else if (index < 0) {
            index = indexMax;
        }
        if (loadedIndices[index]) {
            this.index = index;
            //setTime();
            canRepaint = true;
            currentPainter = painterMap[index];
            setPainter();
            return true;
        } else {
            return false;
        }
    }

    public boolean show(int index) {

        if (loader == null || loadingDone) {
            loadingDone = false;
            canRepaint = false;
            loader = new Thread(new LinkedLoaderThread(index));
            loader.start();
            return true;
        }
        if (!loadingDone && !loader.isAlive()) {
            loader.interrupt();
            loadingDone = true;
        }
        return false;
    }

    private List<GeoPosition> load(int index) {
        if (index > indexMax) {
            index = 0;
        } else if (index < 0) {
            index = indexMax;
        }
        return getListGeoPosition(index);
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
            Logger.getLogger(SimuMapViewer.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidRangeException ex) {
            Logger.getLogger(SimuMapViewer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    private CompoundPainter getCompoundPainter() {

        if (null == cp) {
            Painter<JXMapViewer> particleLayer = new Painter<JXMapViewer>() {

                public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
                    g = (Graphics2D) g.create();
                    //convert from viewport to world bitmap
                    Rectangle rect = map.getViewportBounds();
                    g.translate(-rect.x, -rect.y);

                    //drawRegion(g, map);
                    if (currentPainter != null)
                    for (GeoPosition gp : currentPainter) {
                        drawParticle(g, map, gp);
                    }
                    g.dispose();
                }
            };
            cp = new CompoundPainter();
            cp.setPainters(bgPainter, particleLayer);
            cp.setCacheable(false);
        }
        return cp;

    }

    private void setPainter() {
        getMainMap().setOverlayPainter(getCompoundPainter());
    }

    private class LoaderThread implements Runnable {

        int indexStart;

        LoaderThread(int indexStart) {
            this.indexStart = indexStart;
        }

        private void load(int index) {
            painterMap[index] = getListGeoPosition(index);
            loadedIndices[index] = true;
        }

        public void run() {
            int nbStep = indexMax + 1;
            for (int i = indexStart; i < nbStep; i++) {
                load(i);
            }
            for (int i = 0; i < indexStart; i++) {
                load(i);
            }
        }
    }

    private class FastLoaderThread implements Runnable {

        int newIndex;

        FastLoaderThread(int newIndex) {
            this.newIndex = newIndex;
        }

        public void run() {
            currentPainter = indexp1Painter;
            index = newIndex;
            //setTime();
            canRepaint = true;
            setPainter();
            indexp1Painter = load(newIndex + 1);
            loadingDone = true;
        }
    }

    private class LinkedLoaderThread implements Runnable {

        int newIndex;
        int fadingDuration = 500;

        LinkedLoaderThread(int newIndex) {
            this.newIndex = newIndex;
        }

        public void run() {

            int step = newIndex - index;
            if (step == indexMax) {
                step = -1;
            } else if (step == -indexMax) {
                step = 1;
            }
            switch (step) {
                case 1:
                    indexm1Painter = currentPainter;
                    currentPainter = indexp1Painter;
                    index = newIndex;
                    //setTime();
                    canRepaint = true;
                    setPainter();
                    indexp1Painter = load(newIndex + 1);
                    break;
                case -1:
                    indexp1Painter = currentPainter;
                    currentPainter = indexm1Painter;
                    index = newIndex;
                    //setTime();
                    canRepaint = true;
                    setPainter();
                    indexm1Painter = load(newIndex - 1);
                    break;
                case 2:
                    indexm1Painter = indexp1Painter;
                    currentPainter = load(newIndex);
                    index = newIndex;
                    //setTime();
                    canRepaint = true;
                    setPainter();
                    indexp1Painter = load(newIndex + 1);
                    break;
                case -2:
                    indexp1Painter = indexm1Painter;
                    currentPainter = load(newIndex);
                    index = newIndex;
                    //setTime();
                    canRepaint = true;
                    setPainter();
                    indexp1Painter = load(newIndex + 1);
                    break;
                case 0:
                    canRepaint = true;
                    return;
                default:
                    currentPainter = load(newIndex);
                    index = newIndex;
                    //setTime();
                    canRepaint = true;
                    setPainter();
                    indexm1Painter = load(newIndex - 1);
                    indexp1Painter = load(newIndex + 1);
                    break;
            }
            loadingDone = true;
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
}
