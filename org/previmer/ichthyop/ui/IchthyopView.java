/*
 * IchthyopView.java
 */
package org.previmer.ichthyop.ui;

import java.awt.Component;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.jdesktop.application.Application;
import org.jdesktop.application.Task;
import org.previmer.ichthyop.arch.ISimulationManager;
import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.InitializeListener;
import org.previmer.ichthyop.event.NextStepEvent;
import org.previmer.ichthyop.event.NextStepListener;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.event.SetupListener;
import org.previmer.ichthyop.manager.SimulationManager;

/**
 * The application's main frame.
 */
public class IchthyopView extends FrameView implements SetupListener, InitializeListener, NextStepListener {

    public IchthyopView(SingleFrameApplication app) {
        super(app);

        initComponents();
        getSimulationManager().addSetupListener(this);
        getSimulationManager().addInitializeListener(this);
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
        btnSimulaction.getAction().setEnabled(false);
        btnPrevious.getAction().setEnabled(false);
        btnFirst.getAction().setEnabled(false);
        btnLast.getAction().setEnabled(false);
        btnNext.getAction().setEnabled(false);

        setMessage("Please, open a configuration file or create a new one");

    }

    public ISimulationManager getSimulationManager() {
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

    @Action
    public void openCfgFile() {
        JFileChooser chooser = new JFileChooser(cfgPath);
        chooser.setDialogType(JFileChooser.OPEN_DIALOG);
        chooser.setFileFilter(new FileNameExtensionFilter("Ichthyop configuration file" + " (*.xic)", "xic"));
        int returnPath = chooser.showOpenDialog(getFrame());
        if (returnPath == JFileChooser.APPROVE_OPTION) {
            getSimulationManager().setConfigurationFile(chooser.getSelectedFile());
            cfgPath = new File(chooser.getSelectedFile().getParent());
            setMessage("Open " + chooser.getSelectedFile().toString());
            btnSimulaction.getAction().setEnabled(true);
        }

    }

    public void setupPerformed(SetupEvent e) {
        setMessage("Setting up...");
        btnSimulaction.setEnabled(false);
    }

    public void initializePerformed(InitializeEvent e) {
        setMessage("Initializing...");
    }

    public void nextStepTriggered(NextStepEvent e) {
        if (!btnSimulaction.isEnabled()) {
            btnSimulaction.setEnabled(true);
        }

    }

    @Action
    public void newCfgFile() {
    }

    @Action
    public void editCfgFile() {
    }

    @Action
    public void next() {
    }

    @Action
    public void last() {
    }

    @Action
    public void previous() {
    }

    @Action
    public void first() {
    }

    @Action
    public Task simulAction() {
        if (!isRunning) {
            return simulActionTask = new SimulActionTask(getApplication());
        } else {
            simulActionTask.cancel(true);
            return null;
        }
    }

    public class SimulActionTask extends Task {

        ResourceMap resourceMap = Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);

        SimulActionTask(Application instance) {
            super(instance);
            setMessage("Simulation started");
            btnSimulaction.setIcon(resourceMap.getIcon("simulAction.Action.icon.stop"));
            isRunning = true;
        }

        @Override
        protected Object doInBackground() throws Exception {
            do {
                setMessage("Simulation " + getSimulationManager().getIndexSimulation());
                getSimulationManager().setup();
                getSimulationManager().init();
                getSimulationManager().getTimeManager().firstStepTriggered();
                getSimulationManager().resetTimer();
                do {
                    getSimulationManager().getSimulation().step();
                    setProgress(getSimulationManager().progressGlobal());
                    setMessage(getSimulationManager().timeLeft());
                } while (!getSimulationManager().isStopped() && getSimulationManager().getTimeManager().hasNextStep());
            } while (!getSimulationManager().isStopped() && getSimulationManager().hasNextSimulation());
            return null;
        }

        @Override
        protected void cancelled() {
            getSimulationManager().stop();
            btnSimulaction.setIcon(resourceMap.getIcon("simulAction.Action.icon.play"));
            isRunning = false;
            setMessage("Simulation interrupted");
        }

        @Override
        protected void succeeded(Object obj) {
            setMessage("End of simulation");
        }
    }

