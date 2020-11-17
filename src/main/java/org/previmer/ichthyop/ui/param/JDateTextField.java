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

package org.previmer.ichthyop.ui.param;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JFormattedTextField;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import org.previmer.ichthyop.calendar.InterannualCalendar;

/**
 *
 * @author pverley
 */
public class JDateTextField extends JFormattedTextField {

    ///////////////////////////////
    // Declaration of the variables
    ///////////////////////////////

    /**
    *
    */
    private static final long serialVersionUID = -5905970269251383288L;
    /**
     * The simple date format parses and formats dates in human readable format. The
     * pattern for date-time formatting depends on the calendar (Calendar1900 or
     * ClimatoCalendar)
     */
    private SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
    /**
     * The calendar to convert specific instant in time to date.
     */
    private Calendar calendar;

    ///////////////
    // Constructors
    ///////////////

    public JDateTextField() {
        this(new InterannualCalendar());
    }

    public JDateTextField(Calendar calendar) {
        setCalendar(calendar);
        setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(dtFormat)));
        setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        setValue(0L);
    }

    /*
    private Calendar getCalendar() {
        return calendar;
    }
    */

    public void setCalendar(Calendar calendar) {
        long time = (null != this.calendar) ? getTimeInSeconds() : 0L;
        this.calendar = calendar;
        dtFormat.setCalendar(calendar);
        setValue(time);
    }

    /**
     * Gets the current value of the parameter
     * 
     * @return a long, the current value of the parameter
     */
    public long getTimeInSeconds() {
        return calendar.getTimeInMillis() / 1000L;
    }

    /**
     * Sets the time as milliseconds
     *
     * @param value a long, the time seconds
     */
    public void setValue(long value) {
        calendar.setTimeInMillis(value * 1000L);
        setValue(calendar.getTime());
    }

}
