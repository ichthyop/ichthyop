package ichthyop.ui;

/** import AWT */
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;

/** import java.util */
import java.util.ArrayList;
import java.util.Iterator;

/** import Swing */
import javax.swing.JPanel;

/** local import */
import ichthyop.core.Step;
import ichthyop.core.Zone;
import ichthyop.io.Configuration;
import ichthyop.io.Dataset;
import ichthyop.util.Constant;

/**
 * The class is the graphical component that display the steps of the simulation
 * on screen. The background of the simulation (cost line + bathymetry) is
 * painted in a {@code BufferedImage} with inner class {@code CellUI}.
 * The particles are represented by inner object {@code ParticleUI}.
 * The class provides method to transform grid coordinates into screen
 * coordinates.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 *
 * @author P.Verley
 * @see inner class CellUI
 * @see inner class ParticleUI
 */
public class SimulationUI extends JPanel {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * The step to display on screen
     */
    private Step step;
    /**
     * Dimension of the component.
     */
    private static int hi, wi;
    /**
     * Minimum latitude of the domain to display
     */
    private static double latmin;
    /**
     * Maximum latitude of the domain to display
     */
    private static double latmax;
    /**
     * Minimum longitude of the domain to display
     */
    private static double lonmin;
    /**
     * Maximum longitude of the domain to display
     */
    private static double lonmax;
    /**
     * BufferedImage in which the background (cost + bathymetry) has been
     * drawn.
     */
    private static BufferedImage background;
    /**
     * Associated {@code RenderingHints} object
     */
    private static RenderingHints hints = null;

///////////////
// Constructors
///////////////

