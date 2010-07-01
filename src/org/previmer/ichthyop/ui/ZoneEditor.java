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

            if (e.getActionCommand().matches(EDIT)) {
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
            } else if (e.getActionCommand().matches(NEW)) {
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
