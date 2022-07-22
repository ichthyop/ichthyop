/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Vector;

import javax.swing.JFileChooser;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.event.TableModelListener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import org.previmer.GridType;
import org.previmer.ichthyop.io.GridFile;
import org.previmer.ichthyop.io.XGrid;
import org.previmer.ichthyop.io.XParameter;


/** Class that manages the GUI for the edition of grid parameters.
 *
 * @author barrier
 */
public class GridEditorPanel extends javax.swing.JPanel implements ListSelectionListener, PropertyChangeListener,
ActionListener {
    
    /**
     * Creates new form GridEditorPanel
     */
    public GridEditorPanel() {
        initComponents();
        
        // Adding a listener for when the table has been modified.
        // The grid parameters are modified accordingly, and grid is defined
        // as modified.
        gridParamsTable.getModel().addTableModelListener(new TableModelListener(){
            @Override
            public void tableChanged(TableModelEvent tableModelEvent) {
                if(gridParamsTable.isEditing()) {
                    String value = (String) gridParamsTable.getValueAt(gridParamsTable.getEditingRow(), 1);
                    String key = (String) gridParamsTable.getValueAt(gridParamsTable.getEditingRow(), 0); 
                                     
                    grid.getParameter(key).setValue(value);
                    hasGridChanged = true;
                    gridSaveButton.setEnabled(true);
                }
                //do stuff  with value          
            }
        });
        
        this.setIsEnabled(false);
        
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTextField1 = new javax.swing.JTextField();
        gridSaveButton = new javax.swing.JButton();
        gridSaveAsButton = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        gridListTable = new javax.swing.JTable();
        gridAddButton = new javax.swing.JButton();
        gridRemoveButton = new javax.swing.JButton();
        gridUpButton = new javax.swing.JButton();
        gridDownButton = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        gridEnabledCheckBox = new javax.swing.JCheckBox();
        gridTypeComboBox = new javax.swing.JComboBox<>();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        grid3DCheckBox = new javax.swing.JCheckBox();
        jLabel3 = new javax.swing.JLabel();
        gridCentralLongitudeComboBox = new javax.swing.JComboBox<>();
        jLabel4 = new javax.swing.JLabel();
        gridIdTextField = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        gridFileNameTextField = new javax.swing.JTextField();
        gridFileSelectButton = new javax.swing.JButton();
        gridFileNameLabel = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        gridParamsTable = new javax.swing.JTable();
        gridParamDescriptionLabel = new javax.swing.JLabel();

        jTextField1.setText("jTextField1");

        gridSaveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico32/save.png"))); // NOI18N
        gridSaveButton.setText("Save");

        gridSaveAsButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico32/save-as.png"))); // NOI18N
        gridSaveAsButton.setText("Save as");
        gridSaveAsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridSaveAsButtonActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jPanel1.setName("Grids"); // NOI18N

        gridListTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "Title 1"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        //gridListTable.setName("gridListTable"); // NOI18N
        gridListTable.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        gridListTable.setShowVerticalLines(false);
        gridListTable.getSelectionModel().addListSelectionListener(this);

        gridAddButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico32/list-add.png"))); // NOI18N
        gridAddButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridAddButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridAddButtonActionPerformed(evt);
            }
        });

        gridRemoveButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico32/list-remove.png"))); // NOI18N
        gridRemoveButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridRemoveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridRemoveButtonActionPerformed(evt);
            }
        });

        gridUpButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico32/up.png"))); // NOI18N
        gridUpButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridUpButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridUpButtonActionPerformed(evt);
            }
        });

        gridDownButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico32/down.png"))); // NOI18N
        gridDownButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridDownButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridDownButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gridListTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(33, 33, 33)
                .addComponent(gridAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridUpButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(gridDownButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(21, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(gridListTable, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(gridAddButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gridRemoveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gridUpButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gridDownButton, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        gridEnabledCheckBox.setText("enabled");
        gridEnabledCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridEnabledCheckBoxActionPerformed(evt);
            }
        });

        gridTypeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "NEMO", "ROMS", "MARS", "REGULAR" }));

        jLabel1.setText("Grid Type");

        jLabel2.setText("Use 3D");

        grid3DCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                grid3DCheckBoxActionPerformed(evt);
            }
        });

        jLabel3.setText("Central longitude");

        gridCentralLongitudeComboBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "0", "180" }));

        jLabel4.setText("Grid ID");

        gridIdTextField.setText("Enter ID...");

        jLabel5.setText("Grid file name");

        gridFileNameTextField.setText("Enter or select grid file...");
        gridFileNameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridFileNameTextFieldActionPerformed(evt);
            }
        });

        gridFileSelectButton.setText("...");
        gridFileSelectButton.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        gridFileSelectButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                gridFileSelectButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(gridEnabledCheckBox, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                            .addComponent(jLabel2))
                        .addGap(51, 51, 51)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(grid3DCheckBox)
                            .addComponent(gridTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel3)
                            .addComponent(jLabel4)
                            .addComponent(jLabel5))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(gridCentralLongitudeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(gridFileNameTextField, javax.swing.GroupLayout.DEFAULT_SIZE, 160, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(gridFileSelectButton, javax.swing.GroupLayout.PREFERRED_SIZE, 28, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(gridIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(gridTypeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(grid3DCheckBox))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(gridEnabledCheckBox)
                        .addGap(13, 13, 13)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel2)))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(gridCentralLongitudeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(23, 23, 23)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(gridIdTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(13, 13, 13)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel5)
                    .addComponent(gridFileNameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gridFileSelectButton))
                .addContainerGap(238, Short.MAX_VALUE))
        );

        gridFileNameLabel.setText("Grid Filename");

        jPanel3.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        gridParamsTable.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Param. name", "Param. value"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class, java.lang.Object.class
            };
            boolean[] canEdit = new boolean [] {
                false, true
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        gridParamsTable.setEnabled(false);
        gridParamsTable.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                gridParamsTableMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(gridParamsTable);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 524, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1)
                .addContainerGap())
        );

        gridParamDescriptionLabel.setText("Parameter description");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(gridSaveButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(gridSaveAsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gridFileNameLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(gridParamDescriptionLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(gridSaveAsButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gridFileNameLabel)
                    .addComponent(gridSaveButton, javax.swing.GroupLayout.PREFERRED_SIZE, 33, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gridParamDescriptionLabel))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents
    
    /** Method that is called when the Add button is selected. It opens 
     * a JDialog box, which specifies which model template must be used. */
    private void gridAddButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridAddButtonActionPerformed
        // TODO add your handling code here:
        
        // Create new window to select model type, and recover the
        GridSelectionPanel gridSelectionPanel = new GridSelectionPanel(null, true);
        gridSelectionPanel.setVisible(true);
        String chosenGrid = gridSelectionPanel.getChosenGrid();
        if(chosenGrid == null) {
            return;    
        }
        
        // Choose from where we inser the new grid.
        int index = gridListTable.getSelectedRow();
        if (index < 0) {
            index = gridListTable.getRowCount();
        }
        
        // add newly created grid element to the GridFile map object
        String newKey = String.format("%s-choose-key-%d", chosenGrid, gridListTable.getRowCount());
        gridFile.addGrid(newKey, chosenGrid);
        
        // Insert new row at the index location, and select this row.
        DefaultTableModel model = (DefaultTableModel) gridListTable.getModel();
        model.insertRow(index, new String[] {newKey});
        gridListTable.setRowSelectionInterval(index, index);
                
    }//GEN-LAST:event_gridAddButtonActionPerformed

    /** Action that is called when the remove button is selected. */
    private void gridRemoveButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridRemoveButtonActionPerformed
        // TODO add your handling code here:
                // Choose from where we inser the new grid.
        int index = gridListTable.getSelectedRow();
        int nRows = gridListTable.getRowCount();
        
        // If the index is out of the interval, nothing is done.
        if (index < 0 || index > nRows - 1) {
            return;
        }
        
        DefaultTableModel model = (DefaultTableModel) gridListTable.getModel();
        String key = (String) model.getValueAt(index, 0);
        model.removeRow(index);
        gridListTable.clearSelection();
        gridFile.removeGrid(key);
        
        
    }//GEN-LAST:event_gridRemoveButtonActionPerformed

    private void gridUpButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridUpButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gridUpButtonActionPerformed

    private void gridDownButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridDownButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gridDownButtonActionPerformed

    private void gridSaveAsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridSaveAsButtonActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gridSaveAsButtonActionPerformed

    private void gridEnabledCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridEnabledCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_gridEnabledCheckBoxActionPerformed

    private void grid3DCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_grid3DCheckBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_grid3DCheckBoxActionPerformed

    private void gridFileNameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridFileNameTextFieldActionPerformed
    
    }//GEN-LAST:event_gridFileNameTextFieldActionPerformed

    private void gridFileSelectButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_gridFileSelectButtonActionPerformed
        
        FileFilter ncFilter = new FileNameExtensionFilter("NetCDF Files", "nc", "nc4", "nc3");
        
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(ncFilter);
        chooser.setAcceptAllFileFilterUsed(false);
        
        // optionally set chooser options ...
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            this.gridFileNameTextField.setText(f.getAbsolutePath());
            // read  and/or display the file somehow. ....
        } else {
            // user changed their mind
        }
    }//GEN-LAST:event_gridFileSelectButtonActionPerformed

    /**
     * Action that is taken when the table is edited
     */
    private void gridParamsTableMouseClicked(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_gridParamsTableMouseClicked
        // TODO add your handling code here:
        int row = this.gridParamsTable.rowAtPoint(evt.getPoint());
        int col = gridParamsTable.columnAtPoint(evt.getPoint());
        if ((row < grid.getParameters().size()) && row >= 0 && col >= 0) {
            if (this.gridParamsTable.isCellEditable(row, col)) {
                this.gridParamsTable.editCellAt(row, col);
            }
            
            XParameter param = grid.getParameter((String) this.gridParamsTable.getModel().getValueAt(row, 0));            
            gridParamDescriptionLabel.setText(param.getDescription());
        }

   
    }// GEN-LAST:event_gridParamsTableMouseClicked


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox grid3DCheckBox;
    private javax.swing.JButton gridAddButton;
    private javax.swing.JComboBox<String> gridCentralLongitudeComboBox;
    private javax.swing.JButton gridDownButton;
    private javax.swing.JCheckBox gridEnabledCheckBox;
    private javax.swing.JLabel gridFileNameLabel;
    private javax.swing.JTextField gridFileNameTextField;
    private javax.swing.JButton gridFileSelectButton;
    private javax.swing.JTextField gridIdTextField;
    private javax.swing.JTable gridListTable;
    private javax.swing.JLabel gridParamDescriptionLabel;
    private javax.swing.JTable gridParamsTable;
    private javax.swing.JButton gridRemoveButton;
    private javax.swing.JButton gridSaveAsButton;
    private javax.swing.JButton gridSaveButton;
    private javax.swing.JComboBox<String> gridTypeComboBox;
    private javax.swing.JButton gridUpButton;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
        
    private GridFile gridFile;
    private XGrid grid;
    private boolean hasGridChanged = false;
        
    public void save() {
        this.gridSaveButton.doClick();
    }

    public String getFilename() {
        return gridFileNameLabel.getText();
    }

    /**
     * Loop over all the grids contained in the grids arguments and extracts the
     * keys as an array, to be used as a data model. Called only at the
     * initialisation.
     * 
     */
    private String[][] collection2Array(Collection<XGrid> grids) {
        int nRows = grids.size();
        int nCols = 1;
        String[][] vector = new String[nRows][nCols];
        int cpt = 0;
        for (XGrid xGrid : grids) {
            vector[cpt][0] = xGrid.getKey();
            cpt += 1;
        }
        return vector;
    }

    /**
     * Load the grid file and use it to generate the list of grids to be displayed.
     * This is done only at the initialisation.
     */
    public void loadGridFromFile(File file) throws Exception {
        gridFileNameLabel.setText(file.getAbsolutePath());
        gridFileNameLabel.setToolTipText(gridFileNameLabel.getText());
        gridFile = new GridFile(file);
        DefaultTableModel model = new DefaultTableModel();
        String[] dummyHeader = new String[] {""};
        model.setDataVector(collection2Array(gridFile.getGrids()), dummyHeader);
        this.gridListTable.setModel(model);
        if (this.gridListTable.getRowCount() > 0) {
            this.gridListTable.getSelectionModel().setSelectionInterval(0, 0);
        }
    }

    /** Function that is called when a new grid is selected. */
    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (null != grid && hasGridChanged) {
                updateGrid(grid);
            }
            if (gridListTable.getSelectedRow() >= 0) {
                String key = (String) this.gridListTable.getModel().getValueAt(gridListTable.getSelectedRow(), 0);
                grid = gridFile.getGrid(key);
                displayGrid(grid);
                this.setIsEnabled(true);   
            } else {
                this.setIsEnabled(false);   
            }
        }
    }
    
    /**
     *  Clean the parameter table for the given grid.
     */
    private void cleanParamTable() {
        int nRow = gridParamsTable.getModel().getRowCount();
        int nCol = gridParamsTable.getModel().getColumnCount();
        for(int i = 0; i < nRow; i++) {
            for(int j = 0; j < nCol; j++) {
                gridParamsTable.getModel().setValueAt("", i, j);
            }
        }
    }
    
    /**
     * Render the parameter table given the grid parameters.
     */
    private void renderParamTable() {
        List<XParameter> parameters = grid.getParameters();
        cleanParamTable();
        int nRow = parameters.size();
        for(int i = 0; i < nRow; i++) {
            XParameter param = parameters.get(i);
            param.getKey();
            gridParamsTable.getModel().setValueAt(param.getKey(), i, 0);
            gridParamsTable.getModel().setValueAt(param.getValue(), i, 1);
        }
    }
    
    
    /**
     * If the grid parameters have been changed using the GUI, the grid element is
     * updated based on the values of the GUI
     */
    private void updateGrid(XGrid grid) {
        if (hasGridChanged) {
            grid.setEnabled(gridEnabledCheckBox.isSelected());
            grid.setType((String) gridTypeComboBox.getSelectedItem());
            grid.set3DEnabled(this.grid3DCheckBox.isSelected());
            grid.setCentralLongitude((String) this.gridCentralLongitudeComboBox.getSelectedItem());
            grid.setKey(gridIdTextField.getText());
            grid.setGridMeshFile(gridFileNameTextField.getText());
            this.updateParameters(grid);
        }
    }
    
    
    /** Update the parameters for the given grid object. */
    private void updateParameters(XGrid grid) { 
        int nRow = grid.getParameters().size();
        for(int i = 0; i < nRow; i++) {
            String value = (String)  gridParamsTable.getModel().getValueAt(i, 1);
            XParameter param = grid.getParameters().get(i);
            param.setValue(value);
        }    
    }

    /**
     * Based on the element of the grid, the GUI is displayed accordingly.
     * 
     */
    private void displayGrid(XGrid grid) {
        this.gridEnabledCheckBox.setSelected(grid.isEnabled());
        String type = grid.getType().getName();
        this.gridTypeComboBox.setSelectedItem(type);
        this.grid3DCheckBox.setSelected(grid.is3DEnabled());
        this.gridCentralLongitudeComboBox.setSelectedItem(grid.getCentralLongitude());
        this.gridIdTextField.setText(grid.getKey());
        this.gridFileNameTextField.setText(grid.getsetGridMeshFile());
        addChangeListeners(this, this);
        this.renderParamTable();
    }
    
    /** Enable or disable all the grid parameters */
    private void setIsEnabled(boolean visible) {
        this.gridEnabledCheckBox.setEnabled(visible);
        this.gridTypeComboBox.setEnabled(visible);
        this.grid3DCheckBox.setEnabled(visible);
        this.gridCentralLongitudeComboBox.setEnabled(visible);
        this.gridIdTextField.setEnabled(visible);
        this.gridFileNameTextField.setEnabled(visible);
        if(!visible) { 
            this.cleanParamTable();
        }   
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        // System.out.println(evt.getSource().getClass().getSimpleName() + " " + evt.getPropertyName());
        // String prop = evt.getPropertyName();
        
    }
    
    
    private void addChangeListeners(PropertyChangeListener pl, ActionListener al) {
        
        // set-up property change listeners.
        // each change to the below widgets will lead to a call to propertyChange.
        this.grid3DCheckBox.addPropertyChangeListener(pl);
        
        // add action listeners. each widget below will
        // lead to a call to actionPerformed
        this.gridEnabledCheckBox.addActionListener(al);
        this.gridTypeComboBox.addActionListener(al);
        this.grid3DCheckBox.addActionListener(al);
        this.gridCentralLongitudeComboBox.addActionListener(al);
        this.gridIdTextField.addActionListener(al);
        this.gridFileNameTextField.addActionListener(al);      
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
        hasGridChanged = true;
        gridSaveButton.setEnabled(true);
    }
    
}
