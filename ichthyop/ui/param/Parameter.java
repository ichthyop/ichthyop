package ichthyop.ui.param;

/** import AWT */
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.FocusListener;
import java.awt.event.FocusEvent;
import java.awt.event.MouseListener;
import java.awt.event.KeyListener;
import java.awt.event.KeyEvent;

/** import Swing */
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

/** local import */
import ichthyop.util.INIFile;

/**
 * This abstract class provides a common structures to all kind of parameters
 * needed in the configuration editor. A parameter can be seen as a value, that
 * can be accessed through a getter and a setter, with an associated UI.
 * The class also implements its own set of listeners to inform the
 * configuration editor the value has changed.
 * It provides a <code>write()</code> method to record the value of the
 * parameter in the configuration file.
 * <p>
 * The UI of the parameter is reduced to the minimum: a title + a textfield for
 * typing the value + a unit
 * </p>
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */
abstract public class Parameter extends JComponent implements FocusListener,
        KeyListener {

//////////////////////////////
// Definition of the constants
//////////////////////////////

    public final static byte INTEGER = 0;
    public final static byte LONG = 1;
    public final static byte FLOAT = 2;

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * Title of the parameter
     */
    private String title;
    /**
     * The textfield in which the value of the parameter is typed.
     */
    public JFormattedTextField txtField;
    /**
     * The unit of the parameter.
     * @see #setUnit
     */
    String unit;
    /**
     * True when the object is enabled. An object that is not
     * enabled does not interact with the user.
     *
     * @see #setEnabled
     */
    private boolean enabled;
    /**
     * JLabel to display the title.
     */
    private JLabel lblTitle;
    /**
     * JLabel  to display the unit.
     */
    private JLabel lblUnit;
    /**
     * List of {@link ichthyop.util.param.ValueListener}
     *
     * @see #addValueListener
     * @see #removeValueListener
     */
    EventListenerList listeners;
    /**
     * Parameter default value
     */
    Number defaultValue;
    /**
     * Parameter current value
     */
    Number value;
    /**
     * Minimum allowed value
     */
    Number valMin;
    /**
     * Maximum allowed value
     */
    Number valMax;

//////////////////////////////////////
// Declaration of the abstract methods
//////////////////////////////////////

    /**
     * Initiliazes the parameter
     */
    abstract void init();

    /**
     * Gets the current value of the parameter, which is the
     * last valid value returned by the formatter of the text component.
     * @return a Number, the current value of the parameter
     */
    abstract Number getCurrentValue();

    /**
     * Compares the two values given as parameters.
     *
     * @param value a Number, the value of the parameter after typing.
     * @param oldValue Number, the value of the parameter before typing.
     * @return true if value is different from oldValue, false otherwise.
     */
    abstract boolean isNewValue(Number value, Number oldValue);

    /**
     * Gets the numerical type of the parameter value. Possible types:
     * <ul>
     * <li>integer
     * <li>long
     * <li>float
     * </ul>
     * @return a byte, the numerical type of the value.
     */
    abstract public byte getType();

////////////////////////////
// Definition of the methods
////////////////////////////

    /**
     * Sets the external attibutes of the parameters.
     *
     * @param title a String, the title of the parameter
     * @param unit a String, the unit of the parameter
     * @param enabled a boolean, true to enable the parameter, false to unable.
     */
    public void setAttributes(String title, String unit, boolean enabled) {
        this.title = title;
        this.unit = unit;
        this.enabled = enabled;
    }

    /**
     * Created the UI of the parameter.
     *
     * @return a JPanel, that contains all the elements of the UI.
     */
    public JPanel createUI() {

        JPanel pnlParam = new JPanel(new GridBagLayout());
        lblTitle = new JLabel(title);
        lblUnit = new JLabel(unit);
        setEnabled(enabled);
        //txtField.setPreferredSize(new Dimension(50, 20));
        txtField.setHorizontalAlignment(JTextField.RIGHT);
        txtField.addKeyListener(this);

        pnlParam.add(lblTitle, new GridBagConstraints(0, 0, 1, 1, 50, 10,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        pnlParam.add(txtField, new GridBagConstraints(1, 0, 1, 1, 30, 10,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));
        pnlParam.add(lblUnit, new GridBagConstraints(2, 0, 1, 1, 20, 10,
                GridBagConstraints.WEST,
                GridBagConstraints.NONE,
                new Insets(0, 5, 0, 5), 0, 0));

        return pnlParam;
    }

    /**
     * Writes the value of the parameter in the configuration file under the
     * specified section and property. The method determines the type of
     * parameter and converts to the apropriate numerical value (integer, long
     * or float).
     *
     * @param file a INIFile, the configuration file
     * @param section a String, the section name of the configuration
     * file where the parameter has to be written down.
     * @param property String, the corresponding name of the parameter in the
     * configuration file.
     */
    public void write(INIFile file, String section, String property) {

        if (isEnabled()) {
            switch (getType()) {
            case Parameter.INTEGER:
                file.setIntegerProperty(section, property,
                                        getValue().intValue(), null);
                break;
            case Parameter.LONG:
                file.setLongProperty(section, property,
                                     getValue().longValue() /
                                     1000L, null);
                break;
            case Parameter.FLOAT:
                file.setDoubleProperty(section, property,
                                       getValue().floatValue(), null);
                break;
            }
        } else {
            file.setStringProperty(section, property, "null", null);
        }

    }

////////////////////
// Getters & Setters
////////////////////

    /**
     * Sets the unit of the parameter.
     * @param unit a String, the unit of the parameter
     */
    public void setUnit(String unit) {
        this.unit = unit;
        lblUnit.setText(unit);
    }

    /**
     * Gets the value of the parameter
     * @return a Number, the value of the parameter.
     */
    public Number getValue() {
        return value;
    }

    /**
     * Sets the value of the parameter and updates the textfield.
     *
     * @param value a Number, the new value of the parameter.
     */
    public void setValue(Number value) {
        this.value = (value != null)
                     ? value
                     : defaultValue;
        txtField.setValue(this.value);
    }

    /**
     * Sets the default value of the parameter.
     * @param default_value a Number, the default value of the parameter
     */
    public void setDefaultValue(Number default_value) {
        this.defaultValue = default_value;
    }

    /**
     * Gets the default value of the parameter.
     * @return a Number, the default value of the parameter
     */
    public Number getDefaultValue() {
        return defaultValue;
    }

    /**
     * Sets the extrema of the parameter.
     *
     * @param valMin a Number, the minimum authorized value
     * @param valMax a Number, the maximum authorized value
     */
    public void setBoundary(Number valMin, Number valMax) {
        this.valMin = valMin;
        this.valMax = valMax;
    }

    public void setEditable(boolean editable) {
        txtField.setEditable(editable);
    }

    /**
     * Sets whether or not the parameter is enabled.
     *
     * @param enabled a boolean, true if the parameter should be enabled,
     * false otherwise
     * @see javax.swing.JComponent#setEnabled
     */
    @Override
            public void setEnabled(boolean enabled) {

        super.setEnabled(enabled);
        txtField.setEnabled(enabled);
        if (!(lblTitle == null)) {
            lblTitle.setEnabled(enabled);
            lblUnit.setEnabled(enabled);
        }
        this.enabled = enabled;
    }

    /**
     * Registers the text to display in a tool tip.
     * The text displays when the cursor lingers over the component.
     *
     * @param text the String to display; if the text is <code>null</code>,
     *              the tool tip is turned off for this component
     * @see javax.swing.JComponent#setToolTipText
     */
    @Override
            public void setToolTipText(String text) {

        lblTitle.setToolTipText(text);
        txtField.setToolTipText(text);
        lblUnit.setToolTipText(text);
    }

////////////////
// Event methods
////////////////

    /**
     * Adds the specified mouse listener to receive mouse events from
     * the parameter.
     *
     * @param l the MouseListener
     * @see java.awt.Componentt#addMouseListener
     */
    @Override
            public void addMouseListener(MouseListener l) {

        lblTitle.addMouseListener(l);
        txtField.addMouseListener(l);
        lblUnit.addMouseListener(l);
    }

    /**
    *  Invoked when the parameter loses the keyboard focus.
    *  It compares the value before typing to the value after typing and
    * notifies the ValueListeners if the value has changed.
    *
    * @param e the FocusEvent
    * @see #fireValueChanged
    */
   public void focusLost(FocusEvent e) {
       this.firePropertyChange("", 0, 0);

       SwingUtilities.invokeLater(new Runnable() {
           public void run() {
               value = getCurrentValue();
           }
       });

   }


    /**
     * Invoked when the parameter gains the keyboard focus.
     * Selects all the text in the <code>TextComponent</code>.
     * @param e FocusEvent
     */
    public void focusGained(FocusEvent e) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                txtField.selectAll();
            }
        });
    }


    /**
     * Adds the specified value listener to receive ValueChanged events from
     * the paremeter.
     *
     * @param listener the ValueListener
     */
    public void addValueListener(ValueListener listener) {
        listeners.add(ValueListener.class, listener);
    }

    /**
     * Removes the specified listener from the parameter
     * @param listener the ValueListener
     */
    public void removeValueListener(ValueListener listener) {
        listeners.remove(ValueListener.class, listener);
    }

    /**
     * Reports to all the value listeners the value of the parameter has
     * changed, throwing a new ValueChanged event.
     *
     * @see ichthyop.util.param.ValueChangedEvent
     * @see ichthyop.util.param.ValueListener
     */
    public void fireValueChanged() {

        ValueListener[] listenerList = (ValueListener[]) listeners.getListeners(
                ValueListener.class);

        for (ValueListener listener : listenerList) {
            listener.valueChanged(new ValueChangedEvent(this));
        }
    }

    /**
     * Invoked when a key has been typed.
     * Notifies the ValueListeners that the text component of the parameter has
     * changed.
     *
     * @param e KeyEvent
     */
    public void keyTyped(KeyEvent e) {

        fireValueChanged();
    }

    public void keyPressed(KeyEvent e) {
    }

    public void keyReleased(KeyEvent e) {
    }

    //---------- End of class
}
