/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package fr.ird.ichthyop;

import org.jdom.Element;

/**
 *
 * @author pverley
 */
public class XParameter extends org.jdom.Element {

    final public static String PARAMETER = "parameter";
    final public static String KEY = "key";
    final public static String VALUE = "value";

    XParameter(Element xparameter) {
        super(PARAMETER);
        if (xparameter != null) {
            addContent(xparameter.cloneContent());
        }
    }

    public String getKey() {
        return getChildTextNormalize(KEY);
    }

    @Override
    public String getValue() {
        return getChildTextNormalize(VALUE);
    }

}
