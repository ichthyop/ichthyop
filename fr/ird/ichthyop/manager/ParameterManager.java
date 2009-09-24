package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.arch.IParameterManager;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.io.XParameter;
import java.util.ArrayList;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author pverley
 */
public class ParameterManager extends PropertyManager implements IParameterManager {

    private String className;

    public ParameterManager(Class myClass) {
        super(myClass);
        className = myClass.getCanonicalName();
    }

    public String getValue(String key) {

        XParameter param;
        param = getXParameter(className, key);
        if (param != null) {
            return param.getValue();
        } else if ((param = getXParameter(key)) != null) {
            return param.getValue();
        } else {
            return "";
        }
    }

    public ArrayList<XParameter> getXParameters() {
        return ICFile.getInstance().getAction(className).getParameters();
    }

    private XParameter getXParameter(String actionName, String key) {
        return ICFile.getInstance().getAction(actionName).getParameter(key);
    }

    private XParameter getXParameter(String key) {
        return ICFile.getInstance().getParameter(key);
    }
}
