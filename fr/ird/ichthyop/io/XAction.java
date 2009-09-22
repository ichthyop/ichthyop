/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.io;

import fr.ird.ichthyop.io.ICFile.ICStructure;
import java.util.ArrayList;
import java.util.List;
import org.jdom.Element;
import org.jdom.filter.Filter;

/**
 *
 * @author pverley
 */
public class XAction extends org.jdom.Element {

    private static final String ACTION = "action";
    final public static String KEY = "key";
    final public static String ENABLED = "enabled";

    XAction(Element xaction) {
        super(ACTION);
        if (xaction != null) {
            addContent(xaction.cloneContent());
        }
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
            for (Object elt : getChild(ICStructure.PARAMETERS).getChildren(XParameter.PARAMETER)) {
                list.add(new XParameter((Element) elt));
            }
        } catch (java.lang.NullPointerException ex) {}
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
        List searchResult = getChild(ICStructure.PARAMETERS).getContent(filtre);
        if (searchResult != null && searchResult.size() < 2) {
            return new XParameter((Element) searchResult.get(0));
        } else {
            return null;
        }
    }
}
