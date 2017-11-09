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

/**
 * import AWT
 */
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

/**
 * import java.util
 */
/**
 * import Swing
 */
import javax.swing.JPanel;

/**
 * local import
 */
import org.ichthyop.Zone;
import org.ichthyop.manager.SimulationManager;

/**
 * The class is the graphical component that display the steps of the simulation
 * on screen. The background of the simulation (cost line + bathymetry) is
 * painted in a {@code BufferedImage} with inner class {@code CellUI}. The
 * particles are represented by inner object {@code ParticleUI}. The class
 * provides method to transform grid coordinates into screen coordinates.
 *
 * <p>
 * Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 *
 * @author P.Verley
 * @see inner class CellUI
 * @see inner class ParticleUI
 */
public class SimulationPreviewPanel extends JPanel {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Dimension of the component.
     */
    private int hi, wi;
    /**
     * Minimum latitude of the domain to display
     */
    private double latmin;
    /**
     * Maximum latitude of the domain to display
     */
    private double latmax;
    /**
     * Minimum longitude of the domain to display
     */
    private double lonmin;
    /**
     * Maximum longitude of the domain to display
     */
    private double lonmax;
    /**
     * BufferedImage in which the background (cost + bathymetry) has been drawn.
     */
    private BufferedImage background;
    /**
     * Associated {@code RenderingHints} object
     */
    private RenderingHints hints = null;
    private final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    private int height = 500;
    private double ratio = 1;
    final private Color bottom = new Color(0, 0, 150);
    final private Color surface = Color.CYAN;
    private final Color land = Color.DARK_GRAY;

///////////////
// Constructors
///////////////
    /**
     * Constructs an empty <code>SimulationUI</code>, intializes the range of
     * the domain and the {@code RenderingHints}.
     */
    public SimulationPreviewPanel() {

        hi = -1;
        wi = -1;

        hints = new RenderingHints(RenderingHints.KEY_RENDERING,
                RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS,
                RenderingHints.VALUE_FRACTIONALMETRICS_ON);

    }

////////////////////////////
// Definition of the methods
////////////////////////////
    private SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    /**
     * Paints the current step of the simulation. Redraws the background if the
     * size of the component has changed and draws the location of the particles
     * on screen.
     *
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
    public void paintComponent(Graphics g) {

        int h = getHeight();
        int w = getWidth();

        Graphics2D g2 = (Graphics2D) g;
        //g2.setRenderingHints(hints);
        /**
         * Clear the graphics
         */
        g2.clearRect(0, 0, w, h);

        /* Redraw the background when size changed */
        if (hi != h || wi != w) {
            drawBackground(g2, w, h);
            hi = h;
            wi = w;
        }
        /* Draw the background into the graphics */
        g2.drawImage(background, 0, 0, this);
    }

    /**
     * Draws the background of the simulation.
     *
     * @param g2 the Graphics2D to draw the background
     * @param w the width of the component
     * @param h the height of the component
     */
    private void drawBackground(Graphics2D g2, int w, int h) {

        background = g2.getDeviceConfiguration().createCompatibleImage(w, h);
        Graphics2D graphic = background.createGraphics();
        graphic.setColor(new Color(223, 212, 200));
        graphic.fillRect(0, 0, w, h);

        for (int i = getSimulationManager().getDataset().get_nx(); i-- > 0;) {
            for (int j = getSimulationManager().getDataset().get_ny(); j-- > 0;) {
                double lat = getSimulationManager().getDataset().getLat(i, j);
                double lon = getSimulationManager().getDataset().getLon(i, j);
                double x = w * (lon - lonmin) / (lonmax - lonmin);
                double y = h * (latmax - lat) / (latmax - latmin);
                Rectangle2D rectangle = new Rectangle2D.Double(x, y, 1, 1);
                graphic.setColor(getColor(i, j));
                graphic.draw(rectangle);
            }
        }
    }

    public void setHeight(int height) {
        this.height = height;
        repaintBackground();
    }

    /**
     * Forces the background to repaint.
     */
    public void repaintBackground() {

        hi = -1;
        wi = -1;
        repaint();
    }

    public void init() {

        latmin = getSimulationManager().getDataset().getLatMin();
        latmax = getSimulationManager().getDataset().getLatMax();
        lonmin = getSimulationManager().getDataset().getLonMin();
        lonmax = getSimulationManager().getDataset().getLonMax();

        double avgLat = 0.5d * (latmin + latmax);

        double dlon = Math.abs(lonmax - lonmin) * ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * avgLat / 180.d);
        double dlat = Math.abs(latmax - latmin) * ONE_DEG_LATITUDE_IN_METER;

        ratio = dlon / dlat;
        /*if (ratio > 1) {
        width = (int) (height * ratio);
        } else if (ratio != 0.d) {
        height = (int) (width / ratio);
        }*/
        //setPreferredSize(new Dimension(width, height));
    }

    @Override
    public int getWidth() {
        return (int) (height * ratio);
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) (height * ratio), height);
    }

    /**
     * Determines the color of cell(i, j), depending on the display option.
     *
     * @param i an int, the i-coordinate of the cell
     * @param j an int, the j-coordinate of the cell
     * @return the Color of the cell
     */
    private Color getColor(int i, int j) {

        if (getSimulationManager().getDataset().isInWater(i, j)) {
            for (Zone zone : getSimulationManager().getZoneManager().getZones()) {
                if (getSimulationManager().getZoneManager().isInside(i, j, zone.getKey())) {
                    return zone.getColor();
                }
            }
            return bathyColor(getSimulationManager().getDataset().getBathy(i, j));
        } else {
            return land;
        }
    }

    /**
     * Gets the color of the cell as a function of the bathymetry
     *
     * @param depth a double, the bathymetry at cell's location.
     * @return the Color of the cell.
     */
    private Color bathyColor(double depth) {

        if (Double.isNaN(depth)) {
            return Color.WHITE;
        } else {
            float xdepth = (float) Math.abs((getSimulationManager().getDataset().getDepthMax() - depth)
                    / getSimulationManager().getDataset().getDepthMax());
            xdepth = Math.max(0, Math.min(xdepth, 1));
            return (new Color((int) (xdepth * surface.getRed()
                    + (1 - xdepth) * bottom.getRed()),
                    (int) (xdepth * surface.getGreen()
                    + (1 - xdepth) * bottom.getGreen()),
                    (int) (xdepth * surface.getBlue()
                    + (1 - xdepth) * bottom.getBlue())));
        }
    }

    //---------- End of class
}
