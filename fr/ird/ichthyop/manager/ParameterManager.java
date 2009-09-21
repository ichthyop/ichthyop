package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.arch.IParameterManager;
import fr.ird.ichthyop.io.ICFile;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author pverley
 */
public class ParameterManager extends PropertyManager implements IParameterManager {

    public ParameterManager(Class myClass) {
        super(myClass);
    }

    public String getValue(String key) {
        return ICFile.getInstance().getParameter(getProperty(key + ".key")).getValue();
    }
}
