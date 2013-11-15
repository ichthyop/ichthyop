/*
 *  Copyright (C) 2010 Philippe Verley <philippe dot verley at ird dot fr>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
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