    public void savePreferences() {
        savePreference(openMenuItem, cfgPath.getPath());
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

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        mainPanel = new javax.swing.JPanel();
        toolBar = new javax.swing.JToolBar();
        btnFirst = new javax.swing.JButton();
        btnPrevious = new javax.swing.JButton();
        btnSimulaction = new javax.swing.JButton();
        btnNext = new javax.swing.JButton();
        btnLast = new javax.swing.JButton();
        jSeparator8 = new javax.swing.JToolBar.Separator();
        sliderTime = new javax.swing.JSlider();
        menuBar = new javax.swing.JMenuBar();
        javax.swing.JMenu fileMenu = new javax.swing.JMenu();
        newMenuItem = new javax.swing.JMenuItem();
        openMenuItem = new javax.swing.JMenuItem();
        editMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        javax.swing.JMenuItem exitMenuItem = new javax.swing.JMenuItem();
        javax.swing.JMenu helpMenu = new javax.swing.JMenu();
        javax.swing.JMenuItem aboutMenuItem = new javax.swing.JMenuItem();
        statusPanel = new javax.swing.JPanel();
        javax.swing.JSeparator statusPanelSeparator = new javax.swing.JSeparator();
        statusMessageLabel = new javax.swing.JLabel();
        statusAnimationLabel = new javax.swing.JLabel();
        progressBar = new javax.swing.JProgressBar();

        mainPanel.setName("mainPanel"); // NOI18N

        toolBar.setRollover(true);
        toolBar.setName("toolBar"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getActionMap(IchthyopView.class, this);
        btnFirst.setAction(actionMap.get("first")); // NOI18N
        btnFirst.setFocusable(false);
        btnFirst.setHideActionText(true);
        btnFirst.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnFirst.setName("btnFirst"); // NOI18N
        btnFirst.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnFirst);

        btnPrevious.setAction(actionMap.get("previous")); // NOI18N
        btnPrevious.setFocusable(false);
        btnPrevious.setHideActionText(true);
        btnPrevious.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPrevious.setName("btnPrevious"); // NOI18N
        btnPrevious.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnPrevious);

        btnSimulaction.setAction(actionMap.get("simulAction")); // NOI18N
        btnSimulaction.setFocusable(false);
        btnSimulaction.setHideActionText(true);
        btnSimulaction.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSimulaction.setName("btnSimulaction"); // NOI18N
        btnSimulaction.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnSimulaction);

        btnNext.setAction(actionMap.get("next")); // NOI18N
        btnNext.setFocusable(false);
        btnNext.setHideActionText(true);
        btnNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNext.setName("btnNext"); // NOI18N
        btnNext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnNext);

        btnLast.setAction(actionMap.get("last")); // NOI18N
        btnLast.setFocusable(false);
        btnLast.setHideActionText(true);
        btnLast.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnLast.setName("btnLast"); // NOI18N
        btnLast.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnLast);

        jSeparator8.setName("jSeparator8"); // NOI18N
        toolBar.add(jSeparator8);

        sliderTime.setPaintTrack(false);
        sliderTime.setSnapToTicks(true);
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
        sliderTime.setToolTipText(resourceMap.getString("sliderTime.toolTipText")); // NOI18N
        sliderTime.setAutoscrolls(true);
        sliderTime.setEnabled(false);
        sliderTime.setName("sliderTime"); // NOI18N
        sliderTime.setPreferredSize(new java.awt.Dimension(400, 44));
        toolBar.add(sliderTime);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, 35, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(279, Short.MAX_VALUE))
        );

        menuBar.setName("menuBar"); // NOI18N

        fileMenu.setText(resourceMap.getString("fileMenu.text")); // NOI18N
        fileMenu.setName("fileMenu"); // NOI18N

        newMenuItem.setAction(actionMap.get("newCfgFile")); // NOI18N
        newMenuItem.setName("newMenuItem"); // NOI18N
        fileMenu.add(newMenuItem);

        openMenuItem.setAction(actionMap.get("openCfgFile")); // NOI18N
        openMenuItem.setName("openMenuItem"); // NOI18N
        fileMenu.add(openMenuItem);

        editMenuItem.setAction(actionMap.get("editCfgFile")); // NOI18N
        editMenuItem.setName("editMenuItem"); // NOI18N
        fileMenu.add(editMenuItem);

        jSeparator1.setName("jSeparator1"); // NOI18N
        fileMenu.add(jSeparator1);

        exitMenuItem.setAction(actionMap.get("quit")); // NOI18N
        exitMenuItem.setName("exitMenuItem"); // NOI18N
        fileMenu.add(exitMenuItem);

        menuBar.add(fileMenu);

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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 639, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 453, Short.MAX_VALUE)
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

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnFirst;
    private javax.swing.JButton btnLast;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrevious;
    private javax.swing.JButton btnSimulaction;
    private javax.swing.JMenuItem editMenuItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator8;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JSlider sliderTime;
    private javax.swing.JLabel statusAnimationLabel;
    private javax.swing.JLabel statusMessageLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables
    private final Timer messageTimer;
    private final Timer busyIconTimer;
    private final Icon idleIcon;
    private final Icon[] busyIcons = new Icon[15];
    private int busyIconIndex = 0;
    private JDialog aboutBox;
    private static final Logger logger = Logger.getLogger(IchthyopApp.class.getName());
    private File cfgPath = new File(System.getProperty("user.dir"));
    private boolean isRunning = false;
    private Task simulActionTask;
}
