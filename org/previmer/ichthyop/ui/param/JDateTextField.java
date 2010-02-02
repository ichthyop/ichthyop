/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.ui.param;

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JFormattedTextField;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import org.previmer.ichthyop.calendar.Calendar1900;

/**
 *
 * @author pverley
 */
public class JDateTextField extends JFormattedTextField {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * The simple date format parses and formats dates in human readable format.
     * The pattern for date-time formatting depends on the calendar
     * (Calendar1900 or ClimatoCalendar)
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
        this(new Calendar1900());
    }

    public JDateTextField(Calendar calendar) {
        setCalendar(calendar);
        setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(dtFormat)));
        setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        setValue(0L);
    }

    private Calendar getCalendar() {
        return calendar;
    }

    public void setCalendar(Calendar calendar) {
        long time = calendar.getTimeInMillis();
        this.calendar = calendar;
        dtFormat.setCalendar(calendar);
        setValue(time);
    }

    /**
     * Gets the current value of the parameter
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
