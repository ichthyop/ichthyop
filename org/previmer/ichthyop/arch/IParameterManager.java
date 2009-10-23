/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.previmer.ichthyop.arch;

import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.ParamType;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;
import java.io.File;
import java.util.List;

/**
 *
 * @author pverley
 */
public interface IParameterManager {

    public void setConfigurationFile(File file);

    public String getParameter(String blockName, String key);

    //public XBlock getBlock(BlockType type, String key);
    public boolean isBlockEnabled(BlockType type, String key);

    public Iterable<XBlock> getBlocks(BlockType type);

    public List<XParameter> getParameters(ParamType paramType);

    public String getParameter(BlockType blockType, String blockKey, String key);

}
