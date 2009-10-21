/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop.arch;

import fr.ird.ichthyop.io.BlockType;
import fr.ird.ichthyop.io.ParamType;
import fr.ird.ichthyop.io.XBlock;
import fr.ird.ichthyop.io.XParameter;
import java.io.File;
import java.util.List;

/**
 *
 * @author pverley
 */
public interface IParameterManager {

    public void setConfigurationFile(File file);

    public String getParameter(String blockName, String key);

    public XBlock getBlock(BlockType type, String key);

    public List<XBlock> getBlocks(BlockType type);

    public List<XParameter> getParameters(ParamType paramType);

}
