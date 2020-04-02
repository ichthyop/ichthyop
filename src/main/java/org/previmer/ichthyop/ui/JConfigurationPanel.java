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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 *
 * @author pverley
 */
public class JConfigurationPanel extends javax.swing.JPanel implements TreeSelectionListener, TableModelListener, ListSelectionListener, ActionListener {

    /** Creates new form JConfigurationPanel */
    public JConfigurationPanel() {
        initComponents();
        addActionListeners();
    }

    private void addActionListeners() {
        btnUpper.addActionListener(this);
        btnLower.addActionListener(this);
        ckBoxBlock.addActionListener(this);
        btnRedo.addActionListener(this);
        btnUndo.addActionListener(this);
        btnAddValue.addActionListener(this);
        btnRemoveValue.addActionListener(this);
        ckBoxHiddenParameter.addActionListener(this);
    }

    @Action
    public void upper() {
        blockTree.removeTreeSelectionListener(this);
        hasStructureChanged |= blockTree.upper();
        blockTree.addTreeSelectionListener(this);
        if (hasStructureChanged) {
            firePropertyChange("configurationFile", null, null);
        }
    }

    @Action
    public void lower() {
        blockTree.removeTreeSelectionListener(this);
        hasStructureChanged |= blockTree.lower();
        blockTree.addTreeSelectionListener(this);
        if (hasStructureChanged) {
            firePropertyChange("configurationFile", null, null);
        }
    }

    @Action
    public void expand() {
        blockTree.expandAll();
    }

    @Action
    public void collapse() {
        blockTree.collapseAll();
    }

    private ParameterTable getTable() {
        return (ParameterTable) table;
    }

    private void setupBlockPanel(XBlock block) {

        /* Display block information */
        String title = getResourceMap().getString("pnlBlockInfo.border.title") + " " + block.getTreePath();
        pnlBlockInfo.setBorder(BorderFactory.createTitledBorder(title));
        if (block.getType().equals(BlockType.OPTION) || !block.canBeDeactivated()) {
            ckBoxBlock.setVisible(false);
        } else {
            ckBoxBlock.setVisible(true);
            ckBoxBlock.setSelected(block.isEnabled());
        }

        StringBuffer info = new StringBuffer("<html><i>");
        if (null != block.getDescription()) {
            info.append(block.getDescription());
        } else {
            info.append(getResourceMap().getString("noDescription.text"));
        }
        info.append("</i></html>");
        lblBlockInfo.setText(info.toString());
        /* Initializes parameter information */
        pnlParameterInfo.setBorder(BorderFactory.createTitledBorder(getResourceMap().getString("pnlParameterInfo.border.title")));
        info = new StringBuffer("<html><i>");
        info.append(getResourceMap().getString("noDescription.text"));
        info.append("</i></html>");
        lblParameter.setText(info.toString());

        /* Disabled buttons */
        btnUndo.getAction().setEnabled(false);
        btnRedo.getAction().setEnabled(false);
        btnAddValue.getAction().setEnabled(false);
        btnRemoveValue.getAction().setEnabled(false);
        ckBoxHiddenParameter.setSelected(false);
        getTable().setModel(block, this);
        if (block.getNbHiddenParameters() > 0) {
            ckBoxHiddenParameter.getAction().setEnabled(true);
        } else {
            ckBoxHiddenParameter.getAction().setEnabled(false);
        }
        setParameterEditorEnabled(block.isEnabled());
    }

    @Action
    public void setBlockEnabled() {
        blockTree.getSelectedBlock().setEnabled(ckBoxBlock.isSelected());
        setParameterEditorEnabled(ckBoxBlock.isSelected());
        ensureSingleBlockSelection(BlockType.DATASET);
        ensureSingleBlockSelection(BlockType.RELEASE);
        firePropertyChange("configurationFile", null, null);
    }

    private void ensureSingleBlockSelection(BlockType type) {
        XBlock selectedBlock = blockTree.getSelectedBlock();
        if (selectedBlock.getType().equals(type)) {
            /* If the selected block is newly enable, we
            deactivate all the others blocks with same type */
            if (selectedBlock.isEnabled()) {
                for (XBlock block : getSimulationManager().getParameterManager().getBlocks(type)) {
                    if (!block.getKey().equals(selectedBlock.getKey())) {
                        blockTree.get(block.getTreePath()).setEnabled(false);
                    }
                }
            } else {
                /* Warn user in case no block of this type is enable */
                StringBuffer msg = new StringBuffer();
                msg.append(getResourceMap().getString("noBlockEnabled.text.part1"));
                msg.append(" <");
                msg.append(type.toString());
                msg.append("> ");
                msg.append(getResourceMap().getString("noBlockEnabled.text.part2"));
                JOptionPane.showMessageDialog(this, msg.toString());
            }
        }
    }

