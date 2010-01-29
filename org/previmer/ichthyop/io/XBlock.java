/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;
import org.jdom.filter.Filter;

/**
 *
 * @author pverley
 */
public class XBlock extends org.jdom.Element implements Comparable<XBlock> {

    public final static String BLOCK = "block";
    final public static String KEY = "key";
    final public static String TREEPATH = "tree_path";
    final public static String ENABLED = "enabled";
    final public static String TYPE = "type";
    public final static String PARAMETERS = "parameters";
    public final static String DESCRIPTION = "description";
    private final BlockType block_type;
    private HashMap<String, XParameter> map;
    private int nbHiddenParameters;

    public XBlock(Element element) {
        super(BLOCK);
        this.block_type = getType(element);
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

    public String getTreePath() {
        return getChildTextNormalize(TREEPATH);
    }

    public boolean isEnabled() {

        if (null != getChild(ENABLED)) {
            return Boolean.valueOf(getChildTextNormalize(ENABLED));
        } else {
            return true;
        }
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
            return "No description available";
        }
    }

    public int getNbHiddenParameters() {
        return nbHiddenParameters;
    }

    private HashMap<String, XParameter> createMap() {
        HashMap<String, XParameter> lmap = new HashMap();
        for (XParameter xparam : readParameters()) {
            lmap.put(xparam.getKey(), xparam);
            if (xparam.isHidden()) {
                nbHiddenParameters++;
            }
        }
        return lmap;
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
        List<XParameter> list = new ArrayList(map.values());
        Collections.sort(list, new XParameterComparator());
        return list;
    }

    public int compareTo(XBlock block) {
        return getKey().compareTo(block.getKey());
    }

    @Override
    public String toString() {
        StringBuffer str = new StringBuffer("Block: ");
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

    private class XParameterComparator implements Comparator<XParameter> {

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
}
