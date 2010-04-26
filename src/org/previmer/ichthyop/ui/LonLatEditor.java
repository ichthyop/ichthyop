/*
 *  Copyright (C) 2010 Philippe Verley <philippe dot verley at ird dot fr>
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.previmer.ichthyop.ui;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
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
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class LonLatEditor extends DefaultCellEditor {

    private JFormattedTextField ftf;
    private boolean DEBUG = false;
    private MaskFormatter mask;

    public LonLatEditor() {
        super(new JFormattedTextField());
        ftf = (JFormattedTextField) getComponent();
        try {
            mask = new MaskFormatter("###° ##\' ##\"");
        } catch (ParseException ex) {
            Logger.getLogger(LonLatEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
        ftf.setFormatterFactory(new DefaultFormatterFactory(mask));
        ftf.setValue(0.f);
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

    private String decimalToDegMin(float coord) {

        float coordDeg = (float) Math.floor(coord);
        float coordMin = (float) Math.floor((coord - coordDeg) * 60);
        float coordSec = (float) (Math.round((((coord - coordDeg) - (coordMin / 60)) * 60 * 60) * 100) / 100);
        StringBuffer strCoord = new StringBuffer();
        strCoord.append(coordDeg);
        strCoord.append("° ");
        strCoord.append(coordMin);
        strCoord.append("\' ");
        strCoord.append(coordSec);
        strCoord.append('\"');
        return strCoord.toString();
    }

    private Float degMinToDecimal(String coord) {
        float deg = Float.valueOf(coord.substring(0, coord.indexOf('°')));
        float min = Float.valueOf(coord.substring(coord.indexOf('°') + 1, coord.indexOf("\'")));
        float sec = Float.valueOf(coord.substring(coord.indexOf("\'") + 1, coord.indexOf("\"")));
        return deg + (min / 60) + (sec / 60 / 60);
    }

    //Override to invoke setValue on the formatted text field.
    @Override
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected,
            int row, int column) {

        JFormattedTextField txtField =
                (JFormattedTextField) super.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        txtField.setValue(decimalToDegMin(Float.valueOf(value.toString())));
        return txtField;
    }

    //Override to ensure that the value remains an Integer.
    @Override
    public Object getCellEditorValue() {
        JFormattedTextField txtField = (JFormattedTextField) getComponent();
        Object o = txtField.getValue();
        if (o instanceof Float) {
            return o;
        } else if (o instanceof Number) {
            return new Float(((Number) o).floatValue());
        } else {
            if (DEBUG) {
                System.out.println("getCellEditorValue: o isn't a Number");
            }
            return degMinToDecimal(o.toString());
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
                "The value must be a lon / lat coordinate.\n"
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
