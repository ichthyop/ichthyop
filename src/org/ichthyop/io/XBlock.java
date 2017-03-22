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

package org.ichthyop.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Element;

/**
 *
 * @author pverley
 */
@Deprecated
public class XBlock extends org.jdom2.Element implements Comparable<XBlock> {

    public final static String BLOCK = "block";
    final public static String KEY = "key";
    final public static String TREEPATH = "tree_path";
    final public static String ENABLED = "enabled";
    final public static String TYPE = "type";
    public final static String PARAMETERS = "parameters";
    public final static String DESCRIPTION = "description";
    private final BlockType block_type;
    private HashMap<String, XParameter> map;
    private List<String> sortedKey;

    public XBlock(Element element) throws IOException {
        super(BLOCK);
        this.block_type = getType(element);
        if (null == block_type) {
            throw new IllegalArgumentException("Unknow type for block " + element.getChildText(TREEPATH));
        }
        this.setAttribute(TYPE, block_type.toString());
        if (element != null) {
            addContent(element.cloneContent());
            map = createMap();
        }
    }

    public XBlock(BlockType block_type, Element element) {
        super(BLOCK);
        this.block_type = block_type;
        this.setAttribute(TYPE, block_type.toString());
        if (element != null) {
            addContent(element.cloneContent());
            map = createMap();
        }
    }

    public BlockType getType() {
        return block_type;
    }

    private BlockType getType(Element element) {
        if (null != element && null != element.getAttribute(TYPE)) {
            return BlockType.getType(element.getAttribute(TYPE).getValue());
        } else {
            return BlockType.OPTION;
        }
    }

    public String getKey() {
        return getChildTextNormalize(KEY);
    }

    void setKey(String key) {
        getChild(KEY).setText(key);
    }

    public String getTreePath() {
        return getChildTextNormalize(TREEPATH);
    }

    public void setTreePath(String treePath) {
        getChild(TREEPATH).setText(treePath);
    }

    public boolean isEnabled() {

        if (null != getChild(ENABLED)) {
            return Boolean.valueOf(getChildTextNormalize(ENABLED));
        } else {
            return true;
        }
    }

    public boolean canBeDeactivated() {
        return null != getChild(ENABLED);
    }

    public void setEnabled(boolean enabled) {
        if (null == getChild(ENABLED)) {
            addContent(new Element(ENABLED));
        }
        this.getChild(ENABLED).setText(String.valueOf(enabled));
    }

    public String getDescription() {
        if (null != getChild(DESCRIPTION)) {
            return getChildTextNormalize(DESCRIPTION);
        } else {
            return null;
        }
    }

    private HashMap<String, XParameter> createMap() {
        HashMap<String, XParameter> lmap = new HashMap();
        sortedKey = new ArrayList();
        for (XParameter xparam : readParameters()) {
            lmap.put(xparam.getKey(), xparam);
            sortedKey.add(xparam.getKey());
        }
        return lmap;
    }

    public void addXParameter(XParameter xparam) {
        if (!map.containsKey(xparam.getKey())) {
            getChild(PARAMETERS).addContent(xparam.detach());
            map = createMap();
        }
    }

    private ArrayList<XParameter> readParameters() {
        ArrayList<XParameter> list = new ArrayList();
        try {
            for (Object elt : getChild(PARAMETERS).getChildren(XParameter.PARAMETER)) {
                list.add(new XParameter((Element) elt));
            }
        } catch (java.lang.NullPointerException ex) {
            Logger.getLogger(XBlock.class.getName()).log(Level.SEVERE, null, ex);
        }
        return list;
    }

    public XParameter getXParameter(final String key) {
        return map.get(key);
    }

    public Collection<XParameter> getXParameters() {
        List<XParameter> list = new ArrayList(map.values().size());
        Iterator<String> it = sortedKey.iterator();
        while (it.hasNext()) {
            list.add(map.get(it.next()));
        }
        return list;
    }

    @Override
    public int compareTo(XBlock block) {
        return getKey().compareTo(block.getKey());
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder("Block: ");
        str.append(getKey());
        str.append(" (");
        str.append(getType());
        str.append(")\n");
        for (XParameter param : getXParameters()) {
            str.append("  ");
            str.append(param.getKey());
            str.append("::");
            str.append(param.getValue());
            str.append("\n");
        }
        for (XParameter param : getXParameters()) {
            str.append("  ~");
            str.append(param.getKey());
            str.append("::");
            str.append(param.getValue());
            str.append("\n");
        }
        str.append("\n");
        return str.toString();
    }

    public void prepairForWriting() {
        getChild(PARAMETERS).removeContent();
        for (XParameter param : getXParameters()) {
            getChild(PARAMETERS).addContent(param);
        }
    }
}
