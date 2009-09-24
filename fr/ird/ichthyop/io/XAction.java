/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.ird.ichthyop.io;

import fr.ird.ichthyop.TypeBlock;
import org.jdom.Element;

/**
 *
 * @author pverley
 */
public class XAction extends XBlock {

    private static final String CLASS_NAME = "class_name";

    XAction(Element xaction) {
        super(TypeBlock.ACTION, xaction);
        if (xaction != null) {
            addContent(xaction.cloneContent());
        }
    }

    public String getClassName() {
        return this.getChildTextNormalize(CLASS_NAME);
    }

    public Class getActionClass() throws ClassNotFoundException {
        return Class.forName(getClassName());
    }
}
