/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Nicolas BARRIER, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
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
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
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
