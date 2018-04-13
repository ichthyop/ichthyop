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

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.ichthyop.util.IOTools;
import org.ichthyop.manager.SimulationManager;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JToolBar;
import javax.swing.JViewport;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.ichthyop.Template;
import org.ichthyop.output.ExportToKML;
import org.ichthyop.ui.logging.LogLevel;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.ichthyop.util.MetaFilenameFilter;
import org.jdesktop.swingx.JXHyperlink;
import org.jdesktop.swingx.JXPanel;
import org.jdesktop.swingx.JXTaskPane;
import org.jdesktop.swingx.JXTitledPanel;

/**
 * The application's main frame.
 */
public class IchthyopView extends FrameView {

    // configuration variables
    private File cfgPath = new File(System.getProperty("user.dir"));
    private boolean isRunning = false;
    private Task simulActionTask;
    private Task previewTask;
    private Task createMapTask;
    private Task kmzTask;
    private boolean initDone;
    private Animator animator;
    private float nbfps = 1.f;
    private float time;
    private File outputFile, outputFolder;
    private TimeDirection animationDirection;
    private final AnimationMouseWheelScroller animationMouseScroller = new AnimationMouseWheelScroller();
    private final PreviewMouseAdapter previewMouseAdapter = new PreviewMouseAdapter();
    private boolean cfgUntitled = true;
    private ResourceMap resourceMap;
    private ActionMap actionMap;

    public IchthyopView(SingleFrameApplication app) {
        super(app);

        initAnimator();
        initComponents();
        setStatusBar(statusBar);

        /* Plug the loggerPanel and the StatusBar */
        loggerScrollPane.connectToLogger(getLogger());
        statusBar.connectToLogger(getLogger());
        /* Welcome user */
        getLogger().info(resourceMap.getString("Application.msg.welcome"));

        /* Set frame icon */
        getFrame().setIconImage(resourceMap.getImageIcon("Application.icon").getImage());

        /* Disabled some actions */
        closeMenuItem.getAction().setEnabled(false);
        saveAsMenuItem.getAction().setEnabled(false);
        reloadMenuItem.getAction().setEnabled(false);
        btnSimulationRun.getAction().setEnabled(false);
        btnPreview.getAction().setEnabled(false);
        sliderPreviewZoom.setEnabled(false);
        btnSavePreview.getAction().setEnabled(false);
        btnCancelMapping.getAction().setEnabled(false);
        btnMapping.getAction().setEnabled(false);
        btnCloseNC.getAction().setEnabled(false);
        btnExportToKMZ.getAction().setEnabled(false);
        cbBoxWMS.getAction().setEnabled(false);
        setAnimationToolsEnabled(false);
        setColorbarPanelEnabled(false);

        /* Add some listeners */
        getApplication().addExitListener(new ConfirmExit());
    }

    ////////////////
    // CONFIGURATION
    ////////////////
    @Action
    public Task newConfigurationFile() throws IOException {

        // close any opened configuration file (and save if necessary)
        closeConfigurationFile();

        // create a temp file with generic template
        cfgUntitled = true;
        return loadConfigurationFile(Template.createTemplate());
    }

