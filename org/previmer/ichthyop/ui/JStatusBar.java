/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import org.jdesktop.application.Application;
import org.jdesktop.application.ResourceMap;
import org.jdesktop.application.TaskMonitor;
import org.jdesktop.swingx.JXBusyLabel;
import org.jdesktop.swingx.JXStatusBar;
import org.jdesktop.swingx.icon.EmptyIcon;
import org.jdesktop.swingx.painter.BusyPainter;
import org.jdesktop.swingx.plaf.basic.BasicStatusBarUI;

/**
 *
 * @author pverley
 */
public class JStatusBar extends JXStatusBar {

///////////////////////////////
// Declaration of the variables
///////////////////////////////
    private JLabel statusMessageLabel = new JLabel();
    private JXBusyLabel statusAnimationLabel = new JXBusyLabel();
    private JPanel pnlProgressBar = new JPanel();
    private JProgressBar progressBar = new JProgressBar();
    private JLabel lblFlag = new JLabel();
    private Timer messageTimer;

///////////////
// Constructors
///////////////
    public JStatusBar() {
        initComponents();
        monitorTasks();
    }

////////////////////////////
// Definition of the methods
////////////////////////////
    private void initComponents() {

        // Set opacity of the compoments to false
        statusMessageLabel.setOpaque(false);
        pnlProgressBar.setOpaque(false);
        statusAnimationLabel.setOpaque(false);
        lblFlag.setOpaque(false);

        // Designed of the statusAnimationLabel (the JXBusyLabel)
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

        // Add the components in the status bar
        putClientProperty(BasicStatusBarUI.AUTO_ADD_SEPARATOR, false);
        JXStatusBar.Constraint c0 = new JXStatusBar.Constraint();
        c0.setFixedWidth(20);
        add(lblFlag, c0);
        JXStatusBar.Constraint c1 = new JXStatusBar.Constraint(new Insets(0, 5, 0, 5));
        add(statusMessageLabel, c1);
        JXStatusBar.Constraint c2 = new JXStatusBar.Constraint(JXStatusBar.Constraint.ResizeBehavior.FILL);
        add(pnlProgressBar, c2);
        JXStatusBar.Constraint c3 = new JXStatusBar.Constraint(new Insets(0, 5, 0, 5));
        c3.setFixedWidth(20);
        add(statusAnimationLabel, c3);
    }

    private void monitorTasks() {

        int messageTimeout = getResourceMap().getInteger("StatusBar.messageTimeout");
        messageTimer = new Timer(messageTimeout, new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                statusMessageLabel.setText("");
                lblFlag.setIcon(getResourceMap().getIcon("lblFlag.icon.grey"));
            }
        });
        messageTimer.setRepeats(false);
        progressBar.setVisible(false);

        // connecting action tasks to status bar via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(IchthyopApp.getApplication().getContext());
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
    }

    public JProgressBar getProgressBar() {
        return progressBar;
    }

    public void setMessage(String text) {
        statusMessageLabel.setText((text == null) ? "" : text);
        messageTimer.restart();
    }

    private ResourceMap getResourceMap() {
        return Application.getInstance().getContext().getResourceMap(JStatusBar.class);
    }
}