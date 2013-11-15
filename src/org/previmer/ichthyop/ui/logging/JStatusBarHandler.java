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
import java.util.logging.Level;
import java.util.logging.LogRecord;
import org.previmer.ichthyop.ui.JStatusBar;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class JStatusBarHandler extends ConsoleHandler {

    JStatusBar statusBar;

    public JStatusBarHandler(JStatusBar statusBar) {
        this.statusBar = statusBar;
        setOutputStream(new JStatusBarOutputStream(statusBar));
        setFormatter(new JStatusBarFormatter());
    }

    /**
     * Publish a <tt>LogRecord</tt>.
     * <p>
     * The logging request was made initially to a <tt>Logger</tt> object, which
     * initialized the <tt>LogRecord</tt> and forwarded it here.
     * <p>
     * @param record description of the log event. A null record is silently
     * ignored and is not published
     */
    @Override
    public void publish(LogRecord record) {
        Level level = record.getLevel();
        if (level.equals(LogLevel.SEVERE)) {
            statusBar.setIcon(JStatusBar.ICON.ERROR);
        } else if (level.equals(LogLevel.WARNING)) {
            statusBar.setIcon(JStatusBar.ICON.WARNING);
        } else if (level.equals(LogLevel.COMPLETE)) {
            statusBar.setIcon(JStatusBar.ICON.COMPLETE);
        } else if (level.equals(LogLevel.INFO)) {
            statusBar.setIcon(JStatusBar.ICON.STANDBY);
        } else {
            statusBar.setIcon(JStatusBar.ICON.STANDBY);
        }
        super.publish(record);
    }
}
