/*
 * IchthyopView.java
 */
package org.previmer.ichthyop.ui;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import org.jdesktop.application.Action;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.SingleFrameApplication;
import org.jdesktop.application.FrameView;
import org.jdesktop.application.TaskMonitor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
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
        btnProgress.getAction().setEnabled(false);

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
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setMessage("Setting up...");
            }
        });
    }

    public void initializePerformed(InitializeEvent e) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                setMessage("Initializing...");
            }
        });
    }

    public void nextStepTriggered(NextStepEvent e) {
        try {
            SwingUtilities.invokeAndWait(new Runnable() {

                public void run() {
                    if (!btnSimulaction.isEnabled()) {
                        btnSimulaction.getAction().setEnabled(true);
                        btnProgress.getAction().setEnabled(true);
                    }
                    setProgress();
                }
            });
        } catch (InterruptedException ex) {
            Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvocationTargetException ex) {
            Logger.getLogger(IchthyopView.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void setProgress() {

        progressBarCurrent.setValue((int) (getSimulationManager().progressCurrent() * 100));
        StringBuffer strBf = new StringBuffer();
        strBf.append(getSimulationManager().getTimeManager().stepToString());
        strBf.append(" - ");
        strBf.append(getSimulationManager().timeLeftCurrent());
        lblTimeLeftCurrent.setText(strBf.toString());

        if (getSimulationManager().getNumberOfSimulations() > 1) {
            lblProgressGlobal.setVisible(true);
            progressBarGlobal.setVisible(true);
            lblTimeLeftGlobal.setVisible(true);
            progressBarGlobal.setValue((int) (getSimulationManager().progressGlobal() * 100));
            strBf = new StringBuffer("Simulation ");
            strBf.append(getSimulationManager().indexSimulationToString());
            strBf.append(" - ");
            strBf.append(getSimulationManager().timeLeftGlobal());
            lblTimeLeftGlobal.setText(strBf.toString());
        } else {
            lblProgressGlobal.setVisible(false);
            progressBarGlobal.setVisible(false);
            lblTimeLeftGlobal.setVisible(false);
        }
    }

    @Action
    public void newCfgFile() {
    }

    @Action
    public void editCfgFile() {
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

    private void setMenuEnabled(boolean enabled) {
        openMenuItem.getAction().setEnabled(enabled);
        newMenuItem.getAction().setEnabled(enabled);
        editMenuItem.getAction().setEnabled(enabled);
    }

    @Action
    public void showProgress() {
        popupProgress.show(btnProgress, 0, btnProgress.getHeight());
        //pnlProgress.setVisible(!pnlProgress.isVisible());
    }

    public class SimulActionTask extends Task {

        ResourceMap resourceMap = Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);

        SimulActionTask(Application instance) {
            super(instance);
            setMessage("Simulation started");
            setMenuEnabled(false);
            btnSimulaction.setIcon(resourceMap.getIcon("simulAction.Action.icon.stop"));
            isRunning = true;
            btnSimulaction.getAction().setEnabled(false);
        }

        @Override
        protected Object doInBackground() throws Exception {
            getSimulationManager().resetTimerGlobal();
            do {
                setMessage("Simulation " + getSimulationManager().indexSimulationToString());
                getSimulationManager().setup();
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
            btnSimulaction.setIcon(resourceMap.getIcon("simulAction.Action.icon.play"));
            openMenuItem.getAction().setEnabled(true);
            btnProgress.getAction().setEnabled(false);
            isRunning = false;
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
        btnOpenCfgFile = new javax.swing.JButton();
        btnNewCfgFile = new javax.swing.JButton();
        btnEditCfgFile = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnSimulaction = new javax.swing.JButton();
        btnProgress = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        btnExit = new javax.swing.JButton();
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
        popupProgress = new javax.swing.JPopupMenu();
        pnlProgress = new javax.swing.JPanel();
        lblProgressCurrent = new javax.swing.JLabel();
        progressBarCurrent = new javax.swing.JProgressBar();
        lblTimeLeftCurrent = new javax.swing.JLabel();
        lblProgressGlobal = new javax.swing.JLabel();
        progressBarGlobal = new javax.swing.JProgressBar();
        lblTimeLeftGlobal = new javax.swing.JLabel();

        mainPanel.setName("mainPanel"); // NOI18N

        toolBar.setRollover(true);
        toolBar.setName("toolBar"); // NOI18N

        javax.swing.ActionMap actionMap = org.jdesktop.application.Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getActionMap(IchthyopView.class, this);
        btnOpenCfgFile.setAction(actionMap.get("openCfgFile")); // NOI18N
        org.jdesktop.application.ResourceMap resourceMap = org.jdesktop.application.Application.getInstance(org.previmer.ichthyop.ui.IchthyopApp.class).getContext().getResourceMap(IchthyopView.class);
        btnOpenCfgFile.setIcon(resourceMap.getIcon("btnOpenCfgFile.icon")); // NOI18N
        btnOpenCfgFile.setText(resourceMap.getString("btnOpenCfgFile.text")); // NOI18N
        btnOpenCfgFile.setFocusable(false);
        btnOpenCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnOpenCfgFile.setName("btnOpenCfgFile"); // NOI18N
        btnOpenCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnOpenCfgFile);

        btnNewCfgFile.setAction(actionMap.get("newCfgFile")); // NOI18N
        btnNewCfgFile.setIcon(resourceMap.getIcon("btnNewCfgFile.icon")); // NOI18N
        btnNewCfgFile.setText(resourceMap.getString("btnNewCfgFile.text")); // NOI18N
        btnNewCfgFile.setFocusable(false);
        btnNewCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNewCfgFile.setName("btnNewCfgFile"); // NOI18N
        btnNewCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnNewCfgFile);

        btnEditCfgFile.setAction(actionMap.get("editCfgFile")); // NOI18N
        btnEditCfgFile.setIcon(resourceMap.getIcon("btnEditCfgFile.icon")); // NOI18N
        btnEditCfgFile.setText(resourceMap.getString("btnEditCfgFile.text")); // NOI18N
        btnEditCfgFile.setFocusable(false);
        btnEditCfgFile.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnEditCfgFile.setName("btnEditCfgFile"); // NOI18N
        btnEditCfgFile.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnEditCfgFile);

        jSeparator2.setName("jSeparator2"); // NOI18N
        toolBar.add(jSeparator2);

        btnSimulaction.setAction(actionMap.get("simulAction")); // NOI18N
        btnSimulaction.setFocusable(false);
        btnSimulaction.setHideActionText(true);
        btnSimulaction.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnSimulaction.setName("btnSimulaction"); // NOI18N
        btnSimulaction.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnSimulaction);

        btnProgress.setAction(actionMap.get("showProgress")); // NOI18N
        btnProgress.setFocusable(false);
        btnProgress.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnProgress.setName("btnProgress"); // NOI18N
        btnProgress.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnProgress);

        jSeparator3.setName("jSeparator3"); // NOI18N
        toolBar.add(jSeparator3);

        btnExit.setAction(actionMap.get("quit")); // NOI18N
        btnExit.setIcon(resourceMap.getIcon("btnExit.icon")); // NOI18N
        btnExit.setText(resourceMap.getString("btnExit.text")); // NOI18N
        btnExit.setFocusable(false);
        btnExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExit.setName("btnExit"); // NOI18N
        btnExit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(btnExit);

        javax.swing.GroupLayout mainPanelLayout = new javax.swing.GroupLayout(mainPanel);
        mainPanel.setLayout(mainPanelLayout);
        mainPanelLayout.setHorizontalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE)
        );
        mainPanelLayout.setVerticalGroup(
            mainPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(mainPanelLayout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
        exitMenuItem.setIcon(resourceMap.getIcon("exitMenuItem.icon")); // NOI18N
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
            .addComponent(statusPanelSeparator, javax.swing.GroupLayout.DEFAULT_SIZE, 613, Short.MAX_VALUE)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusMessageLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 427, Short.MAX_VALUE)
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

        popupProgress.setName("popupProgress"); // NOI18N
        popupProgress.add(pnlProgress);

        pnlProgress.setName("pnlProgress"); // NOI18N

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
                    .addComponent(progressBarGlobal, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE)
                    .addComponent(progressBarCurrent, javax.swing.GroupLayout.DEFAULT_SIZE, 356, Short.MAX_VALUE))
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

        setComponent(mainPanel);
        setMenuBar(menuBar);
        setStatusBar(statusPanel);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnEditCfgFile;
    private javax.swing.JButton btnExit;
    private javax.swing.JButton btnNewCfgFile;
    private javax.swing.JButton btnOpenCfgFile;
    private javax.swing.JButton btnProgress;
    private javax.swing.JButton btnSimulaction;
    private javax.swing.JMenuItem editMenuItem;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JLabel lblProgressCurrent;
    private javax.swing.JLabel lblProgressGlobal;
    private javax.swing.JLabel lblTimeLeftCurrent;
    private javax.swing.JLabel lblTimeLeftGlobal;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem newMenuItem;
    private javax.swing.JMenuItem openMenuItem;
    private javax.swing.JPanel pnlProgress;
    private javax.swing.JPopupMenu popupProgress;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JProgressBar progressBarCurrent;
    private javax.swing.JProgressBar progressBarGlobal;
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
