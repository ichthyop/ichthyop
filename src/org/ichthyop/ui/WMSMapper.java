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
package org.ichthyop.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
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
import org.ichthyop.util.IOTools;
import org.ichthyop.manager.SimulationManager;
import org.ichthyop.output.NetcdfOutputReader;

/**
 *
 * @author pverley
 */
public class WMSMapper extends JXMapKit {

    private NetcdfOutputReader ncout;
    private String imgfolder;
    private List<GeoPosition> edge;
    private List<DrawableZone> zones;
    private List<GeoPosition> mask;
    private static final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    boolean canRepaint = false;
    private Painter bgPainter;
    private double defaultLat = 48.38, defaultLon = -4.62;
    private int defaultZoom = 10;
    private double[] time;
    private Color defaultColor = Color.WHITE;
    private int particlePixel = 1;
    private Color[] colorbar;
    private float valmin = 0;
    private float valmax = 100;
    private Painter colorbarPainter;
    private Painter emptyPainter;

    public WMSMapper() {

        setTileFactory(new OfflineTileFactory());
        setMiniMapVisible(false);
        setZoomButtonsVisible(true);
        setZoomSliderVisible(true);
        emptyPainter = (Painter) (Graphics2D gd, Object t, int i, int i1) -> {
            // does not paint anything
        };
    }

