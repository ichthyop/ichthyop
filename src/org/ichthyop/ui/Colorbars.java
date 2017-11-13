/*
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2017
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
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.List;
import javax.swing.ImageIcon;

/**
 *
 * @author pverley
 */
public class Colorbars {

    public final static Color[] PARULA = new Color[]{
        new Color(62, 39, 169),
        new Color(71, 91, 250),
        new Color(40, 151, 236),
        new Color(18, 191, 186),
        new Color(129, 204, 89),
        new Color(252, 188, 63),
        new Color(250, 251, 21)
    };

    public final static Color[] JET = new Color[]{
        new Color(0, 0, 255),
        new Color(0, 128, 255),
        new Color(0, 255, 255),
        new Color(128, 255, 128),
        new Color(255, 255, 0),
        new Color(255, 128, 0),
        new Color(255, 0, 0)
    };

    public final static Color[] HSV = new Color[]{
        new Color(255, 0, 0),
        new Color(255, 219, 0),
        new Color(73, 255, 0),
        new Color(0, 255, 146),
        new Color(0, 146, 255),
        new Color(73, 0, 255),
        new Color(255, 0, 219)
    };

    public final static Color[] HOT = new Color[]{
        new Color(128, 0, 0),
        new Color(255, 0, 0),
        new Color(255, 255, 0),
        new Color(255, 255, 255)
    };

    public final static Color[] COOL = new Color[]{
        new Color(0, 255, 255),
        new Color(255, 0, 255)
    };

    public final static Color[] SPRING = new Color[]{
        new Color(255, 0, 255),
        new Color(255, 255, 0)
    };

    public final static Color[] SUMMER = new Color[]{
        new Color(0, 128, 102),
        new Color(255, 255, 102)
    };

    public final static Color[] AUTUMN = new Color[]{
        new Color(255, 0, 0),
        new Color(255, 255, 0)
    };

    public final static Color[] WINTER = new Color[]{
        new Color(0, 0, 255),
        new Color(0, 255, 128)
    };

    public final static Color[] BONE = new Color[]{
        new Color(0, 0, 16),
        new Color(255, 255, 255)
    };

    public final static Color[] COPPER = new Color[]{
        new Color(0, 0, 0),
        new Color(255, 200, 127)
    };

    public final static Color[] PINK = new Color[]{
        new Color(105, 0, 0),
        new Color(255, 255, 255)
    };

    public final static List<Color[]> ALL = Arrays.asList(PARULA, JET, HSV, HOT, COOL, SPRING, SUMMER, AUTUMN, WINTER, BONE, COPPER, PINK);

    public final static List<String> NAMES = Arrays.asList("parula", "jet", "hsv", "hot", "cool", "spring", "summer", "autumn", "winter", "bone", "copper", "pink");

    public static void draw(Graphics2D g, Color[] colorbar, float wbar, float hbar) {
        float x = 1.f / (colorbar.length - 1);

        // black countour
        Rectangle2D bar = new Rectangle2D.Double(0.5 * hbar, 0, wbar - hbar, hbar);
        g.setColor(Color.BLACK);
        g.draw(bar);
        // left round corner
        Ellipse2D corner = new Ellipse2D.Double(0, 0, hbar, hbar);
        g.setColor(Color.BLACK);
        g.draw(corner);
        g.setColor(colorbar[0]);
        g.fill(corner);
        // right round corner
        corner = new Ellipse2D.Double(wbar - hbar, 0, hbar, hbar);
        g.setColor(Color.BLACK);
        g.draw(corner);
        g.setColor(colorbar[colorbar.length - 1]);
        g.fill(corner);
        // gradients
        float offset = 0.2f;
        for (int i = 0; i < colorbar.length - 1; i++) {
            GradientPaint painter = new GradientPaint(
                    (i + offset) * x * wbar, 0, colorbar[i],
                    (i + 1 - offset) * x * wbar, 0, colorbar[i + 1]);
            g.setPaint(painter);
            g.fill(new Rectangle2D.Double(0.5 * hbar + i * x * (wbar - hbar), 0, x * (wbar - hbar), hbar));
        }
    }

    public static ImageIcon asIcon(Color[] colorbar, int wbar, int hbar) {
        BufferedImage img = new BufferedImage(wbar, hbar, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        Rectangle2D r = new Rectangle2D.Double(0, 0, wbar, hbar);
        g.setColor(Color.WHITE);
        g.fill(r);
        g.translate(0.05f * wbar, 0.1f * hbar);
        draw(g, colorbar, 0.9f * wbar, 0.8f * hbar);
        g.translate(-0.05f * wbar, -0.1f * hbar);
        return new ImageIcon(img);
    }
}
