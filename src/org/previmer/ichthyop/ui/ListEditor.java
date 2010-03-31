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

import javax.swing.AbstractCellEditor;
import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JTable;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class ListEditor extends AbstractCellEditor implements ActionListener, TableCellEditor {

    private JListPanel listEditor;
    private JDialog dialog;
    protected static final String EDIT = "edit";
    private JTextField textField = new JTextField();
    private JPanel panel;
    private JOptionPane optionPane;

    public ListEditor() {


        //Set up the dialog that the button brings up.
        listEditor = new JListPanel();
        optionPane = new JOptionPane(listEditor,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        dialog = new JDialog(new JFrame(), false);
        dialog.setLocation(MouseInfo.getPointerInfo().getLocation());
        dialog.setContentPane(optionPane);
        dialog.setDefaultCloseOperation(
                JDialog.DO_NOTHING_ON_CLOSE);
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                optionPane.setValue(JOptionPane.CLOSED_OPTION);

            }
        });
        optionPane.addPropertyChangeListener(
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent e) {
                        String prop = e.getPropertyName();
                        if (dialog.isVisible()
                                && (e.getSource() == optionPane)
                                && (JOptionPane.VALUE_PROPERTY.equals(prop)
                                || JOptionPane.INPUT_VALUE_PROPERTY.equals(prop))) {
                            Object value = optionPane.getValue();
                            if (value == JOptionPane.UNINITIALIZED_VALUE) {
                                //ignore reset
                                return;
                            }
                            //Reset the JOptionPane's value.
                            //If you don't do this, then if the user
                            //presses the same button next time, no
                            //property change event will be fired.
                            optionPane.setValue(JOptionPane.UNINITIALIZED_VALUE);

                            int answer = ((Integer) value).intValue();
                            if (answer == JOptionPane.OK_OPTION) {
                                textField.setText(valuesToString(listEditor.getValues()));
                                fireEditingStopped();
                            }

                            //If you were going to check something
                            //before closing the window, you'd do
                            //it here.
                            dialog.setVisible(false);
                        }
                    }
                });
        dialog.pack();
        panel = createEditorUI();

    }

    private JPanel createEditorUI() {
        JPanel pnl = new JPanel();
        pnl.setLayout(new GridBagLayout());
        textField = new JTextField();
        textField.setEditable(true);
        JButton btn = new JButton("...");
        btn.setBorderPainted(false);
        btn.setFont(btn.getFont().deriveFont(Font.PLAIN, 10));
        btn.setActionCommand(EDIT);
        btn.addActionListener(this);
        pnl.add(textField, new GridBagConstraints(0, 0, 1, 1, 100, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        pnl.add(btn, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        return pnl;
    }

    private Object[] valuesToArray(String valuesAsString) {
        String[] tokens = valuesAsString.split("\"");
        List<String> vals = new ArrayList();
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                vals.add(token.trim());
            }
        }
        return vals.toArray(new String[vals.size()]);
    }

    private String valuesToString(Object[] values) {
        if (values.length == 1) {
            return values[0].toString();
        }
        StringBuffer str = new StringBuffer();
        for (Object o : values) {
            str.append('"');
            str.append(o.toString());
            str.append("\" ");
        }
        return str.toString();
    }

    /**
     * Handles events from the editor button and from
     * the dialog's OK button.
     */
    public void actionPerformed(ActionEvent e) {

        listEditor.setValues(valuesToArray(textField.getText()));
        dialog.setVisible(true);
    }

    public Object getCellEditorValue() {
        return textField.getText();
    }

    //Implement the one method defined by TableCellEditor.
    public Component getTableCellEditorComponent(JTable table,
            Object value,
            boolean isSelected,
            int row,
            int column) {
        textField.setText(value.toString());
        return panel;
    }
}