    @Action
    public Task openConfigurationFile() {
        JFileChooser chooser = new JFileChooser(cfgPath);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        int returnPath = chooser.showOpenDialog(getFrame());
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            File file = chooser.getSelectedFile();
            cfgUntitled = false;
            loadConfigurationFile(file).execute();
            cfgPath = chooser.getCurrentDirectory();
        }
        return null;
    }

    @Action
    public void closeNetCDF() throws IOException {

        getLogger().log(Level.INFO, "{0} {1}", new Object[]{getResourceMap().getString("closeNetCDF.msg.closed"), outputFile.getAbsolutePath()});
        outputFile = null;
        lblNC.setText(getResourceMap().getString("lblNC.text"));
        lblNC.setFont(lblNC.getFont().deriveFont(Font.PLAIN, 12));
        cbBoxWMS.setSelectedIndex(0);
        wmsMapper.loadFile(null);
        wmsMapper.setVisible(false);
        lblMapping.setVisible(true);
        btnMapping.getAction().setEnabled(false);
        btnExportToKMZ.getAction().setEnabled(false);
        cbBoxWMS.getAction().setEnabled(false);
        btnCloseNC.getAction().setEnabled(false);
        cbBoxVariable.setModel(new DefaultComboBoxModel(new String[]{"None"}));
        setColorbarPanelEnabled(false);
    }

    public Task loadConfigurationFile(File file) {
        try {
            getSimulationManager().setConfigurationFile(file);
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
            return null;
        }
        File cfgFile = getSimulationManager().getConfigurationFile();
        getLogger().log(Level.INFO, "{0} {1}", new Object[]{getResourceMap().getString("openConfigurationFile.msg.opened"), cfgUntitled ? "Untitled configuration" : cfgFile.toString()});
        getFrame().setTitle(getResourceMap().getString("Application.title") + " - " + (cfgUntitled ? "Untitled configuration" : cfgFile.toString()));
        lblCfgFile.setText((cfgUntitled ? "Filename not set yet" : cfgFile.getName()));
        lblCfgFile.setFont(lblCfgFile.getFont().deriveFont(Font.PLAIN, 12));
        initDone = false;
        saveAsMenuItem.getAction().setEnabled(true);
        saveMenuItem.getAction().setEnabled(cfgUntitled);
        reloadMenuItem.getAction().setEnabled(true);
        closeMenuItem.getAction().setEnabled(true);
        btnSimulationRun.getAction().setEnabled(true);
        btnPreview.getAction().setEnabled(true);
        sliderPreviewZoom.setValue(SimulationPreviewPanel.DEFAULT_SIZE);
        setMainTitle();
        return pnlConfiguration.loadParameterTree();
    }

    @Action
    public Task saveConfigurationFile() {
        return cfgUntitled ? saveAsConfigurationFile() : new SaveCfgFileTask(getApplication());
    }

    private class SaveCfgFileTask extends SFTask {

        SaveCfgFileTask(Application instance) {
            super(instance);
        }

        @Override
        protected Object doInBackground() throws Exception {
            getSimulationManager().getParameterManager().saveParameters();
            getSimulationManager().setConfigurationFile(getSimulationManager().getConfigurationFile());
            return null;
        }

        @Override
        protected void onSuccess(Object o) {
            btnSaveCfgFile.getAction().setEnabled(false);
            cfgUntitled = false;
            initDone = false;
            setMessage(resourceMap.getString("saveConfigurationFile.msg.finished") + " " + getSimulationManager().getConfigurationFile().getName(), false, LogLevel.COMPLETE);
        }

        @Override
        void onFailure(Throwable throwable) {
            // do nothing
        }
    }

    @Action
    public Task saveAsConfigurationFile() {
        File cwd = cfgUntitled ? getDefaultCfgPath() : getSimulationManager().getConfigurationFile();
        JFileChooser fc = new JFileChooser(cwd);
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setAcceptAllFileFilterUsed(false);
        fc.setSelectedFile(cwd);
        int returnVal = fc.showSaveDialog(getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();
            if (file.getAbsolutePath().equals(getSimulationManager().getConfigurationFile().getAbsolutePath())) {
                JOptionPane.showMessageDialog(fc, getResourceMap().getString("saveAsConfigurationFile.msg.different"), getResourceMap().getString("saveAsConfigurationFile.Action.text"), JOptionPane.OK_OPTION);
                saveAsConfigurationFile();
                return null;
            } else if (file.exists()) {
                StringBuilder sb = new StringBuilder();
                sb.append(file.toString());
                sb.append(" ");
                sb.append(getResourceMap().getString("saveAsConfigurationFile.msg.exist"));
                sb.append("\n");
                sb.append(getResourceMap().getString("save.msg.overwrite"));
                int answer = JOptionPane.showConfirmDialog(fc, sb.toString(), getResourceMap().getString("saveAsConfigurationFile.Action.text"), JOptionPane.YES_NO_OPTION);
                if (answer != JOptionPane.YES_OPTION) {
                    saveAsConfigurationFile();
                    return null;
                }
            }
            try {
                IOTools.copyFile(getSimulationManager().getConfigurationFile(), file);
                cfgUntitled = false;
                StringBuilder sb = new StringBuilder();
                sb.append(getResourceMap().getString("saveAsConfigurationFile.msg.prefix"));
                sb.append(" ");
                sb.append(getSimulationManager().getConfigurationFile().getName());
                sb.append(" ");
                sb.append(getResourceMap().getString("saveAsConfigurationFile.msg.saved"));
                sb.append(" ");
                sb.append(file.getName());
                getLogger().info(sb.toString());
                return loadConfigurationFile(file);
            } catch (IOException ex) {
                getLogger().log(Level.SEVERE, getResourceMap().getString("saveAsConfigurationFile.msg.failed"), ex);
            }

        }
        return null;
    }
    
    @Action
    public Task reloadConfigurationFile() {
        
        // current configuration file
        File file = getSimulationManager().getConfigurationFile();
        
        // close it
        closeConfigurationFile();
        
        // open it again
        return loadConfigurationFile(file);
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
        getLogger().log(Level.INFO, "{0} {1}", new Object[]{getResourceMap().getString("closeConfigurationFile.msg.finished"), getSimulationManager().getConfigurationFile().toString()});
        try {
            getSimulationManager().setConfigurationFile(null);
        } catch (Exception ex) {
        }
        getFrame().setTitle(getResourceMap().getString("Application.title"));
        lblCfgFile.setText(getResourceMap().getString("lblCfgFile.text"));
        lblCfgFile.setFont(lblCfgFile.getFont().deriveFont(12));
        btnSimulationRun.getAction().setEnabled(false);
        btnSaveAsCfgFile.getAction().setEnabled(false);
        btnSaveCfgFile.getAction().setEnabled(false);
        btnReloadCfgFile.getAction().setEnabled(false);
        closeMenuItem.getAction().setEnabled(false);
        pnlConfiguration.setVisible(false);
        lblConfiguration.setVisible(true);
        btnPreview.getAction().setEnabled(false);
        sliderPreviewZoom.setValue(SimulationPreviewPanel.DEFAULT_SIZE);
        sliderPreviewZoom.setEnabled(false);
        btnSavePreview.getAction().setEnabled(false);
        setMainTitle();
    }

    private boolean savePending() {
        return btnSaveCfgFile.isEnabled();
    }

    private int dialogSave() {

        String msg = getResourceMap().getString("dialogSave.msg.save") + " " + getSimulationManager().getConfigurationFile().getName() + " ?";
        int answer = JOptionPane.showConfirmDialog(getFrame(), msg, getResourceMap().getString("dialogSave.title"), JOptionPane.YES_NO_CANCEL_OPTION);
        switch (answer) {
            case JOptionPane.YES_OPTION:
                btnSaveCfgFile.doClick();
                break;
        }
        return answer;
    }

    /////////////////////
    // SIMULATION PREVIEW
    /////////////////////
    @Action
    public Task previewSimulation() {
        taskPaneSimulation.setCollapsed(false);
        if (!isRunning) {
            return previewTask = new SimulationPreviewTask(getApplication());
        } else {
            previewTask.cancel(true);
            return null;
        }
    }

    private void showSimulationPreview() {
        lblSimulation.setVisible(false);
        previewScrollPane.setVisible(true);
        previewScrollPane.setPreferredSize(gradientPanel.getSize());
        previewPanel.setHeight(sliderPreviewZoom.getValue());
        sliderPreviewZoom.setEnabled(true);
        btnSavePreview.getAction().setEnabled(true);
    }

    private void hideSimulationPreview() {
        previewScrollPane.setVisible(false);
        sliderPreviewZoom.setEnabled(false);
        btnSavePreview.getAction().setEnabled(false);
    }

    private class SimulationPreviewTask extends SFTask {

        SimulationPreviewTask(Application instance) {
            super(instance);
            isRunning = true;
            btnSimulationRun.getAction().setEnabled(false);
            btnPreview.setIcon(resourceMap.getIcon("previewSimulation.Action.cancel.icon"));
            btnPreview.setText(resourceMap.getString("previewSimulation.Action.cancel.text"));
        }

        @Override
        protected Object doInBackground() throws Exception {
            if (!initDone) {
                setMessage(resourceMap.getString("simulationRun.msg.init.start"));
                getSimulationManager().setup();
                getSimulationManager().init();
                setMessage(resourceMap.getString("simulationRun.msg.init.ok"));
                setMessage("[preview] Loading bathymetric data...", true, LogLevel.COMPLETE);
                previewPanel.init();
                setMessage("[preview] Initialisation OK", false, LogLevel.COMPLETE);
                initDone = true;
            }
            return null;
        }

        @Override
        protected void onSuccess(Object obj) {
            showSimulationPreview();
        }

        @Override
        void onFailure(Throwable throwable) {
            StringBuilder msg = new StringBuilder();
            msg.append(resourceMap.getString("previewSimulation.msg.failed"));
            setMessage(msg.toString(), false, Level.SEVERE);
        }

        @Override
        protected void cancelled() {
            setMessage(resourceMap.getString("previewSimulation.msg.interrupted"));
        }

        @Override
        protected void finished() {
            isRunning = false;
            btnSimulationRun.getAction().setEnabled(true);
            btnPreview.setIcon(resourceMap.getIcon("previewSimulation.Action.icon"));
            btnPreview.setText(resourceMap.getString("previewSimulation.Action.text"));
        }
    }

    @Action
    public void savePreview() {
        File cwd = cfgUntitled ? getDefaultCfgPath() : getSimulationManager().getConfigurationFile();
        JFileChooser fc = new JFileChooser(cwd);
        fc.setDialogType(JFileChooser.SAVE_DIALOG);
        fc.setAcceptAllFileFilterUsed(false);
        fc.addChoosableFileFilter(new PNGSaveFilter());
        String pngfile = cwd.isFile() ? changeExtension(cwd.getAbsolutePath(), "png") : "preview.png";
        fc.setSelectedFile(new File(pngfile));
        int returnVal = fc.showSaveDialog(getFrame());
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            try {
                File file = fc.getSelectedFile();
                if (file.isDirectory() || !file.getName().toLowerCase().endsWith("png")) {
                    JOptionPane.showMessageDialog(fc, getResourceMap().getString("savePreview.msg.png"), getResourceMap().getString("savePreview.Action.text"), JOptionPane.OK_OPTION);
                    savePreview();
                    return;
                } else if (file.exists()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(file.getName());
                    sb.append(" ");
                    sb.append(getResourceMap().getString("save.msg.overwrite"));
                    int answer = JOptionPane.showConfirmDialog(fc, sb.toString(), getResourceMap().getString("savePreview.Action.text"), JOptionPane.YES_NO_OPTION);
                    if (answer != JOptionPane.YES_OPTION) {
                        savePreview();
                        return;
                    }
                }
                BufferedImage bi = new BufferedImage(previewPanel.getWidth(), previewPanel.getHeight(), BufferedImage.TYPE_INT_RGB);
                Graphics g = bi.getGraphics();
                previewPanel.paint(g);
                ImageIO.write(bi, "PNG", file);
                getLogger().log(Level.INFO, "[preview] Simulation preview saved as {0}", file.getName());
            } catch (IOException ex) {
                getLogger().log(Level.WARNING, "Failed to save simulation preview", ex);
            }
        }
    }

    /////////////////
    // SIMULATION RUN
    /////////////////
    @Action
    public Task simulationRun() {
        if (!isRunning) {
            return simulActionTask = new SimulationRunTask(getApplication());
        } else {
            simulActionTask.cancel(true);
            return null;
        }
    }

    public class SimulationRunTask extends SFTask {

        private boolean setupSucceeded;
        private boolean initSucceeded;
        private final String title;

        SimulationRunTask(Application instance) {
            super(instance);
            setMessage(resourceMap.getString("simulationRun.msg.started"));
            hideSimulationPreview();
            lblSimulation.setVisible(true);
            setMenuEnabled(false);
            btnSimulationRun.setIcon(resourceMap.getIcon("simulationRun.Action.icon.stop"));
            btnSimulationRun.setText(resourceMap.getString("simulationRun.Action.text.stop"));
            isRunning = true;
            getSimulationManager().resetId();
            setupSucceeded = false;
            initSucceeded = false;
            title = getFrame().getTitle();
        }

        @Override
        protected Object doInBackground() throws Exception {
            getSimulationManager().resetTimer();

            /* setup */
            setMessage(resourceMap.getString("simulationRun.msg.init.start"), true, Level.INFO);
            getSimulationManager().setup();
            setupSucceeded = true;
            /* initialization */
            getSimulationManager().init();
            initSucceeded = true;
            setMessage(resourceMap.getString("simulationRun.msg.init.ok"));
            getSimulationManager().getTimeManager().firstStepTriggered();
            getSimulationManager().resetTimer();
            do {
                /* check whether the simulation has been interrupted by user */
                if (getSimulationManager().isStopped()) {
                    break;
                }
                /* Print message progress */
                StringBuilder msg = new StringBuilder();
                msg.append(getSimulationManager().getTimeManager().stepToString());
                if (getSimulationManager().getTimeManager().index() % 10 == 0) {
                    msg.append(" (");
                    msg.append(resourceMap.getString("simulationRun.msg.time"));
                    msg.append(" ");
                    msg.append(getSimulationManager().getTimeManager().timeToString());
                    msg.append(")");
                }
                // Display current step 
                setMessage(msg.toString());
                /* step simulation */
                getSimulationManager().getSimulation().step();
                // Publish progress when current step is over
                setProgress(getSimulationManager().progress());
                publish(getSimulationManager().progress());

            } while (getSimulationManager().getTimeManager().hasNextStep());
            return null;
        }

        @Override
        protected void onFailure(Throwable t) {
            StringBuilder msg = new StringBuilder();
            msg.append(resourceMap.getString("simulationRun.msg.init.failed"));
            if (!setupSucceeded) {
                msg.append(" (performing: SETUP)");
            } else if (!initSucceeded) {
                msg.append(" (performing: INIT)");
            }
            setMessage(msg.toString(), false, Level.SEVERE);
            setupSucceeded = initSucceeded = false;
        }

        @Override
        protected void process(List values) {
            if (getSimulationManager().isStopped()) {
                return;
            }
            if (getProgress() > 0) {
                btnSimulationRun.getAction().setEnabled(true);
            }
            StringBuilder sb = new StringBuilder();
            sb.append("(");
            sb.append(getProgress());
            sb.append("%) ");
            sb.append(getResourceMap().getString("Application.title"));
            getFrame().setTitle(sb.toString());
        }

        @Override
        protected void cancelled() {
            getSimulationManager().stop();
            setMessage(resourceMap.getString("simulationRun.msg.interrupted"));
        }

        @Override
        public void onSuccess(Object obj) {
            setMessage(resourceMap.getString("simulationRun.msg.completed"), false, LogLevel.COMPLETE);
            outputFile = new File(getSimulationManager().getOutputManager().getFileLocation());
            try {
                openNetCDF();
            } catch (IOException ex) {
                Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, "Failed to open NetCDF output file", ex);
            }
            taskPaneSimulation.setCollapsed(true);
            taskPaneMapping.setCollapsed(false);
        }

        @Override
        protected void finished() {
            getFrame().setTitle(title);
            btnSimulationRun.setIcon(resourceMap.getIcon("simulationRun.Action.icon.play"));
            btnSimulationRun.setText(resourceMap.getString("simulationRun.Action.text.start"));
            setMenuEnabled(true);
            isRunning = false;
        }
    }

    ///////////////
    // MAPPING MAPS
    ///////////////
    @Action
    public Task createMaps() {
        return createMapTask = new CreateMapTask(getApplication());
    }

    private class CreateMapTask extends SFTask {

        private int itime;

        CreateMapTask(Application instance) {
            super(instance);
            btnMapping.getAction().setEnabled(false);
            btnExportToKMZ.getAction().setEnabled(false);
            btnOpenNC.getAction().setEnabled(false);
            btnCloseNC.getAction().setEnabled(false);
            cbBoxWMS.getAction().setEnabled(false);
            btnCancelMapping.getAction().setEnabled(true);
            setColorbarPanelEnabled(false);
            itime = 0;
            wmsMapper.setZoomButtonsVisible(false);
            wmsMapper.setZoomSliderVisible(false);
        }

        @Override
        protected Object doInBackground() throws Exception {

            wmsMapper.setDefaultColor(btnParticleColor.getForeground());
            wmsMapper.setParticlePixel((Integer) spinnerParticleSize.getValue());

            // delete existing pictures in folder
            File wmsfolder = wmsMapper.getFolder();
            if (null != wmsfolder && wmsfolder.isDirectory()) {
                for (File picture : wmsfolder.listFiles(new MetaFilenameFilter("*.png"))) {
                    picture.delete();
                }
                setMessage(resourceMap.getString("createMaps.msg.cleanup"));
            }

            // paint
            setMessage(resourceMap.getString("createMaps.msg.start"), true, Level.INFO);
            for (itime = 0; itime < wmsMapper.getNcOut().getNTime(); itime++) {
                setProgress((float) (itime + 1) / wmsMapper.getNcOut().getNTime());
                wmsMapper.draw(itime);
            }
            return null;
        }

        @Override
        protected void finished() {
            wmsMapper.clear();
            wmsMapper.setZoomButtonsVisible(true);
            wmsMapper.setZoomSliderVisible(true);
            btnMapping.getAction().setEnabled(true);
            btnExportToKMZ.getAction().setEnabled(true);
            btnCancelMapping.getAction().setEnabled(false);
            btnOpenNC.getAction().setEnabled(true);
            btnCloseNC.getAction().setEnabled(true);
            cbBoxWMS.getAction().setEnabled(true);
            setColorbarPanelEnabled(true);
        }

        @Override
        void onSuccess(Object result) {
            StringBuilder sb = new StringBuilder();
            sb.append(resourceMap.getString("mapping.text"));
            sb.append(" ");
            sb.append(itime);
            sb.append(" ");
            sb.append(resourceMap.getString("createMaps.msg.succeeded"));
            setMessage(sb.toString(), false, LogLevel.COMPLETE);
            // move to animation pane
            setAnimationToolsEnabled(false);
            getApplication().getContext().getTaskService().execute(new LoadFolderAnimationTask(getApplication(), wmsMapper.getFolder()));
            taskPaneMapping.setCollapsed(true);
            taskPaneAnimation.setCollapsed(false);
        }

        @Override
        void onFailure(Throwable throwable) {
            // do nothing
        }

        @Override
        protected void cancelled() {
            setMessage(resourceMap.getString("createMaps.msg.cancelled"));
        }
    }

    @Action
    public void cancelMapping() {
        createMapTask.cancel(true);
        kmzTask.cancel(true);
    }

    @Action
    public void openNcMapping() throws IOException {
        File file = (null == outputFile)
                ? (null == cfgPath) ? new File(System.getProperty("user.dir")) : cfgPath
                : outputFile;
        JFileChooser chooser = new JFileChooser(file);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileFilter(new FileNameExtensionFilter(getResourceMap().getString("Application.outputFile"), getResourceMap().getString("Application.outputFile.extension")));
        int returnPath = chooser.showOpenDialog(getFrame());
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            outputFile = chooser.getSelectedFile();
            openNetCDF();
        }
    }

    public void openNetCDF() throws IOException {

        getLogger().log(Level.INFO, "{0} {1}", new Object[]{getResourceMap().getString("openNcMapping.msg.opened"), outputFile.getAbsolutePath()});
        lblNC.setText(outputFile.getName());
        lblNC.setFont(lblNC.getFont().deriveFont(Font.PLAIN, 12));
        wmsMapper.loadFile(outputFile.getAbsolutePath());
        wmsMapper.setVisible(!taskPaneMapping.isCollapsed());
        lblMapping.setVisible(false);
        btnMapping.getAction().setEnabled(true);
        btnCloseNC.getAction().setEnabled(true);
        btnExportToKMZ.getAction().setEnabled(true);
        setMainTitle();
        setColorbarPanelEnabled(true);
        cbBoxVariable.setModel(new DefaultComboBoxModel(wmsMapper.getNcOut().getVariables()));
        cbBoxWMS.getAction().setEnabled(true);
    }

    private void applyColorbarSettings(WMSMapper mapper) {
        String vname = (String) cbBoxVariable.getSelectedItem();
        float vmin = Float.valueOf(txtFieldMin.getText());
        float vmax = Float.valueOf(txtFieldMax.getText());
        Color[] colorbar = Colorbars.MATLAB_COLORBARS.get((int) colorbarChooser.getSelectedItem());
        mapper.setColorbar(vname.toLowerCase().contains("none") ? null : vname, vmin, vmax, colorbar);
    }

    @Action
    public void applyColorbarSettings() {
        applyColorbarSettings(wmsMapper);
        getLogger().info(getResourceMap().getString("applyColorbarSettings.msg.applied"));
    }

    @Action
    public void changeColorbarVariable() {
        btnAutoRange.getAction().setEnabled(!((String) cbBoxVariable.getSelectedItem()).equals("None"));
    }

    @Action
    public Task autoRangeColorbar() {
        String varName = (String) cbBoxVariable.getSelectedItem();
        if (varName.startsWith("None")) {
            btnAutoRange.setEnabled(false);
            return null;
        } else {
            btnAutoRange.setEnabled(true);
            return new AutoRangeTask(getApplication(), (String) cbBoxVariable.getSelectedItem());
        }
    }

    private class AutoRangeTask extends SFTask<float[], Object> {

        ResourceMap resourceMap = Application.getInstance(IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
        String variable;

        AutoRangeTask(Application instance, String variable) {
            super(instance);
            this.variable = variable;
        }

        @Override
        protected float[] doInBackground() throws Exception {
            if (variable.toLowerCase().equals("none")) {
                cancel(true);
                return null;
            }
            setMessage(resourceMap.getString("autoRangeColorbar.msg.range"), true, Level.INFO);
            return wmsMapper.getNcOut().getRange(variable);
        }

        @Override
        void onSuccess(float[] result) {
            if (null != result) {
                txtFieldMin.setValue(result[0]);
                txtFieldMax.setValue(result[1]);
                setMessage(resourceMap.getString("autoRangeColorbar.msg.suggested") + " [" + txtFieldMin.getText() + " : " + txtFieldMax.getText() + "]", false, LogLevel.COMPLETE);
            }
        }

        @Override
        void onFailure(Throwable throwable) {
            setMessage(resourceMap.getString("autoRangeColorbar.msg.error") + " " + variable);
        }

        @Override
        protected void cancelled() {
            setMessage(resourceMap.getString("autoRangeColorbar.msg.cancelled"), false, Level.WARNING);
        }
    }

    private void setColorbarPanelEnabled(boolean enabled) {
        btnParticleColor.setEnabled(enabled);
        cbBoxVariable.setEnabled(enabled);
        txtFieldMin.setEnabled(enabled);
        txtFieldMax.setEnabled(enabled);
        colorbarChooser.setEnabled(enabled);
        btnAutoRange.getAction().setEnabled(enabled && !((String) cbBoxVariable.getSelectedItem()).equals("None"));
        btnApplyColorbar.getAction().setEnabled(enabled);
        spinnerParticleSize.setEnabled(enabled);
    }

    @Action
    public void changeWMS() {
        wmsMapper.setWMS((String) cbBoxWMS.getSelectedItem());
    }

    //////////////
    // MAPPING KMZ
    //////////////
    @Action
    public Task exportToKMZ() {
        return kmzTask = new ExportToKMZTask(getApplication());
    }

    private class ExportToKMZTask extends SFTask {

        private final ExportToKML kmlExport;

        ExportToKMZTask(Application instance) {
            super(instance);
            setMessage(resourceMap.getString("exportToKMZ.msg.init"));
            wmsMapper.setAlpha(0.2f);
            wmsMapper.setEnabled(false);
            btnMapping.getAction().setEnabled(false);
            btnExportToKMZ.getAction().setEnabled(false);
            btnCloseNC.getAction().setEnabled(false);
            cbBoxWMS.getAction().setEnabled(false);
            btnCancelMapping.getAction().setEnabled(true);
            setColorbarPanelEnabled(false);
            kmlExport = new ExportToKML(outputFile.getAbsolutePath());
        }

        @Override
        protected Object doInBackground() throws Exception {
            setMessage(resourceMap.getString("exportToKMZ.msg.exporting"), true, Level.INFO);
            kmlExport.init();
            kmlExport.writeKML();
            setMessage(resourceMap.getString("exportToKMZ.msg.compressing"), true, Level.INFO);
            kmlExport.toKMZ();
            return null;
        }

        @Override
        protected void finished() {
            wmsMapper.setAlpha(1.f);
            wmsMapper.setEnabled(true);
            btnMapping.getAction().setEnabled(true);
            btnExportToKMZ.getAction().setEnabled(true);
            btnCloseNC.getAction().setEnabled(true);
            btnCancelMapping.getAction().setEnabled(false);
            cbBoxWMS.getAction().setEnabled(true);
            setColorbarPanelEnabled(true);
        }

        @Override
        void onSuccess(Object o) {
            setMessage(resourceMap.getString("exportToKMZ.msg.exported") + " " + kmlExport.getKMZ(), false, LogLevel.COMPLETE);
        }

        @Override
        void onFailure(Throwable t) {
        }

        @Override
        protected void cancelled() {
            setMessage(resourceMap.getString("exportToKMZ.msg.cancelled"));
        }
    }

    //////////////
    // ANIMATION
    //////////////
    private void setAnimationToolsEnabled(boolean enabled) {
        btnSaveAsMaps.getAction().setEnabled(enabled);
        btnDeleteMaps.getAction().setEnabled(enabled);
        btnFirst.getAction().setEnabled(enabled);
        btnPrevious.getAction().setEnabled(enabled);
        btnAnimationFW.getAction().setEnabled(enabled);
        btnAnimationBW.getAction().setEnabled(enabled);
        btnAnimationStop.getAction().setEnabled(enabled);
        btnNext.getAction().setEnabled(enabled);
        btnLast.getAction().setEnabled(enabled);
        btnAnimatedGif.getAction().setEnabled(enabled);
        ckBoxReverseTime.setEnabled(enabled);
        if (!enabled) {
            sliderTime.setValue(0);
            lblTime.setText(resourceMap.getString("lblTime.text"));
        }
        sliderTime.setEnabled(enabled);
    }

    @Action
    public void deleteMaps() {
        stopAnimation();
        StringBuilder message = new StringBuilder(getResourceMap().getString("deleteMaps.dialog.msg"));
        message.append(" ");
        message.append(outputFolder.getName());
        message.append(" ?");
        int dialog = JOptionPane.showConfirmDialog(getFrame(), message.toString(), getResourceMap().getString("deleteMaps.dialog.title"), JOptionPane.OK_CANCEL_OPTION);
        if (dialog == JOptionPane.OK_OPTION) {
            replayPanel.deleteImages();
            StringBuilder sb = new StringBuilder();
            sb.append(resourceMap.getString("animation.text"));
            sb.append(" ");
            sb.append(resourceMap.getString("deleteMaps.msg.deleted"));
            getLogger().log(Level.INFO, sb.toString());
            closeFolderAnimation();
        }
    }

    public void closeFolderAnimation() {
        replayPanel.setVisible(false);
        lblAnimation.setVisible(true);
        lblFolder.setText(getResourceMap().getString("lblFolder.text"));
        outputFolder = null;
        replayPanel.clear();
        setAnimationToolsEnabled(false);
    }

    @Action
    public Task openFolderAnimation() {

        stopAnimation();
        File file = (null == outputFolder)
                ? new File(System.getProperty("user.dir"))
                : outputFolder;
        JFileChooser chooser = new JFileChooser(file);
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        int returnPath = chooser.showDialog(getFrame(), getResourceMap().getString("openFolderAnimation.dialog.title"));
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            return new LoadFolderAnimationTask(getApplication(), chooser.getSelectedFile());
        }
        return null;
    }

    private class LoadFolderAnimationTask extends SFTask {

        private final File folder;

        LoadFolderAnimationTask(Application instance, File folder) {
            super(instance);
            this.folder = folder;
            replayPanel.setVisible(false);
            lblAnimation.setVisible(true);
            replayPanel.clear();
            setMessage(resourceMap.getString("openFolderAnimation.msg.opened") + " " + folder.getAbsolutePath());
        }

        @Override
        protected Object doInBackground() throws Exception {
            replayPanel.loadImages(folder);
            return null;
        }

        @Override
        public void onSuccess(Object o) {
            replayPanel.setVisible(true);
            lblAnimation.setVisible(false);
            outputFolder = folder;
            lblFolder.setText(outputFolder.getName());
            lblFolder.setFont(lblFolder.getFont().deriveFont(Font.PLAIN, 12));
            sliderTime.setMaximum(replayPanel.getNImage() - 1);
            setAnimationToolsEnabled(true);
            sliderTime.setValue(0);
        }

        @Override
        public void onFailure(Throwable t) {
            replayPanel.setVisible(false);
            lblAnimation.setVisible(true);
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

    private void replayImageChangeEvent(PropertyChangeEvent evt) {
        if (ReplayPanel.IMAGE_INDEX.equals(evt.getPropertyName())) {
            lblTime.setText((String) evt.getNewValue());
        }
    }

    private void sliderTimeStateChanged(ChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            if (replayPanel.isVisible()) {
                replayPanel.showImage(sliderTime.getValue());
            }
        });
    }

    @Action
    public void saveasMaps() {
        stopAnimation();
        getLogger().info(getResourceMap().getString("saveasMaps.msg.launch"));
        getApplication().show(new ExportMapsView(IchthyopApp.getApplication(), outputFolder));
    }

    @Action
    public Task createAnimatedGif() {
        return new CreateAnimatedGifTask(getApplication());
    }

    private class CreateAnimatedGifTask extends SFTask {

        CreateAnimatedGifTask(Application instance) {
            super(instance);
            stopAnimation();
            setAnimationToolsEnabled(false);
            btnOpenAnimation.getAction().setEnabled(false);
            setMessage(resourceMap.getString("createAnimatedGif.msg.start"), true, Level.INFO);
        }

        @Override
        protected Object doInBackground() throws Exception {
            return replayPanel.toGIF(nbfps, ckBoxReverseTime.isSelected());
        }

        @Override
        void onSuccess(Object result) {
            if (null != result) {
                setMessage(resourceMap.getString("createAnimatedGif.msg.succeeded") + " " + result, false, LogLevel.COMPLETE);
            }
        }

        @Override
        void onFailure(Throwable throwable) {
        }

        @Override
        protected void finished() {
            setAnimationToolsEnabled(true);
            btnOpenAnimation.getAction().setEnabled(true);
        }
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
    public void next() {
        int index = sliderTime.getValue();
        if (index == replayPanel.getNImage() - 1) {
            sliderTime.setValue(0);
        } else {
            sliderTime.setValue(index + 1);
        }
    }

    @Action
    public void last() {
        sliderTime.setValue(replayPanel.getNImage() - 1);
    }

    @Action
    public void startAnimationBW() {
        animationDirection = TimeDirection.BACKWARD;
        startAnimation();
    }

    @Action
    public void startAnimationFW() {
        animationDirection = TimeDirection.FORWARD;
        startAnimation();
    }

    @Action
    public void stopAnimation() {
        if (animator.isRunning()) {
            statusBar.setBusy(false);
            animator.stop();
            replayPanel.addMouseWheelListener(animationMouseScroller);
        }
    }

    private void startAnimation() {
        if (!animator.isRunning()) {
            statusBar.setBusy(true);
            replayPanel.removeMouseWheelListener(animationMouseScroller);
            btnAnimationFW.setEnabled(true);
            animator.start();
        }
    }

    private void initAnimator() {

        float TEN_MINUTES = 10.f * 60.f;
        animator = new Animator((int) (TEN_MINUTES * 1000), new TimingTarget() {
            @Override
            public void timingEvent(float fraction) {
                float ellpased_time = (fraction * TEN_MINUTES - time) * nbfps;
                if (ellpased_time > 1) {
                    time = fraction * TEN_MINUTES;
                    if (animationDirection.equals(TimeDirection.FORWARD)) {
                        next();
                    } else {
                        previous();
                    }
                }
            }

            @Override
            public void begin() {
                nbfps = (Float) animationSpeed.getValue();
                time = 0;
                getLogger().info(getResourceMap().getString("animation.msg.started"));
                //startAccelerationProgress();
            }

            @Override
            public void end() {
                btnOpenAnimation.getAction().setEnabled(true);
                getLogger().info(getResourceMap().getString("animation.msg.stopped"));
            }

            @Override
            public void repeat() {
            }
        });
    }

    //////////////
    // PREFERENCES
    //////////////
    public void savePreferences() {

        if (null != getSimulationManager().getConfigurationFile() && !cfgUntitled) {
            savePreference(openMenuItem, getSimulationManager().getConfigurationFile().getPath());
            //savePreference(blockTree, blockTree.getSelectedKey());
        } else {
            savePreference(openMenuItem, System.getProperty("user.dir"));
        }

        savePreference(leftSplitPane, leftSplitPane.getDividerLocation());
        savePreference(animationSpeed, animationSpeed.getValue());
        savePreference(spinnerParticleSize, spinnerParticleSize.getValue());
        savePreference(btnParticleColor, btnParticleColor.getForeground());
        savePreference(colorbarChooser, colorbarChooser.getSelectedIndex());
        savePreference(txtFieldMin, txtFieldMin.getValue());
        savePreference(txtFieldMax, txtFieldMax.getValue());
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
        return (name == null) ? null : name + ".preferences.xml";
    }

    public void restorePreferences() {
        Object property;

        property = restorePreference(openMenuItem);
        if (property != null) {
            File file = new File((String) property);
            if (file.isFile()) {
                cfgPath = file.getParentFile();
                cfgUntitled = false;
                loadConfigurationFile(file).execute();
            } else if (file.isDirectory()) {
                cfgPath = file;
            }
        }

        property = restorePreference(animationSpeed);
        if (property != null) {
            animationSpeed.setValue(property);
        }
        property = restorePreference(spinnerParticleSize);
        if (property != null) {
            spinnerParticleSize.setValue(property);
        }
        property = restorePreference(btnParticleColor);
        if (property != null) {
            btnParticleColor.setForeground((Color) property);
        }
        property = restorePreference(colorbarChooser);
        if (property != null) {
            colorbarChooser.setSelectedIndex((Integer) property);
        }
        property = restorePreference(txtFieldMin);
        if (property != null) {
            txtFieldMin.setValue(property);
        }
        property = restorePreference(txtFieldMax);
        if (property != null) {
            txtFieldMax.setValue(property);
        }
        property = restorePreference(leftSplitPane);
        if (property != null) {
            leftSplitPane.setDividerLocation((int) property);
        }

        getFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private Object restorePreference(Component bean) {
        try {
            return getContext().getLocalStorage().load(beanFilename(bean));
        } catch (Exception ex) {
            getSimulationManager().debug(ex);
            return null;
        }
    }

    @Action
    public void showTimeConverter() {

        JFrame mainFrame = IchthyopApp.getApplication().getMainFrame();
        JDialog timeConverter = new TimeConverterDialog(mainFrame);
        timeConverter.setLocationRelativeTo(mainFrame);
        IchthyopApp.getApplication().show(timeConverter);
    }

    @Action
    public void showAboutBox() {
        if (aboutBox == null) {
            JFrame mainFrame = IchthyopApp.getApplication().getMainFrame();
            aboutBox = new AboutDialog(mainFrame);
            aboutBox.setLocationRelativeTo(mainFrame);
        }
        IchthyopApp.getApplication().show(aboutBox);
    }

    @Action
    public void exitApplication() {
        getContext().getActionMap().get("quit").actionPerformed(new ActionEvent(exitMenuItem, 0, null));
    }

    private void setMenuEnabled(boolean enabled) {
        openMenuItem.getAction().setEnabled(enabled);
        newMenuItem.getAction().setEnabled(enabled);
        saveAsMenuItem.getAction().setEnabled(enabled);
        closeMenuItem.getAction().setEnabled(enabled);
        previewMenuItem.getAction().setEnabled(enabled);
        savePreviewMenuItem.getAction().setEnabled(enabled);
        simulationMenuItem.getAction().setEnabled(enabled);
        openNCMenuItem.getAction().setEnabled(enabled);
        openAnimationMenuItem.getAction().setEnabled(enabled);
    }

    private void createMainPanel() {

        pnlConfiguration.addPropertyChangeListener("parameterChanged", (evt) -> {
            getSimulationManager().getParameterManager().notifyParameterChanged((String) evt.getNewValue());
            btnSaveCfgFile.getAction().setEnabled(true);
        });

        lblConfiguration = new JLabel(getResourceMap().getIcon("lblConfiguration.icon"));
        lblConfiguration.setHorizontalAlignment(JLabel.CENTER);
        lblConfiguration.setVerticalAlignment(JLabel.CENTER);

        lblSimulation = new JLabel(getResourceMap().getIcon("step.Simulation.bgicon"));
        lblSimulation.setHorizontalAlignment(JLabel.CENTER);
        lblSimulation.setVerticalAlignment(JLabel.CENTER);

        lblMapping = new JLabel(getResourceMap().getIcon("lblMapping.icon"));
        lblMapping.setHorizontalAlignment(JLabel.CENTER);
        lblMapping.setVerticalAlignment(JLabel.CENTER);

        lblAnimation = new JLabel(getResourceMap().getIcon("step.Animation.bgicon"));
        lblAnimation.setHorizontalAlignment(JLabel.CENTER);
        lblAnimation.setVerticalAlignment(JLabel.CENTER);

        replayPanel.setOpaque(false);
        replayPanel.addPropertyChangeListener(this::replayImageChangeEvent);

        gradientPanel.setLayout(new StackLayout());
        gradientPanel.add(pnlLogo, StackLayout.BOTTOM);
        pnlLogo.setVisible(false);
        gradientPanel.add(lblConfiguration, StackLayout.TOP);
        lblConfiguration.setVisible(true);
        gradientPanel.add(pnlConfiguration);
        pnlConfiguration.setVisible(false);
        gradientPanel.add(lblSimulation);
        lblSimulation.setVisible(false);
        gradientPanel.add(previewScrollPane, StackLayout.TOP);
        previewScrollPane.setVisible(false);
        gradientPanel.add(lblMapping, StackLayout.TOP);
        lblMapping.setVisible(false);
        gradientPanel.add(wmsMapper, StackLayout.TOP);
        wmsMapper.setVisible(false);
        gradientPanel.add(lblAnimation, StackLayout.TOP);
        lblAnimation.setVisible(false);
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
            getLogger().log(Level.INFO, "{0} {1}", new Object[]{getResourceMap().getString("browse.msg.openurl"), getResourceMap().getString("Application.homepage")});
        } catch (IOException ex) {
            getLogger().log(Level.INFO, "{0} {1}", new Object[]{getResourceMap().getString("browse.msg.no-browser"), getResourceMap().getString("Application.homepage")});
        }
    }

    private void setMainTitle() {

        pnlLogo.setVisible(false);
        String title = getResourceMap().getString("Application.title") + " - ";
        if (!taskPaneConfiguration.isCollapsed()) {
            if (null != getSimulationManager().getConfigurationFile()) {
                titledPanelMain.setTitle(title + taskPaneConfiguration.getTitle() + " - " + getSimulationManager().getConfigurationFile());
            } else {
                titledPanelMain.setTitle(title + taskPaneConfiguration.getTitle());
            }
        } else if (!taskPaneSimulation.isCollapsed()) {
            if (null != getSimulationManager().getConfigurationFile()) {
                titledPanelMain.setTitle(title + taskPaneSimulation.getTitle() + " - " + getSimulationManager().getConfigurationFile());
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

        @Override
        public boolean canExit(EventObject e) {
            if (savePending()) {
                int answer = dialogSave();
                return ((answer == JOptionPane.YES_OPTION) || (answer == JOptionPane.NO_OPTION));
            }
            return true;
        }

        @Override
        public void willExit(EventObject e) {
            getLogger().info(resourceMap.getString("Application.msg.bye"));
        }
    }

    private Color chooseColor(Component component, Color initial) {
        return JColorChooser.showDialog(component, "", initial);
    }

    private void initMenu() {

        // instantiate and init menu items
        menuBar = new JMenuBar();
        menuBar.setName("menuBar");

        configurationMenu = new JMenu();
        configurationMenu.setName("configurationMenu");
        configurationMenu.setText(resourceMap.getString("configurationMenu.text"));

        newMenuItem = new JMenuItem();
        newMenuItem.setAction(actionMap.get("newConfigurationFile"));
        newMenuItem.setName("newMenuItem");

        openMenuItem = new JMenuItem();
        openMenuItem.setAction(actionMap.get("openConfigurationFile"));
        openMenuItem.setName("openMenuItem");
        
        reloadMenuItem = new JMenuItem();
        reloadMenuItem.setAction(actionMap.get("reloadConfigurationFile"));
        reloadMenuItem.setName("reloadMenuItem");

        closeMenuItem = new JMenuItem();
        closeMenuItem.setAction(actionMap.get("closeConfigurationFile"));
        closeMenuItem.setName("closeMenuItem");

        saveMenuItem = new JMenuItem();
        saveMenuItem.setAction(actionMap.get("saveConfigurationFile"));
        saveMenuItem.setName("saveMenuItem");

        saveAsMenuItem = new JMenuItem();
        saveAsMenuItem.setAction(actionMap.get("saveAsConfigurationFile"));
        saveAsMenuItem.setName("saveAsMenuItem");

        exitMenuItem = new JMenuItem();
        exitMenuItem.setAction(actionMap.get("exitApplication"));
        exitMenuItem.setName("exitMenuItem");

        simulationMenu = new JMenu();
        simulationMenu.setText(resourceMap.getString("simulationMenu.text"));
        simulationMenu.setName("simulationMenu");

        simulationMenuItem = new JMenuItem();
        simulationMenuItem.setAction(actionMap.get("simulationRun"));
        simulationMenuItem.setName("simulationMenuItem");

        previewMenuItem = new JMenuItem();
        previewMenuItem.setAction(actionMap.get("previewSimulation"));
        previewMenuItem.setName("previewMenuItem");

        savePreviewMenuItem = new JMenuItem();
        savePreviewMenuItem.setAction(actionMap.get("savePreview"));
        savePreviewMenuItem.setName("savePreviewMenuItem");

        mappingMenu = new JMenu();
        mappingMenu.setText(resourceMap.getString("mappingMenu.text"));
        mappingMenu.setName("mappingMenu");

        mapMenuItem = new JMenuItem();
        mapMenuItem.setAction(actionMap.get("createMaps"));
        mapMenuItem.setName("mapMenuItem");

        exportToKMZMenuItem = new JMenuItem();
        exportToKMZMenuItem.setAction(actionMap.get("exportToKMZ"));
        exportToKMZMenuItem.setName("exportToKMZMenuItem");

        cancelMapMenuItem = new JMenuItem();
        cancelMapMenuItem.setAction(actionMap.get("cancelMapping"));
        cancelMapMenuItem.setName("cancelMapMenuItem");

        openNCMenuItem = new JMenuItem();
        openNCMenuItem.setAction(actionMap.get("openNcMapping"));
        openNCMenuItem.setName("openNCMenuItem");

        animationMenu = new JMenu();
        animationMenu.setText(resourceMap.getString("animationMenu.text"));
        animationMenu.setName("animationMenu");

        startFWMenuItem = new JMenuItem();
        startFWMenuItem.setAction(actionMap.get("startAnimationFW"));
        startFWMenuItem.setName("startFWMenuItem");

        stopMenuItem = new JMenuItem();
        stopMenuItem.setAction(actionMap.get("stopAnimation"));
        stopMenuItem.setName("stopMenuItem");

        startBWMenuItem = new JMenuItem();
        startBWMenuItem.setAction(actionMap.get("startAnimationBW"));
        startBWMenuItem.setName("startBWMenuItem");

        openAnimationMenuItem = new JMenuItem();
        openAnimationMenuItem.setAction(actionMap.get("openFolderAnimation"));
        openAnimationMenuItem.setName("openAnimationMenuItem");

        saveasMapsMenuItem = new JMenuItem();
        saveasMapsMenuItem.setAction(actionMap.get("saveasMaps"));
        saveasMapsMenuItem.setName("saveasMapsMenuItem");

        deleteMenuItem = new JMenuItem();
        deleteMenuItem.setAction(actionMap.get("deleteMaps"));
        deleteMenuItem.setName("deleteMenuItem");

        helpMenu = new JMenu();
        helpMenu.setText(resourceMap.getString("helpMenu.text"));
        helpMenu.setName("helpMenu");

        timeConverterMenuItem = new JMenuItem();
        timeConverterMenuItem.setAction(actionMap.get("showTimeConverter"));
        timeConverterMenuItem.setName("timeConverterMenuItem");

        aboutMenuItem = new JMenuItem();
        aboutMenuItem.setAction(actionMap.get("showAboutBox"));
        aboutMenuItem.setName("aboutMenuItem");

        // build menu
        // configuration
        configurationMenu.add(newMenuItem);
        configurationMenu.add(openMenuItem);
        configurationMenu.add(closeMenuItem);
        configurationMenu.add(new JSeparator());
        configurationMenu.add(saveMenuItem);
        configurationMenu.add(saveAsMenuItem);
        configurationMenu.add(new JSeparator());
        configurationMenu.add(exitMenuItem);
        // simulation
        simulationMenu.add(simulationMenuItem);
        simulationMenu.add(previewMenuItem);
        simulationMenu.add(savePreviewMenuItem);
        // mapping
        mappingMenu.add(openNCMenuItem);
        mappingMenu.add(new JSeparator());
        mappingMenu.add(mapMenuItem);
        mappingMenu.add(exportToKMZMenuItem);
        mappingMenu.add(cancelMapMenuItem);
        // animation
        animationMenu.add(startFWMenuItem);
        animationMenu.add(stopMenuItem);
        animationMenu.add(startBWMenuItem);
        animationMenu.add(new JSeparator());
        animationMenu.add(openAnimationMenuItem);
        animationMenu.add(saveasMapsMenuItem);
        animationMenu.add(deleteMenuItem);
        // help
        helpMenu.add(timeConverterMenuItem);
        helpMenu.add(aboutMenuItem);
        // main
        menuBar.add(configurationMenu);
        menuBar.add(simulationMenu);
        menuBar.add(mappingMenu);
        menuBar.add(animationMenu);
        menuBar.add(helpMenu);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
    private void initComponents() {

        resourceMap = Application.getInstance(IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
        actionMap = Application.getInstance().getContext().getActionMap(IchthyopView.class, this);

        initMenu();

        mainPanel = new JPanel();
        splitPane = new JSplitPane();
        leftSplitPane = new JSplitPane();
        titledPanelSteps = new JXTitledPanel();
        stepsScrollPane = new JScrollPane();
        stepsPanel = new JPanel();
        taskPaneConfiguration = new JXTaskPane();
        pnlFile = new JPanel();
        lblCfgFile = new JLabel();
        btnNewCfgFile = new JButton();
        btnOpenCfgFile = new JButton();
        btnSaveCfgFile = new JButton();
        btnSaveAsCfgFile = new JButton();
        btnReloadCfgFile = new JButton();
        btnCloseCfgFile = new JButton();
        taskPaneSimulation = new JXTaskPane();
        pnlSimulation = new JPanel();
        btnPreview = new JButton();
        btnSavePreview = new JButton();
        btnSimulationRun = new JButton();
        sliderPreviewZoom = new JSlider(100, 3000, 500);
        taskPaneMapping = new JXTaskPane();
        pnlMapping = new JPanel();
        btnMapping = new JButton();
        btnCancelMapping = new JButton();
        btnOpenNC = new JButton();
        pnlWMS = new JPanel();
        cbBoxWMS = new JComboBox();
        lblWMS = new JLabel();
        lblNC = new JLabel();
        btnCloseNC = new JButton();
        btnExportToKMZ = new JButton();
        pnlColor = new JPanel();
        pnlColorBar = new JPanel();
        lblVariable = new JLabel();
        cbBoxVariable = new JComboBox();
        lblMin = new JLabel();
        lblMax = new JLabel();
        txtFieldMax = new JFormattedTextField();
        btnAutoRange = new JButton();
        btnApplyColorbar = new JButton();
        txtFieldMin = new JFormattedTextField();
        colorbarChooser = new ColorbarChooser();
        lblColorbarChooser = new JLabel();
        lblParticleColor = new JLabel();
        btnParticleColor = new JButton();
        lblParticleSize = new JLabel();
        spinnerParticleSize = new JSpinner();
        taskPaneAnimation = new JXTaskPane();
        pnlAnimation = new JPanel();
        lblFramePerSecond = new JLabel();
        animationSpeed = new JSpinner();
        btnDeleteMaps = new JButton();
        btnSaveAsMaps = new JButton();
        lblAnimationSpeed = new JLabel();
        btnOpenAnimation = new JButton();
        lblFolder = new JLabel();
        sliderTime = new JSlider();
        lblTime = new JLabel();
        animationToolBar = new JToolBar();
        btnFirst = new JButton();
        btnPrevious = new JButton();
        btnAnimationBW = new JButton();
        btnAnimationStop = new JButton();
        btnAnimationFW = new JButton();
        btnNext = new JButton();
        btnLast = new JButton();
        btnAnimatedGif = new JButton();
        ckBoxReverseTime = new JCheckBox();
        titledPanelLogger = new JXTitledPanel();
        loggerScrollPane = new LoggerScrollPane();
        titledPanelMain = new JXTitledPanel();
        rightScrollPane = new JScrollPane();
        gradientPanel = new GradientPanel();
        previewScrollPane = new JScrollPane();
        previewPanel = new SimulationPreviewPanel();
        pnlLogo = new JXPanel();
        hyperLinkLogo = new JXHyperlink();

        mainPanel.setName("mainPanel");

        splitPane.setDividerLocation(490);
        splitPane.setName("splitPane");
        splitPane.setOneTouchExpandable(true);

        leftSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setResizeWeight(0.6);
        leftSplitPane.setName("leftSplitPane");
        leftSplitPane.setOneTouchExpandable(true);

        titledPanelSteps.setTitle(resourceMap.getString("titledPanelSteps.title"));
        titledPanelSteps.setMinimumSize(new java.awt.Dimension(200, 200));
        titledPanelSteps.setName("titledPanelSteps");

        stepsScrollPane.setName("stepsScrollPane");
        stepsScrollPane.setPreferredSize(new java.awt.Dimension(300, 400));

        stepsPanel.setName("stepsPanel");

        taskPaneConfiguration.setAnimated(false);
        taskPaneConfiguration.setIcon(resourceMap.getIcon("step.Configuration.icon"));
        taskPaneConfiguration.setTitle(resourceMap.getString("step.Configuration.text"));
        taskPaneConfiguration.setName("taskPaneConfiguration");
        taskPaneConfiguration.addPropertyChangeListener(this::taskPaneConfigurationPropertyChange);

        pnlFile.setName("pnlFile");
        pnlFile.setOpaque(false);

        lblCfgFile.setName("lblCfgFile");

        btnNewCfgFile.setAction(actionMap.get("newConfigurationFile"));
        btnNewCfgFile.setFocusable(false);
        btnNewCfgFile.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnNewCfgFile.setName("btnNewCfgFile");
        btnNewCfgFile.setVerticalTextPosition(SwingConstants.BOTTOM);

        btnOpenCfgFile.setAction(actionMap.get("openConfigurationFile"));
        btnOpenCfgFile.setFocusable(false);
        btnOpenCfgFile.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnOpenCfgFile.setName("btnOpenCfgFile");
        btnOpenCfgFile.setVerticalTextPosition(SwingConstants.BOTTOM);

        btnSaveCfgFile.setAction(actionMap.get("saveConfigurationFile"));
        btnSaveCfgFile.setFocusable(false);
        btnSaveCfgFile.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnSaveCfgFile.setName("btnSaveCfgFile");
        btnSaveCfgFile.setVerticalTextPosition(SwingConstants.BOTTOM);
        btnSaveCfgFile.getAction().setEnabled(false);

        btnSaveAsCfgFile.setAction(actionMap.get("saveAsConfigurationFile"));
        btnSaveAsCfgFile.setFocusable(false);
        btnSaveAsCfgFile.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnSaveAsCfgFile.setName("btnSaveAsCfgFile");
        btnSaveAsCfgFile.setVerticalTextPosition(SwingConstants.BOTTOM);
        
        btnReloadCfgFile.setAction(actionMap.get("reloadConfigurationFile"));
        btnReloadCfgFile.setFocusable(false);
        btnReloadCfgFile.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnReloadCfgFile.setName("btnReloadCfgFile");
        btnReloadCfgFile.setVerticalTextPosition(SwingConstants.BOTTOM);

        btnCloseCfgFile.setAction(actionMap.get("closeConfigurationFile"));
        btnCloseCfgFile.setFocusable(false);
        btnCloseCfgFile.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnCloseCfgFile.setName("btnCloseCfgFile");
        btnCloseCfgFile.setVerticalTextPosition(SwingConstants.BOTTOM);

        GroupLayout pnlFileLayout = new GroupLayout(pnlFile);
        pnlFile.setLayout(pnlFileLayout);
        pnlFileLayout.setHorizontalGroup(
                pnlFileLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlFileLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlFileLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(lblCfgFile, GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                                .addGroup(pnlFileLayout.createSequentialGroup()
                                        .addComponent(btnNewCfgFile)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnOpenCfgFile)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnReloadCfgFile))
                                .addGroup(pnlFileLayout.createSequentialGroup()
                                        .addComponent(btnSaveCfgFile)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnSaveAsCfgFile)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnCloseCfgFile)))
                        .addContainerGap())
        );
        pnlFileLayout.setVerticalGroup(
                pnlFileLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlFileLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlFileLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(btnOpenCfgFile)
                                .addComponent(btnNewCfgFile)
                                .addComponent(btnReloadCfgFile))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(pnlFileLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(btnSaveCfgFile)
                                .addComponent(btnSaveAsCfgFile)
                                .addComponent(btnCloseCfgFile))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblCfgFile)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taskPaneConfiguration.add(pnlFile);

        taskPaneSimulation.setAnimated(false);
        taskPaneSimulation.setIcon(resourceMap.getIcon("step.Simulation.icon"));
        taskPaneSimulation.setTitle(resourceMap.getString("step.Simulation.text"));
        taskPaneSimulation.setName("taskPaneSimulation");
        taskPaneSimulation.addPropertyChangeListener(this::taskPaneSimulationPropertyChange);

        pnlSimulation.setName("pnlSimulation");
        pnlSimulation.setOpaque(false);

        btnPreview.setAction(actionMap.get("previewSimulation"));
        btnPreview.setFocusable(false);
        btnPreview.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnPreview.setName("btnPreview");
        btnPreview.setVerticalTextPosition(SwingConstants.BOTTOM);

        btnSavePreview.setAction(actionMap.get("savePreview"));
        btnSavePreview.setFocusable(false);
        btnSavePreview.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnSavePreview.setName("btnSavePreview");
        btnSavePreview.setVerticalTextPosition(SwingConstants.BOTTOM);

        btnSimulationRun.setAction(actionMap.get("simulationRun"));
        btnSimulationRun.setFocusable(false);
        btnSimulationRun.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnSimulationRun.setName("btnSimulationRun");
        btnSimulationRun.setVerticalTextPosition(SwingConstants.BOTTOM);

        sliderPreviewZoom.setName("sliderPreviewSize");
        sliderPreviewZoom.addChangeListener(this::sliderPreviewSizeStateChanged);
        sliderPreviewZoom.setAlignmentY(Component.CENTER_ALIGNMENT);

        lblPreviewZoom = new JLabel(resourceMap.getString("lblPreviewZoom.text"));
        lblPreviewZoom.setHorizontalAlignment(SwingConstants.CENTER);

        GroupLayout pnlSimulationLayout = new GroupLayout(pnlSimulation);
        pnlSimulation.setLayout(pnlSimulationLayout);
        pnlSimulationLayout.setHorizontalGroup(
                pnlSimulationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlSimulationLayout.createSequentialGroup()
                        .addComponent(btnPreview)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(btnSavePreview)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(pnlSimulationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(lblPreviewZoom, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(sliderPreviewZoom))
                )
                .addComponent(btnSimulationRun)
        );
        pnlSimulationLayout.setVerticalGroup(
                pnlSimulationLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlSimulationLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(btnPreview)
                        .addComponent(btnSavePreview)
                        .addGroup(pnlSimulationLayout.createSequentialGroup()
                                .addComponent(lblPreviewZoom)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(sliderPreviewZoom)
                        )
                )
                .addPreferredGap(ComponentPlacement.UNRELATED)
                .addComponent(btnSimulationRun)
                .addContainerGap()
        );

        taskPaneSimulation.add(pnlSimulation);

        taskPaneMapping.setAnimated(false);
        taskPaneMapping.setIcon(resourceMap.getIcon("step.Mapping.icon"));
        taskPaneMapping.setTitle(resourceMap.getString("step.Mapping.text"));
        taskPaneMapping.setName("taskPaneMapping");
        taskPaneMapping.addPropertyChangeListener(this::taskPaneMappingPropertyChange);

        pnlMapping.setName("pnlMapping");
        pnlMapping.setOpaque(false);

        btnMapping.setAction(actionMap.get("createMaps"));
        btnMapping.setName("btnMapping");

        btnCancelMapping.setAction(actionMap.get("cancelMapping"));
        btnCancelMapping.setName("btnCancelMapping");

        btnOpenNC.setAction(actionMap.get("openNcMapping"));
        btnOpenNC.setName("btnOpenNC");

        pnlWMS.setBorder(BorderFactory.createTitledBorder("Web Map Service"));
        pnlWMS.setName("pnlWMS");
        pnlWMS.setOpaque(false);

        cbBoxWMS.setModel(new DefaultComboBoxModel(new String[]{"Offline", "http://www.openstreetmap.org/", "http://www.marine-geo.org/services/wms?", "http://www2.demis.nl/wms/wms.asp?wms=WorldMap&"}));
        cbBoxWMS.setAction(actionMap.get("changeWMS"));
        cbBoxWMS.setName("cbBoxWMS");

        lblWMS.setName("lblWMS");

        GroupLayout pnlWMSLayout = new GroupLayout(pnlWMS);
        pnlWMS.setLayout(pnlWMSLayout);
        pnlWMSLayout.setHorizontalGroup(
                pnlWMSLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlWMSLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(lblWMS)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(cbBoxWMS, 0, 372, Short.MAX_VALUE)
                        .addContainerGap())
        );
        pnlWMSLayout.setVerticalGroup(
                pnlWMSLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlWMSLayout.createSequentialGroup()
                        .addGroup(pnlWMSLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblWMS)
                                .addComponent(cbBoxWMS, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblNC.setFont(resourceMap.getFont("lblNC.font"));
        lblNC.setText(resourceMap.getString("lblNC.text"));
        lblNC.setName("lblNC");

        btnCloseNC.setAction(actionMap.get("closeNetCDF"));
        btnCloseNC.setName("btnCloseNC");

        btnExportToKMZ.setAction(actionMap.get("exportToKMZ"));
        btnExportToKMZ.setName("btnExportToKMZ");

        pnlColor.setBorder(BorderFactory.createTitledBorder(resourceMap.getString("pnlColor.border.title")));
        pnlColor.setName("pnlColor");
        pnlColor.setOpaque(false);

        pnlColorBar.setBorder(BorderFactory.createTitledBorder(resourceMap.getString("pnlColorBar.border.title")));
        pnlColorBar.setName("pnlColorBar");
        pnlColorBar.setOpaque(false);

        lblVariable.setText(resourceMap.getString("lblVariable.text"));
        lblVariable.setName("lblVariable");

        cbBoxVariable.setModel(new DefaultComboBoxModel(new String[]{"None"}));
        cbBoxVariable.setAction(actionMap.get("changeColorbarVariable"));
        cbBoxVariable.setName("cbBoxVariable");

        lblMin.setText(resourceMap.getString("lblMin.text"));
        lblMin.setName("lblMin");

        lblMax.setText(resourceMap.getString("lblMax.text"));
        lblMax.setName("lblMax");

        txtFieldMax.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter(new DecimalFormat("###0.###"))));
        txtFieldMax.setName("txtFieldMax");
        NumberFormat floatFormat = NumberFormat.getNumberInstance(Locale.US);
        floatFormat.setGroupingUsed(false);
        NumberFormatter floatFormatter = new NumberFormatter(floatFormat);
        floatFormatter.setFormat(floatFormat);
        txtFieldMax.setFormatterFactory(new DefaultFormatterFactory(floatFormatter));
        txtFieldMax.setValue(100.f);

        btnAutoRange.setAction(actionMap.get("autoRangeColorbar"));
        btnAutoRange.setName("btnAutoRange");

        btnApplyColorbar.setAction(actionMap.get("applyColorbarSettings"));
        btnApplyColorbar.setName("btnApplyColorbar");

        txtFieldMin.setFormatterFactory(new DefaultFormatterFactory(new NumberFormatter()));
        txtFieldMin.setName("txtFieldMin");
        txtFieldMin.setFormatterFactory(new DefaultFormatterFactory(floatFormatter));
        txtFieldMin.setValue(0.f);

        lblColorbarChooser.setText("Colorbar");
        lblColorbarChooser.setName("lblColorbarChooser");

        GroupLayout pnlColorBarLayout = new GroupLayout(pnlColorBar);
        pnlColorBar.setLayout(pnlColorBarLayout);
        pnlColorBarLayout.setHorizontalGroup(
                pnlColorBarLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlColorBarLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlColorBarLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
                                .addGroup(pnlColorBarLayout.createSequentialGroup()
                                        .addComponent(lblVariable)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(cbBoxVariable, 0, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addGroup(pnlColorBarLayout.createSequentialGroup()
                                        .addComponent(btnAutoRange)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnApplyColorbar))
                                .addGroup(pnlColorBarLayout.createSequentialGroup()
                                        .addComponent(lblMin)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(txtFieldMin)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(lblMax)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(txtFieldMax))
                                .addGroup(pnlColorBarLayout.createSequentialGroup()
                                        .addComponent(lblColorbarChooser)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(colorbarChooser)))
                        .addContainerGap(84, Short.MAX_VALUE))
        );
        pnlColorBarLayout.setVerticalGroup(
                pnlColorBarLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlColorBarLayout.createSequentialGroup()
                        .addGroup(pnlColorBarLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblVariable)
                                .addComponent(cbBoxVariable, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlColorBarLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblMin)
                                .addComponent(txtFieldMin, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addComponent(lblMax)
                                .addComponent(txtFieldMax, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(16, 16, 16)
                        .addGroup(pnlColorBarLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblColorbarChooser)
                                .addComponent(colorbarChooser))
                        .addGap(16, 16, 16)
                        .addGroup(pnlColorBarLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(btnAutoRange)
                                .addComponent(btnApplyColorbar))
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        lblParticleColor.setText(resourceMap.getString("lblParticleColor.text"));
        lblParticleColor.setName("lblParticleColor");

        btnParticleColor.setForeground(resourceMap.getColor("btnParticleColor.foreground"));
        btnParticleColor.setIcon(resourceMap.getIcon("btnParticleColor.icon"));
        btnParticleColor.setText(resourceMap.getString("btnParticleColor.text"));
        btnParticleColor.setName("btnParticleColor");
        btnParticleColor.addActionListener(this::btnParticleColorActionPerformed);

        lblParticleSize.setText(resourceMap.getString("lblParticleSize.text"));
        lblParticleSize.setName("lblParticleSize");

        spinnerParticleSize.setModel(new SpinnerNumberModel(1, 1, 10, 1));
        spinnerParticleSize.setName("spinnerParticleSize");
        spinnerParticleSize.addChangeListener(this::spinnerParticleSizeStateChanged);

        GroupLayout pnlColorLayout = new GroupLayout(pnlColor);
        pnlColor.setLayout(pnlColorLayout);
        pnlColorLayout.setHorizontalGroup(
                pnlColorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlColorLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlColorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(pnlColorBar, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(pnlColorLayout.createSequentialGroup()
                                        .addComponent(lblParticleColor)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnParticleColor)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(lblParticleSize)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(spinnerParticleSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
        );
        pnlColorLayout.setVerticalGroup(
                pnlColorLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlColorLayout.createSequentialGroup()
                        .addGroup(pnlColorLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblParticleColor)
                                .addComponent(btnParticleColor)
                                .addComponent(lblParticleSize)
                                .addComponent(spinnerParticleSize, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(pnlColorBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout pnlMappingLayout = new GroupLayout(pnlMapping);
        pnlMapping.setLayout(pnlMappingLayout);
        pnlMappingLayout.setHorizontalGroup(
                pnlMappingLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlMappingLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlMappingLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(pnlWMS, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addGroup(pnlMappingLayout.createSequentialGroup()
                                        .addComponent(btnMapping)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnExportToKMZ))
                                .addGroup(pnlMappingLayout.createSequentialGroup()
                                        .addComponent(btnOpenNC)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnCloseNC))
                                .addComponent(lblNC)
                                .addComponent(btnCancelMapping)
                                .addComponent(pnlColor, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addContainerGap())
        );
        pnlMappingLayout.setVerticalGroup(
                pnlMappingLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlMappingLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(pnlColor)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(pnlMappingLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(btnMapping)
                                .addComponent(btnExportToKMZ))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(btnCancelMapping)
                        .addGap(12, 12, 12)
                        .addGroup(pnlMappingLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(btnOpenNC)
                                .addComponent(btnCloseNC))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(lblNC)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(pnlWMS, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taskPaneMapping.add(pnlMapping);

        taskPaneAnimation.setAnimated(false);
        taskPaneAnimation.setIcon(resourceMap.getIcon("step.Animation.icon"));
        taskPaneAnimation.setTitle(resourceMap.getString("step.Animation.text"));
        taskPaneAnimation.setName("taskPaneAnimation");
        taskPaneAnimation.addPropertyChangeListener(this::taskPaneAnimationPropertyChange);

        pnlAnimation.setName("pnlAnimation");
        pnlAnimation.setOpaque(false);

        lblFramePerSecond.setName("lblFramePerSecond");
        lblFramePerSecond.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFramePerSecondMouseClicked(evt);
            }
        });

        animationSpeed.setModel(new SpinnerNumberModel(Float.valueOf(1.5f), Float.valueOf(0.5f), Float.valueOf(24.0f), Float.valueOf(0.1f)));
        animationSpeed.setToolTipText(resourceMap.getString("animationSpeed.toolTipText"));
        animationSpeed.setFocusable(false);
        animationSpeed.setMaximumSize(new java.awt.Dimension(77, 30));
        animationSpeed.setName("animationSpeed");
        animationSpeed.addChangeListener(this::animationSpeedStateChanged);

        btnDeleteMaps.setAction(actionMap.get("deleteMaps"));
        btnDeleteMaps.setFocusable(false);
        btnDeleteMaps.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnDeleteMaps.setName("btnDeleteMaps");
        btnDeleteMaps.setVerticalTextPosition(SwingConstants.BOTTOM);

        btnSaveAsMaps.setAction(actionMap.get("saveasMaps"));
        btnSaveAsMaps.setFocusable(false);
        btnSaveAsMaps.setHorizontalTextPosition(SwingConstants.RIGHT);
        btnSaveAsMaps.setName("btnSaveAsMaps");
        btnSaveAsMaps.setVerticalTextPosition(SwingConstants.BOTTOM);

        lblAnimationSpeed.setText(resourceMap.getString("lblAnimationSpeed.text"));
        lblAnimationSpeed.setName("lblAnimationSpeed");

        btnOpenAnimation.setAction(actionMap.get("openFolderAnimation"));
        btnOpenAnimation.setName("btnOpenAnimation");
        btnOpenAnimation.setVerticalTextPosition(SwingConstants.BOTTOM);

        lblFolder.setFont(resourceMap.getFont("lblFolder.font"));
        lblFolder.setText(resourceMap.getString("lblFolder.text"));
        lblFolder.setName("lblFolder");

        sliderTime.setValue(0);
        sliderTime.setName("sliderTime");
        sliderTime.addChangeListener(this::sliderTimeStateChanged);

        lblTime.setFont(resourceMap.getFont("lblTime.font"));
        lblTime.setText(resourceMap.getString("lblTime.text"));
        lblTime.setName("lblTime");

        animationToolBar.setFloatable(false);
        animationToolBar.setRollover(true);
        animationToolBar.setName("jToolBar1");

        btnFirst.setAction(actionMap.get("first"));
        btnFirst.setFocusable(false);
        btnFirst.setHorizontalTextPosition(SwingConstants.CENTER);
        btnFirst.setName("btnFirst");
        btnFirst.setVerticalTextPosition(SwingConstants.BOTTOM);
        animationToolBar.add(btnFirst);

        btnPrevious.setAction(actionMap.get("previous"));
        btnPrevious.setFocusable(false);
        btnPrevious.setHorizontalTextPosition(SwingConstants.CENTER);
        btnPrevious.setName("btnPrevious");
        btnPrevious.setVerticalTextPosition(SwingConstants.BOTTOM);
        animationToolBar.add(btnPrevious);

        btnAnimationBW.setAction(actionMap.get("startAnimationBW"));
        btnAnimationBW.setText(resourceMap.getString("btnAnimationBW.text"));
        btnAnimationBW.setFocusable(false);
        btnAnimationBW.setHorizontalTextPosition(SwingConstants.CENTER);
        btnAnimationBW.setName("btnAnimationBW");
        btnAnimationBW.setVerticalTextPosition(SwingConstants.BOTTOM);
        animationToolBar.add(btnAnimationBW);

        btnAnimationStop.setAction(actionMap.get("stopAnimation"));
        btnAnimationStop.setText(resourceMap.getString("btnAnimationStop.text"));
        btnAnimationStop.setFocusable(false);
        btnAnimationStop.setHorizontalTextPosition(SwingConstants.CENTER);
        btnAnimationStop.setName("btnAnimationStop");
        btnAnimationStop.setVerticalTextPosition(SwingConstants.BOTTOM);
        animationToolBar.add(btnAnimationStop);

        btnAnimationFW.setAction(actionMap.get("startAnimationFW"));
        btnAnimationFW.setText(resourceMap.getString("btnAnimationFW.text"));
        btnAnimationFW.setFocusable(false);
        btnAnimationFW.setHorizontalTextPosition(SwingConstants.CENTER);
        btnAnimationFW.setName("btnAnimationFW");
        btnAnimationFW.setVerticalTextPosition(SwingConstants.BOTTOM);
        animationToolBar.add(btnAnimationFW);

        btnNext.setAction(actionMap.get("next"));
        btnNext.setFocusable(false);
        btnNext.setHorizontalTextPosition(SwingConstants.CENTER);
        btnNext.setName("btnNext");
        btnNext.setVerticalTextPosition(SwingConstants.BOTTOM);
        animationToolBar.add(btnNext);

        btnLast.setAction(actionMap.get("last"));
        btnLast.setFocusable(false);
        btnLast.setHorizontalTextPosition(SwingConstants.CENTER);
        btnLast.setName("btnLast");
        btnLast.setVerticalTextPosition(SwingConstants.BOTTOM);
        animationToolBar.add(btnLast);

        btnAnimatedGif.setAction(actionMap.get("createAnimatedGif"));
        btnAnimatedGif.setName("btnAnimatedGif");
        btnAnimatedGif.setVerticalTextPosition(SwingConstants.BOTTOM);

        ckBoxReverseTime.setText(resourceMap.getString("ckBoxReverseTime.text"));
        ckBoxReverseTime.setName("ckBoxReverseTime");

        GroupLayout pnlAnimationLayout = new GroupLayout(pnlAnimation);
        pnlAnimation.setLayout(pnlAnimationLayout);
        pnlAnimationLayout.setHorizontalGroup(
                pnlAnimationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlAnimationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addComponent(sliderTime, GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                                .addComponent(lblTime)
                                .addGroup(pnlAnimationLayout.createSequentialGroup()
                                        .addComponent(btnOpenAnimation)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnDeleteMaps)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(btnSaveAsMaps))
                                .addComponent(lblFolder)
                                .addComponent(animationToolBar, GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                                .addGroup(pnlAnimationLayout.createSequentialGroup()
                                        .addGap(379, 379, 379)
                                        .addComponent(lblFramePerSecond))
                                .addGroup(pnlAnimationLayout.createSequentialGroup()
                                        .addComponent(btnAnimatedGif)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(ckBoxReverseTime))
                                .addGroup(pnlAnimationLayout.createSequentialGroup()
                                        .addComponent(lblAnimationSpeed)
                                        .addPreferredGap(ComponentPlacement.RELATED)
                                        .addComponent(animationSpeed, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
        );
        pnlAnimationLayout.setVerticalGroup(
                pnlAnimationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(animationToolBar, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addComponent(lblTime)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(sliderTime, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(pnlAnimationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                .addGroup(pnlAnimationLayout.createSequentialGroup()
                                        .addGroup(pnlAnimationLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                                                .addComponent(btnOpenAnimation)
                                                .addComponent(btnDeleteMaps))
                                        .addPreferredGap(ComponentPlacement.RELATED, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblFolder))
                                .addComponent(btnSaveAsMaps))
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addGroup(pnlAnimationLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
                                .addComponent(lblFramePerSecond)
                                .addGroup(pnlAnimationLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnAnimatedGif)
                                        .addComponent(ckBoxReverseTime)))
                        .addPreferredGap(ComponentPlacement.UNRELATED)
                        .addGroup(pnlAnimationLayout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                                .addComponent(lblAnimationSpeed)
                                .addComponent(animationSpeed, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12))
        );

        taskPaneAnimation.add(pnlAnimation);

        GroupLayout stepsPanelLayout = new GroupLayout(stepsPanel);
        stepsPanel.setLayout(stepsPanelLayout);
        stepsPanelLayout.setHorizontalGroup(
                stepsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(taskPaneConfiguration, GroupLayout.PREFERRED_SIZE, 466, GroupLayout.PREFERRED_SIZE)
                .addComponent(taskPaneSimulation, GroupLayout.PREFERRED_SIZE, 466, GroupLayout.PREFERRED_SIZE)
                .addComponent(taskPaneMapping, GroupLayout.PREFERRED_SIZE, 466, GroupLayout.PREFERRED_SIZE)
                .addComponent(taskPaneAnimation, GroupLayout.PREFERRED_SIZE, 466, GroupLayout.PREFERRED_SIZE)
        );
        stepsPanelLayout.setVerticalGroup(
                stepsPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(stepsPanelLayout.createSequentialGroup()
                        .addComponent(taskPaneConfiguration, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(taskPaneSimulation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(taskPaneMapping, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(taskPaneAnimation, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        stepsScrollPane.setViewportView(stepsPanel);

        GroupLayout titledPanelStepsLayout = new GroupLayout(titledPanelSteps);
        titledPanelSteps.setLayout(titledPanelStepsLayout);
        titledPanelStepsLayout.setHorizontalGroup(
                titledPanelStepsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(stepsScrollPane, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
        );
        titledPanelStepsLayout.setVerticalGroup(
                titledPanelStepsLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(stepsScrollPane, GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
        );

        leftSplitPane.setLeftComponent(titledPanelSteps);

        titledPanelLogger.setTitle(resourceMap.getString("titledPanelLogger.title"));
        titledPanelLogger.setName("titledPanelLogger");

        loggerScrollPane.setName("loggerScrollPane");

        GroupLayout titledPanelLoggerLayout = new GroupLayout(titledPanelLogger);
        titledPanelLogger.setLayout(titledPanelLoggerLayout);
        titledPanelLoggerLayout.setHorizontalGroup(
                titledPanelLoggerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(loggerScrollPane, GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
        );
        titledPanelLoggerLayout.setVerticalGroup(
                titledPanelLoggerLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(loggerScrollPane, GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
        );

        leftSplitPane.setRightComponent(titledPanelLogger);

        splitPane.setLeftComponent(leftSplitPane);

        titledPanelMain.setTitle(resourceMap.getString("titledPanelMain.title"));
        titledPanelMain.setMinimumSize(new java.awt.Dimension(32, 32));
        titledPanelMain.setName("titledPanelMain");

        rightScrollPane.setName("rightScrollPane");

        gradientPanel.setName("gradientPanel");

        GroupLayout gradientPanelLayout = new GroupLayout(gradientPanel);
        gradientPanel.setLayout(gradientPanelLayout);
        gradientPanelLayout.setHorizontalGroup(
                gradientPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGap(0, 683, Short.MAX_VALUE)
        );
        gradientPanelLayout.setVerticalGroup(
                gradientPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGap(0, 489, Short.MAX_VALUE)
        );

        rightScrollPane.setViewportView(gradientPanel);
        createMainPanel();

        GroupLayout titledPanelMainLayout = new GroupLayout(titledPanelMain);
        titledPanelMain.setLayout(titledPanelMainLayout);
        titledPanelMainLayout.setHorizontalGroup(
                titledPanelMainLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(rightScrollPane, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
        );
        titledPanelMainLayout.setVerticalGroup(
                titledPanelMainLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(rightScrollPane, GroupLayout.Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
        );

        splitPane.setRightComponent(titledPanelMain);

        GroupLayout mainPanelLayout = new GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
                mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 869, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
                mainPanelLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addComponent(splitPane, GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
        );

        previewScrollPane.setBorder(null);
        previewScrollPane.setWheelScrollingEnabled(true);
        previewScrollPane.setName("scrollPaneSimulationUI");
        previewScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        previewScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        previewScrollPane.setViewportView(previewPanel);

        previewPanel.setBorder(null);
        previewPanel.addMouseListener(previewMouseAdapter);
        previewPanel.addMouseMotionListener(previewMouseAdapter);

        pnlLogo.setAlpha(0.4F);
        pnlLogo.setInheritAlpha(false);
        pnlLogo.setName("pnlLogo");

        hyperLinkLogo.setAction(actionMap.get("browse"));
        hyperLinkLogo.setHorizontalAlignment(SwingConstants.CENTER);
        hyperLinkLogo.setHorizontalTextPosition(SwingConstants.CENTER);
        hyperLinkLogo.setName("hyperLinkLogo");
        hyperLinkLogo.setRolloverIcon(resourceMap.getIcon("hyperLinkLogo.rolloverIcon"));
        hyperLinkLogo.setSelectedIcon(resourceMap.getIcon("hyperLinkLogo.selectedIcon"));
        hyperLinkLogo.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                hyperLinkLogoMouseEntered(evt);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                hyperLinkLogoMouseExited(evt);
            }
        });

        GroupLayout pnlLogoLayout = new GroupLayout(pnlLogo);
        pnlLogo.setLayout(pnlLogoLayout);
        pnlLogoLayout.setHorizontalGroup(
                pnlLogoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlLogoLayout.createSequentialGroup()
                        .addGap(0, 33, Short.MAX_VALUE)
                        .addComponent(hyperLinkLogo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 33, Short.MAX_VALUE))
        );
        pnlLogoLayout.setVerticalGroup(
                pnlLogoLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(pnlLogoLayout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(hyperLinkLogo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
        );

        setComponent(mainPanel);
        setMenuBar(menuBar);
    }

    private void lblFramePerSecondMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            animationSpeed.setValue(1.5f);
        }
    }

    private void animationSpeedStateChanged(ChangeEvent evt) {
        JSpinner source = (JSpinner) evt.getSource();
        nbfps = (Float) source.getValue();
    }

    private void hyperLinkLogoMouseEntered(java.awt.event.MouseEvent evt) {
        pnlLogo.setAlpha(0.9f);
    }

    private void hyperLinkLogoMouseExited(java.awt.event.MouseEvent evt) {
        pnlLogo.setAlpha(0.4f);
    }

    private void taskPaneConfigurationPropertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("collapsed")) {
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
    }

    private void taskPaneSimulationPropertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("collapsed")) {
            if (!(Boolean) evt.getNewValue()) {
                taskPaneConfiguration.setCollapsed(true);
                taskPaneAnimation.setCollapsed(true);
                taskPaneMapping.setCollapsed(true);
                lblSimulation.setVisible(true);
            } else {
                lblSimulation.setVisible(false);
                hideSimulationPreview();
            }
            setMainTitle();
        }
    }

    private void taskPaneMappingPropertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("collapsed")) {
            if (!(Boolean) evt.getNewValue()) {
                taskPaneSimulation.setCollapsed(true);
                taskPaneAnimation.setCollapsed(true);
                taskPaneConfiguration.setCollapsed(true);
                wmsMapper.setVisible(null != outputFile);
                lblMapping.setVisible(null == outputFile);
            } else {
                wmsMapper.setVisible(false);
                lblMapping.setVisible(false);
            }
            setMainTitle();
        }
    }

    private void taskPaneAnimationPropertyChange(java.beans.PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals("collapsed")) {
            if (!(Boolean) evt.getNewValue()) {
                taskPaneSimulation.setCollapsed(true);
                taskPaneConfiguration.setCollapsed(true);
                taskPaneMapping.setCollapsed(true);
                replayPanel.setVisible(null != outputFolder);
                lblAnimation.setVisible(null == outputFolder);
            } else {
                stopAnimation();
                replayPanel.setVisible(false);
                lblAnimation.setVisible(false);
            }
            setMainTitle();
        }
    }

    private void btnParticleColorActionPerformed(java.awt.event.ActionEvent evt) {
        final JButton btn = (JButton) evt.getSource();
        SwingUtilities.invokeLater(() -> {
            btn.setForeground(chooseColor(btn, btn.getForeground()));
            wmsMapper.setColorbar(null, 0, 0, null);
            wmsMapper.setDefaultColor(btn.getForeground());
        });
        getLogger().info(resourceMap.getString("btnColor.msg.apply"));
    }

    private void spinnerParticleSizeStateChanged(ChangeEvent evt) {
        JSpinner source = (JSpinner) evt.getSource();
        final int pixel = (Integer) source.getValue();
        SwingUtilities.invokeLater(() -> {
            wmsMapper.setParticlePixel(pixel);
        });
    }

    private void sliderPreviewSizeStateChanged(ChangeEvent e) {
        JSlider source = (JSlider) e.getSource();
        if (!source.getValueIsAdjusting()) {
            previewPanel.setHeight((int) source.getValue());
            previewPanel.revalidate();
        }
    }

    ////////////////////////////
    // utilities
    ////////////////////////////
    private SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    private Logger getLogger() {
        return SimulationManager.getLogger();
    }

    private String changeExtension(String filename, String extension) {
        return filename.substring(0, filename.lastIndexOf(".") + 1) + extension;
    }

    private File getDefaultCfgPath() {
        StringBuilder path = new StringBuilder();
        path.append(System.getProperty("user.dir"));
        if (!path.toString().endsWith(File.separator)) {
            path.append(File.separator);
        }
        path.append("cfg");
        path.append(File.separator);

        // generate a file name
        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyyMMdd");
        Calendar calendar = new GregorianCalendar();
        calendar.setTimeInMillis(System.currentTimeMillis());
        dtFormat.setCalendar(calendar);
        path.append(dtFormat.format(calendar.getTime()));
        path.append("_");
        String username = System.getProperty("user.name");
        if (null != username && !username.isEmpty()) {
            path.append(username);
            path.append("_");

        }
        path.append("ichthyop-config.cfg");
        return new File(path.toString());
    }

    ////////////////////////////
    // UI components declaration
    ////////////////////////////
    // menu 
    private JMenuBar menuBar;
    // menu configuration
    private JMenu configurationMenu;
    private JMenuItem newMenuItem;
    private JMenuItem openMenuItem;
    private JMenuItem reloadMenuItem;
    private JMenuItem closeMenuItem;
    private JMenuItem saveAsMenuItem;
    private JMenuItem saveMenuItem;
    private JMenuItem exitMenuItem;
    // menu simulation
    private JMenu simulationMenu;
    private JMenuItem simulationMenuItem;
    private JMenuItem previewMenuItem;
    private JMenuItem savePreviewMenuItem;
    // menu mapping
    private JMenu mappingMenu;
    private JMenuItem mapMenuItem;
    private JMenuItem cancelMapMenuItem;
    private JMenuItem openNCMenuItem;
    private JMenuItem exportToKMZMenuItem;
    // menu animation
    private JMenu animationMenu;
    private JMenuItem startBWMenuItem;
    private JMenuItem startFWMenuItem;
    private JMenuItem stopMenuItem;
    private JMenuItem deleteMenuItem;
    private JMenuItem openAnimationMenuItem;
    private JMenuItem saveasMapsMenuItem;
    // help menu
    private JMenu helpMenu;
    private JMenuItem timeConverterMenuItem;
    private JMenuItem aboutMenuItem;

    // configuration components
    private JXTaskPane taskPaneConfiguration;
    private JPanel pnlFile;
    private JButton btnOpenCfgFile;
    private JButton btnCloseCfgFile;
    private JButton btnNewCfgFile;
    private JButton btnSaveAsCfgFile;
    private JButton btnReloadCfgFile;
    private JButton btnSaveCfgFile;
    private JLabel lblConfiguration;
    private JLabel lblCfgFile;
    private final JConfigurationPanel pnlConfiguration = new JConfigurationPanel();

    // simulation
    private JXTaskPane taskPaneSimulation;
    private JPanel pnlSimulation;
    private JLabel lblSimulation;
    // simulation preview
    private JButton btnPreview;
    private JScrollPane previewScrollPane;
    private SimulationPreviewPanel previewPanel;
    private JSlider sliderPreviewZoom;
    private JLabel lblPreviewZoom;
    private JButton btnSavePreview;
    // simulation run
    private JButton btnSimulationRun;

    // mapping components
    private JXTaskPane taskPaneMapping;
    private JPanel pnlMapping;
    private JLabel lblMapping;
    private JPanel pnlWMS;
    private JPanel pnlColor;
    private JPanel pnlColorBar;
    private JButton btnApplyColorbar;
    private JButton btnAutoRange;
    private JButton btnCancelMapping;
    private JButton btnCloseNC;
    private JButton btnExportToKMZ;
    private JButton btnMapping;
    private JButton btnOpenNC;
    private JButton btnParticleColor;
    private JComboBox cbBoxVariable;
    private JComboBox cbBoxWMS;
    private JLabel lblVariable;
    private JLabel lblWMS;
    private JLabel lblParticleColor;
    private JLabel lblParticleSize;
    private JLabel lblMax;
    private JLabel lblMin;
    private JLabel lblNC;
    private JFormattedTextField txtFieldMax;
    private JFormattedTextField txtFieldMin;
    private JSpinner spinnerParticleSize;
    private JLabel lblColorbarChooser;
    private JComboBox colorbarChooser;
    private final WMSMapper wmsMapper = new WMSMapper();

    // animation components
    private JXTaskPane taskPaneAnimation;
    private JPanel pnlAnimation;
    private JLabel lblAnimation;
    private JToolBar animationToolBar;
    private JSpinner animationSpeed;
    private JButton btnAnimatedGif;
    private JButton btnAnimationBW;
    private JButton btnAnimationFW;
    private JButton btnAnimationStop;
    private JButton btnDeleteMaps;
    private JButton btnFirst;
    private JButton btnLast;
    private JButton btnNext;
    private JButton btnOpenAnimation;
    private JButton btnPrevious;
    private JButton btnSaveAsMaps;
    private JCheckBox ckBoxReverseTime;
    private JLabel lblAnimationSpeed;
    private JLabel lblFolder;
    private JLabel lblFramePerSecond;
    private JLabel lblTime;
    private JSlider sliderTime;
    private final ReplayPanel replayPanel = new ReplayPanel();
    // animation variables

    // containers
    private JXTitledPanel titledPanelMain;
    private JPanel mainPanel;
    private JSplitPane splitPane;
    // left
    private JSplitPane leftSplitPane;
    // steps
    private JXTitledPanel titledPanelSteps;
    private JPanel stepsPanel;
    private JScrollPane stepsScrollPane;
    // logger
    private JXTitledPanel titledPanelLogger;
    private LoggerScrollPane loggerScrollPane;
    // right
    private JScrollPane rightScrollPane;
    private GradientPanel gradientPanel;
    private JXPanel pnlLogo;
    private JXHyperlink hyperLinkLogo;

    // about box
    private JDialog aboutBox;

    // status bar
    private final JStatusBar statusBar = new JStatusBar();

    // animation mouse wheel scroller
    private class AnimationMouseWheelScroller implements MouseWheelListener {

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            int increment = e.getWheelRotation();
            if (increment > 0) {
                for (int i = 0; i < increment; i++) {
                    next();
                }
            } else {
                for (int i = 0; i < Math.abs(increment); i++) {
                    previous();
                }
            }
        }
    }

    // simulation preview png save filter
    private class PNGSaveFilter extends FileFilter {

        @Override
        public boolean accept(File f) {
            String s = f.getName();
            return f.isDirectory() || s.endsWith(".png") || s.endsWith(".PNG");
        }

        @Override
        public String getDescription() {
            return "*.png,*.PNG";
        }

    }

    // mouse adapter for simulation preview
    private class PreviewMouseAdapter extends MouseAdapter {

        private Point origin;

        @Override
        public void mousePressed(MouseEvent e) {
            origin = new Point(e.getPoint());
            JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, previewPanel);
            if (previewPanel.getSize().getWidth() > viewPort.getSize().getWidth()
                    || previewPanel.getSize().getHeight() > viewPort.getSize().getHeight()) {
                previewPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            previewPanel.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            if (origin != null) {
                JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, previewPanel);
                if (viewPort != null) {
                    int deltaX = origin.x - e.getX();
                    int deltaY = origin.y - e.getY();

                    Rectangle view = viewPort.getViewRect();
                    view.x += deltaX;
                    view.y += deltaY;
                    previewPanel.scrollRectToVisible(view);
                }
            }
        }
    };

    private enum TimeDirection {

        FORWARD, BACKWARD;
    }
}
