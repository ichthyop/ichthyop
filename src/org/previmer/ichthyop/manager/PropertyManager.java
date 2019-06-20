/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.manager;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pverley
 */
public class PropertyManager {

    private static final Logger logger = Logger.getLogger(PropertyManager.class.getName());
    private final Class myClass;
    private ResourceBundle bundle = null;

    private PropertyManager(Class myClass) {
        this.myClass = myClass;

        String bundleName = myClass.getPackage().getName() + ".resources." + myClass.getSimpleName();

        try {
            bundle = ResourceBundle.getBundle(bundleName);
        } catch (MissingResourceException e) {
            logger.log(Level.SEVERE, "Couldn't load bundle: " + bundleName);
        }
    }

    public static PropertyManager getInstance(Class forClass) {
        return new PropertyManager(forClass);
    }

    public String getBundleClass() {
        return myClass.getCanonicalName();
    }

    public String getProperty(String key) {
        if (bundle != null) {
            try {
                return bundle.getString(key);
            } catch (MissingResourceException ex) {
            }
        }
        return null;
    }
}
