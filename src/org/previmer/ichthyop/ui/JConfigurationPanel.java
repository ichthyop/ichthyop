/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/*
 * JConfigurationPanel.java
 *
 * Created on Feb 24, 2010, 2:17:08 PM
 */
package org.previmer.ichthyop.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.application.Action;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.Task;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;
import org.previmer.ichthyop.arch.ISimulationManager;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 *
 * @author pverley
 */
public class JConfigurationPanel extends javax.swing.JPanel implements TreeSelectionListener, TableModelListener, ListSelectionListener {

    /** Creates new form JConfigurationPanel */
    public JConfigurationPanel() {
        initComponents();
    }

    @Action
    public void upper() {
        blockTree.removeTreeSelectionListener(this);
        hasStructureChanged |= blockTree.upper();
        blockTree.addTreeSelectionListener(this);
        if (hasStructureChanged) {
            firePropertyChange("xicfile", null, null);
        }
    }

    @Action
    public void lower() {
        blockTree.removeTreeSelectionListener(this);
        hasStructureChanged |= blockTree.lower();
        blockTree.addTreeSelectionListener(this);
        if (hasStructureChanged) {
            firePropertyChange("xicfile", null, null);
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

    private void setupAdvancedEditor(XBlock block) {

        pnlBlockInfo.setBorder(BorderFactory.createTitledBorder(block.getTreePath()));
        if (block.getType().equals(BlockType.OPTION)) {
            ckBoxBlock.setVisible(false);
        } else {
            ckBoxBlock.setVisible(true);
            ckBoxBlock.setSelected(block.isEnabled());
        }
        StringBuffer info = new StringBuffer("<html><i>");
        info.append(block.getDescription());
        info.append("</i></html>");
        lblBlockInfo.setText(info.toString());
        btnUndo.getAction().setEnabled(false);
        btnRedo.getAction().setEnabled(false);
        btnAddValue.getAction().setEnabled(false);
        btnRemoveValue.getAction().setEnabled(false);
        if (!showHiddenParameters) {
            btnHiddenParameter.doClick();
        } else {
            getTable().setModel(block, this);
        }
        if (block.getNbHiddenParameters() > 0) {
            btnHiddenParameter.getAction().setEnabled(true);
        } else {
            btnHiddenParameter.getAction().setEnabled(false);
        }
        setParameterEditorEnabled(block.isEnabled());
    }

    @Action
    public void setBlockEnabled() {
        blockTree.getSelectedBlock().setEnabled(ckBoxBlock.isSelected());
        setParameterEditorEnabled(ckBoxBlock.isSelected());
        firePropertyChange("xicfile", null, null);
    }

    private void setParameterEditorEnabled(boolean enabled) {
        table.setEnabled(enabled);
        btnUndo.getAction().setEnabled(enabled && getTable().getUndoManager().canUndo());
        btnRedo.getAction().setEnabled(enabled && getTable().getUndoManager().canRedo());
        btnHiddenParameter.getAction().setEnabled(enabled && (blockTree.getSelectedBlock().getNbHiddenParameters() > 0));
        btnAddValue.getAction().setEnabled(false);
        btnRemoveValue.getAction().setEnabled(false);
    }

    @Action
    public void reloadEditor() {
        valueChanged(new TreeSelectionEvent(new JButton(), null, true, null, null));
    }

    private JBlockPanel getBlockEditor(XBlock block) {

        if (null != block.getXParameter("editor")) {
            String editorClass = block.getXParameter("editor").getValue();
            try {
                Constructor constructor = Class.forName(editorClass).getConstructor(XBlock.class);
                return (JBlockPanel) constructor.newInstance(block);
            } catch (InstantiationException ex) {
                Logger.getLogger(JConfigurationPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(JConfigurationPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(JConfigurationPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(JConfigurationPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(JConfigurationPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(JConfigurationPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(JConfigurationPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        } else {
            return null;
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        Application.getInstance().getContext().getTaskService().execute(new ShowConfigEditorsTask());
    }

    private class ShowConfigEditorsTask extends Task {

        ShowConfigEditorsTask() {
            super(Application.getInstance());
            blockTree.setEnabled(false);
            busyLabel.setText("Loading parameter editor...");
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
                if (block.getType().equals(BlockType.ZONE)) {
                    cancel(true);
                }
                setupAdvancedEditor(block);
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
            int row = table.getSelectedRow();
            XParameter xparam = blockTree.getSelectedBlock().getXParameter(getTable().getParameterKey(row).toString());
            xparam.setValue(table.getValueAt(row, 1).toString(), getTable().getParameterIndex(row));
            btnRedo.getAction().setEnabled(false);
            btnUndo.getAction().setEnabled(true);
            firePropertyChange("xicfile", null, null);
        }
        btnAddValue.getAction().setEnabled(false);
        btnRemoveValue.getAction().setEnabled(false);
    }

    public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            try {
                XParameter xparam = blockTree.getSelectedBlock().getXParameter(getTable().getParameterKey(table.getSelectedRow()));
                pnlParamDescription.setBorder(BorderFactory.createTitledBorder(xparam.getKey()));
                StringBuffer info = new StringBuffer("<html><i>");
                info.append(xparam.getDescription());
                info.append("</i></html>");
                lblParameter.setText(info.toString());
                btnAddValue.getAction().setEnabled(xparam.isSerial());
                btnRemoveValue.getAction().setEnabled(xparam.isSerial() && (xparam.getLength() > 1));
            } catch (Exception ex) {
                pnlParamDescription.setBorder(BorderFactory.createTitledBorder(getResourceMap().getString("pnlParamDescription.border.title")));
                lblParameter.setText(getResourceMap().getString("lblParameter.text"));
            }
        }
    }

    private ResourceMap getResourceMap() {
        return Application.getInstance().getContext().getResourceMap(JConfigurationPanel.class);
    }

    @Action
    public void showHiddenParameters() {
        if (showHiddenParameters) {
            btnHiddenParameter.setText(getResourceMap().getString("showHiddenParameters.Action.text.hide"));
            btnHiddenParameter.setIcon(getResourceMap().getIcon("showHiddenParameters.Action.icon.unlock"));
            getTable().setAllRowsVisible(true);
            showHiddenParameters = false;

        } else {
            btnHiddenParameter.setText(getResourceMap().getString("showHiddenParameters.Action.text.show"));
            btnHiddenParameter.setIcon(getResourceMap().getIcon("showHiddenParameters.Action.icon.lock"));
            getTable().setAllRowsVisible(false);
            showHiddenParameters = true;
        }
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
            srollPaneTable.setViewportView(busyLabel);
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
            srollPaneTable.setViewportView(table);
            JConfigurationPanel.this.firePropertyChange("xicfile", null, null);
        }
    }

    public void updateXMLStructure() {
        blockTree.writeStructure(getSimulationManager().getParameterManager());
        hasStructureChanged = false;
    }

    public Task loadBlockTree() {
        return new CreateBlockTreeTask(IchthyopApp.getApplication());
    }

    private class CreateBlockTreeTask extends SFTask {

        CreateBlockTreeTask(Application instance) {
            super(instance);
            blockTree.setVisible(false);
        }

        @Override
        protected Object doInBackground() throws Exception {

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
            splitPaneCfg.setRightComponent(pnlNoBlockSelected);
        }
    }

    public ISimulationManager getSimulationManager() {
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
        pnlParamDescription = new javax.swing.JPanel();
        lblParameter = new javax.swing.JLabel();
        btnHiddenParameter = new org.jdesktop.swingx.JXHyperlink();
        btnUndo = new javax.swing.JButton();
        btnRedo = new javax.swing.JButton();
        btnAddValue = new javax.swing.JButton();
        btnRemoveValue = new javax.swing.JButton();
        srollPaneTable = new javax.swing.JScrollPane();
        table = new ParameterTable();

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

        javax.swing.GroupLayout pnlNoBlockSelectedLayout = new javax.swing.GroupLayout(pnlNoBlockSelected);
        pnlNoBlockSelected.setLayout(pnlNoBlockSelectedLayout);
        pnlNoBlockSelectedLayout.setHorizontalGroup(
            pnlNoBlockSelectedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSelectBlock, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
        );
        pnlNoBlockSelectedLayout.setVerticalGroup(
            pnlNoBlockSelectedLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSelectBlock, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
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

        pnlBlockInfo.setBorder(javax.swing.BorderFactory.createTitledBorder("Block name"));
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
                    .addComponent(lblBlockInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE))
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

        pnlParameters.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameters"));
        pnlParameters.setName("pnlParameters"); // NOI18N

        pnlParamDescription.setBorder(javax.swing.BorderFactory.createTitledBorder("Parameter"));
        pnlParamDescription.setName("pnlParamDescription"); // NOI18N

        lblParameter.setText("<html><i>No description available</i><html>");
        lblParameter.setName("lblParameter"); // NOI18N

        javax.swing.GroupLayout pnlParamDescriptionLayout = new javax.swing.GroupLayout(pnlParamDescription);
        pnlParamDescription.setLayout(pnlParamDescriptionLayout);
        pnlParamDescriptionLayout.setHorizontalGroup(
            pnlParamDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParamDescriptionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblParameter, javax.swing.GroupLayout.DEFAULT_SIZE, 338, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlParamDescriptionLayout.setVerticalGroup(
            pnlParamDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParamDescriptionLayout.createSequentialGroup()
                .addComponent(lblParameter, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnHiddenParameter.setAction(actionMap.get("showHiddenParameters")); // NOI18N
        btnHiddenParameter.setClickedColor(new java.awt.Color(1, 1, 1));
        btnHiddenParameter.setUnclickedColor(new java.awt.Color(1, 1, 1));
        btnHiddenParameter.setFont(new java.awt.Font("DejaVu Sans", 0, 12));
        btnHiddenParameter.setName("btnHiddenParameter"); // NOI18N

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

        srollPaneTable.setName("srollPaneTable"); // NOI18N

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
        table.getSelectionModel().addListSelectionListener(this);
        srollPaneTable.setViewportView(table);

        javax.swing.GroupLayout pnlParametersLayout = new javax.swing.GroupLayout(pnlParameters);
        pnlParameters.setLayout(pnlParametersLayout);
        pnlParametersLayout.setHorizontalGroup(
            pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlParametersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlParamDescription, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(srollPaneTable, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 374, Short.MAX_VALUE)
                    .addComponent(btnHiddenParameter, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
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
                .addComponent(btnHiddenParameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(srollPaneTable, javax.swing.GroupLayout.PREFERRED_SIZE, 226, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnUndo)
                    .addComponent(btnRedo)
                    .addComponent(btnAddValue)
                    .addComponent(btnRemoveValue))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlParamDescription, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlBlockLayout = new javax.swing.GroupLayout(pnlBlock);
        pnlBlock.setLayout(pnlBlockLayout);
        pnlBlockLayout.setHorizontalGroup(
            pnlBlockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlBlockLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBlockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(pnlBlockInfo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlParameters, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel blockEditor;
    private org.previmer.ichthyop.ui.BlockTree blockTree;
    private javax.swing.JButton btnAddValue;
    private javax.swing.JButton btnCollapse;
    private javax.swing.JButton btnExpand;
    private org.jdesktop.swingx.JXHyperlink btnHiddenParameter;
    private javax.swing.JButton btnLower;
    private javax.swing.JButton btnRedo;
    private javax.swing.JButton btnRemoveValue;
    private javax.swing.JButton btnUndo;
    private javax.swing.JButton btnUpper;
    private org.jdesktop.swingx.JXBusyLabel busyLabel;
    private javax.swing.JCheckBox ckBoxBlock;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblBlockInfo;
    private javax.swing.JLabel lblParameter;
    private javax.swing.JLabel lblSelectBlock;
    private javax.swing.JPanel pnlBlock;
    private javax.swing.JPanel pnlBlockInfo;
    private javax.swing.JPanel pnlBlockTree;
    private javax.swing.JPanel pnlEditors;
    private javax.swing.JPanel pnlNoBlockSelected;
    private javax.swing.JPanel pnlParamDescription;
    private javax.swing.JPanel pnlParameters;
    private javax.swing.JScrollPane scrollPaneEditors;
    private javax.swing.JSplitPane splitPaneCfg;
    private javax.swing.JScrollPane srollPaneTable;
    private org.jdesktop.swingx.JXTable table;
    // End of variables declaration//GEN-END:variables
    private boolean showHiddenParameters = true;
    private boolean hasStructureChanged;
}
