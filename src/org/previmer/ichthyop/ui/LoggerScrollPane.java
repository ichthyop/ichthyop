/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.ui;

import org.previmer.ichthyop.ui.logging.JTextAreaHandler;
import java.util.logging.Logger;
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
        setViewportView(textArea);
    }

    public void connectToLogger(Logger logger) {
        logger.addHandler(new JTextAreaHandler(textArea));
    }
}
