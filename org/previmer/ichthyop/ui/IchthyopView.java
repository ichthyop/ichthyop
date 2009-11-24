/*
 * IchthyopView.java
 */
package org.previmer.ichthyop.ui;

import java.awt.Component;
import java.awt.Graphics;
import java.lang.reflect.InvocationTargetException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import javax.imageio.ImageIO;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.previmer.ichthyop.arch.ISimulationManager;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.event.NextStepListener;
import org.previmer.ichthyop.io.IOTools;
import org.previmer.ichthyop.manager.SimulationManager;
import javax.swing.JSpinner;

/**
 * The application's main frame.
 */
public class IchthyopView extends FrameView implements NextStepListener {

    public IchthyopView(SingleFrameApplication app) {
        super(app);

        createLogfile();

        initComponents();
        getFrame().setIconImage(getResourceMap().getImageIcon("Application.icon").getImage());

        getSimulationManager().getTimeManager().addNextStepListener(this);

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

        newMenuItem.getAction().setEnabled(false);
        editMenuItem.getAction().setEnabled(false);
        btnSimulationRun.getAction().setEnabled(false);
        btnSimulationProgress.getAction().setEnabled(false);
        btnSimulationReplay.getAction().setEnabled(false);
        btnSimulationRecord.getAction().setEnabled(false);
        setSimulationReplayBarVisible(false);
        setSimulationRecordBarVisible(false);

        setMessage("Please, open a configuration file or create a new one");
    }

    public ISimulationManager getSimulationManager() {
        return SimulationManager.getInstance();
    }

    private void setSimulationReplayBarVisible(boolean visible) {
        simulationReplayToolBar.setVisible(visible);
        /*btnFirst.setVisible(visible);
        btnPrevious.setVisible(visible);
        btnAnimaction.setVisible(visible);
        btnNext.setVisible(visible);
        btnLast.setVisible(visible);*/
    }

    private void setSimulationRecordBarVisible(boolean visible) {
        simulationRecordToolBar.setVisible(visible);
    }

