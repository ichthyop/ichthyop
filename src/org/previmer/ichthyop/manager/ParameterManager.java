package org.previmer.ichthyop.manager;

import org.previmer.ichthyop.event.InitializeEvent;
import org.previmer.ichthyop.event.SetupEvent;
import org.previmer.ichthyop.io.BlockType;
import org.previmer.ichthyop.arch.IParameterManager;
import org.previmer.ichthyop.io.ParamType;
import org.previmer.ichthyop.io.XBlock;
import org.previmer.ichthyop.io.XParameter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

/**
 *
 * @author pverley
 */
public class ParameterManager extends AbstractManager implements IParameterManager {

    private static ParameterManager parameterManager = new ParameterManager();
    private ConfigurationFile cfgFile;

    public static ParameterManager getInstance() {
        return parameterManager;
    }

    public void setConfigurationFile(File file) {
        cfgFile = new ConfigurationFile(file);
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
            throw new NullPointerException("Could not retrieve parameter " + blockKey + "." + key);
        }
    }

    public boolean isBlockEnabled(BlockType type, String key) {
        return cfgFile.getBlock(type, key).isEnabled();
    }

    public Iterable<XBlock> getBlocks(BlockType type) {
        return cfgFile.getBlocks(type);
    }

    public Collection<XBlock> readBlocks() {
        return cfgFile.readBlocks();
    }

    public void cleanup() {
        cfgFile.removeAllBlocks();
    }

    public void save() throws IOException, FileNotFoundException {
        cfgFile.write(new FileOutputStream(cfgFile.file));
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
                getLogger().log(Level.SEVERE, null, e);
            }
        }

        private void write(OutputStream out) throws IOException {
            org.jdom.output.Format format = org.jdom.output.Format.getPrettyFormat();
            format.setEncoding(System.getProperty("file.encoding"));
            XMLOutputter xmlOut = new XMLOutputter(format);
            xmlOut.output(structure, out);
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

        private void removeAllBlocks() {
            structure.getRootElement().removeChildren(XBlock.BLOCK);
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

        private List<XBlock> readBlocks() {
            List<Element> list = structure.getRootElement().getChildren(XBlock.BLOCK);
            List<XBlock> listBlock = new ArrayList(list.size());
            for (Element elt : list) {
                listBlock.add(new XBlock(elt));
            }
            return listBlock;
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
            for (XBlock xblock : readBlocks()) {
                lmap.put(new BlockId(xblock.getType(), xblock.getKey()).toString(), xblock);
            }
            return lmap;
        }

        private void addBlock(Content child) {
            XBlock block = (XBlock) child.detach();
            block.prepairForWriting();
            structure.getRootElement().addContent(block);
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
}
