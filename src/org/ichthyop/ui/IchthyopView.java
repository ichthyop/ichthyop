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
import java.awt.Font;
import java.beans.PropertyChangeEvent;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.EventObject;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JColorChooser;
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
import org.ichthyop.io.IOTools;
import org.ichthyop.manager.SimulationManager;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import org.ichthyop.Template;
import org.jdesktop.animation.timing.Animator;
import org.jdesktop.animation.timing.TimingTarget;
import org.jdesktop.swingx.painter.Painter;
import org.ichthyop.ui.logging.LogLevel;
import org.ichthyop.util.MetaFilenameFilter;

/**
 * The application's main frame.
 */
public class IchthyopView extends FrameView
        implements TimingTarget, PropertyChangeListener {

    public IchthyopView(SingleFrameApplication app) {
        super(app);

        initComponents();
        setStatusBar(statusBar);

        /* Plug the loggerPanel and the StatusBar */
        loggerScrollPane.connectToLogger(getLogger());
        statusBar.connectToLogger(getLogger());
        /* Welcome user */
        getLogger().info(Application.getInstance().getContext().getResourceMap().getString("Application.msg.welcome"));

        /* Set frame icon */
        getFrame().setIconImage(getResourceMap().getImageIcon("Application.icon").getImage());

        /* Disabled some actions */
        closeMenuItem.getAction().setEnabled(false);
        saveAsMenuItem.getAction().setEnabled(false);
        btnSimulationRun.getAction().setEnabled(false);
        btnPreview.getAction().setEnabled(false);
        ckBoxDrawGrid.setEnabled(false);
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

    private SimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
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
        File[] files2Delete = outputFolder.listFiles(new MetaFilenameFilter("*.png"));
        StringBuilder message = new StringBuilder(getResourceMap().getString("deleteMaps.dialog.msg.part1"));
        message.append(" ");
        message.append(outputFolder.getName());
        message.append(" ?");
        message.append('\n');
        message.append(files2Delete.length);
        message.append(" ");
        message.append(getResourceMap().getString("deleteMaps.dialog.msg.part2"));
        int dialog = JOptionPane.showConfirmDialog(getFrame(), message.toString(), getResourceMap().getString("deleteMaps.dialog.title"), JOptionPane.OK_CANCEL_OPTION);
        if (dialog == JOptionPane.OK_OPTION) {
            for (File file : files2Delete) {
                file.delete();
            }
            outputFolder.delete();
            StringBuilder sb = new StringBuilder();
            sb.append(resourceMap.getString("animation.text"));
            sb.append(" ");
            sb.append(files2Delete.length);
            sb.append(" ");
            sb.append(resourceMap.getString("deleteMaps.msg.deleted"));
            getLogger().log(Level.INFO, sb.toString());
            closeFolderAnimation();
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        btnSaveCfgFile.getAction().setEnabled(true);
    }

    @Action
    public Task exportToKMZ() {
        return kmzTask = new ExportToKMZTask(getApplication());
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
            btnCancelMapping.getAction().setEnabled(true);
            setColorbarPanelEnabled(false);
        }

        @Override
        protected Object doInBackground() throws Exception {
            wmsMapper.createKML();
            setMessage(resourceMap.getString("exportToKMZ.msg.exporting"), true, Level.INFO);
            for (int i = 0; i < wmsMapper.getNbSteps(); i++) {
                setProgress((float) (i + 1) / wmsMapper.getNbSteps());
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
            btnCancelMapping.getAction().setEnabled(false);
            setColorbarPanelEnabled(true);
        }

        @Override
        void onSuccess(Object o) {
            setMessage(resourceMap.getString("exportToKMZ.msg.exported") + " " + wmsMapper.getKMZPath(), false, LogLevel.COMPLETE);
        }

        @Override
        void onFailure(Throwable t) {
        }

        @Override
        protected void cancelled() {
            setMessage(resourceMap.getString("exportToKMZ.msg.cancelled"));
        }
    }

    @Action
    public Task createMaps() {
        return createMapTask = new CreateMapTask(getApplication());
    }

    private class CreateMapTask extends SFTask<Object, Painter[]> {

        private int index;

        CreateMapTask(Application instance) {
            super(instance);
            applyColorbarSettings();
            wmsMapper.setDefaultColor(btnParticleColor.getForeground());
            wmsMapper.setParticlePixel((Integer) spinnerParticleSize.getValue());
            wmsMapper.setZoomButtonsVisible(false);
            wmsMapper.setZoomSliderVisible(false);
            btnMapping.getAction().setEnabled(false);
            btnExportToKMZ.getAction().setEnabled(false);
            btnOpenNC.getAction().setEnabled(false);
            btnCloseNC.getAction().setEnabled(false);
            btnCancelMapping.getAction().setEnabled(true);
            setColorbarPanelEnabled(false);
            index = 0;
        }

        @Override
        protected Object doInBackground() throws Exception {

            /* Delete existing pictures in folder */
            File wmsfolder = wmsMapper.getFolder();
            if (null != wmsfolder && wmsfolder.isDirectory()) {
                for (File picture : wmsfolder.listFiles(new MetaFilenameFilter("*.png"))) {
                    picture.delete();
                }
                setMessage(resourceMap.getString("createMaps.msg.cleanup"));
            }

            /* Create painters */
            setMessage(resourceMap.getString("createMaps.msg.start"), true, Level.INFO);
            for (int iStep = 0; iStep < wmsMapper.getNbSteps(); iStep++) {
                setProgress((float) (iStep + 1) / wmsMapper.getNbSteps());
                publish(new Painter[]{wmsMapper.getPainterForStep(iStep), wmsMapper.getTimePainter(iStep)});
                if (getProgress() % 10 == 0) {
                    Thread.sleep(500);
                }
            }
            return null;
        }

        @Override
        protected void process(List<Painter[]> painters) {
            for (Painter[] painter : painters) {
                wmsMapper.map(painter[0], painter[1]);
                wmsMapper.screen2File(index++);
            }
        }

        @Override
        protected void finished() {
            wmsMapper.setZoomButtonsVisible(true);
            wmsMapper.setZoomSliderVisible(true);
            wmsMapper.drawBackground();
            btnMapping.getAction().setEnabled(true);
            btnExportToKMZ.getAction().setEnabled(true);
            btnCancelMapping.getAction().setEnabled(false);
            btnOpenNC.getAction().setEnabled(true);
            btnCloseNC.getAction().setEnabled(true);
            setColorbarPanelEnabled(true);
        }

        @Override
        void onSuccess(Object result) {
            StringBuilder sb = new StringBuilder();
            sb.append(resourceMap.getString("mapping.text"));
            sb.append(" ");
            sb.append(index);
            sb.append(" ");
            sb.append(resourceMap.getString("createMaps.msg.succeeded"));
            setMessage(sb.toString(), false, LogLevel.COMPLETE);
            replayPanel.setFolder(null);
            taskPaneMapping.setCollapsed(true);
            // move to animation pane
            taskPaneAnimation.setCollapsed(false);
            setAnimationToolsEnabled(false);
            btnAnimationFW.getAction().setEnabled(true);
            animationLoaded = false;
            lblFolder.setText(wmsMapper.getFolder().getName());
            lblFolder.setFont(lblFolder.getFont().deriveFont(Font.PLAIN, 12));
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
            openNetCDF();
        }
    }

    @Action
    public void closeNetCDF() {

        getLogger().log(Level.INFO, "{0} {1}", new Object[]{getResourceMap().getString("closeNetCDF.msg.closed"), outputFile.getAbsolutePath()});
        outputFile = null;
        lblNC.setText(getResourceMap().getString("lblNC.text"));
        lblNC.setFont(lblNC.getFont().deriveFont(Font.PLAIN, 12));
        wmsMapper.setFile(null);
        btnMapping.getAction().setEnabled(false);
        btnExportToKMZ.getAction().setEnabled(false);
        btnCloseNC.getAction().setEnabled(false);
        cbBoxVariable.setModel(new DefaultComboBoxModel(new String[]{"None"}));
        setColorbarPanelEnabled(false);
    }

    public void openNetCDF() {

        getLogger().log(Level.INFO, "{0} {1}", new Object[]{getResourceMap().getString("openNcMapping.msg.opened"), outputFile.getAbsolutePath()});
        lblNC.setText(outputFile.getName());
        lblNC.setFont(lblNC.getFont().deriveFont(Font.PLAIN, 12));
        wmsMapper.setFile(outputFile);
        wmsMapper.setVisible(!taskPaneMapping.isCollapsed());
        lblMapping.setVisible(false);
        btnMapping.getAction().setEnabled(true);
        btnCloseNC.getAction().setEnabled(true);
        btnExportToKMZ.getAction().setEnabled(true);
        setMainTitle();
        setColorbarPanelEnabled(true);
        cbBoxVariable.setModel(new DefaultComboBoxModel(wmsMapper.getVariableList()));
    }

    public void closeFolderAnimation() {
        lblFolder.setText(getResourceMap().getString("lblFolder.text"));
        outputFolder = null;
        replayPanel.setFolder(null);
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
        int nbPNG = 0;

        LoadFolderAnimationTask(Application instance, File folder) {
            super(instance);
            this.folder = folder;
            setMessage(resourceMap.getString("openFolderAnimation.msg.opened") + " " + folder.getAbsolutePath());
        }

        @Override
        protected Object doInBackground() throws Exception {
            try {
                nbPNG = folder.listFiles(new MetaFilenameFilter("*.png")).length;
                if (nbPNG > 0) {
                    return null;
                }
            } catch (Exception ex) {
            }
            throw new NullPointerException(resourceMap.getString("openFolderAnimation.msg.failed") + " " + folder.getAbsolutePath());
        }

        @Override
        public void onSuccess(Object o) {
            outputFolder = folder;
            lblFolder.setText(outputFolder.getName());
            lblFolder.setFont(lblFolder.getFont().deriveFont(Font.PLAIN, 12));
            replayPanel.setFolder(outputFolder);
            sliderTime.setMaximum(nbPNG - 1);
            setAnimationToolsEnabled(true);
            sliderTime.setValue(0);
            animationLoaded = true;
            startAnimationFW();
        }

        @Override
        public void onFailure(Throwable t) {
            lblFolder.setText(resourceMap.getString("lblFolder.text"));
            lblFolder.setFont(lblFolder.getFont().deriveFont(Font.PLAIN, 12));
            sliderTime.setMaximum(0);
            sliderTime.setValue(0);
            setAnimationToolsEnabled(false);
            animationLoaded = false;
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
    public void saveasMaps() {
        stopAnimation();
        getLogger().info(getResourceMap().getString("saveasMaps.msg.launch"));
        getApplication().show(new ExportMapsView(IchthyopApp.getApplication(), replayPanel.getFolder()));
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
        }

        @Override
        protected Object doInBackground() throws Exception {
            AnimatedGifEncoder gif = new AnimatedGifEncoder();
            String path = replayPanel.getFolder().getAbsolutePath();
            if (path.endsWith(File.separator)) {
                path = path.substring(0, path.length() - 1);
            }
            path += ".gif";
            /* Should i ask every time if user agrees to overwrite */
 /*if (new File(path).exists()) {
             String message = path + " " + resourceMap.getString("createAnimatedGif.dialog.overwrite");
             int dialog = JOptionPane.showConfirmDialog(getFrame(), message, resourceMap.getString("createAnimatedGif.dialog.title"), JOptionPane.OK_CANCEL_OPTION);
             if (!(dialog == JOptionPane.OK_OPTION)) {
             cancel(true);
             return null;
             }
             }*/
            gif.start(path);
            gif.setDelay((int) (1000 / nbfps));
            setMessage(resourceMap.getString("createAnimatedGif.msg.start"), true, Level.INFO);
            List<BufferedImage> pictures = replayPanel.getImages();
            if (ckBoxReverseTime.isSelected()) {
                Collections.reverse(pictures);
            }
            for (int i = 0; i < pictures.size(); i++) {
                setProgress((i + 1.f) / pictures.size());
                gif.addFrame(pictures.get(i));
            }
            gif.finish();
            return path;
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

    private void showSimulationPreview() {
        pnlProgress.setVisible(false);
        //scrollPaneSimulationUI.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        //scrollPaneSimulationUI.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPaneSimulationUI.setPreferredSize(pnlSimulationUI.getSize());
        scrollPaneSimulationUI.revalidate();
        scrollPaneSimulationUI.setVisible(true);
        getSimulationUI().init();
        getSimulationUI().repaintBackground();
        ckBoxDrawGrid.setEnabled(true);
    }

    private void hideSimulationPreview() {
        scrollPaneSimulationUI.setVisible(false);
        if (!taskPaneSimulation.isCollapsed()) {
            pnlProgress.setVisible(true);
        }
        ckBoxDrawGrid.setEnabled(false);
    }

    private class SimulationPreviewTask extends SFTask {

        private boolean setupSucceeded;
        private boolean initSucceeded;

        SimulationPreviewTask(Application instance, boolean isEnabled) {
            super(instance);
            setupSucceeded = false;
            initSucceeded = false;
        }

        @Override
        protected Object doInBackground() throws Exception {
            if (!initDone) {
                setMessage(resourceMap.getString("simulationRun.msg.init.start"));
                getSimulationManager().setup();
                setupSucceeded = true;
                getSimulationManager().init();
                initSucceeded = true;
            }
            return null;
        }

        @Override
        protected void onSuccess(Object obj) {
            initDone = true;
            setMessage(resourceMap.getString("simulationRun.msg.init.ok"), false, LogLevel.COMPLETE);
            showSimulationPreview();
        }

        @Override
        void onFailure(Throwable throwable) {
            StringBuilder msg = new StringBuilder();
            msg.append(resourceMap.getString("simulationRun.msg.init.failed"));
            if (!setupSucceeded) {
                msg.append(" (performing: SETUP)");
            } else if (!initSucceeded) {
                msg.append(" (performing: INIT)");
            }
            setMessage(msg.toString(), false, Level.SEVERE);
            setupSucceeded = initSucceeded = false;
            btnPreview.setSelected(false);
            initDone = false;
        }
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

    @Action
    public Task saveAsConfigurationFile() {
        File cwd = cfgUntitled ? getDefaultCfgPath() : getSimulationManager().getConfigurationFile();
        System.out.println(cwd);
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
                sb.append(getResourceMap().getString("saveAsConfigurationFile.msg.overwrite"));
                int answer = JOptionPane.showConfirmDialog(fc, sb.toString(), getResourceMap().getString("saveAsConfigurationFile.Action.text"), JOptionPane.YES_NO_OPTION);
                if (answer != JOptionPane.YES_OPTION) {
                    saveAsConfigurationFile();
                    return null;
                }
            }
            try {
                getSimulationManager().getParameterManager().saveParameters(file.getAbsolutePath());
                //IOTools.copyFile(getSimulationManager().getConfigurationFile(), file);
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

    private File addExtension(File f, String extension) {

        if (!f.isDirectory() && f.getName().endsWith("." + extension)) {
            return f;
        }
        return new File(f.toString() + "." + extension);
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
            getSimulationManager().getParameterManager().saveParamters();
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
        closeMenuItem.getAction().setEnabled(false);
        pnlConfiguration.setVisible(false);
        lblConfiguration.setVisible(true);
        btnPreview.getAction().setEnabled(false);
        ckBoxDrawGrid.setEnabled(false);
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

    private class FailedTask extends SFTask {

        private final Exception exception;

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

    public Task loadConfigurationFile(File file) {
        try {
            getSimulationManager().setConfigurationFile(file);
        } catch (Exception e) {
            try {
                String msg = resourceMap.getString("openConfigurationFile.msg.error") + " ==> " + e.getMessage();
                Exception eclone = e.getClass().getConstructor(String.class).newInstance(msg);
                eclone.setStackTrace(e.getStackTrace());
                return new FailedTask(getApplication(), eclone);
            } catch (Exception ex) {
                return new FailedTask(getApplication(), e);
            }
        }
        File cfgFile = getSimulationManager().getConfigurationFile();
        getLogger().log(Level.INFO, "{0} {1}", new Object[]{getResourceMap().getString("openConfigurationFile.msg.opened"), cfgUntitled ? "Untitled configuration" : cfgFile.toString()});
        getFrame().setTitle(getResourceMap().getString("Application.title") + " - " + (cfgUntitled ? "Untitled configuration" : cfgFile.toString()));
        lblCfgFile.setText((cfgUntitled ? "Filename not set yet" : cfgFile.getAbsolutePath()));
        lblCfgFile.setFont(lblCfgFile.getFont().deriveFont(Font.PLAIN, 12));
        initDone = false;
        saveAsMenuItem.getAction().setEnabled(true);
        saveMenuItem.getAction().setEnabled(cfgUntitled);
        closeMenuItem.getAction().setEnabled(true);
        btnSimulationRun.getAction().setEnabled(true);
        btnPreview.getAction().setEnabled(true);
        setMainTitle();
        return pnlConfiguration.loadParameterTree();
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
        if (progressTimer != null && progressTimer.isRunning()) {
            progressTimer.stop();
            statusBar.getProgressBar().setValue(0);
            statusBar.getProgressBar().setVisible(false);
        }
        if (animator.isRunning()) {
            animator.stop();
            replayPanel.addMouseWheelListener(mouseScroller);
        }
    }

    private void startAnimation() {
        if (!animationLoaded) {
            getApplication().getContext().getTaskService().execute(new LoadFolderAnimationTask(getApplication(), wmsMapper.getFolder()));
        } else if (!animator.isRunning()) {
            replayPanel.removeMouseWheelListener(mouseScroller);
            animator.setAcceleration(0.002f);
            btnAnimationFW.setEnabled(true);
            animator.start();
        }
    }

    private void startAccelerationProgress() {
        progressTimer = new Timer(20, new ProgressAction());
        progressTimer.start();
    }

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
        startAccelerationProgress();
    }

    @Override
    public void end() {
        btnOpenAnimation.getAction().setEnabled(true);
        getLogger().info(getResourceMap().getString("animation.msg.stopped"));
    }

    @Override
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

        @Override
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
    public Task newConfigurationFile() throws IOException {

        // close any opened configuration file (and save if necessary)
        closeConfigurationFile();

        // create a temp file with generic template
        cfgUntitled = true;
        return loadConfigurationFile(Template.createTemplate());
    }

    @Action
    public void previewSimulation() {
        if (btnPreview.isSelected()) {
            if (!initDone) {
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
            if (btnPreview.isSelected()) {
                btnPreview.doClick();
            }
            btnCloseNC.doClick();
            closeFolderAnimation();
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
        previewMenuItem.getAction().setEnabled(enabled);
        simulationMenuItem.getAction().setEnabled(enabled);
        openNCMenuItem.getAction().setEnabled(enabled);
        openAnimationMenuItem.getAction().setEnabled(enabled);
    }

    public class SimulationRunTask extends SFTask {

        private boolean setupSucceeded;
        private boolean initSucceeded;
        private final String title;

        SimulationRunTask(Application instance) {
            super(instance);
            setMessage(resourceMap.getString("simulationRun.msg.started"));
            setMenuEnabled(false);
            pnlProgress.setupProgress();
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
            pnlProgress.printProgress();
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
            taskPaneSimulation.setCollapsed(true);
            taskPaneMapping.setCollapsed(false);
        }

        @Override
        protected void finished() {
            if (!isFailed()) {
                outputFile = new File(getSimulationManager().getOutputManager().getFileLocation());
                openNetCDF();
            }
            getFrame().setTitle(title);
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

        if (null != getSimulationManager().getConfigurationFile() && !cfgUntitled) {
            savePreference(openMenuItem, getSimulationManager().getConfigurationFile().getPath());
            //savePreference(blockTree, blockTree.getSelectedKey());
        } else {
            savePreference(openMenuItem, System.getProperty("user.dir"));
        }

        savePreference(animationSpeed, animationSpeed.getValue());
        savePreference(spinnerParticleSize, spinnerParticleSize.getValue());
        savePreference(btnParticleColor, btnParticleColor.getForeground());
        savePreference(btnColorMin, btnColorMin.getForeground());
        savePreference(btnColorMed, btnColorMed.getForeground());
        savePreference(btnColorMax, btnColorMax.getForeground());
        savePreference(txtFieldMin, txtFieldMin.getValue());
        savePreference(txtFieldMed, txtFieldMed.getValue());
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
        return (name == null) ? null : name + "." + getResourceMap().getString("preferences.filename");
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
        property = restorePreference(btnColorMin);
        if (property != null) {
            btnColorMin.setForeground((Color) property);
        }
        property = restorePreference(btnColorMed);
        if (property != null) {
            btnColorMed.setForeground((Color) property);
        }
        property = restorePreference(btnColorMax);
        if (property != null) {
            btnColorMax.setForeground((Color) property);
        }
        property = restorePreference(txtFieldMin);
        if (property != null) {
            txtFieldMin.setValue(property);
        }
        property = restorePreference(txtFieldMed);
        if (property != null) {
            txtFieldMed.setValue(property);
        }
        property = restorePreference(txtFieldMax);
        if (property != null) {
            txtFieldMax.setValue(property);
        }

        getFrame().setExtendedState(JFrame.MAXIMIZED_BOTH);
    }

    private Object restorePreference(Component bean) {
        try {
            return getContext().getLocalStorage().load(beanFilename(bean));
        } catch (IOException ex) {
            //getLogger().log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private class MouseWheelScroller implements MouseWheelListener {

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

    public JPanel getMainPanel() {
        return mainPanel;
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

    @Action
    public void applyColorbarSettings() {
        String vname = (String) cbBoxVariable.getSelectedItem();
        float vmin = Float.valueOf(txtFieldMin.getText());
        float vmed = Float.valueOf(txtFieldMed.getText());
        float vmax = Float.valueOf(txtFieldMax.getText());
        Color cmin = btnColorMin.getForeground();
        Color cmed = btnColorMed.getForeground();
        Color cmax = btnColorMax.getForeground();
        wmsMapper.setColorbar(vname, vmin, vmed, vmax, cmin, cmed, cmax);
        if (!vname.toLowerCase().contains("none")) {
            getLogger().info(getResourceMap().getString("applyColorbarSettings.msg.applied"));
        }
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

        ResourceMap resourceMap = Application.getInstance(org.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
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
            return wmsMapper.getRange(variable);
        }

        @Override
        void onSuccess(float[] result) {
            if (null != result) {
                txtFieldMin.setValue(result[0]);
                txtFieldMed.setValue(0.5f * (result[0] + result[1]));
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
        txtFieldMed.setEnabled(enabled);
        txtFieldMax.setEnabled(enabled);
        btnColorMin.setEnabled(enabled);
        btnColorMed.setEnabled(enabled);
        btnColorMax.setEnabled(enabled);
        btnAutoRange.getAction().setEnabled(enabled && !((String) cbBoxVariable.getSelectedItem()).equals("None"));
        btnApplyColorbar.getAction().setEnabled(enabled);
        spinnerParticleSize.setEnabled(enabled);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     */
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
        ckBoxDrawGrid = new javax.swing.JCheckBox();
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
        lblMed = new javax.swing.JLabel();
        txtFieldMed = new javax.swing.JFormattedTextField();
        btnColorMed = new javax.swing.JButton();
        lblColor = new javax.swing.JLabel();
        btnParticleColor = new javax.swing.JButton();
        lblColor1 = new javax.swing.JLabel();
        spinnerParticleSize = new javax.swing.JSpinner();
        taskPaneAnimation = new org.jdesktop.swingx.JXTaskPane();
        pnlAnimation = new javax.swing.JPanel();
        lblFramePerSecond = new javax.swing.JLabel();
        animationSpeed = new javax.swing.JSpinner();
        btnDeleteMaps = new javax.swing.JButton();
        btnSaveAsMaps = new javax.swing.JButton();
        lblAnimationSpeed = new javax.swing.JLabel();
        btnOpenAnimation = new javax.swing.JButton();
        lblFolder = new javax.swing.JLabel();
        sliderTime = new javax.swing.JSlider();
        lblTime = new javax.swing.JLabel();
        jToolBar1 = new javax.swing.JToolBar();
        btnFirst = new javax.swing.JButton();
        btnPrevious = new javax.swing.JButton();
        btnAnimationBW = new javax.swing.JButton();
        btnAnimationStop = new javax.swing.JButton();
        btnAnimationFW = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnLast = new javax.swing.JButton();
        btnAnimatedGif = new javax.swing.JButton();
        ckBoxReverseTime = new javax.swing.JCheckBox();
        titledPanelLogger = new org.jdesktop.swingx.JXTitledPanel();
        loggerScrollPane = new org.ichthyop.ui.LoggerScrollPane();
        titledPanelMain = new org.jdesktop.swingx.JXTitledPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        gradientPanel = new org.ichthyop.ui.GradientPanel();
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
        simulationMenuItem = new javax.swing.JMenuItem();
        previewMenuItem = new javax.swing.JMenuItem();
        mappingMenu = new javax.swing.JMenu();
        mapMenuItem = new javax.swing.JMenuItem();
        exportToKMZMenuItem = new javax.swing.JMenuItem();
        cancelMapMenuItem = new javax.swing.JMenuItem();
        jSeparator13 = new javax.swing.JPopupMenu.Separator();
        openNCMenuItem = new javax.swing.JMenuItem();
        animationMenu = new javax.swing.JMenu();
        startFWMenuItem = new javax.swing.JMenuItem();
        stopMenuItem = new javax.swing.JMenuItem();
        startBWMenuItem = new javax.swing.JMenuItem();
        jSeparator15 = new javax.swing.JPopupMenu.Separator();
        openAnimationMenuItem = new javax.swing.JMenuItem();
        jSeparator14 = new javax.swing.JPopupMenu.Separator();
        saveasMapsMenuItem = new javax.swing.JMenuItem();
        deleteMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        scrollPaneSimulationUI = new javax.swing.JScrollPane();
        pnlSimulationUI = new SimulationUI();
        btnExit = new javax.swing.JButton();
        pnlLogo = new org.jdesktop.swingx.JXPanel();
        hyperLinkLogo = new org.jdesktop.swingx.JXHyperlink();

        mainPanel.setName("mainPanel"); // NOI18N

        splitPane.setDividerLocation(490);
        splitPane.setName("splitPane"); // NOI18N
        splitPane.setOneTouchExpandable(true);

        leftSplitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        leftSplitPane.setResizeWeight(1.0);
        leftSplitPane.setName("leftSplitPane"); // NOI18N
        leftSplitPane.setOneTouchExpandable(true);

        org.jdesktop.application.ResourceMap ivResourceMap = org.jdesktop.application.Application.getInstance().getContext().getResourceMap(IchthyopView.class);
        titledPanelSteps.setTitle(ivResourceMap.getString("titledPanelSteps.title")); // NOI18N
        titledPanelSteps.setMinimumSize(new java.awt.Dimension(200, 200));
        titledPanelSteps.setName("titledPanelSteps"); // NOI18N

        stepsScrollPane.setName("stepsScrollPane"); // NOI18N
        stepsScrollPane.setPreferredSize(new java.awt.Dimension(300, 400));

        stepsPanel.setName("stepsPanel"); // NOI18N

        taskPaneConfiguration.setAnimated(false);
        taskPaneConfiguration.setIcon(ivResourceMap.getIcon("step.Configuration.icon")); // NOI18N
        taskPaneConfiguration.setTitle(ivResourceMap.getString("step.Configuration.text")); // NOI18N
        taskPaneConfiguration.setName("taskPaneConfiguration"); // NOI18N
        taskPaneConfiguration.addPropertyChangeListener(this::taskPaneConfigurationPropertyChange);

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
                        .addGroup(pnlFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(btnOpenCfgFile)
                                .addComponent(btnNewCfgFile)
                                .addComponent(btnCloseCfgFile))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlFileLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnSaveCfgFile)
                                .addComponent(btnSaveAsCfgFile))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(lblCfgFile)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taskPaneConfiguration.add(pnlFile);

        taskPaneSimulation.setAnimated(false);
        taskPaneSimulation.setIcon(ivResourceMap.getIcon("step.Simulation.icon")); // NOI18N
        taskPaneSimulation.setTitle(ivResourceMap.getString("step.Simulation.text")); // NOI18N
        taskPaneSimulation.setName("taskPaneSimulation"); // NOI18N
        taskPaneSimulation.addPropertyChangeListener(this::taskPaneSimulationPropertyChange);

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

        ckBoxDrawGrid.setText(ivResourceMap.getString("ckBoxDrawGrid.text")); // NOI18N
        ckBoxDrawGrid.setName("ckBoxDrawGrid"); // NOI18N
        ckBoxDrawGrid.addActionListener(this::ckBoxDrawGridActionPerformed);

        javax.swing.GroupLayout pnlSimulationLayout = new javax.swing.GroupLayout(pnlSimulation);
        pnlSimulation.setLayout(pnlSimulationLayout);
        pnlSimulationLayout.setHorizontalGroup(
                pnlSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlSimulationLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnlSimulationLayout.createSequentialGroup()
                                        .addComponent(btnPreview)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSimulationRun))
                                .addComponent(ckBoxDrawGrid))
                        .addContainerGap(188, Short.MAX_VALUE))
        );
        pnlSimulationLayout.setVerticalGroup(
                pnlSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlSimulationLayout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(pnlSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(btnSimulationRun)
                                .addComponent(btnPreview))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(ckBoxDrawGrid)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        taskPaneSimulation.add(pnlSimulation);

        taskPaneMapping.setAnimated(false);
        taskPaneMapping.setIcon(ivResourceMap.getIcon("step.Mapping.icon")); // NOI18N
        taskPaneMapping.setTitle(ivResourceMap.getString("step.Mapping.text")); // NOI18N
        taskPaneMapping.setName("taskPaneMapping"); // NOI18N
        taskPaneMapping.addPropertyChangeListener(this::taskPaneMappingPropertyChange);

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

        cbBoxWMS.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"Offline", "http://www.openstreetmap.org/", "http://www.marine-geo.org/services/wms?", "http://www2.demis.nl/wms/wms.asp?wms=WorldMap&"}));
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

        lblNC.setFont(ivResourceMap.getFont("lblNC.font")); // NOI18N
        lblNC.setText(ivResourceMap.getString("lblNC.text")); // NOI18N
        lblNC.setName("lblNC"); // NOI18N

        btnCloseNC.setAction(actionMap.get("closeNetCDF")); // NOI18N
        btnCloseNC.setName("btnCloseNC"); // NOI18N

        btnExportToKMZ.setAction(actionMap.get("exportToKMZ")); // NOI18N
        btnExportToKMZ.setName("btnExportToKMZ"); // NOI18N

        pnlColor.setBorder(javax.swing.BorderFactory.createTitledBorder(ivResourceMap.getString("pnlColor.border.title"))); // NOI18N
        pnlColor.setName("pnlColor"); // NOI18N
        pnlColor.setOpaque(false);

        pnlColorBar.setBorder(javax.swing.BorderFactory.createTitledBorder(ivResourceMap.getString("pnlColorBar.border.title"))); // NOI18N
        pnlColorBar.setName("pnlColorBar"); // NOI18N
        pnlColorBar.setOpaque(false);

        lblVariable.setText(ivResourceMap.getString("lblVariable.text")); // NOI18N
        lblVariable.setName("lblVariable"); // NOI18N

        cbBoxVariable.setModel(new javax.swing.DefaultComboBoxModel(new String[]{"None"}));
        cbBoxVariable.setAction(actionMap.get("changeColorbarVariable")); // NOI18N
        cbBoxVariable.setName("cbBoxVariable"); // NOI18N

        lblMin.setText(ivResourceMap.getString("lblMin.text")); // NOI18N
        lblMin.setName("lblMin"); // NOI18N

        lblMax.setText(ivResourceMap.getString("lblMax.text")); // NOI18N
        lblMax.setName("lblMax"); // NOI18N

        txtFieldMax.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter(new java.text.DecimalFormat("###0.###"))));
        txtFieldMax.setName("txtFieldMax"); // NOI18N
        NumberFormat floatFormat = NumberFormat.getNumberInstance(Locale.US);
        floatFormat.setGroupingUsed(false);
        NumberFormatter floatFormatter = new NumberFormatter(floatFormat);
        floatFormatter.setFormat(floatFormat);
        txtFieldMax.setFormatterFactory(new DefaultFormatterFactory(floatFormatter));
        txtFieldMax.setValue(100.f);

        btnAutoRange.setAction(actionMap.get("autoRangeColorbar")); // NOI18N
        btnAutoRange.setName("btnAutoRange"); // NOI18N

        btnApplyColorbar.setAction(actionMap.get("applyColorbarSettings")); // NOI18N
        btnApplyColorbar.setName("btnApplyColorbar"); // NOI18N

        btnColorMin.setForeground(ivResourceMap.getColor("btnColorMin.foreground")); // NOI18N
        btnColorMin.setIcon(ivResourceMap.getIcon("btnColorMin.icon")); // NOI18N
        btnColorMin.setText(ivResourceMap.getString("btnColorMin.text")); // NOI18N
        btnColorMin.setName("btnColorMin"); // NOI18N
        btnColorMin.addActionListener(this::btnColorMinActionPerformed);

        btnColorMax.setForeground(ivResourceMap.getColor("btnColorMax.foreground")); // NOI18N
        btnColorMax.setIcon(ivResourceMap.getIcon("btnColorMax.icon")); // NOI18N
        btnColorMax.setText(ivResourceMap.getString("btnColorMax.text")); // NOI18N
        btnColorMax.setName("btnColorMax"); // NOI18N
        btnColorMax.addActionListener(this::btnColorMaxActionPerformed);

        txtFieldMin.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        txtFieldMin.setName("txtFieldMin"); // NOI18N
        txtFieldMin.setFormatterFactory(new DefaultFormatterFactory(floatFormatter));
        txtFieldMin.setValue(0.f);

        lblMed.setText(ivResourceMap.getString("lblMed.text")); // NOI18N
        lblMed.setName("lblMed"); // NOI18N

        txtFieldMed.setFormatterFactory(new javax.swing.text.DefaultFormatterFactory(new javax.swing.text.NumberFormatter()));
        txtFieldMed.setName("txtFieldMed"); // NOI18N
        txtFieldMed.setFormatterFactory(new DefaultFormatterFactory(floatFormatter));
        txtFieldMed.setValue(50.f);

        btnColorMed.setForeground(ivResourceMap.getColor("btnColorMed.foreground")); // NOI18N
        btnColorMed.setIcon(ivResourceMap.getIcon("btnColorMed.icon")); // NOI18N
        btnColorMed.setText(ivResourceMap.getString("btnColorMed.text")); // NOI18N
        btnColorMed.setName("btnColorMed"); // NOI18N
        btnColorMed.addActionListener(this::btnColorMedActionPerformed);

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
                                        .addComponent(btnAutoRange)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnApplyColorbar))
                                .addGroup(pnlColorBarLayout.createSequentialGroup()
                                        .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addGroup(pnlColorBarLayout.createSequentialGroup()
                                                        .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                                .addComponent(lblMin)
                                                                .addComponent(lblMax))
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                                .addComponent(txtFieldMax)
                                                                .addComponent(txtFieldMin, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addComponent(txtFieldMed, javax.swing.GroupLayout.PREFERRED_SIZE, 122, javax.swing.GroupLayout.PREFERRED_SIZE)))
                                                .addGroup(pnlColorBarLayout.createSequentialGroup()
                                                        .addComponent(lblMed)
                                                        .addGap(148, 148, 148)))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addGroup(pnlColorBarLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(btnColorMed)
                                                .addComponent(btnColorMax)
                                                .addComponent(btnColorMin))))
                        .addContainerGap(84, Short.MAX_VALUE))
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
                                .addComponent(lblMed)
                                .addComponent(txtFieldMed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addComponent(btnColorMed))
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

        lblColor.setText(ivResourceMap.getString("lblColor.text")); // NOI18N
        lblColor.setName("lblColor"); // NOI18N

        btnParticleColor.setForeground(ivResourceMap.getColor("btnParticleColor.foreground")); // NOI18N
        btnParticleColor.setIcon(ivResourceMap.getIcon("btnParticleColor.icon")); // NOI18N
        btnParticleColor.setText(ivResourceMap.getString("btnParticleColor.text")); // NOI18N
        btnParticleColor.setName("btnParticleColor"); // NOI18N
        btnParticleColor.addActionListener(this::btnParticleColorActionPerformed);

        lblColor1.setText(ivResourceMap.getString("lblColor1.text")); // NOI18N
        lblColor1.setName("lblColor1"); // NOI18N

        spinnerParticleSize.setModel(new javax.swing.SpinnerNumberModel(1, 1, 10, 1));
        spinnerParticleSize.setName("spinnerParticleSize"); // NOI18N
        spinnerParticleSize.addChangeListener(this::spinnerParticleSizeStateChanged);

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
                                        .addComponent(btnParticleColor)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(lblColor1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(spinnerParticleSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
        );
        pnlColorLayout.setVerticalGroup(
                pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlColorLayout.createSequentialGroup()
                        .addGroup(pnlColorLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblColor)
                                .addComponent(btnParticleColor)
                                .addComponent(lblColor1)
                                .addComponent(spinnerParticleSize, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(pnlColorBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
                        .addComponent(pnlColor, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
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

        taskPaneMapping.add(pnlMapping);

        taskPaneAnimation.setAnimated(false);
        taskPaneAnimation.setIcon(ivResourceMap.getIcon("step.Animation.icon")); // NOI18N
        taskPaneAnimation.setTitle(ivResourceMap.getString("step.Animation.text")); // NOI18N
        taskPaneAnimation.setName("taskPaneAnimation"); // NOI18N
        taskPaneAnimation.addPropertyChangeListener(this::taskPaneAnimationPropertyChange);

        pnlAnimation.setName("pnlAnimation"); // NOI18N
        pnlAnimation.setOpaque(false);

        lblFramePerSecond.setName("lblFramePerSecond"); // NOI18N
        lblFramePerSecond.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                lblFramePerSecondMouseClicked(evt);
            }
        });

        animationSpeed.setModel(new javax.swing.SpinnerNumberModel(Float.valueOf(1.5f), Float.valueOf(0.5f), Float.valueOf(24.0f), Float.valueOf(0.1f)));
        animationSpeed.setToolTipText(ivResourceMap.getString("animationSpeed.toolTipText")); // NOI18N
        animationSpeed.setFocusable(false);
        animationSpeed.setMaximumSize(new java.awt.Dimension(77, 30));
        animationSpeed.setName("animationSpeed"); // NOI18N
        animationSpeed.addChangeListener(this::animationSpeedStateChanged);

        btnDeleteMaps.setAction(actionMap.get("deleteMaps")); // NOI18N
        btnDeleteMaps.setFocusable(false);
        btnDeleteMaps.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnDeleteMaps.setName("btnDeleteMaps"); // NOI18N
        btnDeleteMaps.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        btnSaveAsMaps.setAction(actionMap.get("saveasMaps")); // NOI18N
        btnSaveAsMaps.setFocusable(false);
        btnSaveAsMaps.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);
        btnSaveAsMaps.setName("btnSaveAsMaps"); // NOI18N
        btnSaveAsMaps.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        lblAnimationSpeed.setText(ivResourceMap.getString("lblAnimationSpeed.text")); // NOI18N
        lblAnimationSpeed.setName("lblAnimationSpeed"); // NOI18N

        btnOpenAnimation.setAction(actionMap.get("openFolderAnimation")); // NOI18N
        btnOpenAnimation.setName("btnOpenAnimation"); // NOI18N
        btnOpenAnimation.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        lblFolder.setFont(ivResourceMap.getFont("lblFolder.font")); // NOI18N
        lblFolder.setText(ivResourceMap.getString("lblFolder.text")); // NOI18N
        lblFolder.setName("lblFolder"); // NOI18N

        sliderTime.setValue(0);
        sliderTime.setName("sliderTime"); // NOI18N
        sliderTime.addChangeListener(this::sliderTimeStateChanged);

        lblTime.setFont(ivResourceMap.getFont("lblTime.font")); // NOI18N
        lblTime.setText(ivResourceMap.getString("lblTime.text")); // NOI18N
        lblTime.setName("lblTime"); // NOI18N

        jToolBar1.setFloatable(false);
        jToolBar1.setRollover(true);
        jToolBar1.setName("jToolBar1"); // NOI18N

        btnFirst.setAction(actionMap.get("first")); // NOI18N
        btnFirst.setFocusable(false);
        btnFirst.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFirst.setName("btnFirst"); // NOI18N
        btnFirst.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnFirst);

        btnPrevious.setAction(actionMap.get("previous")); // NOI18N
        btnPrevious.setFocusable(false);
        btnPrevious.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPrevious.setName("btnPrevious"); // NOI18N
        btnPrevious.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnPrevious);

        btnAnimationBW.setAction(actionMap.get("startAnimationBW")); // NOI18N
        btnAnimationBW.setText(ivResourceMap.getString("btnAnimationBW.text")); // NOI18N
        btnAnimationBW.setFocusable(false);
        btnAnimationBW.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAnimationBW.setName("btnAnimationBW"); // NOI18N
        btnAnimationBW.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnAnimationBW);

        btnAnimationStop.setAction(actionMap.get("stopAnimation")); // NOI18N
        btnAnimationStop.setText(ivResourceMap.getString("btnAnimationStop.text")); // NOI18N
        btnAnimationStop.setFocusable(false);
        btnAnimationStop.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAnimationStop.setName("btnAnimationStop"); // NOI18N
        btnAnimationStop.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnAnimationStop);

        btnAnimationFW.setAction(actionMap.get("startAnimationFW")); // NOI18N
        btnAnimationFW.setText(ivResourceMap.getString("btnAnimationFW.text")); // NOI18N
        btnAnimationFW.setFocusable(false);
        btnAnimationFW.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAnimationFW.setName("btnAnimationFW"); // NOI18N
        btnAnimationFW.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnAnimationFW);

        btnNext.setAction(actionMap.get("next")); // NOI18N
        btnNext.setFocusable(false);
        btnNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNext.setName("btnNext"); // NOI18N
        btnNext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnNext);

        btnLast.setAction(actionMap.get("last")); // NOI18N
        btnLast.setFocusable(false);
        btnLast.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLast.setName("btnLast"); // NOI18N
        btnLast.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        jToolBar1.add(btnLast);

        btnAnimatedGif.setAction(actionMap.get("createAnimatedGif")); // NOI18N
        btnAnimatedGif.setName("btnAnimatedGif"); // NOI18N
        btnAnimatedGif.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);

        ckBoxReverseTime.setText(ivResourceMap.getString("ckBoxReverseTime.text")); // NOI18N
        ckBoxReverseTime.setName("ckBoxReverseTime"); // NOI18N

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
                                        .addComponent(btnOpenAnimation)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnDeleteMaps)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(btnSaveAsMaps))
                                .addComponent(lblFolder)
                                .addComponent(jToolBar1, javax.swing.GroupLayout.DEFAULT_SIZE, 420, Short.MAX_VALUE)
                                .addGroup(pnlAnimationLayout.createSequentialGroup()
                                        .addGap(379, 379, 379)
                                        .addComponent(lblFramePerSecond))
                                .addGroup(pnlAnimationLayout.createSequentialGroup()
                                        .addComponent(btnAnimatedGif)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(ckBoxReverseTime))
                                .addGroup(pnlAnimationLayout.createSequentialGroup()
                                        .addComponent(lblAnimationSpeed)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(animationSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap())
        );
        pnlAnimationLayout.setVerticalGroup(
                pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(pnlAnimationLayout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(lblTime)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(sliderTime, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(pnlAnimationLayout.createSequentialGroup()
                                        .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                .addComponent(btnOpenAnimation)
                                                .addComponent(btnDeleteMaps))
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(lblFolder))
                                .addComponent(btnSaveAsMaps))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                                .addComponent(lblFramePerSecond)
                                .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(btnAnimatedGif)
                                        .addComponent(ckBoxReverseTime)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(pnlAnimationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(lblAnimationSpeed)
                                .addComponent(animationSpeed, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(12, 12, 12))
        );

        taskPaneAnimation.add(pnlAnimation);

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

        javax.swing.GroupLayout titledPanelStepsLayout = new javax.swing.GroupLayout(titledPanelSteps);
        titledPanelSteps.setLayout(titledPanelStepsLayout);
        titledPanelStepsLayout.setHorizontalGroup(
                titledPanelStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(stepsScrollPane, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
        );
        titledPanelStepsLayout.setVerticalGroup(
                titledPanelStepsLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(stepsScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 228, Short.MAX_VALUE)
        );

        leftSplitPane.setLeftComponent(titledPanelSteps);

        titledPanelLogger.setTitle(ivResourceMap.getString("titledPanelLogger.title")); // NOI18N
        titledPanelLogger.setName("titledPanelLogger"); // NOI18N

        loggerScrollPane.setName("loggerScrollPane"); // NOI18N

        javax.swing.GroupLayout titledPanelLoggerLayout = new javax.swing.GroupLayout(titledPanelLogger);
        titledPanelLogger.setLayout(titledPanelLoggerLayout);
        titledPanelLoggerLayout.setHorizontalGroup(
                titledPanelLoggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(loggerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 486, Short.MAX_VALUE)
        );
        titledPanelLoggerLayout.setVerticalGroup(
                titledPanelLoggerLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(loggerScrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 186, Short.MAX_VALUE)
        );

        leftSplitPane.setRightComponent(titledPanelLogger);

        splitPane.setLeftComponent(leftSplitPane);

        titledPanelMain.setTitle(ivResourceMap.getString("titledPanelMain.title")); // NOI18N
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

        javax.swing.GroupLayout titledPanelMainLayout = new javax.swing.GroupLayout(titledPanelMain);
        titledPanelMain.setLayout(titledPanelMainLayout);
        titledPanelMainLayout.setHorizontalGroup(
                titledPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 369, Short.MAX_VALUE)
        );
        titledPanelMainLayout.setVerticalGroup(
                titledPanelMainLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 424, Short.MAX_VALUE)
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
                .addComponent(splitPane, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
        );

        menuBar.setName("menuBar"); // NOI18N

        configurationMenu.setText(ivResourceMap.getString("configurationMenu.text")); // NOI18N
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

        simulationMenu.setText(ivResourceMap.getString("simulationMenu.text")); // NOI18N
        simulationMenu.setName("simulationMenu"); // NOI18N

        simulationMenuItem.setAction(actionMap.get("simulationRun")); // NOI18N
        simulationMenuItem.setName("simulationMenuItem"); // NOI18N
        simulationMenu.add(simulationMenuItem);

        previewMenuItem.setAction(actionMap.get("previewSimulation")); // NOI18N
        previewMenuItem.setName("previewMenuItem"); // NOI18N
        simulationMenu.add(previewMenuItem);

        menuBar.add(simulationMenu);

        mappingMenu.setText(ivResourceMap.getString("mappingMenu.text")); // NOI18N
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

        animationMenu.setText(ivResourceMap.getString("animationMenu.text")); // NOI18N
        animationMenu.setName("animationMenu"); // NOI18N

        startFWMenuItem.setAction(actionMap.get("startAnimationFW")); // NOI18N
        startFWMenuItem.setName("startFWMenuItem"); // NOI18N
        animationMenu.add(startFWMenuItem);

        stopMenuItem.setAction(actionMap.get("stopAnimation")); // NOI18N
        stopMenuItem.setName("stopMenuItem"); // NOI18N
        animationMenu.add(stopMenuItem);

        startBWMenuItem.setAction(actionMap.get("startAnimationBW")); // NOI18N
        startBWMenuItem.setName("startBWMenuItem"); // NOI18N
        animationMenu.add(startBWMenuItem);

        jSeparator15.setName("jSeparator15"); // NOI18N
        animationMenu.add(jSeparator15);

        openAnimationMenuItem.setAction(actionMap.get("openFolderAnimation")); // NOI18N
        openAnimationMenuItem.setName("openAnimationMenuItem"); // NOI18N
        animationMenu.add(openAnimationMenuItem);

        jSeparator14.setName("jSeparator14"); // NOI18N
        animationMenu.add(jSeparator14);

        saveasMapsMenuItem.setAction(actionMap.get("saveasMaps")); // NOI18N
        saveasMapsMenuItem.setName("saveasMapsMenuItem"); // NOI18N
        animationMenu.add(saveasMapsMenuItem);

        deleteMenuItem.setAction(actionMap.get("deleteMaps")); // NOI18N
        deleteMenuItem.setName("deleteMenuItem"); // NOI18N
        animationMenu.add(deleteMenuItem);

        menuBar.add(animationMenu);

        helpMenu.setText(ivResourceMap.getString("helpMenu.text")); // NOI18N
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
        hyperLinkLogo.setRolloverIcon(ivResourceMap.getIcon("hyperLinkLogo.rolloverIcon")); // NOI18N
        hyperLinkLogo.setSelectedIcon(ivResourceMap.getIcon("hyperLinkLogo.selectedIcon")); // NOI18N
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
    }

    private void lblFramePerSecondMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getClickCount() > 1) {
            animationSpeed.setValue(1.5f);
        }
    }

    private void animationSpeedStateChanged(javax.swing.event.ChangeEvent evt) {
        JSpinner source = (JSpinner) evt.getSource();
        nbfps = (Float) source.getValue();
    }

    private void sliderTimeStateChanged(javax.swing.event.ChangeEvent evt) {
        SwingUtilities.invokeLater(() -> {
            replayPanel.setIndex(sliderTime.getValue());
            lblTime.setText(replayPanel.getTime());
        });
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
                pnlProgress.setVisible(true);
            } else {
                pnlProgress.setVisible(false);
                if (btnPreview.isSelected()) {
                    btnPreview.doClick();
                }
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
                wmsMapper.setVisible(null != wmsMapper.getFile());
                lblMapping.setVisible(true);
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
                replayPanel.setVisible(true);
            } else {
                stopAnimation();
                replayPanel.setVisible(false);
            }
            setMainTitle();
        }
    }

    private void btnColorMinActionPerformed(java.awt.event.ActionEvent evt) {
        final JButton btn = (JButton) evt.getSource();
        SwingUtilities.invokeLater(() -> {
            btn.setForeground(chooseColor(btn, btn.getForeground()));
        });
    }

    private void btnColorMaxActionPerformed(java.awt.event.ActionEvent evt) {
        final JButton btn = (JButton) evt.getSource();
        SwingUtilities.invokeLater(() -> {
            btn.setForeground(chooseColor(btn, btn.getForeground()));
        });
    }

    private void btnParticleColorActionPerformed(java.awt.event.ActionEvent evt) {
        final JButton btn = (JButton) evt.getSource();
        SwingUtilities.invokeLater(() -> {
            btn.setForeground(chooseColor(btn, btn.getForeground()));
            wmsMapper.setColorbar(null, 0, 0, 0, null, null, null);
            wmsMapper.setDefaultColor(btn.getForeground());
        });
        getLogger().info(resourceMap.getString("btnColor.msg.apply"));
    }

    private void btnColorMedActionPerformed(java.awt.event.ActionEvent evt) {
        final JButton btn = (JButton) evt.getSource();
        SwingUtilities.invokeLater(() -> {
            btn.setForeground(chooseColor(btn, btn.getForeground()));
        });
    }

    private void spinnerParticleSizeStateChanged(javax.swing.event.ChangeEvent evt) {
        JSpinner source = (JSpinner) evt.getSource();
        final int pixel = (Integer) source.getValue();
        SwingUtilities.invokeLater(() -> {
            wmsMapper.setParticlePixel(pixel);
        });
    }

    private void ckBoxDrawGridActionPerformed(java.awt.event.ActionEvent evt) {
        getSimulationUI().setGridVisible(ckBoxDrawGrid.isSelected());
    }
    // Variables declaration
    private javax.swing.JMenu animationMenu;
    private javax.swing.JSpinner animationSpeed;
    private javax.swing.JButton btnAnimatedGif;
    private javax.swing.JButton btnAnimationBW;
    private javax.swing.JButton btnAnimationFW;
    private javax.swing.JButton btnAnimationStop;
    private javax.swing.JButton btnApplyColorbar;
    private javax.swing.JButton btnAutoRange;
    private javax.swing.JButton btnCancelMapping;
    private javax.swing.JButton btnCloseCfgFile;
    private javax.swing.JButton btnCloseNC;
    private javax.swing.JButton btnColorMax;
    private javax.swing.JButton btnColorMed;
    private javax.swing.JButton btnColorMin;
    private javax.swing.JButton btnDeleteMaps;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnExportToKMZ;
    private javax.swing.JButton btnFirst;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnMapping;
    private javax.swing.JButton btnNewCfgFile;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnOpenAnimation;
    private javax.swing.JButton btnOpenCfgFile;
    private javax.swing.JButton btnOpenNC;
    private javax.swing.JButton btnParticleColor;
    private javax.swing.JToggleButton btnPreview;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnSaveAsCfgFile;
    private javax.swing.JButton btnSaveAsMaps;
    private javax.swing.JButton btnSaveCfgFile;
    private javax.swing.JButton btnSimulationRun;
    private javax.swing.JMenuItem cancelMapMenuItem;
    private javax.swing.JComboBox cbBoxVariable;
    private javax.swing.JComboBox cbBoxWMS;
    private javax.swing.JCheckBox ckBoxDrawGrid;
    private javax.swing.JCheckBox ckBoxReverseTime;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem deleteMenuItem;
    private javax.swing.JMenuItem exportToKMZMenuItem;
    private org.ichthyop.ui.GradientPanel gradientPanel;
    private org.jdesktop.swingx.JXHyperlink hyperLinkLogo;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator13;
    private javax.swing.JPopupMenu.Separator jSeparator14;
    private javax.swing.JPopupMenu.Separator jSeparator15;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel lblAnimationSpeed;
    private javax.swing.JLabel lblCfgFile;
    private javax.swing.JLabel lblColor;
    private javax.swing.JLabel lblColor1;
    private javax.swing.JLabel lblFolder;
    private javax.swing.JLabel lblFramePerSecond;
    private javax.swing.JLabel lblMax;
    private javax.swing.JLabel lblMed;
    private javax.swing.JLabel lblMin;
    private javax.swing.JLabel lblNC;
    private javax.swing.JLabel lblTime;
    private javax.swing.JLabel lblVariable;
    private javax.swing.JLabel lblWMS;
    private javax.swing.JSplitPane leftSplitPane;
    private org.ichthyop.ui.LoggerScrollPane loggerScrollPane;
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
    private javax.swing.JMenuItem saveasMapsMenuItem;
    private javax.swing.JScrollPane scrollPaneSimulationUI;
    private javax.swing.JMenu simulationMenu;
    private javax.swing.JMenuItem simulationMenuItem;
    private javax.swing.JSlider sliderTime;
    private javax.swing.JSpinner spinnerParticleSize;
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JMenuItem startBWMenuItem;
    private javax.swing.JMenuItem startFWMenuItem;
    private javax.swing.JPanel stepsPanel;
    private javax.swing.JScrollPane stepsScrollPane;
    private javax.swing.JMenuItem stopMenuItem;
    private org.jdesktop.swingx.JXTaskPane taskPaneAnimation;
    private org.jdesktop.swingx.JXTaskPane taskPaneConfiguration;
    private org.jdesktop.swingx.JXTaskPane taskPaneMapping;
    private org.jdesktop.swingx.JXTaskPane taskPaneSimulation;
    private org.jdesktop.swingx.JXTitledPanel titledPanelLogger;
    private org.jdesktop.swingx.JXTitledPanel titledPanelMain;
    private org.jdesktop.swingx.JXTitledPanel titledPanelSteps;
    private javax.swing.JFormattedTextField txtFieldMax;
    private javax.swing.JFormattedTextField txtFieldMed;
    private javax.swing.JFormattedTextField txtFieldMin;
    private JDialog aboutBox;
    private File cfgPath = new File(System.getProperty("user.dir"));
    private boolean isRunning = false;
    private Task simulActionTask;
    private Task createMapTask;
    private Task kmzTask;
    private boolean initDone;
    private final ReplayPanel replayPanel = new ReplayPanel();
    private final float TEN_MINUTES = 10.f * 60.f;
    private final Animator animator = new Animator((int) (TEN_MINUTES * 1000), this);
    private float nbfps = 1.f;
    private float time;
    private Timer progressTimer;
    private final WMSMapper wmsMapper = new WMSMapper();
    private File outputFile, outputFolder;
    private JLabel lblConfiguration;
    private JLabel lblMapping;
    private final JConfigurationPanel pnlConfiguration = new JConfigurationPanel();
    private final JStatusBar statusBar = new JStatusBar();
    private final JRunProgressPanel pnlProgress = new JRunProgressPanel();
    private final ResourceMap resourceMap = Application.getInstance(org.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
    private TimeDirection animationDirection;
    private final MouseWheelScroller mouseScroller = new MouseWheelScroller();
    private boolean cfgUntitled = true;
    private boolean animationLoaded = false;

    private enum TimeDirection {

        FORWARD, BACKWARD;
    }
}
