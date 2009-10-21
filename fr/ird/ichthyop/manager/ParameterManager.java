package fr.ird.ichthyop.manager;

import fr.ird.ichthyop.io.BlockType;
import fr.ird.ichthyop.arch.IParameterManager;
import fr.ird.ichthyop.io.ParamType;
import fr.ird.ichthyop.io.XBlock;
import fr.ird.ichthyop.io.XParameter;
import java.io.File;
import java.util.ArrayList;
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

        if (!parameters.containsKey(paramType)) {
            List<XParameter> list = new ArrayList();
            for (BlockType type : BlockType.values()) {
                for (XBlock xblock : getBlocks(type)) {
                    if (xblock.isEnabled()) {
                        list.addAll(xblock.getParameters(paramType));
                    }
                }
            }
            parameters.put(paramType, list);
        }
        return parameters.get(paramType);
    }

    public String getParameter(String blockName, String key) {

        XParameter xparam = getBlock(BlockType.OPTION, blockName).getParameter(key);
        if (xparam != null) {
            return xparam.getValue();
        } else {
            return "";
        }
    }

    public XBlock getBlock(BlockType type, String key) {
        return cfgFile.getBlock(type, key);
    }

    public List<XBlock> getBlocks(BlockType type) {
        return cfgFile.getBlocks(type);
    }

    private class ConfigurationFile {

        private File file;
        private Document structure;

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
                //structure.createMaps();
            } catch (Exception e) {
                Logger.getLogger(ConfigurationFile.class.getName()).log(Level.SEVERE, null, e);
            }
        }

        private XBlock getBlock(final BlockType type, final String key) {
            List<XBlock> list = new ArrayList();
            for (XBlock block : getBlocks(type)) {
                if (block.getKey().matches(key)) {
                    list.add(block);
                }
            }
            if (list.size() > 0 && list.size() < 2) {
                return list.get(0);
            } else {
                return null;
            }
        }

        private List<XBlock> getBlocks(final BlockType type) {
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
    }
}
