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
}
