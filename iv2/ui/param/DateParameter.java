package ichthyop.ui.param;

/** import java.text */
import java.text.SimpleDateFormat;
/** import java.util */
import java.util.Calendar;
/** import AWT */
import java.awt.Dimension;
/** import Swing */
import javax.swing.JFormattedTextField;
import javax.swing.event.EventListenerList;

/**
 * This parameter has been specialized to handle date format. The value of
 * this parameter is a specific instant in time, expressed in milliseconds.
 * A calendar transforms the time as milliseconds to a date displayed in
 * the TextField.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see ichthyop.util.param.Parameter the inherited abstract class
 * @see package ichthyop.util.calendar for details about the Calendar.
 */

public class DateParameter extends Parameter {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * The simple date format parses and formats dates in human readable format.
     * The pattern for date-time formatting depends on the calendar
     * (Calendar1900 or ClimatoCalendar)
     */
    private SimpleDateFormat dtFormat;
    /**
     * The calendar to convert specific instant in time to date.
     */
    private Calendar calendar;

///////////////
// Constructors
///////////////

    /**
     * Constructs a new DateParameter, date format "yyyy/MM/dd HH:mm" and
     * default value set to zero.
     *
     * @param title a String, the title of the parameter
     * @param cld a Calendar, the calendar of the parameter
     * @param unit a String, the unit of the parameter
     * @param enabled a boolean, true if the parameter should be enabled, false
     * otherwise.
     */
    public DateParameter(String title, Calendar cld,
                         String unit, boolean enabled) {

        super.setAttributes(title, unit, enabled);
        this.calendar = cld;
        dtFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        txtField = new JFormattedTextField(dtFormat);
        dtFormat.setCalendar(cld);
        txtField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        this.defaultValue = 0;
        this.valMin = 0L;
        this.valMax = Long.MAX_VALUE;
        listeners = new EventListenerList();
        init();
    }

///////////////////////////////////////////////
// Definition of the inherited abstract methods
///////////////////////////////////////////////

    /**
     * Initiliazes the parameter
     */
    void init() {
        setValue(defaultValue.longValue());
        txtField.addFocusListener(this);
        txtField.setPreferredSize(new Dimension(120, 20));
    }

    /**
     * Gets the current value of the parameter
     * @return a Number, the current value of the parameter
     */
    Number getCurrentValue() {
        return calendar.getTimeInMillis();
    }

    /**
     * Compares the two values given as parameters.
     *
     * @param value a Number, the value of the parameter after typing.
     * @param oldValue Number, the value of the parameter before typing.
     * @return true if value is different from oldValue, false otherwise.
     */
    boolean isNewValue(Number value, Number oldValue) {
        return (value.longValue() != oldValue.longValue());
    }

    /**
     * Gets the numerical type of the DateParameter value : long
     * @return a byte, <code>Parameter.LONG</code>
     */
    public byte getType() {
        return Parameter.LONG;
    }

//////////
// Setters
//////////

    /**
     * Sets the pattern for date-time formatting
     *
     * @param pattern a String, the pattern to apply
     */
    public void setPattern(String pattern) {
        dtFormat.applyPattern(pattern);
    }

    /**
     * Sets the time as milliseconds
     *
     * @param value a long, the time milliseconds
     */
    public void setValue(long value) {

        this.value = value;
        calendar.setTimeInMillis(value);
        txtField.setValue(calendar.getTime());
    }

    /**
     * Sets the specified calendar.
     *
     * @param cld the Calendar to use for converting between a specific
     * instant in time and a set of date fields (year, month, day, hour, etc...)
     */
    public void setCalendar(Calendar cld) {

        this.calendar = cld;
        dtFormat.setCalendar(cld);
        this.unit = dtFormat.toPattern();
    }

    /**
     * Gets the parameter calendar.
     *
     * @return Calendar, the Calendar used for converting between a specific
     * instant in time and a set of date fields (year, month, day, hour, etc...)
     */
    public Calendar getCalendar() {
        return calendar;
    }

    //---------- End of class
}
