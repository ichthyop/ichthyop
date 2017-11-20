/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
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
package org.ichthyop.ui;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;
import org.ichthyop.ui.param.UIParameter;
import org.ichthyop.ui.param.UIParameterSubset;
import org.ichthyop.manager.SimulationManager;

/**
 *
 * @author pverley
 */
public class JConfigurationPanel extends JPanel implements TreeSelectionListener, TableModelListener, ListSelectionListener, ActionListener {

    /**
     * Creates new form JConfigurationPanel
     */
    public JConfigurationPanel() {
        initComponents();
        addActionListeners();
    }

    private void addActionListeners() {
        ckBoxNode.addActionListener(this);
        btnRedo.addActionListener(this);
        btnUndo.addActionListener(this);
    }

    @Action
    public void expand() {
        parameterTree.expandAll();
    }

    @Action
    public void collapse() {
        parameterTree.collapseAll();
    }

    private ParameterTable getTable() {
        return (ParameterTable) table;
    }

    private void setupParameterPanel(UIParameterSubset pset) throws Exception {

        if (pset.getType().equals(UIParameterSubset.Type.OPTION)) {
            ckBoxNode.setVisible(false);
        } else {
            ckBoxNode.setVisible(true);
            ckBoxNode.setSelected(pset.isEnabled());
        }

        StringBuilder info = new StringBuilder("<html>");
        info.append("<p>Set of parameters: ");
        info.append(pset.getTreePath());
        info.append("</p><br><p><i>");
        if (null != pset.getDescription()) {
            info.append(pset.getDescription());
        } else {
            info.append(getResourceMap().getString("noDescription.text"));
        }
        info.append("</i></p><br></html>");
        lblInfo.setText(info.toString());

        /* Disabled buttons */
        btnUndo.getAction().setEnabled(false);
        btnRedo.getAction().setEnabled(false);
        getTable().setModel(pset, this);
        setParameterEditorEnabled(pset.getType().equals(UIParameterSubset.Type.OPTION) ? true : pset.isEnabled());
    }

    @Action
    public void setNodeEnabled() {
        parameterTree.getParameterSet().setEnabled(ckBoxNode.isSelected());
        setParameterEditorEnabled(ckBoxNode.isSelected());
        ensureSingleNodeSelection(UIParameterSubset.Type.DATASET);
        ensureSingleNodeSelection(UIParameterSubset.Type.RELEASE);
        firePropertyChange("configurationFile", null, null);
    }

    private void ensureSingleNodeSelection(UIParameterSubset.Type type) {
        UIParameterSubset selectedSet = parameterTree.getParameterSet();
        if (selectedSet.getType().equals(type)) {
            /* If the selected node is newly enable, we
            deactivate all the others nodes with same type */
            if (selectedSet.isEnabled()) {
                for (String key : getSimulationManager().getParameterManager().getParameterSubsets()) {
                    UIParameterSubset otherSet = new UIParameterSubset(key, getSimulationManager().getParameterManager().getParameters(key));
                    if (otherSet.getType().equals(type) && !otherSet.getKey().equals(selectedSet.getKey())) {
                        parameterTree.get(otherSet.getTreePath()).setEnabled(false);
                    }
                }
            } else {
                /* Warn user in case no set of this type is enable */
                StringBuilder msg = new StringBuilder();
                msg.append(getResourceMap().getString("noNodeEnabled.text.part1"));
                msg.append(" <");
                msg.append(type.toString());
                msg.append("> ");
                msg.append(getResourceMap().getString("noNodeEnabled.text.part2"));
                JOptionPane.showMessageDialog(this, msg.toString());
            }
        }
    }

    private void setParameterEditorEnabled(boolean enabled) {
        table.setEnabled(enabled);
        btnUndo.getAction().setEnabled(enabled && getTable().getUndoManager().canUndo());
        btnRedo.getAction().setEnabled(enabled && getTable().getUndoManager().canRedo());
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Application.getInstance().getContext().getTaskService().execute(new ShowConfigEditorsTask());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(() -> {
            getTable().stopEditing();
        });
    }

    private class ShowConfigEditorsTask extends Task {

        ShowConfigEditorsTask() {
            super(Application.getInstance());
            parameterTree.setEnabled(false);
            busyLabel.setText(getResourceMap().getString("ShowConfigEditorsTask.loading.text"));
            busyLabel.setBusy(true);
            pnlParameterSet.setVisible(false);
            pnlEditors.setLayout(new StackLayout());
            pnlEditors.add(busyLabel, StackLayout.TOP);
        }

