package ichthyop.util;

import java.text.*;
import java.util.*;
import java.awt.*;
import javax.swing.*;

/**
 *
 * <p>Title: ColorBar</p>
 *
 * <p>Description: Creates a colorbar</p>
 *
 * <p>Copyright: Copyright (c) Philippe VERLEY 2007</p>
 *
 *
 */

public class ColorBar extends JPanel {

    private ColorPane colorPane;
    private JLabel lblTitle;
    private JLabel lblMin, lblMid, lblMax;
    private int alignement;
    private NumberFormat nbFormat;

    //--------------------------------------------------------------------------
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

    //--------------------------------------------------------------------------
    private void createUI() {

        int top = 0, bottom = 0;

        switch (alignement) {
        case Resources.HORIZONTAL:
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

    //##########################################################################
    private class ColorPane extends JPanel {

        Color clrMin, clrMax;
        int alignement;

        //--------------------------------------------------------------------------
        private ColorPane(Color clrMin, Color clrMax, int alignement) {
            super();
            this.setBorder(BorderFactory.createLineBorder(Color.black));
            this.clrMin = clrMin;
            this.clrMax = clrMax;
            this.alignement = alignement;
        }

        //--------------------------------------------------------------------------
        public void paintComponent(Graphics g) {
            float x_clr;
            int nx;
            super.paintComponent(g);
            if (alignement == Resources.HORIZONTAL) {
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
            } else if (alignement == Resources.VERTICAL) {
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

}
