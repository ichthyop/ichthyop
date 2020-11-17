/* 
 * 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 * 
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2020
 * http://www.ird.fr
 * 
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr), Nicolas Barrier (nicolas.barrier@ird.fr)
 * Contributors (alphabetically sorted):
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothée BROCHIER,
 * Christophe HOURDIN, Mariem JELASSI, David KAPLAN, Fabrice LECORNU,
 * Christophe LETT, Christian MULLON, Carolina PARADA, Pierrick PENVEN,
 * Stephane POUS, Nathan PUTMAN.
 * 
 * Ichthyop is a free Java tool designed to study the effects of physical and
 * biological factors on ichthyoplankton dynamics. It incorporates the most
 * important processes involved in fish early life: spawning, movement, growth,
 * mortality and recruitment. The tool uses as input time series of velocity,
 * temperature and salinity fields archived from oceanic models such as NEMO,
 * ROMS, MARS or SYMPHONIE. It runs with a user-friendly graphic interface and
 * generates output files that can be post-processed easily using graphic and
 * statistical software. 
 * 
 * To cite Ichthyop, please refer to Lett et al. 2008
 * A Lagrangian Tool for Modelling Ichthyoplankton Dynamics
 * Environmental Modelling & Software 23, no. 9 (September 2008) 1210-1214
 * doi:10.1016/j.envsoft.2008.02.005
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation (version 3 of the License). For a full 
 * description, see the LICENSE file.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 * 
 */

package org.previmer.ichthyop.io;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.previmer.ichthyop.Version;

public class ConfigurationFile {

    private File file;
    private Document structure;
    private HashMap<String, XBlock> map;
    public final static String DESCRIPTION = "description";
    public final static String LONG_NAME = "long_name";
    public final static String VERSION = "version";
    public final static String DATE = "date";

    public ConfigurationFile(File file) {
        this.file = file;
    }

    public ConfigurationFile(InputStream stream) {
        this.file = null;
        try {
            SAXBuilder sxb = new SAXBuilder();
            Element racine = sxb.build(stream).getRootElement();
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

    public Version getVersion() {
        if (null != structure.getRootElement().getChild(VERSION)) {
            String number = structure.getRootElement().getChildTextNormalize(VERSION);
            String date = structure.getRootElement().getChild(VERSION).getAttributeValue(DATE);
            return new Version(number, date);
        } else {
            return Version.V30B;
        }
    }

    public void setVersion(Version version) {
        if (null == structure.getRootElement().getChild(VERSION)) {
            structure.getRootElement().addContent(new Element(VERSION));
        }
        structure.getRootElement().getChild(VERSION).setText(version.getNumber());
        structure.getRootElement().getChild(VERSION).setAttribute(DATE, version.getDate());
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
        org.jdom2.output.Format format = org.jdom2.output.Format.getPrettyFormat();
        // barrier.n: correct a bug related to Cp1252 encoding (windows).
        // should force encoding to UTF-8
        //format.setEncoding(System.getProperty("file.encoding"));
        XMLOutputter xmlOut = new XMLOutputter(format);
        xmlOut.output(structure, out);
    }

    public List<XParameter> getParameters(ParamType paramType) {
        List<XParameter> list = new ArrayList<>();
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

    public HashMap<String, XBlock> getMap() {
        return map;
    }

    public Iterable<XBlock> getBlocks(BlockType type) {
        ArrayList<XBlock> list = new ArrayList<>();
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

    public boolean containsBlock(final BlockType type, final String key) {
        return map.containsKey(new BlockId(type, key).toString());
    }

    public List<XBlock> readBlocks() throws IOException {
        List<Element> list = structure.getRootElement().getChildren(XBlock.BLOCK);
        List<XBlock> listBlock = new ArrayList<>(list.size());
        for (Element elt : list) {
            listBlock.add(new XBlock(elt));
        }
        return listBlock;
    }

    public List<XBlock> readBlocks(final BlockType type) {

        Filter filtre = new Filter() {

            @Override
            public boolean matches(Object obj) {
                if (!(obj instanceof Element)) {
                    return false;
                }
                Element element = (Element) obj;
                return element.getAttributeValue(XBlock.TYPE).equals(type.toString());
            }

            @Override
            public List filter(List list) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Object filter(Object o) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Filter negate() {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Filter or(Filter filter) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Filter and(Filter filter) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }

            @Override
            public Filter refine(Filter filter) {
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        };
        List<XBlock> list = new ArrayList();
        for (Object elt : structure.getRootElement().getContent(filtre)) {
            list.add(new XBlock(type, (Element) elt));
        }
        return list;
    }

    public void updateBlockKey(String newKey, XBlock xblock) {
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

    public void removeBlock(final BlockType type, final String key) {
        map.remove(key);
        structure.getRootElement().removeContent(getBlock(type, key));
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
