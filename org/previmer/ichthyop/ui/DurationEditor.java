/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;

/**
 *
 * @author pverley
 */
public class DurationEditor extends DefaultCellEditor {
    
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
    private JFormattedTextField ftf;
    private boolean DEBUG = false;

    public DurationEditor() {
        super(new JFormattedTextField());
        ftf = (JFormattedTextField) getComponent();
        try {
            maskFormatter = new MaskFormatter("#### day(s) ## hour(s) ## minute(s)");
        } catch (ParseException ex) {
            Logger.getLogger(DurationEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        ftf.setFormatterFactory(new DefaultFormatterFactory(maskFormatter));
        ftf.setValue(ONE_DAY);
        ftf.setHorizontalAlignment(JTextField.TRAILING);
        ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);

        //React when the user presses Enter while the editor is
        //active.  (Tab is handled as specified by
        //JFormattedTextField's focusLostBehavior property.)
        ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
        ftf.getActionMap().put("check", new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (!ftf.isEditValid()) { //The text is invalid.
                    if (userSaysRevert()) { //reverted
                        ftf.postActionEvent(); //inform the editor
                    }
                } else {
                    try {              //The text is valid,
                        ftf.commitEdit();     //so use it.
                        ftf.postActionEvent(); //stop editing
                    } catch (java.text.ParseException exc) {
                    }
                }
            }
        });
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

    //Override to invoke setValue on the formatted text field.
    @Override
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected,
            int row, int column) {

        JFormattedTextField txtField =
                (JFormattedTextField) super.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        txtField.setValue(format(Long.valueOf(value.toString())));
        return txtField;
    }

    //Override to ensure that the value remains an Integer.
    @Override
    public Object getCellEditorValue() {
        JFormattedTextField txtField = (JFormattedTextField) getComponent();
        Object o = txtField.getValue();
        if (o instanceof Long) {
            return o;
        } else if (o instanceof Number) {
            return new Long(((Number) o).longValue());
        } else {
            if (DEBUG) {
                System.out.println("getCellEditorValue: o isn't a Number");
            }
            return parse(o.toString());
        }
    }

    //Override to check whether the edit is valid,
    //setting the value if it is and complaining if
    //it isn't.  If it's OK for the editor to go
    //away, we need to invoke the superclass's version
    //of this method so that everything gets cleaned up.
    @Override
    public boolean stopCellEditing() {
        JFormattedTextField txtField = (JFormattedTextField) getComponent();
        if (txtField.isEditValid()) {
            try {
                txtField.commitEdit();
            } catch (java.text.ParseException exc) {
            }

        } else { //text is invalid
            if (!userSaysRevert()) { //user wants to edit
                return false; //don't let the editor go away
            }
        }
        return super.stopCellEditing();
    }

    /**
     * Lets the user know that the text they entered is
     * bad. Returns true if the user elects to revert to
     * the last good value.  Otherwise, returns false,
     * indicating that the user wants to continue editing.
     */
    protected boolean userSaysRevert() {
        Toolkit.getDefaultToolkit().beep();
        ftf.selectAll();
        Object[] options = {"Edit",
            "Revert"};
        int answer = JOptionPane.showOptionDialog(
                SwingUtilities.getWindowAncestor(ftf),
                "The value must be a duration formatted as nb_days/nb_hours:nb_minutes.\n"
                + "You can either continue editing "
                + "or revert to the last valid value.",
                "Invalid Text Entered",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.ERROR_MESSAGE,
                null,
                options,
                options[1]);

        if (answer == 1) { //Revert!
            ftf.setValue(ftf.getValue());
            return true;
        }
        return false;
    }
}
