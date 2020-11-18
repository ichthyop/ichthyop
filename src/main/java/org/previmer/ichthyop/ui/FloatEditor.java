/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 * 
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 * 
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.ui;

import java.awt.event.FocusEvent;
import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.JFormattedTextField;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.event.FocusListener;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author pverley
 */
public class FloatEditor extends DefaultCellEditor {

    /**
     *
     */
    private static final long serialVersionUID = -215131367777648409L;
    JFormattedTextField ftf;
    DecimalFormat floatFormat;
    private Float minimum, maximum;
    private boolean DEBUG = false;

    public FloatEditor() {
        this(-1 * Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public FloatEditor(float min, float max) {
        super(new JFormattedTextField());
        ftf = (JFormattedTextField) getComponent();
        minimum = Float.valueOf(min);
        maximum = Float.valueOf(max);

        //Set up the editor for the integer cells.
        //floatFormat = new DecimalFormat("0.######E0");
        floatFormat = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        floatFormat.applyPattern("0.######E0");
        floatFormat.setGroupingUsed(false);
        NumberFormatter floatFormatter = new NumberFormatter(floatFormat);
        floatFormatter.setFormat(floatFormat);
        floatFormatter.setMinimum(minimum);
        floatFormatter.setMaximum(maximum);


        ftf.setFormatterFactory(
                new DefaultFormatterFactory(floatFormatter));
        //ftf.setValue(minimum);
        ftf.setHorizontalAlignment(JTextField.TRAILING);
        ftf.setFocusLostBehavior(JFormattedTextField.PERSIST);
        ftf.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                SwingUtilities.invokeLater(new Runnable() {

                    public void run() {
                        ftf.selectAll();
                    }
                });
            }

            public void focusLost(FocusEvent e) {
                // do nothing
            }
        });

        //React when the user presses Enter while the editor is
        //active.  (Tab is handled as specified by
        //JFormattedTextField's focusLostBehavior property.)
        ftf.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "check");
        ftf.getActionMap().put("check", new AbstractAction() {

            /**
             *
             */
            private static final long serialVersionUID = -3594349915290571887L;

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

    //Override to invoke setValue on the formatted text field.
    @Override
    public Component getTableCellEditorComponent(JTable table,
            Object value, boolean isSelected,
            int row, int column) {

        JFormattedTextField txtField =
                (JFormattedTextField) super.getTableCellEditorComponent(
                table, value, isSelected, row, column);
        txtField.setValue(Float.valueOf(value.toString()));
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
            return ((Number) o).floatValue();
        } else {
            if (DEBUG) {
                System.out.println("getCellEditorValue: o isn't a Number");
            }
            try {
                return floatFormat.parseObject(o.toString());
            } catch (ParseException exc) {
                System.err.println("getCellEditorValue: can't parse o: " + o);
                return null;
            }
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
                "The value must be a float.\n"
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
