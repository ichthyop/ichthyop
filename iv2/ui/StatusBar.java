package ichthyop.ui;

/** import AWT */
import java.awt.Dimension;

/** import Swing */
import javax.swing.JLabel;

/** local import */
import ichthyop.util.Resources;

/**
 * A display area to be added at the bottom of a frame to display information
 * about the current state of the application, or some messages.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 */

public class StatusBar extends JLabel {

///////////////////////////////
// Declaration of the variables
///////////////////////////////

    /**
     * The String to display as a prefix of the message.
     */
    private String prefix;

///////////////
// Constructors
///////////////

    /**
     * Constructs a new Statusbar with the specified prefix.
     *
     * @param prefix String to display as prefix.
     */
    public StatusBar(String prefix) {

        this.prefix = prefix;
        super.setPreferredSize(new Dimension(100, 16));
    }

    /**
     * Creates a new empty Statusbar.
     */
    public StatusBar() {

        this(Resources.MSG_PREFIX);
        setMessage(Resources.MSG_WAITING);
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
        setText(prefix + message);
    }

    /**
     * Gets the message displays in the statusbar.
     *
     * @return the String currently displayed
     */
    public String getMessage() {
        return getText();
    }

    //---------- End of class
}
