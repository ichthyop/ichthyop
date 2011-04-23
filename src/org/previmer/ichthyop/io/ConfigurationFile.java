package org.previmer.ichthyop.io;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.filter.Filter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;

public class ConfigurationFile {

    private File file;
    private Document structure;
    private HashMap<String, XBlock> map;
    public final static String DESCRIPTION = "description";
    public final static String LONG_NAME = "long_name";
    public final static String VERSION = "version";

    public ConfigurationFile(File file) {
        this.file = file;
    }

    public ConfigurationFile(URL url) {
        try {
            SAXBuilder sxb = new SAXBuilder();
            Element racine = sxb.build(url).getRootElement();
            racine.detach();
            structure = new Document(racine);
            map = createMap();
        } catch (Exception ex) {
            Logger.getLogger(ConfigurationFile.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public File getFile() {
        return file;
    }

    public void load() throws Exception {

        SAXBuilder sxb = new SAXBuilder();
        Element racine = sxb.build(file).getRootElement();
        racine.detach();
        structure = new Document(racine);
        map = createMap();
    }

    public String getDescription() {
        if (null != structure.getRootElement().getChild(DESCRIPTION)) {
            return structure.getRootElement().getChildTextNormalize(DESCRIPTION);
        } else {
            return null;
        }
    }

    public void setDescription(String description) {
        if (null == structure.getRootElement().getChild(DESCRIPTION)) {
            structure.getRootElement().addContent(new Element(DESCRIPTION));
        }
        structure.getRootElement().getChild(DESCRIPTION).setText(description);
    }

    public String getVersion() {
        if (null != structure.getRootElement().getChild(VERSION)) {
            return structure.getRootElement().getChildTextNormalize(VERSION);
        } else {
            return null;
        }
    }

    public void setVersion(String version) {
        if (null == structure.getRootElement().getChild(VERSION)) {
            structure.getRootElement().addContent(new Element(VERSION));
        }
        structure.getRootElement().getChild(VERSION).setText(version);
    }

    public void setLongName(String longName) {
        if (null == structure.getRootElement().getChild(LONG_NAME)) {
            structure.getRootElement().addContent(new Element(LONG_NAME));
        }
        structure.getRootElement().getChild(LONG_NAME).setText(longName);
    }

    public String getLongName() {
        if (null != structure.getRootElement().getChild(LONG_NAME)) {
            return structure.getRootElement().getChildTextNormalize(LONG_NAME);
        } else {
            String filename = file.getName();
            filename = filename.substring(0, filename.lastIndexOf("."));
            return filename;
        }
    }

    public void write(OutputStream out) throws IOException {
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

    public XParameter getXParameter(BlockType type, String blockKey, String key) {
        return map.get(new BlockId(type, blockKey).toString()).getXParameter(key);
    }

    public void removeAllBlocks() {
        structure.getRootElement().removeChildren(XBlock.BLOCK);
    }

    public Iterable<XBlock> getAllBlocks() {
        return map.values();
    }

    public Iterable getBlocks(BlockType type) {
        ArrayList<XBlock> list = new ArrayList();
        for (XBlock xblock : map.values()) {
            if (xblock.getType().equals(type)) {
                list.add(xblock);
            }
        }
        return list;
    }

    public XBlock getBlock(final BlockType type, final String key) {
        return map.get(new BlockId(type, key).toString());
    }

    public List<XBlock> readBlocks() throws IOException {
        List<Element> list = structure.getRootElement().getChildren(XBlock.BLOCK);
        List<XBlock> listBlock = new ArrayList(list.size());
        for (Element elt : list) {
            listBlock.add(new XBlock(elt));
        }
        return listBlock;
    }

    public List<XBlock> readBlocks(final BlockType type) {

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

    public void updateKey(String newKey, XBlock xblock) {
        map.remove(xblock.getKey());
        xblock.setKey(newKey);
        map.put(new BlockId(xblock.getType(), xblock.getKey()).toString(), xblock);
    }

    public HashMap<String, XBlock> createMap() throws Exception {
        HashMap<String, XBlock> lmap = new HashMap();
        for (XBlock xblock : readBlocks()) {
            lmap.put(new BlockId(xblock.getType(), xblock.getKey()).toString(), xblock);
        }
        return lmap;
    }

    public void addBlock(Content child) {
        XBlock block = (XBlock) child.detach();
        block.prepairForWriting();
        structure.getRootElement().addContent(block);
        map.put(new BlockId(block.getType(), block.getKey()).toString(), block);
    }
}

class BlockId {

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