        @Override
        protected Object doInBackground() throws Exception {

            DefaultMutableTreeNode node = parameterTree.getSelectedNode();
            if (node != null && node.isLeaf()) {
                setupParameterPanel(parameterTree.getParameterSet());
            } else {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void succeeded(Object o) {
            pnlParameterSet.setVisible(true);
            splitPaneCfg.setRightComponent(scrollPaneEditors);
        }

        @Override
        protected void finished() {
            busyLabel.setBusy(false);
            pnlEditors.remove(busyLabel);
            parameterTree.setEnabled(true);
        }

        @Override
        protected void cancelled() {
            splitPaneCfg.setRightComponent(pnlNoNodeSelected);
        }
    }

    @Override
    public void tableChanged(TableModelEvent e) {
        if (e != null) {
            btnRedo.getAction().setEnabled(false);
            btnUndo.getAction().setEnabled(true);
            UIParameter parameter = getTable().getParameter(table.convertRowIndexToModel(e.getFirstRow()));
            firePropertyChange("parameterChanged", null, parameter.getKey());
        }
    }

    @Override
    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            try {
                int viewRow = table.getSelectedRow();
                if (viewRow < 0) {
                    //Selection got filtered away.
                    return;
                }
                UIParameterSubset pset = parameterTree.getParameterSet();
                UIParameter parameter = getTable().getParameter(table.convertRowIndexToModel(viewRow));
                StringBuilder info = new StringBuilder("<html>");
                info.append("<p>Set of parameters: ");
                info.append(pset.getTreePath());
                info.append("</p>");
                if (null != pset.getDescription()) {
                    info.append("<br><p><i>");
                    info.append(pset.getDescription());
                    info.append("</i></p>");
                }
                info.append("<br>");
                info.append("<p>Parameter: ");
                info.append(parameter.getLongName());
                info.append("</p><br><p><i>");
                if (null != parameter.getDescription()) {
                    info.append(parameter.getDescription());
                } else {
                    info.append(getResourceMap().getString("noDescription.text"));
                }
                info.append("</i></p><br></html>");
                lblInfo.setText(info.toString());
            } catch (Exception ex) {
                lblInfo.setText(getResourceMap().getString("noDescription.text"));
                SimulationManager.getInstance().debug(ex);
            }
        }
    }

    private ResourceMap getResourceMap() {
        return Application.getInstance().getContext().getResourceMap(JConfigurationPanel.class);
    }

    @Action
    public void undo() {
        if (getTable().getUndoManager().canUndo()) {
            getTable().getUndoManager().undo();
            btnRedo.getAction().setEnabled(getTable().getUndoManager().canRedo());
            btnUndo.getAction().setEnabled(getTable().getUndoManager().canUndo());
        } else {
            btnUndo.getAction().setEnabled(false);
        }
    }

    @Action
    public void redo() {
        if (getTable().getUndoManager().canRedo()) {
            getTable().getUndoManager().redo();
            btnRedo.getAction().setEnabled(getTable().getUndoManager().canRedo());
            btnUndo.getAction().setEnabled(getTable().getUndoManager().canUndo());
        } else {
            btnRedo.getAction().setEnabled(false);
        }
    }

    public Task loadParameterTree() {
        return new CreateParameterTreeTask(IchthyopApp.getApplication());
    }

    private class CreateParameterTreeTask extends SFTask {

        private String cfgTitle, cfgDescription, cfgVersion;

        CreateParameterTreeTask(Application instance) {
            super(instance);
            parameterTree.setVisible(false);
            textFieldTitle.getDocument().removeDocumentListener(titleDL);
            textAreaDescription.getDocument().removeDocumentListener(descriptionDL);
        }

        @Override
        protected Object doInBackground() throws Exception {
            cfgTitle = getSimulationManager().getParameterManager().getConfigurationTitle();
            cfgDescription = getSimulationManager().getParameterManager().getConfigurationDescription();
            cfgVersion = getSimulationManager().getParameterManager().getConfigurationVersion().toString();
            parameterTree.createModel();
            return null;
        }

        @Override
        void onSuccess(Object result) {
            setVisible(true);
            parameterTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            parameterTree.addTreeSelectionListener(JConfigurationPanel.this);
        }

        @Override
        void onFailure(Throwable throwable) {
            // do nothing
        }

