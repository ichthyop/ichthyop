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
 * Gwendoline ANDRES, Sylvain BONHOMMEAU, Bruno BLANKE, Timothee BROCHIER,
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
public class XBlock extends org.jdom2.Element implements Comparable<XBlock> {

    /**
     *
     */
    private static final long serialVersionUID = 5504576033373599230L;
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
    private int nbHiddenParameters;

    public XBlock(Element element) throws IOException {
        super(BLOCK);
        this.block_type = getType(element);
        if (null == block_type) {
            throw new IllegalArgumentException("Unknow type for block " + element.getChildText(TREEPATH));
        }
        this.setAttribute(TYPE, block_type.toString());
        nbHiddenParameters = 0;
        if (element != null) {
            addContent(element.cloneContent());
            map = createMap();
        }
    }

    public XBlock(BlockType block_type, Element element) {
        super(BLOCK);
        this.block_type = block_type;
        this.setAttribute(TYPE, block_type.toString());
        nbHiddenParameters = 0;
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

    public int getNbHiddenParameters() {
        return nbHiddenParameters;
    }

    private HashMap<String, XParameter> createMap() {
        HashMap<String, XParameter> lmap = new HashMap<>();
        sortedKey = new ArrayList<>();
        for (XParameter xparam : readParameters()) {
            lmap.put(xparam.getKey(), xparam);
            sortedKey.add(xparam.getKey());
            if (xparam.isHidden()) {
                nbHiddenParameters++;
            }
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
        ArrayList<XParameter> list = new ArrayList<>();
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
        List<XParameter> list = new ArrayList<>(map.values().size());
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
            str.append(param.getValues());
            str.append("\n");
        }
        for (XParameter param : getXParameters()) {
            str.append("  ~");
            str.append(param.getKey());
            str.append("::");
            str.append(param.getValues());
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

    /*public List<XParameter> getParameters(final ParamType type) {
    Filter filtre = new Filter() {

    public boolean matches(Object obj) {
    if (!(obj instanceof Element)) {
    return false;
    }
    Element element = (Element) obj;
    if (element.getAttribute(XParameter.TYPE) != null && element.getAttributeValue(XParameter.TYPE).matches(type.toString())) {
    return true;
    } else {
    return false;
    }
    }
    };
    List<XParameter> list = new ArrayList();
    for (Object elt : getChild(PARAMETERS).getContent(filtre)) {
    list.add(new XParameter((Element) elt));
    }
    return list;
    }

    public XParameter getParameter(final String key) {
    Filter filtre = new Filter() {

    public boolean matches(Object obj) {
    if (!(obj instanceof Element)) {
    return false;
    }
    Element element = (Element) obj;
    if (element.getChildTextNormalize(XParameter.KEY).matches(key)) {
    return true;
    } else {
    return false;
    }
    }
    };
    List searchResult = getChild(PARAMETERS).getContent(filtre);
    if (searchResult != null && searchResult.size() > 0 && searchResult.size() < 2) {
    return new XParameter((Element) searchResult.get(0));
    } else {
    return null;
    }

    }*/
    /*
    private class XParameterComparator implements Comparator<XParameter> {

        @Override
        public int compare(XParameter o1, XParameter o2) {
            if (o1.isHidden()) {
                if (o2.isHidden()) {
                    return o1.getKey().compareToIgnoreCase(o2.getKey());
                } else {
                    return 1;
                }
            } else {
                if (o2.isHidden()) {
                    return -1;
                } else {
                    return o1.getKey().compareToIgnoreCase(o2.getKey());
                }
            }
        }
    }
    */
}
