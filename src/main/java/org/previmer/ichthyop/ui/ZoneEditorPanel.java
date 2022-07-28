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

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
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
import java.text.ParseException;
import java.util.Collection;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import org.jdesktop.application.ResourceMap;
import org.previmer.ichthyop.Template;
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

    /**
     *
     */
    private static final long serialVersionUID = -4846396600115277018L;

    /** Creates new form ZoneEditorPanel */
    public ZoneEditorPanel() {
        initComponents();
        tablePolygon.setDefaultEditor(Float.class, new FloatEditor());
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

    public void loadZonesFromFile(File file) throws Exception {
        lblFile.setText(file.getAbsolutePath());
        lblFile.setToolTipText(lblFile.getText());
        zoneFile = new ZoneFile(file);
        DefaultTableModel model = new DefaultTableModel();
        Vector<String> dummyHeader = new Vector<>();
        dummyHeader.addElement("");
        model.setDataVector(array2Vector(zoneFile.getZones()), dummyHeader);
        tableZone.setModel(model);
        setPanelZoneEnabled(false);
        if (tableZone.getRowCount() > 0) {
            tableZone.getSelectionModel().setSelectionInterval(0, 0);
        }
    }

    private Vector<Vector<String>> array2Vector(Collection<XZone> zones) {
        Vector<Vector<String>> vector = new Vector<>();
        for (XZone xzone : zones) {
            Vector<String> v = new Vector<>();
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

        Vector<Vector<String>> vector = new Vector<>();
        for (XPoint point : zone.getPolygon()) {
            Vector<String> v = new Vector<>();
            v.addElement(point.getLon());
            v.addElement(point.getLat());
            vector.addElement(v);
        }
        DefaultTableModel model = new DefaultTableModel();
        Vector<String> header = new Vector<>();
        header.addElement(getResourceMap().getString("tableZone.longitude"));
        header.addElement(getResourceMap().getString("tableZone.latitude"));
        model.setDataVector(vector, header);
        tablePolygon.setModel(model);
        tablePolygon.getColumnModel().getColumn(0).setCellEditor(new StringCellEditor());
        tablePolygon.getColumnModel().getColumn(1).setCellEditor(new StringCellEditor());
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
            default:
                throw new UnsupportedOperationException("Invalid format for lat/lon");
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

            /**
             *
             */
            private static final long serialVersionUID = -4924727441632979305L;

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
        if (prop.equals("enabled")
                || prop.equals("value")
                || prop.equals("tableCellEditor")
                || prop.equals("color")
                || prop.equals("point")
                || prop.equals("format")) {
            hasZoneChanged = true;
            btnSave.setEnabled(true);
        }

        if (evt.getSource().equals(tableZone) && prop.equals("tableCellEditor")) {
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
        return SimulationManager.getLogger();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
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
        labelNParticles = new javax.swing.JLabel();
        textNParticles = new javax.swing.JFormattedTextField();
        pnlOption = new javax.swing.JPanel();
        lblLonlat = new javax.swing.JLabel();
        rdBtnDecimalDeg = new javax.swing.JRadioButton();
        rdBtnDegMinSec = new javax.swing.JRadioButton();
        rdBtnDegDecimalMin = new javax.swing.JRadioButton();
        btnSave = new javax.swing.JButton();
        btnSaveAs = new javax.swing.JButton();
        lblFile = new javax.swing.JLabel();
        btnHelp = new javax.swing.JButton();

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(250);
        jSplitPane1.setName("jSplitPane1"); // NOI18N

        jScrollPane4.setVerticalScrollBarPolicy(javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        jScrollPane4.setName("jScrollPane4"); // NOI18N

        pnlTableZone.setBorder(javax.swing.BorderFactory.createTitledBorder("Zones"));
        pnlTableZone.setName("pnlTableZone"); // NOI18N

        toolBarZone.setFloatable(false);
        toolBarZone.setRollover(true);
        toolBarZone.setName("toolBarZone"); // NOI18N

        btnNewZone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/list-add.png"))); // NOI18N
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

        btnDeleteZone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/list-remove.png"))); // NOI18N
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

        btnUpZone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/up.png"))); // NOI18N
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

        btnDownZone.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/down.png"))); // NOI18N
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
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(toolBarZone, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        jScrollPane3.setAutoscrolls(true);
        jScrollPane3.setName("jScrollPane3"); // NOI18N

        jPanel4.setName("jPanel4"); // NOI18N

        pnlZone.setBorder(javax.swing.BorderFactory.createTitledBorder("Select a zone"));
        pnlZone.setName("pnlZone"); // NOI18N

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/previmer/ichthyop/ui/resources/ZoneEditorPanel"); // NOI18N
        ckBoxEnabled.setText(bundle.getString("ckBoxEnabled.text")); // NOI18N
        ckBoxEnabled.setName("ckBoxEnabled"); // NOI18N
        ckBoxEnabled.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckBoxEnabledActionPerformed(evt);
            }
        });

        pnlPolygon.setBorder(javax.swing.BorderFactory.createTitledBorder("Polygon"));
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

        pnlThickness.setBorder(javax.swing.BorderFactory.createTitledBorder("Thickness"));
        pnlThickness.setName("pnlThickness"); // NOI18N

        txtFieldUpperDepth.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.###"))));
        txtFieldUpperDepth.setName("txtFieldUpperDepth"); // NOI18N

        ckBoxThickness.setText(bundle.getString("ckBoxThickness.text")); // NOI18N
        ckBoxThickness.setName("ckBoxThickness"); // NOI18N
        ckBoxThickness.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckBoxThicknessActionPerformed(evt);
            }
        });

        lblUpperDepth.setText(bundle.getString("lblUpperDepth.text")); // NOI18N
        lblUpperDepth.setToolTipText("");
        lblUpperDepth.setName("lblUpperDepth"); // NOI18N

        lblLowerDepth.setText(bundle.getString("lblLowerDepth.text")); // NOI18N
        lblLowerDepth.setName("lblLowerDepth"); // NOI18N

        txtFieldLowerDepth.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.###"))));
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
                            .addGroup(pnlThicknessLayout.createSequentialGroup()
                                .addComponent(lblLowerDepth)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(txtFieldLowerDepth))
                            .addGroup(pnlThicknessLayout.createSequentialGroup()
                                .addComponent(lblUpperDepth)
                                .addGap(18, 18, 18)
                                .addComponent(txtFieldUpperDepth, javax.swing.GroupLayout.PREFERRED_SIZE, 197, javax.swing.GroupLayout.PREFERRED_SIZE))))
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
                .addContainerGap(53, Short.MAX_VALUE))
        );

        pnlBathyMask.setBorder(javax.swing.BorderFactory.createTitledBorder("Bathymetric mask"));
        pnlBathyMask.setName("pnlBathyMask"); // NOI18N

        txtFieldInshore.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.###"))));
        txtFieldInshore.setName("txtFieldInshore"); // NOI18N

        ckBoxBathyMask.setText(bundle.getString("ckBoxBathyMask.text")); // NOI18N
        ckBoxBathyMask.setName("ckBoxBathyMask"); // NOI18N
        ckBoxBathyMask.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ckBoxBathyMaskActionPerformed(evt);
            }
        });

        lblInshore.setText(bundle.getString("lblInshore.text")); // NOI18N
        lblInshore.setName("lblInshore"); // NOI18N

        txtFieldOffshore.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.###"))));
        txtFieldOffshore.setName("txtFieldOffshore"); // NOI18N

        lblOffshore.setText(bundle.getString("lblOffshore.text")); // NOI18N
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
                    .addComponent(txtFieldOffshore)
                    .addComponent(txtFieldInshore))
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

        pnlColor.setBorder(javax.swing.BorderFactory.createTitledBorder("Color"));
        pnlColor.setName("pnlColor"); // NOI18N

        btnColor.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/fill-color.png"))); // NOI18N
        btnColor.setText(bundle.getString("btnColor.text")); // NOI18N
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
        pnlTypeZone.add(cbBoxType);

        labelNParticles.setText("Number of released particles:");
        labelNParticles.setName("labelNParticles"); // NOI18N

        textNParticles.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("0"))));
        textNParticles.setName("textNParticles"); // NOI18N
        textNParticles.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textNParticlesActionPerformed(evt);
            }
        });
        textNParticles.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                textNParticlesPropertyChange(evt);
            }
        });

        pnlOption.setBorder(javax.swing.BorderFactory.createTitledBorder("Options"));
        pnlOption.setName("pnlOption"); // NOI18N

        lblLonlat.setText("Sow lon / lat");
        lblLonlat.setName("lblLonlat"); // NOI18N

        buttonGroup1.add(rdBtnDecimalDeg);
        rdBtnDecimalDeg.setSelected(true);
        rdBtnDecimalDeg.setText(bundle.getString("rdBtnDecimalDeg.text")); // NOI18N
        rdBtnDecimalDeg.setName("rdBtnDecimalDeg"); // NOI18N
        rdBtnDecimalDeg.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdBtnDecimalDegActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdBtnDegMinSec);
        rdBtnDegMinSec.setText(bundle.getString("rdBtnDegMinSec.text")); // NOI18N
        rdBtnDegMinSec.setName("rdBtnDegMinSec"); // NOI18N
        rdBtnDegMinSec.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                rdBtnDegMinSecActionPerformed(evt);
            }
        });

        buttonGroup1.add(rdBtnDegDecimalMin);
        rdBtnDegDecimalMin.setText(bundle.getString("rdBtnDegDecimalMin.text")); // NOI18N
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        pnlOptionLayout.setVerticalGroup(
            pnlOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlOptionLayout.createSequentialGroup()
                .addComponent(lblLonlat)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlOptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(rdBtnDecimalDeg)
                    .addComponent(rdBtnDegMinSec)
                    .addComponent(rdBtnDegDecimalMin)))
        );

        javax.swing.GroupLayout pnlZoneLayout = new javax.swing.GroupLayout(pnlZone);
        pnlZone.setLayout(pnlZoneLayout);
        pnlZoneLayout.setHorizontalGroup(
            pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlZoneLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ckBoxEnabled)
                    .addComponent(pnlOption, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlZoneLayout.createSequentialGroup()
                        .addComponent(pnlPolygon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(pnlThickness, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(pnlBathyMask, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(pnlZoneLayout.createSequentialGroup()
                                .addComponent(pnlTypeZone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(pnlColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(pnlZoneLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(labelNParticles)
                                .addGap(18, 18, 18)
                                .addComponent(textNParticles, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE)))))
                .addContainerGap())
        );
        pnlZoneLayout.setVerticalGroup(
            pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlZoneLayout.createSequentialGroup()
                .addGroup(pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlZoneLayout.createSequentialGroup()
                        .addComponent(ckBoxEnabled)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlPolygon, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlZoneLayout.createSequentialGroup()
                        .addGroup(pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(pnlColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(pnlTypeZone, javax.swing.GroupLayout.PREFERRED_SIZE, 74, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlZoneLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(labelNParticles)
                            .addComponent(textNParticles, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(11, 11, 11)
                        .addComponent(pnlThickness, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(pnlBathyMask, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(1, 1, 1)
                .addComponent(pnlOption, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlZone, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlZone, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        jScrollPane3.setViewportView(jPanel4);

        jSplitPane1.setRightComponent(jScrollPane3);

        btnSave.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/save.png"))); // NOI18N
        btnSave.setText(bundle.getString("btnSave.text")); // NOI18N
        btnSave.setEnabled(false);
        btnSave.setName("btnSave"); // NOI18N
        btnSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveActionPerformed(evt);
            }
        });

        btnSaveAs.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/save-as.png"))); // NOI18N
        btnSaveAs.setText(bundle.getString("btnSaveAs.text")); // NOI18N
        btnSaveAs.setName("btnSaveAs"); // NOI18N
        btnSaveAs.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnSaveAsActionPerformed(evt);
            }
        });

        lblFile.setFont(new java.awt.Font("DejaVu Sans", 0, 12)); // NOI18N
        lblFile.setName("lblFile"); // NOI18N

        btnHelp.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/previmer/ichthyop/ui/resources/images/ico22/help.png"))); // NOI18N
        btnHelp.setText(bundle.getString("btnHelp.text")); // NOI18N
        btnHelp.setName("btnHelp"); // NOI18N
        btnHelp.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnHelpActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jSplitPane1)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(btnSave)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSaveAs)
                        .addGap(135, 135, 135)
                        .addComponent(lblFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(btnHelp)
                        .addGap(47, 47, 47))))
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
                    .addComponent(lblFile)
                    .addComponent(btnHelp))
                .addContainerGap(22, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void btnNewZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewZoneActionPerformed
        
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
        
        DefaultTableModel model = (DefaultTableModel) tableZone.getModel();
        int index = tableZone.getSelectedRow();
        if (index < 0) {
            return;
        }
        model.removeRow(index);
        index = Math.max(Math.min(model.getRowCount() - 1, index), 0);
        zoneFile.removeZone(zone.getKey());
        if (model.getRowCount() < 1) {
            setPanelZoneEnabled(false);
            return;
        }
        tableZone.setRowSelectionInterval(index, index);
}//GEN-LAST:event_btnDeleteZoneActionPerformed

    private void btnUpZoneActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpZoneActionPerformed
        
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

    private void rdBtnDecimalDegActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_rdBtnDecimalDegActionPerformed
        
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
    private ResourceMap getResource() {
        return IchthyopApp.getApplication().getContext().getResourceMap();
    }

    private void btnHelpActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnHelpActionPerformed

        if (null != dialogHowto && dialogHowto.isVisible()) {
            return;
        }
        JEditorPane editor = new JEditorPane();
        editor.setPreferredSize(new Dimension(600, 600));
        editor.setEditable(false);
        try {
            editor.setPage(Template.getTemplateURL("zone_help.html"));
        } catch (IOException ex) {
            editor.setText("Failed to load help file ==> " + ex.toString());
        }
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(editor);
        final JOptionPane optionPane = new JOptionPane(scrollPane,
                JOptionPane.PLAIN_MESSAGE,
                JOptionPane.OK_CANCEL_OPTION);
        dialogHowto = new JDialog(new JFrame(), false);
        dialogHowto.setTitle(getResource().getString("ZoneEditor.title"));
        dialogHowto.setIconImage(getResource().getImageIcon("Application.icon").getImage());
        dialogHowto.setLocation(MouseInfo.getPointerInfo().getLocation());
        dialogHowto.setContentPane(optionPane);
        dialogHowto.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        dialogHowto.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent we) {
                optionPane.setValue(JOptionPane.CLOSED_OPTION);

            }
        });
        optionPane.addPropertyChangeListener(
                new PropertyChangeListener() {

                    public void propertyChange(PropertyChangeEvent e) {
                        String prop = e.getPropertyName();
                        if (dialogHowto.isVisible()
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

                            //int answer = ((Integer) value).intValue();
                            //If you were going to check something
                            //before closing the window, you'd do
                            //it here.
                            dialogHowto.setVisible(false);
                        }
                    }
                });
        dialogHowto.pack();
        dialogHowto.setVisible(true);
    }//GEN-LAST:event_btnHelpActionPerformed

    private void textNParticlesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textNParticlesActionPerformed
        // TODO add your handling code here:
        try {
            this.textNParticles.commitEdit();
            this.zone.setNParticles(this.textNParticles.getText());
        } catch (ParseException e) { 
            
        }
    }//GEN-LAST:event_textNParticlesActionPerformed

    private void btnColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnColorActionPerformed

        Color currentColor = btnColor.getBackground();
        Color newColor = JColorChooser.showDialog(btnColor, "", btnColor.getBackground());
        if (null != newColor) {
            repaintBtnColor(newColor);
        } else {
            repaintBtnColor(currentColor);
        }
    }//GEN-LAST:event_btnColorActionPerformed

    private void ckBoxBathyMaskActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckBoxBathyMaskActionPerformed

        txtFieldInshore.setEnabled(ckBoxBathyMask.isSelected());
        txtFieldOffshore.setEnabled(ckBoxBathyMask.isSelected());
    }//GEN-LAST:event_ckBoxBathyMaskActionPerformed

    private void ckBoxThicknessActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckBoxThicknessActionPerformed

        txtFieldUpperDepth.setEnabled(ckBoxThickness.isSelected());
        txtFieldLowerDepth.setEnabled(ckBoxThickness.isSelected());
    }//GEN-LAST:event_ckBoxThicknessActionPerformed

    private void btnDownPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDownPointActionPerformed

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

    private void btnUpPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnUpPointActionPerformed

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

    private void btnDeletePointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnDeletePointActionPerformed

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

    private void btnNewPointActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNewPointActionPerformed

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

    private void ckBoxEnabledActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ckBoxEnabledActionPerformed

        setZoneEnabled(zone, ckBoxEnabled.isSelected());
    }//GEN-LAST:event_ckBoxEnabledActionPerformed

    private void textNParticlesPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_textNParticlesPropertyChange
        // TODO add your handling code here:
    }//GEN-LAST:event_textNParticlesPropertyChange

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnColor;
    private javax.swing.JButton btnDeletePoint;
    private javax.swing.JButton btnDeleteZone;
    private javax.swing.JButton btnDownPoint;
    private javax.swing.JButton btnDownZone;
    private javax.swing.JButton btnHelp;
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
    private javax.swing.JLabel labelNParticles;
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
    private javax.swing.JFormattedTextField textNParticles;
    private javax.swing.JToolBar toolBarZone;
    private javax.swing.JFormattedTextField txtFieldInshore;
    private javax.swing.JFormattedTextField txtFieldLowerDepth;
    private javax.swing.JFormattedTextField txtFieldOffshore;
    private javax.swing.JFormattedTextField txtFieldUpperDepth;
    // End of variables declaration//GEN-END:variables
    private ZoneFile zoneFile;
    private XZone zone;
    private boolean hasZoneChanged = false;
    private JDialog dialogHowto;
}
