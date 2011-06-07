/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui.param;

import java.awt.Dimension;
import java.text.NumberFormat;
import java.text.ParseException;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JTextField;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author pverley
 */
public class JDurationTextField extends JFormattedTextField {

///////////////////////////////
// Declaration of the constants
///////////////////////////////
    private static final int ONE_SECOND = 1;
    private static final int ONE_MINUTE = 60 * ONE_SECOND;
    private static final int ONE_HOUR = 60 * ONE_MINUTE;
    private static final long ONE_DAY = 24 * ONE_HOUR;
///////////////////////////////
// Declaration of the variables
///////////////////////////////
    /**
     * Formatter used to edit and format the durations expressed as Strings.
     */
    private MaskFormatter maskFormatter;

///////////////
// Constructors
///////////////
    
    public JDurationTextField() {
        setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        try {
            maskFormatter = new MaskFormatter("#### day(s) ## hour(s) ## minute(s)");
        } catch (ParseException ex) {
        }
        setFormatterFactory(new DefaultFormatterFactory(maskFormatter));
        setValue(ONE_DAY);
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Gets the current value of the parameter
     * @return a Number, the current value of the parameter
     */
    public long getDurationInSeconds() {

        return parse((String) getValue());
    }

    /**
     * Sets the duration as milliseconds
     *
     * @param value a long, the duration as milliseconds
     */
    public void setValue(long value) {

        setValue(format(value));
    }

    /**
     * Formats the duration as milliseconds and produces a String
     *
     * @param time a long, the specified duration as milliseconds
     * @return a String, the formatted duration.
     */
    private String format(long time) {

        StringBuffer duration;
        int nbDays, nbHours, nbMin;
        NumberFormat nbFormat = NumberFormat.getInstance();
        nbFormat.setParseIntegerOnly(true);
        nbFormat.setGroupingUsed(false);
        nbDays = (int) (time / ONE_DAY);
        time -= nbDays * ONE_DAY;
        nbHours = (int) (time / ONE_HOUR);
        time -= nbHours * ONE_HOUR;
        nbMin = (int) (time / ONE_MINUTE);

        nbFormat.setMinimumIntegerDigits(4);
        nbFormat.setMaximumIntegerDigits(4);
        duration = new StringBuffer(nbFormat.format(nbDays));
        nbFormat.setMinimumIntegerDigits(2);
        nbFormat.setMaximumIntegerDigits(2);
        //duration += "/" + nbFormat.format(nbHours) + ":" + nbFormat.format(nbMin);
        duration.append(" day(s) ");
        duration.append(nbFormat.format(nbHours));
        duration.append(" hour(s) ");
        duration.append(nbFormat.format(nbMin));
        duration.append(" minute(s) ");

        return duration.toString();
    }

    /**
     * Parses the given String to produce a duration as milliseconds.
     *
     * @param source the duration as a human readable String
     * @return a long, the duration as milliseconds.
     */
    private long parse(String source) {

        long duration = 0L;
        NumberFormat nbFormat = NumberFormat.getInstance();
        nbFormat.setParseIntegerOnly(true);
        nbFormat.setGroupingUsed(false);
        try {
            duration = nbFormat.parse(source.substring(source.indexOf("hour") + 8, source.indexOf("minute"))).longValue()
                    * ONE_MINUTE
                    + nbFormat.parse(source.substring(source.indexOf("day") + 7,
                    source.indexOf("hour")).trim()).longValue()
                    * ONE_HOUR
                    + nbFormat.parse(source.substring(0, source.indexOf("day")).trim()).longValue()
                    * ONE_DAY;
        } catch (ParseException ex) {
            // Voluntarily ignore the exception
        }
        return duration;
    }
}
