package ichthyop.ui.param;

/** import java.text */
import java.text.NumberFormat;
import java.text.ParseException;

/** import java.util */
import java.util.Locale;

/** import AWT */
import java.awt.Dimension;
import java.awt.event.FocusListener;

/** import Swing */
import javax.swing.JFormattedTextField;
import javax.swing.event.EventListenerList;

/**
 * This parameter has been specialized to handle Integer format. Numbers are
 * formatted according to the US Locale {@link java.util.Locale}.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see ichthyop.util.param.Parameter the inherited abstract class
 * @see java.util.Locale for details about the Locale.US
 */
public class IntegerParameter extends Parameter implements FocusListener {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * NumberFormat for formatting and parsing numbers according to a Locale.
     */
    private NumberFormat nbFormat;

///////////////
// Constructors
///////////////

    /**
     * Constructs a new IntegerParameter.
     *
     * @param title a String, the title of the parameter
     * @param default_value, an int, the parameter default value
     * @param unit a String, the unit of the parameter
     * @param enabled a boolean, true if the parameter should be enabled, false
     * otherwise.
     */
    public IntegerParameter(String title, int default_value,
                            String unit, boolean enabled) {

        super.setAttributes(title, unit, enabled);
        txtField = new JFormattedTextField(nbFormat =
                                           NumberFormat.getInstance(Locale.US));
        nbFormat.setParseIntegerOnly(true);
        nbFormat.setGroupingUsed(false);
        this.defaultValue = default_value;
        this.valMin = 0;
        this.valMax = Integer.MAX_VALUE;
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

        value = defaultValue;
        txtField.setValue(value.intValue());
        txtField.addFocusListener(this);
        txtField.setPreferredSize(new Dimension(50, 20));
    }

    /**
     * Gets the current value of the parameter
     * @return a Number, the current value of the parameter
     */
    Number getCurrentValue() {

        Number newValue;
        try {
            newValue = nbFormat.parse(txtField.getText());
        } catch (ParseException e) {
            newValue = getValue();
        }
        if (newValue.intValue() < valMin.intValue()
            | newValue.intValue() > valMax.intValue()) {
            txtField.setValue(getValue());

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
        return (value.intValue() != oldValue.intValue());
    }

    /**
    * Gets the numerical type of the IntegerParameter value : int
    * @return a byte, <code>Parameter.FLOAT</code>
    */
   public byte getType() {
       return Parameter.INTEGER;
   }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Sets the minimum and maximum number of digits allowed in the
     * integer portion of a number.
     *
     * @param minDigits an int, the minimum number of integer digits to be shown
     * @param maxDigits an int, the maximum number of integer digits to be shown
     */
    public void setFormatPolicy(int minDigits, int maxDigits) {
        nbFormat.setParseIntegerOnly(true);
        nbFormat.setMinimumIntegerDigits(minDigits);
        nbFormat.setMaximumIntegerDigits(maxDigits);
    }
    //---------- End of class
}
