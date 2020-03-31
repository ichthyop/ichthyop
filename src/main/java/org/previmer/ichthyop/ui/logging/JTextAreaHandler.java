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

import java.util.logging.ConsoleHandler;
import javax.swing.JTextArea;
import org.previmer.ichthyop.util.IchthyopLogFormatter;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class JTextAreaHandler extends ConsoleHandler {

    public JTextAreaHandler(JTextArea textArea) {
        setOutputStream(new JTextAreaOutputStream(textArea));
        setFormatter(new IchthyopLogFormatter());
    }
}
