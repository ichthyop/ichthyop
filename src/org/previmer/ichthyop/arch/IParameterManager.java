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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import org.previmer.ichthyop.io.ConfigurationFile;

/**
 *
 * @author pverley
 */
public interface IParameterManager {

    public void setConfigurationFile(File file) throws Exception;

    public ConfigurationFile getConfigurationFile();

    public String getParameter(String blockName, String key);

    //public XBlock getBlock(BlockType type, String key);
    public boolean isBlockEnabled(BlockType type, String key);

    public Iterable<XBlock> getBlocks(BlockType type);

    public List<XParameter> getParameters(ParamType paramType);

    public String getParameter(BlockType blockType, String blockKey, String key);

    public void addBlock(XBlock block);

    public void cleanup();

    public void save() throws IOException, FileNotFoundException;

    public Collection<XBlock> readBlocks() throws IOException;

    public String getConfigurationTitle();

    public void setConfigurationTitle(String title);

    public String getConfigurationDescription();

    public void setConfigurationDescription(String description);

    public String getConfigurationVersion();

    public void setConfigurationVersion(String version);
}
