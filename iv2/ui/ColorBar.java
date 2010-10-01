package ichthyop.ui;

/** import AWT */
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Graphics;
import java.text.NumberFormat;
import java.util.Locale;

/** import Swing */
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.BorderFactory;

/** local import */
import ichthyop.util.Constant;

/**
 * The class creates a ColorBar within a JPanel. The ColorBar provides a few
 * attributes:
 * <ul>
 * <li>a title
 * <li>alignement (vertical or horizontal)
 * <li>labels for minimum, mean an maximum values.
 * </ul>
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */

public class ColorBar extends JPanel {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * The panel to paint the colorbar
     * @see inner class #ColorPane
     */
    private ColorPane colorPane;
    /**
     * Label to display the title
     */
    private JLabel lblTitle;
    /**
     * Label to display values of the colorbar
     */
    private JLabel lblMin, lblMid, lblMax;
    /**
     * Alignement of the color bar. Horizontal = 0; Vertical = 1
     */
    private int alignement;
    /**
     * Number format used to format the values of the colorbar.
     */
    private NumberFormat nbFormat;

///////////////
// Constructors
///////////////

    /**
     * Constructs a new Colorbar with specified attributes.
     *
     * @param title the String to display as title
     * @param alignement Horizontal = 0; Vertical = 1
     * @param valMin a float, minimum value of the range
     * @param valMax a float, the maximum value of the range
     * @param clrMin the Color associated to the minimum value
     * @param clrMax the Color associated to the maximum value
     */
    public ColorBar(String title, int alignement, float valMin, float valMax,
                    Color clrMin, Color clrMax) {

        super(new GridBagLayout());
        lblTitle = new JLabel(title);
        this.alignement = alignement;
        nbFormat = NumberFormat.getInstance(Locale.getDefault());
        nbFormat.setMinimumIntegerDigits(1);
        nbFormat.setMaximumIntegerDigits(5);
        nbFormat.setMinimumFractionDigits(1);
        nbFormat.setMaximumFractionDigits(1);
        colorPane = new ColorPane(clrMin, clrMax, alignement);
        lblMin = new JLabel(nbFormat.format(valMin));
        lblMax = new JLabel(nbFormat.format(valMax));
        lblMid = new JLabel(nbFormat.format(valMin + (valMax - valMin) / 2));
        createUI();
    }

///////////////////////////
// Definition of the method
///////////////////////////

    /**
     * Creates the Colorbar
     */
    private void createUI() {

        int top = 0, bottom = 0;

        switch (alignement) {
        case Constant.HORIZONTAL:
            add(lblTitle, new GridBagConstraints(0, 0, 3, 1, 60, 10,
                                                 GridBagConstraints.CENTER,
                                                 GridBagConstraints.NONE,
                                                 new Insets(top, 5, bottom, 5),
                                                 0, 0));
            add(colorPane, new GridBagConstraints(0, 1, 3, 1, 60, 10,
                                                  GridBagConstraints.CENTER,
                                                  GridBagConstraints.HORIZONTAL,
                                                  new Insets(top, 5, bottom, 5),
                                                  0, 0));
            add(lblMin, new GridBagConstraints(0, 2, 1, 1, 20, 10,
                                               GridBagConstraints.WEST,
                                               GridBagConstraints.NONE,
                                               new Insets(top, 5, bottom, 5), 0,
                                               0));
            add(lblMid, new GridBagConstraints(1, 2, 1, 1, 20, 10,
                                               GridBagConstraints.CENTER,
                                               GridBagConstraints.NONE,
                                               new Insets(top, 5, bottom, 5), 0,
                                               0));
            add(lblMax, new GridBagConstraints(2, 2, 1, 1, 20, 10,
                                               GridBagConstraints.EAST,
                                               GridBagConstraints.NONE,
                                               new Insets(top, 5, bottom, 5), 0,
                                               0));
            break;
        }
    }

////////////////////////
// Inner class ColorPane
////////////////////////

    /**
     * Inner class ColorPane. The JPanel where the colorbar is effectively
     * painted.
     */
    private class ColorPane extends JPanel {

        ///////////////////////////////
        // Declaration of the variables
        ///////////////////////////////

        /**
         * Color associated to the minimum value of the colorbar
         */
        Color clrMin;
        /**
         * Color associated to the maximum value of the colorbar
         */
        Color clrMax;
        /**
         * Alignement of the colorbar. Horizontal = 0; Vertical = 1
         */
        int alignement;

        ///////////////
        // Constructors
        ///////////////

        /**
         * Constructs a new ColorPane with the specified attributes.
         *
         * @param clrMin the Color associated to the minimum value
         * @param clrMax the Color associated to the maximum value
         * @param alignement Horizontal = 0; Vertical = 1
         */
        private ColorPane(Color clrMin, Color clrMax, int alignement) {

            super();
            this.setBorder(BorderFactory.createLineBorder(Color.black));
            this.clrMin = clrMin;
            this.clrMax = clrMax;
            this.alignement = alignement;
        }

        ///////////////////////////
        // Definition of the method
        ///////////////////////////
        /**
         * Paints the Colorbar in the <code>Graphics</code> of the JPanel.
         *
         * @param g the <code>Graphics</code> object to draw the colorbar
         */
        public void paintComponent(Graphics g) {

            float x_clr;
            int nx;
            super.paintComponent(g);
            if (alignement == Constant.HORIZONTAL) {
                nx = getWidth();
                for (int x = 0; x < nx; x++) {
                    x_clr = (float) (nx - x) / nx;
                    g.setColor(new Color((int) (x_clr * clrMin.getRed() +
                                                (1 - x_clr) * clrMax.getRed()),
                                         (int) (x_clr * clrMin.getGreen() +
                                                (1 - x_clr) * clrMax.getGreen()),
                                         (int) (x_clr * clrMin.getBlue() +
                                                (1 - x_clr) * clrMax.getBlue())));
                    g.drawLine(x, 0, x, getHeight());
                }
            } else if (alignement == Constant.VERTICAL) {
                nx = getHeight();
                for (int x = 0; x < nx; x++) {
                    x_clr = (float) (nx - x) / nx;
                    g.setColor(new Color((int) (x_clr * clrMin.getRed() +
                                                (1 - x_clr) * clrMax.getRed()),
                                         (int) (x_clr * clrMin.getGreen() +
                                                (1 - x_clr) * clrMax.getGreen()),
                                         (int) (x_clr * clrMin.getBlue() +
                                                (1 - x_clr) * clrMax.getBlue())));
                    g.drawLine(0, x, getWidth(), x);
                }

            }
        }
    }
    //---------- End of inner class ColorPane

    //---------- End of class
}
