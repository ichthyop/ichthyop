package ichthyop.ui;

/** import AWT */
import java.awt.event.ActionListener;

/** import java.io */
import java.io.IOException;

/** local import */
import ichthyop.util.INIFile;

/**
 * This interface provides common methods to the tabs of the configuration
 * editor.
 *
 * <p>Copyright: Copyright (c) 2007 - Free software under GNU GPL</p>
 *
 * @author P.Verley
 * @see ichthyop.ui.ConfigurationUI for the implementation of the interface.
 */

interface ITab extends ActionListener {

    /**
     * Adds listeners to the tab.
     */
    void addListeners();

    /**
     * Creates the UI
     */
    void createUI();

    /**
     * Reads the default value of the parameters in the specified
     * configuration file
     * @param file INIFile, the configuration file.
     */
    void read(INIFile file);

    /**
     * Writes the value of the parameters in te specified configuration file.
     * @param file INIFile, the configuration file
     * @throws an IOException if an error occured while writing the values in
     * the file.
     */
    void write(INIFile file) throws IOException;

    /**
     * Gets the name of the tab.
     * @return the tab's name.
     */
    public String getName();

    /**
     * Sets the tooltips of the components in the tab.
     */
    void setToolTips();
}
