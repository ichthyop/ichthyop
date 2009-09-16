package fr.ird.ichthyop;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author pverley
 */
public class ParameterManager {

    private static final Logger logger = Logger.getLogger(ParameterManager.class.getName());
    private final Class myClass;
    private final Class icfileClass = fr.ird.ichthyop.ICFile.class;
    // Resource bundle for internationalized and accessible text
    private ResourceBundle bundle = null;
    private ResourceBundle generalBundle = null;

    public ParameterManager(Class myClass) {
        this.myClass = myClass;

        String bundleName = myClass.getPackage().getName() + ".resources." + myClass.getSimpleName();

        try {
            bundle = ResourceBundle.getBundle(bundleName);
        } catch (MissingResourceException e) {
            logger.log(Level.SEVERE, "Couldn't load bundle: " + bundleName);
        }

        bundleName = icfileClass.getPackage().getName() + ".resources." + icfileClass.getSimpleName();
        try {
            generalBundle = ResourceBundle.getBundle(bundleName);
        } catch (MissingResourceException e) {
            logger.log(Level.SEVERE, "Couldn't load bundle: " + bundleName);
        }
    }

    public String getBundleClass() {
        return myClass.getSimpleName();
    }

    public String getString(String key) {
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException ex) {
            }
        }
        return generalBundle != null ? generalBundle.getString(key) : null;
    }

    public String getValue(String key) {
        return ICFile.getInstance().getParameter(getString(key + ".key")).getValue();
    }
}
