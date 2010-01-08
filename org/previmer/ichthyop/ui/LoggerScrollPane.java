/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author pverley
 */
public class LoggerScrollPane extends JScrollPane {

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
        
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        setViewportView(textArea);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        setPreferredSize(new Dimension(250, 150));
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
        StringBuffer msg = new StringBuffer(getMessage());
        if (!getMessage().isEmpty()) {
            msg.append('\n');
            msg.append('\n');
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        msg.append(sdf.format(Calendar.getInstance().getTime()));
        msg.append('\n');
        msg.append(message);
        textArea.setText(msg.toString());
    }

    /**
     * Gets the message displays in the statusbar.
     *
     * @return the String currently displayed
     */
    public String getMessage() {
        return textArea.getText();
    }
}
