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
import org.ichthyop.dataset.BathymetryDataset;
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
    // Dimension of the component.
    private int hi, wi;
    // minimum latitude of the domain to display
    private double latmin;
    // maximum latitude of the domain to display
    private double latmax;
    // minimum longitude of the domain to display
    private double lonmin;
    // maximum longitude of the domain to display
    private double lonmax;
    // latitude closest to equator
    private double latClosestEq;
    // BufferedImage of the grid
    private BufferedImage biGrid;
    // panel height
    private int height;
    // ratio such as width = ratio * height
    private double ratio = 1;
    // both topography and bathymetry data
    private double[][] elevation;
    // deepest point in ocean
    private double deepest;
    // highest point on land
    private double highest;
    //
    private int sampling = 1;

///////////////////////////////
// Declaration of the constants
///////////////////////////////
    // panel default size
    public static final int DEFAULT_SIZE = 500;

////////////////////////////
// Definition of the methods
////////////////////////////
    private SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    @Override
    public void paintComponent(Graphics g) {

        int h = getHeight();
        int w = getWidth();

        Graphics2D g2 = (Graphics2D) g;
        // clear the graphics
        g2.clearRect(0, 0, w, h);

        // redraw the background when size changed
        if (hi != h || wi != w) {
            biGrid = gridToBI(g2, w, h);
            hi = h;
            wi = w;
        }
        // draw the grid into the graphics
        g2.drawImage(biGrid, 0, 0, this);
    }

    /*
     * Draw the grid
     *
     * @param g2 the Graphics2D to draw the background
     * @param w the width of the component
     * @param h the height of the component
     */
    private BufferedImage gridToBI(Graphics2D g2, int w, int h) {

        BufferedImage bi = g2.getDeviceConfiguration().createCompatibleImage(w, h);
        Graphics2D graphic = bi.createGraphics();
        graphic.setColor(Color.WHITE);
        graphic.fillRect(0, 0, w, h);

        double csizeh = Math.max(1.d, Math.ceil(h / getSimulationManager().getGrid().get_ny()));
        double csizew = Math.max(1.d, Math.ceil(w / getSimulationManager().getGrid().get_nx()));
        double csize = 2 * Math.max(csizeh, csizew) * sampling;
        int nx = getSimulationManager().getGrid().get_nx() / sampling;
        int ny = getSimulationManager().getGrid().get_ny() / sampling;
        for (int i = 0; i < nx; i++) {
            for (int j = 0; j < ny; j++) {
                double lat = getSimulationManager().getGrid().getLat(i * sampling, j * sampling);
                double lon = getSimulationManager().getGrid().getLon(i * sampling, j * sampling);
//                double wlat = w * Math.cos(Math.PI * lat / 180.d) / Math.cos(Math.PI * latClosestEq / 180.d);
//                double x = 0.5 * (w - wlat) + wlat * (lon - lonmin) / (lonmax - lonmin);
                double x = w * (lon - lonmin) / (lonmax - lonmin);
                double y = h * (latmax - lat) / (latmax - latmin);
                Rectangle2D rectangle = new Rectangle2D.Double(x + 0.5 * csize, y - 0.5 * csize, csize, csize);
                graphic.setColor(getColor(i, j));
                graphic.fill(rectangle);
            }
        }
        return bi;
    }

    public void setHeight(int height) {
        this.height = height;
        repaint();
    }

    /**
     * Assess domain extent, computes height width ratio and retrieves
     * bathymetry data.
     */
    public void init() {

        latmin = getSimulationManager().getGrid().getLatMin();
        latmax = getSimulationManager().getGrid().getLatMax();
        lonmin = getSimulationManager().getGrid().getLonMin();
        lonmax = getSimulationManager().getGrid().getLonMax();
        latClosestEq = (latmin * latmax >= 0)
                ? Math.min(Math.abs(latmin), Math.abs(latmax))
                : 0.d;
        double dlon = Math.abs(lonmax - lonmin) * Math.cos(Math.PI * latClosestEq / 180.d);
        double dlat = Math.abs(latmax - latmin);
        ratio = dlon / dlat;

        // bathymetry
        int nx = getSimulationManager().getGrid().get_nx() / sampling;
        int ny = getSimulationManager().getGrid().get_ny() / sampling;
        elevation = new double[nx][ny];
        deepest = Double.MAX_VALUE;
        highest = Double.MIN_VALUE;
        BathymetryDataset bathymetry = (BathymetryDataset) getSimulationManager().getDatasetManager().getDataset("dataset.bathymetry");
        for (int i = 0; i < nx ; i++) {
            for (int j = 0; j < ny; j++) {
                double lat = getSimulationManager().getGrid().getLat(i * sampling, j * sampling);
                double lon = getSimulationManager().getGrid().getLon(i * sampling, j * sampling);
                double[] xy = bathymetry.getGrid().latlon2xy(lat, lon);
                elevation[i][j] = (xy == null) ? Double.NaN : bathymetry.getBathymetry(xy);
                if (elevation[i][j] < deepest) {
                    deepest = elevation[i][j];
                }
                if (elevation[i][j] > highest) {
                    highest = elevation[i][j];
                }
            }
        }

        deepest = Math.min(deepest, -0.5);
        highest = Math.max(highest, 0.5);
    }

    @Override
    public int getWidth() {
        return (int) Math.ceil(height * ratio);
    }

    @Override
    public int getHeight() {
        return height;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension((int) Math.ceil(height * ratio), height);
    }

    /*
     * Determines the color of cell(i, j)
     *
     * @param i an int, the i-coordinate of the cell
     * @param j an int, the j-coordinate of the cell
     * @return the Color of the cell
     */
    private Color getColor(int i, int j) {

        if (getSimulationManager().getGrid().isInWater(i * sampling, j * sampling)) {
            // zone
            for (Zone zone : getSimulationManager().getZoneManager().getZones()) {
                double lat = getSimulationManager().getGrid().getLat(i * sampling, j * sampling);
                double lon = getSimulationManager().getGrid().getLon(i * sampling, j * sampling);
                if (getSimulationManager().getZoneManager().isInside(lat, lon, zone.getKey())) {
                    return zone.getColor();
                }
            }
            // bathymetry
            return Colorbars.getColor(Colorbars.OCEAN, elevation[i][j], deepest, 0);
        } else {
            // topography
            return Colorbars.getColor(Colorbars.LAND, elevation[i][j], 0, highest);
        }
    }
}
