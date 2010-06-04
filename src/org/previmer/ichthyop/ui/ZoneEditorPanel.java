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

/*
 * ZoneEditorPanel.java
 *
 * Created on Apr 8, 2010, 6:41:55 PM
 */
package org.previmer.ichthyop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.ResourceMap;
import org.previmer.ichthyop.TypeZone;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.io.XZone;
import org.previmer.ichthyop.io.XZone.XPoint;
import org.previmer.ichthyop.io.ZoneFile;
import org.previmer.ichthyop.manager.SimulationManager;
import org.previmer.ichthyop.ui.LonLatConverter.LonLatFormat;

/**
 *
 * @author Philippe Verley <philippe dot verley at ird dot fr>
 */
public class ZoneEditorPanel extends javax.swing.JPanel
        implements ListSelectionListener, PropertyChangeListener,
        ActionListener {

    /** Creates new form ZoneEditorPanel */
    public ZoneEditorPanel() {
        initComponents();
    }

    private void addChangeListeners(PropertyChangeListener pl, ActionListener al) {
        ckBoxEnabled.addPropertyChangeListener(pl);
        cbBoxType.addPropertyChangeListener(pl);
        ckBoxBathyMask.addPropertyChangeListener(pl);
        ckBoxThickness.addPropertyChangeListener(pl);
        txtFieldInshore.addPropertyChangeListener(pl);
        txtFieldOffshore.addPropertyChangeListener(pl);
        txtFieldUpperDepth.addPropertyChangeListener(pl);
        txtFieldLowerDepth.addPropertyChangeListener(pl);
        tablePolygon.addPropertyChangeListener(pl);
        btnNewPoint.addPropertyChangeListener(pl);
        btnDeletePoint.addPropertyChangeListener(pl);
        btnUpPoint.addPropertyChangeListener(pl);
        btnDownPoint.addPropertyChangeListener(pl);
        btnNewZone.addPropertyChangeListener(pl);
        btnDeleteZone.addPropertyChangeListener(pl);
        btnColor.addPropertyChangeListener(pl);
        tableZone.addPropertyChangeListener(pl);
        rdBtnDegMinSec.addPropertyChangeListener(pl);
        rdBtnDecimalDeg.addPropertyChangeListener(pl);
        rdBtnDegDecimalMin.addPropertyChangeListener(pl);
        //
        cbBoxType.addActionListener(al);
        ckBoxThickness.addActionListener(al);
        ckBoxBathyMask.addActionListener(al);
        btnNewZone.addActionListener(al);
        btnDeleteZone.addActionListener(al);
        btnUpZone.addActionListener(al);
        btnDownZone.addActionListener(al);
    }

    private void removeChangeListeners(PropertyChangeListener pl, ActionListener al) {
        ckBoxEnabled.removePropertyChangeListener(pl);
        cbBoxType.removePropertyChangeListener(pl);
        ckBoxBathyMask.removePropertyChangeListener(pl);
        ckBoxThickness.removePropertyChangeListener(pl);
        txtFieldInshore.removePropertyChangeListener(pl);
        txtFieldOffshore.removePropertyChangeListener(pl);
        txtFieldUpperDepth.removePropertyChangeListener(pl);
        txtFieldLowerDepth.removePropertyChangeListener(pl);
        tablePolygon.removePropertyChangeListener(pl);
        btnNewPoint.removePropertyChangeListener(pl);
        btnDeletePoint.removePropertyChangeListener(pl);
        btnUpPoint.removePropertyChangeListener(pl);
        btnDownPoint.removePropertyChangeListener(pl);
        btnNewZone.removePropertyChangeListener(pl);
        btnDeleteZone.removePropertyChangeListener(pl);
        btnColor.removePropertyChangeListener(pl);
        tableZone.removePropertyChangeListener(pl);
        rdBtnDegMinSec.removePropertyChangeListener(pl);
        rdBtnDecimalDeg.removePropertyChangeListener(pl);
        rdBtnDegDecimalMin.removePropertyChangeListener(pl);
        //
        cbBoxType.removeActionListener(al);
        ckBoxThickness.removeActionListener(al);
        ckBoxBathyMask.removeActionListener(al);
        btnNewZone.removeActionListener(al);
        btnDeleteZone.removeActionListener(al);
        btnUpZone.removeActionListener(al);
        btnDownZone.removeActionListener(al);
    }

    public void loadZonesFromFile(File file) {
        lblFile.setText(file.getAbsolutePath());
        lblFile.setToolTipText(lblFile.getText());
        zoneFile = new ZoneFile(file);
        DefaultTableModel model = new DefaultTableModel();
        Vector dummyHeader = new Vector();
        dummyHeader.addElement("");
        model.setDataVector(array2Vector(zoneFile.getZones()), dummyHeader);
        tableZone.setModel(model);
        setPanelZoneEnabled(false);
        if (tableZone.getRowCount() > 0) {
            tableZone.getSelectionModel().setSelectionInterval(0, 0);
        }
    }

    private Vector array2Vector(Collection<XZone> zones) {
        Vector vector = new Vector();
        for (XZone xzone : zones) {
            Vector v = new Vector();
            v.addElement(xzone.getKey());
            vector.addElement(v);
        }
        return vector;
    }

    public String getFilename() {
        return lblFile.getText();

    }

    private void displayZone(XZone zone) {
        pnlZone.setBorder(BorderFactory.createTitledBorder(zone.getKey()));
        removeChangeListeners(this, this);
        setPanelZoneEnabled(true);
        ckBoxEnabled.setSelected(zone.isEnabled());
        cbBoxType.setSelectedItem(zone.getTypeZone());
        repaintBtnColor(zone.getColor());
        txtFieldUpperDepth.setValue(zone.getUpperDepth());
        txtFieldLowerDepth.setValue(zone.getLowerDepth());
        txtFieldInshore.setValue(zone.getInshoreLine());
        txtFieldOffshore.setValue(zone.getOffshoreLine());
        ckBoxBathyMask.setSelected(zone.isBathyMaskEnabled());
        ckBoxThickness.setSelected(zone.isThicknessEnabled());
        txtFieldUpperDepth.setEnabled(ckBoxThickness.isSelected());
        txtFieldLowerDepth.setEnabled(ckBoxThickness.isSelected());
        txtFieldInshore.setEnabled(ckBoxBathyMask.isSelected());
        txtFieldOffshore.setEnabled(ckBoxBathyMask.isSelected());

        Vector vector = new Vector();
        for (XPoint point : zone.getPolygon()) {
            Vector v = new Vector();
            v.addElement(point.getLon());
            v.addElement(point.getLat());
            vector.addElement(v);
        }
        DefaultTableModel model = new DefaultTableModel();
        Vector header = new Vector();
        header.addElement(getResourceMap().getString("tableZone.longitude"));
        header.addElement(getResourceMap().getString("tableZone.latitude"));
        model.setDataVector(vector, header);
        tablePolygon.setModel(model);
        LonLatFormat format = LonLatConverter.getFormat(zone.getPolygon().get(0).getLat());
        switch (format) {
            case DegMinSec:
                rdBtnDegMinSec.doClick();
                break;
            case DegDecimalMin:
                rdBtnDegDecimalMin.doClick();
                break;
            case DecimalDeg:
                rdBtnDecimalDeg.doClick();
                break;
        }
        if (format.equals(LonLatFormat.DegMinSec)) {
            rdBtnDegMinSec.doClick();
        } else {
            rdBtnDecimalDeg.doClick();
        }
        setZoneEnabled(zone, ckBoxEnabled.isSelected());
        hasZoneChanged = false;
        addChangeListeners(this, this);
    }

    public void setPanelZoneEnabled(final boolean enabled) {
        ckBoxEnabled.setEnabled(enabled);
        tablePolygon.setEnabled(enabled);
        tablePolygon.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {

            @Override
            public Component getTableCellRendererComponent(JTable table,
                    Object value, boolean isSelected, boolean hasFocus,
                    int row,
                    int column) {
                Component comp = super.getTableCellRendererComponent(table,
                        value, isSelected, hasFocus, row, column);
                if (!enabled) {
                    comp.setForeground(Color.LIGHT_GRAY);
                }
                return comp;
            }
        });
        btnUpPoint.setEnabled(enabled);
        btnDownPoint.setEnabled(enabled);
        btnNewPoint.setEnabled(enabled);
        btnDeletePoint.setEnabled(enabled);
        cbBoxType.setEnabled(enabled);
        btnColor.setEnabled(enabled);
        ckBoxBathyMask.setEnabled(enabled);
        txtFieldInshore.setEnabled(enabled && ckBoxBathyMask.isSelected());
        txtFieldOffshore.setEnabled(enabled && ckBoxBathyMask.isSelected());
        ckBoxThickness.setEnabled(enabled);
        txtFieldUpperDepth.setEnabled(enabled && ckBoxThickness.isSelected());
        txtFieldLowerDepth.setEnabled(enabled && ckBoxThickness.isSelected());
    }

    public void save() {
        btnSave.doClick();
    }

    private void updateZone(XZone zone) {
        zone.setEnabled(ckBoxEnabled.isSelected());
        zone.setColor(btnColor.getBackground());
        zone.setType((TypeZone) cbBoxType.getSelectedItem());
        zone.setBathyMaskEnabled(ckBoxBathyMask.isSelected());
        zone.setInshoreLine(Float.valueOf(txtFieldInshore.getText()));
        zone.setOffshoreLine(Float.valueOf(txtFieldOffshore.getText()));
        zone.setThicknessEnabled(ckBoxThickness.isSelected());
        zone.setUpperDepth(Float.valueOf(txtFieldUpperDepth.getText()));
        zone.setLowerDepth(Float.valueOf(txtFieldLowerDepth.getText()));
        zone.cleanupPolygon();
        for (int i = 0; i < tablePolygon.getRowCount(); i++) {
            zone.addPoint(i, tablePolygon.getModel().getValueAt(i, 0).toString(), tablePolygon.getModel().getValueAt(i, 1).toString());
        }
    }

    public void setZoneEnabled(XZone zone, boolean enabled) {
        setPanelZoneEnabled(enabled);
        ckBoxEnabled.setEnabled(true);
        zone.setEnabled(enabled);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            if (null != zone && hasZoneChanged) {
                updateZone(zone);
            }
            if (tableZone.getSelectedRow() >= 0) {
                String key = (String) tableZone.getModel().getValueAt(tableZone.getSelectedRow(), 0);
                zone = zoneFile.getZone(key);
                displayZone(zone);
            }
        }
    }

    private void repaintBtnColor(Color color) {
        btnColor.setBackground(color);
        btnColor.firePropertyChange("color", 0, 1);
        /*String str = color.toString();
        str = str.substring(str.indexOf("["));
        btnColor.setText("Color " + str);*/
    }

    private File addExtension(File f, String extension) {

        if (!f.isDirectory() && f.getName().endsWith("." + extension)) {
            return f;
        }
        return new File(f.toString() + "." + extension);
    }

    public void propertyChange(PropertyChangeEvent evt) {
        //System.out.println(evt.getSource().getClass().getSimpleName() + " " + evt.getPropertyName());
        String prop = evt.getPropertyName();
        if (prop.matches("enabled")
                || prop.matches("value")
                || prop.matches("tableCellEditor")
                || prop.matches("color")
                || prop.matches("point")
                || prop.matches("format")) {
            hasZoneChanged = true;
            btnSave.setEnabled(true);
        }

        if (evt.getSource().equals(tableZone) && prop.matches("tableCellEditor")) {
            zoneFile.updateKey(zone.getKey(), (String) tableZone.getModel().getValueAt(tableZone.getSelectedRow(), 0));
        }
    }

    public void actionPerformed(ActionEvent e) {
        hasZoneChanged = true;
        btnSave.setEnabled(true);
    }

    public ResourceMap getResourceMap() {
        return org.jdesktop.application.Application.getInstance().getContext().getResourceMap(ZoneEditorPanel.class);
    }

    public Logger getLogger() {
        return SimulationManager.getInstance().getLogger();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        jSplitPane1 = new javax.swing.JSplitPane();
        jScrollPane4 = new javax.swing.JScrollPane();
        pnlTableZone = new javax.swing.JPanel();
        toolBarZone = new javax.swing.JToolBar();
        btnNewZone = new javax.swing.JButton();
        btnDeleteZone = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        btnUpZone = new javax.swing.JButton();
        btnDownZone = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        tableZone = new javax.swing.JTable();
        jScrollPane3 = new javax.swing.JScrollPane();
        jPanel4 = new javax.swing.JPanel();
        pnlZone = new javax.swing.JPanel();
        ckBoxEnabled = new javax.swing.JCheckBox();
        pnlPolygon = new javax.swing.JPanel();
        jToolBar2 = new javax.swing.JToolBar();
        btnNewPoint = new javax.swing.JButton();
        btnDeletePoint = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnUpPoint = new javax.swing.JButton();
        btnDownPoint = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        tablePolygon = new javax.swing.JTable();
        pnlThickness = new javax.swing.JPanel();
        txtFieldUpperDepth = new javax.swing.JFormattedTextField();
        ckBoxThickness = new javax.swing.JCheckBox();
        lblUpperDepth = new javax.swing.JLabel();
        lblLowerDepth = new javax.swing.JLabel();
        txtFieldLowerDepth = new javax.swing.JFormattedTextField();
        pnlBathyMask = new javax.swing.JPanel();
        txtFieldInshore = new javax.swing.JFormattedTextField();
        ckBoxBathyMask = new javax.swing.JCheckBox();
        lblInshore = new javax.swing.JLabel();
        txtFieldOffshore = new javax.swing.JFormattedTextField();
        lblOffshore = new javax.swing.JLabel();
        pnlColor = new javax.swing.JPanel();
        btnColor = new javax.swing.JButton();
        pnlTypeZone = new javax.swing.JPanel();
        cbBoxType = new javax.swing.JComboBox();
        pnlOption = new javax.swing.JPanel();
        lblLonlat = new javax.swing.JLabel();
        rdBtnDecimalDeg = new javax.swing.JRadioButton();
        rdBtnDegMinSec = new javax.swing.JRadioButton();
        rdBtnDegDecimalMin = new javax.swing.JRadioButton();
        btnSave = new javax.swing.JButton();
        btnSaveAs = new javax.swing.JButton();
        lblFile = new javax.swing.JLabel();

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setResizeWeight(0.5);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane4.setName("jScrollPane4"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(ZoneEditorPanel.class);
        pnlTableZone.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlTableZone.border.title"))); // NOI18N
        pnlTableZone.setName("pnlTableZone"); // NOI18N

        toolBarZone.setFloatable(false);
        toolBarZone.setRollover(true);
        toolBarZone.setName("toolBarZone"); // NOI18N

        btnNewZone.setIcon(resourceMap.getIcon("btnNewZone.icon")); // NOI18N
        btnNewZone.setFocusable(false);
        btnNewZone.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNewZone.setName("btnNewZone"); // NOI18N
        btnNewZone.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNewZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewZoneActionPerformed(evt);
            }
        });
        toolBarZone.add(btnNewZone);

        btnDeleteZone.setIcon(resourceMap.getIcon("btnDeleteZone.icon")); // NOI18N
        btnDeleteZone.setFocusable(false);
        btnDeleteZone.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDeleteZone.setName("btnDeleteZone"); // NOI18N
        btnDeleteZone.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDeleteZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeleteZoneActionPerformed(evt);
            }
        });
        toolBarZone.add(btnDeleteZone);

        jSeparator1.setName("jSeparator1"); // NOI18N
        toolBarZone.add(jSeparator1);

        btnUpZone.setIcon(resourceMap.getIcon("btnUpZone.icon")); // NOI18N
        btnUpZone.setFocusable(false);
        btnUpZone.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnUpZone.setName("btnUpZone"); // NOI18N
        btnUpZone.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnUpZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpZoneActionPerformed(evt);
            }
        });
        toolBarZone.add(btnUpZone);

        btnDownZone.setIcon(resourceMap.getIcon("btnDownZone.icon")); // NOI18N
        btnDownZone.setFocusable(false);
        btnDownZone.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDownZone.setName("btnDownZone"); // NOI18N
        btnDownZone.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDownZone.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownZoneActionPerformed(evt);
            }
        });
        toolBarZone.add(btnDownZone);

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        tableZone.setBackground(new java.awt.Color(254, 254, 254));
        tableZone.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null},
                {null},
                {null},
                {null}
            },
            new String [] {
                "Title 1"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.String.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tableZone.setName("tableZone"); // NOI18N
        tableZone.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        tableZone.setShowVerticalLines(false);
        tableZone.getSelectionModel().addListSelectionListener(this);
        jScrollPane1.setViewportView(tableZone);

        javax.swing.GroupLayout pnlTableZoneLayout = new javax.swing.GroupLayout(pnlTableZone);
        pnlTableZone.setLayout(pnlTableZoneLayout);
        pnlTableZoneLayout.setHorizontalGroup(
            pnlTableZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTableZoneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlTableZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE)
                    .addComponent(toolBarZone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 212, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlTableZoneLayout.setVerticalGroup(
            pnlTableZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlTableZoneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 453, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(toolBarZone, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jScrollPane4.setViewportView(pnlTableZone);

        jSplitPane1.setLeftComponent(jScrollPane4);

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        pnlZone.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlZone.border.title"))); // NOI18N
        pnlZone.setName("pnlZone"); // NOI18N

        ckBoxEnabled.setText(resourceMap.getString("ckBoxEnabled.text")); // NOI18N
        ckBoxEnabled.setName("ckBoxEnabled"); // NOI18N
        ckBoxEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckBoxEnabledActionPerformed(evt);
            }
        });

        pnlPolygon.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlPolygon.border.title"))); // NOI18N
        pnlPolygon.setName("pnlPolygon"); // NOI18N

        jToolBar2.setFloatable(false);
        jToolBar2.setRollover(true);
        jToolBar2.setName("jToolBar2"); // NOI18N

        btnNewPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/list-add.png"))); // NOI18N
        btnNewPoint.setFocusable(false);
        btnNewPoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNewPoint.setName("btnNewPoint"); // NOI18N
        btnNewPoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNewPoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNewPointActionPerformed(evt);
            }
        });
        jToolBar2.add(btnNewPoint);

        btnDeletePoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/list-remove.png"))); // NOI18N
        btnDeletePoint.setFocusable(false);
        btnDeletePoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDeletePoint.setName("btnDeletePoint"); // NOI18N
        btnDeletePoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDeletePoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDeletePointActionPerformed(evt);
            }
        });
        jToolBar2.add(btnDeletePoint);

        jSeparator2.setName("jSeparator2"); // NOI18N
        jToolBar2.add(jSeparator2);

        btnUpPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/up.png"))); // NOI18N
        btnUpPoint.setFocusable(false);
        btnUpPoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnUpPoint.setName("btnUpPoint"); // NOI18N
        btnUpPoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnUpPoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnUpPointActionPerformed(evt);
            }
        });
        jToolBar2.add(btnUpPoint);

        btnDownPoint.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/down.png"))); // NOI18N
        btnDownPoint.setFocusable(false);
        btnDownPoint.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnDownPoint.setName("btnDownPoint"); // NOI18N
        btnDownPoint.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnDownPoint.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnDownPointActionPerformed(evt);
            }
        });
        jToolBar2.add(btnDownPoint);

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        tablePolygon.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null},
                {null, null},
                {null, null},
                {null, null}
            },
            new String [] {
                "Longitude", "Latitude"
            }
        ) {
            Class[] types = new Class [] {
                java.lang.Float.class, java.lang.Float.class
            };

            public Class getColumnClass(int columnIndex) {
                return types [columnIndex];
            }
        });
        tablePolygon.setName("tablePolygon"); // NOI18N
        jScrollPane2.setViewportView(tablePolygon);

        javax.swing.GroupLayout pnlPolygonLayout = new javax.swing.GroupLayout(pnlPolygon);
        pnlPolygon.setLayout(pnlPolygonLayout);
        pnlPolygonLayout.setHorizontalGroup(
            pnlPolygonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPolygonLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlPolygonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jToolBar2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 224, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        pnlPolygonLayout.setVerticalGroup(
            pnlPolygonLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlPolygonLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane2, 0, 0, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar2, javax.swing.GroupLayout.PREFERRED_SIZE, 25, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pnlThickness.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlThickness.border.title"))); // NOI18N
        pnlThickness.setName("pnlThickness"); // NOI18N

        txtFieldUpperDepth.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat(""))));
        txtFieldUpperDepth.setName("txtFieldUpperDepth"); // NOI18N

        ckBoxThickness.setText(resourceMap.getString("ckBoxThickness.text")); // NOI18N
        ckBoxThickness.setName("ckBoxThickness"); // NOI18N
        ckBoxThickness.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckBoxThicknessActionPerformed(evt);
            }
        });

        lblUpperDepth.setText(resourceMap.getString("lblUpperDepth.text")); // NOI18N
        lblUpperDepth.setName("lblUpperDepth"); // NOI18N

        lblLowerDepth.setText(resourceMap.getString("lblLowerDepth.text")); // NOI18N
        lblLowerDepth.setName("lblLowerDepth"); // NOI18N

        txtFieldLowerDepth.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat(""))));
        txtFieldLowerDepth.setName("txtFieldLowerDepth"); // NOI18N

        javax.swing.GroupLayout pnlThicknessLayout = new javax.swing.GroupLayout(pnlThickness);
        pnlThickness.setLayout(pnlThicknessLayout);
        pnlThicknessLayout.setHorizontalGroup(
            pnlThicknessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThicknessLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlThicknessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlThicknessLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(pnlThicknessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlThicknessLayout.createSequentialGroup()
                                .addComponent(lblLowerDepth)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFieldLowerDepth, javax.swing.GroupLayout.DEFAULT_SIZE, 175, Short.MAX_VALUE))
                            .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlThicknessLayout.createSequentialGroup()
                                .addComponent(lblUpperDepth)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFieldUpperDepth, javax.swing.GroupLayout.DEFAULT_SIZE, 176, Short.MAX_VALUE))))
                    .addComponent(ckBoxThickness))
                .addContainerGap())
        );
        pnlThicknessLayout.setVerticalGroup(
            pnlThicknessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlThicknessLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ckBoxThickness)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlThicknessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblUpperDepth)
                    .addComponent(txtFieldUpperDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlThicknessLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblLowerDepth)
                    .addComponent(txtFieldLowerDepth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pnlBathyMask.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlBathyMask.border.title"))); // NOI18N
        pnlBathyMask.setName("pnlBathyMask"); // NOI18N

        txtFieldInshore.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.###"))));
        txtFieldInshore.setName("txtFieldInshore"); // NOI18N

        ckBoxBathyMask.setText(resourceMap.getString("ckBoxBathyMask.text")); // NOI18N
        ckBoxBathyMask.setName("ckBoxBathyMask"); // NOI18N
        ckBoxBathyMask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckBoxBathyMaskActionPerformed(evt);
            }
        });

        lblInshore.setText(resourceMap.getString("lblInshore.text")); // NOI18N
        lblInshore.setName("lblInshore"); // NOI18N

        txtFieldOffshore.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.###"))));
        txtFieldOffshore.setName("txtFieldOffshore"); // NOI18N

        lblOffshore.setText(resourceMap.getString("lblOffshore.text")); // NOI18N
        lblOffshore.setName("lblOffshore"); // NOI18N

        javax.swing.GroupLayout pnlBathyMaskLayout = new javax.swing.GroupLayout(pnlBathyMask);
        pnlBathyMask.setLayout(pnlBathyMaskLayout);
        pnlBathyMaskLayout.setHorizontalGroup(
            pnlBathyMaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBathyMaskLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBathyMaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBathyMaskLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addGroup(pnlBathyMaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblInshore)
                            .addComponent(lblOffshore)))
                    .addComponent(ckBoxBathyMask))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBathyMaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(txtFieldOffshore, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE)
                    .addComponent(txtFieldInshore, javax.swing.GroupLayout.DEFAULT_SIZE, 171, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlBathyMaskLayout.setVerticalGroup(
            pnlBathyMaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBathyMaskLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(ckBoxBathyMask)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBathyMaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblInshore)
                    .addComponent(txtFieldInshore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlBathyMaskLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblOffshore)
                    .addComponent(txtFieldOffshore, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        pnlColor.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlColor.border.title"))); // NOI18N
        pnlColor.setName("pnlColor"); // NOI18N

        btnColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/fill-color.png"))); // NOI18N
        btnColor.setText(resourceMap.getString("btnColor.text")); // NOI18N
        btnColor.setName("btnColor"); // NOI18N
        btnColor.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnColorActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlColorLayout = new javax.swing.GroupLayout(pnlColor);
        pnlColor.setLayout(pnlColorLayout);
        pnlColorLayout.setHorizontalGroup(
            pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlColorLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(btnColor)
                .addContainerGap())
        );
        pnlColorLayout.setVerticalGroup(
            pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlColorLayout.createSequentialGroup()
                .addComponent(btnColor)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlTypeZone.setBorder(javax.swing.BorderFactory.createTitledBorder("Type of zone"));
        pnlTypeZone.setName("pnlTypeZone"); // NOI18N

        cbBoxType.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        cbBoxType.setName("cbBoxType"); // NOI18N
        cbBoxType.setModel(new DefaultComboBoxModel(TypeZone.values()));

        javax.swing.GroupLayout pnlTypeZoneLayout = new javax.swing.GroupLayout(pnlTypeZone);
        pnlTypeZone.setLayout(pnlTypeZoneLayout);
        pnlTypeZoneLayout.setHorizontalGroup(
            pnlTypeZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTypeZoneLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(cbBoxType, 0, 217, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlTypeZoneLayout.setVerticalGroup(
            pnlTypeZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlTypeZoneLayout.createSequentialGroup()
                .addComponent(cbBoxType, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(16, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout pnlZoneLayout = new javax.swing.GroupLayout(pnlZone);
        pnlZone.setLayout(pnlZoneLayout);
        pnlZoneLayout.setHorizontalGroup(
            pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlZoneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ckBoxEnabled)
                    .addGroup(pnlZoneLayout.createSequentialGroup()
                        .addComponent(pnlPolygon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlZoneLayout.createSequentialGroup()
                                .addComponent(pnlTypeZone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pnlColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(pnlThickness, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlBathyMask, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
                .addContainerGap())
        );
        pnlZoneLayout.setVerticalGroup(
            pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlZoneLayout.createSequentialGroup()
                .addComponent(ckBoxEnabled)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlPolygon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlZoneLayout.createSequentialGroup()
                        .addGroup(pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(pnlTypeZone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlThickness, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlBathyMask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );

        pnlOption.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlOption.border.title"))); // NOI18N
        pnlOption.setName("pnlOption"); // NOI18N

        lblLonlat.setText("Sow lon / lat");
        lblLonlat.setName("lblLonlat"); // NOI18N

        buttonGroup1.add(rdBtnDecimalDeg);
        rdBtnDecimalDeg.setSelected(true);
        rdBtnDecimalDeg.setText(resourceMap.getString("rdBtnDecimalDeg.text")); // NOI18N
        rdBtnDecimalDeg.setName("rdBtnDecimalDeg"); // NOI18N
        rdBtnDecimalDeg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdBtnDecimalDegActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdBtnDegMinSec);
        rdBtnDegMinSec.setText(resourceMap.getString("rdBtnDegMinSec.text")); // NOI18N
        rdBtnDegMinSec.setName("rdBtnDegMinSec"); // NOI18N
        rdBtnDegMinSec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdBtnDegMinSecActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdBtnDegDecimalMin);
        rdBtnDegDecimalMin.setText(resourceMap.getString("rdBtnDegDecimalMin.text")); // NOI18N
        rdBtnDegDecimalMin.setName("rdBtnDegDecimalMin"); // NOI18N
        rdBtnDegDecimalMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdBtnDegDecimalMinActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout pnlOptionLayout = new javax.swing.GroupLayout(pnlOption);
        pnlOption.setLayout(pnlOptionLayout);
        pnlOptionLayout.setHorizontalGroup(
            pnlOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlOptionLayout.createSequentialGroup()
                        .addGap(12, 12, 12)
                        .addComponent(rdBtnDecimalDeg)
                        .addGap(12, 12, 12)
                        .addComponent(rdBtnDegDecimalMin)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(rdBtnDegMinSec))
                    .addComponent(lblLonlat))
                .addContainerGap(15, Short.MAX_VALUE))
        );
        pnlOptionLayout.setVerticalGroup(
            pnlOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionLayout.createSequentialGroup()
                .addComponent(lblLonlat)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdBtnDecimalDeg)
                    .addComponent(rdBtnDegMinSec)
                    .addComponent(rdBtnDegDecimalMin))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlOption, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlZone, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addComponent(pnlZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlOption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        jScrollPane3.setViewportView(jPanel4);

        jSplitPane1.setRightComponent(jScrollPane3);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/save.png"))); // NOI18N
        btnSave.setText(resourceMap.getString("btnSave.text")); // NOI18N
        btnSave.setEnabled(false);
        btnSave.setName("btnSave"); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnSaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/save-as.png"))); // NOI18N
        btnSaveAs.setText(resourceMap.getString("btnSaveAs.text")); // NOI18N
        btnSaveAs.setName("btnSaveAs"); // NOI18N
        btnSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveAsActionPerformed(evt);
            }
        });

        lblFile.setFont(new java.awt.Font("DejaVu Sans", 0, 12)); // NOI18N
        lblFile.setText(resourceMap.getString("lblFile.text")); // NOI18N
        lblFile.setName("lblFile"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jSplitPane1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 730, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addComponent(btnSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSaveAs)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFile)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 535, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnSave)
                    .addComponent(btnSaveAs)
                    .addComponent(lblFile))
                .addContainerGap(17, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewZoneActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel) tableZone.getModel();
        int index = tableZone.getSelectedRow();
        if (index < 0) {
            index = tableZone.getRowCount();
        }
        String newZone = getResourceMap().getString("btnNewZone.new") + " " + tableZone.getRowCount();
        model.insertRow(index, new String[]{newZone});
        zoneFile.addZone(newZone);
        tableZone.setRowSelectionInterval(index, index);
}//GEN-LAST:event_btnNewZoneActionPerformed

    private void btnDeleteZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeleteZoneActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel) tableZone.getModel();
        int index = tableZone.getSelectedRow();
        if (index < 0) {
            return;
        }
        model.removeRow(index);
        index = Math.max(Math.min(model.getRowCount() - 1, index), 0);
        zoneFile.removeZone(zone.getKey());
        tableZone.setRowSelectionInterval(index, index);
}//GEN-LAST:event_btnDeleteZoneActionPerformed

    private void btnUpZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpZoneActionPerformed
        // TODO add your handling code here:
        tableZone.getSelectionModel().removeListSelectionListener(this);
        int selectedIndex = tableZone.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) tableZone.getModel();
        int newIndex = Math.max(selectedIndex - 1, 0);
        if (selectedIndex < 0 || selectedIndex == newIndex) {
            tableZone.getSelectionModel().addListSelectionListener(this);
            return;
        }
        model.moveRow(selectedIndex, selectedIndex, newIndex);
        tableZone.setRowSelectionInterval(newIndex, newIndex);
        tableZone.getSelectionModel().addListSelectionListener(this);
}//GEN-LAST:event_btnUpZoneActionPerformed

    private void btnDownZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownZoneActionPerformed
        // TODO add your handling code here:
        tableZone.getSelectionModel().removeListSelectionListener(this);
        int selectedIndex = tableZone.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) tableZone.getModel();
        int newIndex = Math.min(selectedIndex + 1, model.getRowCount() - 1);
        if (selectedIndex < 0 || selectedIndex == newIndex) {
            tableZone.getSelectionModel().addListSelectionListener(this);
            return;
        }
        model.moveRow(selectedIndex, selectedIndex, newIndex);
        tableZone.setRowSelectionInterval(newIndex, newIndex);
        tableZone.getSelectionModel().addListSelectionListener(this);
}//GEN-LAST:event_btnDownZoneActionPerformed

    private void btnNewPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewPointActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel) tablePolygon.getModel();
        int index = tablePolygon.getSelectedRow() + 1;
        if (index < 0) {
            index = tablePolygon.getRowCount();
        }
        if (rdBtnDecimalDeg.isSelected()) {
            model.insertRow(index, new Float[]{0.f, 0.f});
        } else {
            model.insertRow(index, new String[]{"0° 0\' 0.0\"", "0° 0\' 0.0\""});
        }
        tablePolygon.setRowSelectionInterval(index, index);
        btnNewPoint.firePropertyChange("point", 0, 1);
    }//GEN-LAST:event_btnNewPointActionPerformed

    private void btnDeletePointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeletePointActionPerformed
        // TODO add your handling code here:
        DefaultTableModel model = (DefaultTableModel) tablePolygon.getModel();
        int index = tablePolygon.getSelectedRow();
        if (index < 0) {
            return;
        }
        model.removeRow(index);
        index = Math.max(Math.min(model.getRowCount() - 1, index), 0);
        tablePolygon.setRowSelectionInterval(index, index);
        btnDeletePoint.firePropertyChange("point", 0, 1);
    }//GEN-LAST:event_btnDeletePointActionPerformed

    private void btnUpPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpPointActionPerformed
        // TODO add your handling code here:
        int selectedIndex = tablePolygon.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) tablePolygon.getModel();
        int newIndex = Math.max(selectedIndex - 1, 0);
        if (selectedIndex < 0 || selectedIndex == newIndex) {
            return;
        }
        model.moveRow(selectedIndex, selectedIndex, newIndex);
        tablePolygon.setRowSelectionInterval(newIndex, newIndex);
        btnUpPoint.firePropertyChange("point", 0, 1);
    }//GEN-LAST:event_btnUpPointActionPerformed

    private void btnDownPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownPointActionPerformed
        // TODO add your handling code here:
        int selectedIndex = tablePolygon.getSelectedRow();
        DefaultTableModel model = (DefaultTableModel) tablePolygon.getModel();
        int newIndex = Math.min(selectedIndex + 1, model.getRowCount() - 1);
        if (selectedIndex < 0 || selectedIndex == newIndex) {
            return;
        }
        model.moveRow(selectedIndex, selectedIndex, newIndex);
        tablePolygon.setRowSelectionInterval(newIndex, newIndex);
        btnDownPoint.firePropertyChange("point", 0, 1);
    }//GEN-LAST:event_btnDownPointActionPerformed

    private void ckBoxEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckBoxEnabledActionPerformed
        // TODO add your handling code here:
        setZoneEnabled(zone, ckBoxEnabled.isSelected());
    }//GEN-LAST:event_ckBoxEnabledActionPerformed

    private void btnColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnColorActionPerformed
        // TODO add your handling code here:
        Color currentColor = btnColor.getBackground();
        Color newColor = JColorChooser.showDialog(btnColor, "", btnColor.getBackground());
        if (null != newColor) {
            repaintBtnColor(newColor);
        } else {
            repaintBtnColor(currentColor);
        }
    }//GEN-LAST:event_btnColorActionPerformed

    private void ckBoxThicknessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckBoxThicknessActionPerformed
        // TODO add your handling code here:
        txtFieldUpperDepth.setEnabled(ckBoxThickness.isSelected());
        txtFieldLowerDepth.setEnabled(ckBoxThickness.isSelected());
    }//GEN-LAST:event_ckBoxThicknessActionPerformed

    private void ckBoxBathyMaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckBoxBathyMaskActionPerformed
        // TODO add your handling code here:
        txtFieldInshore.setEnabled(ckBoxBathyMask.isSelected());
        txtFieldOffshore.setEnabled(ckBoxBathyMask.isSelected());
    }//GEN-LAST:event_ckBoxBathyMaskActionPerformed

    private void rdBtnDecimalDegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdBtnDecimalDegActionPerformed
        // TODO add your handling code here:
        for (int i = 0; i < tablePolygon.getRowCount(); i++) {
            String lon = tablePolygon.getModel().getValueAt(i, 0).toString();
            String lat = tablePolygon.getModel().getValueAt(i, 1).toString();
            tablePolygon.getModel().setValueAt(LonLatConverter.convert(lon, LonLatFormat.DecimalDeg), i, 0);
            tablePolygon.getModel().setValueAt(LonLatConverter.convert(lat, LonLatFormat.DecimalDeg), i, 1);

        }
        rdBtnDecimalDeg.firePropertyChange("format", 0, 1);
        for (int i = 0; i < tablePolygon.getColumnCount(); i++) {
            tablePolygon.getColumnModel().getColumn(i).setCellEditor(new FloatEditor());
        }
    }//GEN-LAST:event_rdBtnDecimalDegActionPerformed

    private void rdBtnDegMinSecActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdBtnDegMinSecActionPerformed
        // TODO add your handling code here:
        for (int i = 0; i < tablePolygon.getRowCount(); i++) {
            String lon = tablePolygon.getModel().getValueAt(i, 0).toString();
            String lat = tablePolygon.getModel().getValueAt(i, 1).toString();
            tablePolygon.getModel().setValueAt(LonLatConverter.convert(lon, LonLatFormat.DegMinSec), i, 0);
            tablePolygon.getModel().setValueAt(LonLatConverter.convert(lat, LonLatFormat.DegMinSec), i, 1);
        }
        rdBtnDegMinSec.firePropertyChange("format", 0, 1);
        for (int i = 0; i < tablePolygon.getColumnCount(); i++) {
            tablePolygon.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new JFormattedTextField()));
        }
    }//GEN-LAST:event_rdBtnDegMinSecActionPerformed

    private void btnSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveActionPerformed
        try {
            // TODO add your handling code here:
            if (hasZoneChanged) {
                updateZone(zone);
            }
            String[] keys = new String[tableZone.getRowCount()];
            for (int i = 0; i < keys.length; i++) {
                keys[i] = (String) tableZone.getModel().getValueAt(i, 0);
            }
            zoneFile.save(keys);
            hasZoneChanged = false;
            btnSave.setEnabled(false);
        } catch (FileNotFoundException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_btnSaveActionPerformed

    private void btnSaveAsActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSaveAsActionPerformed
        // TODO add your handling code here:
        JFileChooser fc = new JFileChooser(zoneFile.getFile());
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileNameExtensionFilter(getResourceMap().getString("Application.zoneFile"), getResourceMap().getString("Application.zoneFile.extension")));
        fc.setSelectedFile(zoneFile.getFile());
        int returnVal = fc.showSaveDialog(this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File file = addExtension(fc.getSelectedFile(), getResourceMap().getString("Application.zoneFile.extension"));
                IOTools.copyFile(zoneFile.getFile(), file);
                zoneFile.setFile(file);
                btnSave.doClick();
                lblFile.setText(file.toString());
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        }
    }//GEN-LAST:event_btnSaveAsActionPerformed

    private void rdBtnDegDecimalMinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdBtnDegDecimalMinActionPerformed
        // TODO add your handling code here:

        for (int i = 0; i < tablePolygon.getRowCount(); i++) {
            String lon = tablePolygon.getModel().getValueAt(i, 0).toString();
            String lat = tablePolygon.getModel().getValueAt(i, 1).toString();
            tablePolygon.getModel().setValueAt(LonLatConverter.convert(lon, LonLatFormat.DegDecimalMin), i, 0);
            tablePolygon.getModel().setValueAt(LonLatConverter.convert(lat, LonLatFormat.DegDecimalMin), i, 1);

        }
        rdBtnDegDecimalMin.firePropertyChange("format", 0, 1);

        for (int i = 0; i < tablePolygon.getColumnCount(); i++) {
            tablePolygon.getColumnModel().getColumn(i).setCellEditor(new DefaultCellEditor(new JFormattedTextField()));
        }
    }//GEN-LAST:event_rdBtnDegDecimalMinActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnColor;
    private javax.swing.JButton btnDeletePoint;
    private javax.swing.JButton btnDeleteZone;
    private javax.swing.JButton btnDownPoint;
    private javax.swing.JButton btnDownZone;
    private javax.swing.JButton btnNewPoint;
    private javax.swing.JButton btnNewZone;
    private javax.swing.JButton btnSave;
    private javax.swing.JButton btnSaveAs;
    private javax.swing.JButton btnUpPoint;
    private javax.swing.JButton btnUpZone;
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JComboBox cbBoxType;
    private javax.swing.JCheckBox ckBoxBathyMask;
    private javax.swing.JCheckBox ckBoxEnabled;
    private javax.swing.JCheckBox ckBoxThickness;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JScrollPane jScrollPane4;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JToolBar jToolBar2;
    private javax.swing.JLabel lblFile;
    private javax.swing.JLabel lblInshore;
    private javax.swing.JLabel lblLonlat;
    private javax.swing.JLabel lblLowerDepth;
    private javax.swing.JLabel lblOffshore;
    private javax.swing.JLabel lblUpperDepth;
    private javax.swing.JPanel pnlBathyMask;
    private javax.swing.JPanel pnlColor;
    private javax.swing.JPanel pnlOption;
    private javax.swing.JPanel pnlPolygon;
    private javax.swing.JPanel pnlTableZone;
    private javax.swing.JPanel pnlThickness;
    private javax.swing.JPanel pnlTypeZone;
    private javax.swing.JPanel pnlZone;
    private javax.swing.JRadioButton rdBtnDecimalDeg;
    private javax.swing.JRadioButton rdBtnDegDecimalMin;
    private javax.swing.JRadioButton rdBtnDegMinSec;
    private javax.swing.JTable tablePolygon;
    private javax.swing.JTable tableZone;
    private javax.swing.JToolBar toolBarZone;
    private javax.swing.JFormattedTextField txtFieldInshore;
    private javax.swing.JFormattedTextField txtFieldLowerDepth;
    private javax.swing.JFormattedTextField txtFieldOffshore;
    private javax.swing.JFormattedTextField txtFieldUpperDepth;
    // End of variables declaration//GEN-END:variables
    private ZoneFile zoneFile;
    private XZone zone;
    private boolean hasZoneChanged = false;

    public static void main(String[] args) {
        JFrame frame = new JFrame();
        ZoneEditorPanel editor = new ZoneEditorPanel();
        editor.loadZonesFromFile(new File("/home/pverley/ichthyop/dev/nb/iv3/cfg/zone-mars3d.xml"));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.add(editor);
        frame.pack();
        frame.setVisible(true);
    }
}
