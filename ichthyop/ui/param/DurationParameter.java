package ichthyop.ui.param;

/** import java.text */
import java.text.NumberFormat;
import java.text.ParseException;

/** import AWT */
import java.awt.Dimension;
import java.awt.event.FocusListener;

/** import Swing */
import javax.swing.JFormattedTextField;
import javax.swing.text.MaskFormatter;
import javax.swing.event.EventListenerList;

/**
 * This parameter has been specialized to handle duration format. The value of
 * this parameter is a specific duration in time, expressed in milliseconds.
 * The class provides some methods for converting between duration in
 * milliseconds and a human readable format (such as days hours and minutes).
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see ichthyop.util.param.Parameter the inherited abstract class
 */
public class DurationParameter extends Parameter implements FocusListener {

///////////////////////////////
// Declaration of the constants
///////////////////////////////

    private static final int ONE_SECOND = 1000;
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

    /**
     * Constructs a new DurationParameter, pattern "dddd/HH:mm" and
     * default value set to zero.
     *
     * @param title a String, the title of the parameter
     * @param str a String, the default value
     * @param unit a String, the unit of the parameter
     * @param enabled a boolean, true if the parameter should be enabled, false
     * otherwise.
     */

    public DurationParameter(String title, String str,
                             String unit, boolean enabled) {

        super.setAttributes(title, unit, enabled);
        try {
            txtField = new JFormattedTextField(maskFormatter =
                                               new MaskFormatter("####/##:##"));
            str = maskFormatter.valueToString(str);
            defaultValue = parse(str);
        } catch (ParseException ex) {
            //System.out.println("Duration format error");
            defaultValue = 0L;
        }
        txtField.setFocusLostBehavior(JFormattedTextField.COMMIT_OR_REVERT);
        valMin = 0L;
        valMax = 9999L * ONE_DAY + 99L * ONE_HOUR + 99L * ONE_MINUTE;
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
        txtField.setPreferredSize(new Dimension(80, 20));
    }

    /**
     * Gets the current value of the parameter
     * @return a Number, the current value of the parameter
     */
    Number getCurrentValue() {

        Number newValue = parse((String) txtField.getValue());
        if (newValue.longValue() < valMin.longValue()
            | newValue.longValue() > valMax.longValue()) {
            txtField.setValue(format(getValue().longValue()));
            return getValue();
        }
        return newValue;
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
     * Gets the numerical type of the DurationParameter value : long
     * @return a byte, <code>Parameter.LONG</code>
     */
    public byte getType() {
        return Parameter.LONG;
    }



////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Formats the duration as milliseconds and produces a String
     *
     * @param time a long, the specified duration as milliseconds
     * @return a String, the formatted duration.
     */
    private String format(long time) {

        String duration;
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
        duration = nbFormat.format(nbDays);
        nbFormat.setMinimumIntegerDigits(2);
        nbFormat.setMaximumIntegerDigits(2);
        duration += "/" + nbFormat.format(nbHours) + ":" + nbFormat.format(nbMin);

        return (duration);
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
            duration = nbFormat.parse(source.substring(source.indexOf(":") + 1)).
                       longValue() *
                       ONE_MINUTE
                       +
                       nbFormat.parse(source.substring(source.indexOf("/") + 1,
                    source.indexOf(":"))).
                       longValue() *
                       ONE_HOUR
                       +
                       nbFormat.parse(source.substring(0, source.indexOf("/"))).
                       longValue() *
                       ONE_DAY;
        } catch (ParseException ex) {
            // Voluntarily ignore the exception
        }
        return duration;
    }

//////////
// Setters
//////////

    /**
     * Sets the duration as milliseconds
     *
     * @param value a long, the duration as milliseconds
     */
    public void setValue(long value) {

        this.value = value;
        txtField.setValue(format(value));
    }

    //---------- End of class
}
