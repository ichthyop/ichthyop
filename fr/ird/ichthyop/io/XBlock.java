/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.io;

import fr.ird.ichthyop.TypeBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom.Element;
import org.jdom.filter.Filter;

/**
 *
 * @author pverley
 */
public class XBlock extends org.jdom.Element {

    private final static String BLOCK = "block";
    final public static String KEY = "key";
    final public static String ENABLED = "enabled";
    final public static String TYPE = "type";
    public final static String PARAMETERS = "parameters";
    private final TypeBlock block_type;

    public XBlock(TypeBlock block_type, Element element) {
        super(BLOCK);
        this.block_type = block_type;
        this.setAttribute(TYPE, block_type.toString());
        if (element != null) {
            addContent(element.cloneContent());
        }
    }

    public TypeBlock getType() {
        return block_type;
    }

    public String getKey() {
        return getChildTextNormalize(KEY);
    }

    public boolean isEnabled() {
        return Boolean.valueOf(getChildTextNormalize(ENABLED));
    }

    public ArrayList<XParameter> getParameters() {
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
        if (searchResult != null && searchResult.size() < 2) {
            return new XParameter((Element) searchResult.get(0));
        } else {
            return null;
        }
    }
}
