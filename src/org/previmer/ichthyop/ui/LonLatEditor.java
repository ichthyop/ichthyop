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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellEditor;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class LonLatEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

    private JPanel panel;
    private JTextField textField;
    private JPopupMenu popup;
    private JMenuItem menuItemDecimalDeg;
    private JMenuItem menuItemDegDecimalMin;
    private JMenuItem menuItemDegMinSec;
    private JButton btnShowPopup;

    LonLatEditor() {

        panel = createEditorUI();
    }

    private JPanel createEditorUI() {

        /* create popmenu */
        popup = new JPopupMenu();
        menuItemDecimalDeg = new JMenuItem(LonLatFormat.DecimalDeg.getName());
        menuItemDecimalDeg.addActionListener(this);
        menuItemDegDecimalMin = new JMenuItem(LonLatFormat.DegDecimalMin.getName());
        menuItemDegDecimalMin.addActionListener(this);
        menuItemDegMinSec = new JMenuItem(LonLatFormat.DegMinSec.getName());
        menuItemDegMinSec.addActionListener(this);
        popup.add(menuItemDecimalDeg);
        popup.add(menuItemDegDecimalMin);
        popup.add(menuItemDegMinSec);

        /* create panel */
        JPanel pnl = new JPanel();
        pnl.setLayout(new GridBagLayout());
        textField = new JTextField();
        textField.setEditable(true);
        btnShowPopup = new JButton("Format");
        btnShowPopup.setToolTipText("Choose lon/lat format");
        btnShowPopup.setFont(btnShowPopup.getFont().deriveFont(Font.PLAIN, 10));
        btnShowPopup.addActionListener(this);
        pnl.add(textField, new GridBagConstraints(0, 0, 1, 1, 100, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        pnl.add(btnShowPopup, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        return pnl;
    }

    public Object getCellEditorValue() {
        return textField.getText();
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        textField.setText((String) value);
        return panel;
    }

    public void actionPerformed(ActionEvent e) {

        Object src = e.getSource();
        if (src == btnShowPopup) {
            JButton btn = (JButton) e.getSource();
            popup.show(btn, btn.getWidth() - popup.getWidth(), btn.getY() + btn.getHeight());
        } else if (src == menuItemDecimalDeg) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    textField.setText(LonLatConverter.convert(textField.getText(), LonLatConverter.LonLatFormat.DecimalDeg));
                }
            });
        } else if (src == menuItemDegDecimalMin) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    textField.setText(LonLatConverter.convert(textField.getText(), LonLatConverter.LonLatFormat.DegDecimalMin));
                }
            });
        } else if (src == menuItemDegMinSec) {
            SwingUtilities.invokeLater(new Runnable() {

                public void run() {
                    textField.setText(LonLatConverter.convert(textField.getText(), LonLatConverter.LonLatFormat.DegMinSec));
                }
            });
        }
    }
}
