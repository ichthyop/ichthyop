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
 * This parameter has been specialized to handle decimal format. Numbers are
 * formatted according to the US Locale {@link java.util.Locale}.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see ichthyop.util.param.Parameter the inherited abstract class
 * @see java.util.Locale for details about the Locale.US
 */
public class FloatParameter extends Parameter implements FocusListener {

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
     * Constructs a new FloatParameter.
     *
     * @param title a String, the title of the parameter
     * @param default_value, a float, the parameter default value
     * @param unit a String, the unit of the parameter
     * @param enabled a boolean, true if the parameter should be enabled, false
     * otherwise.
     */
    public FloatParameter(String title, float default_value,
                          String unit, boolean enabled) {

        super.setAttributes(title, unit, enabled);
        txtField = new JFormattedTextField(nbFormat =
                                           NumberFormat.getInstance(Locale.US));
        this.defaultValue = default_value;
        this.valMin = Float.MIN_VALUE;
        this.valMax = Float.MAX_VALUE;
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
        txtField.setValue(value.floatValue());
        txtField.setPreferredSize(new Dimension(50, 20));
        txtField.addFocusListener(this);
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
        if (newValue.floatValue() < valMin.floatValue()
            | newValue.floatValue() > valMax.floatValue()) {
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
        return (value.floatValue() != oldValue.floatValue());
    }

    /**
     * Gets the numerical type of the FloatParameter value : float
     * @return a byte, <code>Parameter.FLOAT</code>
     */
    public byte getType() {
        return Parameter.FLOAT;
    }

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Sets the minimum and maximum number of digits allowed in the fraction
     * and the integer portions of a number.
     *
     * @param minDigits an int, the minimum number of integer digits to be shown
     * @param maxDigits an int, the maximum number of integer digits to be shown
     * @param minFrac an int, the minimum number of fraction digits to be shown
     * @param maxFrac an int, the maximum number of fraction digits to be shown
     */
    public void setFormatPolicy(int minDigits, int maxDigits, int minFrac,
                                int maxFrac) {
        nbFormat.setMinimumIntegerDigits(minDigits);
        nbFormat.setMaximumIntegerDigits(maxDigits);
        nbFormat.setMinimumFractionDigits(minFrac);
        nbFormat.setMaximumFractionDigits(maxFrac);
    }
    //---------- End of class
}