    /**
     * Constructs an empty <code>SimulationUI</code>, intializes the range of
     * the domain and the {@code RenderingHints}.
     */
    public SimulationUI() {

        latmin = Dataset.getInstance().getLatMin();
        latmax = Dataset.getInstance().getLatMax();
        lonmin = Dataset.getInstance().getLonMin();
        lonmax = Dataset.getInstance().getLonMax();

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

    /**
     * Paints the current step of the simulation.
     * Redraws the background if the size of the component has changed and
     * draws the location of the particles on screen.
     *
     * @param g the <code>Graphics</code> object to protect
     */
    @Override
            public void paintComponent(Graphics g) {

        int h = getHeight();
        int w = getWidth();

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHints(hints);
        /** Clear the graphics */
        g2.clearRect(0, 0, w, h);

        /* Redraw the background when size changed */
        if (hi != h || wi != w) {
            drawBackground(g2, w, h);
            hi = h;
            wi = w;
        }
        /* Draw the background into the graphics */
        g2.drawImage(background, 0, 0, this);

        /* Draw the particles */
        if (step != null) {
            ParticleUI particleUI = new ParticleUI(MainFrame.getValMin(),
                    MainFrame.getValMax(), MainFrame.getDisplayColor());
            float[][] data = step.getData();
            for (int i = data.length; i-- > 0; ) {
                particleUI.draw(data[i], w, h);
                g2.setColor(particleUI.getColor());
                g2.fill(particleUI);
            }
        }
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
        CellUI cell = new CellUI();
        for (int i = Dataset.getInstance().get_nx() - 1; i-- > 0; ) {
            for (int j = Dataset.getInstance().get_ny() - 1; j-- > 0; ) {
                cell.draw(i, j, w, h);
                graphic.setColor(cell.getColor(i, j));
                graphic.fillPolygon(cell);
            }
        }
    }

    /**
     * Forces the background to repaint.
     */
    public void repaintBackground() {

        hi = -1;
        wi = -1;
        repaint();
    }

    /**
     * Transforms particle (x, y) coordinates into a screen point.
     *
     * @param xgrid a double, the particle x-coordinate
     * @param ygrid a double, the particle y-coordinate
     * @param w the width of the component
     * @param h the height of the component
     * @return an int[], the corresponding (x-sreen, y-screen) coordinates.
     */
    private int[] grid2Screen(double xgrid, double ygrid, int w, int h) {

        int[] point = new int[2];
        int igrid, jgrid;
        double dx, dy;
        double[] p1, p2, p3, p4;

        igrid = (int) xgrid;
        jgrid = (int) ygrid;
        dx = xgrid - igrid;
        dy = ygrid - jgrid;
        p1 = grid2Screen(igrid, jgrid, w, h);
        p2 = grid2Screen(igrid + 1, jgrid, w, h);
        p3 = grid2Screen(igrid, jgrid + 1, w, h);
        p4 = grid2Screen(igrid + 1, jgrid + 1, w, h);

        for (int n = 0; n < 2; n++) {
            point[n] = (int) (
                    (1.d - dx) * (1.d - dy) * p1[n]
                    + dx * (1.d - dy) * p2[n]
                    + (1.d - dx) * dy * p3[n]
                    + dx * dy * p4[n]);
        }
        return point;
    }

    /**
     * Transforms a grid cell coordinate (i, j) into a screen point.
     *
     * @param igrid an int, the i-grid coordinate
     * @param jgrid an int, the j-grid coordiante
     * @param w the width of the component
     * @param h the height of the component
     * @return a double[], the corresponding (x-sreen, y-screen) coordinates.
     */
    private double[] grid2Screen(int igrid, int jgrid, int w, int h) {

        double[] point = new double[2];

        point[0] = w * ((Dataset.getInstance().getLon(igrid, jgrid) - lonmin) /
                        Math.abs(lonmax - lonmin));
        point[1] = h * (1.d - ((Dataset.getInstance().getLat(igrid, jgrid) - latmin) /
                               Math.abs(latmax - latmin)));

        return (point);
    }

//////////
// Setters
//////////

    /**
     * Sets the step to display on screen
     * @param step the current Step of the simulation.
     */
    public void setStep(Step step) {
        this.step = step;
    }

    //////////////////////////////////////////////////////////////////////////////
    /**
     * This class is the graphical representation of a grid cell on screen.
     * It is a quadrilateral with an associated color, drawn around a specified
     * point, the center of the cell.
     * The class provides all the methods to transform the grid cell coordinates
     * into screen coordinates. The color is determined function of the
     * bathymetry or the color of the zones, depending on the display options.
     */
    private class CellUI extends Polygon {

        ///////////////////////////////
        // Declaration of the constants
        ///////////////////////////////

        /**
         * Color at the bottom.
         */
        final private Color bottom = new Color(0, 0, 150);
        /**
         * Color at the surface.
         */
        final private Color surface = Color.CYAN;

        ///////////////////////////////
        // Declaration of the variables
        ///////////////////////////////

        /**
         * The (x-screen, y-screen) coordinates of the quadrilateral.
         * point[0:3][0:1] first dimension refers to the number of points (4
         * in this case) and the second dimension, the (x, y) coordinates.
         */
        private int[][] points;
        /**
         * List of the predefined zones. Used to determine cell color.
         */
        private ArrayList<Zone> listZones;

        ///////////////
        // Constructors
        ///////////////

        /**
         * Constructs an empty <code>CellUI</code>
         */
        public CellUI() {

            points = new int[4][2];
            listZones = new ArrayList(0);
            if (MainFrame.isBgReleaseZone()) {
                listZones.addAll(Configuration.getReleaseZones());
            }
            if (MainFrame.isBgRecruitmentZone()) {
                listZones.addAll(Configuration.
                                 getRecruitmentZones());
            }
            Iterator<Zone> iter = listZones.iterator();
            while(iter.hasNext()) {
                iter.next().geo2Grid();
            }
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////

        /**
         * Computes the coordinates of the quadrilateral, around the specified
         * grid point(i, j).
         *
         * @param i an int, the i-coordinate of the cell
         * @param j an int, the j-coordinate of the cell
         * @param w the width of the component
         * @param h the height of the component
         */
        public void draw(int i, int j, int w, int h) {

            this.reset();
            points[0] = grid2Screen(i - 0.5f, j - 0.5f, w, h);
            points[1] = grid2Screen(i + 0.5f, j - 0.5f, w, h);
            points[2] = grid2Screen(i + 0.5f, j + 0.5f, w, h);
            points[3] = grid2Screen(i - 0.5f, j + 0.5f, w, h);

            for (int n = 0; n < 4; n++) {
                addPoint(points[n][0], points[n][1]);
            }
        }

        /**
         * Determines the color of cell(i, j), depending on the display option.
         * @param i an int, the i-coordinate of the cell
         * @param j an int, the j-coordinate of the cell
         * @return the Color of the cell
         */
        private Color getColor(int i, int j) {

            if (Dataset.getInstance().isInWater(i, j)) {
                Color color = getColor(Dataset.getInstance().getBathy(i, j));
                boolean found = false;
                Iterator<Zone> iter = listZones.iterator();
                Zone zone;
                while (!found && iter.hasNext()) {
                    zone = iter.next();
                    if (zone.isXYInZone(i, j)) {
                        color = zone.getColor();
                        found = true;
                    }
                }
                return (color);
            } else {
                return Color.darkGray;
            }
        }

        /**
         * Gets the color of the cell as a funtion of the bathymetry
         * @param depth a double, the bathymetry at cell's location.
         * @return the Color of the cell.
         */
        private Color getColor(double depth) {

            float xdepth = 0.f;
            if (Double.isNaN(depth)) {
                return (Color.darkGray);
            } else {
                xdepth = (float) Math.abs((Dataset.getInstance().getDepthMax() - depth) /
                                          Dataset.getInstance().getDepthMax());
                xdepth = Math.max(0, Math.min(xdepth, 1));

            }
            return (new Color((int) (xdepth * surface.getRed() +
                                     (1 - xdepth) * bottom.getRed()),
                              (int) (xdepth * surface.getGreen() +
                                     (1 - xdepth) * bottom.getGreen()),
                              (int) (xdepth * surface.getBlue() +
                                     (1 - xdepth) * bottom.getBlue())));

        }

        //---------- End of class CellUI
    }


    //////////////////////////////////////////////////////////////////////////////
    /**
     * This class is the graphical representation of a {@code Particle} object.
     * The Particle is represented by an {@code Ellipse2D} with an associated
     * color.
     */
    private class ParticleUI extends Ellipse2D.Double {

        ///////////////////////////////
        // Declaration of the constants
        ///////////////////////////////

        /**
         * Lightest color of the color range.
         */
        final private Color colormin = Color.YELLOW;
        /**
         * Darkest color of the color range.
         */
        final private Color colormax = Color.RED;

        ///////////////////////////////
        // Declaration of the variables
        ///////////////////////////////

        /**
         * Extreme value of the color range (depth or temperature)
         */
        private float valmin, valmax;
        /**
         * An int characterizing the display option.
         * @see ichthyop.util.Constant for details about the labels
         * characterizing the display options.
         */
        private int displayColor;
        /**
         * The color of the particle
         */
        private Color color;


        ///////////////
        // Constructors
        ///////////////

        /**
         * Constructs an empty {@code ParticleUI} and specifies the display
         * options.
         *
         * @param valmin a float, the minimum value of the color range. It can
         * be a minimum depth or temperature depending on the display option.
         * @param valmax a float, the maximum value of the color range. It can
         * be a maximum depth or temperature depending on the display option.
         * @param displayColor an int characterizing the display option.
         * @see ichthyop.util.Constant for details about the labels
         * characterizing the display options.

         */
        public ParticleUI(float valmin, float valmax, int displayColor) {
            this.valmin = valmin;
            this.valmax = valmax;
            this.displayColor = displayColor;
        }

        ////////////////////////////
        // Definition of the methods
        ////////////////////////////

        /**
         * Draws the particle at specified grid point
         *
         * @param data a float[] (xgrid, ygrid) particle's coordinate.
         * @param w the width of the component
         * @param h the height of the component
         */
        private void draw(float[] data, int w, int h) {

            int[] corner = grid2Screen(data[0], data[1], w, h);
            //setFrame(corner[0] - 1, corner[1] - 1, 2, 2);
            setFrame(corner[0], corner[1], 1, 1);

            switch (displayColor) {
            case Constant.DISPLAY_DEPTH:
                color = getColor(data[2]);
                break;
            case Constant.DISPLAY_TP:
                color = getColor(data[2]);
                break;
            case Constant.DISPLAY_ZONE:
                color = getColor((int) data[2]);
                break;
            default:
                color = Color.WHITE;
            }
        }

        /**
         * Gets the color of the particle.
         *
         * @return the Color of the particle
         */
        private Color getColor() {
            return color;
        }

        /**
         * Determines the color of the particle as a function of its depth or
         * the sea water temperature (depending on the display option).
         * @param value a float, the depth or the water temperature
         * of the particle
         * @return the Color of the particle.
         */
        private Color getColor(float value) {

            float xval = bound((valmax - Math.abs(value)) / (valmax - valmin));
            return (new Color(((int) (xval * colormin.getRed() +
                                      (1 - xval) * colormax.getRed())),
                              ((int) (xval * colormin.getGreen() +
                                      (1 - xval) * colormax.getGreen())),
                              ((int) (xval * colormin.getBlue() +
                                      (1 - xval) * colormax.getBlue()))));
        }

        /**
         * Determines the color of the particle as a function of its release
         * zone.
         * @param nZone an int, the index of the particle release zone.
         * @return the Color of the particle release zone.
         */
        private Color getColor(int nZone) {

            return Configuration.getReleaseZones().get(nZone).getColor();
        }

        /**
         * Ensures that float {@code x} belongs to [0, 1]
         * @param x any float
         * @return <code>x</code> if between 0 and 1, the closest boundary
         * otherwise.
         */
        private float bound(float x) {

            return Math.max(Math.min(1.f, x), 0.f);
        }

        //---------- End of class ParticleUI
    }

    //---------- End of class
}