    private void setParameterEditorEnabled(boolean enabled) {
        table.setEnabled(enabled);
        btnUndo.getAction().setEnabled(enabled && getTable().getUndoManager().canUndo());
        btnRedo.getAction().setEnabled(enabled && getTable().getUndoManager().canRedo());
        ckBoxHiddenParameter.getAction().setEnabled(enabled && (blockTree.getSelectedBlock().getNbHiddenParameters() > 0));
        btnAddValue.getAction().setEnabled(false);
        btnRemoveValue.getAction().setEnabled(false);
    }

    public void valueChanged(TreeSelectionEvent e) {
        Application.getInstance().getContext().getTaskService().execute(new ShowConfigEditorsTask());
    }

    public void actionPerformed(ActionEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                getTable().stopEditing();
            }
        });
    }

    private class ShowConfigEditorsTask extends Task {

        ShowConfigEditorsTask() {
            super(Application.getInstance());
            blockTree.setEnabled(false);
            busyLabel.setText(getResourceMap().getString("ShowConfigEditorsTask.loading.text"));
            busyLabel.setBusy(true);
            pnlBlock.setVisible(false);
            pnlEditors.setLayout(new StackLayout());
            pnlEditors.add(busyLabel, StackLayout.TOP);
        }

        @Override
        protected Object doInBackground() throws Exception {

            DefaultMutableTreeNode node = blockTree.getSelectedNode();
            if (node != null && node.isLeaf()) {
                XBlock block = blockTree.getSelectedBlock();
                setupBlockPanel(block);
            } else {
                cancel(true);
            }
            return null;
        }

        @Override
        protected void succeeded(Object o) {
            pnlBlock.setVisible(true);
            splitPaneCfg.setRightComponent(scrollPaneEditors);
        }

        @Override
        protected void finished() {
            busyLabel.setBusy(false);
            pnlEditors.remove(busyLabel);
            blockTree.setEnabled(true);
        }

        @Override
        protected void cancelled() {
            splitPaneCfg.setRightComponent(pnlNoBlockSelected);
        }
    }

    public void tableChanged(TableModelEvent e) {
        if (e != null) {
            btnRedo.getAction().setEnabled(false);
            btnUndo.getAction().setEnabled(true);
            firePropertyChange("configurationFile", null, null);
        }
        btnAddValue.getAction().setEnabled(false);
        btnRemoveValue.getAction().setEnabled(false);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            try {
                int viewRow = table.getSelectedRow();
                int modelRow = viewRow;
                if (viewRow < 0) {
                    //Selection got filtered away.
                    return;
                } else {
                    modelRow = table.convertRowIndexToModel(viewRow);
                }
                XParameter xparam = blockTree.getSelectedBlock().getXParameter(getTable().getParameterKey(modelRow));
                String title = getResourceMap().getString("pnlParameterInfo.border.title") + " " + xparam.getLongName();
                pnlParameterInfo.setBorder(BorderFactory.createTitledBorder(title));
                StringBuffer info = new StringBuffer("<html><i><p>");
                if (null != xparam.getDescription()) {
                    info.append(xparam.getDescription());
                } else {
                    info.append(getResourceMap().getString("noDescription.text"));
                }
                info.append("</p><br>");
                if (xparam.isSerial()) {
                    info.append(getResourceMap().getString("serialParameterDescription.text"));
                    info.append("<br>");
                }
                if (xparam.isHidden()) {
                    info.append(getResourceMap().getString("hiddenParameterDescription.text"));
                }
                info.append("</i></html>");
                lblParameter.setText(info.toString());
                btnAddValue.getAction().setEnabled(xparam.isSerial());
                btnRemoveValue.getAction().setEnabled(xparam.isSerial() && (xparam.getLength() > 1));
            } catch (Exception ex) {
                pnlParameterInfo.setBorder(BorderFactory.createTitledBorder(getResourceMap().getString("pnlParameterInfo.border.title")));
                lblParameter.setText(getResourceMap().getString("noDescription.text"));
            }
        }
    }

    private ResourceMap getResourceMap() {
        return Application.getInstance().getContext().getResourceMap(JConfigurationPanel.class);
    }

    @Action
    public void showHiddenParameters() {
        getTable().setAllRowsVisible(ckBoxHiddenParameter.isSelected());
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

    @Action
    public Task addSerialValue() {
        return new SerialValueTask(SerialValueTask.ADD);
    }

    @Action
    public Task removeSerialValue() {
        return new SerialValueTask(SerialValueTask.REMOVE);
    }

    private class SerialValueTask extends Task {

        final static int ADD = 1;
        final static int REMOVE = -1;
        private int actionType;

        SerialValueTask(int actionType) {
            super(Application.getInstance());
            this.actionType = actionType;
            busyLabel.setBusy(true);
            busyLabel.setText("");
            scrollPaneTable.setViewportView(busyLabel);
            btnRemoveValue.getAction().setEnabled(false);
            btnAddValue.getAction().setEnabled(false);
        }

        @Override
        protected Object doInBackground() throws Exception {
            int row = table.getSelectedRow();
            XParameter xparam = blockTree.getSelectedBlock().getXParameter(getTable().getParameterKey(row).toString());
            if (actionType == ADD) {
                xparam.addValue();
            } else {
                int index = getTable().getParameterIndex(row);
                xparam.removeValue(index);
            }
            getTable().setModel(blockTree.getSelectedBlock(), JConfigurationPanel.this);
            return null;
        }

        @Override
        protected void succeeded(Object o) {
            scrollPaneTable.setViewportView(table);
            JConfigurationPanel.this.firePropertyChange("configurationFile", null, null);
        }
    }

    public void updateXMLStructure() {
        getSimulationManager().getParameterManager().setConfigurationTitle(textFieldTitle.getText());
        getSimulationManager().getParameterManager().setConfigurationDescription(textAreaDescription.getText());
        blockTree.writeStructure(getSimulationManager().getParameterManager());
        hasStructureChanged = false;
    }

    public Task loadBlockTree() {
        return new CreateBlockTreeTask(IchthyopApp.getApplication());
    }

    private class CreateBlockTreeTask extends SFTask {

        private String cfgTitle, cfgDescription, cfgVersion;

        CreateBlockTreeTask(Application instance) {
            super(instance);
            blockTree.setVisible(false);
        }

        @Override
        protected Object doInBackground() throws Exception {
            cfgTitle = getSimulationManager().getParameterManager().getConfigurationTitle();
            cfgDescription = getSimulationManager().getParameterManager().getConfigurationDescription();
            cfgVersion = getSimulationManager().getParameterManager().getConfigurationVersion().toString();
            blockTree.createModel();
            return null;
        }

        @Override
        void onSuccess(Object result) {
            setVisible(true);
            blockTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            blockTree.addTreeSelectionListener(JConfigurationPanel.this);
            //blockTree.setNodeVisible(blockTree.getRoot().getFirstLeaf());
        }

        @Override
        void onFailure(Throwable throwable) {
            // do nothing
        }

        @Override
        protected void finished() {
            blockTree.setVisible(true);
            textFieldTitle.setText(cfgTitle);
            if (null != cfgDescription) {
                textAreaDescription.setText(cfgDescription);
            } else {
                textAreaDescription.setText("You can type here a description for this configuration");
            }
            if (null != cfgVersion) {
                textFieldVersion.setText(cfgVersion);
            }
            splitPaneCfg.setRightComponent(pnlNoBlockSelected);
        }
    }

    private SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        blockEditor = new javax.swing.JPanel();
        pnlNoBlockSelected = new javax.swing.JPanel();
        lblSelectBlock = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        lblTitle = new javax.swing.JLabel();
        lblDescription = new javax.swing.JLabel();
        textFieldTitle = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        textAreaDescription = new javax.swing.JTextArea();
        lblVersion = new javax.swing.JLabel();
        textFieldVersion = new javax.swing.JTextField();
        busyLabel = new JXBusyLabel(new Dimension(100, 100));
        splitPaneCfg = new javax.swing.JSplitPane();
        pnlBlockTree = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        blockTree = new org.previmer.ichthyop.ui.BlockTree();
        jToolBar1 = new javax.swing.JToolBar();
        btnExpand = new javax.swing.JButton();
        btnCollapse = new javax.swing.JButton();
        btnUpper = new javax.swing.JButton();
        btnLower = new javax.swing.JButton();
        scrollPaneEditors = new javax.swing.JScrollPane();
        pnlEditors = new javax.swing.JPanel();
        pnlBlock = new javax.swing.JPanel();
        pnlBlockInfo = new javax.swing.JPanel();
        lblBlockInfo = new javax.swing.JLabel();
        ckBoxBlock = new javax.swing.JCheckBox();
        pnlParameters = new javax.swing.JPanel();
        pnlParameterInfo = new javax.swing.JPanel();
        lblParameter = new javax.swing.JLabel();
        btnUndo = new javax.swing.JButton();
        btnRedo = new javax.swing.JButton();
        btnAddValue = new javax.swing.JButton();
        btnRemoveValue = new javax.swing.JButton();
        ckBoxHiddenParameter = new javax.swing.JCheckBox();
        scrollPaneTable = new javax.swing.JScrollPane();
        table = new org.previmer.ichthyop.ui.ParameterTable();

        blockEditor.setName("blockEditor"); // NOI18N

        javax.swing.GroupLayout blockEditorLayout = new javax.swing.GroupLayout(blockEditor);
        blockEditor.setLayout(blockEditorLayout);
        blockEditorLayout.setHorizontalGroup(
            blockEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 650, Short.MAX_VALUE)
        );
        blockEditorLayout.setVerticalGroup(
            blockEditorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 574, Short.MAX_VALUE)
        );

        pnlNoBlockSelected.setName("pnlNoBlockSelected"); // NOI18N

        lblSelectBlock.setFont(new java.awt.Font("DejaVu Sans", 1, 16));
        lblSelectBlock.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSelectBlock.setText("Select a block in the tree.");
        lblSelectBlock.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblSelectBlock.setName("lblSelectBlock"); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Configuration details"));
        jPanel1.setName("jPanel1"); // NOI18N

        lblTitle.setText("Title");
        lblTitle.setName("lblTitle"); // NOI18N

        lblDescription.setText("Description");
        lblDescription.setName("lblDescription"); // NOI18N

        textFieldTitle.setName("textFieldTitle"); // NOI18N
        textFieldTitle.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textFieldTitleKeyTyped(evt);
            }
        });

        jScrollPane2.setName("jScrollPane2"); // NOI18N

        textAreaDescription.setColumns(20);
        textAreaDescription.setLineWrap(true);
        textAreaDescription.setRows(5);
        textAreaDescription.setWrapStyleWord(true);
        textAreaDescription.setName("textAreaDescription"); // NOI18N
        textAreaDescription.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                textFieldTitleKeyTyped(evt);
            }
        });
        jScrollPane2.setViewportView(textAreaDescription);

        lblVersion.setText("Version");
        lblVersion.setName("lblVersion"); // NOI18N

        textFieldVersion.setEditable(false);
        textFieldVersion.setText("undetermined");
        textFieldVersion.setName("textFieldVersion"); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(textFieldTitle, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 526, Short.MAX_VALUE)
                    .addComponent(lblTitle, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblDescription, javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                        .addComponent(lblVersion)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textFieldVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(lblTitle)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textFieldTitle, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblDescription)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVersion)
                    .addComponent(textFieldVersion, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlNoBlockSelectedLayout = new javax.swing.GroupLayout(pnlNoBlockSelected);
        pnlNoBlockSelected.setLayout(pnlNoBlockSelectedLayout);
        pnlNoBlockSelectedLayout.setHorizontalGroup(
            pnlNoBlockSelectedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSelectBlock, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 586, Short.MAX_VALUE)
            .addGroup(pnlNoBlockSelectedLayout.createSequentialGroup()
                .addGap(12, 12, 12)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(12, 12, 12))
        );
        pnlNoBlockSelectedLayout.setVerticalGroup(
            pnlNoBlockSelectedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlNoBlockSelectedLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblSelectBlock, javax.swing.GroupLayout.DEFAULT_SIZE, 188, Short.MAX_VALUE))
        );

        busyLabel.setText("jXBusyLabel1");
        busyLabel.setName("busyLabel"); // NOI18N
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
        splitPaneCfg.setName("splitPaneCfg"); // NOI18N

        pnlBlockTree.setName("pnlBlockTree"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        blockTree.setName("blockTree"); // NOI18N
        blockTree.setRootVisible(true);
        jScrollPane1.setViewportView(blockTree);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(JConfigurationPanel.class, this);
        btnExpand.setAction(actionMap.get("expand")); // NOI18N
        btnExpand.setFocusable(false);
        btnExpand.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExpand.setName("btnExpand"); // NOI18N
        btnExpand.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnExpand);

        btnCollapse.setAction(actionMap.get("collapse")); // NOI18N
        btnCollapse.setFocusable(false);
        btnCollapse.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCollapse.setName("btnCollapse"); // NOI18N
        btnCollapse.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnCollapse);

        btnUpper.setAction(actionMap.get("upper")); // NOI18N
        btnUpper.setFocusable(false);
        btnUpper.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnUpper.setName("btnUpper"); // NOI18N
        btnUpper.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnUpper);

        btnLower.setAction(actionMap.get("lower")); // NOI18N
        btnLower.setFocusable(false);
        btnLower.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLower.setName("btnLower"); // NOI18N
        btnLower.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnLower);

        javax.swing.GroupLayout pnlBlockTreeLayout = new javax.swing.GroupLayout(pnlBlockTree);
        pnlBlockTree.setLayout(pnlBlockTreeLayout);
        pnlBlockTreeLayout.setHorizontalGroup(
            pnlBlockTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBlockTreeLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBlockTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlBlockTreeLayout.setVerticalGroup(
            pnlBlockTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBlockTreeLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 493, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        splitPaneCfg.setLeftComponent(pnlBlockTree);

        scrollPaneEditors.setName("scrollPaneEditors"); // NOI18N

        pnlEditors.setName("pnlEditors"); // NOI18N

        pnlBlock.setName("pnlBlock"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(JConfigurationPanel.class);
        pnlBlockInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlBlockInfo.border.title"))); // NOI18N
        pnlBlockInfo.setName("pnlBlockInfo"); // NOI18N

        lblBlockInfo.setText("Block information");
        lblBlockInfo.setName("lblBlockInfo"); // NOI18N

        ckBoxBlock.setAction(actionMap.get("setBlockEnabled")); // NOI18N
        ckBoxBlock.setName("ckBoxBlock"); // NOI18N

        javax.swing.GroupLayout pnlBlockInfoLayout = new javax.swing.GroupLayout(pnlBlockInfo);
        pnlBlockInfo.setLayout(pnlBlockInfoLayout);
        pnlBlockInfoLayout.setHorizontalGroup(
            pnlBlockInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBlockInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBlockInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(ckBoxBlock)
                    .addComponent(lblBlockInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlBlockInfoLayout.setVerticalGroup(
            pnlBlockInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBlockInfoLayout.createSequentialGroup()
                .addComponent(lblBlockInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 58, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(ckBoxBlock)
                .addContainerGap())
        );

        pnlParameters.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlParameters.border.title"))); // NOI18N
        pnlParameters.setName("pnlParameters"); // NOI18N

        pnlParameterInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlParameterInfo.border.title"))); // NOI18N
        pnlParameterInfo.setName("pnlParameterInfo"); // NOI18N

        lblParameter.setText("<html><i>No description available</i><html>");
        lblParameter.setName("lblParameter"); // NOI18N

        javax.swing.GroupLayout pnlParameterInfoLayout = new javax.swing.GroupLayout(pnlParameterInfo);
        pnlParameterInfo.setLayout(pnlParameterInfoLayout);
        pnlParameterInfoLayout.setHorizontalGroup(
            pnlParameterInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParameterInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblParameter, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlParameterInfoLayout.setVerticalGroup(
            pnlParameterInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParameterInfoLayout.createSequentialGroup()
                .addComponent(lblParameter, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnUndo.setAction(actionMap.get("undo")); // NOI18N
        btnUndo.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
        btnUndo.setName("btnUndo"); // NOI18N

        btnRedo.setAction(actionMap.get("redo")); // NOI18N
        btnRedo.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
        btnRedo.setName("btnRedo"); // NOI18N

        btnAddValue.setAction(actionMap.get("addSerialValue")); // NOI18N
        btnAddValue.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
        btnAddValue.setName("btnAddValue"); // NOI18N

        btnRemoveValue.setAction(actionMap.get("removeSerialValue")); // NOI18N
        btnRemoveValue.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
        btnRemoveValue.setName("btnRemoveValue"); // NOI18N

        ckBoxHiddenParameter.setAction(actionMap.get("showHiddenParameters")); // NOI18N
        ckBoxHiddenParameter.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
        ckBoxHiddenParameter.setName("ckBoxHiddenParameter"); // NOI18N

        scrollPaneTable.setName("scrollPaneTable"); // NOI18N

        table.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null},
                {null, null, null, null}
            },
            new String [] {
                "Title 1", "Title 2", "Title 3", "Title 4"
            }
        ));
        table.setName("table"); // NOI18N
        table.getModel().addTableModelListener(this);
        table.getSelectionModel().addListSelectionListener(this);
        scrollPaneTable.setViewportView(table);

        javax.swing.GroupLayout pnlParametersLayout = new javax.swing.GroupLayout(pnlParameters);
        pnlParameters.setLayout(pnlParametersLayout);
        pnlParametersLayout.setHorizontalGroup(
            pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlParametersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(scrollPaneTable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 626, Short.MAX_VALUE)
                    .addComponent(ckBoxHiddenParameter, javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlParameterInfo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, pnlParametersLayout.createSequentialGroup()
                        .addComponent(btnUndo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRedo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddValue)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveValue)))
                .addContainerGap())
        );
        pnlParametersLayout.setVerticalGroup(
            pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParametersLayout.createSequentialGroup()
                .addComponent(ckBoxHiddenParameter)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPaneTable, javax.swing.GroupLayout.DEFAULT_SIZE, 208, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUndo)
                    .addComponent(btnRedo)
                    .addComponent(btnAddValue)
                    .addComponent(btnRemoveValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlParameterInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlBlockLayout = new javax.swing.GroupLayout(pnlBlock);
        pnlBlock.setLayout(pnlBlockLayout);
        pnlBlockLayout.setHorizontalGroup(
            pnlBlockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBlockLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBlockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlParameters, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlBlockInfo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlBlockLayout.setVerticalGroup(
            pnlBlockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBlockLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlBlockInfo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlParameters, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlEditorsLayout = new javax.swing.GroupLayout(pnlEditors);
        pnlEditors.setLayout(pnlEditorsLayout);
        pnlEditorsLayout.setHorizontalGroup(
            pnlEditorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBlock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        pnlEditorsLayout.setVerticalGroup(
            pnlEditorsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(pnlBlock, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        scrollPaneEditors.setViewportView(pnlEditors);

        splitPaneCfg.setRightComponent(scrollPaneEditors);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPaneCfg, javax.swing.GroupLayout.DEFAULT_SIZE, 944, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPaneCfg, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 569, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void textFieldTitleKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_textFieldTitleKeyTyped
        // TODO add your handling code here:
        firePropertyChange("configurationFile", null, null);
    }//GEN-LAST:event_textFieldTitleKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel blockEditor;
    private org.previmer.ichthyop.ui.BlockTree blockTree;
    private javax.swing.JButton btnAddValue;
    private javax.swing.JButton btnCollapse;
    private javax.swing.JButton btnExpand;
    private javax.swing.JButton btnLower;
    private javax.swing.JButton btnRedo;
    private javax.swing.JButton btnRemoveValue;
    private javax.swing.JButton btnUndo;
    private javax.swing.JButton btnUpper;
    private org.jdesktop.swingx.JXBusyLabel busyLabel;
    private javax.swing.JCheckBox ckBoxBlock;
    private javax.swing.JCheckBox ckBoxHiddenParameter;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblBlockInfo;
    private javax.swing.JLabel lblDescription;
    private javax.swing.JLabel lblParameter;
    private javax.swing.JLabel lblSelectBlock;
    private javax.swing.JLabel lblTitle;
    private javax.swing.JLabel lblVersion;
    private javax.swing.JPanel pnlBlock;
    private javax.swing.JPanel pnlBlockInfo;
    private javax.swing.JPanel pnlBlockTree;
    private javax.swing.JPanel pnlEditors;
    private javax.swing.JPanel pnlNoBlockSelected;
    private javax.swing.JPanel pnlParameterInfo;
    private javax.swing.JPanel pnlParameters;
    private javax.swing.JScrollPane scrollPaneEditors;
    private javax.swing.JScrollPane scrollPaneTable;
    private javax.swing.JSplitPane splitPaneCfg;
    private javax.swing.JTable table;
    private javax.swing.JTextArea textAreaDescription;
    private javax.swing.JTextField textFieldTitle;
    private javax.swing.JTextField textFieldVersion;
    // End of variables declaration//GEN-END:variables
    private boolean hasStructureChanged;
}