    private void createLogfile() {
        try {
            String logPath = System.getProperty("user.dir") + File.separator + "log" + File.separator;
            StringBuffer logfile = new StringBuffer(logPath);
            logfile.append(getRunId());
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

    private String getRunId() {

        if (null == runId) {
            StringBuffer strBfRunId = new StringBuffer(getResourceMap().getString("Application.name"));
            strBfRunId.append("-run");
            Calendar calendar = new GregorianCalendar();
            calendar.setTimeInMillis(System.currentTimeMillis());
            SimpleDateFormat dtformatter = new SimpleDateFormat("yyyyMMddHHmm");
            dtformatter.setCalendar(calendar);
            strBfRunId.append(dtformatter.format(calendar.getTime()));
            runId = strBfRunId.toString();
        }
        return runId;
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

    @Action
    public Task simulationReplay() {
        boolean isEnabled = btnSimulationReplay.isSelected();
        if (btnSimulationRecord.isSelected()) {
            btnSimulationRecord.doClick();
        }
        setSimulationReplayBarVisible(isEnabled);
        pnlSimulation.setVisible(isEnabled);
        if (isEnabled) {
            pnlSimulation.remove(viewerPanel);
            pnlSimulation.add(viewerPanel = new ViewerPanel(), StackLayout.TOP);
        }
        getFrame().pack();
        return null;
    }

    @Action
    public Task simulationRecord() {
        if (btnSimulationReplay.isSelected()) {
            btnSimulationReplay.doClick();
        }
        return new SimulationRecordTask(getApplication(), btnSimulationRecord.isSelected());
    }

    private class SimulationRecordTask extends Task {

        boolean isEnabled;

        SimulationRecordTask(Application instance, boolean isEnabled) {
            super(instance);
            this.isEnabled = isEnabled;
            setSimulationRecordBarVisible(isEnabled);
        }

        @Override
        protected Object doInBackground() throws Exception {
            if (!isSetup) {
                setMessage("Setting up...");
                getSimulationManager().setup();
                isSetup = true;
                setMessage("Setup [OK]");
            }
            getSimulationUI().init();
            return null;
        }

        @Override
        protected void succeeded(Object obj) {
            pnlSimulation.setVisible(isEnabled);
            if (isEnabled) {
                pnlSimulation.remove(scrollPaneSimulationUI);
                pnlSimulation.add(scrollPaneSimulationUI, StackLayout.TOP);
                getSimulationUI().repaintBackground();
            }
            getFrame().pack();
        }
    }

    @Action
    public void openConfigurationFile() {
        JFileChooser chooser = new JFileChooser(cfgPath);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileFilter(new FileNameExtensionFilter("Ichthyop configuration file" + " (*.xic)", "xic"));
        int returnPath = chooser.showOpenDialog(getFrame());
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            setMessage("Opened " + chooser.getSelectedFile().toString());
            logger.info("Opened " + chooser.getSelectedFile().toString());
            getSimulationManager().setConfigurationFile(chooser.getSelectedFile());
            isSetup = false;
            btnSimulationRun.getAction().setEnabled(true);
            btnSimulationProgress.getAction().setEnabled(true);
            if (getSimulationManager().getNumberOfSimulations() > 1) {
                btnSimulationProgress.doClick();
            } else {
                btnSimulationReplay.getAction().setEnabled(true);
                btnSimulationRecord.getAction().setEnabled(true);
            }
        }
    }

    @Action
    public void first() {
    }

    @Action
    public void previous() {
    }

    @Action
    public void animAction() {
    }

    @Action
    public void next() {
    }

    @Action
    public void last() {
    }

    public void nextStepTriggered(NextStepEvent e) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    if (!btnSimulationRun.isEnabled()) {
                        btnSimulationRun.getAction().setEnabled(true);
                        btnSimulationProgress.getAction().setEnabled(true);
                    }
                    setProgress();
                    if (btnSimulationRecord.isVisible()) {
                        int dt_refresh = ((Integer) refreshFrequency.getValue()) * getSimulationManager().getTimeManager().get_dt();
                        if (((getSimulationManager().getTimeManager().getTime() - getSimulationManager().getTimeManager().get_tO()) % dt_refresh) == 0) {
                            scrollPaneSimulationUI.repaint();
                            if (btnSimulationReplay.isSelected()) {
                                screen2File(pnlSimulationUI, getSimulationManager().getTimeManager().getCalendar());
                            }
                        }
                    }
                }
            });
        } catch (InterruptedException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Saves the snapshot of the specified component as a PNG picture.
     * The name of the picture includes the current time of the simulation.
     * @param cpnt the Component to save as a PNG picture.
     * @param cld the Calendar of the current {@code Step} object.
     * @throws an IOException if an ouput exception occurs when saving the
     * picture.
     */
    private void screen2File(Component component, Calendar calendar) {

        SimpleDateFormat dtFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm");
        dtFormat.setCalendar(calendar);
        StringBuffer fileName = new StringBuffer(System.getProperty("user.dir"));
        fileName.append(File.separator);
        fileName.append("img");
        fileName.append(File.separator);
        fileName.append(getRunId());
        if (getSimulationManager().getNumberOfSimulations() > 1) {
            fileName.append("_s");
            fileName.append(getSimulationManager().getIndexSimulation() + 1);
        }
        fileName.append('_');
        fileName.append(dtFormat.format(calendar.getTime()));
        fileName.append(".png");

        BufferedImage bi = new BufferedImage(component.getWidth(),
                component.getHeight(),
                BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.getGraphics();
        component.paintAll(g);
        try {
            ImageIO.write(bi, "PNG", new File(fileName.toString()));
        } catch (IOException ex) {
            Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private SimulationUI getSimulationUI() {
        return (SimulationUI) pnlSimulationUI;
    }

    private void setProgress() {

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

    @Action
    public void exitApplication() {
        if (pnlProgress.isVisible()) {
            btnSimulationProgress.doClick();
        }
        if (btnSimulationReplay.isSelected()) {
            btnSimulationReplay.doClick();
        }
        getContext().getActionMap().get("quit").actionPerformed(new ActionEvent(btnExit, 0, null));
    }

    @Action
    public void newConfigurationFile() {
    }

    @Action
    public void editConfigurationFile() {
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
        editMenuItem.getAction().setEnabled(enabled);
    }

    @Action
    public void simulationProgress() {
        if (getSimulationManager().getNumberOfSimulations() > 1) {
            lblProgressGlobal.setVisible(true);
            progressBarGlobal.setVisible(true);
            lblTimeLeftGlobal.setVisible(true);
        } else {
            lblProgressGlobal.setVisible(false);
            progressBarGlobal.setVisible(false);
            lblTimeLeftGlobal.setVisible(false);
        }
        pnlProgress.setVisible(!pnlProgress.isVisible());
        getFrame().pack();
    }

    public class SimulationRunTask extends Task {

        ResourceMap resourceMap = Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);

        SimulationRunTask(Application instance) {
            super(instance);
            setMessage("Simulation started");
            setMenuEnabled(false);
            btnSimulationRun.setIcon(resourceMap.getIcon("simulationRun.Action.icon.stop"));
            isRunning = true;
            btnSimulationRun.getAction().setEnabled(false);
            btnSimulationRecord.getAction().setEnabled(false);
            if (btnSimulationReplay.isSelected()) {
                btnSimulationReplay.doClick();
            }
            btnSimulationReplay.getAction().setEnabled(false);
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
                    setMessage(getSimulationManager().getTimeManager().stepToString() + " - Time " + getSimulationManager().getTimeManager().timeToString());
                } while (!getSimulationManager().isStopped() && getSimulationManager().getTimeManager().hasNextStep());
            } while (!getSimulationManager().isStopped() && getSimulationManager().hasNextSimulation());
            return null;
        }

        @Override
        protected void cancelled() {
            getSimulationManager().stop();
            setMessage("Simulation interrupted");
        }

        @Override
        protected void succeeded(Object obj) {
            setMessage("End of simulation");
        }

        @Override
        protected void finished() {
            //setMenuEnabled(true);
            btnSimulationRun.setIcon(resourceMap.getIcon("simulationRun.Action.icon.play"));
            openMenuItem.getAction().setEnabled(true);
            btnSimulationRecord.getAction().setEnabled(true);
            isRunning = false;
            if (btnSimulationRecord.isSelected()) {
                btnSimulationRecord.doClick();
                btnSimulationReplay.doClick();
            }
        }
    }

    public void savePreferences() {
        savePreference(openMenuItem, cfgPath.getPath());
        savePreference(lafMenu, UIManager.getLookAndFeel().getClass().getName());
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
            cfgPath = new File((String) property);
        }

        property = restorePreference(lafMenu);
        disableUnsupportedLaF();
        if (property != null && isSupportedLookAndFeel((String) property)) {
            getMapLaF().get((String) property).doClick();
        } else {
            if (isSupportedLookAndFeel(getResourceMap().getString("metalLaF.classpath"))) {
                metalMenuItem.doClick();
            }
        }
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
    }

    private void updateLookAndFeel(String laf) {
        try {
            UIManager.setLookAndFeel(laf);
            SwingUtilities.updateComponentTreeUI(getFrame());
            SwingUtilities.updateComponentTreeUI(getMenuBar());
            SwingUtilities.updateComponentTreeUI(getStatusBar());
            SwingUtilities.updateComponentTreeUI(getToolBar());
        } catch (ClassNotFoundException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (UnsupportedLookAndFeelException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    @Action
    public void gtkLaF() {
        updateLookAndFeel(getResourceMap().getString("gtkLaF.classpath"));
    }

    @Action
    public void nimbusLaF() {
        updateLookAndFeel(getResourceMap().getString("nimbusLaF.classpath"));
    }

    @Action
    public void metalLaF() {
        updateLookAndFeel(getResourceMap().getString("metalLaF.classpath"));
    }

    @Action
    public void motifLaF() {
        updateLookAndFeel(getResourceMap().getString("motifLaF.classpath"));
    }

    @Action
    public void windowsLaF() {
        updateLookAndFeel(getResourceMap().getString("windowsLaF.classpath"));
    }

    @Action
    public void macLaF() {
        updateLookAndFeel(getResourceMap().getString("macLaF.classpath"));
    }

    private HashMap<String, JMenuItem> getMapLaF() {

        HashMap<String, JMenuItem> map = new HashMap(lafMenu.getMenuComponentCount());
        map.put(getResourceMap().getString("nimbusLaF.classpath"), nimbusMenuItem);
        map.put(getResourceMap().getString("gtkLaF.classpath"), gtkMenuItem);
        map.put(getResourceMap().getString("metalLaF.classpath"), metalMenuItem);
        map.put(getResourceMap().getString("motifLaF.classpath"), motifMenuItem);
        map.put(getResourceMap().getString("windowsLaF.classpath"), windowsMenuItem);
        map.put(getResourceMap().getString("macLaF.classpath"), macMenuItem);
        return map;
    }

    private void disableUnsupportedLaF() {
        nimbusMenuItem.getAction().setEnabled(isSupportedLookAndFeel(getResourceMap().getString("nimbusLaF.classpath")));
        metalMenuItem.getAction().setEnabled(isSupportedLookAndFeel(getResourceMap().getString("metalLaF.classpath")));
        motifMenuItem.getAction().setEnabled(isSupportedLookAndFeel(getResourceMap().getString("motifLaF.classpath")));
        windowsMenuItem.getAction().setEnabled(isSupportedLookAndFeel(getResourceMap().getString("windowsLaF.classpath")));
        macMenuItem.getAction().setEnabled(isSupportedLookAndFeel(getResourceMap().getString("macLaF.classpath")));
    }

    protected boolean isSupportedLookAndFeel(String laf) {
        try {
            Class lnfClass = Class.forName(laf);
            if (lnfClass != null) {
                LookAndFeel newLAF = (LookAndFeel) (lnfClass.newInstance());
                if (newLAF != null) {
                    return newLAF.isSupportedLookAndFeel();
                }
            }
        } catch (Throwable t) {
        }
        return false;
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
        pnlProgress = new javax.swing.JPanel();
        lblProgressCurrent = new javax.swing.JLabel();
        progressBarCurrent = new javax.swing.JProgressBar();
        lblTimeLeftCurrent = new javax.swing.JLabel();
        lblProgressGlobal = new javax.swing.JLabel();
        progressBarGlobal = new javax.swing.JProgressBar();
        lblTimeLeftGlobal = new javax.swing.JLabel();
        toolBar = new javax.swing.JToolBar();
        btnOpenCfgFile = new javax.swing.JButton();
        btnNewCfgFile = new javax.swing.JButton();
        btnEditCfgFile = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnSimulationRun = new javax.swing.JButton();
        btnSimulationRecord = new javax.swing.JToggleButton();
        btnSimulationProgress = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnSimulationReplay = new javax.swing.JToggleButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        btnExit = new javax.swing.JButton();
        pnlSimulation = new javax.swing.JPanel();
        simulationRecordToolBar = new javax.swing.JToolBar();
        lblSimulationRecord = new javax.swing.JLabel();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        lblRefreshFrequency = new javax.swing.JLabel();
        refreshFrequency = new javax.swing.JSpinner();
        lblSteps = new javax.swing.JLabel();
        simulationReplayToolBar = new javax.swing.JToolBar();
        lblSimulationReplay = new javax.swing.JLabel();
        jSeparator6 = new javax.swing.JToolBar.Separator();
        btnFirst = new javax.swing.JButton();
        btnPrevious = new javax.swing.JButton();
        btnAnimaction = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnLast = new javax.swing.JButton();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        editMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        lafMenu = new javax.swing.JMenu();
        nimbusMenuItem = new javax.swing.JRadioButtonMenuItem();
        gtkMenuItem = new javax.swing.JRadioButtonMenuItem();
        metalMenuItem = new javax.swing.JRadioButtonMenuItem();
        motifMenuItem = new javax.swing.JRadioButtonMenuItem();
        macMenuItem = new javax.swing.JRadioButtonMenuItem();
        windowsMenuItem = new javax.swing.JRadioButtonMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();
        btnGroupLaf = new javax.swing.ButtonGroup();
        scrollPaneSimulationUI = new javax.swing.JScrollPane();
        pnlSimulationUI = new SimulationUI();

        mainPanel.setName("mainPanel"); // NOI18N

        pnlProgress.setName("pnlProgress"); // NOI18N
        pnlProgress.setVisible(false);

        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
        lblProgressCurrent.setText(resourceMap.getString("lblProgressCurrent.text")); // NOI18N
        lblProgressCurrent.setName("lblProgressCurrent"); // NOI18N

        progressBarCurrent.setName("progressBarCurrent"); // NOI18N
        progressBarCurrent.setStringPainted(true);

        lblTimeLeftCurrent.setText(resourceMap.getString("lblTimeLeftCurrent.text")); // NOI18N
        lblTimeLeftCurrent.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lblTimeLeftCurrent.setName("lblTimeLeftCurrent"); // NOI18N

        lblProgressGlobal.setText(resourceMap.getString("lblProgressGlobal.text")); // NOI18N
        lblProgressGlobal.setName("lblProgressGlobal"); // NOI18N

        progressBarGlobal.setName("progressBarGlobal"); // NOI18N
        progressBarGlobal.setStringPainted(true);

        lblTimeLeftGlobal.setText(resourceMap.getString("lblTimeLeftGlobal.text")); // NOI18N
        lblTimeLeftGlobal.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);
        lblTimeLeftGlobal.setName("lblTimeLeftGlobal"); // NOI18N

        javax.swing.GroupLayout pnlProgressLayout = new javax.swing.GroupLayout(pnlProgress);
        pnlProgress.setLayout(pnlProgressLayout);
        pnlProgressLayout.setHorizontalGroup(
            pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProgressLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(lblProgressCurrent)
                    .addComponent(lblProgressGlobal))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(progressBarGlobal, javax.swing.GroupLayout.DEFAULT_SIZE, 677, Short.MAX_VALUE)
                    .addComponent(progressBarCurrent, javax.swing.GroupLayout.DEFAULT_SIZE, 677, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(lblTimeLeftCurrent)
                    .addComponent(lblTimeLeftGlobal))
                .addContainerGap())
        );
        pnlProgressLayout.setVerticalGroup(
            pnlProgressLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pnlProgressLayout.createSequentialGroup()
                .addContainerGap()
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
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        toolBar.setFloatable(false);
        toolBar.setName("toolBar"); // NOI18N
        toolBar.setPreferredSize(new java.awt.Dimension(400, 62));

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getActionMap(IchthyopView.class, this);
        btnOpenCfgFile.setAction(actionMap.get("openConfigurationFile")); // NOI18N
        btnOpenCfgFile.setIcon(resourceMap.getIcon("openConfigurationFile.Action.toolBarIcon")); // NOI18N
        btnOpenCfgFile.setText(resourceMap.getString("btnOpenCfgFile.text")); // NOI18N
        btnOpenCfgFile.setFocusable(false);
        btnOpenCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpenCfgFile.setName("btnOpenCfgFile"); // NOI18N
        btnOpenCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnOpenCfgFile);

        btnNewCfgFile.setAction(actionMap.get("newConfigurationFile")); // NOI18N
        btnNewCfgFile.setIcon(resourceMap.getIcon("newConfigurationFile.Action.toolBarIcon")); // NOI18N
        btnNewCfgFile.setText(resourceMap.getString("btnNewCfgFile.text")); // NOI18N
        btnNewCfgFile.setFocusable(false);
        btnNewCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNewCfgFile.setName("btnNewCfgFile"); // NOI18N
        btnNewCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnNewCfgFile);

        btnEditCfgFile.setAction(actionMap.get("editConfigurationFile")); // NOI18N
        btnEditCfgFile.setIcon(resourceMap.getIcon("editConfigurationFile.Action.toolBarIcon")); // NOI18N
        btnEditCfgFile.setText(resourceMap.getString("btnEditCfgFile.text")); // NOI18N
        btnEditCfgFile.setFocusable(false);
        btnEditCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEditCfgFile.setName("btnEditCfgFile"); // NOI18N
        btnEditCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnEditCfgFile);

        jSeparator2.setName("jSeparator2"); // NOI18N
        toolBar.add(jSeparator2);

        btnSimulationRun.setAction(actionMap.get("simulationRun")); // NOI18N
        btnSimulationRun.setFocusable(false);
        btnSimulationRun.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSimulationRun.setName("btnSimulationRun"); // NOI18N
        btnSimulationRun.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnSimulationRun);

        btnSimulationRecord.setAction(actionMap.get("simulationRecord")); // NOI18N
        btnSimulationRecord.setFocusable(false);
        btnSimulationRecord.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSimulationRecord.setName("btnSimulationRecord"); // NOI18N
        btnSimulationRecord.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnSimulationRecord);

        btnSimulationProgress.setAction(actionMap.get("simulationProgress")); // NOI18N
        btnSimulationProgress.setFocusable(false);
        btnSimulationProgress.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSimulationProgress.setName("btnSimulationProgress"); // NOI18N
        btnSimulationProgress.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnSimulationProgress);

        jSeparator3.setName("jSeparator3"); // NOI18N
        toolBar.add(jSeparator3);

        btnSimulationReplay.setAction(actionMap.get("simulationReplay")); // NOI18N
        btnSimulationReplay.setFocusable(false);
        btnSimulationReplay.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSimulationReplay.setName("btnSimulationReplay"); // NOI18N
        btnSimulationReplay.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnSimulationReplay);

        jSeparator5.setName("jSeparator5"); // NOI18N
        toolBar.add(jSeparator5);

        btnExit.setAction(actionMap.get("exitApplication")); // NOI18N
        btnExit.setIcon(resourceMap.getIcon("exitApplication.Action.toolBarIcon")); // NOI18N
        btnExit.setText(resourceMap.getString("btnExit.text")); // NOI18N
        btnExit.setFocusable(false);
        btnExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExit.setName("btnExit"); // NOI18N
        btnExit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnExit);

        pnlSimulation.setBorder(null);
        pnlSimulation.setName("pnlSimulation"); // NOI18N
        pnlSimulation.setVisible(false);

        javax.swing.GroupLayout pnlSimulationLayout = new javax.swing.GroupLayout(pnlSimulation);
        pnlSimulation.setLayout(pnlSimulationLayout);
        pnlSimulationLayout.setHorizontalGroup(
            pnlSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 980, Short.MAX_VALUE)
        );
        pnlSimulationLayout.setVerticalGroup(
            pnlSimulationLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 273, Short.MAX_VALUE)
        );

        simulationRecordToolBar.setFloatable(false);
        simulationRecordToolBar.setRollover(true);
        simulationRecordToolBar.setName("simulationRecordToolBar"); // NOI18N

        lblSimulationRecord.setIcon(resourceMap.getIcon("lblSimulationRecord.icon")); // NOI18N
        lblSimulationRecord.setText(resourceMap.getString("lblSimulationRecord.text")); // NOI18N
        lblSimulationRecord.setEnabled(false);
        lblSimulationRecord.setName("lblSimulationRecord"); // NOI18N
        simulationRecordToolBar.add(lblSimulationRecord);

        jSeparator4.setName("jSeparator4"); // NOI18N
        simulationRecordToolBar.add(jSeparator4);

        lblRefreshFrequency.setLabelFor(refreshFrequency);
        lblRefreshFrequency.setText(resourceMap.getString("lblRefreshFrequency.text")); // NOI18N
        lblRefreshFrequency.setToolTipText(resourceMap.getString("lblRefreshFrequency.toolTipText")); // NOI18N
        lblRefreshFrequency.setName("lblRefreshFrequency"); // NOI18N
        simulationRecordToolBar.add(lblRefreshFrequency);

        refreshFrequency.setModel(new javax.swing.SpinnerNumberModel(Integer.valueOf(4), Integer.valueOf(1), null, Integer.valueOf(1)));
        refreshFrequency.setFocusable(false);
        refreshFrequency.setMaximumSize(new java.awt.Dimension(77, 30));
        refreshFrequency.setName("refreshFrequency"); // NOI18N
        refreshFrequency.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                refreshFrequencyStateChanged(evt);
            }
        });
        simulationRecordToolBar.add(refreshFrequency);

        lblSteps.setText(resourceMap.getString("lblSteps.text")); // NOI18N
        lblSteps.setName("lblSteps"); // NOI18N
        simulationRecordToolBar.add(lblSteps);

        simulationReplayToolBar.setFloatable(false);
        simulationReplayToolBar.setRollover(true);
        simulationReplayToolBar.setName("simulationReplayToolBar"); // NOI18N

        lblSimulationReplay.setIcon(resourceMap.getIcon("lblSimulationReplay.icon")); // NOI18N
        lblSimulationReplay.setText(resourceMap.getString("lblSimulationReplay.text")); // NOI18N
        lblSimulationReplay.setEnabled(false);
        lblSimulationReplay.setName("lblSimulationReplay"); // NOI18N
        simulationReplayToolBar.add(lblSimulationReplay);

        jSeparator6.setName("jSeparator6"); // NOI18N
        simulationReplayToolBar.add(jSeparator6);

        btnFirst.setAction(actionMap.get("first")); // NOI18N
        btnFirst.setFocusable(false);
        btnFirst.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFirst.setName("btnFirst"); // NOI18N
        btnFirst.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        simulationReplayToolBar.add(btnFirst);

        btnPrevious.setAction(actionMap.get("previous")); // NOI18N
        btnPrevious.setFocusable(false);
        btnPrevious.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPrevious.setName("btnPrevious"); // NOI18N
        btnPrevious.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        simulationReplayToolBar.add(btnPrevious);

        btnAnimaction.setAction(actionMap.get("animAction")); // NOI18N
        btnAnimaction.setFocusable(false);
        btnAnimaction.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAnimaction.setName("btnAnimaction"); // NOI18N
        btnAnimaction.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        simulationReplayToolBar.add(btnAnimaction);

        btnNext.setAction(actionMap.get("next")); // NOI18N
        btnNext.setFocusable(false);
        btnNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNext.setName("btnNext"); // NOI18N
        btnNext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        simulationReplayToolBar.add(btnNext);

        btnLast.setAction(actionMap.get("last")); // NOI18N
        btnLast.setFocusable(false);
        btnLast.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLast.setName("btnLast"); // NOI18N
        btnLast.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        simulationReplayToolBar.add(btnLast);

        pnlSimulation.setLayout(new StackLayout());
        pnlSimulation.add(new GradientPanel(), StackLayout.TOP);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 980, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, mainPanelLayout.createSequentialGroup()
                .addComponent(simulationRecordToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 452, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(simulationReplayToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 522, Short.MAX_VALUE))
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(pnlProgress, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
            .addComponent(pnlSimulation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(simulationRecordToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(simulationReplayToolBar, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlProgress, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pnlSimulation, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        newMenuItem.setAction(actionMap.get("newConfigurationFile")); // NOI18N
        newMenuItem.setName("newMenuItem"); // NOI18N
        fileMenu.add(newMenuItem);

        openMenuItem.setAction(actionMap.get("openConfigurationFile")); // NOI18N
        openMenuItem.setName("openMenuItem"); // NOI18N
        fileMenu.add(openMenuItem);

        editMenuItem.setAction(actionMap.get("editConfigurationFile")); // NOI18N
        editMenuItem.setName("editMenuItem"); // NOI18N
        fileMenu.add(editMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        exitMenuItem.setAction(actionMap.get("exitApplication")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

        lafMenu.setText(resourceMap.getString("lafMenu.text")); // NOI18N
        lafMenu.setToolTipText(resourceMap.getString("lafMenu.toolTipText")); // NOI18N
        lafMenu.setName("lafMenu"); // NOI18N

        nimbusMenuItem.setAction(actionMap.get("nimbusLaF")); // NOI18N
        btnGroupLaf.add(nimbusMenuItem);
        nimbusMenuItem.setSelected(true);
        nimbusMenuItem.setName("nimbusMenuItem"); // NOI18N
        lafMenu.add(nimbusMenuItem);

        gtkMenuItem.setAction(actionMap.get("gtkLaF")); // NOI18N
        btnGroupLaf.add(gtkMenuItem);
        gtkMenuItem.setName("gtkMenuItem"); // NOI18N
        lafMenu.add(gtkMenuItem);

        metalMenuItem.setAction(actionMap.get("metalLaF")); // NOI18N
        btnGroupLaf.add(metalMenuItem);
        metalMenuItem.setName("metalMenuItem"); // NOI18N
        lafMenu.add(metalMenuItem);

        motifMenuItem.setAction(actionMap.get("motifLaF")); // NOI18N
        btnGroupLaf.add(motifMenuItem);
        motifMenuItem.setName("motifMenuItem"); // NOI18N
        lafMenu.add(motifMenuItem);

        macMenuItem.setAction(actionMap.get("macLaF")); // NOI18N
        btnGroupLaf.add(macMenuItem);
        macMenuItem.setName("macMenuItem"); // NOI18N
        lafMenu.add(macMenuItem);

        windowsMenuItem.setAction(actionMap.get("windowsLaF")); // NOI18N
        btnGroupLaf.add(windowsMenuItem);
        windowsMenuItem.setName("windowsMenuItem"); // NOI18N
        lafMenu.add(windowsMenuItem);

        menuBar.add(lafMenu);

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

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 980, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 794, Short.MAX_VALUE)
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

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
        setToolBar(toolBar);
    }// </editor-fold>//GEN-END:initComponents

    private void refreshFrequencyStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_refreshFrequencyStateChanged
        // TODO add your handling code here:
        JSpinner source = (JSpinner) evt.getSource();
}//GEN-LAST:event_refreshFrequencyStateChanged
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAnimaction;
    private javax.swing.JButton btnEditCfgFile;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnFirst;
    private javax.swing.ButtonGroup btnGroupLaf;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnNewCfgFile;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnOpenCfgFile;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JToggleButton btnSimulationProgress;
    private javax.swing.JToggleButton btnSimulationRecord;
    private javax.swing.JToggleButton btnSimulationReplay;
    private javax.swing.JButton btnSimulationRun;
    private javax.swing.JMenuItem editMenuItem;
    private javax.swing.JRadioButtonMenuItem gtkMenuItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JToolBar.Separator jSeparator6;
    private javax.swing.JMenu lafMenu;
    private javax.swing.JLabel lblProgressCurrent;
    private javax.swing.JLabel lblProgressGlobal;
    private javax.swing.JLabel lblRefreshFrequency;
    private javax.swing.JLabel lblSimulationRecord;
    private javax.swing.JLabel lblSimulationReplay;
    private javax.swing.JLabel lblSteps;
    private javax.swing.JLabel lblTimeLeftCurrent;
    private javax.swing.JLabel lblTimeLeftGlobal;
    private javax.swing.JRadioButtonMenuItem macMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JRadioButtonMenuItem metalMenuItem;
    private javax.swing.JRadioButtonMenuItem motifMenuItem;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JRadioButtonMenuItem nimbusMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JPanel pnlProgress;
    private javax.swing.JPanel pnlSimulation;
    private javax.swing.JPanel pnlSimulationUI;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JProgressBar progressBarCurrent;
    private javax.swing.JProgressBar progressBarGlobal;
    private javax.swing.JSpinner refreshFrequency;
    private javax.swing.JScrollPane scrollPaneSimulationUI;
    private javax.swing.JToolBar simulationRecordToolBar;
    private javax.swing.JToolBar simulationReplayToolBar;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JRadioButtonMenuItem windowsMenuItem;
    // End of variables declaration//GEN-END:variables
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
    private String runId;
    private boolean isSetup;
    private JPanel viewerPanel = new JPanel();
}
