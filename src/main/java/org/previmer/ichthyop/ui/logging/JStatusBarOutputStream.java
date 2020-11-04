/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
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
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.ui.logging;

import java.io.IOException;
import java.io.OutputStream;
import org.previmer.ichthyop.ui.JStatusBar;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class JStatusBarOutputStream extends OutputStream {

    private JStatusBar statusBar = null;

    /**
     * Method JTextAreaOutputStream.
     *
     * @param statusBar le JTextArea qui recevra les caractères.
     */
    public JStatusBarOutputStream(JStatusBar statusBar) {
        this.statusBar = statusBar;
    }

    /**
     * Écrit un caractère dans le JTextArea. Si le caractère est un retour
     * chariot, scrolling.
     *
     * @throws java.io.IOException
     * @see java.io.OutputStream#write(int)
     */
    @Override
    public void write(int b) throws IOException {
        byte[] bytes = new byte[1];
        bytes[0] = (byte) b;
        write(bytes);
    }

    /**
     * Écrit un tableau de bytes dans le JTextArea. Scrolling du JTextArea à la
     * fin du texte ajouté.
     *
     * @param arg0
     * @throws java.io.IOException
     * @see java.io.OutputStream#write(byte[])
     */
    @Override
    public final void write(byte[] arg0) throws IOException {
        StringBuilder txt = new StringBuilder();
        if (!statusBar.getMessage().endsWith("\n")) {
            txt.append(statusBar.getMessage());
        }
        txt.append(new String(arg0));
        statusBar.setMessage(txt.toString());
    }
}
