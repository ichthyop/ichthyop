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

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import javax.swing.JPanel;
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
    private final double ONE_DEG_LATITUDE_IN_METER = 111138.d;
    private int height;
    private double ratio = 1;
    final private Color bottom = new Color(0, 0, 150);
    final private Color surface = Color.CYAN;
    private final Color sealevel = Color.DARK_GRAY;
    private final Color highland = new Color(200, 150, 100);
    public static final int DEFAULT_SIZE = 500;
    private double[][] bathymetry;
    private double deepest;
    private double highest;

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
        // clear the graphics
        g2.clearRect(0, 0, w, h);

        // redraw the background when size changed
        if (hi != h || wi != w) {
            background = drawBackground(g2, w, h);
            hi = h;
            wi = w;
        }
        // draw the background into the graphics
        g2.drawImage(background, 0, 0, this);
    }

    /**
     * Draws the background of the simulation.
     *
     * @param g2 the Graphics2D to draw the background
     * @param w the width of the component
     * @param h the height of the component
     */
    private BufferedImage drawBackground(Graphics2D g2, int w, int h) {

        BufferedImage bimg = g2.getDeviceConfiguration().createCompatibleImage(w, h);
        Graphics2D graphic = bimg.createGraphics();
        graphic.setColor(new Color(223, 212, 200));
        graphic.fillRect(0, 0, w, h);

        double csizeh = Math.max(1.d, Math.ceil(h / getSimulationManager().getDataset().getGrid().get_ny()));
        double csizew = Math.max(1.d, Math.ceil(w / getSimulationManager().getDataset().getGrid().get_nx()));
        double csize = 2 * Math.max(csizeh, csizew);
        for (int i = getSimulationManager().getDataset().getGrid().get_nx(); i-- > 0;) {
            for (int j = getSimulationManager().getDataset().getGrid().get_ny(); j-- > 0;) {
                double lat = getSimulationManager().getDataset().getGrid().getLat(i, j);
                double lon = getSimulationManager().getDataset().getGrid().getLon(i, j);
                double x = w * (lon - lonmin) / (lonmax - lonmin);
                double y = h * (latmax - lat) / (latmax - latmin);
                Rectangle2D rectangle = new Rectangle2D.Double(x + 0.5 * csize, y - 0.5 * csize, csize, csize);
                graphic.setColor(getColor(i, j));
                graphic.fill(rectangle);
            }
        }
        return bimg;
    }

    public void setHeight(int height) {
        this.height = height;
        repaint();
    }

    public void init() {

        latmin = getSimulationManager().getDataset().getGrid().getLatMin();
        latmax = getSimulationManager().getDataset().getGrid().getLatMax();
        lonmin = getSimulationManager().getDataset().getGrid().getLonMin();
        lonmax = getSimulationManager().getDataset().getGrid().getLonMax();
        double avgLat = 0.5d * (latmin + latmax);
        double dlon = Math.abs(lonmax - lonmin) * ONE_DEG_LATITUDE_IN_METER * Math.cos(Math.PI * avgLat / 180.d);
        double dlat = Math.abs(latmax - latmin) * ONE_DEG_LATITUDE_IN_METER;
        ratio = dlon / dlat;

        // bathymetry
        int nx = getSimulationManager().getDataset().getGrid().get_nx();
        int ny = getSimulationManager().getDataset().getGrid().get_ny();
        bathymetry = new double[nx][ny];
        deepest = Double.MAX_VALUE;
        highest = Double.MIN_VALUE;
        for (int i = nx; i-- > 0;) {
            for (int j = ny; j-- > 0;) {
                double lat = getSimulationManager().getDataset().getGrid().getLat(i, j);
                double lon = getSimulationManager().getDataset().getGrid().getLon(i, j);
                bathymetry[i][j] = getSimulationManager().getDatasetManager().getBathymetryDataset().getBathymetry(lat, lon);
                if (bathymetry[i][j] < deepest) {
                    deepest = bathymetry[i][j];
                }
                if (bathymetry[i][j] > highest) {
                    highest = bathymetry[i][j];
                }
            }
        }
        deepest = Math.min(deepest, -0.5);
        highest = Math.max(highest, 0.5);
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

        if (getSimulationManager().getDataset().getGrid().isInWater(i, j)) {
            // zone
            for (Zone zone : getSimulationManager().getZoneManager().getZones()) {
                if (getSimulationManager().getZoneManager().isInside(i, j, zone.getKey())) {
                    return zone.getColor();
                }
            }
            // bathymetry
            double xdepth = (deepest - bathymetry[i][j]) / deepest;
            xdepth = Math.max(0, Math.min(xdepth, 1));
            return (new Color(
                    (int) (xdepth * surface.getRed() + (1 - xdepth) * bottom.getRed()),
                    (int) (xdepth * surface.getGreen() + (1 - xdepth) * bottom.getGreen()),
                    (int) (xdepth * surface.getBlue() + (1 - xdepth) * bottom.getBlue())));
        } else {
            // topography
            double xtopo = (highest - bathymetry[i][j]) / highest;
            xtopo = Math.max(0, Math.min(xtopo, 1));
            return (new Color(
                    (int) (xtopo * sealevel.getRed() + (1 - xtopo) * highland.getRed()),
                    (int) (xtopo * sealevel.getGreen() + (1 - xtopo) * highland.getGreen()),
                    (int) (xtopo * sealevel.getBlue() + (1 - xtopo) * highland.getBlue())));
        }
    }
}