    public NetcdfOutputReader getNcOut() {
        return ncout;
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

    public void init() {

        if (null != ncout) {

            defaultLat = 0.5d * (ncout.getLatmin() + ncout.getLatmax());
            defaultLon = 0.5d * (ncout.getLonmin() + ncout.getLonmax());
            //double dlon_meter = Math.abs(lonmax - lonmin) * ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * avgLat / 180.d);
            double dlat_meter = Math.abs(ncout.getLatmax() - ncout.getLatmin()) * ONE_DEG_LATITUDE_IN_METER;
            defaultZoom = (int) Math.round(1.17 * Math.log(dlat_meter * 1.25) - 4.8);
        } else {
            defaultLat = 48.38;
            defaultLon = -4.62;
            defaultZoom = 10;
        }

        setAddressLocation(new GeoPosition(defaultLat, defaultLon));
        setZoom(defaultZoom);

    }

    public File getFolder() {
        return new File(imgfolder);
    }

    public void loadFile(String ncfile) throws IOException {

        // close previous output file
        if (null != ncout) {
            ncout.close();
        }

        CompoundPainter cp = new CompoundPainter();
        if (ncfile != null) {
            ncout = new NetcdfOutputReader(ncfile);
            ncout.init();
            imgfolder = ncfile.substring(0, ncfile.lastIndexOf(".nc"));
            edge = ncout.readEdge();
            zones = ncout.readZones();
            mask = ncout.readMask();
            time = ncout.readTime();
            defaultLat = 0.5d * (ncout.getLatmin() + ncout.getLatmax());
            defaultLon = 0.5d * (ncout.getLonmin() + ncout.getLonmax());
//            double dlon_meter = Math.abs(lonmax - lonmin) * ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * avgLat / 180.d);
            double dlat_meter = Math.abs(ncout.getLatmax() - ncout.getLatmin()) * ONE_DEG_LATITUDE_IN_METER;
            defaultZoom = (int) Math.round(1.17 * Math.log(dlat_meter * 1.25) - 4.8);
            bgPainter = getPainterBackground();
            cp.setPainters(bgPainter);

        } else {
            ncout = null;
            imgfolder = null;
            edge = null;
            zones = null;
            mask = null;
            time = null;
            defaultLat = 48.38;
            defaultLon = -4.62;
            defaultZoom = 10;
            bgPainter = null;
            colorbarPainter = null;
        }
        cp.setCacheable(false);
        getMainMap().setOverlayPainter(cp);
        setAddressLocation(new GeoPosition(defaultLat, defaultLon));
        setZoom(defaultZoom);
    }

    private Painter<JXMapViewer> getPainterParticles(int itime) {

        List<DrawableParticle> listParticles = ncout.readParticles(itime);
        return (Graphics2D g, JXMapViewer map, int w, int h) -> {
            g = (Graphics2D) g.create();
            //convert from viewport to world bitmap
            Rectangle rect = map.getViewportBounds();
            g.translate(-rect.x, -rect.y);
            // draw particles
            for (DrawableParticle particle : listParticles) {
                drawParticle(g, map, particle);
            }
            g.dispose();
        };
    }

    void clear() {
        CompoundPainter cp = new CompoundPainter();
        cp.setPainters(
                null == bgPainter ? emptyPainter : bgPainter,
                null == colorbarPainter ? emptyPainter : colorbarPainter);
        cp.setCacheable(false);
        getMainMap().setOverlayPainter(cp);
        repaint();
    }

    void draw(int itime) throws IOException {

        getMainMap().setOverlayPainter(new CompoundPainter(
                null == bgPainter ? emptyPainter : bgPainter,
                getPainterParticles(itime),
                getPainterTimestamp(itime),
                null == colorbarPainter ? emptyPainter : colorbarPainter));

        // paint graphics in BufferedImage 
        BufferedImage bi = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        paint(g);
        new Thread(new ImageWriter(itime, bi)).start();
    }

    private Painter<JXMapViewer> getPainterBackground() {

        return (Graphics2D g, JXMapViewer map, int w, int h) -> {
            Rectangle rect = map.getViewportBounds();
            g.translate(-rect.x, -rect.y);
            drawEdge(g, map);
            drawMask(g, map);
            drawZones(g, map);
        };
    }

    private void drawEdge(Graphics2D g, JXMapViewer map) {

        for (GeoPosition gp : edge) {
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
            Rectangle2D rectangle = new Rectangle2D.Double(pt.getX() + 2, pt.getY() - 2, 4, 4);
            g.setColor(Color.WHITE);
            g.fill(rectangle);
        }
    }

    private void drawZones(Graphics2D g, JXMapViewer map) {

        for (DrawableZone zone : zones) {
            Color alphacolor = new Color(zone.getColor().getRed(), zone.getColor().getGreen(), zone.getColor().getBlue(), 70);
            for (GeoPosition gp : zone.getPoints()) {
                Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
                Rectangle2D rectangle = new Rectangle2D.Double(pt.getX() + 1, pt.getY() - 1, 2, 2);
                g.setColor(alphacolor);
                g.fill(rectangle);
            }
        }
    }

    private void drawMask(Graphics2D g, JXMapViewer map) {

        for (GeoPosition gp : mask) {
            Point2D pt = map.getTileFactory().geoToPixel(gp, map.getZoom());
            Rectangle2D rectangle = new Rectangle2D.Double(pt.getX() + 1, pt.getY() - 1, 2, 2);
            g.setColor(new Color(64, 64, 64, 70));
            g.fill(rectangle);
        }
    }

    private void drawParticle(Graphics2D g, JXMapViewer map, DrawableParticle particle) {

        Point2D pt = map.getTileFactory().geoToPixel(particle, map.getZoom());
        if (particle.isAlive()) {
            Ellipse2D ellipse = new Ellipse2D.Double(pt.getX(), pt.getY(), particlePixel, particlePixel);
            g.setColor(getColor(particle.getColorValue()));
            g.fill(ellipse);
        } else {
            g.setColor(Color.BLACK);
            Rectangle2D rectangle = new Rectangle2D.Double(pt.getX() + 0.5d * particlePixel, pt.getY() - 0.5d * particlePixel, particlePixel, particlePixel);
            g.fill(rectangle);
        }
    }

    /*
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

        int cinf = (int) (bound((value - valmin) / (valmax - valmin)) * (colorbar.length - 1));
        float valinf = valmin + cinf * (valmax - valmin) / colorbar.length;
        float valsup = valmin + (cinf + 1) * (valmax - valmin) / colorbar.length;
        float xval = Math.abs(bound((valsup - value) / (valsup - valinf)));
        if (cinf >= (colorbar.length - 1)) {
            return colorbar[cinf];
        } else {
            return (new Color(((int) (xval * colorbar[cinf].getRed()
                    + (1 - xval) * colorbar[cinf + 1].getRed())),
                    ((int) (xval * colorbar[cinf].getGreen()
                    + (1 - xval) * colorbar[cinf + 1].getGreen())),
                    ((int) (xval * colorbar[cinf].getBlue()
                    + (1 - xval) * colorbar[cinf + 1].getBlue()))));
        }
    }

    public void setParticlePixel(int pixel) {
        this.particlePixel = pixel;
    }

    public void setDefaultColor(Color color) {
        defaultColor = color;
    }

    public void setColorbar(String variable, float valmin, float valmax, Color[] colorbar) {

        ncout.setColorVariableName(variable);
        CompoundPainter cp = new CompoundPainter();
        if (null != variable) {
            this.valmin = valmin;
            this.valmax = valmax;
            this.colorbar = colorbar;
            colorbarPainter = getPainterColorbar();
            cp.setPainters(bgPainter, colorbarPainter);
        } else {
            this.colorbar = null;
            colorbarPainter = null;
            cp.setPainters(bgPainter);
        }
        cp.setCacheable(false);
        getMainMap().setOverlayPainter(cp);
    }

    private Painter<JXMapViewer> getPainterTimestamp(int index) {

        return (Graphics2D g, JXMapViewer map, int w, int h) -> {

            int wbar = 300;
            int hbar = 20;
            int xbar = hbar / 2;
            int ybar = h - 3 * hbar / 2;
            g.translate(xbar, ybar);

            RoundRectangle2D bar = new RoundRectangle2D.Double(0.0, 0.0, wbar, hbar, hbar, hbar);
            g.setColor(Color.BLACK);
            g.draw(bar);
            g.setColor(Color.WHITE);
            g.fill(bar);

            SimpleDateFormat dtFormat = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");
            dtFormat.setCalendar(ncout.getCalendar());
            FontRenderContext context = g.getFontRenderContext();
            Font font = new Font("Dialog", Font.PLAIN, 11);
            TextLayout layout = new TextLayout("Time: " + dtFormat.format(getTime(index)), font, context);
            Rectangle2D bounds = layout.getBounds();
            float text_x = (float) ((wbar - bounds.getWidth()) / 2.0);
            float text_y = (float) ((hbar - layout.getAscent() - layout.getDescent()) / 2.0) + layout.getAscent() - layout.getLeading();
            g.setColor(Color.BLACK);
            layout.draw(g, text_x, text_y);

            g.translate(-xbar, -ybar);
        };
    }

    private Painter getPainterColorbar() {

        return (Graphics2D g, Object o, int w, int h) -> {

            int wbar = 400;
            int hbar = 30;
            int xbar = (w - wbar) - hbar;
            int ybar = h - 3 * hbar / 2;
            g.translate(xbar, ybar);

            Colorbars.draw(g, colorbar, wbar, hbar);

            FontRenderContext context = g.getFontRenderContext();
            Font font = new Font("Dialog", Font.PLAIN, 11);
            // colorbar min
            TextLayout layout = new TextLayout(String.valueOf(valmin), font, context);
            float text_x = 10;
            //float text_y = (float) ((hbar - layout.getAscent() - layout.getDescent()) / 2.0) + layout.getAscent() - layout.getLeading();
            float text_y = -10;
            g.setColor(Color.BLACK);
            layout.draw(g, text_x, text_y);
            // colorbar name
            layout = new TextLayout(ncout.getColorVariableLongname(), font, context);
            Rectangle2D bounds = layout.getBounds();
            text_x = (float) ((wbar - bounds.getWidth()) / 2.0);
            //text_y = (float) ((hbar - layout.getAscent() - layout.getDescent()) / 2.0) + layout.getAscent() - layout.getLeading();
            g.setColor(Color.BLACK);
            layout.draw(g, text_x, text_y);
            // colorbar max
            layout = new TextLayout(String.valueOf(valmax), font, context);
            bounds = layout.getBounds();
            text_x = (float) (wbar - bounds.getWidth() - 10);
            // = (float) ((hbar - layout.getAscent() - layout.getDescent()) / 2.0) + layout.getAscent() - layout.getLeading();
            g.setColor(Color.BLACK);
            layout.draw(g, text_x, text_y);

            g.translate(-xbar, -ybar);
        };
    }

    private void drawText(Graphics2D g2, float x, float y, String text) {

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

    private Date getTime(int itime) {
        Calendar calendar = ncout.getCalendar();
        if (itime > time.length - 1) {
            double dt = time[1] - time[0];
            long ltime = (long) (time[0] + itime * dt) * 1000L;
            calendar.setTimeInMillis(ltime);
        } else {
            calendar.setTimeInMillis((long) (time[itime] * 1000L));
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
            dtFormat.setCalendar(ncout.getCalendar());
            StringBuilder filename = new StringBuilder(ncout.getFile().getParent());
            filename.append(File.separator);
            String id = ncout.getFile().getName().substring(0, ncout.getFile().getName().indexOf(".nc"));
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

    class MGDSTileFactory extends DefaultTileFactory {

        /**
         * Creates a new instance of IchthyopTileFactory
         */
        public MGDSTileFactory() {
            super(new TileFactoryInfo(4, 15, 17, 300, true, true, "", "x", "y", "zoom") {
                @Override
                public String getTileUrl(int x, int y, int zoom) {
                    int zz = 17 - zoom;
                    int z = (int) Math.pow(2, (double) zz - 1);
                    return new WMSService("http://www.marine-geo.org/services/wms?", "GMRT,topo").toWMSURL(x - z, z - 1 - y, zz, getTileSize(zoom));
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
        private final Color OCEAN = new Color(181, 208, 208);

        /**
         * Creates a new instance of EmptyTileFactory
         */
        public OfflineTileFactory() {
            this(new TileFactoryInfo("EmptyTileFactory 300x300", 1, 15, 17, 300,
                    true, true, "", "x", "y", "z"));
        }

        /**
         * Creates a new instance of EmptyTileFactory using the specified info.
         *
         * @param info
         */
        public OfflineTileFactory(TileFactoryInfo info) {

            super(info);
            int tileSize = info.getTileSize(info.getMinimumZoomLevel());
            emptyTile = new BufferedImage(tileSize, tileSize,
                    BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = emptyTile.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(OCEAN);
            g.fillRect(0, 0, tileSize, tileSize);
            g.setColor(Color.WHITE);
            g.dispose();
        }

        /**
         * Gets an instance of an empty tile for the given tile position and
         * zoom on the world map.
         *
         * @param x The tile's x position on the world map.
         * @param y The tile's y position on the world map.
         * @param zoom The current zoom level.
         * @return
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
}