        @Override
        protected void finished() {
            parameterTree.setVisible(true);
            textFieldTitle.setText(cfgTitle);
            if (null != cfgDescription) {
                textAreaDescription.setText(cfgDescription);
            } else {
                textAreaDescription.setText("You can type here a description for this configuration");
            }
            if (null != cfgVersion) {
                textFieldVersion.setText(cfgVersion);
            }
            textFieldTitle.getDocument().addDocumentListener(titleDL);
            textAreaDescription.getDocument().addDocumentListener(descriptionDL);
            splitPaneCfg.setRightComponent(pnlNoNodeSelected);
        }
    }

    private SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {

        parameterEditor = new JPanel();
        pnlNoNodeSelected = new JPanel();
        lblSelectNode = new JLabel();
        jPanel1 = new JPanel();
        lblTitle = new JLabel();
        lblDescription = new JLabel();
        textFieldTitle = new JTextField();
        jScrollPane2 = new JScrollPane();
        textAreaDescription = new JTextArea();
        lblVersion = new JLabel();
        textFieldVersion = new JTextField();
        busyLabel = new JXBusyLabel(new Dimension(100, 100));
        splitPaneCfg = new JSplitPane();
        pnlParameterTree = new JPanel();
        jScrollPane1 = new JScrollPane();
        parameterTree = new org.ichthyop.ui.ParameterTree();
        jToolBar1 = new JToolBar();
        btnExpand = new JButton();
        btnCollapse = new JButton();
        scrollPaneEditors = new JScrollPane();
        pnlEditors = new JPanel();
        pnlParameterSet = new JPanel();
        pnlInfo = new JPanel();
        lblInfo = new JLabel();
        ckBoxNode = new JCheckBox();
        pnlParameters = new JPanel();
        btnUndo = new JButton();
        btnRedo = new JButton();
        scrollPaneTable = new JScrollPane();
        table = new org.ichthyop.ui.ParameterTable();

        parameterEditor.setName("parameterEditor");

        GroupLayout parameterEditorLayout = new GroupLayout(parameterEditor);
        parameterEditor.setLayout(parameterEditorLayout);
        parameterEditorLayout.setHorizontalGroup(
                parameterEditorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 650, Short.MAX_VALUE)
        );
        parameterEditorLayout.setVerticalGroup(
                parameterEditorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGap(0, 574, Short.MAX_VALUE)
        );

        pnlNoNodeSelected.setName("pnlNoNodeSelected");

        lblSelectNode.setFont(new java.awt.Font("DejaVu Sans", 1, 16));
        lblSelectNode.setHorizontalAlignment(SwingConstants.CENTER);
        lblSelectNode.setText("Select a node in the tree.");
        lblSelectNode.setHorizontalTextPosition(SwingConstants.CENTER);
        lblSelectNode.setName("lblSelectNode");

        jPanel1.setBorder(BorderFactory.createTitledBorder("Configuration details"));
        jPanel1.setName("jPanel1");

        lblTitle.setText("Title");
        lblTitle.setName("lblTitle");

        lblDescription.setText("Description");
        lblDescription.setName("lblDescription");

        textFieldTitle.setName("textFieldTitle");

        jScrollPane2.setName("jScrollPane2");

        textAreaDescription.setColumns(20);
        textAreaDescription.setLineWrap(true);
        textAreaDescription.setRows(5);
        textAreaDescription.setWrapStyleWord(true);
        textAreaDescription.setName("textAreaDescription");
        jScrollPane2.setViewportView(textAreaDescription);

        lblVersion.setText("Version");
        lblVersion.setName("lblVersion");

