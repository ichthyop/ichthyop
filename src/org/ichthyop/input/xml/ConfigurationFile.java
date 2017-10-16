/* 
 * ICHTHYOP, a Lagrangian tool for simulating ichthyoplankton dynamics
 * http://www.ichthyop.org
 *
 * Copyright (C) IRD (Institut de Recherce pour le Developpement) 2006-2016
 * http://www.ird.fr
 *
 * Main developper: Philippe VERLEY (philippe.verley@ird.fr)
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
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software. You can use, modify
 * and/ or redistribute the software under the terms of the CeCILL-B license as
 * circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify
 * and redistribute granted by the license, users are provided only with a
 * limited warranty and the software's author, the holder of the economic
 * rights, and the successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with
 * loading, using, modifying and/or developing or reproducing the software by
 * the user in light of its specific status of free software, that may mean that
 * it is complicated to manipulate, and that also therefore means that it is
 * reserved for developers and experienced professionals having in-depth
 * computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling
 * the security of their systems and/or data to be ensured and, more generally,
 * to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package org.ichthyop.input.xml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.logging.Level;
import org.ichthyop.IchthyopLinker;
import org.ichthyop.Template;
import org.jdom2.Content;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.filter.Filter;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import org.ichthyop.Version;
import org.ichthyop.util.StringUtil;
import org.jdom2.JDOMException;

@Deprecated
public class ConfigurationFile extends IchthyopLinker {

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

    public ConfigurationFile(URL url) {
        try {
            SAXBuilder sxb = new SAXBuilder();
            Element racine = sxb.build(url).getRootElement();
            racine.detach();
            structure = new Document(racine);
            map = createMap();
        } catch (Exception ex) {
            getLogger().log(Level.SEVERE, null, ex);
        }
    }

    public File getFile() {
        return file;
    }

    public void load() throws Exception {

        /* Make sure file exists */
        if (!file.isFile()) {
            throw new FileNotFoundException("Configuration file " + file.getPath() + " not found.");
        }
        if (!file.canRead()) {
            throw new IOException("Configuration file " + file.getPath() + " cannot be read");
        }
        /* Make sure file is valid */
        if (isValidXML(file)) {
            if (!isValidConfigFile(file)) {
                throw new IOException(file.getName() + " is not a valid Ichthyop configuration file.");
            }
        }

        SAXBuilder sxb = new SAXBuilder();
        Element racine = sxb.build(file).getRootElement();
        racine.detach();
        structure = new Document(racine);
        map = createMap();
    }

    public void upgrade() {
        try {
            if (getVersion().priorTo(Version.V31)) {
                u30bTo31();

            }
            if (getVersion().priorTo(Version.V32)) {
                u31To32();
            }
            if (getVersion().priorTo(Version.V33)) {
                u32To33();
            }
        } catch (Exception ex) {
            error("Error while upgrading the XML configuration file", ex);
        }
    }

    private boolean isValidXML(File file) throws IOException {
        try {
            new SAXBuilder().build(file).getRootElement();
        } catch (JDOMException ex) {
            IOException ioex = new IOException("Error occured reading " + file.getName() + " \n" + ex.getMessage(), ex);
            ioex.setStackTrace(ex.getStackTrace());
            throw ioex;
        }
        return true;
    }

    private boolean isValidConfigFile(File file) {
        try {
            return new SAXBuilder().build(file).getRootElement().getName().equals("icstructure");

        } catch (JDOMException | IOException ex) {
            return false;
        }
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

    public void setTitle(String longName) {
        if (null == structure.getRootElement().getChild(LONG_NAME)) {
            structure.getRootElement().addContent(new Element(LONG_NAME));
        }
        structure.getRootElement().getChild(LONG_NAME).setText(longName);
    }

    public String getTitle() {
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
        format.setEncoding(System.getProperty("file.encoding"));
        XMLOutputter xmlOut = new XMLOutputter(format);
        xmlOut.output(structure, out);
    }

    public List<XParameter> getParameters() {
        List<XParameter> list = new ArrayList();
        for (XBlock xblock : map.values()) {
            if (xblock.isEnabled()) {
                for (XParameter xparam : xblock.getXParameters()) {
                    list.add(xparam);
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

    public Iterable<XBlock> getBlocks(BlockType type) {
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

    public boolean containsBlock(final BlockType type, final String key) {
        return map.containsKey(new BlockId(type, key).toString());
    }

    public List<XBlock> readBlocks() throws IOException {
        List<Element> list = structure.getRootElement().getChildren(XBlock.BLOCK);
        List<XBlock> listBlock = new ArrayList(list.size());
        for (Element elt : list) {
            listBlock.add(new XBlock(elt));
        }
        return listBlock;
    }

    public HashMap<String, String> toProperties(boolean extended) throws IOException {
        HashMap<String, String> parameters = new LinkedHashMap();

        parameters.put("configuration.title", getTitle());
        parameters.put("configuration.description", StringUtil.removeQuotes(getDescription()));
        parameters.put("configuration.version", getVersion().toString());
        parameters.put("configuration.subsets", listBlocks());
        for (XBlock block : readBlocks()) {
            String bkey = block.getKey().toLowerCase();
            if (block.getType() != BlockType.OPTION) {
                parameters.put(bkey + ".enabled", String.valueOf(block.isEnabled()));
                parameters.put(bkey + ".type", block.getType().toString());
            }
            if (extended) {
                parameters.put(bkey + ".description", StringUtil.removeQuotes(block.getDescription()));
                parameters.put(bkey + ".treepath", block.getTreePath());
            }
            block.getXParameters().forEach((parameter) -> {
                StringBuilder key;
                String pkey = parameter.getKey().toLowerCase();
                if (extended) {
                    key = new StringBuilder(bkey).append(".").append(pkey).append(".longname");
                    parameters.put(key.toString(), parameter.getLongName());
                    key = new StringBuilder(bkey).append(".").append(pkey).append(".format");
                    parameters.put(key.toString(), StringUtil.nullify(parameter.getFormat().toString()));
                    key = new StringBuilder(bkey).append(".").append(pkey).append(".description");
                    parameters.put(key.toString(), StringUtil.removeQuotes(StringUtil.nullify(parameter.getDescription())));
                    if (parameter.getAcceptedValues().length > 0) {
                        key = new StringBuilder(bkey).append(".").append(pkey).append(".accepted");
                        parameters.put(key.toString(), handleArray(parameter.getAcceptedValues()));
                    }
                }
                key = new StringBuilder(bkey).append(".").append(pkey);
                parameters.put(key.toString(), handleArray(StringUtil.nullify(parameter.getValue())));
                key = new StringBuilder(bkey).append(".parameters");
                parameters.put(key.toString(), listParameters(block));
            });
        }

        return parameters;
    }

    private String listBlocks() throws IOException {
        List<String> list = new ArrayList();
        for (XBlock block : readBlocks()) {
            list.add(block.getKey().toLowerCase());
        }
        return handleArray(list.toArray(new String[list.size()]));
    }

    private String listParameters(XBlock block) {
        List<String> list = new ArrayList();
        for (XParameter param : block.getXParameters()) {
            list.add(param.getKey());
        }
        return handleArray(list.toArray(new String[list.size()]));
    }

    private String handleArray(String[] values) {

        String[] array = new String[values.length];
        for (int k = 0; k < array.length; k++) {
            array[k] = StringUtil.isNotString(values[k]) ? values[k] : "\"" + values[k] + "\"";
        }
        return Arrays.toString(array);
    }

    private String handleArray(String value) {
        String[] tokens = value.split("\"");
        List<String> list = new ArrayList();
        for (String token : tokens) {
            String str = token.trim();
            if (!str.isEmpty()) {
                list.add(StringUtil.isNotString(str) ? str : "\"" + str + "\"");
            }
        }
        if (list.size() > 1) {
            return Arrays.toString(list.toArray(new String[list.size()]));
        } else {
            return value;
        }
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

    private HashMap<String, XBlock> createMap() throws Exception {
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

    /*
     * Upgrade the 3.2 configuration file to 3.3
     */
    private void u32To33() throws Exception {

        ConfigurationFile cfg33 = new ConfigurationFile(Template.getTemplateURL("cfg-generic_3.3.xml"));
        /*
         * Update linear growth 
         */
        if (null != getBlock(BlockType.ACTION, "action.growth")) {
            if (null == getBlock(BlockType.ACTION, "action.growth").getXParameter("stage_tags")) {
                getBlock(BlockType.ACTION, "action.growth").addXParameter(cfg33.getXParameter(BlockType.ACTION, "action.growth", "stage_tags"));
            }
            if (null == getBlock(BlockType.ACTION, "action.growth").getXParameter("stage_thresholds")) {
                getBlock(BlockType.ACTION, "action.growth").addXParameter(cfg33.getXParameter(BlockType.ACTION, "action.growth", "stage_thresholds"));
            }
        }
        
        /*
         * Delete generic larva stages definition
         */
        if (null != getBlock(BlockType.OPTION, "app.particle_length")) {
            removeBlock(BlockType.ACTION, "app.particle_length");
        }

        /*
         * Update version number and date
         */
        setVersion(Version.V33);
        StringBuilder str = new StringBuilder(getDescription());
        str.append("  --@@@--  ");
        str.append((new GregorianCalendar()).getTime());
        str.append(" File updated to version ");
        str.append(Version.V33);
        str.append('.');
        setDescription(str.toString());
    }

    /*
     * Upgrade the 3.1 configuration file to 3.2
     */
    private void u31To32() throws Exception {
        // cfg-generic_3.3.xml is not a mistake
        ConfigurationFile cfg32 = new ConfigurationFile(Template.getTemplateURL("cfg-generic_3.3.xml"));
        String treepath, newTreepath;
        /*
         * Update block action.lethal_temp
         */
        if (null != getBlock(BlockType.ACTION, "action.lethal_temp")) {
            treepath = getBlock(BlockType.ACTION, "action.lethal_temp").getTreePath();
            newTreepath = treepath.startsWith("Advanced")
                    ? "Advanced/Biology/Lethal temperatures"
                    : "Biology/Lethal temperatures";
            removeBlock(BlockType.ACTION, "action.lethal_temp");
            addBlock(cfg32.getBlock(BlockType.ACTION, "action.lethal_temp"));
            getBlock(BlockType.ACTION, "action.lethal_temp").setTreePath(newTreepath);
        }
        /*
         * Update version number and date
         */
        setVersion(Version.V32);
        StringBuilder str = new StringBuilder(getDescription());
        str.append("  --@@@--  ");
        str.append((new GregorianCalendar()).getTime());
        str.append(" File updated to version ");
        str.append(Version.V32);
        str.append('.');
        setDescription(str.toString());
    }

    /*
     * Upgrade the 3.0b configuration file to 3.1
     */
    private void u30bTo31() throws Exception {
        ConfigurationFile cfg31 = new ConfigurationFile(Template.getTemplateURL("cfg-generic_3.1.xml"));
        String treepath, newTreepath;
        /*
         * Add the density_file parameter in the action.buoyancy block
         */
        if (null != getBlock(BlockType.ACTION, "action.buoyancy")) {
            if (null == getBlock(BlockType.ACTION, "action.buoyancy").getXParameter("density_file")) {
                getBlock(BlockType.ACTION, "action.buoyancy").addXParameter(cfg31.getXParameter(BlockType.ACTION, "action.buoyancy", "density_file"));
            }
        }

        /*
         * Update the recruitment in zone block
         */
        if (null != getBlock(BlockType.ACTION, "action.recruitment")) {
            getXParameter(BlockType.ACTION, "action.recruitment", "class_name").setValue(org.ichthyop.action.RecruitmentZoneAction.class.getCanonicalName());
            treepath = getBlock(BlockType.ACTION, "action.recruitment").getTreePath();
            newTreepath = treepath.startsWith("Advanced")
                    ? "Advanced/Biology/Recruitment/In zones"
                    : "Biology/Recruitment/In zones";
            getBlock(BlockType.ACTION, "action.recruitment").setTreePath(newTreepath);
            updateBlockKey("action.recruitment.zone", getBlock(BlockType.ACTION, "action.recruitment"));
        }
        /*
         * Add the recruitment in stain block
         */
        if (!containsBlock(BlockType.ACTION, "action.recruitment.stain")) {
            addBlock(cfg31.getBlock(BlockType.ACTION, "action.recruitment.stain").detach());
            treepath = getBlock(BlockType.ACTION, "action.recruitment.zone").getTreePath();
            newTreepath = treepath.startsWith("Advanced")
                    ? "Advanced/Biology/Recruitment/In stain"
                    : "Biology/Recruitment/In stain";
            getBlock(BlockType.ACTION, "action.recruitment.stain").setTreePath(newTreepath);
        }
        /*
         * Add the coastline behavior block
         */
        if (!containsBlock(BlockType.OPTION, "app.transport")) {
            addBlock(cfg31.getBlock(BlockType.OPTION, "app.transport").detach());
            treepath = getBlock(BlockType.ACTION, "action.advection").getTreePath();
            newTreepath = treepath.startsWith("Advanced")
                    ? "Advanced/Transport/General"
                    : "Transport/General";
            getBlock(BlockType.OPTION, "app.transport").setTreePath(newTreepath);
        }
        /*
         * Update MARS OpendDAP URL
         */
        if (null != getBlock(BlockType.DATASET, "dataset.mars_2d_opendap")) {
            getXParameter(BlockType.DATASET, "dataset.mars_2d_opendap", "opendap_url").setValue("http://tds1.ifremer.fr/thredds/dodsC/PREVIMER-MANGA4000-MARS3DF1-FOR_FULL_TIME_SERIE");
        }
        if (null != getBlock(BlockType.DATASET, "dataset.mars_3d_opendap")) {
            getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "opendap_url").setValue("http://tds1.ifremer.fr/thredds/dodsC/PREVIMER-MANGA4000-MARS3DF1-FOR_FULL_TIME_SERIE");
        }
        /*
         * Update MARS Generelized Sigma parameters
         */
        if (null != getBlock(BlockType.DATASET, "dataset.mars_3d")) {
            if (null == getBlock(BlockType.DATASET, "dataset.mars_3d").getXParameter("field_var_hc")) {
                getBlock(BlockType.DATASET, "dataset.mars_3d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d", "field_var_hc"));
            }
            if (null == getBlock(BlockType.DATASET, "dataset.mars_3d").getXParameter("field_var_a")) {
                getBlock(BlockType.DATASET, "dataset.mars_3d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d", "field_var_a"));
            }
            if (null == getBlock(BlockType.DATASET, "dataset.mars_3d").getXParameter("field_var_b")) {
                getBlock(BlockType.DATASET, "dataset.mars_3d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d", "field_var_b"));
            }
        }
        if (null != getBlock(BlockType.DATASET, "dataset.mars_3d_opendap")) {
            if (null == getBlock(BlockType.DATASET, "dataset.mars_3d_opendap").getXParameter("field_var_hc")) {
                getBlock(BlockType.DATASET, "dataset.mars_3d_opendap").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "field_var_hc"));
            }
            if (null == getBlock(BlockType.DATASET, "dataset.mars_3d_opendap").getXParameter("field_var_a")) {
                getBlock(BlockType.DATASET, "dataset.mars_3d_opendap").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "field_var_a"));
            }
            if (null == getBlock(BlockType.DATASET, "dataset.mars_3d_opendap").getXParameter("field_var_b")) {
                getBlock(BlockType.DATASET, "dataset.mars_3d_opendap").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.mars_3d_opendap", "field_var_b"));
            }
        }
        /*
         * Update OPA NEMO parameters
         */
        if (null != getBlock(BlockType.DATASET, "dataset.nemo")) {
            if (null == getBlock(BlockType.DATASET, "dataset.nemo").getXParameter("field_var_e3u")) {
                getBlock(BlockType.DATASET, "dataset.nemo").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.nemo", "field_var_e3u"));
            }
            if (null == getBlock(BlockType.DATASET, "dataset.nemo").getXParameter("field_var_e3v")) {
                getBlock(BlockType.DATASET, "dataset.nemo").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.nemo", "field_var_e3v"));
            }
        }
        /*
         * Add skip_sorting option in Dataset blocks
         */
        for (XBlock xblock : getBlocks(BlockType.DATASET)) {
            if (null == xblock.getXParameter("skip_sorting")) {
                if (null != cfg31.getXParameter(BlockType.DATASET, xblock.getKey(), "skip_sorting")) {
                    xblock.addXParameter(cfg31.getXParameter(BlockType.DATASET, xblock.getKey(), "skip_sorting"));
                }
            }
        }
        /*
         * Fix lethal_temperature_larva value 12.0 instead of 12.O  
         */
        if (null != getBlock(BlockType.ACTION, "action.lethal_temp")) {
            try {
                float f = Float.valueOf(getXParameter(BlockType.ACTION, "action.lethal_temp", "lethal_temperature_larva").getValue());
            } catch (NumberFormatException ex) {
                getXParameter(BlockType.ACTION, "action.lethal_temp", "lethal_temperature_larva").setValue("12.0");
            }
        }
        /*
         * Add grid_file parameter in ROMS configuration
         */
        if (null != getBlock(BlockType.DATASET, "dataset.roms_2d")) {
            if (null == getBlock(BlockType.DATASET, "dataset.roms_2d").getXParameter("grid_file")) {
                getBlock(BlockType.DATASET, "dataset.roms_2d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.roms_2d", "grid_file"));
            }
        }
        if (null != getBlock(BlockType.DATASET, "dataset.roms_3d")) {
            if (null == getBlock(BlockType.DATASET, "dataset.roms_3d").getXParameter("grid_file")) {
                getBlock(BlockType.DATASET, "dataset.roms_3d").addXParameter(cfg31.getXParameter(BlockType.DATASET, "dataset.roms_3d", "grid_file"));
            }
        }
        /*
         * Update version number and date
         */
        setVersion(Version.V31);
        StringBuilder str = new StringBuilder(getDescription());
        str.append("  --@@@--  ");
        str.append((new GregorianCalendar()).getTime());
        str.append(" File updated to version ");
        str.append(Version.V31);
        str.append('.');
        setDescription(str.toString());
    }
}

class BlockId {

    private final BlockType blockType;
    private final String blockKey;

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
        StringBuilder id = new StringBuilder(getBlockType().toString());
        id.append('/');
        id.append(getBlockKey());
        return id.toString();
    }
}
