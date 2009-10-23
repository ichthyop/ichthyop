package org.previmer.ichthyop.manager;

import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.arch.IParameterManager;
import org.previmer.ichthyop.io.ParamType;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;

/**
 *
 * @author pverley
 */
public class ParameterManager implements IParameterManager {

    private static ParameterManager parameterManager = new ParameterManager();
    private ConfigurationFile cfgFile;
    private Hashtable<ParamType, List<XParameter>> parameters;

    public static ParameterManager getInstance() {
        return parameterManager;
    }

    public void setConfigurationFile(File file) {
        cfgFile = new ConfigurationFile(file);
        parameters = new Hashtable();
    }

    public List<XParameter> getParameters(ParamType paramType) {
        return cfgFile.getParameters(paramType);
    }

    public String getParameter(String blockKey, String key) {
        return getParameter(BlockType.OPTION, blockKey, key);
    }

    public String getParameter(BlockType blockType, String blockKey, String key) {

        XParameter xparam = cfgFile.getBlock(blockType, blockKey).getXParameter(key);
        if (xparam != null) {
            return xparam.getValue();
        } else {
            return "";
        }
    }

    public boolean isBlockEnabled(BlockType type, String key) {
        return cfgFile.getBlock(type, key).isEnabled();
    }

    public Iterable<XBlock> getBlocks(BlockType type) {
        return cfgFile.getBlocks(type);
    }

    private class ConfigurationFile {

        private File file;
        private Document structure;
        private HashMap<String, XBlock> map;

        ConfigurationFile(File file) {
            this.file = file;
            load();
        }

        void load() {

            SAXBuilder sxb = new SAXBuilder();
            try {
                Element racine = sxb.build(file).getRootElement();
                racine.detach();
                structure = new Document(racine);
                map = createMap();
            } catch (Exception e) {
                Logger.getLogger(ConfigurationFile.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        public List<XParameter> getParameters(ParamType paramType) {
            List<XParameter> list = new ArrayList();
            for (XBlock xblock : map.values()) {
                if (xblock.isEnabled()) {
                    for (XParameter xparam : xblock.getXParameters()) {
                        if (xparam.getType().equals(paramType)) {
                            list.add(xparam);
                        }
                    }
                }
            }
            return list;
        }

        private XParameter getXParameter(BlockType type, String blockKey, String key) {
            return map.get(new BlockId(type, blockKey).toString()).getXParameter(key);
        }

        private Iterable getBlocks(BlockType type) {
            ArrayList<XBlock> list = new ArrayList();
            for (XBlock xblock : map.values()) {
                if (xblock.getType().equals(type)) {
                    list.add(xblock);
                }
            }
            return list;
        }

        private XBlock getBlock(final BlockType type, final String key) {
            return map.get(new BlockId(type, key).toString());
        }

        private List<XBlock> readBlocks(final BlockType type) {

            Filter filtre = new Filter() {

                public boolean matches(Object obj) {
                    if (!(obj instanceof Element)) {
                        return false;
                    }
                    Element element = (Element) obj;
                    if (element.getAttributeValue(XBlock.TYPE).matches(type.toString())) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            List<XBlock> list = new ArrayList();
            for (Object elt : structure.getRootElement().getContent(filtre)) {
                list.add(new XBlock(type, (Element) elt));
            }
            return list;
        }

        private HashMap<String, XBlock> createMap() {
            HashMap<String, XBlock> lmap = new HashMap();
            for (BlockType type : BlockType.values()) {
                for (XBlock xblock : readBlocks(type)) {
                    lmap.put(new BlockId(xblock.getType(), xblock.getKey()).toString(), xblock);
                }
            }
            return lmap;
        }
    }

    private class BlockId {

        private BlockType blockType;
        private String blockKey;

        BlockId(BlockType type, String blockName) {
            this.blockType = type;
            this.blockKey = blockName;
        }

        private BlockType getBlockType() {
            return blockType;
        }

        private String getBlockKey() {
            return blockKey.trim().toLowerCase();
        }

        @Override
        public String toString() {
            StringBuffer id = new StringBuffer(getBlockType().toString());
            id.append('/');
            id.append(getBlockKey());
            return id.toString();
        }
    }

    private class ParameterId {

        private BlockType type;
        private String blockName;
        private String key;

        ParameterId(BlockType type, String blockName, String key) {
            this.type = type;
            this.blockName = blockName;
            this.key = key;
        }

        private BlockType getBlockType() {
            return type;
        }

        private String getBlockName() {
            return blockName.trim().toLowerCase();
        }

        /**
         * @return the key
         */
        private String getKey() {
            return key.trim().toLowerCase();
        }

        @Override
        public String toString() {
            StringBuffer id = new StringBuffer(getBlockType().toString());
            id.append('/');
            id.append(getBlockName());
            id.append('/');
            id.append(getKey());
            return id.toString();
        }
    }
}
