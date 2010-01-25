/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.ui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.text.DateFormatter;
import javax.swing.text.DefaultFormatterFactory;
import org.previmer.ichthyop.calendar.ClimatoCalendar;

/**
 *
 * @author pverley
 */
public class DateEditor extends DefaultCellEditor {

/**
     * The simple date format parses and formats dates in human readable format.
     * The pattern for date-time formatting depends on the calendar
     * (Calendar1900 or ClimatoCalendar)
     */
    private SimpleDateFormat dtFormat;
    /**
     * The calendar to convert specific instant in time to date.
     */
    private Calendar calendar = new ClimatoCalendar();
    private JFormattedTextField ftf;
    private boolean DEBUG = false;

    public DateEditor() {
        super(new JFormattedTextField());
        ftf = (JFormattedTextField) getComponent();
        dtFormat = new SimpleDateFormat("'year' yyyy 'month' MM 'day' dd 'at' HH:mm");
        dtFormat.setCalendar(getCalendar());
        ftf.setFormatterFactory(new DefaultFormatterFactory(new DateFormatter(dtFormat)));
        ftf.setValue(0);
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

    private Calendar getCalendar() {
        return calendar;
    }

    //Override to invoke setValue on the formatted text field.
    @Override
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected,
            int row, int column) {

        JFormattedTextField txtField =
                (JFormattedTextField) super.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        getCalendar().setTimeInMillis(Long.valueOf(value.toString()) * 1000L);
        txtField.setValue(getCalendar().getTime());
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
            return getCalendar().getTimeInMillis() / 1000L;
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
                "The value must be a date " + dtFormat.toPattern() + "\n"
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
