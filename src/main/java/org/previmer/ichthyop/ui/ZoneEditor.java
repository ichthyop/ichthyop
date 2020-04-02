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

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractCellEditor;
import javax.swing.table.TableCellEditor;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JTable;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.MouseInfo;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.jdesktop.application.ResourceMap;
import org.previmer.ichthyop.Template;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class ZoneEditor extends AbstractCellEditor implements ActionListener, TableCellEditor {

    private ZoneEditorPanel zoneEditor;
    private JDialog dialog;
    protected static final String EDIT = "Edit";
    protected static final String NEW = "New";
    private JTextField textField = new JTextField();
    private JPanel panel;
    private JOptionPane optionPane;
    private JFileChooser fileChooser;
    private String template;

    public ZoneEditor() {
        this(null);
    }

    public ZoneEditor(String template) {

        this.template = template;

        //Set up the dialog that the button brings up.
        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        zoneEditor = new ZoneEditorPanel();
        optionPane = new JOptionPane(zoneEditor,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        dialog = new JDialog(new JFrame(), false);
        dialog.setTitle(getResource().getString("ZoneEditor.title"));
        dialog.setIconImage(getResource().getImageIcon("Application.icon").getImage());
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
                                zoneEditor.save();
                                textField.setText(zoneEditor.getFilename());
                                fireEditingStopped();
                            } else {
                                fireEditingCanceled();
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

    private ResourceMap getResource() {
        return IchthyopApp.getApplication().getContext().getResourceMap();
    }

    private JPanel createEditorUI() {

        JPanel pnl = new JPanel();
        pnl.setLayout(new GridBagLayout());
        textField = new JTextField();
        textField.setEditable(true);
        JButton btnOpen = new JButton(EDIT);
        btnOpen.setToolTipText("Edit an existing zone file");
        //btnOpen.setBorderPainted(false);
        btnOpen.setFont(btnOpen.getFont().deriveFont(Font.PLAIN, 10));
        btnOpen.setActionCommand(EDIT);
        btnOpen.addActionListener(this);
        JButton btnNew = new JButton(NEW);
        btnNew.setToolTipText("Create a new zone file");
        //btnNew.setBorderPainted(false);
        btnNew.setFont(btnOpen.getFont().deriveFont(Font.PLAIN, 10));
        btnNew.setActionCommand(NEW);
        btnNew.addActionListener(this);
        pnl.add(textField, new GridBagConstraints(0, 0, 1, 1, 100, 0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        pnl.add(btnOpen, new GridBagConstraints(1, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        pnl.add(btnNew, new GridBagConstraints(2, 0, 1, 1, 0, 0, GridBagConstraints.EAST, GridBagConstraints.VERTICAL, new Insets(0, 0, 0, 0), 0, 0));
        return pnl;
    }

    /**
     * Handles events from the editor button and from
     * the dialog's OK button.
     */
    public void actionPerformed(ActionEvent e) {

        try {
            String path = IOTools.resolveFile(textField.getText());

            if (e.getActionCommand().equals(EDIT)) {
                fileChooser.setSelectedFile(new File(path));
                int answer = fileChooser.showOpenDialog(panel);
                if (answer == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (!file.isFile()) {
                        String msg = file.getAbsolutePath() + " does not exist. Create it ?";
                        int create = JOptionPane.showConfirmDialog(panel, msg, "Create file ?", JOptionPane.YES_NO_CANCEL_OPTION);
                        switch (create) {
                            case JOptionPane.YES_OPTION:
                                saveAndEditNewFile(file);
                                break;
                            case JOptionPane.NO_OPTION:
                                actionPerformed(new ActionEvent(new JButton(), 0, EDIT));
                                return;
                            case JOptionPane.CANCEL_OPTION:
                                fireEditingStopped();
                                return;
                        }
                    }
                    textField.setText(fileChooser.getSelectedFile().getAbsolutePath());
                    try {
                        zoneEditor.loadZonesFromFile(fileChooser.getSelectedFile());
                        dialog.setVisible(true);
                    } catch (Exception ex) {
                        Logger.getLogger(ZoneEditor.class.getName()).log(Level.SEVERE, null, ex);
                    }
                } else {
                    fireEditingCanceled();
                }
            } else if (e.getActionCommand().equals(NEW)) {
                File f = new File(path);
                if (f.isFile()) {
                    f = f.getParentFile();
                    String filename = f.getAbsolutePath();
                    if (!filename.endsWith(File.separator)) {
                        filename += File.separator + "NewZoneFile.xml";
                    }
                    fileChooser.setSelectedFile(new File(filename));
                } else {
                    fileChooser.setSelectedFile(f);
                }
                int answer = fileChooser.showSaveDialog(panel);
                if (answer == JFileChooser.APPROVE_OPTION) {
                    File file = fileChooser.getSelectedFile();
                    if (file.exists()) {
                        String msg = file.getName() + " already exists. Overwrite it ?";
                        int overwrite = JOptionPane.showConfirmDialog(panel, msg, "Overwrite file ?", JOptionPane.YES_NO_CANCEL_OPTION);
                        switch (overwrite) {
                            case JOptionPane.YES_OPTION:
                                file.delete();
                                break;
                            case JOptionPane.NO_OPTION:
                                actionPerformed(new ActionEvent(new JButton(), 0, NEW));
                                return;
                            case JOptionPane.CANCEL_OPTION:
                                fireEditingStopped();
                                return;
                        }
                    }
                    saveAndEditNewFile(file);
                } else {
                    fireEditingCanceled();
                }
            }
        } catch (Exception ex) {
            SimulationManager.getLogger().log(Level.SEVERE, "Problem for editing or creating zone file ==> " + ex.getMessage(), ex);
        }
    }

    private void saveAndEditNewFile(File file) throws IOException {
        /* create the template */
        if (null != template) {
            Template.createTemplate(template, file);
        }
        /* edit the file */
        textField.setText(file.getAbsolutePath());
        try {
            zoneEditor.loadZonesFromFile(file);
        } catch (Exception ex) {
            Logger.getLogger(ZoneEditor.class.getName()).log(Level.SEVERE, null, ex);
        }
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
