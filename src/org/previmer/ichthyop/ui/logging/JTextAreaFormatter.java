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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class JTextAreaFormatter extends Formatter {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        StringBuffer sb = new StringBuffer();
        String thrown = null;
        Calendar cld = Calendar.getInstance();
        cld.setTimeInMillis(record.getMillis());
        sb.append("[");
        sb.append(sdf.format(cld.getTime()));
        sb.append("]$");
        String message = formatMessage(record);
        if (message.trim().isEmpty()) {
            return null;
        }
        if (message.indexOf("\t") > 0) {
            thrown = message.substring(message.indexOf("\t") + 1);
            thrown = thrown.substring(0, thrown.indexOf("\n"));
            message = message.substring(0, message.indexOf("\t") - 1);
        }
        sb.append(record.getLevel().getLocalizedName());
        sb.append(": ");
        sb.append(message);
        if (null != thrown) {
            sb.append("\n");
            sb.append(thrown);
        }
        sb.append("\n");
        return sb.toString();
    }
}
