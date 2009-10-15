package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.io.TypeBlock;
import fr.ird.ichthyop.arch.IParameterManager;
import fr.ird.ichthyop.io.ICFile;
import fr.ird.ichthyop.io.XBlock;
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
public class ParameterManager implements IParameterManager {

    private static ParameterManager parameterManager = new ParameterManager();

    public static ParameterManager getInstance() {
        return parameterManager;
    }

    public String getValue(String blockName, String key) {

        XParameter param;
        param = getXParameter(blockName, key);
        if (param != null) {
            return param.getValue();
        } else {
            return "";
        }
    }

    public ArrayList<XParameter> getXParameters(String blockName) {
        return getBlock(blockName).getParameters();
    }

    private XParameter getXParameter(String blockName, String key) {
        return getBlock(blockName).getParameter(key);
    }

    private XBlock getBlock(String blockName) {
        return ICFile.getInstance().getBlock(TypeBlock.OPTION, blockName);
    }
}
