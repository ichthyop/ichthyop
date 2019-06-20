package org.previmer.ichthyop.manager;

import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.io.ParamType;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.previmer.ichthyop.Version;
import org.previmer.ichthyop.io.ConfigurationFile;

/**
 *
 * @author pverley
 */
public class ParameterManager extends AbstractManager {

    private static ParameterManager parameterManager = new ParameterManager();
    private ConfigurationFile cfgFile;

    public static ParameterManager getInstance() {
        return parameterManager;
    }

    public void setConfigurationFile(File file) throws Exception {
        cfgFile = new ConfigurationFile(file);
        cfgFile.load();
    }

    public ConfigurationFile getConfigurationFile() {
        return cfgFile;
    }

    public String getConfigurationDescription() {
        return cfgFile.getDescription();
    }

    public void setConfigurationDescription(String description) {
        cfgFile.setDescription(description);
    }

    public Version getConfigurationVersion() {
        return cfgFile.getVersion();
    }

    public void setConfigurationVersion(Version version) {
        cfgFile.setVersion(version);
    }

    public String getConfigurationTitle() {
        return cfgFile.getLongName();
    }

    public void setConfigurationTitle(String longName) {
        cfgFile.setLongName(longName);
    }

    public List<XParameter> getParameters(ParamType paramType) {
        return cfgFile.getParameters(paramType);
    }

    public String getParameter(String blockKey, String key) {
        return getParameter(BlockType.OPTION, blockKey, key);
    }

    public String[] getListParameter(BlockType blockType, String blockKey, String key) {
        String[] tokens = getParameter(blockType, blockKey, key).split("\"");
        List<String> list = new ArrayList();
        for (String token : tokens) {
            if (!token.trim().isEmpty()) {
                list.add(token.trim());
            }
        }
        return list.toArray(new String[list.size()]);
    }

    public String getParameter(BlockType blockType, String blockKey, String key) {

        XParameter xparam = cfgFile.getBlock(blockType, blockKey).getXParameter(key);
        if (xparam != null) {
            return xparam.getValue();
        } else {
            throw new NullPointerException("Could not retrieve parameter " + blockKey + "/" + key);
        }
    }

    public boolean isBlockEnabled(BlockType type, String key) {
        return cfgFile.getBlock(type, key).isEnabled();
    }

    public Iterable<XBlock> getBlocks(BlockType type) {
        return cfgFile.getBlocks(type);
    }

    public Collection<XBlock> readBlocks() throws IOException {
        return cfgFile.readBlocks();
    }

    public void cleanup() {
        cfgFile.removeAllBlocks();
    }

    public void save() throws IOException, FileNotFoundException {
        cfgFile.write(new FileOutputStream(cfgFile.getFile()));
    }

    public void addBlock(XBlock block) {
        cfgFile.addBlock(block);
    }

    public void setupPerformed(SetupEvent e) throws Exception {
        // does nothing
    }

    public void initializePerformed(InitializeEvent e) {
        // does nothing
    }
}
