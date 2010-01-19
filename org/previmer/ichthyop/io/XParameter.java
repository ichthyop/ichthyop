/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.previmer.ichthyop.io;

import java.util.List;
import org.jdom.Element;

/**
 *
 * @author pverley
 */
public class XParameter extends org.jdom.Element {

    final public static String PARAMETER = "parameter";
    final public static String KEY = "key";
    final public static String VALUE = "value";
    final public static String TYPE = "type";
    final public static String HIDDEN = "hidden";
    private int index;
    private final ParamType param_type;
    private List<Element> values;
    private boolean hidden;

    XParameter(Element xparameter) {
        super(PARAMETER);
        param_type = getType(xparameter);
        hidden = isHidden(xparameter);
        if (xparameter != null) {
            addContent(xparameter.cloneContent());
            values = getChildren(VALUE);
        }
        reset();
    }

    public String getKey() {
        return getChildTextNormalize(KEY);
    }

    public ParamType getType() {
        return param_type;
    }

    public boolean isHidden() {
        return hidden;
    }

    private boolean isHidden(Element element) {
        if (null != element && null != element.getAttribute(HIDDEN)) {
            return Boolean.valueOf(element.getAttribute(HIDDEN).getValue());
        } else {
            return false;
        }
    }

    private ParamType getType(Element element) {
        if (null != element && null != element.getAttribute(TYPE)) {
            return ParamType.getType(element.getAttribute(TYPE).getValue());
        } else {
            return ParamType.SINGLE;
        }
    }

    @Override
    public String getValue() {
        return values.get(index).getTextNormalize();
    }

    public String getValues() {
        if (getType().equals(ParamType.SERIAL) && getLength() > 1) {
            StringBuffer strV = new StringBuffer();
            for (Element elt : values) {
                strV.append('"');
                strV.append(elt.getTextNormalize());
                strV.append("\" ");
            }
            return strV.toString().trim();
        } else {
            return getValue();
        }
    }

    public boolean isSerial() {
        return getType().equals(ParamType.SERIAL);
    }

    public int getLength() {
        return getChildren(VALUE).size();
    }

    public boolean hasNext() {
        return index < (getLength() - 1);
    }

    public int index() {
        return index;
    }

    public void reset() {
        index = 0;
    }

    public void increment() {
        index++;
    }
}
