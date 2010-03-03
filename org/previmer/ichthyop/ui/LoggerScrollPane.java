/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import org.jdesktop.application.TaskMonitor;

/**
 *
 * @author pverley
 */
public class LoggerScrollPane extends JScrollPane implements PropertyChangeListener {

    private JTextArea textArea = new JTextArea();

///////////////
// Constructors
///////////////
    /**
     * Constructs a new Statusbar with the specified prefix.
     *
     * @param prefix String to display as prefix.
     */
    public LoggerScrollPane() {

        setViewportView(textArea);

        // connecting action tasks to the logger pane via TaskMonitor
        TaskMonitor taskMonitor = new TaskMonitor(IchthyopApp.getApplication().getContext());
        taskMonitor.addPropertyChangeListener(new java.beans.PropertyChangeListener() {

            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                String propertyName = evt.getPropertyName();
                if ("message".equals(propertyName)) {
                    String text = (String) (evt.getNewValue());
                    setMessage(text);
                }
            }
        });
    }

///////////////////////////
// Definition of the method
///////////////////////////
    /**
     * Sets the message to display in the statusbar.
     *
     * @param message the String to display
     */
    public void setMessage(String message) {
        if (message != null && !message.isEmpty()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            textArea.append(sdf.format(Calendar.getInstance().getTime()));
            textArea.append("\n");
            textArea.append(message);
            textArea.append("\n");
        }
    }

    /**
     * Gets the message displays in the statusbar.
     *
     * @return the String currently displayed
     */
    public String getMessage() {
        return textArea.getText();
    }

    public void propertyChange(PropertyChangeEvent evt) {
        setMessage((String) evt.getNewValue());
    }
}
