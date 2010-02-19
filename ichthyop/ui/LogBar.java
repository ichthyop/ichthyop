/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package ichthyop.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/**
 *
 * @author pverley
 */
public class LogBar {

    private final static String TITLE = "Log panel";
    private JTextArea textArea;

///////////////
// Constructors
///////////////
    /**
     * Constructs a new Statusbar with the specified prefix.
     *
     * @param prefix String to display as prefix.
     */
    public LogBar() {
    }

///////////////////////////
// Definition of the method
///////////////////////////
    public JScrollPane createUI() {

        textArea = new JTextArea();
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane areaScrollPane = new JScrollPane(textArea);
        areaScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        areaScrollPane.setPreferredSize(new Dimension(250, 150));
        areaScrollPane.setBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(TITLE),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)),
                areaScrollPane.getBorder()));

        return areaScrollPane;
    }

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
