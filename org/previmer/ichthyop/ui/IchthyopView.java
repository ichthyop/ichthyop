/*
 * IchthyopView.java
 */
package org.previmer.ichthyop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeEvent;
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
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.Timer;
import javax.swing.Icon;
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
import javax.swing.JSplitPane;
import javax.swing.tree.TreeSelectionModel;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTaskPaneContainer;
import org.jdesktop.swingx.JXTitledPanel;
import org.jdesktop.swingx.VerticalLayout;
import org.previmer.ichthyop.ui.WMSMapper.MapStep;
import org.previmer.ichthyop.util.MetaFilenameFilter;

/**
 * The application's main frame.
 */
public class IchthyopView extends FrameView implements TimingTarget {

    public IchthyopView(SingleFrameApplication app) {
        super(app);

        createLogfile();

        initComponents();
        createTaskPaneContainer();
        getFrame().setIconImage(getResourceMap().getImageIcon("Application.icon").getImage());

        // status bar initialization - message timeout, idle icon and busy animation, etc
        ResourceMap resourceMap = getResourceMap();
        int messageTimeout = resourceMap.getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
            }
        });
        messageTimer.setRepeats(false);
        int busyAnimationRate = resourceMap.getInteger("StatusBar.busyAnimationRate");
        for (int i = 0; i < busyIcons.length; i++) {
            busyIcons[i] = resourceMap.getIcon("StatusBar.busyIcons[" + i + "]");
        }
        busyIconTimer = new Timer(busyAnimationRate, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                busyIconIndex = (busyIconIndex + 1) % busyIcons.length;
                statusAnimationLabel.setIcon(busyIcons[busyIconIndex]);
            }
        });
        idleIcon = resourceMap.getIcon("StatusBar.idleIcon");
        statusAnimationLabel.setIcon(idleIcon);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("started".equals(propertyName)) {
                    if (!busyIconTimer.isRunning()) {
                        statusAnimationLabel.setIcon(busyIcons[0]);
                        busyIconIndex = 0;
                        busyIconTimer.start();
                    }
                    progressBar.setVisible(true);
                    progressBar.setIndeterminate(true);
                } else if ("done".equals(propertyName)) {
                    busyIconTimer.stop();
                    statusAnimationLabel.setIcon(idleIcon);
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

        //newMenuItem.getAction().setEnabled(false);
        closeMenuItem.getAction().setEnabled(false);
        saveMenuItem.getAction().setEnabled(false);
        btnSimulationRun.getAction().setEnabled(false);
        btnCancelMapping.getAction().setEnabled(false);
        btnMapping.getAction().setEnabled(false);
        setAnimationToolsEnabled(false);

        setMessage("Please, open a configuration file or create a new one");
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
            tpMapping.setCollapsed(true);
            tpAnimation.setCollapsed(false);
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
            setMainTitle();
        }
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
                cancel(true);
            }
            return null;
        }

        @Override
        protected void succeeded(Object o) {
            outputFolder = folder;
            lblFolder.setText(outputFolder.getName());
            lblFolder.setFont(lblFolder.getFont().deriveFont(Font.PLAIN, 12));
            replayPanel.setFolder(outputFolder);
            sliderTime.setMaximum(nbPNG - 1);
            setAnimationToolsEnabled(true);
        }

        @Override
        protected void cancelled() {
            lblFolder.setText(IchthyopView.this.getResourceMap().getString("lblFolder.text"));
            lblFolder.setFont(lblFolder.getFont().deriveFont(Font.PLAIN, 12));
            sliderTime.setMaximum(0);
            setAnimationToolsEnabled(false);
            setMessage("No PNG pictures found in folder " + folder.getAbsolutePath());
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
        pnlBackground.setVisible(false);
        pnlBackground.add(scrollPaneSimulationUI, StackLayout.TOP);
        getSimulationUI().init();
        getSimulationUI().repaintBackground();
        pnlBackground.setVisible(true);
    }

    private void hideSimulationPreview() {
        pnlBackground.setVisible(false);
        pnlBackground.remove(scrollPaneSimulationUI);
        pnlBackground.setVisible(true);
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
                isSetup = true;
                setMessage("Setup [OK]");
            }
            return null;
        }

        @Override
        protected void succeeded(Object obj) {
            showSimulationPreview();
        }
    }

    @Action
    public void saveConfigurationFile() {
    }

    @Action
    public void closeConfigurationFile() {
        if (null == getSimulationManager().getConfigurationFile()) {
            return;
        }
        setMessage("Closed " + getSimulationManager().getConfigurationFile().toString());
        getSimulationManager().setConfigurationFile(null);
        lblCfgFile.setText("Configuration file");
        lblCfgFile.setFont(lblCfgFile.getFont().deriveFont(12));
        btnSimulationRun.getAction().setEnabled(false);
        btnSaveCfgFile.getAction().setEnabled(false);
        closeMenuItem.getAction().setEnabled(false);
        editionSplitPane.setVisible(false);
        lblConfiguration.setVisible(true);
        setMainTitle();
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
        //saveMenuItem.getAction().setEnabled(true);
        closeMenuItem.getAction().setEnabled(true);
        btnSimulationRun.getAction().setEnabled(true);
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
            replayPanel.initAnim();
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
        replayPanel.endAnim();
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
        saveMenuItem.getAction().setEnabled(enabled);
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
            setMessage("End of simulation");
            outputFile = new File(getSimulationManager().getOutputManager().getFileLocation());
            lblNC.setText(outputFile.getName());
            lblNC.setFont(lblNC.getFont().deriveFont(Font.PLAIN, 12));
            tpSimulation.setCollapsed(true);
            tpMapping.setCollapsed(false);
        }

        @Override
        protected void finished() {
            btnSimulationRun.setIcon(resourceMap.getIcon("simulationRun.Action.icon.play"));
            btnSimulationRun.setText(resourceMap.getString("simulationRun.Action.text.start"));
            openMenuItem.getAction().setEnabled(true);
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
            editionSplitPane.setVisible(true);
            blockTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
            //blockTree.addTreeSelectionListener(IchthyopView.this);
            blockTree.setNodeVisible(blockTree.getRoot().getFirstLeaf());
        }

        @Override
        protected void failed(Throwable t) {
            logger.log(Level.SEVERE, null, t);
        }
    }

    public void savePreferences() {

        if (null != getSimulationManager().getConfigurationFile()) {
            savePreference(openMenuItem, getSimulationManager().getConfigurationFile().getPath());
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
                //tpConfiguration.setCollapsed(false);
            } else if (file.isDirectory()) {
                cfgPath = file;
            }
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

    private void createTaskPaneContainer() {

        JXTaskPaneContainer tpContainer = new JXTaskPaneContainer();
        tpContainer.setLayout(new GridBagLayout());
        tpContainer.setOpaque(false);

        tpConfiguration = new JXTaskPane();
        tpConfiguration.setCollapsed(false);
        tpConfiguration.setAnimated(false);
        tpConfiguration.setTitle(getResourceMap().getString("step.Configuration.text"));
        tpConfiguration.setIcon(getResourceMap().getIcon("step.Configuration.icon"));
        tpConfiguration.add(pnlFile);

        tpSimulation = new JXTaskPane();
        tpSimulation.setCollapsed(true);
        tpSimulation.setAnimated(false);
        tpSimulation.setTitle(getResourceMap().getString("step.Simulation.text"));
        tpSimulation.setIcon(getResourceMap().getIcon("step.Simulation.icon"));
        tpSimulation.add(pnlSimulation);

        tpMapping = new JXTaskPane();
        tpMapping.setCollapsed(true);
        tpMapping.setAnimated(false);
        tpMapping.setTitle(getResourceMap().getString("step.Mapping.text"));
        tpMapping.setIcon(getResourceMap().getIcon("step.Mapping.icon"));
        tpMapping.add(pnlMapping);

        tpAnimation = new JXTaskPane();
        tpAnimation.setCollapsed(true);
        tpAnimation.setAnimated(false);
        tpAnimation.setTitle(getResourceMap().getString("step.Animation.text"));
        tpAnimation.setIcon(getResourceMap().getIcon("step.Animation.icon"));
        tpAnimation.add(pnlAnimation);

        addTaskPaneListeners();

        tpContainer.add(tpConfiguration, new GridBagConstraints(0, 0, 1, 1, 100, 0,
                GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        tpContainer.add(tpSimulation, new GridBagConstraints(0, 1, 1, 1, 100, 0,
                GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        tpContainer.add(tpMapping, new GridBagConstraints(0, 2, 1, 1, 100, 0,
                GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));
        tpContainer.add(tpAnimation, new GridBagConstraints(0, 3, 1, 1, 100, 0,
                GridBagConstraints.NORTH,
                GridBagConstraints.HORIZONTAL,
                new Insets(0, 0, 0, 0), 0, 0));


        leftPane = new JPanel();
        leftPane.setLayout(new GridBagLayout());
        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setResizeWeight(0.5);
        JXTitledPanel pnlSteps = new JXTitledPanel("Management");
        pnlSteps.setLayout(new VerticalLayout());
        pnlSteps.add(tpContainer);
        leftSplitPane.setTopComponent(pnlSteps);
        JXTitledPanel informationPanel = new JXTitledPanel("Information");
        informationPanel.add(loggerScrollPane);
        leftSplitPane.setBottomComponent(informationPanel);
        leftPane.add(leftSplitPane, new GridBagConstraints(0, 0, 1, 1, 100, 100,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        splitPane.setLeftComponent(leftPane);

        lblConfiguration = new JLabel(getResourceMap().getIcon("lblConfiguration.icon"));
        lblConfiguration.setHorizontalAlignment(JLabel.CENTER);
        lblConfiguration.setVerticalAlignment(JLabel.CENTER);
        editionSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        editionSplitPane.setLeftComponent(blockTree);
        editionSplitPane.setRightComponent(new GradientPanel());

        lblMapping = new JLabel(getResourceMap().getIcon("lblMapping.icon"));
        lblMapping.setHorizontalAlignment(JLabel.CENTER);
        lblMapping.setVerticalAlignment(JLabel.CENTER);

        JPanel rightPane = new JPanel();
        rightPane.setLayout(new GridBagLayout());
        mainTitledPanel = new JXTitledPanel("Ichthyop");
        pnlBackground = new JPanel();
        pnlBackground.setLayout(new StackLayout());
        pnlBackground.add(new GradientPanel(), StackLayout.BOTTOM);
        pnlBackground.add(lblConfiguration, StackLayout.TOP);
        lblConfiguration.setVisible(true);
        pnlBackground.add(editionSplitPane);
        editionSplitPane.setVisible(false);
        pnlBackground.add(pnlProgress, StackLayout.TOP);
        pnlProgress.setVisible(false);
        pnlBackground.add(lblMapping, StackLayout.TOP);
        lblMapping.setVisible(false);
        pnlBackground.add(wmsMapper, StackLayout.TOP);
        wmsMapper.setVisible(false);
        pnlBackground.add(replayPanel, StackLayout.TOP);
        replayPanel.setVisible(false);
        mainTitledPanel.add(pnlBackground);
        rightPane.add(mainTitledPanel, new GridBagConstraints(0, 0, 1, 1, 100, 100,
                GridBagConstraints.CENTER,
                GridBagConstraints.BOTH,
                new Insets(5, 5, 5, 5), 0, 0));
        splitPane.setRightComponent(rightPane);
    }

    private void addTaskPaneListeners() {

        tpConfiguration.addPropertyChangeListener("collapsed", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (!(Boolean) evt.getNewValue()) {
                    tpSimulation.setCollapsed(true);
                    tpAnimation.setCollapsed(true);
                    tpMapping.setCollapsed(true);
                    if (null != getSimulationManager().getConfigurationFile()) {
                        editionSplitPane.setVisible(true);
                        lblConfiguration.setVisible(false);
                    } else {
                        lblConfiguration.setVisible(true);
                    }
                } else {
                    editionSplitPane.setVisible(false);
                    lblConfiguration.setVisible(false);
                }
                setMainTitle();
            }
        });

        tpSimulation.addPropertyChangeListener("collapsed", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (!(Boolean) evt.getNewValue()) {
                    tpConfiguration.setCollapsed(true);
                    tpAnimation.setCollapsed(true);
                    tpMapping.setCollapsed(true);
                    pnlProgress.setVisible(true);
                    setupProgress();
                } else {
                    pnlProgress.setVisible(false);
                }
                setMainTitle();
            }
        });

        tpMapping.addPropertyChangeListener("collapsed", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (!(Boolean) evt.getNewValue()) {
                    tpSimulation.setCollapsed(true);
                    tpAnimation.setCollapsed(true);
                    tpConfiguration.setCollapsed(true);
                    if (null != outputFile && outputFile.isFile()) {
                        wmsMapper.setFile(outputFile);
                        wmsMapper.setVisible(true);
                        lblMapping.setVisible(false);
                        btnMapping.getAction().setEnabled(true);
                    } else {
                        wmsMapper.setFile(null);
                        wmsMapper.setVisible(false);
                        lblMapping.setVisible(true);
                        btnMapping.getAction().setEnabled(false);
                    }
                } else {
                    wmsMapper.setVisible(false);
                    lblMapping.setVisible(false);
                }
                setMainTitle();
            }
        });

        tpAnimation.addPropertyChangeListener("collapsed", new PropertyChangeListener() {

            public void propertyChange(PropertyChangeEvent evt) {
                if (!(Boolean) evt.getNewValue()) {
                    tpSimulation.setCollapsed(true);
                    tpConfiguration.setCollapsed(true);
                    tpMapping.setCollapsed(true);
                    replayPanel.setVisible(true);
                    replayPanel.setFolder(outputFolder);
                } else {
                    replayPanel.setVisible(false);
                }
                setMainTitle();
            }
        });
    }

    private void setMainTitle() {

        if (!tpConfiguration.isCollapsed()) {
            if (null != getSimulationManager().getConfigurationFile()) {
                mainTitledPanel.setTitle("Ichthyop - " + tpConfiguration.getTitle() + " - " + lblCfgFile.getText());
            } else {
                mainTitledPanel.setTitle("Ichthyop - " + tpConfiguration.getTitle());
            }
        } else if (!tpSimulation.isCollapsed()) {
            if (null != getSimulationManager().getConfigurationFile()) {
                mainTitledPanel.setTitle("Ichthyop - " + tpSimulation.getTitle() + " - " + lblCfgFile.getText());
            } else {
                mainTitledPanel.setTitle("Ichthyop - " + tpSimulation.getTitle());
            }
        } else if (!tpMapping.isCollapsed()) {
            if (null != outputFile) {
                mainTitledPanel.setTitle("Ichthyop - " + tpMapping.getTitle() + " - " + outputFile.getName());
            } else {
                mainTitledPanel.setTitle("Ichthyop - " + tpMapping.getTitle());
            }
        } else if (!tpAnimation.isCollapsed()) {
            if (null != outputFolder) {
                mainTitledPanel.setTitle("Ichthyop - " + tpAnimation.getTitle() + " - " + outputFolder.getName());
            } else {
                mainTitledPanel.setTitle("Ichthyop - " + tpAnimation.getTitle());
            }
        } else {
            mainTitledPanel.setTitle("Ichthyop");
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

        mainPanel = new javax.swing.JPanel();
        splitPane = new javax.swing.JSplitPane();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu configurationMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        saveMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        simulationMenu = new javax.swing.JMenu();
        simulactionMenuItem = new javax.swing.JMenuItem();
        previewMenuItem = new javax.swing.JMenuItem();
        mappingMenu = new javax.swing.JMenu();
        openNCMenuItem = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        mapMenuItem = new javax.swing.JMenuItem();
        cancelMapMenuItem = new javax.swing.JMenuItem();
        animationMenu = new javax.swing.JMenu();
        openAnimationMenuItem = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        exportMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        animactionMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        popupCharts = new javax.swing.JPopupMenu();
        itemDepthChart = new javax.swing.JCheckBoxMenuItem();
        itemEdgeChart = new javax.swing.JCheckBoxMenuItem();
        itemRecruitChart = new javax.swing.JCheckBoxMenuItem();
        itemLengthChart = new javax.swing.JCheckBoxMenuItem();
        itemDeadChart = new javax.swing.JCheckBoxMenuItem();
        itemStageChart = new javax.swing.JCheckBoxMenuItem();
        pnlSimulation = new javax.swing.JPanel();
        btnSimulationRun = new javax.swing.JButton();
        btnPreview = new javax.swing.JToggleButton();
        pnlFile = new javax.swing.JPanel();
        btnNewCfgFile = new javax.swing.JButton();
        btnOpenCfgFile = new javax.swing.JButton();
        btnCloseCfgFile = new javax.swing.JButton();
        btnSaveCfgFile = new javax.swing.JButton();
        lblCfgFile = new javax.swing.JLabel();
        scrollPaneSimulationUI = new javax.swing.JScrollPane();
        pnlSimulationUI = new SimulationUI();
        pnlMapping = new javax.swing.JPanel();
        btnMapping = new javax.swing.JButton();
        btnCancelMapping = new javax.swing.JButton();
        btnOpenNC = new javax.swing.JButton();
        pnlWMS = new javax.swing.JPanel();
        cbBoxWMS = new javax.swing.JComboBox();
        lblWMS = new javax.swing.JLabel();
        lblNC = new javax.swing.JLabel();
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
        pnlConfiguration = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        blockTree = new org.previmer.ichthyop.ui.BlockTree();
        pnlProgress = new javax.swing.JPanel();
        lblProgressCurrent = new javax.swing.JLabel();
        progressBarCurrent = new javax.swing.JProgressBar();
        lblTimeLeftCurrent = new javax.swing.JLabel();
        lblProgressGlobal = new javax.swing.JLabel();
        progressBarGlobal = new javax.swing.JProgressBar();
        lblTimeLeftGlobal = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        btnExit = new javax.swing.JButton();

        mainPanel.setName("mainPanel"); // NOI18N

        splitPane.setName("splitPane"); // NOI18N

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 298, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
        configurationMenu.setText(resourceMap.getString("configurationMenu.text")); // NOI18N
        configurationMenu.setName("configurationMenu"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getActionMap(IchthyopView.class, this);
        newMenuItem.setAction(actionMap.get("newConfigurationFile")); // NOI18N
        newMenuItem.setName("newMenuItem"); // NOI18N
        configurationMenu.add(newMenuItem);

        openMenuItem.setAction(actionMap.get("openConfigurationFile")); // NOI18N
        openMenuItem.setName("openMenuItem"); // NOI18N
        configurationMenu.add(openMenuItem);

        saveMenuItem.setAction(actionMap.get("saveConfigurationFile")); // NOI18N
        saveMenuItem.setName("saveMenuItem"); // NOI18N
        configurationMenu.add(saveMenuItem);

        closeMenuItem.setAction(actionMap.get("closeConfigurationFile")); // NOI18N
        closeMenuItem.setName("closeMenuItem"); // NOI18N
        configurationMenu.add(closeMenuItem);

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

        openNCMenuItem.setAction(actionMap.get("openNcMapping")); // NOI18N
        openNCMenuItem.setName("openNCMenuItem"); // NOI18N
        mappingMenu.add(openNCMenuItem);

        jSeparator13.setName("jSeparator13"); // NOI18N
        mappingMenu.add(jSeparator13);

        mapMenuItem.setAction(actionMap.get("createMaps")); // NOI18N
        mapMenuItem.setName("mapMenuItem"); // NOI18N
        mappingMenu.add(mapMenuItem);

        cancelMapMenuItem.setAction(actionMap.get("cancelMapping")); // NOI18N
        cancelMapMenuItem.setName("cancelMapMenuItem"); // NOI18N
        mappingMenu.add(cancelMapMenuItem);

        menuBar.add(mappingMenu);

        animationMenu.setText(resourceMap.getString("animationMenu.text")); // NOI18N
        animationMenu.setName("animationMenu"); // NOI18N

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

        jSeparator15.setName("jSeparator15"); // NOI18N
        animationMenu.add(jSeparator15);

        animactionMenuItem.setAction(actionMap.get("animAction")); // NOI18N
        animactionMenuItem.setName("animactionMenuItem"); // NOI18N
        animationMenu.add(animactionMenuItem);

        menuBar.add(animationMenu);

        helpMenu.setText(resourceMap.getString("helpMenu.text")); // NOI18N
        helpMenu.setName("helpMenu"); // NOI18N

        aboutMenuItem.setAction(actionMap.get("showAboutBox")); // NOI18N
        aboutMenuItem.setName("aboutMenuItem"); // NOI18N
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        statusPanel.setName("statusPanel"); // NOI18N

        statusPanelSeparator.setName("statusPanelSeparator"); // NOI18N

        statusMessageLabel.setName("statusMessageLabel"); // NOI18N

        statusAnimationLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        statusAnimationLabel.setName("statusAnimationLabel"); // NOI18N

        progressBar.setName("progressBar"); // NOI18N
        progressBar.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                progressBarMouseEntered(evt);
            }
        });

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 645, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 459, Short.MAX_VALUE)
                .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(statusAnimationLabel)
                .addContainerGap())
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addComponent(statusPanelSeparator, javax.swing.GroupLayout.PREFERRED_SIZE, 2, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusMessageLabel)
                    .addComponent(statusAnimationLabel)
                    .addComponent(progressBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(3, 3, 3))
        );

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

        pnlSimulation.setBorder(null);
        pnlSimulation.setName("pnlSimulation"); // NOI18N
        pnlSimulation.setOpaque(false);

        btnSimulationRun.setAction(actionMap.get("simulationRun")); // NOI18N
        btnSimulationRun.setFocusable(false);
        btnSimulationRun.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnSimulationRun.setName("btnSimulationRun"); // NOI18N
        btnSimulationRun.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnPreview.setAction(actionMap.get("previewSimulation")); // NOI18N
        btnPreview.setFocusable(false);
        btnPreview.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnPreview.setName("btnPreview"); // NOI18N
        btnPreview.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        javax.swing.GroupLayout pnlSimulationLayout = new javax.swing.GroupLayout(pnlSimulation);
        pnlSimulation.setLayout(pnlSimulationLayout);
        pnlSimulationLayout.setHorizontalGroup(
            pnlSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlSimulationLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(btnSimulationRun)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnPreview)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

        pnlFile.setName("pnlFile"); // NOI18N
        pnlFile.setOpaque(false);

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

        btnCloseCfgFile.setAction(actionMap.get("closeConfigurationFile")); // NOI18N
        btnCloseCfgFile.setFocusable(false);
        btnCloseCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnCloseCfgFile.setName("btnCloseCfgFile"); // NOI18N
        btnCloseCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnSaveCfgFile.setAction(actionMap.get("saveConfigurationFile")); // NOI18N
        btnSaveCfgFile.setName("btnSaveCfgFile"); // NOI18N

        lblCfgFile.setName("lblCfgFile"); // NOI18N

        javax.swing.GroupLayout pnlFileLayout = new javax.swing.GroupLayout(pnlFile);
        pnlFile.setLayout(pnlFileLayout);
        pnlFileLayout.setHorizontalGroup(
            pnlFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFileLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblCfgFile, javax.swing.GroupLayout.DEFAULT_SIZE, 415, Short.MAX_VALUE)
                    .addGroup(pnlFileLayout.createSequentialGroup()
                        .addComponent(btnNewCfgFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnOpenCfgFile)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnSaveCfgFile, javax.swing.GroupLayout.PREFERRED_SIZE, 96, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCloseCfgFile)))
                .addContainerGap())
        );
        pnlFileLayout.setVerticalGroup(
            pnlFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlFileLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(btnSaveCfgFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnNewCfgFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnOpenCfgFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(btnCloseCfgFile, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblCfgFile)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

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
                .addComponent(cbBoxWMS, 0, 399, Short.MAX_VALUE)
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

        javax.swing.GroupLayout pnlMappingLayout = new javax.swing.GroupLayout(pnlMapping);
        pnlMapping.setLayout(pnlMappingLayout);
        pnlMappingLayout.setHorizontalGroup(
            pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMappingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlWMS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlMappingLayout.createSequentialGroup()
                        .addComponent(btnOpenNC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnMapping)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCancelMapping))
                    .addComponent(lblNC))
                .addContainerGap())
        );
        pnlMappingLayout.setVerticalGroup(
            pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMappingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMapping)
                    .addComponent(btnCancelMapping)
                    .addComponent(btnOpenNC))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(lblNC)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlWMS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

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
                    .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addComponent(btnOpenAnimation)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExportMaps)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnDeleteMaps))
                    .addComponent(lblFolder)
                    .addComponent(sliderTime, javax.swing.GroupLayout.DEFAULT_SIZE, 405, Short.MAX_VALUE)
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
                    .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addComponent(lblAnimationSpeed)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(animationSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFramePerSecond)))
                .addContainerGap())
        );
        pnlAnimationLayout.setVerticalGroup(
            pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlAnimationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnExportMaps)
                            .addComponent(btnOpenAnimation))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblFolder))
                    .addComponent(btnDeleteMaps))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
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
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblAnimationSpeed)
                    .addComponent(animationSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblFramePerSecond))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pnlConfiguration.setName("pnlConfiguration"); // NOI18N

        jScrollPane1.setName("jScrollPane1"); // NOI18N

        blockTree.setName("blockTree"); // NOI18N
        blockTree.setRootVisible(true);
        jScrollPane1.setViewportView(blockTree);

        javax.swing.GroupLayout pnlConfigurationLayout = new javax.swing.GroupLayout(pnlConfiguration);
        pnlConfiguration.setLayout(pnlConfigurationLayout);
        pnlConfigurationLayout.setHorizontalGroup(
            pnlConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
        );
        pnlConfigurationLayout.setVerticalGroup(
            pnlConfigurationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE)
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
        jLabel1.setIcon(resourceMap.getIcon("jLabel1.icon")); // NOI18N
        jLabel1.setText(resourceMap.getString("jLabel1.text")); // NOI18N
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

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
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

    private void progressBarMouseEntered(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_progressBarMouseEntered
        // TODO add your handling code here:
        //popupProgress.show(progressBar, 0, 0);
    }//GEN-LAST:event_progressBarMouseEntered

    private void sliderTimeStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_sliderTimeStateChanged
        // TODO add your handling code here:
        replayPanel.setIndex(sliderTime.getValue());
        lblTime.setText(replayPanel.getTime());
    }//GEN-LAST:event_sliderTimeStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem animactionMenuItem;
    private javax.swing.JMenu animationMenu;
    private javax.swing.JSpinner animationSpeed;
    private org.previmer.ichthyop.ui.BlockTree blockTree;
    private javax.swing.JButton btnAnimaction;
    private javax.swing.JButton btnCancelMapping;
    private javax.swing.JButton btnCloseCfgFile;
    private javax.swing.JButton btnDeleteMaps;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnExportMaps;
    private javax.swing.JButton btnFirst;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnMapping;
    private javax.swing.JButton btnNewCfgFile;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnOpenAnimation;
    private javax.swing.JButton btnOpenCfgFile;
    private javax.swing.JButton btnOpenNC;
    private javax.swing.JToggleButton btnPreview;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnSaveCfgFile;
    private javax.swing.JButton btnSimulationRun;
    private javax.swing.JMenuItem cancelMapMenuItem;
    private javax.swing.JComboBox cbBoxWMS;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JCheckBoxMenuItem itemDeadChart;
    private javax.swing.JCheckBoxMenuItem itemDepthChart;
    private javax.swing.JCheckBoxMenuItem itemEdgeChart;
    private javax.swing.JCheckBoxMenuItem itemLengthChart;
    private javax.swing.JCheckBoxMenuItem itemRecruitChart;
    private javax.swing.JCheckBoxMenuItem itemStageChart;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JLabel lblAnimationSpeed;
    private javax.swing.JLabel lblCfgFile;
    private javax.swing.JLabel lblFolder;
    private javax.swing.JLabel lblFramePerSecond;
    private javax.swing.JLabel lblNC;
    private javax.swing.JLabel lblProgressCurrent;
    private javax.swing.JLabel lblProgressGlobal;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblTimeLeftCurrent;
    private javax.swing.JLabel lblTimeLeftGlobal;
    private javax.swing.JLabel lblWMS;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuItem mapMenuItem;
    private javax.swing.JMenu mappingMenu;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem openAnimationMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JMenuItem openNCMenuItem;
    private javax.swing.JPanel pnlAnimation;
    private javax.swing.JPanel pnlConfiguration;
    private javax.swing.JPanel pnlFile;
    private javax.swing.JPanel pnlMapping;
    private javax.swing.JPanel pnlProgress;
    private javax.swing.JPanel pnlSimulation;
    private javax.swing.JPanel pnlSimulationUI;
    private javax.swing.JPanel pnlWMS;
    private javax.swing.JPopupMenu popupCharts;
    private javax.swing.JMenuItem previewMenuItem;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JProgressBar progressBarCurrent;
    private javax.swing.JProgressBar progressBarGlobal;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JScrollPane scrollPaneSimulationUI;
    private javax.swing.JMenuItem simulactionMenuItem;
    private javax.swing.JMenu simulationMenu;
    private javax.swing.JSlider sliderTime;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    // End of variables declaration//GEN-END:variables
    private JPanel pnlBackground;
    private JXTitledPanel mainTitledPanel;
    private JPanel leftPane;
    private JXTaskPane tpConfiguration;
    private JXTaskPane tpSimulation;
    private JXTaskPane tpMapping;
    private JXTaskPane tpAnimation;
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
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
    private LoggerScrollPane loggerScrollPane = new LoggerScrollPane();
    private File outputFile, outputFolder;
    private JSplitPane editionSplitPane;
    private JLabel lblConfiguration;
    private JLabel lblMapping;
}
