/*
 * IchthyopView.java
 */
package org.previmer.ichthyop.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.NumberFormat;
import java.util.EventObject;
import java.util.List;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.Timer;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.previmer.ichthyop.arch.ISimulationManager;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.manager.SimulationManager;
import javax.swing.JSpinner;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.swingx.painter.Painter;
import org.previmer.ichthyop.ui.logging.SystemOutHandler;
import org.previmer.ichthyop.util.MetaFilenameFilter;

/**
 * The application's main frame.
 */
public class IchthyopView extends FrameView
        implements TimingTarget, PropertyChangeListener {

    public IchthyopView(SingleFrameApplication app) {
        super(app);

        initComponents();
        setStatusBar(statusBar);

        /* Initialize the logs */
        initLogging();

        /* Set frame icon */
        getFrame().setIconImage(getResourceMap().getImageIcon("Application.icon").getImage());

        /* Disabled some actions */
        closeMenuItem.getAction().setEnabled(false);
        saveAsMenuItem.getAction().setEnabled(false);
        btnSimulationRun.getAction().setEnabled(false);
        btnPreview.getAction().setEnabled(false);
        btnCancelMapping.getAction().setEnabled(false);
        btnMapping.getAction().setEnabled(false);
        btnCloseNC.getAction().setEnabled(false);
        btnExportToKMZ.getAction().setEnabled(false);
        setAnimationToolsEnabled(false);
        setColorbarPanelEnabled(false);

        /* Add some listeners */
        getApplication().addExitListener(new ConfirmExit());
        pnlConfiguration.addPropertyChangeListener("configurationFile", this);

    }

    public ISimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    private void initLogging() {

        /* Create a FileHandler (logs will be recorded in a file */
        try {
            String logPath = System.getProperty("user.dir") + File.separator + "log" + File.separator;
            StringBuffer logfile = new StringBuffer(logPath);
            logfile.append("ichthyop");
            logfile.append(".log");
            IOTools.makeDirectories(logfile.toString());
            FileHandler fh = new FileHandler(logfile.toString());
            getLogger().addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
            getLogger().info(getResourceMap().getString("createdLogFile.msg") + " " + logfile.toString());
        } catch (IOException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }

        /* Plug the loggerPanel and the StatusBar */
        loggerScrollPane.connectToLogger(getLogger());
        statusBar.connectToLogger(getLogger());

        /* Connect to the java console */
        getLogger().addHandler(new SystemOutHandler());

        /* Welcome user */
        getLogger().info(getResourceMap().getString("Application.title") + " - Welcome on board !");
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
        StringBuffer message = new StringBuffer(getResourceMap().getString("deleteMaps.dialog.msg.part1"));
        message.append(" ");
        message.append(outputFolder.getName());
        message.append(" ?");
        message.append('\n');
        message.append(files2Delete.length);
        message.append(" ");
        message.append(getResourceMap().getString("deleteMaps.dialog.msg.part2"));
        int dialog = JOptionPane.showConfirmDialog(getFrame(), message.toString(), getResourceMap().getString("deleteMaps.dialog.title"), JOptionPane.OK_CANCEL_OPTION);
        if (dialog == JOptionPane.OK_OPTION) {
            for (File file : outputFolder.listFiles(new MetaFilenameFilter("*.png"))) {
                if (file.delete()) {
                    getLogger().info(getResourceMap().getString("deleteMaps.deleted") + " " + file.toString());
                }
            }
            outputFolder.delete();
            lblFolder.setText(getResourceMap().getString("lblFolder.text"));
            outputFolder = null;
            replayPanel.setFolder(null);
            setAnimationToolsEnabled(false);
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        btnSaveCfgFile.getAction().setEnabled(true);
    }

    @Action
    public Task exportToKMZ() {
        return new ExportToKMZTask(getApplication());
    }

    private class ExportToKMZTask extends SFTask {

        ExportToKMZTask(Application instance) {
            super(instance);
            setMessage(resourceMap.getString("exportToKMZ.msg.init"));
            wmsMapper.setAlpha(0.2f);
            wmsMapper.setEnabled(false);
            btnMapping.getAction().setEnabled(false);
            btnExportToKMZ.getAction().setEnabled(false);
            btnCloseNC.getAction().setEnabled(false);
            setColorbarPanelEnabled(false);
        }

        @Override
        protected Object doInBackground() throws Exception {
            wmsMapper.createKML();
            for (int i = 0; i < wmsMapper.getNbSteps(); i++) {
                setProgress((float) (i + 1) / wmsMapper.getNbSteps());
                setMessage(resourceMap.getString("exportToKMZ.msg.exporting") + " " + (i + 1) + "/" + (wmsMapper.getNbSteps()), true, Level.INFO);
                wmsMapper.writeKMLStep(i);
            }
            setMessage(resourceMap.getString("exportToKMZ.msg.compressing"), true, Level.INFO);
            wmsMapper.marshalAndKMZ();
            return null;
        }

        @Override
        protected void finished() {
            wmsMapper.setAlpha(1.f);
            wmsMapper.setEnabled(true);
            btnMapping.getAction().setEnabled(true);
            btnExportToKMZ.getAction().setEnabled(true);
            btnCloseNC.getAction().setEnabled(true);
            setColorbarPanelEnabled(true);
        }

        @Override
        void onSuccess(Object o) {
            setMessage(resourceMap.getString("exportToKMZ.msg.exported") + " " + wmsMapper.getKMZPath());
        }

        @Override
        void onFailure(Throwable t) {
        }
    }

    @Action
    public Task createMaps() {
        return createMapTask = new CreateMapTask(getApplication());
    }

    private class CreateMapTask extends SFTask<Object, Painter> {

        private int index;

        CreateMapTask(Application instance) {
            super(instance);
            wmsMapper.setZoomButtonsVisible(false);
            wmsMapper.setZoomSliderVisible(false);
            btnMapping.getAction().setEnabled(false);
            btnExportToKMZ.getAction().setEnabled(false);
            btnCancelMapping.getAction().setEnabled(true);
            setColorbarPanelEnabled(false);
            index = 0;
        }

        @Override
        protected Object doInBackground() throws Exception {
            for (int iStep = 0; iStep < wmsMapper.getNbSteps(); iStep++) {
                setProgress((float) (iStep + 1) / wmsMapper.getNbSteps());
                publish(wmsMapper.getPainterForStep(iStep));
                Thread.sleep(500);
            }
            return null;
        }

        @Override
        protected void process(List<Painter> painters) {
            for (Painter painter : painters) {
                wmsMapper.map(painter);
                wmsMapper.screen2File(wmsMapper, index++);
            }
        }

        @Override
        protected void finished() {
            wmsMapper.setZoomButtonsVisible(true);
            wmsMapper.setZoomSliderVisible(true);
            btnMapping.getAction().setEnabled(true);
            btnExportToKMZ.getAction().setEnabled(true);
            btnCancelMapping.getAction().setEnabled(false);
            setColorbarPanelEnabled(true);
        }

        @Override
        void onSuccess(Object result) {
            taskPaneMapping.setCollapsed(true);
            taskPaneAnimation.setCollapsed(false);
            getApplication().getContext().getTaskService().execute(new OpenFolderAnimationTask(getApplication(), wmsMapper.getFolder()));
        }

        @Override
        void onFailure(Throwable throwable) {
            // do nothing
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
        chooser.setFileFilter(new FileNameExtensionFilter(getResourceMap().getString("Application.outputFile"), getResourceMap().getString("Application.outputFile.extension")));
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
            btnExportToKMZ.getAction().setEnabled(true);
            setMainTitle();
            setColorbarPanelEnabled(true);
            cbBoxVariable.setModel(new DefaultComboBoxModel(wmsMapper.getVariableList()));
        }
    }

    @Action
    public void closeNetCDF() {
        outputFile = null;
        lblNC.setText(getResourceMap().getString("lblNC.text"));
        lblNC.setFont(lblNC.getFont().deriveFont(Font.PLAIN, 12));
        wmsMapper.setFile(outputFile);
        btnMapping.getAction().setEnabled(false);
        btnExportToKMZ.getAction().setEnabled(false);
        btnCloseNC.getAction().setEnabled(false);
        cbBoxVariable.setModel(new DefaultComboBoxModel(new String[]{"None"}));
        setColorbarPanelEnabled(false);
    }

    @Action
    public Task openFolderAnimation() {

        File file = (null == outputFolder)
                ? new File(System.getProperty("user.dir"))
                : outputFolder;
        JFileChooser chooser = new JFileChooser(file);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnPath = chooser.showDialog(getFrame(), getResourceMap().getString("openFolderAnimation.dialog"));
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            return new OpenFolderAnimationTask(getApplication(), chooser.getSelectedFile());
        }
        return null;
    }

    private class OpenFolderAnimationTask extends SFTask {

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
                throw new NullPointerException(resourceMap.getString("openFolderAnimation.failed") + " " + folder.getAbsolutePath());
            }
        }

        public void onSuccess(Object o) {
            outputFolder = folder;
            lblFolder.setText(outputFolder.getName());
            lblFolder.setFont(lblFolder.getFont().deriveFont(Font.PLAIN, 12));
            replayPanel.setFolder(outputFolder);
            sliderTime.setMaximum(nbPNG - 1);
            setAnimationToolsEnabled(true);
            sliderTime.setValue(0);
            animate(true);
        }

        public void onFailure(Throwable t) {
            lblFolder.setText(resourceMap.getString("lblFolder.text"));
            lblFolder.setFont(lblFolder.getFont().deriveFont(Font.PLAIN, 12));
            sliderTime.setMaximum(0);
            sliderTime.setValue(0);
            setAnimationToolsEnabled(false);
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

    private class SimulationPreviewTask extends SFTask {

        SimulationPreviewTask(Application instance, boolean isEnabled) {
            super(instance);
        }

        @Override
        protected Object doInBackground() throws Exception {
            if (!isSetup) {
                setMessage(resourceMap.getString("previewSimulation.Action.setup.start"));
                getSimulationManager().setup();
            }
            return null;
        }

        protected void onSuccess(Object obj) {
            isSetup = true;
            setMessage(resourceMap.getString("previewSimulation.Action.setup.ok"));
            showSimulationPreview();
        }

        @Override
        void onFailure(Throwable throwable) {
            setMessage(resourceMap.getString("previewSimulation.Action.setup.failed"), false, Level.WARNING);
            btnPreview.setSelected(false);
        }
    }

    @Action
    public Task saveAsConfigurationFile() {
        JFileChooser fc = new JFileChooser(getSimulationManager().getConfigurationFile());
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileNameExtensionFilter(getResourceMap().getString("Application.configurationFile"), getResourceMap().getString("Application.configurationFile.extension")));
        fc.setSelectedFile(getSimulationManager().getConfigurationFile());
        int returnVal = fc.showSaveDialog(getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = addExtension(fc.getSelectedFile(), getResourceMap().getString("Application.configurationFile.extension"));
            try {
                IOTools.copyFile(getSimulationManager().getConfigurationFile(), file);
                return loadConfigurationFile(file);
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, null, ex);
            }
        }
        return null;
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

    private class SaveCfgFileTask extends SFTask {

        SaveCfgFileTask(Application instance) {
            super(instance);
            setMessage(resourceMap.getString("saveConfigurationFile.Action.pending") + " " + getSimulationManager().getConfigurationFile().getName() + "...");
        }

        @Override
        protected Object doInBackground() throws Exception {
            getSimulationManager().getParameterManager().cleanup();
            pnlConfiguration.updateXMLStructure();
            getSimulationManager().getParameterManager().save();
            getSimulationManager().setConfigurationFile(getSimulationManager().getConfigurationFile());
            return null;
        }

        @Override
        protected void onSuccess(Object o) {
            btnSaveCfgFile.getAction().setEnabled(false);
            isSetup = false;
            setMessage(getSimulationManager().getConfigurationFile().getName() + " " + resourceMap.getString("saveConfigurationFile.Action.finished"));
        }

        @Override
        void onFailure(Throwable throwable) {
            // do nothing
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
        getLogger().info(getResourceMap().getString("closeConfigurationFile.Action.finished") + getSimulationManager().getConfigurationFile().toString());
        try {
            getSimulationManager().setConfigurationFile(null);
        } catch (IOException ex) {
        }
        lblCfgFile.setText(getResourceMap().getString("lblCfgFile.text"));
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

        String msg = getResourceMap().getString("dialogSave.msg") + " " + getSimulationManager().getConfigurationFile().getName() + " ?";
        int answer = JOptionPane.showConfirmDialog(getFrame(), msg, getResourceMap().getString("dialogSave.title"), JOptionPane.YES_NO_CANCEL_OPTION);
        switch (answer) {
            case JOptionPane.YES_OPTION:
                btnSaveCfgFile.doClick();
                break;
        }
        return answer;
    }

    @Action
    public Task openConfigurationFile() {
        JFileChooser chooser = new JFileChooser(cfgPath);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileFilter(new FileNameExtensionFilter(getResourceMap().getString("Application.configurationFile"), getResourceMap().getString("Application.configurationFile.extension")));
        int returnPath = chooser.showOpenDialog(getFrame());
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            return loadConfigurationFile(chooser.getSelectedFile());
        }
        return null;
    }

    private class FailedTask extends SFTask {

        private Exception exception;

        FailedTask(Application instance, Exception exception) {
            super(instance);
            this.exception = exception;
        }

        @Override
        void onSuccess(Object result) {
            // never happens
        }

        @Override
        void onFailure(Throwable throwable) {
            // do nothing
        }

        @Override
        protected Object doInBackground() throws Exception {
            throw exception;
        }
    }

    private Logger getLogger() {
        return SimulationManager.getLogger();
    }

    private Task loadConfigurationFile(File file) {
        try {
            getSimulationManager().setConfigurationFile(file);
        } catch (IOException ex) {
            return new FailedTask(getApplication(), ex);
        }
        getLogger().info(getResourceMap().getString("loadConfigurationFile.opened") + " " + file.toString());
        getFrame().setTitle(getResourceMap().getString("Application.title") + " - " + file.getName());
        lblCfgFile.setText(file.getAbsolutePath());
        lblCfgFile.setFont(lblCfgFile.getFont().deriveFont(Font.PLAIN, 12));
        isSetup = false;
        saveAsMenuItem.getAction().setEnabled(true);
        closeMenuItem.getAction().setEnabled(true);
        btnSimulationRun.getAction().setEnabled(true);
        btnPreview.getAction().setEnabled(true);
        setMainTitle();
        return pnlConfiguration.loadBlockTree();
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
                statusBar.getProgressBar().setValue(0);
                statusBar.getProgressBar().setVisible(false);
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
        getLogger().info(getResourceMap().getString("animAction.started.message") + " " + getResourceMap().getString("animAction.speedup.message"));
        startAccelerationProgress();
    }

    public void end() {
        btnOpenAnimation.getAction().setEnabled(true);
        getLogger().info(getResourceMap().getString("animAction.stopped.message"));
    }

    public void repeat() {
    }

    private class ProgressAction implements ActionListener {

        float duration;
        int progress;

        ProgressAction() {
            duration = animator.getAcceleration() / 2;
            statusBar.getProgressBar().setVisible(true);
            statusBar.getProgressBar().setIndeterminate(false);
        }

        public void actionPerformed(ActionEvent e) {
            progress = (int) (100.f * animator.getTimingFraction() / duration);
            statusBar.getProgressBar().setValue(progress);
            if (progress >= 100) {
                progressTimer.stop();
                statusBar.getProgressBar().setVisible(false);
            }
        }
    }

    @Action
    public void exitApplication() {
        getContext().getActionMap().get("quit").actionPerformed(new ActionEvent(btnExit, 0, null));
    }

    @Action
    public void newConfigurationFile() {
        getLogger().info("New configuration file - not supported yet.");
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

    public class SimulationRunTask extends SFTask {

        JLabel lblProgress;
        private boolean bln;
        private boolean isInit;

        SimulationRunTask(Application instance) {
            super(instance);
            setMessage(resourceMap.getString("simulationRun.Action.started"));
            setMenuEnabled(false);
            bln = false;
            pnlProgress.setupProgress();
            btnSimulationRun.setIcon(resourceMap.getIcon("simulationRun.Action.icon.stop"));
            btnSimulationRun.setText(resourceMap.getString("simulationRun.Action.text.stop"));
            isRunning = true;
            btnSimulationRun.getAction().setEnabled(false);
            if (btnPreview.isSelected()) {
                btnPreview.doClick();
            }
            btnPreview.getAction().setEnabled(false);
            getSimulationManager().resetId();
            isSetup = false;
            isInit = false;
        }

        @Override
        protected Object doInBackground() throws Exception {
            getSimulationManager().resetTimerGlobal();
            do {
                setMessage(resourceMap.getString("simulationRun.Action.simulation") + getSimulationManager().indexSimulationToString());
                /* setup */
                setMessage(resourceMap.getString("simulationRun.Action.setup.start"), true, Level.INFO);
                getSimulationManager().setup();
                setMessage(resourceMap.getString("simulationRun.Action.setup.ok"));
                isSetup = true;
                /* initialization */
                setMessage(resourceMap.getString("simulationRun.Action.init.start"), true, Level.INFO);
                getSimulationManager().init();
                setMessage(resourceMap.getString("simulationRun.Action.init.ok"));
                isInit = true;
                /* */
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

        protected void onFailure(Throwable t) {
            if (!isSetup) {
                setMessage(resourceMap.getString("simulationRun.Action.setup.failed"), false, Level.WARNING);
            } else if (!isInit) {
                setMessage(resourceMap.getString("simulationRun.Action.init.failed"), false, Level.WARNING);
            }
            btnSimulationRun.getAction().setEnabled(true);
            finished();
        }

        @Override
        protected void process(List values) {
            if (!bln) {
                btnSimulationRun.getAction().setEnabled(true);
                bln = true;
            }
            pnlProgress.printProgress();
            if (getSimulationManager().isStopped()) {
                return;
            }
            StringBuffer msg = new StringBuffer();
            msg.append(getSimulationManager().getTimeManager().stepToString());
            msg.append(" - ");
            msg.append(resourceMap.getString("simulationRun.Action.time"));
            msg.append(" ");
            msg.append(getSimulationManager().getTimeManager().timeToString());
            setMessage(msg.toString());
        }

        @Override
        protected void cancelled() {
            getSimulationManager().stop();
            setMessage(resourceMap.getString("simulationRun.Action.interrupted"));
        }

        public void onSuccess(Object obj) {
            setMessage(resourceMap.getString("simulationRun.Action.completed"));
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
            pnlProgress.hideBars();
            pnlProgress.resetProgressBar();
            btnPreview.getAction().setEnabled(true);
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
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    private String beanFilename(Component bean) {
        String name = bean.getName();
        return (name == null) ? null : name + "." + getResourceMap().getString("preferences.filename");
    }

    public void restorePreferences() {
        Object property;

        property = restorePreference(openMenuItem);
        if (property != null) {
            File file = new File((String) property);
            if (file.isFile()) {
                cfgPath = file.getParentFile();
                loadConfigurationFile(file).execute();
            } else if (file.isDirectory()) {
                cfgPath = file;
            }
        } else {
            getLogger().info(getResourceMap().getString("restorePreferences.msg"));
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
            getLogger().log(Level.SEVERE, null, ex);
            return null;
        }
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
            getLogger().info(getResourceMap().getString("browse.Action.openurl") + " " + getResourceMap().getString("Application.homepage"));
        } catch (IOException ex) {
            getLogger().info(getResourceMap().getString("browse.Action.no-browser") + " " + getResourceMap().getString("Application.homepage"));
        }
    }

    private void setMainTitle() {

        pnlLogo.setVisible(false);
        String title = getResourceMap().getString("Application.title") + " - ";
        if (!taskPaneConfiguration.isCollapsed()) {
            if (null != getSimulationManager().getConfigurationFile()) {
                titledPanelMain.setTitle(title + taskPaneConfiguration.getTitle() + " - " + lblCfgFile.getText());
            } else {
                titledPanelMain.setTitle(title + taskPaneConfiguration.getTitle());
            }
        } else if (!taskPaneSimulation.isCollapsed()) {
            if (null != getSimulationManager().getConfigurationFile()) {
                titledPanelMain.setTitle(title + taskPaneSimulation.getTitle() + " - " + lblCfgFile.getText());
            } else {
                titledPanelMain.setTitle(title + taskPaneSimulation.getTitle());
            }
        } else if (!taskPaneMapping.isCollapsed()) {
            if (null != outputFile) {
                titledPanelMain.setTitle(title + taskPaneMapping.getTitle() + " - " + outputFile.getName());
            } else {
                titledPanelMain.setTitle(title + taskPaneMapping.getTitle());
            }
        } else if (!taskPaneAnimation.isCollapsed()) {
            if (null != outputFolder) {
                titledPanelMain.setTitle(title + taskPaneAnimation.getTitle() + " - " + outputFolder.getName());
            } else {
                titledPanelMain.setTitle(title + taskPaneAnimation.getTitle());
            }
        } else {
            titledPanelMain.setTitle(title);
            pnlLogo.setVisible(true);
        }
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

    private Color chooseColor(Component component, Color initial) {
        return JColorChooser.showDialog(component, "", initial);
    }

    @Action
    public void applyColorbarSettings() {
        String vname = (String) cbBoxVariable.getSelectedItem();
        float vmin = Float.valueOf(txtFieldMin.getText());
        float vmax = Float.valueOf(txtFieldMax.getText());
        Color cmin = btnColorMin.getForeground();
        Color cmax = btnColorMax.getForeground();
        wmsMapper.setColorbar(vname, vmin, vmax, cmin, cmax);
        getLogger().info(getResourceMap().getString("applyColorbarSettings.applied"));
    }

    @Action
    public Task autoRangeColorbar() {
        return new AutoRangeTask(getApplication(), (String) cbBoxVariable.getSelectedItem());
    }

    private class AutoRangeTask extends SFTask<float[], Object> {

        ResourceMap resourceMap = Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
        String variable;

        AutoRangeTask(Application instance, String variable) {
            super(instance);
            this.variable = variable;
        }

        @Override
        protected float[] doInBackground() throws Exception {
            if (variable.toLowerCase().matches("none")) {
                cancel(true);
            }
            setMessage(resourceMap.getString("applyColorbarSettings.range"), true, Level.INFO);
            return wmsMapper.getRange(variable);
        }

        @Override
        void onSuccess(float[] result) {
            txtFieldMin.setValue(result[0]);
            txtFieldMax.setValue(result[1]);
            setMessage(resourceMap.getString("applyColorbarSettings.suggested") + " [" + txtFieldMin.getText() + " : " + txtFieldMax.getText() + "]");
        }

        @Override
        void onFailure(Throwable throwable) {
            setMessage(resourceMap.getString("applyColorbarSettings.error") + " " + variable + " - " + throwable.getMessage());
        }

        @Override
        protected void cancelled() {
            setMessage(resourceMap.getString("applyColorbarSettings.cancelled"));
        }
    }

    private void setColorbarPanelEnabled(boolean enabled) {
        btnColor.setEnabled(enabled);
        cbBoxVariable.setEnabled(enabled);
        txtFieldMin.setEnabled(enabled);
        txtFieldMax.setEnabled(enabled);
        btnColorMin.setEnabled(enabled);
        btnColorMax.setEnabled(enabled);
        btnAutoRange.getAction().setEnabled(enabled);
        btnApplyColorbar.getAction().setEnabled(enabled);
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
        leftSplitPane = new javax.swing.JSplitPane();
        titledPanelSteps = new org.jdesktop.swingx.JXTitledPanel();
        stepsScrollPane = new javax.swing.JScrollPane();
        stepsPanel = new javax.swing.JPanel();
        taskPaneConfiguration = new org.jdesktop.swingx.JXTaskPane();
        pnlFile = new javax.swing.JPanel();
        lblCfgFile = new javax.swing.JLabel();
        btnNewCfgFile = new javax.swing.JButton();
        btnOpenCfgFile = new javax.swing.JButton();
        btnSaveCfgFile = new javax.swing.JButton();
        btnSaveAsCfgFile = new javax.swing.JButton();
        btnCloseCfgFile = new javax.swing.JButton();
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
        btnExportToKMZ = new javax.swing.JButton();
        pnlColor = new javax.swing.JPanel();
        pnlColorBar = new javax.swing.JPanel();
        lblVariable = new javax.swing.JLabel();
        cbBoxVariable = new javax.swing.JComboBox();
        lblMin = new javax.swing.JLabel();
        lblMax = new javax.swing.JLabel();
        txtFieldMax = new javax.swing.JFormattedTextField();
        btnAutoRange = new javax.swing.JButton();
        btnApplyColorbar = new javax.swing.JButton();
        btnColorMin = new javax.swing.JButton();
        btnColorMax = new javax.swing.JButton();
        txtFieldMin = new javax.swing.JFormattedTextField();
        lblColor = new javax.swing.JLabel();
        btnColor = new javax.swing.JButton();
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
        jScrollPane3 = new javax.swing.JScrollPane();
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
        exportToKMZMenuItem = new javax.swing.JMenuItem();
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
        scrollPaneSimulationUI = new javax.swing.JScrollPane();
        pnlSimulationUI = new SimulationUI();
        btnExit = new javax.swing.JButton();
        pnlLogo = new org.jdesktop.swingx.JXPanel();
        hyperLinkLogo = new org.jdesktop.swingx.JXHyperlink();

        mainPanel.setName("mainPanel"); // NOI18N

        splitPane.setName("splitPane"); // NOI18N

        leftSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setResizeWeight(1.0);
        leftSplitPane.setName("leftSplitPane"); // NOI18N

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(IchthyopView.class);
        titledPanelSteps.setTitle(resourceMap.getString("titledPanelSteps.title")); // NOI18N
        titledPanelSteps.setMinimumSize(new java.awt.Dimension(200, 200));
        titledPanelSteps.setName("titledPanelSteps"); // NOI18N

        stepsScrollPane.setName("stepsScrollPane"); // NOI18N
        stepsScrollPane.setPreferredSize(new java.awt.Dimension(300, 400));

        stepsPanel.setName("stepsPanel"); // NOI18N

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

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance().getContext().getActionMap(IchthyopView.class, this);
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
                        .addComponent(btnSaveAsCfgFile)))
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taskPaneConfiguration.getContentPane().add(pnlFile);

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

        btnExportToKMZ.setAction(actionMap.get("exportToKMZ")); // NOI18N
        btnExportToKMZ.setName("btnExportToKMZ"); // NOI18N

        pnlColor.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlColor.border.title"))); // NOI18N
        pnlColor.setOpaque(false);
        pnlColor.setName("pnlColor"); // NOI18N

        pnlColorBar.setBorder(javax.swing.BorderFactory.createTitledBorder(resourceMap.getString("pnlColorBar.border.title"))); // NOI18N
        pnlColorBar.setName("pnlColorBar"); // NOI18N
        pnlColorBar.setOpaque(false);

        lblVariable.setText(resourceMap.getString("lblVariable.text")); // NOI18N
        lblVariable.setName("lblVariable"); // NOI18N

        cbBoxVariable.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "None" }));
        cbBoxVariable.setName("cbBoxVariable"); // NOI18N

        lblMin.setText(resourceMap.getString("lblMin.text")); // NOI18N
        lblMin.setName("lblMin"); // NOI18N

        lblMax.setText(resourceMap.getString("lblMax.text")); // NOI18N
        lblMax.setName("lblMax"); // NOI18N

        txtFieldMax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.###"))));
        txtFieldMax.setName("txtFieldMax"); // NOI18N
        NumberFormat floatFormat = NumberFormat.getNumberInstance(Locale.US);
        floatFormat.setGroupingUsed(false);
        NumberFormatter floatFormatter = new NumberFormatter(floatFormat);
        floatFormatter.setFormat(floatFormat);
        txtFieldMax.setFormatterFactory(new DefaultFormatterFactory(floatFormatter));
        txtFieldMax.setValue(new Float(100.f));

        btnAutoRange.setAction(actionMap.get("autoRangeColorbar")); // NOI18N
        btnAutoRange.setName("btnAutoRange"); // NOI18N

        btnApplyColorbar.setAction(actionMap.get("applyColorbarSettings")); // NOI18N
        btnApplyColorbar.setName("btnApplyColorbar"); // NOI18N

        btnColorMin.setForeground(resourceMap.getColor("btnColorMin.foreground")); // NOI18N
        btnColorMin.setIcon(resourceMap.getIcon("btnColorMin.icon")); // NOI18N
        btnColorMin.setText(resourceMap.getString("btnColorMin.text")); // NOI18N
        btnColorMin.setName("btnColorMin"); // NOI18N
        btnColorMin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnColorMinActionPerformed(evt);
            }
        });

        btnColorMax.setForeground(resourceMap.getColor("btnColorMax.foreground")); // NOI18N
        btnColorMax.setIcon(resourceMap.getIcon("btnColorMax.icon")); // NOI18N
        btnColorMax.setText(resourceMap.getString("btnColorMax.text")); // NOI18N
        btnColorMax.setName("btnColorMax"); // NOI18N
        btnColorMax.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnColorMaxActionPerformed(evt);
            }
        });

        txtFieldMin.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        txtFieldMin.setName("txtFieldMin"); // NOI18N
        txtFieldMin.setFormatterFactory(new DefaultFormatterFactory(floatFormatter));
        txtFieldMin.setValue(new Float(0.f));

        javax.swing.GroupLayout pnlColorBarLayout = new javax.swing.GroupLayout(pnlColorBar);
        pnlColorBar.setLayout(pnlColorBarLayout);
        pnlColorBarLayout.setHorizontalGroup(
            pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlColorBarLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(pnlColorBarLayout.createSequentialGroup()
                        .addComponent(lblVariable)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(cbBoxVariable, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(pnlColorBarLayout.createSequentialGroup()
                        .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(lblMin)
                            .addComponent(lblMax))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(txtFieldMax)
                            .addComponent(txtFieldMin, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(btnColorMax)
                            .addComponent(btnColorMin)))
                    .addGroup(pnlColorBarLayout.createSequentialGroup()
                        .addComponent(btnAutoRange)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnApplyColorbar)))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        pnlColorBarLayout.setVerticalGroup(
            pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlColorBarLayout.createSequentialGroup()
                .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblVariable)
                    .addComponent(cbBoxVariable, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblMin)
                    .addComponent(btnColorMin)
                    .addComponent(txtFieldMin, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(txtFieldMax, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(lblMax)
                    .addComponent(btnColorMax))
                .addGap(16, 16, 16)
                .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnAutoRange)
                    .addComponent(btnApplyColorbar))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblColor.setText(resourceMap.getString("lblColor.text")); // NOI18N
        lblColor.setName("lblColor"); // NOI18N

        btnColor.setForeground(resourceMap.getColor("btnColor.foreground")); // NOI18N
        btnColor.setIcon(resourceMap.getIcon("btnColor.icon")); // NOI18N
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
            .addGroup(pnlColorLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlColorBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlColorLayout.createSequentialGroup()
                        .addComponent(lblColor)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnColor)))
                .addContainerGap())
        );
        pnlColorLayout.setVerticalGroup(
            pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlColorLayout.createSequentialGroup()
                .addGroup(pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lblColor)
                    .addComponent(btnColor))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlColorBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout pnlMappingLayout = new javax.swing.GroupLayout(pnlMapping);
        pnlMapping.setLayout(pnlMappingLayout);
        pnlMappingLayout.setHorizontalGroup(
            pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMappingLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pnlWMS, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(pnlMappingLayout.createSequentialGroup()
                        .addComponent(btnMapping)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnExportToKMZ))
                    .addGroup(pnlMappingLayout.createSequentialGroup()
                        .addComponent(btnOpenNC)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(btnCloseNC))
                    .addComponent(lblNC)
                    .addComponent(btnCancelMapping)
                    .addComponent(pnlColor, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        pnlMappingLayout.setVerticalGroup(
            pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlMappingLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlColor, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnMapping)
                    .addComponent(btnExportToKMZ))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(btnCancelMapping)
                .addGap(12, 12, 12)
                .addGroup(pnlMappingLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(btnOpenNC)
                    .addComponent(btnCloseNC))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(lblNC)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(pnlWMS, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taskPaneMapping.getContentPane().add(pnlMapping);

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

        javax.swing.GroupLayout stepsPanelLayout = new javax.swing.GroupLayout(stepsPanel);
        stepsPanel.setLayout(stepsPanelLayout);
        stepsPanelLayout.setHorizontalGroup(
            stepsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(taskPaneConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, 466, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(taskPaneSimulation, javax.swing.GroupLayout.PREFERRED_SIZE, 466, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(taskPaneMapping, javax.swing.GroupLayout.PREFERRED_SIZE, 466, javax.swing.GroupLayout.PREFERRED_SIZE)
            .addComponent(taskPaneAnimation, javax.swing.GroupLayout.PREFERRED_SIZE, 466, javax.swing.GroupLayout.PREFERRED_SIZE)
        );
        stepsPanelLayout.setVerticalGroup(
            stepsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(stepsPanelLayout.createSequentialGroup()
                .addComponent(taskPaneConfiguration, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(taskPaneSimulation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(taskPaneMapping, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(taskPaneAnimation, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        stepsScrollPane.setViewportView(stepsPanel);

        javax.swing.GroupLayout titledPanelStepsLayout = new javax.swing.GroupLayout(titledPanelSteps.getContentContainer());
        titledPanelSteps.getContentContainer().setLayout(titledPanelStepsLayout);
        titledPanelStepsLayout.setHorizontalGroup(
            titledPanelStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(stepsScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
        );
        titledPanelStepsLayout.setVerticalGroup(
            titledPanelStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(stepsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 168, Short.MAX_VALUE)
        );

        leftSplitPane.setLeftComponent(titledPanelSteps);

        titledPanelLogger.setTitle(resourceMap.getString("titledPanelLogger.title")); // NOI18N
        titledPanelLogger.setName("titledPanelLogger"); // NOI18N

        loggerScrollPane.setName("loggerScrollPane"); // NOI18N

        javax.swing.GroupLayout titledPanelLoggerLayout = new javax.swing.GroupLayout(titledPanelLogger.getContentContainer());
        titledPanelLogger.getContentContainer().setLayout(titledPanelLoggerLayout);
        titledPanelLoggerLayout.setHorizontalGroup(
            titledPanelLoggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loggerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE)
        );
        titledPanelLoggerLayout.setVerticalGroup(
            titledPanelLoggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(loggerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
        );

        leftSplitPane.setRightComponent(titledPanelLogger);

        splitPane.setLeftComponent(leftSplitPane);

        titledPanelMain.setTitle(resourceMap.getString("titledPanelMain.title")); // NOI18N
        titledPanelMain.setMinimumSize(new java.awt.Dimension(32, 32));
        titledPanelMain.setName("titledPanelMain"); // NOI18N

        jScrollPane3.setName("jScrollPane3"); // NOI18N

        gradientPanel.setName("gradientPanel"); // NOI18N

        javax.swing.GroupLayout gradientPanelLayout = new javax.swing.GroupLayout(gradientPanel);
        gradientPanel.setLayout(gradientPanelLayout);
        gradientPanelLayout.setHorizontalGroup(
            gradientPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 683, Short.MAX_VALUE)
        );
        gradientPanelLayout.setVerticalGroup(
            gradientPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 489, Short.MAX_VALUE)
        );

        jScrollPane3.setViewportView(gradientPanel);
        createMainPanel();

        javax.swing.GroupLayout titledPanelMainLayout = new javax.swing.GroupLayout(titledPanelMain.getContentContainer());
        titledPanelMain.getContentContainer().setLayout(titledPanelMainLayout);
        titledPanelMainLayout.setHorizontalGroup(
            titledPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 659, Short.MAX_VALUE)
        );
        titledPanelMainLayout.setVerticalGroup(
            titledPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 392, Short.MAX_VALUE)
        );

        splitPane.setRightComponent(titledPanelMain);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 869, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
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

        exportToKMZMenuItem.setAction(actionMap.get("exportToKMZ")); // NOI18N
        exportToKMZMenuItem.setName("exportToKMZMenuItem"); // NOI18N
        mappingMenu.add(exportToKMZMenuItem);

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

        btnExit.setAction(actionMap.get("exitApplication")); // NOI18N
        btnExit.setName("btnExit"); // NOI18N

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

        setComponent(mainPanel);
        setMenuBar(menuBar);
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
                    btnExportToKMZ.getAction().setEnabled(true);
                    btnCloseNC.getAction().setEnabled(true);
                    setColorbarPanelEnabled(true);
                    cbBoxVariable.setModel(new DefaultComboBoxModel(wmsMapper.getVariableList()));
                } else {
                    wmsMapper.setFile(null);
                    wmsMapper.setVisible(false);
                    lblMapping.setVisible(true);
                    btnMapping.getAction().setEnabled(false);
                    btnExportToKMZ.getAction().setEnabled(false);
                    btnCloseNC.getAction().setEnabled(false);
                    setColorbarPanelEnabled(false);
                    cbBoxVariable.setModel(new DefaultComboBoxModel(new String[]{"None"}));
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

    private void btnColorMinActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnColorMinActionPerformed
        // TODO add your handling code here:
        JButton btn = (JButton) evt.getSource();
        btn.setForeground(chooseColor(btn, btn.getForeground()));
    }//GEN-LAST:event_btnColorMinActionPerformed

    private void btnColorMaxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnColorMaxActionPerformed
        // TODO add your handling code here:
        JButton btn = (JButton) evt.getSource();
        btn.setForeground(chooseColor(btn, btn.getForeground()));
    }//GEN-LAST:event_btnColorMaxActionPerformed

    private void btnColorActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnColorActionPerformed
        // TODO add your handling code here:
        JButton btn = (JButton) evt.getSource();
        btn.setForeground(chooseColor(btn, btn.getForeground()));
        wmsMapper.setDefaultColor(btn.getForeground());
    }//GEN-LAST:event_btnColorActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem animactionMenuItem;
    private javax.swing.JMenu animationMenu;
    private javax.swing.JSpinner animationSpeed;
    private javax.swing.JButton btnAnimaction;
    private javax.swing.JButton btnApplyColorbar;
    private javax.swing.JButton btnAutoRange;
    private javax.swing.JButton btnCancelMapping;
    private javax.swing.JButton btnCloseCfgFile;
    private javax.swing.JButton btnCloseNC;
    private javax.swing.JButton btnColor;
    private javax.swing.JButton btnColorMax;
    private javax.swing.JButton btnColorMin;
    private javax.swing.JButton btnDeleteMaps;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnExportMaps;
    private javax.swing.JButton btnExportToKMZ;
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
    private javax.swing.JButton btnSaveAsCfgFile;
    private javax.swing.JButton btnSaveCfgFile;
    private javax.swing.JButton btnSimulationRun;
    private javax.swing.JMenuItem cancelMapMenuItem;
    private javax.swing.JComboBox cbBoxVariable;
    private javax.swing.JComboBox cbBoxWMS;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem exportMenuItem;
    private javax.swing.JMenuItem exportToKMZMenuItem;
    private org.previmer.ichthyop.ui.GradientPanel gradientPanel;
    private org.jdesktop.swingx.JXHyperlink hyperLinkLogo;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JLabel lblAnimationSpeed;
    private javax.swing.JLabel lblCfgFile;
    private javax.swing.JLabel lblColor;
    private javax.swing.JLabel lblFolder;
    private javax.swing.JLabel lblFramePerSecond;
    private javax.swing.JLabel lblMax;
    private javax.swing.JLabel lblMin;
    private javax.swing.JLabel lblNC;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblVariable;
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
    private javax.swing.JPanel pnlColor;
    private javax.swing.JPanel pnlColorBar;
    private javax.swing.JPanel pnlFile;
    private org.jdesktop.swingx.JXPanel pnlLogo;
    private javax.swing.JPanel pnlMapping;
    private javax.swing.JPanel pnlSimulation;
    private javax.swing.JPanel pnlSimulationUI;
    private javax.swing.JPanel pnlWMS;
    private javax.swing.JMenuItem previewMenuItem;
    private javax.swing.JMenuItem saveAsMenuItem;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JScrollPane scrollPaneSimulationUI;
    private javax.swing.JMenuItem simulactionMenuItem;
    private javax.swing.JMenu simulationMenu;
    private javax.swing.JSlider sliderTime;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JPanel stepsPanel;
    private javax.swing.JScrollPane stepsScrollPane;
    private org.jdesktop.swingx.JXTaskPane taskPaneAnimation;
    private org.jdesktop.swingx.JXTaskPane taskPaneConfiguration;
    private org.jdesktop.swingx.JXTaskPane taskPaneMapping;
    private org.jdesktop.swingx.JXTaskPane taskPaneSimulation;
    private org.jdesktop.swingx.JXTitledPanel titledPanelLogger;
    private org.jdesktop.swingx.JXTitledPanel titledPanelMain;
    private org.jdesktop.swingx.JXTitledPanel titledPanelSteps;
    private javax.swing.JFormattedTextField txtFieldMax;
    private javax.swing.JFormattedTextField txtFieldMin;
    // End of variables declaration//GEN-END:variables
    private JDialog aboutBox;
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
    private JConfigurationPanel pnlConfiguration = new JConfigurationPanel();
    private JStatusBar statusBar = new JStatusBar();
    private JRunProgressPanel pnlProgress = new JRunProgressPanel();
    ResourceMap resourceMap = Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
}