        textFieldVersion.setEditable(false);
        textFieldVersion.setText("undetermined");
        textFieldVersion.setName("textFieldVersion");

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(jScrollPane2, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                                        .addComponent(textFieldTitle, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                                        .addComponent(lblTitle, GroupLayout.Alignment.LEADING)
                                        .addComponent(lblDescription, GroupLayout.Alignment.LEADING)
                                        .addGroup(GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                                                .addComponent(lblVersion)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(textFieldVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(lblTitle)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(textFieldTitle, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(lblDescription)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel1Layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(lblVersion)
                                        .addComponent(textFieldVersion, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                                .addContainerGap())
        );

        GroupLayout pnlNoNodeSelectedLayout = new GroupLayout(pnlNoNodeSelected);
        pnlNoNodeSelected.setLayout(pnlNoNodeSelectedLayout);
        pnlNoNodeSelectedLayout.setHorizontalGroup(
                pnlNoNodeSelectedLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(lblSelectNode, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
                        .addGroup(pnlNoNodeSelectedLayout.createSequentialGroup()
                                .addGap(12, 12, 12)
                                .addComponent(jPanel1, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGap(12, 12, 12))
        );
        pnlNoNodeSelectedLayout.setVerticalGroup(
                pnlNoNodeSelectedLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(pnlNoNodeSelectedLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(lblSelectNode, GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE))
        );

        busyLabel.setText("jXBusyLabel1");
        busyLabel.setName("busyLabel");
        BusyPainter painter = new BusyPainter(
                new RoundRectangle2D.Float(0, 0, 8.5f, 5.6f, 10.0f, 10.0f),
                new Ellipse2D.Float(15.0f, 15.0f, 70.0f, 70.0f));
        painter.setTrailLength(4);
        painter.setPoints(8);
        painter.setFrame(-1);
        busyLabel.setPreferredSize(new Dimension(100, 100));
        busyLabel.setIcon(new EmptyIcon(100, 100));
        busyLabel.setBusyPainter(painter);
        busyLabel.setHorizontalAlignment(JLabel.CENTER);
        busyLabel.setVerticalTextPosition(JLabel.BOTTOM);
        busyLabel.setHorizontalTextPosition(JLabel.CENTER);

        splitPaneCfg.setDividerLocation(250);
        splitPaneCfg.setName("splitPaneCfg");

        pnlParameterTree.setName("pnlParameterTree");

        jScrollPane1.setName("jScrollPane1");

        parameterTree.setName("parameterTree");
        parameterTree.setRootVisible(true);
        jScrollPane1.setViewportView(parameterTree);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1");

        ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(JConfigurationPanel.class, this);
        btnExpand.setAction(actionMap.get("expand"));
        btnExpand.setFocusable(false);
        btnExpand.setHorizontalTextPosition(SwingConstants.CENTER);
        btnExpand.setName("btnExpand");
        btnExpand.setVerticalTextPosition(SwingConstants.BOTTOM);
        jToolBar1.add(btnExpand);

        btnCollapse.setAction(actionMap.get("collapse"));
        btnCollapse.setFocusable(false);
        btnCollapse.setHorizontalTextPosition(SwingConstants.CENTER);
        btnCollapse.setName("btnCollapse");
        btnCollapse.setVerticalTextPosition(SwingConstants.BOTTOM);
        jToolBar1.add(btnCollapse);

        GroupLayout pnlParameterTreeLayout = new GroupLayout(pnlParameterTree);
        pnlParameterTree.setLayout(pnlParameterTreeLayout);
        pnlParameterTreeLayout.setHorizontalGroup(
                pnlParameterTreeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(pnlParameterTreeLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlParameterTreeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                        .addComponent(jToolBar1, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                                        .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
                                .addContainerGap())
        );
        pnlParameterTreeLayout.setVerticalGroup(
                pnlParameterTreeLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, pnlParameterTreeLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jToolBar1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );

        splitPaneCfg.setLeftComponent(pnlParameterTree);

        scrollPaneEditors.setName("scrollPaneEditors");

        pnlEditors.setName("pnlEditors");

        pnlParameterSet.setName("pnlParameterSet");

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(JConfigurationPanel.class);
        pnlInfo.setBorder(BorderFactory.createTitledBorder(resourceMap.getString("pnlInfo.border.title")));
        pnlInfo.setName("pnlInfo");

        lblInfo.setText("Select a node or a parameter to display some help.");
        lblInfo.setName("lblInfo");

        ckBoxNode.setAction(actionMap.get("setNodeEnabled"));
        ckBoxNode.setName("ckBoxNode");

        GroupLayout pnlInfoLayout = new GroupLayout(pnlInfo);
        pnlInfo.setLayout(pnlInfoLayout);
        pnlInfoLayout.setHorizontalGroup(
                pnlInfoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(pnlInfoLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(lblInfo, GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE)
                                .addContainerGap())
        );
        pnlInfoLayout.setVerticalGroup(
                pnlInfoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, pnlInfoLayout.createSequentialGroup()
                                .addComponent(lblInfo, GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                                .addContainerGap())
        );

        pnlParameters.setBorder(BorderFactory.createTitledBorder(resourceMap.getString("pnlParameters.border.title")));
        pnlParameters.setName("pnlParameters");

        btnUndo.setAction(actionMap.get("undo"));
        btnUndo.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
        btnUndo.setName("btnUndo");

        btnRedo.setAction(actionMap.get("redo"));
        btnRedo.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
        btnRedo.setName("btnRedo");

        scrollPaneTable.setName("scrollPaneTable");

        table.setModel(new DefaultTableModel());
        table.setName("table");
        table.getModel().addTableModelListener(this);
        table.getSelectionModel().addListSelectionListener(this);
        scrollPaneTable.setViewportView(table);

        GroupLayout pnlParametersLayout = new GroupLayout(pnlParameters);
        pnlParameters.setLayout(pnlParametersLayout);
        pnlParametersLayout.setHorizontalGroup(
                pnlParametersLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, pnlParametersLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlParametersLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(scrollPaneTable, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE)
                                        .addGroup(GroupLayout.Alignment.LEADING, pnlParametersLayout.createSequentialGroup()
                                                .addComponent(btnUndo)
                                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(btnRedo)))
                                .addContainerGap())
        );
        pnlParametersLayout.setVerticalGroup(
                pnlParametersLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(pnlParametersLayout.createSequentialGroup()
                                .addComponent(scrollPaneTable, GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(pnlParametersLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnUndo)
                                        .addComponent(btnRedo))
                                .addContainerGap())
        );

        GroupLayout pnlParameterLayout = new GroupLayout(pnlParameterSet);
        pnlParameterSet.setLayout(pnlParameterLayout);
        pnlParameterLayout.setHorizontalGroup(
                pnlParameterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(GroupLayout.Alignment.TRAILING, pnlParameterLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(pnlParameterLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                        .addComponent(pnlInfo, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(ckBoxNode, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(pnlParameters, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addContainerGap())
        );
        pnlParameterLayout.setVerticalGroup(
                pnlParameterLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addGroup(pnlParameterLayout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(ckBoxNode, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pnlParameters, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(pnlInfo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addContainerGap())
        );

        GroupLayout pnlEditorsLayout = new GroupLayout(pnlEditors);
        pnlEditors.setLayout(pnlEditorsLayout);
        pnlEditorsLayout.setHorizontalGroup(
                pnlEditorsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(pnlParameterSet, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlEditorsLayout.setVerticalGroup(
                pnlEditorsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(pnlParameterSet, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        scrollPaneEditors.setViewportView(pnlEditors);

        splitPaneCfg.setRightComponent(scrollPaneEditors);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(splitPaneCfg, GroupLayout.DEFAULT_SIZE, 944, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                        .addComponent(splitPaneCfg, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
        );
    }

    private void updateDescription(String str) {
        getSimulationManager().getParameterManager().setConfigurationDescription(str);
        firePropertyChange("configurationFile", null, null);
    }

    private void updateTitle(String str) {
        getSimulationManager().getParameterManager().setConfigurationTitle(str);
        firePropertyChange("configurationFile", null, null);
    }

    private JPanel parameterEditor;
    private org.ichthyop.ui.ParameterTree parameterTree;
    private JButton btnCollapse;
    private JButton btnExpand;
    private JButton btnRedo;
    private JButton btnUndo;
    private JXBusyLabel busyLabel;
    private JCheckBox ckBoxNode;
    private JPanel jPanel1;
    private JScrollPane jScrollPane1;
    private JScrollPane jScrollPane2;
    private JToolBar jToolBar1;
    private JLabel lblInfo;
    private JLabel lblDescription;
    private JLabel lblSelectNode;
    private JLabel lblTitle;
    private JLabel lblVersion;
    private JPanel pnlParameterSet;
    private JPanel pnlInfo;
    private JPanel pnlParameterTree;
    private JPanel pnlEditors;
    private JPanel pnlNoNodeSelected;
    private JPanel pnlParameters;
    private JScrollPane scrollPaneEditors;
    private JScrollPane scrollPaneTable;
    private JSplitPane splitPaneCfg;
    private JTable table;
    private JTextArea textAreaDescription;
    private JTextField textFieldTitle;
    private JTextField textFieldVersion;

    private final DocumentListener titleDL = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            updateTitle(textFieldTitle.getText());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateTitle(textFieldTitle.getText());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateTitle(textFieldTitle.getText());
        }
    };
    private final DocumentListener descriptionDL = new DocumentListener() {
        @Override
        public void insertUpdate(DocumentEvent e) {
            updateDescription(textAreaDescription.getText());
        }

        @Override
        public void removeUpdate(DocumentEvent e) {
            updateDescription(textAreaDescription.getText());
        }

        @Override
        public void changedUpdate(DocumentEvent e) {
            updateDescription(textAreaDescription.getText());
        }
    };
}
