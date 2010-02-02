/*
 * IchthyopView.java
 */
package org.previmer.ichthyop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Toolkit;
import java.beans.PropertyChangeEvent;
import java.lang.reflect.InvocationTargetException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeSelectionEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.EventObject;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.BorderFactory;
import javax.swing.Timer;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.previmer.ichthyop.arch.ISimulationManager;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.manager.SimulationManager;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;
import org.previmer.ichthyop.ui.WMSMapper.MapStep;
import org.previmer.ichthyop.util.MetaFilenameFilter;

/**
 * The application's main frame.
 */
public class IchthyopView extends FrameView
        implements TimingTarget, TreeSelectionListener, TableModelListener,
        ListSelectionListener, PropertyChangeListener {

    public IchthyopView(SingleFrameApplication app) {
        super(app);

        createLogfile();

        initComponents();
        getFrame().setIconImage(getResourceMap().getImageIcon("Application.icon").getImage());
        getApplication().addExitListener(new ConfirmExit());

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
                lblFlag.setIcon(getResourceMap().getIcon("lblFlag.icon.grey"));
            }
        });
        messageTimer.setRepeats(false);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    statusAnimationLabel.setBusy(true);
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("failed".equals(propertyName)) {
                    Toolkit.getDefaultToolkit().beep();
                    statusAnimationLabel.setBusy(false);
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                    lblFlag.setIcon(getResourceMap().getIcon("lblFlag.icon.red"));
                } else if ("succeeded".equals(propertyName)) {
                    statusAnimationLabel.setBusy(false);
                    lblFlag.setIcon(getResourceMap().getIcon("lblFlag.icon.green"));
                    progressBar.setVisible(false);
                    progressBar.setValue(0);
                } else if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    statusMessageLabel.setText((text == null) ? "" : text);
                    messageTimer.restart();
                } else if ("progress".equals(propertyName)) {
                    int value = (Integer) (evt.getNewValue());
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(false);
                    progressBar.setValue(value);
                }
            }
        });

        closeMenuItem.getAction().setEnabled(false);
        saveAsMenuItem.getAction().setEnabled(false);
        btnSimulationRun.getAction().setEnabled(false);
        btnPreview.getAction().setEnabled(false);
        btnCancelMapping.getAction().setEnabled(false);
        btnMapping.getAction().setEnabled(false);
        btnCloseNC.getAction().setEnabled(false);
        setAnimationToolsEnabled(false);
        addPropertyChangeListener("xicfile", this);
    }

    public ISimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    private void createLogfile() {
        try {
            String logPath = System.getProperty("user.dir") + File.separator + "log" + File.separator;
            StringBuffer logfile = new StringBuffer(logPath);
            logfile.append("ichthyop");
            logfile.append(".log");
            IOTools.makeDirectories(logfile.toString());
            FileHandler fh = new FileHandler(logfile.toString());
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            logger.info("Created log file " + logfile.toString());
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = IchthyopApp.getApplication().getMainFrame();
            aboutBox = new IchthyopAboutBox(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        IchthyopApp.getApplication().show(aboutBox);
    }

    private void setAnimationToolsEnabled(boolean enabled) {
        btnExportMaps.getAction().setEnabled(enabled);
        btnDeleteMaps.getAction().setEnabled(enabled);
        btnFirst.getAction().setEnabled(enabled);
        btnPrevious.getAction().setEnabled(enabled);
        btnAnimaction.getAction().setEnabled(enabled);
        btnNext.getAction().setEnabled(enabled);
        btnLast.getAction().setEnabled(enabled);
        sliderTime.setEnabled(enabled);
    }

    @Action
    public void upper() {
        blockTree.removeTreeSelectionListener(IchthyopView.this);
        hasStructureChanged |= blockTree.upper();
        blockTree.addTreeSelectionListener(IchthyopView.this);
        if (hasStructureChanged) {
            firePropertyChange("xicfile", null, null);
        }
    }

    @Action
    public void lower() {
        blockTree.removeTreeSelectionListener(IchthyopView.this);
        hasStructureChanged |= blockTree.lower();
        blockTree.addTreeSelectionListener(IchthyopView.this);
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

    @Action
    public void deleteMaps() {
        File[] files2Delete = outputFolder.listFiles(new MetaFilenameFilter("*.png"));
        StringBuffer message = new StringBuffer("Delete maps from ");
        message.append(outputFolder.getName());
        message.append(" ?");
        message.append('\n');
        message.append(files2Delete.length);
        message.append(" PNG pictures will be deleted from your computer.");
        int dialog = JOptionPane.showConfirmDialog(getFrame(), message.toString(), "Ichthytop - Delete snapshots", JOptionPane.OK_CANCEL_OPTION);
        if (dialog == JOptionPane.OK_OPTION) {
            for (File file : outputFolder.listFiles(new MetaFilenameFilter("*.png"))) {
                if (file.delete()) {
                    setMessage("Deleted " + file.toString());
                }
            }
            outputFolder.delete();
            lblFolder.setText(getResourceMap().getString("lblFolder.text"));
            outputFolder = null;
            replayPanel.setFolder(null);
            setAnimationToolsEnabled(false);
        }
    }

    @Action
    public Task createMaps() {
        return createMapTask = new CreateMapTask(getApplication());
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
        valueChanged(new TreeSelectionEvent(ckBoxAdvancedEditor, null, true, null, null));
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

    private JBlockPanel getBlockEditor(XBlock block) {

        if (null != block.getXParameter("editor")) {
            String editorClass = block.getXParameter("editor").getValue();
            try {
                Constructor constructor = Class.forName(editorClass).getConstructor(XBlock.class);
                return (JBlockPanel) constructor.newInstance(block);
            } catch (InstantiationException ex) {
                Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (InvocationTargetException ex) {
                Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (NoSuchMethodException ex) {
                Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
            }
            return null;
        } else {
            return null;
        }
    }

    public void valueChanged(TreeSelectionEvent e) {
        final DefaultMutableTreeNode node = blockTree.getSelectedNode();
        blockEditor = null;
        if (node != null && node.isLeaf()) {
            XBlock block = blockTree.getSelectedBlock();
            if (block.getType().equals(BlockType.ZONE)) {
                splitPaneCfg.setRightComponent(pnlTree);
                return;
            }
            setupAdvancedEditor(block);
            if (!ckBoxAdvancedEditor.isSelected()) {
                blockEditor = getBlockEditor(block);
            }
            if (null != blockEditor) {
                JTabbedPane tabbedPane = new JTabbedPane();
                tabbedPane.add(blockEditor, "User-friendly editor");
                tabbedPane.add(pnlBlock, "Advanced editor");
                splitPaneCfg.setRightComponent(tabbedPane);
            } else {
                splitPaneCfg.setRightComponent(pnlBlock);
            }
        } else {
            splitPaneCfg.setRightComponent(pnlTree);
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

    public void propertyChange(PropertyChangeEvent evt) {
        btnSaveCfgFile.getAction().setEnabled(true);
    }

    private class CreateMapTask extends Task<Object, MapStep> {

        CreateMapTask(Application instance) {
            super(instance);
            wmsMapper.setZoomButtonsVisible(false);
            wmsMapper.setZoomSliderVisible(false);
            btnMapping.getAction().setEnabled(false);
            btnCancelMapping.getAction().setEnabled(true);
        }

        @Override
        protected Object doInBackground() throws Exception {
            for (int i = 0; i < wmsMapper.getIndexMax() - 1; i++) {
                setProgress((float) i / wmsMapper.getIndexMax());
                publish(wmsMapper.getMapStep(i));
                Thread.sleep(500);
            }
            return null;
        }

        @Override
        protected void process(List<MapStep> mapSteps) {
            for (MapStep mapStep : mapSteps) {
                wmsMapper.map(mapStep);
                wmsMapper.screen2File(wmsMapper, mapStep.getCalendar());
            }
        }

        @Override
        protected void finished() {
            wmsMapper.setZoomButtonsVisible(true);
            wmsMapper.setZoomSliderVisible(true);
            btnMapping.getAction().setEnabled(true);
            btnCancelMapping.getAction().setEnabled(false);
        }

        @Override
        protected void succeeded(Object o) {
            firePropertyChange("succeeded", null, null);
            taskPaneMapping.setCollapsed(true);
            taskPaneAnimation.setCollapsed(false);
            getApplication().getContext().getTaskService().execute(new OpenFolderAnimationTask(getApplication(), wmsMapper.getFolder()));
        }
    }

    @Action
    public void cancelMapping() {
        createMapTask.cancel(true);
    }

    @Action
    public void openNcMapping() {
        File file = (null == outputFile)
                ? new File(System.getProperty("user.dir"))
                : outputFile.getParentFile();
        JFileChooser chooser = new JFileChooser(file);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileFilter(new FileNameExtensionFilter("Ichthyop output file (*.nc)", "nc"));
        int returnPath = chooser.showOpenDialog(getFrame());
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            outputFile = chooser.getSelectedFile();
            lblNC.setText(outputFile.getName());
            lblNC.setFont(lblNC.getFont().deriveFont(Font.PLAIN, 12));
            wmsMapper.setFile(outputFile);
            wmsMapper.setVisible(true);
            lblMapping.setVisible(false);
            btnMapping.getAction().setEnabled(true);
            btnCloseNC.getAction().setEnabled(true);
            setMainTitle();
        }
    }

    @Action
    public void closeNetCDF() {
        outputFile = null;
        lblNC.setText(getResourceMap().getString("lblNC.text"));
        lblNC.setFont(lblNC.getFont().deriveFont(Font.PLAIN, 12));
        wmsMapper.setFile(outputFile);
        btnMapping.getAction().setEnabled(false);
        btnCloseNC.getAction().setEnabled(false);
    }

    @Action
    public Task openFolderAnimation() {

        File file = (null == outputFolder)
                ? new File(System.getProperty("user.dir"))
                : outputFolder;
        JFileChooser chooser = new JFileChooser(file);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnPath = chooser.showDialog(getFrame(), "Select folder");
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            return new OpenFolderAnimationTask(getApplication(), chooser.getSelectedFile());
        }
        return null;
    }

    private class OpenFolderAnimationTask extends Task {

        private File folder;
        int nbPNG = 0;

        OpenFolderAnimationTask(Application instance, File folder) {
            super(instance);
            this.folder = folder;
        }

        @Override
        protected Object doInBackground() throws Exception {
            nbPNG = folder.listFiles(new MetaFilenameFilter("*.png")).length;
            if (nbPNG > 0) {
                return null;
            } else {
                throw new NullPointerException("No PNG pictures found in folder " + folder.getAbsolutePath());
            }
        }

        @Override
        protected void succeeded(Object o) {
            firePropertyChange("succeeded", null, null);
            outputFolder = folder;
            lblFolder.setText(outputFolder.getName());
            lblFolder.setFont(lblFolder.getFont().deriveFont(Font.PLAIN, 12));
            replayPanel.setFolder(outputFolder);
            sliderTime.setMaximum(nbPNG - 1);
            setAnimationToolsEnabled(true);
            sliderTime.setValue(0);
            animate(true);
        }

        @Override
        protected void failed(Throwable t) {
            firePropertyChange("failed", null, null);
            lblFolder.setText(IchthyopView.this.getResourceMap().getString("lblFolder.text"));
            lblFolder.setFont(lblFolder.getFont().deriveFont(Font.PLAIN, 12));
            sliderTime.setMaximum(0);
            sliderTime.setValue(0);
            setAnimationToolsEnabled(false);
            setMessage(t.getLocalizedMessage());
            outputFolder = null;
        }

        @Override
        protected void finished() {
            setMainTitle();
        }
    }

    @Action
    public void changeWMS() {
        wmsMapper.setWMS((String) cbBoxWMS.getSelectedItem());
    }

    @Action
    public void exportMaps() {
        getApplication().show(new ExportMapsView(IchthyopApp.getApplication(), replayPanel.getFolder()));
    }

    private void showSimulationPreview() {
        pnlProgress.setVisible(false);
        scrollPaneSimulationUI.setVisible(true);
        getSimulationUI().init();
        getSimulationUI().repaintBackground();
    }

    private void hideSimulationPreview() {
        scrollPaneSimulationUI.setVisible(false);
        if (!taskPaneSimulation.isCollapsed()) {
            pnlProgress.setVisible(true);
        }
    }

    private class SimulationPreviewTask extends Task {

        SimulationPreviewTask(Application instance, boolean isEnabled) {
            super(instance);
        }

        @Override
        protected Object doInBackground() throws Exception {
            if (!isSetup) {
                setMessage("Setting up...");
                getSimulationManager().setup();
            }
            return null;
        }

        @Override
        protected void succeeded(Object obj) {
            firePropertyChange("succeeded", null, null);
            isSetup = true;
            setMessage("Setup [OK]");
            showSimulationPreview();
        }
    }

    @Action
    public void saveAsConfigurationFile() {
        JFileChooser fc = new JFileChooser(getSimulationManager().getConfigurationFile());
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileNameExtensionFilter("Ichthyop configuration file" + " (*.xic)", "xic"));
        fc.setSelectedFile(getSimulationManager().getConfigurationFile());
        int returnVal = fc.showSaveDialog(getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = addExtension(fc.getSelectedFile(), "xic");
            try {
                IOTools.copyFile(getSimulationManager().getConfigurationFile(), file);
                loadConfigurationFile(file);
            } catch (IOException ex) {
                Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private File addExtension(File f, String extension) {

        if (!f.isDirectory() && f.getName().endsWith("." + extension)) {
            return f;
        }
        return new File(f.toString() + "." + extension);
    }

    @Action
    public Task saveConfigurationFile() {
        return new SaveCfgFileTask(getApplication());
    }

    private class SaveCfgFileTask extends Task {

        SaveCfgFileTask(Application instance) {
            super(instance);
            setMessage("Saving " + getSimulationManager().getConfigurationFile().getName() + "...");
        }

        @Override
        protected Object doInBackground() throws Exception {
            getSimulationManager().getParameterManager().cleanup();
            blockTree.writeStructure(getSimulationManager().getParameterManager());
            getSimulationManager().getParameterManager().save();
            getSimulationManager().setConfigurationFile(getSimulationManager().getConfigurationFile());
            return null;
        }

        @Override
        protected void succeeded(Object o) {
            firePropertyChange("succeeded", null, null);
            btnSaveCfgFile.getAction().setEnabled(false);
            hasStructureChanged = false;
            isSetup = false;
            setMessage(getSimulationManager().getConfigurationFile().getName() + " saved.");
        }
    }

    @Action
    public void closeConfigurationFile() {
        if (null == getSimulationManager().getConfigurationFile()) {
            return;
        }
        if (savePending()) {
            int answer = dialogSave();
            if ((answer == JOptionPane.CANCEL_OPTION) || (answer == JOptionPane.CLOSED_OPTION)) {
                return;
            }
        }
        setMessage("Closed " + getSimulationManager().getConfigurationFile().toString());
        getSimulationManager().setConfigurationFile(null);
        lblCfgFile.setText("Configuration file");
        lblCfgFile.setFont(lblCfgFile.getFont().deriveFont(12));
        btnSimulationRun.getAction().setEnabled(false);
        btnSaveAsCfgFile.getAction().setEnabled(false);
        btnSaveCfgFile.getAction().setEnabled(false);
        closeMenuItem.getAction().setEnabled(false);
        pnlConfiguration.setVisible(false);
        lblConfiguration.setVisible(true);
        btnPreview.getAction().setEnabled(false);
        setMainTitle();
    }

    private boolean savePending() {
        return btnSaveCfgFile.isEnabled();
    }

    private int dialogSave() {
        String msg = "Save changes in " + getSimulationManager().getConfigurationFile().getName() + " ?";
        int answer = JOptionPane.showConfirmDialog(getFrame(), msg, "Save before exit", JOptionPane.YES_NO_CANCEL_OPTION);
        switch (answer) {
            case JOptionPane.YES_OPTION:
                btnSaveCfgFile.doClick();
                break;
        }
        return answer;
    }

    @Action
    public void openConfigurationFile() {
        JFileChooser chooser = new JFileChooser(cfgPath);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileFilter(new FileNameExtensionFilter("Ichthyop configuration file" + " (*.xic)", "xic"));
        int returnPath = chooser.showOpenDialog(getFrame());
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            loadConfigurationFile(chooser.getSelectedFile());
        }
    }

    private void loadConfigurationFile(File file) {
        setMessage("Opened " + file.toString());
        logger.info("Opened " + file.toString());
        getFrame().setTitle(getResourceMap().getString("Application.title") + " - " + file.getName());
        lblCfgFile.setText(file.getAbsolutePath());
        lblCfgFile.setFont(lblCfgFile.getFont().deriveFont(Font.PLAIN, 12));
        getSimulationManager().setConfigurationFile(file);
        //getSimulationManager().getZoneManager().loadZones();
        isSetup = false;
        saveAsMenuItem.getAction().setEnabled(true);
        closeMenuItem.getAction().setEnabled(true);
        btnSimulationRun.getAction().setEnabled(true);
        btnPreview.getAction().setEnabled(true);
        setMainTitle();
        getApplication().getContext().getTaskService().execute(new CreateBlockTreeTask(getApplication()));
    }

    @Action
    public void showChartsPopup() {
        //popupCharts.show(btnCharts, 0, btnCharts.getHeight());
    }

    @Action
    public void first() {
        sliderTime.setValue(0);
    }

    @Action
    public void previous() {
        int index = sliderTime.getValue();
        if (index == 0) {
            sliderTime.setValue(sliderTime.getMaximum());
        } else {
            sliderTime.setValue(index - 1);
        }
    }

    @Action
    public void animAction() {
        animate(!animator.isRunning());
    }

    @Action
    public void next() {
        int index = sliderTime.getValue();
        if (index == replayPanel.getIndexMax()) {
            sliderTime.setValue(0);
        } else {
            sliderTime.setValue(index + 1);
        }
    }

    @Action
    public void last() {
        sliderTime.setValue(replayPanel.getIndexMax());
    }

    private void animate(boolean animated) {

        ResourceMap resourceMap = Application.getInstance(IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
        if (animated) {
            //animator.setStartDelay(1000);
            animator.setAcceleration(0.01f);
            btnAnimaction.setEnabled(true);
            btnOpenAnimation.getAction().setEnabled(false);
            animator.start();
            btnAnimaction.setIcon(resourceMap.getIcon("animAction.Action.icon.stop"));
        } else {
            btnAnimaction.setIcon(resourceMap.getIcon("animAction.Action.icon.play"));
            if (progressTimer != null && progressTimer.isRunning()) {
                progressTimer.stop();
                progressBar.setValue(0);
                progressBar.setVisible(false);
            }
            if (animator.isRunning()) {
                animator.stop();
            }
        }
    }

    private void startAccelerationProgress() {
        progressTimer = new Timer(100, new ProgressAction());
        progressTimer.start();
    }

    public void timingEvent(float fraction) {
        float ellpased_time = (fraction * TEN_MINUTES - time) * nbfps;
        if (ellpased_time > 1) {
            time = fraction * TEN_MINUTES;
            next();
        }
    }

    public void begin() {
        nbfps = (Float) animationSpeed.getValue();
        time = 0;
        setMessage(getResourceMap().getString("animAction.started.message") + " " + getResourceMap().getString("animAction.speedup.message"));
        startAccelerationProgress();
    }

    public void end() {
        btnOpenAnimation.getAction().setEnabled(true);
        setMessage(getResourceMap().getString("animAction.stopped.message"));
    }

    public void repeat() {
    }

    private class ProgressAction implements ActionListener {

        float duration;
        int progress;

        ProgressAction() {
            duration = animator.getAcceleration() / 2;
            progressBar.setVisible(true);
            progressBar.setIndeterminate(false);
        }

        public void actionPerformed(ActionEvent e) {
            progress = (int) (100.f * animator.getTimingFraction() / duration);
            progressBar.setValue(progress);
            if (progress >= 100) {
                progressTimer.stop();
                progressBar.setVisible(false);
            }
        }
    }

    private void resetProgressBar() {
        progressBarCurrent.setValue(0);
        lblTimeLeftCurrent.setText(getResourceMap().getString("lblTimeLeftCurrent.text"));
        progressBarGlobal.setValue(0);
        lblTimeLeftGlobal.setText(getResourceMap().getString("lblTimeLeftGlobal.text"));
    }

    @Action
    public void exitApplication() {
        getContext().getActionMap().get("quit").actionPerformed(new ActionEvent(btnExit, 0, null));
    }

    @Action
    public void newConfigurationFile() {
        setMessage("New configuration file - not supported yet.");
    }

    @Action
    public void previewSimulation() {
        if (btnPreview.isSelected()) {
            if (!isSetup) {
                getApplication().getContext().getTaskService().execute(new SimulationPreviewTask(getApplication(), btnPreview.isSelected()));
            } else {
                showSimulationPreview();
            }
        } else {
            hideSimulationPreview();
        }
    }

    private SimulationUI getSimulationUI() {
        return (SimulationUI) pnlSimulationUI;
    }

    @Action
    public Task simulationRun() {
        if (!isRunning) {
            return simulActionTask = new SimulationRunTask(getApplication());
        } else {
            simulActionTask.cancel(true);
            return null;
        }
    }

    private void setMenuEnabled(boolean enabled) {
        openMenuItem.getAction().setEnabled(enabled);
        newMenuItem.getAction().setEnabled(enabled);
        saveAsMenuItem.getAction().setEnabled(enabled);
        closeMenuItem.getAction().setEnabled(enabled);
    }

    @Action
    public void setupProgress() {
        if (getSimulationManager().getNumberOfSimulations() > 1) {
            lblProgressGlobal.setVisible(true);
            progressBarGlobal.setVisible(true);
            lblTimeLeftGlobal.setVisible(true);
        } else {
            lblProgressGlobal.setVisible(false);
            progressBarGlobal.setVisible(false);
            lblTimeLeftGlobal.setVisible(false);
        }

    }

    public class SimulationRunTask extends Task {

        ResourceMap resourceMap = Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
        JLabel lblProgress;
        private boolean bln;

        SimulationRunTask(Application instance) {
            super(instance);
            setMessage("Simulation started");
            setMenuEnabled(false);
            bln = false;
            btnSimulationRun.setIcon(resourceMap.getIcon("simulationRun.Action.icon.stop"));
            btnSimulationRun.setText(resourceMap.getString("simulationRun.Action.text.stop"));
            isRunning = true;
            btnSimulationRun.getAction().setEnabled(false);
            if (btnPreview.isSelected()) {
                btnPreview.doClick();
            }
            btnPreview.getAction().setEnabled(false);
            getSimulationManager().resetId();
        }

        @Override
        protected Object doInBackground() throws Exception {
            getSimulationManager().resetTimerGlobal();
            do {
                setMessage("Simulation " + getSimulationManager().indexSimulationToString());
                setMessage("Initializing...");
                getSimulationManager().setup();
                isSetup = true;
                getSimulationManager().init();
                getSimulationManager().getTimeManager().firstStepTriggered();
                getSimulationManager().resetTimerCurrent();
                do {
                    getSimulationManager().getSimulation().step();
                    setProgress(getSimulationManager().progressCurrent());
                    publish(getSimulationManager().progressCurrent());
                } while (!getSimulationManager().isStopped() && getSimulationManager().getTimeManager().hasNextStep());
            } while (!getSimulationManager().isStopped() && getSimulationManager().hasNextSimulation());
            return null;
        }

        @Override
        protected void failed(Throwable t) {
            firePropertyChange("failed", null, null);
            logger.log(Level.SEVERE, null, t);
        }

        @Override
        protected void process(List values) {
            if (!bln) {
                btnSimulationRun.getAction().setEnabled(true);
                bln = true;
            }
            printProgress();
            //setMessage(getSimulationManager().getTimeManager().stepToString() + " - Time " + getSimulationManager().getTimeManager().timeToString());
        }

        private void printProgress() {

            if (progressBarCurrent.isVisible()) {
                progressBarCurrent.setValue((int) (getSimulationManager().progressCurrent() * 100));
                StringBuffer strBf = new StringBuffer();
                strBf.append(getSimulationManager().getTimeManager().stepToString());
                strBf.append(" - ");
                strBf.append(getSimulationManager().timeLeftCurrent());
                lblTimeLeftCurrent.setText(strBf.toString());
            }

            if (progressBarGlobal.isVisible()) {
                progressBarGlobal.setValue((int) (getSimulationManager().progressGlobal() * 100));
                StringBuffer strBf = new StringBuffer("Simulation ");
                strBf.append(getSimulationManager().indexSimulationToString());
                strBf.append(" - ");
                strBf.append(getSimulationManager().timeLeftGlobal());
                lblTimeLeftGlobal.setText(strBf.toString());
            }
        }

        @Override
        protected void cancelled() {
            getSimulationManager().stop();
            setMessage("Simulation interrupted");
        }

        @Override
        protected void succeeded(Object obj) {
            firePropertyChange("succeeded", null, null);
            setMessage("End of simulation");
            outputFile = new File(getSimulationManager().getOutputManager().getFileLocation());
            lblNC.setText(outputFile.getName());
            lblNC.setFont(lblNC.getFont().deriveFont(Font.PLAIN, 12));
            taskPaneSimulation.setCollapsed(true);
            taskPaneMapping.setCollapsed(false);
        }

        @Override
        protected void finished() {
            btnSimulationRun.setIcon(resourceMap.getIcon("simulationRun.Action.icon.play"));
            btnSimulationRun.setText(resourceMap.getString("simulationRun.Action.text.start"));
            setMenuEnabled(true);
            isRunning = false;
            resetProgressBar();
        }
    }

    private class CreateBlockTreeTask extends Task {

        CreateBlockTreeTask(Application instance) {
            super(instance);
        }

        @Override
        protected Object doInBackground() throws Exception {

            blockTree.createModel();
            return null;
        }

        @Override
        protected void succeeded(Object o) {
            firePropertyChange("succeeded", null, null);
            pnlConfiguration.setVisible(true);
            blockTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            blockTree.addTreeSelectionListener(IchthyopView.this);
            blockTree.setNodeVisible(blockTree.getRoot().getFirstLeaf());
        }

        @Override
        protected void failed(Throwable t) {
            firePropertyChange("failed", null, null);
            logger.log(Level.SEVERE, null, t);
        }
    }

    public void savePreferences() {

        if (null != getSimulationManager().getConfigurationFile()) {
            savePreference(openMenuItem, getSimulationManager().getConfigurationFile().getPath());
            //savePreference(blockTree, blockTree.getSelectedKey());
        } else {
            savePreference(openMenuItem, cfgPath.getPath());
        }

        savePreference(animationSpeed, animationSpeed.getValue());
    }

    private void savePreference(Component bean, Object property) {
        try {
            String filename = beanFilename(bean);
            if (filename != null) {
                getContext().getLocalStorage().save(property, filename);
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    private String beanFilename(Component bean) {
        String name = bean.getName();
        return (name == null) ? null : name + "." + getResourceMap().getString("preferences.filename");
    }

    public void restorePreferences() {
        Object property = restorePreference(openMenuItem);
        if (property != null) {
            File file = new File((String) property);
            if (file.isFile()) {
                cfgPath = file.getParentFile();
                loadConfigurationFile(file);
            } else if (file.isDirectory()) {
                cfgPath = file;
            }
        } else {
            setMessage("Please, open a configuration file or create a new one");
        }

        property = restorePreference(animationSpeed);
        if (property != null) {
            animationSpeed.setValue(property);
        }

        getFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private Object restorePreference(Component bean) {
        try {
            return getContext().getLocalStorage().load(beanFilename(bean));
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            return null;
        }
    }

    public void setMessage(String text) {
        statusMessageLabel.setText((text == null) ? "" : text);
        messageTimer.restart();
        loggerScrollPane.setMessage(text);
    }

    private class MouseWheelScroller implements MouseWheelListener {

        public void mouseWheelMoved(MouseWheelEvent e) {
            int increment = e.getWheelRotation();
            int index = sliderTime.getValue();
            int newIndex = index + increment;
            newIndex = Math.min(sliderTime.getMaximum(), Math.max(0, newIndex));
            sliderTime.setValue(newIndex);
        }
    }

    private class KeyScroller extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {
            int keyCode = e.getKeyCode();
            switch (keyCode) {
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_UP:
                    next();
                    break;
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_DOWN:
                    previous();
                    break;
                case KeyEvent.VK_PAGE_DOWN:
                    first();
                    break;
                case KeyEvent.VK_PAGE_UP:
                    last();
            }
        }
    }

    private void createMainPanel() {

        lblConfiguration = new JLabel(getResourceMap().getIcon("lblConfiguration.icon"));
        lblConfiguration.setHorizontalAlignment(JLabel.CENTER);
        lblConfiguration.setVerticalAlignment(JLabel.CENTER);

        lblMapping = new JLabel(getResourceMap().getIcon("lblMapping.icon"));
        lblMapping.setHorizontalAlignment(JLabel.CENTER);
        lblMapping.setVerticalAlignment(JLabel.CENTER);

        gradientPanel.setLayout(new StackLayout());
        gradientPanel.add(pnlLogo, StackLayout.BOTTOM);
        pnlLogo.setVisible(false);
        gradientPanel.add(lblConfiguration, StackLayout.TOP);
        lblConfiguration.setVisible(true);
        gradientPanel.add(pnlConfiguration);
        pnlConfiguration.setVisible(false);
        gradientPanel.add(pnlProgress, StackLayout.TOP);
        pnlProgress.setVisible(false);
        gradientPanel.add(scrollPaneSimulationUI, StackLayout.TOP);
        scrollPaneSimulationUI.setVisible(false);
        gradientPanel.add(lblMapping, StackLayout.TOP);
        lblMapping.setVisible(false);
        gradientPanel.add(wmsMapper, StackLayout.TOP);
        wmsMapper.setVisible(false);
        gradientPanel.add(replayPanel, StackLayout.TOP);
        replayPanel.setVisible(false);

        taskPaneSimulation.setCollapsed(true);
        taskPaneMapping.setCollapsed(true);
        taskPaneAnimation.setCollapsed(true);
    }

    @Action
    public void browse() {
        try {
            IOTools.browse(URI.create(getResourceMap().getString("Application.homepage")));
            setMessage(getResourceMap().getString("browse.Action.openurl") + " " + getResourceMap().getString("Application.homepage"));
        } catch (IOException ex) {
            setMessage(getResourceMap().getString("browse.Action.no-browser") + " " + getResourceMap().getString("Application.homepage"));
        }
    }

    private void setMainTitle() {

        pnlLogo.setVisible(false);
        if (!taskPaneConfiguration.isCollapsed()) {
            if (null != getSimulationManager().getConfigurationFile()) {
                titledPanelMain.setTitle("Ichthyop - " + taskPaneConfiguration.getTitle() + " - " + lblCfgFile.getText());
            } else {
                titledPanelMain.setTitle("Ichthyop - " + taskPaneConfiguration.getTitle());
            }
        } else if (!taskPaneSimulation.isCollapsed()) {
            if (null != getSimulationManager().getConfigurationFile()) {
                titledPanelMain.setTitle("Ichthyop - " + taskPaneSimulation.getTitle() + " - " + lblCfgFile.getText());
            } else {
                titledPanelMain.setTitle("Ichthyop - " + taskPaneSimulation.getTitle());
            }
        } else if (!taskPaneMapping.isCollapsed()) {
            if (null != outputFile) {
                titledPanelMain.setTitle("Ichthyop - " + taskPaneMapping.getTitle() + " - " + outputFile.getName());
            } else {
                titledPanelMain.setTitle("Ichthyop - " + taskPaneMapping.getTitle());
            }
        } else if (!taskPaneAnimation.isCollapsed()) {
            if (null != outputFolder) {
                titledPanelMain.setTitle("Ichthyop - " + taskPaneAnimation.getTitle() + " - " + outputFolder.getName());
            } else {
                titledPanelMain.setTitle("Ichthyop - " + taskPaneAnimation.getTitle());
            }
        } else {
            titledPanelMain.setTitle("Ichthyop");
            pnlLogo.setVisible(true);
        }
    }

    private ParameterTable getTable() {
        return (ParameterTable) table;
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
    public void addSerialValue() {
        int row = table.getSelectedRow();
        XParameter xparam = blockTree.getSelectedBlock().getXParameter(getTable().getParameterKey(row).toString());
        xparam.addValue();
        getTable().setModel(blockTree.getSelectedBlock(), this);
        btnRemoveValue.getAction().setEnabled(false);
        btnAddValue.getAction().setEnabled(false);
        firePropertyChange("xicfile", null, null);
    }

    @Action
    public void removeSerialValue() {
        int row = table.getSelectedRow();
        XParameter xparam = blockTree.getSelectedBlock().getXParameter(getTable().getParameterKey(row).toString());
        int index = getTable().getParameterIndex(row);
        xparam.removeValue(index);
        getTable().setModel(blockTree.getSelectedBlock(), this);
        btnRemoveValue.getAction().setEnabled(false);
        btnAddValue.getAction().setEnabled(false);
        firePropertyChange("xicfile", null, null);
    }

    class ConfirmExit implements Application.ExitListener {

        public boolean canExit(EventObject e) {
            if (savePending()) {
                int answer = dialogSave();
                return ((answer == JOptionPane.YES_OPTION) || (answer == JOptionPane.NO_OPTION));
            }
            return true;
        }

        public void willExit(EventObject e) {
        }
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        mainPanel = new javax.swing.JPanel();
        splitPane = new javax.swing.JSplitPane();
        leftSplitPane = new javax.swing.JSplitPane();
        titledPanelSteps = new org.jdesktop.swingx.JXTitledPanel();
        taskPaneContainerSteps = new org.jdesktop.swingx.JXTaskPaneContainer();
        taskPaneConfiguration = new org.jdesktop.swingx.JXTaskPane();
        pnlFile = new javax.swing.JPanel();
        lblCfgFile = new javax.swing.JLabel();
        btnNewCfgFile = new javax.swing.JButton();
        btnOpenCfgFile = new javax.swing.JButton();
        btnSaveCfgFile = new javax.swing.JButton();
        btnSaveAsCfgFile = new javax.swing.JButton();
        btnCloseCfgFile = new javax.swing.JButton();
        ckBoxAdvancedEditor = new javax.swing.JCheckBox();
        taskPaneSimulation = new org.jdesktop.swingx.JXTaskPane();
        pnlSimulation = new javax.swing.JPanel();
        btnPreview = new javax.swing.JToggleButton();
        btnSimulationRun = new javax.swing.JButton();
        taskPaneMapping = new org.jdesktop.swingx.JXTaskPane();
        pnlMapping = new javax.swing.JPanel();
        btnMapping = new javax.swing.JButton();
        btnCancelMapping = new javax.swing.JButton();
        btnOpenNC = new javax.swing.JButton();
        pnlWMS = new javax.swing.JPanel();
        cbBoxWMS = new javax.swing.JComboBox();
        lblWMS = new javax.swing.JLabel();
        lblNC = new javax.swing.JLabel();
        btnCloseNC = new javax.swing.JButton();
        taskPaneAnimation = new org.jdesktop.swingx.JXTaskPane();
        pnlAnimation = new javax.swing.JPanel();
        btnFirst = new javax.swing.JButton();
        btnPrevious = new javax.swing.JButton();
        btnAnimaction = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnLast = new javax.swing.JButton();
        lblFramePerSecond = new javax.swing.JLabel();
        animationSpeed = new javax.swing.JSpinner();
        btnDeleteMaps = new javax.swing.JButton();
        btnExportMaps = new javax.swing.JButton();
        lblAnimationSpeed = new javax.swing.JLabel();
        btnOpenAnimation = new javax.swing.JButton();
        lblFolder = new javax.swing.JLabel();
        sliderTime = new javax.swing.JSlider();
        lblTime = new javax.swing.JLabel();
        titledPanelLogger = new org.jdesktop.swingx.JXTitledPanel();
        loggerScrollPane = new org.previmer.ichthyop.ui.LoggerScrollPane();
        titledPanelMain = new org.jdesktop.swingx.JXTitledPanel();
        gradientPanel = new org.previmer.ichthyop.ui.GradientPanel();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu configurationMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        saveAsMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        simulationMenu = new javax.swing.JMenu();
        simulactionMenuItem = new javax.swing.JMenuItem();
        previewMenuItem = new javax.swing.JMenuItem();
        mappingMenu = new javax.swing.JMenu();
        mapMenuItem = new javax.swing.JMenuItem();
        cancelMapMenuItem = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        openNCMenuItem = new javax.swing.JMenuItem();
        animationMenu = new javax.swing.JMenu();
        animactionMenuItem = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        openAnimationMenuItem = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        exportMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        popupCharts = new javax.swing.JPopupMenu();
        itemDepthChart = new javax.swing.JCheckBoxMenuItem();
        itemEdgeChart = new javax.swing.JCheckBoxMenuItem();
        itemRecruitChart = new javax.swing.JCheckBoxMenuItem();
        itemLengthChart = new javax.swing.JCheckBoxMenuItem();
        itemDeadChart = new javax.swing.JCheckBoxMenuItem();
        itemStageChart = new javax.swing.JCheckBoxMenuItem();
        scrollPaneSimulationUI = new javax.swing.JScrollPane();
        pnlSimulationUI = new SimulationUI();
        pnlConfiguration = new javax.swing.JPanel();
        splitPaneCfg = new javax.swing.JSplitPane();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        blockTree = new org.previmer.ichthyop.ui.BlockTree();
        jToolBar1 = new javax.swing.JToolBar();
        btnExpand = new javax.swing.JButton();
        btnCollapse = new javax.swing.JButton();
        btnUpper = new javax.swing.JButton();
        btnLower = new javax.swing.JButton();
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
        jScrollPane2 = new javax.swing.JScrollPane();
        table = new ParameterTable();
        pnlProgress = new javax.swing.JPanel();
        lblProgressCurrent = new javax.swing.JLabel();
        progressBarCurrent = new javax.swing.JProgressBar();
        lblTimeLeftCurrent = new javax.swing.JLabel();
        lblProgressGlobal = new javax.swing.JLabel();
        progressBarGlobal = new javax.swing.JProgressBar();
        lblTimeLeftGlobal = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnExit = new javax.swing.JButton();
        pnlTree = new javax.swing.JPanel();
        lblSelectBlock = new javax.swing.JLabel();
        pnlLogo = new org.jdesktop.swingx.JXPanel();
        hyperLinkLogo = new org.jdesktop.swingx.JXHyperlink();
        statusBar = new org.jdesktop.swingx.JXStatusBar();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new org.jdesktop.swingx.JXBusyLabel();
        pnlProgressBar = new javax.swing.JPanel();
        progressBar = new javax.swing.JProgressBar();
        lblFlag = new javax.swing.JLabel();

        mainPanel.setName("mainPanel"); // NOI18N

        splitPane.setName("splitPane"); // NOI18N

        leftSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setName("leftSplitPane"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
        titledPanelSteps.setTitle(resourceMap.getString("titledPanelSteps.title")); // NOI18N
        titledPanelSteps.setName("titledPanelSteps"); // NOI18N
        titledPanelSteps.getContentContainer().setLayout(new org.netbeans.lib.awtextra.AbsoluteLayout());

        taskPaneContainerSteps.setOpaque(false);
        taskPaneContainerSteps.setName("taskPaneContainerSteps"); // NOI18N
        taskPaneContainerSteps.setLayout(new java.awt.GridBagLayout());

        taskPaneConfiguration.setAnimated(false);
        taskPaneConfiguration.setIcon(resourceMap.getIcon("step.Configuration.icon")); // NOI18N
        taskPaneConfiguration.setTitle(resourceMap.getString("step.Configuration.text")); // NOI18N
        taskPaneConfiguration.setName("taskPaneConfiguration"); // NOI18N
        taskPaneConfiguration.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                taskPaneConfigurationPropertyChange(evt);
            }
        });

        pnlFile.setName("pnlFile"); // NOI18N
        pnlFile.setOpaque(false);

        lblCfgFile.setName("lblCfgFile"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getActionMap(IchthyopView.class, this);
        btnNewCfgFile.setAction(actionMap.get("newConfigurationFile")); // NOI18N
        btnNewCfgFile.setFocusable(false);
        btnNewCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnNewCfgFile.setName("btnNewCfgFile"); // NOI18N
        btnNewCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnOpenCfgFile.setAction(actionMap.get("openConfigurationFile")); // NOI18N
        btnOpenCfgFile.setFocusable(false);
        btnOpenCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnOpenCfgFile.setName("btnOpenCfgFile"); // NOI18N
        btnOpenCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnSaveCfgFile.setAction(actionMap.get("saveConfigurationFile")); // NOI18N
        btnSaveCfgFile.setFocusable(false);
        btnSaveCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnSaveCfgFile.setName("btnSaveCfgFile"); // NOI18N
        btnSaveCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnSaveCfgFile.getAction().setEnabled(false);

        btnSaveAsCfgFile.setAction(actionMap.get("saveAsConfigurationFile")); // NOI18N
        btnSaveAsCfgFile.setFocusable(false);
        btnSaveAsCfgFile.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        btnSaveAsCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnSaveAsCfgFile.setName("btnSaveAsCfgFile"); // NOI18N
        btnSaveAsCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnCloseCfgFile.setAction(actionMap.get("closeConfigurationFile")); // NOI18N
        btnCloseCfgFile.setFocusable(false);
        btnCloseCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnCloseCfgFile.setName("btnCloseCfgFile"); // NOI18N
        btnCloseCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        ckBoxAdvancedEditor.setAction(actionMap.get("reloadEditor")); // NOI18N
        ckBoxAdvancedEditor.setText(resourceMap.getString("ckBoxAdvancedEditor.text")); // NOI18N
        ckBoxAdvancedEditor.setName("ckBoxAdvancedEditor"); // NOI18N

        javax.swing.GroupLayout pnlFileLayout = new javax.swing.GroupLayout(pnlFile);
        pnlFile.setLayout(pnlFileLayout);
        pnlFileLayout.setHorizontalGroup(
            pnlFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFileLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCfgFile, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                    .addGroup(pnlFileLayout.createSequentialGroup()
                        .addComponent(btnNewCfgFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOpenCfgFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCloseCfgFile))
                    .addGroup(pnlFileLayout.createSequentialGroup()
                        .addComponent(btnSaveCfgFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSaveAsCfgFile))
                    .addComponent(ckBoxAdvancedEditor))
                .addContainerGap())
        );
        pnlFileLayout.setVerticalGroup(
            pnlFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFileLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnOpenCfgFile)
                    .addComponent(btnNewCfgFile)
                    .addComponent(btnCloseCfgFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnSaveAsCfgFile)
                    .addComponent(btnSaveCfgFile))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCfgFile)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ckBoxAdvancedEditor)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taskPaneConfiguration.getContentPane().add(pnlFile);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 100.0;
        taskPaneContainerSteps.add(taskPaneConfiguration, gridBagConstraints);

        taskPaneSimulation.setAnimated(false);
        taskPaneSimulation.setIcon(resourceMap.getIcon("step.Simulation.icon")); // NOI18N
        taskPaneSimulation.setTitle(resourceMap.getString("step.Simulation.text")); // NOI18N
        taskPaneSimulation.setName("taskPaneSimulation"); // NOI18N
        taskPaneSimulation.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                taskPaneSimulationPropertyChange(evt);
            }
        });

        pnlSimulation.setName("pnlSimulation"); // NOI18N
        pnlSimulation.setOpaque(false);

        btnPreview.setAction(actionMap.get("previewSimulation")); // NOI18N
        btnPreview.setFocusable(false);
        btnPreview.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnPreview.setName("btnPreview"); // NOI18N
        btnPreview.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnSimulationRun.setAction(actionMap.get("simulationRun")); // NOI18N
        btnSimulationRun.setFocusable(false);
        btnSimulationRun.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnSimulationRun.setName("btnSimulationRun"); // NOI18N
        btnSimulationRun.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout pnlSimulationLayout = new javax.swing.GroupLayout(pnlSimulation);
        pnlSimulation.setLayout(pnlSimulationLayout);
        pnlSimulationLayout.setHorizontalGroup(
            pnlSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSimulationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnSimulationRun)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPreview)
                .addContainerGap(135, Short.MAX_VALUE))
        );
        pnlSimulationLayout.setVerticalGroup(
            pnlSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSimulationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnPreview)
                    .addComponent(btnSimulationRun))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taskPaneSimulation.getContentPane().add(pnlSimulation);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 100.0;
        taskPaneContainerSteps.add(taskPaneSimulation, gridBagConstraints);

        taskPaneMapping.setAnimated(false);
        taskPaneMapping.setIcon(resourceMap.getIcon("step.Mapping.icon")); // NOI18N
        taskPaneMapping.setTitle(resourceMap.getString("step.Mapping.text")); // NOI18N
        taskPaneMapping.setName("taskPaneMapping"); // NOI18N
        taskPaneMapping.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                taskPaneMappingPropertyChange(evt);
            }
        });

        pnlMapping.setName("pnlMapping"); // NOI18N
        pnlMapping.setOpaque(false);

        btnMapping.setAction(actionMap.get("createMaps")); // NOI18N
        btnMapping.setName("btnMapping"); // NOI18N

        btnCancelMapping.setAction(actionMap.get("cancelMapping")); // NOI18N
        btnCancelMapping.setName("btnCancelMapping"); // NOI18N

        btnOpenNC.setAction(actionMap.get("openNcMapping")); // NOI18N
        btnOpenNC.setName("btnOpenNC"); // NOI18N

        pnlWMS.setBorder(javax.swing.BorderFactory.createTitledBorder("Web Map Service"));
        pnlWMS.setName("pnlWMS"); // NOI18N
        pnlWMS.setOpaque(false);

        cbBoxWMS.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "http://www.marine-geo.org/services/wms?", "http://wms.jpl.nasa.gov/wms.cgi?", "http://www2.demis.nl/wms/wms.asp?wms=WorldMap&" }));
        cbBoxWMS.setAction(actionMap.get("changeWMS")); // NOI18N
        cbBoxWMS.setName("cbBoxWMS"); // NOI18N

        lblWMS.setName("lblWMS"); // NOI18N

        javax.swing.GroupLayout pnlWMSLayout = new javax.swing.GroupLayout(pnlWMS);
        pnlWMS.setLayout(pnlWMSLayout);
        pnlWMSLayout.setHorizontalGroup(
            pnlWMSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlWMSLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblWMS)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(cbBoxWMS, 0, 372, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlWMSLayout.setVerticalGroup(
            pnlWMSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlWMSLayout.createSequentialGroup()
                .addGroup(pnlWMSLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblWMS)
                    .addComponent(cbBoxWMS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblNC.setFont(resourceMap.getFont("lblNC.font")); // NOI18N
        lblNC.setText(resourceMap.getString("lblNC.text")); // NOI18N
        lblNC.setName("lblNC"); // NOI18N

        btnCloseNC.setAction(actionMap.get("closeNetCDF")); // NOI18N
        btnCloseNC.setName("btnCloseNC"); // NOI18N

        javax.swing.GroupLayout pnlMappingLayout = new javax.swing.GroupLayout(pnlMapping);
        pnlMapping.setLayout(pnlMappingLayout);
        pnlMappingLayout.setHorizontalGroup(
            pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMappingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlMappingLayout.createSequentialGroup()
                        .addComponent(btnMapping)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelMapping))
                    .addComponent(pnlWMS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlMappingLayout.createSequentialGroup()
                        .addComponent(btnOpenNC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCloseNC))
                    .addComponent(lblNC))
                .addContainerGap())
        );
        pnlMappingLayout.setVerticalGroup(
            pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMappingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMapping)
                    .addComponent(btnCancelMapping))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlWMS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOpenNC)
                    .addComponent(btnCloseNC))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNC)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taskPaneMapping.getContentPane().add(pnlMapping);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 100.0;
        taskPaneContainerSteps.add(taskPaneMapping, gridBagConstraints);

        taskPaneAnimation.setAnimated(false);
        taskPaneAnimation.setIcon(resourceMap.getIcon("step.Animation.icon")); // NOI18N
        taskPaneAnimation.setTitle(resourceMap.getString("step.Animation.text")); // NOI18N
        taskPaneAnimation.setName("taskPaneAnimation"); // NOI18N
        taskPaneAnimation.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                taskPaneAnimationPropertyChange(evt);
            }
        });

        pnlAnimation.setName("pnlAnimation"); // NOI18N
        pnlAnimation.setOpaque(false);

        btnFirst.setAction(actionMap.get("first")); // NOI18N
        btnFirst.setFocusable(false);
        btnFirst.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFirst.setName("btnFirst"); // NOI18N
        btnFirst.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnPrevious.setAction(actionMap.get("previous")); // NOI18N
        btnPrevious.setFocusable(false);
        btnPrevious.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPrevious.setName("btnPrevious"); // NOI18N
        btnPrevious.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnAnimaction.setAction(actionMap.get("animAction")); // NOI18N
        btnAnimaction.setText(resourceMap.getString("btnAnimaction.text")); // NOI18N
        btnAnimaction.setFocusable(false);
        btnAnimaction.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAnimaction.setName("btnAnimaction"); // NOI18N
        btnAnimaction.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnNext.setAction(actionMap.get("next")); // NOI18N
        btnNext.setFocusable(false);
        btnNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNext.setName("btnNext"); // NOI18N
        btnNext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnLast.setAction(actionMap.get("last")); // NOI18N
        btnLast.setFocusable(false);
        btnLast.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLast.setName("btnLast"); // NOI18N
        btnLast.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        lblFramePerSecond.setText(resourceMap.getString("lblFramePerSecond.text")); // NOI18N
        lblFramePerSecond.setToolTipText(resourceMap.getString("lblFramePerSecond.toolTipText")); // NOI18N
        lblFramePerSecond.setName("lblFramePerSecond"); // NOI18N
        lblFramePerSecond.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFramePerSecondMouseClicked(evt);
            }
        });

        animationSpeed.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.5f), Float.valueOf(0.5f), Float.valueOf(24.0f), Float.valueOf(0.1f)));
        animationSpeed.setToolTipText(resourceMap.getString("animationSpeed.toolTipText")); // NOI18N
        animationSpeed.setFocusable(false);
        animationSpeed.setMaximumSize(new java.awt.Dimension(77, 30));
        animationSpeed.setName("animationSpeed"); // NOI18N
        animationSpeed.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                animationSpeedStateChanged(evt);
            }
        });

        btnDeleteMaps.setAction(actionMap.get("deleteMaps")); // NOI18N
        btnDeleteMaps.setFocusable(false);
        btnDeleteMaps.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnDeleteMaps.setName("btnDeleteMaps"); // NOI18N
        btnDeleteMaps.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnExportMaps.setAction(actionMap.get("exportMaps")); // NOI18N
        btnExportMaps.setFocusable(false);
        btnExportMaps.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnExportMaps.setName("btnExportMaps"); // NOI18N
        btnExportMaps.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        lblAnimationSpeed.setText(resourceMap.getString("lblAnimationSpeed.text")); // NOI18N
        lblAnimationSpeed.setName("lblAnimationSpeed"); // NOI18N

        btnOpenAnimation.setAction(actionMap.get("openFolderAnimation")); // NOI18N
        btnOpenAnimation.setName("btnOpenAnimation"); // NOI18N

        lblFolder.setFont(resourceMap.getFont("lblFolder.font")); // NOI18N
        lblFolder.setText(resourceMap.getString("lblFolder.text")); // NOI18N
        lblFolder.setName("lblFolder"); // NOI18N

        sliderTime.setValue(0);
        sliderTime.setName("sliderTime"); // NOI18N
        sliderTime.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                sliderTimeStateChanged(evt);
            }
        });

        lblTime.setFont(resourceMap.getFont("lblTime.font")); // NOI18N
        lblTime.setText(resourceMap.getString("lblTime.text")); // NOI18N
        lblTime.setName("lblTime"); // NOI18N

        javax.swing.GroupLayout pnlAnimationLayout = new javax.swing.GroupLayout(pnlAnimation);
        pnlAnimation.setLayout(pnlAnimationLayout);
        pnlAnimationLayout.setHorizontalGroup(
            pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAnimationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sliderTime, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                    .addComponent(lblTime)
                    .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addComponent(btnFirst)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnPrevious)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAnimaction)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnNext)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnLast))
                    .addComponent(lblFolder)
                    .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addComponent(lblAnimationSpeed)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(animationSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFramePerSecond))
                    .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addComponent(btnOpenAnimation)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExportMaps)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteMaps)))
                .addContainerGap())
        );
        pnlAnimationLayout.setVerticalGroup(
            pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAnimationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnLast)
                    .addComponent(btnNext)
                    .addComponent(btnAnimaction)
                    .addComponent(btnPrevious)
                    .addComponent(btnFirst))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblTime)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(sliderTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(btnExportMaps)
                    .addComponent(btnOpenAnimation)
                    .addComponent(btnDeleteMaps))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblFolder)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAnimationSpeed)
                    .addComponent(animationSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFramePerSecond))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taskPaneAnimation.getContentPane().add(pnlAnimation);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 100.0;
        taskPaneContainerSteps.add(taskPaneAnimation, gridBagConstraints);

        titledPanelSteps.getContentContainer().add(taskPaneContainerSteps, new org.netbeans.lib.awtextra.AbsoluteConstraints(0, 0, -1, -1));

        leftSplitPane.setLeftComponent(titledPanelSteps);

        titledPanelLogger.setTitle(resourceMap.getString("titledPanelLogger.title")); // NOI18N
        titledPanelLogger.setName("titledPanelLogger"); // NOI18N

        loggerScrollPane.setName("loggerScrollPane"); // NOI18N

        javax.swing.GroupLayout titledPanelLoggerLayout = new javax.swing.GroupLayout(titledPanelLogger.getContentContainer());
        titledPanelLogger.getContentContainer().setLayout(titledPanelLoggerLayout);
        titledPanelLoggerLayout.setHorizontalGroup(
            titledPanelLoggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loggerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
        );
        titledPanelLoggerLayout.setVerticalGroup(
            titledPanelLoggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loggerScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 23, Short.MAX_VALUE)
        );

        leftSplitPane.setRightComponent(titledPanelLogger);

        splitPane.setLeftComponent(leftSplitPane);

        titledPanelMain.setTitle(resourceMap.getString("titledPanelMain.title")); // NOI18N
        titledPanelMain.setName("titledPanelMain"); // NOI18N

        gradientPanel.setName("gradientPanel"); // NOI18N

        javax.swing.GroupLayout gradientPanelLayout = new javax.swing.GroupLayout(gradientPanel);
        gradientPanel.setLayout(gradientPanelLayout);
        gradientPanelLayout.setHorizontalGroup(
            gradientPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 348, Short.MAX_VALUE)
        );
        gradientPanelLayout.setVerticalGroup(
            gradientPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 503, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout titledPanelMainLayout = new javax.swing.GroupLayout(titledPanelMain.getContentContainer());
        titledPanelMain.getContentContainer().setLayout(titledPanelMainLayout);
        titledPanelMainLayout.setHorizontalGroup(
            titledPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gradientPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        titledPanelMainLayout.setVerticalGroup(
            titledPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(gradientPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        createMainPanel();

        splitPane.setRightComponent(titledPanelMain);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 848, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.PREFERRED_SIZE, 535, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        configurationMenu.setText(resourceMap.getString("configurationMenu.text")); // NOI18N
        configurationMenu.setName("configurationMenu"); // NOI18N

        newMenuItem.setAction(actionMap.get("newConfigurationFile")); // NOI18N
        newMenuItem.setName("newMenuItem"); // NOI18N
        configurationMenu.add(newMenuItem);

        openMenuItem.setAction(actionMap.get("openConfigurationFile")); // NOI18N
        openMenuItem.setName("openMenuItem"); // NOI18N
        configurationMenu.add(openMenuItem);

        closeMenuItem.setAction(actionMap.get("closeConfigurationFile")); // NOI18N
        closeMenuItem.setName("closeMenuItem"); // NOI18N
        configurationMenu.add(closeMenuItem);

        jSeparator2.setName("jSeparator2"); // NOI18N
        configurationMenu.add(jSeparator2);

        saveMenuItem.setAction(actionMap.get("saveConfigurationFile")); // NOI18N
        saveMenuItem.setName("saveMenuItem"); // NOI18N
        configurationMenu.add(saveMenuItem);

        saveAsMenuItem.setAction(actionMap.get("saveAsConfigurationFile")); // NOI18N
        saveAsMenuItem.setName("saveAsMenuItem"); // NOI18N
        configurationMenu.add(saveAsMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        configurationMenu.add(jSeparator1);

        exitMenuItem.setAction(actionMap.get("exitApplication")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        configurationMenu.add(exitMenuItem);

        menuBar.add(configurationMenu);

        simulationMenu.setText(resourceMap.getString("simulationMenu.text")); // NOI18N
        simulationMenu.setName("simulationMenu"); // NOI18N

        simulactionMenuItem.setAction(actionMap.get("simulationRun")); // NOI18N
        simulactionMenuItem.setName("simulactionMenuItem"); // NOI18N
        simulationMenu.add(simulactionMenuItem);

        previewMenuItem.setAction(actionMap.get("previewSimulation")); // NOI18N
        previewMenuItem.setName("previewMenuItem"); // NOI18N
        simulationMenu.add(previewMenuItem);

        menuBar.add(simulationMenu);

        mappingMenu.setText(resourceMap.getString("mappingMenu.text")); // NOI18N
        mappingMenu.setName("mappingMenu"); // NOI18N

        mapMenuItem.setAction(actionMap.get("createMaps")); // NOI18N
        mapMenuItem.setName("mapMenuItem"); // NOI18N
        mappingMenu.add(mapMenuItem);

        cancelMapMenuItem.setAction(actionMap.get("cancelMapping")); // NOI18N
        cancelMapMenuItem.setName("cancelMapMenuItem"); // NOI18N
        mappingMenu.add(cancelMapMenuItem);

        jSeparator13.setName("jSeparator13"); // NOI18N
        mappingMenu.add(jSeparator13);

        openNCMenuItem.setAction(actionMap.get("openNcMapping")); // NOI18N
        openNCMenuItem.setName("openNCMenuItem"); // NOI18N
        mappingMenu.add(openNCMenuItem);

        menuBar.add(mappingMenu);

        animationMenu.setText(resourceMap.getString("animationMenu.text")); // NOI18N
        animationMenu.setName("animationMenu"); // NOI18N

        animactionMenuItem.setAction(actionMap.get("animAction")); // NOI18N
        animactionMenuItem.setName("animactionMenuItem"); // NOI18N
        animationMenu.add(animactionMenuItem);

        jSeparator15.setName("jSeparator15"); // NOI18N
        animationMenu.add(jSeparator15);

        openAnimationMenuItem.setAction(actionMap.get("openFolderAnimation")); // NOI18N
        openAnimationMenuItem.setName("openAnimationMenuItem"); // NOI18N
        animationMenu.add(openAnimationMenuItem);

        jSeparator14.setName("jSeparator14"); // NOI18N
        animationMenu.add(jSeparator14);

        exportMenuItem.setAction(actionMap.get("exportMaps")); // NOI18N
        exportMenuItem.setName("exportMenuItem"); // NOI18N
        animationMenu.add(exportMenuItem);

        deleteMenuItem.setAction(actionMap.get("deleteMaps")); // NOI18N
        deleteMenuItem.setName("deleteMenuItem"); // NOI18N
        animationMenu.add(deleteMenuItem);

        menuBar.add(animationMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        popupCharts.setName("popupCharts"); // NOI18N

        itemDepthChart.setText(resourceMap.getString("itemDepthChart.text")); // NOI18N
        itemDepthChart.setName("itemDepthChart"); // NOI18N
        popupCharts.add(itemDepthChart);

        itemEdgeChart.setText(resourceMap.getString("itemEdgeChart.text")); // NOI18N
        itemEdgeChart.setName("itemEdgeChart"); // NOI18N
        popupCharts.add(itemEdgeChart);

        itemRecruitChart.setText(resourceMap.getString("itemRecruitChart.text")); // NOI18N
        itemRecruitChart.setName("itemRecruitChart"); // NOI18N
        popupCharts.add(itemRecruitChart);

        itemLengthChart.setText(resourceMap.getString("itemLengthChart.text")); // NOI18N
        itemLengthChart.setName("itemLengthChart"); // NOI18N
        popupCharts.add(itemLengthChart);

        itemDeadChart.setText(resourceMap.getString("itemDeadChart.text")); // NOI18N
        itemDeadChart.setName("itemDeadChart"); // NOI18N
        popupCharts.add(itemDeadChart);

        itemStageChart.setText(resourceMap.getString("itemStageChart.text")); // NOI18N
        itemStageChart.setName("itemStageChart"); // NOI18N
        popupCharts.add(itemStageChart);

        scrollPaneSimulationUI.setBorder(null);
        scrollPaneSimulationUI.setName("scrollPaneSimulationUI"); // NOI18N
        scrollPaneSimulationUI.setPreferredSize(new java.awt.Dimension(500, 500));

        pnlSimulationUI.setBorder(null);
        pnlSimulationUI.setName("pnlSimulationUI"); // NOI18N
        pnlSimulationUI.setPreferredSize(new java.awt.Dimension(500, 500));

        javax.swing.GroupLayout pnlSimulationUILayout = new javax.swing.GroupLayout(pnlSimulationUI);
        pnlSimulationUI.setLayout(pnlSimulationUILayout);
        pnlSimulationUILayout.setHorizontalGroup(
            pnlSimulationUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );
        pnlSimulationUILayout.setVerticalGroup(
            pnlSimulationUILayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 500, Short.MAX_VALUE)
        );

        scrollPaneSimulationUI.setViewportView(pnlSimulationUI);

        pnlConfiguration.setName("pnlConfiguration"); // NOI18N

        splitPaneCfg.setDividerLocation(250);
        splitPaneCfg.setName("splitPaneCfg"); // NOI18N

        jPanel1.setName("jPanel1"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        blockTree.setName("blockTree"); // NOI18N
        blockTree.setRootVisible(true);
        jScrollPane1.setViewportView(blockTree);

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 190, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 590, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        splitPaneCfg.setLeftComponent(jPanel1);

        pnlBlock.setName("pnlBlock"); // NOI18N

        pnlBlockInfo.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlBlockInfo.border.title"))); // NOI18N
        pnlBlockInfo.setName("pnlBlockInfo"); // NOI18N

        lblBlockInfo.setText(resourceMap.getString("lblBlockInfo.text")); // NOI18N
        lblBlockInfo.setName("lblBlockInfo"); // NOI18N

        ckBoxBlock.setAction(actionMap.get("setBlockEnabled")); // NOI18N
        ckBoxBlock.setText(resourceMap.getString("ckBoxBlock.text")); // NOI18N
        ckBoxBlock.setName("ckBoxBlock"); // NOI18N

        javax.swing.GroupLayout pnlBlockInfoLayout = new javax.swing.GroupLayout(pnlBlockInfo);
        pnlBlockInfo.setLayout(pnlBlockInfoLayout);
        pnlBlockInfoLayout.setHorizontalGroup(
            pnlBlockInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlBlockInfoLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBlockInfoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblBlockInfo, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE)
                    .addComponent(ckBoxBlock))
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

        pnlParamDescription.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlParamDescription.border.title"))); // NOI18N
        pnlParamDescription.setName("pnlParamDescription"); // NOI18N

        lblParameter.setText(resourceMap.getString("lblParameter.text")); // NOI18N
        lblParameter.setName("lblParameter"); // NOI18N

        javax.swing.GroupLayout pnlParamDescriptionLayout = new javax.swing.GroupLayout(pnlParamDescription);
        pnlParamDescription.setLayout(pnlParamDescriptionLayout);
        pnlParamDescriptionLayout.setHorizontalGroup(
            pnlParamDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParamDescriptionLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(lblParameter, javax.swing.GroupLayout.DEFAULT_SIZE, 566, Short.MAX_VALUE)
                .addContainerGap())
        );
        pnlParamDescriptionLayout.setVerticalGroup(
            pnlParamDescriptionLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParamDescriptionLayout.createSequentialGroup()
                .addComponent(lblParameter, javax.swing.GroupLayout.DEFAULT_SIZE, 45, Short.MAX_VALUE)
                .addContainerGap())
        );

        btnHiddenParameter.setAction(actionMap.get("showHiddenParameters")); // NOI18N
        btnHiddenParameter.setClickedColor(resourceMap.getColor("btnHiddenParameter.clickedColor")); // NOI18N
        btnHiddenParameter.setUnclickedColor(resourceMap.getColor("btnHiddenParameter.unclickedColor")); // NOI18N
        btnHiddenParameter.setFont(resourceMap.getFont("btnHiddenParameter.font")); // NOI18N
        btnHiddenParameter.setName("btnHiddenParameter"); // NOI18N

        btnUndo.setAction(actionMap.get("undo")); // NOI18N
        btnUndo.setFont(resourceMap.getFont("btnUndo.font")); // NOI18N
        btnUndo.setName("btnUndo"); // NOI18N

        btnRedo.setAction(actionMap.get("redo")); // NOI18N
        btnRedo.setFont(resourceMap.getFont("btnRedo.font")); // NOI18N
        btnRedo.setName("btnRedo"); // NOI18N

        btnAddValue.setAction(actionMap.get("addSerialValue")); // NOI18N
        btnAddValue.setFont(resourceMap.getFont("btnAddValue.font")); // NOI18N
        btnAddValue.setName("btnAddValue"); // NOI18N

        btnRemoveValue.setAction(actionMap.get("removeSerialValue")); // NOI18N
        btnRemoveValue.setFont(resourceMap.getFont("btnRemoveValue.font")); // NOI18N
        btnRemoveValue.setName("btnRemoveValue"); // NOI18N

        jScrollPane2.setName("jScrollPane2"); // NOI18N

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
        jScrollPane2.setViewportView(table);

        javax.swing.GroupLayout pnlParametersLayout = new javax.swing.GroupLayout(pnlParameters);
        pnlParameters.setLayout(pnlParametersLayout);
        pnlParametersLayout.setHorizontalGroup(
            pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParametersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlParamDescription, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlParametersLayout.createSequentialGroup()
                        .addComponent(btnUndo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRedo)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnAddValue)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnRemoveValue))
                    .addComponent(btnHiddenParameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 602, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlParametersLayout.setVerticalGroup(
            pnlParametersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlParametersLayout.createSequentialGroup()
                .addComponent(btnHiddenParameter, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 312, javax.swing.GroupLayout.PREFERRED_SIZE)
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
            .addGroup(pnlBlockLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlBlockLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(pnlBlockInfo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pnlParameters, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap(47, Short.MAX_VALUE))
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

        splitPaneCfg.setRightComponent(pnlBlock);

        javax.swing.GroupLayout pnlConfigurationLayout = new javax.swing.GroupLayout(pnlConfiguration);
        pnlConfiguration.setLayout(pnlConfigurationLayout);
        pnlConfigurationLayout.setHorizontalGroup(
            pnlConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPaneCfg, javax.swing.GroupLayout.DEFAULT_SIZE, 905, Short.MAX_VALUE)
        );
        pnlConfigurationLayout.setVerticalGroup(
            pnlConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPaneCfg, javax.swing.GroupLayout.Alignment.TRAILING)
        );

        pnlProgress.setBackground(new Color(0,0,0,0));
        pnlProgress.setName("pnlProgress"); // NOI18N
        pnlProgress.setOpaque(false);
        pnlProgress.setPreferredSize(new java.awt.Dimension(600, 100));

        lblProgressCurrent.setForeground(java.awt.Color.white);
        lblProgressCurrent.setText(resourceMap.getString("lblProgressCurrent.text")); // NOI18N
        lblProgressCurrent.setName("lblProgressCurrent"); // NOI18N

        progressBarCurrent.setName("progressBarCurrent"); // NOI18N
        progressBarCurrent.setStringPainted(true);

        lblTimeLeftCurrent.setForeground(resourceMap.getColor("lblTimeLeftCurrent.foreground")); // NOI18N
        lblTimeLeftCurrent.setText(resourceMap.getString("lblTimeLeftCurrent.text")); // NOI18N
        lblTimeLeftCurrent.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lblTimeLeftCurrent.setName("lblTimeLeftCurrent"); // NOI18N

        lblProgressGlobal.setForeground(resourceMap.getColor("lblProgressGlobal.foreground")); // NOI18N
        lblProgressGlobal.setText(resourceMap.getString("lblProgressGlobal.text")); // NOI18N
        lblProgressGlobal.setName("lblProgressGlobal"); // NOI18N

        progressBarGlobal.setName("progressBarGlobal"); // NOI18N
        progressBarGlobal.setStringPainted(true);

        lblTimeLeftGlobal.setForeground(java.awt.Color.white);
        lblTimeLeftGlobal.setText(resourceMap.getString("lblTimeLeftGlobal.text")); // NOI18N
        lblTimeLeftGlobal.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lblTimeLeftGlobal.setName("lblTimeLeftGlobal"); // NOI18N

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setName("jLabel1"); // NOI18N

        javax.swing.GroupLayout pnlProgressLayout = new javax.swing.GroupLayout(pnlProgress);
        pnlProgress.setLayout(pnlProgressLayout);
        pnlProgressLayout.setHorizontalGroup(
            pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProgressLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, 576, Short.MAX_VALUE)
                    .addGroup(pnlProgressLayout.createSequentialGroup()
                        .addGroup(pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblProgressCurrent)
                            .addComponent(lblProgressGlobal))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(progressBarGlobal, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE)
                            .addComponent(progressBarCurrent, javax.swing.GroupLayout.DEFAULT_SIZE, 321, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(lblTimeLeftCurrent)
                            .addComponent(lblTimeLeftGlobal))))
                .addContainerGap())
        );
        pnlProgressLayout.setVerticalGroup(
            pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProgressLayout.createSequentialGroup()
                .addGap(48, 48, 48)
                .addComponent(jLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGap(18, 18, 18)
                .addGroup(pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProgressCurrent)
                    .addGroup(pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(progressBarCurrent, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(lblTimeLeftCurrent)))
                .addGap(18, 18, 18)
                .addGroup(pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProgressGlobal)
                    .addComponent(lblTimeLeftGlobal)
                    .addComponent(progressBarGlobal, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(44, 44, 44))
        );

        btnExit.setAction(actionMap.get("exitApplication")); // NOI18N
        btnExit.setName("btnExit"); // NOI18N

        pnlTree.setName("pnlTree"); // NOI18N

        lblSelectBlock.setFont(resourceMap.getFont("lblSelectBlock.font")); // NOI18N
        lblSelectBlock.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        lblSelectBlock.setText(resourceMap.getString("lblSelectBlock.text")); // NOI18N
        lblSelectBlock.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        lblSelectBlock.setName("lblSelectBlock"); // NOI18N

        javax.swing.GroupLayout pnlTreeLayout = new javax.swing.GroupLayout(pnlTree);
        pnlTree.setLayout(pnlTreeLayout);
        pnlTreeLayout.setHorizontalGroup(
            pnlTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSelectBlock, javax.swing.GroupLayout.DEFAULT_SIZE, 302, Short.MAX_VALUE)
        );
        pnlTreeLayout.setVerticalGroup(
            pnlTreeLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(lblSelectBlock, javax.swing.GroupLayout.DEFAULT_SIZE, 79, Short.MAX_VALUE)
        );

        pnlLogo.setAlpha(0.4F);
        pnlLogo.setInheritAlpha(false);
        pnlLogo.setName("pnlLogo"); // NOI18N

        hyperLinkLogo.setAction(actionMap.get("browse")); // NOI18N
        hyperLinkLogo.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        hyperLinkLogo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        hyperLinkLogo.setName("hyperLinkLogo"); // NOI18N
        hyperLinkLogo.setRolloverIcon(resourceMap.getIcon("hyperLinkLogo.rolloverIcon")); // NOI18N
        hyperLinkLogo.setSelectedIcon(resourceMap.getIcon("hyperLinkLogo.selectedIcon")); // NOI18N
        hyperLinkLogo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hyperLinkLogoMouseEntered(evt);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                hyperLinkLogoMouseExited(evt);
            }
        });

        javax.swing.GroupLayout pnlLogoLayout = new javax.swing.GroupLayout(pnlLogo);
        pnlLogo.setLayout(pnlLogoLayout);
        pnlLogoLayout.setHorizontalGroup(
            pnlLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLogoLayout.createSequentialGroup()
                .addGap(0, 33, Short.MAX_VALUE)
                .addComponent(hyperLinkLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 33, Short.MAX_VALUE))
        );
        pnlLogoLayout.setVerticalGroup(
            pnlLogoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlLogoLayout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(hyperLinkLogo, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        statusBar.setName("statusBar"); // NOI18N
        statusBar.putClientProperty(BasicStatusBarUI.AUTO_ADD_SEPARATOR, false);
        JXStatusBar.Constraint c0 = new JXStatusBar.Constraint();
        c0.setFixedWidth(20);
        statusBar.add(lblFlag, c0);
        //statusBar.add(new JSeparator(JSeparator.VERTICAL));
        JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(new Insets(0, 5, 0, 5));
        statusBar.add(statusMessageLabel, c1);
        JXStatusBar.Constraint c2 = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL);
        statusBar.add(pnlProgressBar, c2);
        JXStatusBar.Constraint c3 = new JXStatusBar.Constraint(new Insets(0, 5, 0, 5));
        c3.setFixedWidth(20);
        statusBar.add(statusAnimationLabel, c3);

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setMaximumSize(new java.awt.Dimension(20, 20));
        statusAnimationLabel.setMinimumSize(new java.awt.Dimension(20, 20));
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N
        statusAnimationLabel.setPreferredSize(new java.awt.Dimension(20, 20));
        BusyPainter painter = new BusyPainter(
            new RoundRectangle2D.Float(0, 0,4.0f,1.8f,10.0f,10.0f),
            new Ellipse2D.Float(3.0f,3.0f,14.0f,14.0f));
        painter.setTrailLength(4);
        painter.setPoints(8);
        painter.setFrame(-1);
        statusAnimationLabel.setPreferredSize(new Dimension(20,20));
        statusAnimationLabel.setIcon(new EmptyIcon(20,20));
        statusAnimationLabel.setBusyPainter(painter);

        pnlProgressBar.setName("pnlProgressBar"); // NOI18N
        pnlProgressBar.setOpaque(false);

        progressBar.setName("progressBar"); // NOI18N

        javax.swing.GroupLayout pnlProgressBarLayout = new javax.swing.GroupLayout(pnlProgressBar);
        pnlProgressBar.setLayout(pnlProgressBarLayout);
        pnlProgressBarLayout.setHorizontalGroup(
            pnlProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, pnlProgressBarLayout.createSequentialGroup()
                .addContainerGap(421, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        pnlProgressBarLayout.setVerticalGroup(
            pnlProgressBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(progressBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        lblFlag.setIcon(resourceMap.getIcon("lblFlag.icon")); // NOI18N
        lblFlag.setName("lblFlag"); // NOI18N

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusBar);
    }// </editor-fold>//GEN-END:initComponents

    private void lblFramePerSecondMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_lblFramePerSecondMouseClicked
        // TODO add your handling code here:
        if (evt.getClickCount() > 1) {
            animationSpeed.setValue(1.5f);
        }
}//GEN-LAST:event_lblFramePerSecondMouseClicked

    private void animationSpeedStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_animationSpeedStateChanged
        // TODO add your handling code here:
        JSpinner source = (JSpinner) evt.getSource();
        nbfps = (Float) source.getValue();
}//GEN-LAST:event_animationSpeedStateChanged

    private void sliderTimeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderTimeStateChanged
        // TODO add your handling code here:
        replayPanel.setIndex(sliderTime.getValue());
        lblTime.setText(replayPanel.getTime());
    }//GEN-LAST:event_sliderTimeStateChanged

    private void hyperLinkLogoMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_hyperLinkLogoMouseEntered
        // TODO add your handling code here:
        pnlLogo.setAlpha(0.9f);
    }//GEN-LAST:event_hyperLinkLogoMouseEntered

    private void hyperLinkLogoMouseExited(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_hyperLinkLogoMouseExited
        // TODO add your handling code here:
        pnlLogo.setAlpha(0.4f);
    }//GEN-LAST:event_hyperLinkLogoMouseExited

    private void taskPaneConfigurationPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_taskPaneConfigurationPropertyChange
        // TODO add your handling code here:
        if (evt.getPropertyName().matches("collapsed")) {
            if (!(Boolean) evt.getNewValue()) {
                taskPaneSimulation.setCollapsed(true);
                taskPaneAnimation.setCollapsed(true);
                taskPaneMapping.setCollapsed(true);
                if (null != getSimulationManager().getConfigurationFile()) {
                    pnlConfiguration.setVisible(true);
                    lblConfiguration.setVisible(false);
                    //btnSaveCfgFile.getAction().setEnabled(false);
                } else {
                    lblConfiguration.setVisible(true);
                }
            } else {
                pnlConfiguration.setVisible(false);
                lblConfiguration.setVisible(false);
            }
            setMainTitle();
        }
    }//GEN-LAST:event_taskPaneConfigurationPropertyChange

    private void taskPaneSimulationPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_taskPaneSimulationPropertyChange
        // TODO add your handling code here:
        if (evt.getPropertyName().matches("collapsed")) {
            if (!(Boolean) evt.getNewValue()) {
                taskPaneConfiguration.setCollapsed(true);
                taskPaneAnimation.setCollapsed(true);
                taskPaneMapping.setCollapsed(true);
                pnlProgress.setVisible(true);
                setupProgress();
            } else {
                pnlProgress.setVisible(false);
                if (btnPreview.isSelected()) {
                    btnPreview.doClick();
                }
            }
            setMainTitle();
        }
    }//GEN-LAST:event_taskPaneSimulationPropertyChange

    private void taskPaneMappingPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_taskPaneMappingPropertyChange
        // TODO add your handling code here:
        if (evt.getPropertyName().matches("collapsed")) {
            if (!(Boolean) evt.getNewValue()) {
                taskPaneSimulation.setCollapsed(true);
                taskPaneAnimation.setCollapsed(true);
                taskPaneConfiguration.setCollapsed(true);
                if (null != outputFile && outputFile.isFile()) {
                    wmsMapper.setFile(outputFile);
                    wmsMapper.setVisible(true);
                    lblMapping.setVisible(false);
                    btnMapping.getAction().setEnabled(true);
                    btnCloseNC.getAction().setEnabled(true);
                } else {
                    wmsMapper.setFile(null);
                    wmsMapper.setVisible(false);
                    lblMapping.setVisible(true);
                    btnMapping.getAction().setEnabled(false);
                    btnCloseNC.getAction().setEnabled(false);
                }
            } else {
                wmsMapper.setVisible(false);
                lblMapping.setVisible(false);
            }
            setMainTitle();
        }
    }//GEN-LAST:event_taskPaneMappingPropertyChange

    private void taskPaneAnimationPropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_taskPaneAnimationPropertyChange
        // TODO add your handling code here:
        if (evt.getPropertyName().matches("collapsed")) {
            if (!(Boolean) evt.getNewValue()) {
                taskPaneSimulation.setCollapsed(true);
                taskPaneConfiguration.setCollapsed(true);
                taskPaneMapping.setCollapsed(true);
                replayPanel.setVisible(true);
                replayPanel.setFolder(outputFolder);
            } else {
                if (animator.isRunning()) {
                    animate(false);
                }
                replayPanel.setVisible(false);
            }
            setMainTitle();
        }
    }//GEN-LAST:event_taskPaneAnimationPropertyChange
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem animactionMenuItem;
    private javax.swing.JMenu animationMenu;
    private javax.swing.JSpinner animationSpeed;
    private org.previmer.ichthyop.ui.BlockTree blockTree;
    private javax.swing.JButton btnAddValue;
    private javax.swing.JButton btnAnimaction;
    private javax.swing.JButton btnCancelMapping;
    private javax.swing.JButton btnCloseCfgFile;
    private javax.swing.JButton btnCloseNC;
    private javax.swing.JButton btnCollapse;
    private javax.swing.JButton btnDeleteMaps;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnExpand;
    private javax.swing.JButton btnExportMaps;
    private javax.swing.JButton btnFirst;
    private org.jdesktop.swingx.JXHyperlink btnHiddenParameter;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnLower;
    private javax.swing.JButton btnMapping;
    private javax.swing.JButton btnNewCfgFile;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnOpenAnimation;
    private javax.swing.JButton btnOpenCfgFile;
    private javax.swing.JButton btnOpenNC;
    private javax.swing.JToggleButton btnPreview;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnRedo;
    private javax.swing.JButton btnRemoveValue;
    private javax.swing.JButton btnSaveAsCfgFile;
    private javax.swing.JButton btnSaveCfgFile;
    private javax.swing.JButton btnSimulationRun;
    private javax.swing.JButton btnUndo;
    private javax.swing.JButton btnUpper;
    private javax.swing.JMenuItem cancelMapMenuItem;
    private javax.swing.JComboBox cbBoxWMS;
    private javax.swing.JCheckBox ckBoxAdvancedEditor;
    private javax.swing.JCheckBox ckBoxBlock;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem exportMenuItem;
    private org.previmer.ichthyop.ui.GradientPanel gradientPanel;
    private org.jdesktop.swingx.JXHyperlink hyperLinkLogo;
    private javax.swing.JCheckBoxMenuItem itemDeadChart;
    private javax.swing.JCheckBoxMenuItem itemDepthChart;
    private javax.swing.JCheckBoxMenuItem itemEdgeChart;
    private javax.swing.JCheckBoxMenuItem itemLengthChart;
    private javax.swing.JCheckBoxMenuItem itemRecruitChart;
    private javax.swing.JCheckBoxMenuItem itemStageChart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblAnimationSpeed;
    private javax.swing.JLabel lblBlockInfo;
    private javax.swing.JLabel lblCfgFile;
    private javax.swing.JLabel lblFlag;
    private javax.swing.JLabel lblFolder;
    private javax.swing.JLabel lblFramePerSecond;
    private javax.swing.JLabel lblNC;
    private javax.swing.JLabel lblParameter;
    private javax.swing.JLabel lblProgressCurrent;
    private javax.swing.JLabel lblProgressGlobal;
    private javax.swing.JLabel lblSelectBlock;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTimeLeftCurrent;
    private javax.swing.JLabel lblTimeLeftGlobal;
    private javax.swing.JLabel lblWMS;
    private javax.swing.JSplitPane leftSplitPane;
    private org.previmer.ichthyop.ui.LoggerScrollPane loggerScrollPane;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuItem mapMenuItem;
    private javax.swing.JMenu mappingMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem openAnimationMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem openNCMenuItem;
    private javax.swing.JPanel pnlAnimation;
    private javax.swing.JPanel pnlBlock;
    private javax.swing.JPanel pnlBlockInfo;
    private javax.swing.JPanel pnlConfiguration;
    private javax.swing.JPanel pnlFile;
    private org.jdesktop.swingx.JXPanel pnlLogo;
    private javax.swing.JPanel pnlMapping;
    private javax.swing.JPanel pnlParamDescription;
    private javax.swing.JPanel pnlParameters;
    private javax.swing.JPanel pnlProgress;
    private javax.swing.JPanel pnlProgressBar;
    private javax.swing.JPanel pnlSimulation;
    private javax.swing.JPanel pnlSimulationUI;
    private javax.swing.JPanel pnlTree;
    private javax.swing.JPanel pnlWMS;
    private javax.swing.JPopupMenu popupCharts;
    private javax.swing.JMenuItem previewMenuItem;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JProgressBar progressBarCurrent;
    private javax.swing.JProgressBar progressBarGlobal;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JScrollPane scrollPaneSimulationUI;
    private javax.swing.JMenuItem simulactionMenuItem;
    private javax.swing.JMenu simulationMenu;
    private javax.swing.JSlider sliderTime;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JSplitPane splitPaneCfg;
    private org.jdesktop.swingx.JXBusyLabel statusAnimationLabel;
    private org.jdesktop.swingx.JXStatusBar statusBar;
    private javax.swing.JLabel statusMessageLabel;
    private org.jdesktop.swingx.JXTable table;
    private org.jdesktop.swingx.JXTaskPane taskPaneAnimation;
    private org.jdesktop.swingx.JXTaskPane taskPaneConfiguration;
    private org.jdesktop.swingx.JXTaskPaneContainer taskPaneContainerSteps;
    private org.jdesktop.swingx.JXTaskPane taskPaneMapping;
    private org.jdesktop.swingx.JXTaskPane taskPaneSimulation;
    private org.jdesktop.swingx.JXTitledPanel titledPanelLogger;
    private org.jdesktop.swingx.JXTitledPanel titledPanelMain;
    private org.jdesktop.swingx.JXTitledPanel titledPanelSteps;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private JDialog aboutBox;
    private static final Logger logger = Logger.getLogger(ISimulationManager.class.getName());
    private File cfgPath = new File(System.getProperty("user.dir"));
    private boolean isRunning = false;
    private Task simulActionTask;
    private Task createMapTask;
    private boolean isSetup;
    private ReplayPanel replayPanel = new ReplayPanel();
    private final float TEN_MINUTES = 10.f * 60.f;
    private Animator animator = new Animator((int) (TEN_MINUTES * 1000), this);
    private float nbfps = 1.f;
    private float time;
    private Timer progressTimer;
    private WMSMapper wmsMapper = new WMSMapper();
    private File outputFile, outputFolder;
    private JLabel lblConfiguration;
    private JLabel lblMapping;
    private boolean hasStructureChanged;
    private boolean showHiddenParameters = true;
    private JBlockPanel blockEditor;
}
